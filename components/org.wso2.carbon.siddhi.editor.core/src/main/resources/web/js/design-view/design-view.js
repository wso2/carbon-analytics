/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

define(['require', 'log', 'lodash', 'jquery', 'tool_palette/tool-palette', 'designViewGrid', 'appData',
        'initialiseData'],
    function (require, log, _, $, ToolPalette, DesignViewGrid, AppData, InitialiseData) {

        /**
         * @class DesignView
         * @constructor
         * @class DesignView  Wraps the Ace editor for design view
         * @param {Object} options Rendering options for the view
         * @param application Application data
         * @param jsPlumbInstance js plumb instance for the design grid
         */
        var DesignView = function (options, application ,jsPlumbInstance) {
            var errorMessage1 = 'unable to find design view container in design-view.js';
            var errorMessage2 = 'unable to find application in design-view.js';
            if (!_.has(options, 'container')) {
                log.error(errorMessage1);
            }
            var container = $(_.get(options, 'container'));
            if (!container.length > 0) {
                log.error(errorMessage1);
            }
            if (!_.has(options, 'application')) {
                log.error(errorMessage2);
            }
            this._$parent_el = container;
            this.options = options;
            this.application = application;
            this.jsPlumbInstance =jsPlumbInstance;
        };

        /**
         * @function Renders tool palette in the design container
         */
        DesignView.prototype.renderToolPalette = function () {
            var errMsg = '';
            var toolPaletteContainer =
                this._$parent_el.find(_.get(this.options, 'design_view.tool_palette.container')).get(0);
            if (toolPaletteContainer === undefined) {
                errMsg = 'unable to find tool palette container with selector: '
                    + _.get(this.options, 'design_view.tool_palette.container');
                log.error(errMsg);
            }
            var toolPaletteOpts = _.clone(_.get(this.options, 'design_view.tool_palette'));
            if (toolPaletteOpts === undefined) {
                errMsg = 'unable to find tool palette with selector: '
                    + _.get(this.options, 'design_view.tool_palette');
                log.error(errMsg);
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
            var initialiseData = new InitialiseData(self._$parent_el);
            this.configurationData = initialiseData.initialiseSiddhiAppData(configurationJSON);
            var designViewGridOpts = {};
            _.set(designViewGridOpts, 'container', this.designViewGridContainer);
            _.set(designViewGridOpts, 'configurationData', this.configurationData);
            _.set(designViewGridOpts, 'application', this.application);
            _.set(designViewGridOpts, 'jsPlumbInstance', this.jsPlumbInstance);
            this.designViewGrid = new DesignViewGrid(designViewGridOpts);
            this.designViewGrid.render();
        };

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
            }
            // remove any child nodes from designViewGridContainer if exists
            this.designViewGridContainer.empty();
            // reset the jsPlumb common instance
            if (this.jsPlumbInstance !== undefined) {
                this.jsPlumbInstance.reset();
            }
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
                result = {
                    status: "success",
                    responseJSON: {
                        //appName: match[1],
                        //appDescription: match[2],
                        "siddhiAppConfig":{
                            "streamList":[],
                            "tableList":[],
                            "windowList":[],
                            "triggerList":[],
                            "aggregationList":[],
                            "functionList":[],
                            "partitionList":[],
                            "sourceList":[],
                            "sinkList":[],
                            "queryLists":{
                                "WINDOW_FILTER_PROJECTION":[],
                                "PATTERN":[],
                                "SEQUENCE":[],
                                "JOIN":[]
                            },
                            "finalElementCount":0
                        },
                        "edgeList":[]
                    }
                };
            } else {
                self.codeToDesignURL = window.location.protocol + "//" + window.location.host + "/editor/design-view";
                $.ajax({
                    type: "POST",
                    url: self.codeToDesignURL,
                    data: window.btoa(code),
                    async: false,
                    success: function (response) {
                        result = {status: "success", responseJSON: response};
                    },
                    error: function (error) {
                        console.log(error);
                        if (error.status === 400) {
                            result = {status: "fail", errorMessage: "Siddhi App Contains Errors"};
                            //TODO: remove partition warning from service call
                        } else if (error.responseText === "pattern queries are not supported") {
                            result = {status: "fail", errorMessage: error.responseText};
                        } else if (error.responseText === "sequence queries are not supported") {
                            result = {status: "fail", errorMessage: error.responseText};
                        } else if (error.responseText === "Partitions are not supported") {
                            result = {status: "fail", errorMessage: error.responseText};
                        } else {
                            result = {status: "fail", errorMessage: "Internal Server Error Occurred"};
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
                contentType: "application/json",
                data: designViewJSON,
                async: false,
                success: function (response) {
                    result = {status: "success", responseJSON: window.atob(response)};
                },
                error: function (error) {
                    console.log(error);
                    if (error.status === 400) {
                        result = {status: "fail", errorMessage: "Siddhi App Contains Errors"};
                    } else {
                        result = {status: "fail", errorMessage: "Internal Server Error Occurred"};
                    }
                }
            });
            return result;
        };

        //TODO: replace all the alert with the alert library objects

        return DesignView;
    });
