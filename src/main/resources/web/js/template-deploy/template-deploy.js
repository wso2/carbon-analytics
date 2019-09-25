/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

define(['jquery', 'lodash', 'log', 'handlebar', 'designViewUtils', 'app/source-editor/completion-engine'],
    function ($, _, log, Handlebars, DesignViewUtils, CompletionEngine) {

        var TemplateDeploy = function (options) {
            this._options = options;
            this._application = options.application;
            this._activateBtn = $(options.activateBtn);
            this._container = $(options.container);
            this._containerToAdjust = $(this._options.containerToAdjust);
            this._verticalSeparator = $(this._options.separator);
            // Register event handler to toggle operator finder.
            this._application.commandManager.registerCommand(options.command.id, { shortcuts: options.command.shortcuts });
            this._application.commandManager.registerHandler(options.command.id, this.toggleOperatorFinder, this);
            // Compile Handlebar templates.
            this._templates = {
                container: Handlebars.compile($('#template-deploy-side-panel-template').html()),
            };
        };

        TemplateDeploy.prototype.render = function () {
            var self = this;
            console.log(this._templates.container());
            this._container.append(this._templates.container());
            self._activateBtn.on('click', function (e) {
                e.preventDefault();
                e.stopPropagation();
                if (!$(this).hasClass('disabled')) {
                    self._application.commandManager.dispatch(self._options.command.id);
                }

                // If the operators are not available, get them from the completion engine.
                if (!self._operators) {
                    
                }
            });
            
            
        };

        TemplateDeploy.prototype.toggleOperatorFinder = function () {
            if (this._activateBtn.parent('li').hasClass('active')) {
                this._container.parent().width('0px');
                this._containerToAdjust.css('padding-left', this._options.leftOffset);
                this._verticalSeparator.css('left', this._options.leftOffset - this._options.separatorOffset);
                this._activateBtn.parent('li').removeClass("active");
            } else {
                this._activateBtn.tab('show');
                this._container.parent().width(this._options.defaultWidth);
                this._containerToAdjust.css('padding-left', this._options.defaultWidth);
                this._verticalSeparator.css('left', this._options.defaultWidth - this._options.separatorOffset);
            }
        };


        var toggleAddToSource = function (disable) {
            var elements = $('#template-deploy').find('.result-content a.add-to-source');
            if (disable) {
                elements.addClass('disabled');
            } else {
                elements.removeClass('disabled');
            }
        };

        var toggleSidePanel = function () {
            $()
        }

        return {
            TemplateDeploy: TemplateDeploy,
            toggleAddToSource: toggleAddToSource
        }

    });
