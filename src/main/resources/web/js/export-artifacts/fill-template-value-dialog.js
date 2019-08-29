/**
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

define(['require', 'lodash', 'jquery', 'log','ace/ace','app/source-editor/editor'],
    function (require, _, $, log,ace,SiddhiEditor) {

        var constants = {
            TEMPLATED_ELEMENT_REGEX: /\${([^\\$\\]+)}/g
        };

        var FillTemplateValueDialog = function (options) {
            this.container = options.container;
            this.templatedApps = options.payload.templatedSiddhiApps;
            this.templatedKeyList = [];
        };

        FillTemplateValueDialog.prototype.constructor = FillTemplateValueDialog;

        FillTemplateValueDialog.prototype.render = function () {
            var self = this;

            _.forEach(self.templatedApps, function(element, i) {
                self.findTemplatedKeys(element.appContent)
            });

            var allTemplatedKeysHTMLContent = '<div class="clearfix form-min-width">' +
                '<div class="source-sink-form-container source-div">' +
                '<div id="define-source">Add values for templates</div>' +
                '<div class = "source-sink-map-options" id="source-options-div">';
            var dynamicKeyHTMLContent = "";

            _.forEach(self.templatedKeyList, function(key) {
                dynamicKeyHTMLContent = dynamicKeyHTMLContent + '<div id="source-options" class="template-element">' +
                    '<div class="sub-source-options-div">' +
                    '<div class="option">' +
                    '<div class="clearfix">' + '<label class="option-name optional-option">' +
                    key +
                    '</label>' + '</div>' + '<div class="clearfix">' +
                    '<input class="option-value" type="text" data-toggle="popover" data-placement="bottom" data-original-title="" title="">' +
                    '</div>' + ' <label class="error-message"></label>' + '</div> </div> </div>'

            });

            allTemplatedKeysHTMLContent = allTemplatedKeysHTMLContent + dynamicKeyHTMLContent + '</div></div></div>';
            self.container.append(allTemplatedKeysHTMLContent);
        };


        FillTemplateValueDialog.prototype.selectAll = function () {

        };

        FillTemplateValueDialog.prototype.findTemplatedKeys = function (text) {
            var self = this;
            var match = constants.TEMPLATED_ELEMENT_REGEX.exec(text);
            while (match != null) {
                // matched text: match[0]
                // match start: match.index
                // capturing group n: match[n]
                self.templatedKeyList.push(match[0].substring(1));
                match = constants.TEMPLATED_ELEMENT_REGEX.exec(text);
            }

        };

        FillTemplateValueDialog.prototype.getTemplatedKeyValues = function () {
            var self = this;
            var keyValueList = [];
            self.container.find(".template-element").each(function(i, obj) {
                var templateKeyValue = {};
                templateKeyValue["key"] = $(obj).find(".option-name").text();
                templateKeyValue["value"] = $(obj).find(".option-value").val();
                keyValueList.push(templateKeyValue);
            });
            return keyValueList;

        };

        FillTemplateValueDialog.prototype.show = function () {
            this._fileOpenModal.modal('show');
        };

        return FillTemplateValueDialog;
    });
