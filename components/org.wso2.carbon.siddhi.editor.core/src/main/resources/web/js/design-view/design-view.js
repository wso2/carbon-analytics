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

define(['require', 'log', 'lodash', 'jquery', 'tool_palette/tool-palette', 'designViewGrid'],
    function (require, log, _, $, ToolPalette, DesignViewGrid) {

        /**
         * @class DesignView
         * @constructor
         * @class DesignView  Wraps the Ace editor for design view
         * @param {Object} options Rendering options for the view
         * @param application Application data
         * @param siddhiAppContent Siddhi application content
         */
        var DesignView = function (options, application, siddhiAppContent) {
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
            this.siddhiAppContent = siddhiAppContent;
            this.application = application;
        };

        DesignView.prototype.render = function () {
            var errMsg = '';
            var toolPaletteContainer = this._$parent_el.find(_.get(this.options, 'design_view.tool_palette.container'))
                .get(0);
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

            var designViewGridContainer = this._$parent_el.find(_.get(this.options, 'design_view.grid_container'));
            if (!designViewGridContainer.length > 0) {
                errMsg = 'unable to find design view grid container with selector: '
                    + _.get(this.options, 'design_view.grid_container');
                log.error(errMsg);
            }
            var designViewGridOpts = {};
            _.set(designViewGridOpts, 'container', designViewGridContainer);
            _.set(designViewGridOpts, 'appData', this.siddhiAppContent);
            _.set(designViewGridOpts, 'application', this.application);
            var designViewGrid = new DesignViewGrid(designViewGridOpts);
            designViewGrid.render();
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

        return DesignView;
    });
