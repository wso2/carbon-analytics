/*
 *
 *  * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *  *
 *  * WSO2 Inc. licenses this file to you under the Apache License,
 *  * Version 2.0 (the "License"); you may not use this file except
 *  * in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied. See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 */
define(['require', 'jquery', 'lodash', 'log', 'smart_wizard', 'app/source-editor/completion-engine', 'alerts',
        'inputOutputMapper', 'inputOptionConfigurator', 'dataMapper', 'outputConfigurator', 'handlebar',
        'dataMapperUtil', 'ace/ace', 'app/source-editor/editor', 'js/async-api/asyncapi-rest-client', 'alerts',
        'js/async-api/constants'],

    function (require, $, _, log, smartWizard, CompletionEngine, Alerts, InputOutputMapper,
              InputOptionConfigurator, DataMapper, OutputConfigurator, Handlebars, DataMapperUtil,
              ace, AsyncAPIEditor, AsyncAPIRESTClient, alerts, AsyncAPIConstants) {
    
        var AsyncAPIView = function (initOpts) {
            this.__$parent_el_container = $(initOpts.container);
            var self = this;
            this.etlWizardContainer = this.__$parent_el_container.find(_.get(initOpts, 'etl_wizard.container'));
            this.canvasContainer = this.__$parent_el_container.find(_.get(initOpts, 'canvas.container'));
            this.sourceContainer = this.__$parent_el_container.find(_.get(initOpts, 'source.container'));
            this.designContainer = this.__$parent_el_container.find(_.get(initOpts, 'design_view.container'));
            this.previewContainer = this.__$parent_el_container.find(_.get(initOpts, 'preview.container'));
            this.toggleControlsContainer = this.__$parent_el_container.find('.toggle-controls-container');

            this.asyncAPIViewContainer = initOpts.asyncAPIViewContainer;
            this.asyncAPIViewContainer.removeClass("hide-div");
            this.asyncAPISpecContainer = this.asyncAPIViewContainer.find(_.get(initOpts, 'async_api_view.specContainer'));
            this.asyncAPIGenContainer = this.asyncAPIViewContainer.find(_.get(initOpts, 'async_api_view.generatorContainer'));
            this.asyncAPIYamlContainer = this.asyncAPIViewContainer.find(_.get(initOpts, 'async_api_view.yamlContainer'));

            this.renderCallback = this.renderCallback.bind(this);
            self.__options = initOpts;
            self.__editorInstance = initOpts.editorInstance;
            self.__app = initOpts.application;
            self.__tab = initOpts.application.tabController.getActiveTab();
            self.asyncAPIDefYaml = initOpts.asyncAPIDefYaml;
            this.divId = $(this.asyncAPIYamlContainer[0]).attr('id');
            this._mainEditor = new AsyncAPIEditor({
                divID: this.divId,
                realTimeValidation: true,
                autoCompletion: false,
                mode: "ASYNC_API_MODE",
            });
            self.asyncAPISpecContainerDom = this.asyncAPISpecContainer[0];
            self.editor = ace.edit(this.divId);
            self.editor.getSession().on('change', this.renderCallback);
            self._fromGenerator = initOpts.fromGenerator;
            self._contentChanged = false;
            if (initOpts.fromGenerator) {
                $(self.toggleControlsContainer[0]).find('.async-api-add-update-button').html('Add Async API');
            } else {
                $(self.toggleControlsContainer[0]).find('.async-api-add-update-button').html('Update Async API');
            }

            self.hideOthers();
            self.hideInternalViews();
            self.renderAsyncAPIView();

            $(self.toggleControlsContainer[0]).find('#asyncbtn-to-code-view').removeClass('hide-div');
            $(self.toggleControlsContainer[0]).find('#asyncbtn-asyncapi-view').addClass('hide-div');

            $('.toggle-controls-container #asyncbtn-to-code-view').on('click', function(e) {
                e.preventDefault();
                if (self._contentChanged) {
                    self.__editorInstance.getSession().setValue(self._changedEditorText);
                }
                self.sourceContainer.show();
                self.asyncAPIViewContainer.hide();
                $(self.toggleControlsContainer[0]).find('.toggle-view-button').removeClass('hide-div');
                $(self.toggleControlsContainer[0]).find('.wizard-view-button').removeClass('hide-div');
                var asyncAPIAddUpdateButton = $(self.toggleControlsContainer[0]).find('.async-api-add-update-button');
                asyncAPIAddUpdateButton.addClass('hide-div');
                var codeViewButton = $(self.toggleControlsContainer[0]).find('#asyncbtn-to-code-view');
                codeViewButton.addClass('hide-div');
                var asyncAPIViewButton = $(self.toggleControlsContainer[0]).find('#asyncbtn-asyncapi-view');
                asyncAPIViewButton.removeClass('hide-div');
            });

            $('.toggle-controls-container #btn-add-update-async-btn').on('click', function(e) {
                e.preventDefault();
                var response = AsyncAPIRESTClient.getSiddhiElements(self.__tab.getFile().getContent());
                if (response.status === "success") {
                    self.JSONObject = JSON.parse(self.__app.utils.b64DecodeUnicode(response.response));
                    if (self.__app.tabController.getActiveTab().getFile().getName().replace(".siddhi", "").localeCompare(self.JSONObject.siddhiAppConfig.siddhiAppName) === 0) {
                        var asyncApiDef = '@App:asyncAPI("""' + self.editor.getSession().getValue() + '""")';
                        for (var i=0; i<self.JSONObject.siddhiAppConfig.appAnnotationList.length; i++) {
                            var asyncAPIAnnotationRegex = new RegExp(AsyncAPIConstants.ASYNC_API_ANNOTATION_REGEX);
                            var asyncAPIAnnotation = self.JSONObject.siddhiAppConfig.appAnnotationList[i].match(asyncAPIAnnotationRegex);
                            if (asyncAPIAnnotation.length > 0) {
                                self.JSONObject.siddhiAppConfig.appAnnotationList.splice(i, 1);
                            }
                        }
                        self.JSONObject.siddhiAppConfig.appAnnotationList.push(asyncApiDef);
                        console.log(self.JSONObject);
                        var editorTextResponse = AsyncAPIRESTClient.getCodeView(JSON.stringify(self.JSONObject));
                        if (editorTextResponse.status === "success") {
                            self._changedEditorText = self.__app.utils.b64DecodeUnicode(editorTextResponse.responseString);
                            if (self._fromGenerator && !self._contentChanged) {
                                alerts.info("Async API Added.");
                                self._contentChanged = true;
                            } else {
                                alerts.info("Async API Updated.");
                            }
                        } else if (response.status === "fail") {
                            alerts.error(response.errorMessage);
                        }
                    }
                } else if (response.status === "fail") {
                    alerts.error(response.errorMessage);
                }


            });
        };
        //Constructor for the AsyncAPIView
        AsyncAPIView.prototype.constructor = AsyncAPIView;

        AsyncAPIView.prototype.hideInternalViews = function () {
            var self = this;
            self.asyncAPISpecContainer.removeClass('hide-div');
            self.asyncAPIYamlContainer.removeClass('hide-div');
            self.asyncAPIGenContainer.addClass('hide-div');
        }
        AsyncAPIView.prototype.hideOthers = function () {
            var self = this;
            self.canvasContainer.addClass('hide-div');
            self.previewContainer.addClass('hide-div');
            self.designContainer.addClass('hide-div');
            self.sourceContainer.addClass('hide-div');
            $(self.toggleControlsContainer[0]).find('.toggle-view-button').addClass('hide-div');
            $(self.toggleControlsContainer[0]).find('.wizard-view-button').addClass('hide-div');
            $(self.toggleControlsContainer[0]).find('.async-api-add-update-button').removeClass('hide-div');
            $(self.toggleControlsContainer[0]).find('#asyncbtn-to-code-view').removeClass('hide-div');
            $(self.toggleControlsContainer[0]).find('#asyncbtn-asyncapi-view').addClass('hide-div');
            self.etlWizardContainer.addClass('hide');
            self.asyncAPIViewContainer.addClass('etl-wizard-view-enabled');
        }

        AsyncAPIView.prototype.renderAsyncAPIView = function () {
            var self = this;
            self.editor.getSession().setValue(self.asyncAPIDefYaml);
            self.editor.resize(true);
            window.getAsyncAPIUI(self.asyncAPISpecContainerDom, self.asyncAPIDefYaml);
        };

        AsyncAPIView.prototype.renderCallback = function () {
            var self = this;
            self.checkVar = "dsfsdf";
            console.log(self.checkVar);
            if (self.editor.getSession().getValue() !== "" && self.asyncAPIDefYaml !== self.editor.getSession().getValue()) {
                self._contentChanged = true;
                window.getAsyncAPIUI(self.asyncAPISpecContainerDom, self.editor.getSession().getValue());
            }
        };

        return AsyncAPIView;
    });
