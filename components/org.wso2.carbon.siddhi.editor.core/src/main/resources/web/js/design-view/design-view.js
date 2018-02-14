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

define(['require', 'log', 'lodash', 'jquery', 'app/tool-palette/tool-palette', 'designViewGrid'],
    function (require, log, _, $, ToolPalette, DesignViewGrid) {

        var DesignView = function (options) {
            if (!_.has(options, 'container')) {
                throw "container is not defined."
            }
            var container = $(_.get(options, 'container'));
            if (!container.length > 0) {
                throw "container not found."
            }
            this._$parent_el = container;
            this.options = options;
        };

        //TODO: add logs,use jquery($) if needed
        DesignView.prototype.render = function () {
            var self = this;
            var canvasContainer = this._$parent_el.find(_.get(this.options, 'canvas.container'));
            var designViewOpts = {};
            _.set(designViewOpts, 'container', canvasContainer.get(0));
            self.designViewContainer = this._$parent_el.find(_.get(this.options, 'design_view.container'));

            // check whether container element exists in dom
            if (!self.designViewContainer.length > 0) {
                errMsg = 'unable to find container for file composer with selector: '
                    + _.get(this.options, 'design_view.container');
                log.error(errMsg);
                throw errMsg;
            }

            var toolPaletteContainer = self._$parent_el.find(_.get(self.options, 'design_view.tool_palette.container')).get(0);
            var toolPaletteOpts = _.clone(_.get(self.options, 'design_view.tool_palette'));
            toolPaletteOpts.container = toolPaletteContainer;
            self.toolPalette = new ToolPalette(toolPaletteOpts);
            self.toolPalette.render();
            //TODO: set the grid from jsplumb

            var designViewGrid = new DesignViewGrid(designViewOpts);

        };

        DesignView.prototype.getDesignViewContainer = function () {
            if (this.designViewContainer !== undefined) {
                return this.designViewContainer;
            } else {
                return undefined;
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

        return DesignView;
    });