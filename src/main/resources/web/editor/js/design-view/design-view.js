/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['require', 'log', 'lodash', 'jquery', 'tool_palette/tool-palette', 'designViewGrid', 'appData',
        'initialiseData', 'jsonValidator'],
    function (require, log, _, $, ToolPalette, DesignViewGrid, AppData, InitialiseData, JSONValidator) {

        /**
         * @class DesignView
         * @constructor
         * @class DesignView  Wraps the Ace editor for design view
         * @param {Object} options Rendering options for the view
         * @param application Application data
         * @param jsPlumbInstance js plumb instance for the design grid
         */
        var DesignView = function (options, application, jsPlumbInstance) {
            var errorMessage1 = 'unable to find design view container in design-view.js';
            var errorMessage2 = 'unable to find application in design-view.js';
            if (!_.has(options, 'container')) {
                log.error(errorMessage1);
                throw errorMessage1;
            }
            var container = $(_.get(options, 'container'));
            if (!container.length > 0) {
                log.error(errorMessage1);
                throw errorMessage1;
            }
            if (!_.has(options, 'application')) {
                log.error(errorMessage2);
                throw errorMessage2;
            }
            this._$parent_el = container;
            this.options = options;
            this.application = application;
            this.jsPlumbInstance = jsPlumbInstance;
        };

        DesignView.prototype.setRawExtensions = function (rawExtensions) {
            this.rawExtensions = rawExtensions;
        }

        DesignView.prototype.getRawExtensions = function () {
            return this.rawExtensions;
        }

        /**
         * @function Renders tool palette in the design container
         */
        DesignView.prototype.renderToolPalette = function () {
            var errMsg = '';
            var toolPaletteContainer =
                this._$parent_el.find(_.get(this.options, 'design_view.tool_palette.container')).get(0);
            if (!toolPaletteContainer) {
                errMsg = 'unable to find tool palette container with selector: '
                    + _.get(this.options, 'design_view.tool_palette.container');
                log.error(errMsg);
                throw errMsg;
            }
            var toolPaletteOpts = _.clone(_.get(this.options, 'design_view.tool_palette'));
            if (!toolPaletteOpts) {
                errMsg = 'unable to find tool palette with selector: '
                    + _.get(this.options, 'design_view.tool_palette');
                log.error(errMsg);
                throw errMsg;
            }
            toolPaletteOpts.container = toolPaletteContainer;
            this.toolPalette = new ToolPalette(toolPaletteOpts);
            this.toolPalette.render();
        };

        /**
         * @function Renders design view in the design container
         * @param configurationJSON Siddhi application content JSON
         */
        DesignView.prototype.renderDesignGrid = function (configurationJSON) {
            var self = this;
            var initialiseData = new InitialiseData(self._$parent_el, self.application);
            initialiseData.setRawExtension(self.rawExtensions);
            this.configurationData = initialiseData.initialiseSiddhiAppData(configurationJSON);
            var designViewGridOpts = {};
            _.set(designViewGridOpts, 'container', this.designViewGridContainer);
            _.set(designViewGridOpts, 'configurationData', this.configurationData);
            _.set(designViewGridOpts, 'application', this.application);
            _.set(designViewGridOpts, 'jsPlumbInstance', this.jsPlumbInstance);
            this.designViewGrid = new DesignViewGrid(designViewGridOpts);
            this.designViewGrid.render();
        };

        DesignView.prototype.setHashCode = function (hashCode) {
            this.renderedAppContentHashCode = hashCode;
        }

        DesignView.prototype.getHashCode = function () {
            return this.renderedAppContentHashCode;
        }

        DesignView.prototype.autoAlign = function () {
            if (!_.isUndefined(this.designViewGrid)) {
                this.designViewGrid.autoAlignElements();
            }
        };

        DesignView.prototype.getConfigurationData = function () {
            return this.configurationData;
        };

        DesignView.prototype.setConfigurationData = function (configurationData) {
            this.configurationData = configurationData;
        };

        DesignView.prototype.emptyDesignViewGridContainer = function () {
            var errMsg = '';
            this.designViewGridContainer = this._$parent_el.find(_.get(this.options, 'design_view.grid_container'));
            if (!this.designViewGridContainer.length > 0) {
                errMsg = 'unable to find design view grid container with selector: '
                    + _.get(this.options, 'design_view.grid_container');
                log.error(errMsg);
                throw errMsg;
            }

            // reset the jsPlumb common instance
            if (this.jsPlumbInstance !== undefined) {
                this.jsPlumbInstance.clear();
                this.jsPlumbInstance.reset();
            }
            // remove any child nodes from designViewGridContainer if exists
            this.designViewGridContainer.empty();
        };

        DesignView.prototype.showToolPalette = function () {
            if (this.toolPalette !== undefined) {
                this.toolPalette.showToolPalette();
            }
        };

        DesignView.prototype.hideToolPalette = function () {
            if (this.toolPalette !== undefined) {
                this.toolPalette.hideToolPalette();
            }
        };

        /**
         * @function Renders design view in the design container
         * @param configurationJSON JSON which needs to be validated
         * @return {boolean} return whether the json is valid or not
         */
        DesignView.prototype.validateJSONBeforeSendingToBackend = function (configurationJSON) {
            var self = this;
            var jsonValidator = new JSONValidator();
            return jsonValidator.validate(configurationJSON, self.jsPlumbInstance);
        };

        DesignView.prototype.getDesign = function (code) {
            var self = this;
            var result = {};
            // Remove Single Line Comments
            var regexStr = code.replace(/--.*/g, '');
            // Remove Multi-line Comments
            regexStr = regexStr.replace(/\/\*(.|\s)*?\*\//g, '');
            var regex = /^\s*@\s*app\s*:\s*name\s*\(\s*["|'](.*)["|']\s*\)\s*@\s*app\s*:\s*description\s*\(\s*["|'](.*)["|']\s*\)\s*$/gi;
            var match = regex.exec(regexStr);

            // check whether Siddhi app is in initial mode(initial Siddhi app template) and if yes then go to the
            // design view with the no content
            if (match !== null) {
                var defaultResponse = {
                    "siddhiAppConfig": {
                        "siddhiAppName": match[1],
                        "siddhiAppDescription": match[2],
                        "appAnnotationList": [],
                        "streamList": [],
                        "tableList": [],
                        "windowList": [],
                        "triggerList": [],
                        "aggregationList": [],
                        "functionList": [],
                        "partitionList": [],
                        "sourceList": [],
                        "sinkList": [],
                        "queryLists": {
                            "WINDOW_FILTER_PROJECTION": [],
                            "PATTERN": [],
                            "SEQUENCE": [],
                            "JOIN": []
                        },
                        "finalElementCount": 0
                    },
                    "edgeList": []
                };
                var defaultString = JSON.stringify(defaultResponse);
                result = {
                    status: "success",
                    responseString: defaultString
                };
            } else {
                self.codeToDesignURL = window.location.protocol + "//" + window.location.host + "/editor/design-view";
                $.ajax({
                    type: "POST",
                    url: self.codeToDesignURL,
                    data: window.btoa(code),
                    async: false,
                    success: function (response) {
                        result = {status: "success", responseString: self.options.application.utils.
                        b64DecodeUnicode(response)};
                    },
                    error: function (error) {
                        if (error.responseText) {
                            result = {status: "fail", errorMessage: error.responseText};
                        } else {
                            result = {status: "fail", errorMessage: "Error Occurred while processing your request"};
                        }
                    }
                });
            }
            return result;
        };

        DesignView.prototype.getCode = function (designViewJSON) {
            var self = this;
            var result = {};
            self.designToCodeURL = window.location.protocol + "//" + window.location.host + "/editor/code-view";
            $.ajax({
                type: "POST",
                url: self.designToCodeURL,
                data: window.btoa(designViewJSON),
                async: false,
                success: function (response) {
                    result = {status: "success", responseString: self.options.application.utils.
                    b64DecodeUnicode(response)};
                },
                error: function (error) {
                    if (error.responseText) {
                        result = {status: "fail", errorMessage: error.responseText};
                    } else {
                        result = {status: "fail", errorMessage: "Error Occurred while processing your request"};
                    }
                }
            });
            return result;
        };
        return DesignView;
    });
