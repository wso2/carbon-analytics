define(['jquery', 'backbone', 'log', 'lodash', 'ace/range', 'render_json'], function ($, Backbone, log, _, AceRange) {
    var DebugManager = Backbone.View.extend({
        initialize: function (config) {
            this._template = $("#debugger-template");
            this._breakpoints = [];
            this._validBreakpoints = {};
            this._currentDebugLine = null;
            this._lineIndex = {};
            this._debugStarted = false;
            this._debugger = _.get(config, 'debugger_instance');
            this._editor = _.get(config, 'editorInstance');
            var self = this;
            this._debuggerOption = config.option;

            self._editor.on("guttermousedown", function (e) {
                var target = e.domEvent.target;
                if (target.className.indexOf("ace_gutter-cell") == -1)
                    return;
                if (!self._editor.isFocused())
                    return;
                if (e.clientX > 25 + target.getBoundingClientRect().left)
                    return;

                var breakpoints = e.editor.session.getBreakpoints(row, 0);
                var row = e.getDocumentPosition().row;

                if (row in self._validBreakpoints) {
                    if (typeof breakpoints[row] === typeof undefined) {
                        if (self._debugStarted) {
                            self._debugger.acquire(row, function (d) {
                                self._breakpoints[row] = true;
                                e.editor.session.setBreakpoint(row);
                            });
                        } else {
                            self._breakpoints[row] = true;
                            e.editor.session.setBreakpoint(row);
                        }
                        console.info("Acquire Breakpoint " +
                            JSON.stringify(self._validBreakpoints[row]));
                    } else {
                        if (self._debugStarted) {
                            self._debugger.release(row, function (d) {
                                delete self._breakpoints[row];
                                e.editor.session.clearBreakpoint(row);
                            });
                        } else {
                            delete self._breakpoints[row];
                            e.editor.session.clearBreakpoint(row);
                        }
                        console.info("Release Breakpoint " +
                            JSON.stringify(self._validBreakpoints[row]));
                    }
                } else {
                    console.warn("Trying to acquire an invalid breakpoint");
                }
                e.stop();
            });

            self._editor.on("change", function (e) {
                var len, firstRow;

                if (e.end.row == e.start.row) {
                    // editing in same line
                    return;
                } else {
                    // new line or remove line
                    if (e.action == "insert") {
                        len = e.end.row - e.start.row;
                        firstRow = e.start.column == 0 ? e.start.row : e.start.row + 1;
                    } else if (e.action == "remove") {
                        len = e.start.row - e.end.row;
                        firstRow = e.start.row;
                    }

                    if (len > 0) {
                        var args = new Array(len);
                        args.unshift(firstRow, 0);
                        self._breakpoints.splice.apply(self._breakpoints, args);
                    } else if (len < 0) {
                        var rem = self._breakpoints.splice(firstRow + 1, -len);
                        if (!self._breakpoints[firstRow]) {
                            for (var oldBP in rem) {
                                if (rem[oldBP]) {
                                    self._breakpoints[firstRow] = rem[oldBP];
                                    break
                                }
                            }
                        }
                    }

                    // Redraw the breakpoints
                    for (var r in self._breakpoints) {
                        if (self._breakpoints[r]) {
                            self._editor.session.setBreakpoint(r);
                        } else {
                            self._editor.session.clearBreakpoint(r);
                        }
                    }
                }
            });

            self._debugger.setOnChangeLineNumbersCallback(function (validBreakPoints) {
                self._validBreakpoints = validBreakPoints;

                // update line indexes
                self._lineIndex = {};
                for (var i in self._validBreakpoints) {
                    if (self._validBreakpoints.hasOwnProperty(i)) {
                        // i is the line number
                        var breakpoints = self._validBreakpoints[i];
                        for (var j = 0; j < breakpoints.length; j++) {
                            var key = breakpoints[j]['queryIndex'] + '_' + breakpoints[j]['terminal'];
                            key = key.toLowerCase();
                            self._lineIndex[key] = i;
                        }

                    }
                }
            });
        },

        initContainerOpts: function (containerOpts) {
            var errMsg;
            var self = this;
            if (!_.has(containerOpts, 'parent-container')) {
                errMsg = 'unable to find configuration for parent-container';
                log.error(errMsg);
                throw errMsg;
            }
            var parentContainer = $(_.get(containerOpts, 'parent-container'));

            this._$parent_el = parentContainer;

            if (!_.has(containerOpts, 'application')) {
                log.error('Cannot init debugger. config: application not found.')
            }

            var clonedDebugger = this._template.children('div').clone();
            this._console = clonedDebugger;

            this.application = _.get(containerOpts, 'application');
            this._options = containerOpts;
            //this.debuggerServiceUrl = _.get(this._options, 'application.config.services.debugger.endpoint');
            this._lastWidth = undefined;
            this._appName = _.get(containerOpts, 'appName');
            this._consoleObj = _.get(containerOpts, 'consoleObj')

        },

        isActive: function () {
            return this._activateBtn.parent('li').hasClass('active');
        },

        getConsole: function () {
            return this._console;
        },

        getDebugger: function () {
            return this._debugger;
        },

        debug: function (success, error) {
            var self = this;
            self._debugger.debug(success, error);
        },

        setAppName: function (appName) {
            if (this._debugger !== undefined) {
                this._debugger.setSiddhiAppName(appName);
            }
        },

        setDebuggerStarted: function (started) {
            this._debugStarted = started;
        },

        unHighlightDebugLine: function () {
            var self = this;
            if (self._currentDebugLine !== null)
                self._editor.session.removeMarker(self._currentDebugLine);
        },

        getLineNumber: function (queryIndex, queryTerminal) {
            var self = this;
            var key = queryIndex + '_' + queryTerminal;
            key = key.toLowerCase();
            if (self._lineIndex.hasOwnProperty(key)) {
                return self._lineIndex[key];
            } else {
                return null;
            }
        },

        highlightDebugLine: function (lineNo) {
            var self = this;
            self.unHighlightDebugLine();
            self._currentDebugLine = self._editor.session.addMarker(
                new AceRange.Range(lineNo, 0, lineNo, 256),
                "debug_line_highlight",
                "fullLine",
                true
            );
        },

        render: function () {
            var self = this;
            var debuggerModel = this._console;
            //var appName = this._appName;
            var debuggerModalName = debuggerModel.find(".appName");
            self._debugStarted = true;
            // debuggerModalName.text(appName);

            for (var i = 0; i < self._breakpoints.length; i++) {
                if (self._breakpoints[i] && i in self._validBreakpoints) {
                    self._debugger.acquire(i);
                    console.info("Acquire Breakpoint " + JSON.stringify(self._validBreakpoints[i]));
                }
            }

            debuggerModel.find(".fw-resume").click(function(e) {
                e.preventDefault();
                self._debugger.play();
                //console.log("debugger resume click");
            });
            // this._resumeBtn.on('click', function (e) {
            //     // e.preventDefault();
            //     // self._debugger.play();
            //
            //     console.log("inside click")
            //
            // });
            // resumeBtn.attr("data-placement", "bottom").attr("data-container", "body");

            // if (this.application.isRunningOnMacOS()) {
            //     resumeBtn.attr("title", "Debugger resume (" + _.get(self._debuggerOption, 'commandResume.shortcuts.other.label') + ") ").tooltip();
            // } else {
            //     resumeBtn.attr("title", "Debugger resume () ").tooltip();
            // }

            self._debugger.setOnUpdateCallback(function (data) {
                var line = self.getLineNumber(data['eventState']['queryIndex'], data['eventState']['queryTerminal']);
                self.highlightDebugLine(line);
                renderjson.set_show_to_level(1);
                debuggerModel.find("#event-state").html(renderjson(data['eventState']));
                debuggerModel.find("#query-state").html(renderjson(data['queryState']));
            });

            self._debugger.setOnBeforeUpdateCallback(function () {
                self.unHighlightDebugLine();
                debuggerModel.find("#event-state").html("");
                debuggerModel.find("#query-state").html("");
            });

            debuggerModel.find(".fw-stepover").click(function (e) {
                e.preventDefault();
                self._debugger.next();
            });

            debuggerModel.find(".fw-stop").click(function (e) {
                e.preventDefault();
                self.stop();
            });
        },

        stop: function () {
            var console = this.application.outputController.getGlobalConsole();
            var activeTab = this.application.tabController.getActiveTab();
            var workspace = this.application.workspaceManager;
            var siddhiAppName = activeTab.getTitle().split('.')[0];
            this.unHighlightDebugLine();
            this._debugger.stop(
                function (data) {
                    var msg = "";
                    if (activeTab.getFile().getDebugStatus()) {
                        activeTab.getFile().setDebugStatus(false);
                        activeTab.getFile().save();
                        msg = "" + siddhiAppName + ".siddhi - Stopped Debug mode Successfully!.";
                    } else if (activeTab.getFile().getRunStatus()) {
                        activeTab.getFile().setRunStatus(false);
                        activeTab.getFile().save();
                        msg = "" + siddhiAppName + ".siddhi - Stopped Successfully!."
                    }
                    var message = {
                        "type": "INFO",
                        "message": msg
                    }
                    console.println(message);
                    workspace.updateRunMenuItem();
                    this._debugStarted = false;
                },
                function (error) {
                    if (activeTab.getFile().getDebugStatus()) {
                        msg = "" + siddhiAppName + ".siddhi - Error in Stopping Debug mode !.";
                    } else if (activeTab.getFile().getRunStatus()) {
                        msg = "" + siddhiAppName + ".siddhi - Error in Stopping."
                    }
                    var message = {
                        "type": "ERROR",
                        "message": msg
                    }
                    console.println(message);
                    workspace.updateRunMenuItem();
                });
        }
    });

    return DebugManager;
});