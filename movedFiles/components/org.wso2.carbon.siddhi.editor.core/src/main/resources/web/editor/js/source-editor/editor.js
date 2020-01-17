/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
/*
 * This module contains the integration code segment of Siddhi editor.
 * This will set the options of ACE editor, attach client side parser and attach SiddhiCompletion Engine with the editor
 */
define(["ace/ace", "jquery", "./constants", "./utils", "./completion-engine", "./token-tooltip",
        "ace/ext/language_tools", "./debug-rest-client", "log", 'ace/range', 'lodash', 'utils'],
    function (ace, $, constants, utils, CompletionEngine, aceTokenTooltip
              , aceExtLangTools, DebugRESTClient, log, AceRange, _, Utils) {

        "use strict";   // JS strict mode

        /*
         * Map for completion list styles
         * Update this map to update the styles applied to the completion list popup items
         */
        var completionTypeToStyleMap = {};
        completionTypeToStyleMap[constants.SNIPPETS] = "font-style: italic;";

        /*
         * Generating the displayNameToStyleMap from completionTypeToStyleMap
         * This is done to support defining completion popup styles using the completion type name rather than the display name
         */
        var displayNameToStyleMap = {};
        for (var completionType in completionTypeToStyleMap) {
            if (completionTypeToStyleMap.hasOwnProperty(completionType)) {
                displayNameToStyleMap[constants.typeToDisplayNameMap[completionType]] =
                    completionTypeToStyleMap[completionType];
            }
        }

        /*
         * Loading meta data for the completion engine from the server
         */
        CompletionEngine.loadMetaData();

        /**
         * Siddhi Editor prototype constructor
         *
         * @constructor
         * @param {Object} config The configuration object to be used in the initialization
         */
        function SiddhiEditor(config) {
            var self = this;
            var aceEditor = ace.edit(config.divID);                // Setting the DivID of the Editor .. Could be <pre> or <div> tags
            aceEditor.$blockScrolling = Infinity;
            self.realTimeValidation = config.realTimeValidation;
            new aceTokenTooltip.TokenTooltip(aceEditor);

            /*
             * Setting the language mode to siddhi
             *
             * Language mode is located at ace-editor/mode/siddhi.js
             * Highlight style is located at ace-editor/mode/siddhi_highlight_rules.js.js
             * Folding is located at ace-editor/mode/folding/siddhi.js
             * Snippets are located at ace-editor/snippets/siddhi.js
             */
            aceEditor.session.setMode(constants.ace.SIDDHI_MODE);

            // Setting the editor options
            aceEditor.setReadOnly(config.readOnly);
            aceEditor.setTheme(config.theme ? "ace/theme/" + config.theme : constants.ace.DEFAULT_THEME);
            aceEditor.getSession().setUseWrapMode(true);
            aceEditor.getSession().setTabSize(4);
            aceEditor.getSession().setUseSoftTabs(true);
            aceEditor.setShowFoldWidgets(true);
            aceEditor.setBehavioursEnabled(true);
            aceEditor.setHighlightSelectedWord(true);
            aceEditor.setHighlightActiveLine(true);
            aceEditor.setDisplayIndentGuides(true);
            aceEditor.setShowPrintMargin(false);
            aceEditor.setShowFoldWidgets(true);
            aceEditor.session.setFoldStyle("markbeginend");
            aceEditor.setFontSize(12);
            aceEditor.setOptions({
                enableBasicAutocompletion: !config.readOnly && config.autoCompletion,
                enableSnippets: !config.readOnly && config.autoCompletion,
                enableLiveAutocompletion: config.autoCompletion,
                autoScrollEditorIntoView: true,
                enableMultiselect: false
            });

            // State variables for error checking and highlighting
            self.state = new State();

            self.completionEngine = new CompletionEngine();
            self.rawExtensions = CompletionEngine.rawExtensions;

            // Attaching editor's onChange event handler
            aceEditor.getSession().on('change', editorChangeHandler);

            // For adjusting the completer list as required
            adjustAutoCompletionHandlers();
            aceEditor.commands.on('afterExec', function () {
                adjustAutoCompletionHandlers();
            });

            // Adding events for adjusting the completions list styles
            // This is used for showing different styles for different types of completions
            aceEditor.renderer.on("afterRender", function () {
                // Checking if a popup is open when the editor is re-rendered
                if (aceEditor.completer && aceEditor.completer.popup) {
                    // Adding a on after render event for updating the popup styles
                    aceEditor.completer.popup.renderer.on("afterRender", function () {
                        var completionElements = document.querySelectorAll(
                            ".ace_autocomplete > .ace_scroller > .ace_content > .ace_text-layer > .ace_line"
                        );
                        for (var i = 0; i < completionElements.length; i++) {
                            var element =
                                completionElements[i].getElementsByClassName("ace_rightAlignedText")[0];
                            if (element && displayNameToStyleMap[element.innerHTML]) {
                                completionElements[i].setAttribute(
                                    "style",
                                    displayNameToStyleMap[element.innerHTML]
                                );
                            }
                        }
                    });
                }
            });

            /*
             * Starting a new siddhi worker for running antlr tasks
             * This is done to isolate the antlr tasks form the main js to solve RequireJS conflicts
             * Also this will enable the antlr tasks to run without blocking the UI thread
             */
            if (self.realTimeValidation) {
                var siddhiWorker = new SiddhiWorker(new MessageHandler(self));
                self.debugger = new Debugger(aceEditor);
            }


            self.getDebugger = function () {
                return self.debugger;
            };

            self.getRawExtensions = function () {
                return self.rawExtensions;
            };

            /**
             * Returns the ace editor object
             * Can be used for getting the ace editor object and making custom changes
             */
            self.getAceEditorObject = function () {
                return aceEditor;
            };

            /**
             * Returns the content in the ace editor when the method is invoked
             *
             * @return {string} Content in the editor when the method is invoked
             */
            self.getContent = function () {
                return aceEditor.getValue();
            };

            /**
             * Sets the content in the ace editor
             *
             * @param {string} content Content to set into the ace editor
             */
            self.setContent = function (content) {
                aceEditor.setValue((content ? content : ""), 1);
            };

            /**
             * Dynamically select the completers suitable for current context
             *
             * @private
             */
            function adjustAutoCompletionHandlers() {
                // Selecting the completer for current context when auto complete event is fired
                // SiddhiCompleter needs to be the first completer in the list as it will update the snippets
                var completerList =
                    [self.completionEngine.SiddhiCompleter, self.completionEngine.SnippetCompleter];

                // Adding keyword completor if the cursor is not in front of dot or colon
                var objectNameRegex = new RegExp("[a-zA-Z_][a-zA-Z_0-9]*\\s*\\.\\s*$", "i");
                var namespaceRegex = new RegExp("[a-zA-Z_][a-zA-Z_0-9]*\\s*:\\s*$", "i");
                var singleLineCommentRegex = new RegExp("--(?:.(?!\n))*$");
                var blockCommentRegex = new RegExp("\\/\\*(?:(?:.|\n)(?!\\*\\/))*$");

                // Adding the keyword completor
                var editorText = aceEditor.getValue();
                if (!(objectNameRegex.test(editorText) || namespaceRegex.test(editorText) ||
                    singleLineCommentRegex.test(editorText) || blockCommentRegex.test(editorText))) {
                    // todo removing keyword suggestions
                    // completerList.push(aceExtLangTools.keyWordCompleter);
                }

                aceEditor.completers = completerList;
            }

            /**
             * Editor change handler
             *
             * @private
             */
            function editorChangeHandler() {
                    clearErrorsAndTriggerOnChange();
            }

            function clearErrorsAndTriggerOnChange() {
                if (self.realTimeValidation) {
                    // Clearing all errors before finding the errors again
                    self.state.semanticErrorList = [];
                    self.state.syntaxErrorList = [];
                    self.unMarkErrors();
                    siddhiWorker.onEditorChange(aceEditor.getValue().trim());
                }
            }

            self.resetValidations = function () {
                clearErrorsAndTriggerOnChange();
            };

            /**
             * Start the timer for checking the semantic errors
             * After the timer elapses if the user had not typed anything semantic errors will be checked using the server
             */
            self.startCheckForSemanticErrorsTimer = function () {
                if (config.realTimeValidation) {
                    // If there are no syntax errors and there is a change in parserTree
                    // check for semantic errors if there is no change in the query within 3sec period
                    // 3 seconds delay is added to avoid repeated server calls while user is typing the query.
                    setTimeout(function () {
                        if (Date.now() - self.state.lastEdit >= constants.SERVER_SIDE_VALIDATION_DELAY - 100) {
                            // Check for semantic errors by sending a validate request to the server
                            self.checkForSemanticErrors();
                        }
                    }, constants.SERVER_SIDE_VALIDATION_DELAY);
                }

                self.state.lastEdit = Date.now();         // Save user's last edit time
            };

            function State() {
                this.syntaxErrorList = [];    // To save the syntax Errors with line numbers
                this.semanticErrorList = [];  // To save semanticErrors with line numbers
                this.lastEdit = 0;            // Last edit time
                this.errorMarkers = [];       // Holds highlighted syntax/semantic error markers
            }

            /**
             * This method send server calls to check the semantic errors
             * Also retrieves the missing completion engine data from the server if the siddhi app is valid
             *
             * @private
             */
            self.checkForSemanticErrors = function() {
                var editorText = aceEditor.getValue();
                var variableMap = Utils.prototype.retrieveEnvVariables();

                // If the user has not typed anything after 3 seconds from his last change, then send the query for
                // semantic check to check whether the query contains errors or not.
                submitToServerForSemanticErrorCheck(
                    {
                        siddhiApp: editorText,
                        variables: variableMap,
                        missingStreams: self.completionEngine.incompleteData.streams,
                        missingInnerStreams: self.completionEngine.incompleteData.partitions,
                        missingAggregationDefinitions: self.completionEngine.incompleteData.aggregationDefinitions
                    },
                    function (response) {
                        if (response.hasOwnProperty("status") && response.status === "SUCCESS") {
                            /*
                             * Siddhi app is valid
                             */

                            // Populating the fetched data for incomplete data items into the completion engine's data
                            var streams = getStreamsFromStreamDefinitions(response.streams);
                            for (var streamName in streams) {
                                if (streams.hasOwnProperty(streamName)) {
                                    self.completionEngine.streamsList[streamName] = streams[streamName];
                                }
                            }

                            var aggregationDefinitions
                                = getAggregationsFromAggregationDefinitions(response.aggregationDefinitions);
                            for (var aggregationDefinition in aggregationDefinitions) {
                                if (aggregationDefinitions.hasOwnProperty(aggregationDefinition)) {
                                    self.completionEngine.aggregationsList[aggregationDefinition]
                                        = aggregationDefinitions[aggregationDefinition];
                                }
                            }

                            self.completionEngine.partitionsList = [];
                            if (response.innerStreams != undefined) {
                                for (var i = 0; i < response.innerStreams.length; i++) {
                                    var innerStreams = getStreamsFromStreamDefinitions(response.innerStreams[i], true);
                                    self.completionEngine.partitionsList.push(innerStreams);
                                }
                            }

                            // Updating token tooltips
                            self.completionEngine.clearIncompleteDataLists();
                            self.unMarkErrors();
                        } else {
                            try {
                                /*
                                * Error found in Siddhi app
                                */
                                if (response.queryContextStartIndex === undefined) {
                                    // Update the semanticErrorList
                                    self.state.semanticErrorList = [({
                                        row: 0,
                                        // Change attribute "text" to "html" if html is sent from server
                                        html: utils.wordWrap(_.escape(response.message), 120),
                                        type: "error"
                                    })];

                                    // Show the errors in the ace editor gutter
                                    aceEditor.session.setAnnotations(
                                        self.state.semanticErrorList
                                            .concat(self.state.syntaxErrorList)
                                    );
                                } else {
                                    // Update the semanticErrorList
                                    self.state.semanticErrorList = [({
                                        row: response.queryContextStartIndex[0] - 1,
                                        // Change attribute "text" to "html" if html is sent from server
                                        html: utils.wordWrap(_.escape(response.message), 120),
                                        type: "error"
                                    })];

                                    // Show the errors in the ace editor gutter
                                    aceEditor.session.setAnnotations(
                                        self.state.semanticErrorList
                                            .concat(self.state.syntaxErrorList)
                                    );

                                    // Highlight the error
                                    self.markError(response);
                                }
                            } catch (error) {
                                console.log("Error while parsing Errors in Siddhi app" + error);
                            }
                        }
                        siddhiWorker.generateTokenTooltips();
                    },
                    siddhiWorker.generateTokenTooltips
                );

                /**
                 * Get the streams list from the stream definitions list returned from the server
                 * This is used for transforming server's stream definitions to completion engine's stream data
                 *
                 * @param {object[]} streamDefinitionsList Stream definitions list returned from the server
                 * @param {boolean} [isInner] Boolean indicating whether the set of stream definitions are inner streams or not
                 * @return {object} Stream data extracted from the stream definitions
                 */
                function getStreamsFromStreamDefinitions(streamDefinitionsList, isInner) {
                    var streams = {};
                    for (var i = 0; i < streamDefinitionsList.length; i++) {
                        var streamDefinition = streamDefinitionsList[i];
                        var attributes = {};
                        for (var k = 0; k < streamDefinition.attributeList.length; k++) {
                            attributes[streamDefinition.attributeList[k].name] =
                                streamDefinition.attributeList[k].type;
                        }
                        streams[streamDefinitionsList[i].id] = {
                            attributes: attributes,
                            description: utils.generateDescriptionForStreamOrTable(
                                (isInner ? "Inner " : "") + "Stream",
                                streamDefinitionsList[i].id, attributes
                            )
                        };
                    }
                    return streams;
                }

                /**
                 * Get the attribute list from the attribute definitions list returned from the server
                 * This is used for transforming server's aggregation definitions to completion engine's aggregation
                 * definition data
                 *
                 * @param {object[]} aggregationDefinitionsList aggregation definitions list returned from the server
                 * @return {object} Aggregation definition data extracted from the aggregation definitions
                 */
                function getAggregationsFromAggregationDefinitions(aggregationDefinitionsList) {
                    var aggregationDefinitions = {};
                    for (var i = 0; i < aggregationDefinitionsList.length; i++) {
                        var aggregationDefinition = aggregationDefinitionsList[i];
                        var attributes = {};
                        for (var k = 0; k < aggregationDefinition.attributeList.length; k++) {
                            attributes[aggregationDefinition.attributeList[k].name] =
                                aggregationDefinition.attributeList[k].type;
                        }
                        aggregationDefinitions[aggregationDefinitionsList[i].id] = {
                            attributes: attributes,
                            description: utils.generateDescriptionForAggregation(
                                aggregationDefinitionsList[i].id, attributes
                            )
                        };
                    }
                    return aggregationDefinitions;
                }
            }

            /**
             * Highlights the section with the semantic errors.
             * @param error
             */
            self.markError = function (error) {
                if (error.hasOwnProperty("queryContextStartIndex") && error.hasOwnProperty("queryContextEndIndex")) {
                    self.state.errorMarkers.push(aceEditor.session.addMarker(
                        new AceRange.Range(error.queryContextStartIndex[0] - 1, error.queryContextStartIndex[1],
                            error.queryContextEndIndex[0] - 1, error.queryContextEndIndex[1]),
                        "error_line_highlight",
                        "text",
                        true
                    ));
                }
            };

            /**
             * Remove previously highlighted semantic errors.
             */
            self.unMarkErrors = function () {
                for (var i = 0; i < self.state.errorMarkers.length; i++) {
                    if (self.state.errorMarkers[i] !== null)
                        aceEditor.session.removeMarker(self.state.errorMarkers[i]);
                }
            };

            /**
             * Submit the siddhi app to server for semantic error checking
             * Also fetched the incomplete data from the server for the completion engine
             *
             * @private
             * @param {Object} data The siddhi app and the missing data in a java script object
             * @param {function} callback Callback to be called after successful semantic error check
             * @param {function} [errorCallback] Callback to be called after errors in semantic error check
             */
            function submitToServerForSemanticErrorCheck(data, callback, errorCallback) {
                if (data.siddhiApp === "") {
                    return;
                }
                $.ajax({
                    type: "POST",
                    url: constants.SERVER_URL + "validator",
                    data: JSON.stringify(data),
                    success: callback,
                    error: errorCallback
                });
            }

            return self;
        }


        /**
         * Siddhi Web Worker wrapper prototype
         * Handles all ANTLR related processing
         * Automatically starts up the web worker as well
         *
         * @param {MessageHandler} messageHandler Message handler object which will handle all incoming messages from the worker
         * @return {SiddhiWorker} Siddhi worker instance
         * @constructor
         */
        function SiddhiWorker(messageHandler) {
            var self = this;
            var worker;

            /**
             * Restart the web worker
             */
            self.restart = function () {
                if (worker) {
                    worker.terminate();
                }
                worker = new Worker("/editor/js/source-editor/antlr-worker.js");
                self.init();
            };

            /**
             * Initialize the web worker
             * Constants are passed into the web worker
             * Constants are passed in this way because some of the constants are generated values and the generation of which requires the window object
             */
            self.init = function () {
                worker.postMessage(JSON.stringify({
                    type: constants.worker.INIT,
                    data: constants
                }));

                // Add event receiver to listen to incoming messages from the web worker
                worker.addEventListener('message', function (event) {
                    messageHandler.handle(JSON.parse(event.data));
                });
            };

            /**
             * Run on editor's change
             * Send message to the worker to create the parse tree and generate completion engine data
             *
             * @param {string} editorText Text in the editor after the change
             */
            var editorChangeDelayTimer;
            self.onEditorChange = function (editorText) {
                clearTimeout(editorChangeDelayTimer);
                editorChangeDelayTimer = setTimeout(function () {
                    worker.postMessage(JSON.stringify({
                        type: constants.worker.EDITOR_CHANGE_EVENT,
                        data: editorText
                    }));
                }, 2000);
            };

            /**
             * Send message to the worker to start generating token tool tips
             * The worker will recognize the token tooltip generation points and pass the the relevant data back so that they can be added
             */
            self.generateTokenTooltips = function () {
                worker.postMessage(JSON.stringify({
                    type: constants.worker.GENERATE_TOKEN_TOOLTIP
                }));
            };

            self.restart();     // Starts up the web worker
            return self;
        }

        /**
         * Siddhi Debugger prototype
         * Siddhi Debugger is used to debug current query in the editor
         *
         * @param {object} aceEditor The Ace Editor object
         * @return {Debugger} Siddhi Debugger instance
         * @constructor
         */
        function Debugger(aceEditor) {
            var self = this;
            self.__pollingInterval = 1000;
            self.__pollingLock = false;
            self.__pollingJob = null;
            self.__callback = null;
            self.__onChangeLineNumbers = null;
            self.__onDebugStopped = null;
            self.__client = DebugRESTClient;
            self.siddhiAppName = null;
            self.streams = null;
            self.queries = null;
            self.__validBreakPoints = null;
            self.__failedStateRequests = 0;
            self.__isRunning = false;
            self.siddhiAppName = 'siddhiApp';

            self.setSiddhiAppName = function (appName) {
                self.siddhiAppName = appName;
            };

            self.debug = function (successCallback, errorCallback, async) {
                if (!self.__isRunning) {
                    self.__client.debug(
                        self.siddhiAppName,
                        function (data) {
                            self.streams = data['streams'];
                            self.queries = data['queries'];
                            if (self.streams === null || self.streams.length === 0) {
                                console.warn("Streams cannot be empty.");
                            }
                            if (self.queries === null || self.queries.length === 0) {
                                console.warn("Queries cannot be empty.");
                            }
                            if (self.streams !== null && self.streams.length > 0 &&
                                self.queries !== null && self.queries.length > 0) {
                                console.log("Debugger started : " + self.siddhiAppName);
                                self.__isRunning = true;
                                self.__pollingJob = setInterval(function () {
                                    if (!self.__pollingLock) {
                                        self.state();
                                    }
                                }, self.__pollingInterval);
                                if (typeof successCallback === 'function')
                                    successCallback(self.siddhiAppName, self.streams, self.queries)
                            }
                        },
                        function (error) {
                            if (typeof errorCallback === 'function')
                                errorCallback(error)
                        },
                        async
                    );
                } else {
                    log.error("Siddhi app is already running.")
                }
            };

            self.stop = function (successCallback, errorCallback) {
                if (self.__pollingJob !== null) {
                    clearInterval(self.__pollingJob);
                }
                if (self.__isRunning) {
                    self.__client.stop(
                        self.siddhiAppName,
                        function (data) {
                            console.log("Debugger stopped : " + self.siddhiAppName);
                            self.__isRunning = false;
                            if (typeof successCallback === 'function')
                                successCallback(data);
                            if (typeof self.__onDebugStopped === 'function')
                                self.__onDebugStopped()
                        },
                        function (error) {
                            if (typeof errorCallback === 'function')
                                errorCallback(error);
                        }
                    );
                } else {
                    console.log("Debugger has not been started yet.")
                }
            };

            self.clearInterval = function () {
                if (self.__pollingJob !== null) {
                    clearInterval(self.__pollingJob);
                }
            };

            self.acquire = function (lineNo, success) {
                var breakPoints = self.__validBreakPoints[lineNo];
                if (self.__isRunning && breakPoints !== null && breakPoints.length > 0) {
                    for (var i = 0; i < breakPoints.length; i++) {
                        self.__client.acquireBreakPoint(
                            self.siddhiAppName,
                            breakPoints[i]['queryIndex'],
                            breakPoints[i]['terminal'],
                            function (data) {
                                console.info(JSON.stringify(data));
                                if (typeof success === 'function')
                                    success(data)
                            },
                            function (error) {
                                log.error(JSON.stringify(error));
                            }
                        );
                    }
                } else {
                    log.error("Debugger has not been started yet.")
                }
            };

            self.release = function (lineNo, success) {
                var breakPoints = self.__validBreakPoints[lineNo];
                if (self.__isRunning && breakPoints !== null && breakPoints.length > 0) {
                    for (var i = 0; i < breakPoints.length; i++) {
                        self.__client.releaseBreakPoint(
                            self.siddhiAppName,
                            breakPoints[i]['queryIndex'],
                            breakPoints[i]['terminal'],
                            function (data) {
                                console.info(JSON.stringify(data));
                                if (typeof success === 'function')
                                    success(data)
                            },
                            function (error) {
                                log.error(JSON.stringify(error));
                            }
                        );
                    }
                } else {
                    console.log("Debugger has not been started yet.")
                }
            };

            self.next = function () {
                if (self.__isRunning) {
                    self.__client.next(
                        self.siddhiAppName,
                        function (data) {
                            console.info(JSON.stringify(data));
                            if (typeof self.__onBeforeUpdateCallback === 'function')
                                self.__onBeforeUpdateCallback();
                            self.state();
                        },
                        function (error) {
                            log.error(JSON.stringify(error));
                        }
                    );
                } else {
                    log.error("Debugger has not been started yet.")
                }
            };

            self.play = function () {
                if (self.__isRunning) {
                    self.__client.play(
                        self.siddhiAppName,
                        function (data) {
                            console.info(JSON.stringify(data));
                            if (typeof self.__onBeforeUpdateCallback === 'function')
                                self.__onBeforeUpdateCallback();
                            self.state();
                        },
                        function (error) {
                            log.error(JSON.stringify(error));
                        }
                    );
                } else {
                    console.log("Debugger has not been started yet.")
                }
            };

            self.state = function () {
                self.__pollingLock = true;
                if (self.__isRunning) {
                    self.__client.state(
                        self.siddhiAppName,
                        function (data) {
                            if (data.hasOwnProperty('eventState')) {
                                if (typeof self.__callback === 'function') {
                                    self.__callback(data);
                                }
                            }
                            self.__pollingLock = false;
                            self.__failedStateRequests = 0;
                        },
                        function (error) {
                            log.error(JSON.stringify(error));
                            self.__failedStateRequests += 1;
                            self.__pollingLock = false;
                            if (self.__failedStateRequests >= 5) {
                                console.warn("Backend is unreachable. Hence, stopping debugger.");
                                self.stop();
                            }
                        }
                    );
                } else {
                    console.log("Debugger has not been started yet.")
                }
            };

            self.sendEvent = function (streamId, event) {
                if (self.__isRunning) {
                    self.__client.sendEvent(
                        self.siddhiAppName,
                        streamId,
                        event,
                        function (data) {
                            console.info(JSON.stringify(data));
                        },
                        function (error) {
                            log.error(JSON.stringify(error));
                        }
                    );
                } else {
                    console.log("Debugger has not been started yet.")
                }
            };

            self.setOnUpdateCallback = function (onUpdateCallback) {
                self.__callback = onUpdateCallback;
            };

            self.setOnBeforeUpdateCallback = function (onBeforeUpdateCallback) {
                self.__onBeforeUpdateCallback = onBeforeUpdateCallback;
            };

            self.setOnDebugStoppedCallback = function (onDebugStopped) {
                self.__onDebugStopped = onDebugStopped;
            };

            self.setOnChangeLineNumbersCallback = function (onChangeLineNumbers) {
                self.__onChangeLineNumbers = onChangeLineNumbers;
            };

            self._resetQueryMeta = function () {
                self.__validBreakPoints = {};
            };

            self._updateQueryMeta = function (metaData) {
                self.__validBreakPoints = {};
                if (metaData !== null && metaData.length > 0) {
                    for (var i = 0; i < metaData.length; i++) {
                        var inLineNo = metaData[i]['in'] - 1; // breakpoints starts from 0
                        var outLineNo = metaData[i]['out'] - 1;
                        if (self.__validBreakPoints.hasOwnProperty(inLineNo)) {
                            self.__validBreakPoints[inLineNo].push({
                                terminal: 'in',
                                queryIndex: i
                            });
                        } else {
                            self.__validBreakPoints[inLineNo] = [{
                                terminal: 'in',
                                queryIndex: i
                            }]
                        }

                        if (self.__validBreakPoints.hasOwnProperty(outLineNo)) {
                            self.__validBreakPoints[outLineNo].push({
                                terminal: 'out',
                                queryIndex: i
                            });
                        } else {
                            self.__validBreakPoints[outLineNo] = [{
                                terminal: 'out',
                                queryIndex: i
                            }]
                        }
                    }
                }
                if (typeof self.__onChangeLineNumbers === 'function') {
                    self.__onChangeLineNumbers(self.__validBreakPoints);
                }
            };

            return self;
        }

        /**
         * Message handler prototype
         * Message handler is used by the siddhi worker
         *
         * @param {object} editor The editor object
         * @return {MessageHandler} Message handler instance
         * @constructor
         */
        function MessageHandler(editor) {

            var handler = this;
            var messageHandlerMap = {};
            var tokenTooltipUpdater = new TokenTooltipUpdater(editor);

            // Generating the map from message types to handler functions
            messageHandlerMap[constants.worker.PARSE_TREE_GENERATION_COMPLETION] = updateSyntaxErrorList;
            messageHandlerMap[constants.worker.DATA_POPULATION_COMPLETION] = updateCompletionEngineData;
            messageHandlerMap[constants.worker.TOKEN_TOOLTIP_POINT_RECOGNITION_COMPLETION] = updateTokenTooltips;

            /**
             * Handle an incoming message from the web worker
             * @param {object} message
             */
            handler.handle = function (message) {
                messageHandlerMap[message.type](message.data);
            };

            /**
             * Update the list of syntax errors and add annotations
             *
             * @param {object} data Syntax errors data list
             */
            function updateSyntaxErrorList(data) {
                editor.state.syntaxErrorList = data;
                editor.getAceEditorObject().session.setAnnotations(data);
                markSyntaxError(data);
            }

            /**
             * Highlight syntax errors
             * @param errors
             */
            function markSyntaxError(errors) {
                if (errors.length > 0) {
                    var error = errors[0];
                    if (error.type === "error") {
                        var syntax = editor.getAceEditorObject().session.getLine(error.row).substr(error.column)
                            .split(/[^0-9a-zA-Z]+/g)[0];
                        var syntaxLength = syntax.length;
                        var errorObj = {
                            message: error.text,
                            queryContextStartIndex: [error.row + 1, error.column],
                            queryContextEndIndex: [error.row + 1, error.column + syntaxLength]
                        };
                        editor.markError(errorObj);
                    }
                }
            }

            /**
             * Update the completion engine's data using the data generated by the worker
             *
             * @param {object} data Completion engine data generated by the worker
             */
            function updateCompletionEngineData(data) {
                editor.debugger._resetQueryMeta();
                editor.debugger._updateQueryMeta(data.debugData);
                editor.completionEngine.clearData();            // Clear the exiting completion engine data
                editor.completionEngine.streamsList = data.completionData.streamsList;
                editor.completionEngine.partitionsList = data.completionData.partitionsList;
                editor.completionEngine.eventTablesList = data.completionData.eventTablesList;
                editor.completionEngine.eventTriggersList = data.completionData.eventTriggersList;
                editor.completionEngine.evalScriptsList = data.completionData.evalScriptsList;
                editor.completionEngine.eventWindowsList = data.completionData.eventWindowsList;
                editor.completionEngine.aggregationsList = data.completionData.aggregationsList;
                editor.completionEngine.updateDescriptions();
                editor.completionEngine.incompleteData = data.incompleteData;
                editor.completionEngine.statementsList = data.statementsList;
                editor.startCheckForSemanticErrorsTimer();
            }

            /**
             * Update the token tool tips using the data generated by the worker
             *
             * @param {object} data Token tool tip points and information for generating token tool tips
             */
            function updateTokenTooltips(data) {
                for (var i = 0; i < data.length; i++) {
                    var tooltipType = data[i].type;
                    var tooltipData = data[i].tooltipData;
                    var row = data[i].row;
                    var column = data[i].column;

                    tokenTooltipUpdater.update(tooltipType, tooltipData, row, column);
                }
            }

            return handler;
        }

        /**
         * Token tooltips generator prototype
         *
         * @param {object} editor The editor object
         * @return {TokenTooltipUpdater} Token tooltip generator instance
         * @constructor
         */
        function TokenTooltipUpdater(editor) {
            var updater = this;

            /**
             * Update the tooltip for the given type using the tool tip data
             *
             * @param {string} tooltipType Type of the tool tip to be updated
             * @param {object} tooltipData Tool tip data from which the tool tip will be generated
             * @param {int} row The row at which the target token is at
             * @param {int} column The column at which the target token is at
             */
            updater.update = function (tooltipType, tooltipData, row, column) {
                switch (tooltipType) {
                    case constants.FUNCTION_OPERATION:
                        updateFunctionOperationTooltip(tooltipData, row, column);
                        break;
                    case constants.SOURCE:
                        updateSourceTooltip(tooltipData, row, column);
                        break;
                    case constants.INNER_STREAMS:
                        updateInnerStreamTooltip(tooltipData, row, column);
                        break;
                    case constants.TRIGGERS:
                        updateTriggerTooltip(tooltipData, row, column);
                        break;
                    case constants.AGGREGATIONS:
                        updateAggregationTooltip(tooltipData, row, column);
                        break;
                    case constants.IO:
                        updateIOToolTip(tooltipData, row, column);
                        break;
                    case constants.MAP:
                        updateMapToolTip(tooltipData, row, column);
                        break;
                    case constants.STORE:
                        updateStoreToolTip(tooltipData, row, column);
                        break;
                }
            };

            /**
             * Update the tooltip for a function operation
             *
             * @param {object} tooltipData Tool tip data to be added. Should contain the function operation name and the namespace
             * @param {int} row The row at which the target token is at
             * @param {int} column The column at which the target token is at
             */
            function updateFunctionOperationTooltip(tooltipData, row, column) {
                var processorName = tooltipData.processorName;
                var namespace = tooltipData.namespace;

                var snippets;
                if (namespace) {
                    snippets = CompletionEngine.functionOperationSnippets.extensions[namespace];
                } else {
                    snippets = CompletionEngine.functionOperationSnippets.inBuilt;
                }

                // Adding WindowProcessor/StreamProcessor/Function/additional tool tip
                var description;
                if (snippets) {
                    if (snippets.windowProcessors && snippets.windowProcessors[processorName]) {
                        description = snippets.windowProcessors[processorName].description;
                    } else if (snippets.streamProcessors && snippets.streamProcessors[processorName]) {
                        description = snippets.streamProcessors[processorName].description;
                    } else if (snippets.functions && snippets.functions[processorName]) {
                        description = snippets.functions[processorName].description;
                    } else if (editor.completionEngine.evalScriptsList[processorName]) {
                        description = editor.completionEngine.evalScriptsList[processorName].description;
                    }
                }
                if (description) {
                    updateTokenTooltip(row, column, description);
                }
            }

            /**
             * Update the tooltip for a IO connectors source/sink
             *
             * @param {object} tooltipData Tool tip data to be added. Should contain the io name and the io namespace
             * @param {int} row The row at which the target token is at
             * @param {int} column The column at which the target token is at
             */
            function updateIOToolTip(tooltipData, row, column) {
                var implementationName = tooltipData.implementationName;
                var namespace = tooltipData.namespace;

                var snippets;
                if (namespace) {
                    snippets = CompletionEngine.functionOperationSnippets.extensions[namespace];
                } else {
                    snippets = CompletionEngine.functionOperationSnippets.inBuilt;
                }

                // Adding IO source/sink tool tip
                var description;
                if (snippets) {
                    if (snippets.sinks && snippets.sinks[implementationName]) {
                        description = snippets.sinks[implementationName].description;
                    } else if (snippets.sources && snippets.sources[implementationName]) {
                        description = snippets.sources[implementationName].description;
                    }
                }
                if (description) {
                    updateTokenTooltip(row, column, description);
                }
            }

            /**
             * Update the tooltip for a Store
             *
             * @param {object} tooltipData Tool tip data to be added. Should contain the store name and the store
             * namespace
             * @param {int} row The row at which the target token is at
             * @param {int} column The column at which the target token is at
             */
            function updateStoreToolTip(tooltipData, row, column) {
                var implementationName = tooltipData.implementationName;
                var namespace = tooltipData.namespace;

                var snippets;
                if (namespace) {
                    snippets = CompletionEngine.functionOperationSnippets.extensions[namespace];
                } else {
                    snippets = CompletionEngine.functionOperationSnippets.inBuilt;
                }

                // Adding IO source/sink tool tip
                var description;
                if (snippets) {
                    if (snippets.stores && snippets.stores[implementationName]) {
                        description = snippets.stores[implementationName].description;
                    }
                }
                if (description) {
                    updateTokenTooltip(row, column, description);
                }
            }

            /**
             * Update the tooltip for a MAP source/sink
             *
             * @param {object} tooltipData Tool tip data to be added. Should contain the io name and the io namespace
             * @param {int} row The row at which the target token is at
             * @param {int} column The column at which the target token is at
             */
            function updateMapToolTip(tooltipData, row, column) {
                var implementationName = tooltipData.implementationName;
                var namespace = tooltipData.namespace;

                var snippets;
                if (namespace) {
                    snippets = CompletionEngine.functionOperationSnippets.extensions[namespace];
                } else {
                    snippets = CompletionEngine.functionOperationSnippets.inBuilt;
                }

                // Adding Map source/sink tool tip
                var description;
                if (snippets) {
                    if (snippets.sinkMaps && snippets.sinkMaps[implementationName]) {
                        description = snippets.sinkMaps[implementationName].description;
                    } else if (snippets.sourceMaps && snippets.sourceMaps[implementationName]) {
                        description = snippets.sourceMaps[implementationName].description;
                    }
                }
                if (description) {
                    updateTokenTooltip(row, column, description);
                }
            }

            /**
             * Update the tooltip for a stream/table/window
             *
             * @param {object} tooltipData Tool tip data to be added. Should contain the source name
             * @param {int} row The row at which the target token is at
             * @param {int} column The column at which the target token is at
             */
            function updateSourceTooltip(tooltipData, row, column) {
                var sourceName = tooltipData.sourceName;
                var source;

                if (editor.completionEngine.streamsList[sourceName]) {
                    source = editor.completionEngine.streamsList[sourceName];
                } else if (editor.completionEngine.eventTablesList[sourceName]) {
                    source = editor.completionEngine.eventTablesList[sourceName];
                } else if (editor.completionEngine.eventWindowsList[sourceName]) {
                    source = editor.completionEngine.eventWindowsList[sourceName];
                } else if (editor.completionEngine.aggregationsList[sourceName]) {
                    source = editor.completionEngine.aggregationsList[sourceName];
                }

                if (source && source.description) {
                    updateTokenTooltip(row, column, source.description);
                }
            }

            /**
             * Update the tooltip for a inner stream
             *
             * @param {object} tooltipData Tool tip data to be added. Should contain the inner stream name and the partition number
             * @param {int} row The row at which the target token is at
             * @param {int} column The column at which the target token is at
             */
            function updateInnerStreamTooltip(tooltipData, row, column) {
                var innerStreamName = tooltipData.sourceName;
                var partitionNumber = tooltipData.partitionNumber;

                if (editor.completionEngine.partitionsList[partitionNumber]) {
                    var innerStream =
                        editor.completionEngine.partitionsList[partitionNumber][innerStreamName];
                    if (innerStream && innerStream.description) {
                        updateTokenTooltip(row, column, innerStream.description);
                    }
                }
            }

            /**
             * Update the tooltip for a trigger
             *
             * @param {object} tooltipData Tool tip data to be added. Should contain the trigger name
             * @param {int} row The row at which the target token is at
             * @param {int} column The column at which the target token is at
             */
            function updateTriggerTooltip(tooltipData, row, column) {
                var triggerName = tooltipData.triggerName;

                var trigger = editor.completionEngine.eventTriggersList[triggerName];
                if (trigger && trigger.description) {
                    updateTokenTooltip(row, column, trigger.description);
                }
            }

            /**
             * Update the tooltip for an aggregation
             *
             * @param {object} tooltipData Tool tip data to be added. Should contain the aggregation name
             * @param {int} row The row at which the target token is at
             * @param {int} column The column at which the target token is at
             */
            function updateAggregationTooltip(tooltipData, row, column) {
                var aggregationName = tooltipData.aggregationName;

                var aggregation = editor.completionEngine.aggregationsList[aggregationName];
                //var details = editor.incompleteData.aggregationsList;
                if (aggregation && aggregation.description) {
                    updateTokenTooltip(row, column, aggregation.description);
                }
            }

            /**
             * Add a tooltip at the position specified
             *
             * @param {int} tokenRow The row at which the target token is at
             * @param {int} tokenColumn The column at which the target token is at
             * @param {string} tooltip Tooltip to be added
             */
            function updateTokenTooltip(tokenRow, tokenColumn, tooltip) {
                var token = editor.getAceEditorObject().session.getTokenAt(tokenRow, tokenColumn);
                if (token) {
                    token.tooltip = tooltip;
                }
            }

            return updater;
        }

        return SiddhiEditor;
    });
