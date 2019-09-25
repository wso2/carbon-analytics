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

define(['jquery', 'lodash', 'log', 'handlebar', 'designViewUtils', 'app/source-editor/completion-engine', 'alerts'],
    function ($, _, log, Handlebars, DesignViewUtils, CompletionEngine, alerts) {

        var TemplateDeploy = function (options) {
            this._options = options;
            console.log(options);
            this._application = options.application;
            this._activateBtn = $(options.activateBtn);
            this._container = $(options.container);
            this._containerToAdjust = $(this._options.containerToAdjust);
            this._verticalSeparator = $(this._options.separator);
            // Register event handler to toggle operator finder.
            this._application.commandManager.registerCommand(options.command.id, {shortcuts: options.command.shortcuts});
            this._application.commandManager.registerHandler(options.command.id, this.toggleTemplateDeploy, this);
            // Compile Handlebar templates.
            this._templates = {
                container: Handlebars.compile($('#template-deploy-side-panel-template').html()),
            };

            console.log(CompletionEngine);
        };

        TemplateDeploy.prototype.render = function () {
            var self = this;

            this._container.append(this._templates.container());
            self._activateBtn.on('click', function (e) {
                e.preventDefault();
                e.stopPropagation();
                if (!$(this).hasClass('disabled')) {
                    self._application.commandManager.dispatch(self._options.command.id);
                }

                $('#template-app-name-select').on('change', function (e) {
                    self.populateTemplateAttributes(e.target.value);
                });

                self._application.utils.retrieveSiddhiAppNames(self.populateTemplateApps, self.handleErrorMsg);


            });

            $('#fill-and-send').on('click', function () {
                console.log("ahhaha");

            });

        };

        TemplateDeploy.prototype.populateTemplateApps = function (data) {
            console.log(data);
            $('#template-app-name-select').empty();
            if (data.length > 0) {
                $('#template-attr-table-body').empty().append(
                    '<tr>' +
                    '   <td><label style="opacity: 0.6">Please select a Siddhi app with template place holders</label></td>\n' +
                    '</tr>');

                $('#template-app-name-select')
                    .append('<option value="-1" disabled="" selected="selected">-- No Siddhi app selected. --</option>');
                data.forEach(function (el) {
                    $('#template-app-name-select')
                        .append('<option>' + el.siddhiAppName + '</option>');
                });
            } else {
                $('#template-app-name-select')
                    .append('<option value="-1" disabled="" selected="selected">-- No saved Siddhi Apps available. --</option>');
            }
        };

        TemplateDeploy.prototype.populateTemplateAttributes = function (fileName) {
            var self = this;

            var openServiceURL = self._application.config.services.workspace.endpoint + "/read";
            var path = "workspace" + self._application.getPathSeperator() + fileName + '.siddhi';

            $.ajax({
                url: openServiceURL,
                type: "POST",
                data: path,
                contentType: "text/plain; charset=utf-8",
                async: false,
                success: function (data, textStatus, xhr) {
                    console.log('awa');
                    var attr = data.content.match(/\${([^(\\$\\|\\{\\]+)}/g);
                    $('#template-attr-table-body').empty();
                    if (attr && attr.length > 0) {
                        var tbody = $('#template-attr-table-body');
                        attr.forEach(function (attr) {
                            tbody.append(
                                '<tr>' +
                                '   <td>' +
                                '       <label>' + attr + '</label>' +
                                '       <input type="text" class="form-control" data-element-type="attribute" name="item-attr" data-type="STRING" aria-required="true">' +
                                '   </td>' +
                                '</tr>');
                        });

                    } else {
                        // alerts.info("No template placeholders found in the Siddhi App.");
                        $('#template-attr-table-body').append(
                            '<tr>' +
                            '   <td>' +
                            '       <label style="opacity: 0.6">Coudn\'t find any template placeholders in the siddhi app</label>' +
                            '   </td>' +
                            '</tr>');
                    }

                },
                error: function (res, errorCode, error) {
                    var msg = _.isString(error) ? error : res.statusText;
                    if (isJsonString(res.responseText)) {
                        var resObj = JSON.parse(res.responseText);
                        if (_.has(resObj, 'Error')) {
                            msg = _.get(resObj, 'Error');
                        }
                    }
                    alerts.error(msg);
                }
            });

        };

        function isJsonString(str) {
            try {
                JSON.parse(str);
            } catch (e) {
                return false;
            }
            return true;
        }


        TemplateDeploy.prototype.handleErrorMsg = function (msg) {
            // todo: check
        };


        TemplateDeploy.prototype.toggleTemplateDeploy = function () {
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


        return {
            TemplateDeploy: TemplateDeploy,
            toggleAddToSource: toggleAddToSource
        }

    });
