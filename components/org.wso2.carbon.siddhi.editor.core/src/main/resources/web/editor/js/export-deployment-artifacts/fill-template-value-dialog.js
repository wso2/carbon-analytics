/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['require', 'lodash', 'jquery', 'utils'],
    function (require, _, $, Utils) {

        var constants = {
            TEMPLATED_ELEMENT_REGEX: /\${([^(\\$\\|\\{\\]+)}/g,
            KEY_SYS_CARBON_HOME: "${sys:carbon.home}",
            KEY_RUNTIME: "${sys:wso2.runtime}",
            KEY_CARBON_HOME: "${carbon.home}"
        };

        var FillTemplateValueDialog = function (options) {
            this.container = options.container;
            this.templatedApps = options.payload.templatedSiddhiApps;
            this.deploymentConfig = options.payload.configuration;
            this.templatedKeyList = [];
        };

        FillTemplateValueDialog.prototype.constructor = FillTemplateValueDialog;

        FillTemplateValueDialog.prototype.render = function () {
            var self = this;
            var variableMap = Utils.prototype.retrieveEnvVariables();

            _.forEach(self.templatedApps, function(element, i) {
                self.findTemplatedKeys(element.appContent);
            });

            //find templated content in deployment config
            self.findTemplatedKeys(self.deploymentConfig);

            var allTemplatedKeysHTMLContent = '<div class="clearfix">' +
                '<div class="template-values-div nano">' +
                '<div class = "template-value-elements nano-content" id="template-value-elements-div">';
            var dynamicKeyHTMLContent = '<div id="fillTemplateValueError" ' +
                'class="alert" style="display: none">' +
                'Please provide values for all empty fields' +
                '</div>';

            if (self.templatedKeyList.length == 0) {
                dynamicKeyHTMLContent = dynamicKeyHTMLContent + '<div id="fillTemplateValueError" class="alert">' +
                    'No values are templated to fill.' +
                    '</div>';
            } else {
                _.forEach(self.templatedKeyList, function(key) {
                    var value = variableMap[key] || "";
                    dynamicKeyHTMLContent = dynamicKeyHTMLContent + '<div id="template-value-element-id" class="template-element" style="width: 50%;float: left">' +
                        '<div class="sub-template-value-element-div">' +
                        '<div class="option">' +
                        '<div class="clearfix">' + '<label class="option-name optional-option">' +
                        key +
                        '</label>' + '</div>' + '<div class="clearfix">' +
                        '<input class="option-value" type="text" data-toggle="popover" data-placement="bottom" data-original-title="" title=""'
                        +' value="'+ value + '"'+ '>' +
                        '</div>' + ' <label class="error-message"></label>' + '</div> </div> </div>'

                });
            }

            allTemplatedKeysHTMLContent = allTemplatedKeysHTMLContent + dynamicKeyHTMLContent + '</div></div>';
            self.container.append(allTemplatedKeysHTMLContent);
            $(".nano").nanoScroller();
        };

        FillTemplateValueDialog.prototype.validateTemplatedValues = function (keyValueList) {
            var self = this;
            var isValid = true;
            _.forOwn(keyValueList, function(obj, i) {
                if (obj.value.trim() == "") {
                    isValid = false;
                    self.container.find("#fillTemplateValueError").fadeIn( 300 ).delay( 1500 ).fadeOut( 400 );
                    return false;
                }
            } );
            return isValid;
        };

        FillTemplateValueDialog.prototype.findTemplatedKeys = function (text) {
            var self = this;
            var match = constants.TEMPLATED_ELEMENT_REGEX.exec(text);
            while (match != null) {
                if (match[0].trim() !== constants.KEY_CARBON_HOME && match[0].trim() !== constants.KEY_SYS_CARBON_HOME
                    && match[0].trim() !== constants.KEY_RUNTIME) {
                    var templatedKey = match[0].trim().substring(1).replace("{","").replace("}","")
                    if (!self.templatedKeyList.includes(templatedKey)) {
                        self.templatedKeyList.push(match[0].trim().substring(1).replace("{","").replace("}",""));
                    }
                }
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
