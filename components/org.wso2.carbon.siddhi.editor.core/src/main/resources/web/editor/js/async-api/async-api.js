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
define(['require', 'jquery', 'lodash', 'log', 'smart_wizard', 'app/source-editor/completion-engine', 'alerts', 'inputOutputMapper', 'inputOptionConfigurator', 'dataMapper', 'outputConfigurator', 'handlebar', 'etlWizardUtil', 'dataMapperUtil'],

    function (require, $, _, log, smartWizard, CompletionEngine, Alerts, InputOutputMapper, InputOptionConfigurator, DataMapper, OutputConfigurator, Handlebars, etlWizardUtil, DataMapperUtil) {

        /**
         * Constants used by the wizard
         */
        const constants = {
            CLASS_WIZARD_MODAL_HEADER: '.header-content',
            CLASS_WIZARD_MODAL_BODY: '.body-content',
            CLASS_WIZARD_MODAL_FOOTER: '.footer-content',
            ID_ETL_WIZARD_BODY: '#ETLWizardForm',
            SERVER_URL: window.location.protocol + "//" + window.location.host + "/editor/",
            SOURCE_TYPE: 'source',
            SINK_TYPE: 'sink',
            SUPPORTED_DATA_TYPES: ['INT', 'LONG', 'FLOAT', 'DOUBLE', 'STRING', 'BOOL'],
            commands: {
                EXPORT_FOR_DOCKER: 'export-for-docker',
                EXPORT_FOR_KUBERNETES: 'export-for-kubernetes',
                DEPLOY_TO_SERVER: 'deploy-to-server',
                TOGGLE_EVENT_SIMULATOR: 'toggle-event-simulator'
            }
        };
        var ETLWizard = function (initOpts) {
            var self = this;
            this.__options = initOpts;
            this.__app = initOpts.application;
            this.__tab = initOpts.application.tabController.getActiveTab();
            this.__$parent_el_container = $(initOpts.container);
            self.asyncAPIDefYaml = initOpts.asyncAPIDefYaml;
            self.render();
        };
        //Constructor for the ETLWizard
        ETLWizard.prototype.constructor = ETLWizard;
        ETLWizard.prototype.render = function (viewData) {
            var self = this;
            var etlWizardContainer = this.__$parent_el_container.find(_.get(this.__options, 'etl_wizard.container'));
            var canvasContainer = this.__$parent_el_container.find(_.get(this.__options, 'canvas.container'));
            var sourceContainer = this.__$parent_el_container.find(_.get(this.__options, 'source.container'));
            var designContainer = this.__$parent_el_container.find(_.get(this.__options, 'design_view.container'));
            var previewContainer = this.__$parent_el_container.find(_.get(this.__options, 'preview.container'));
            var toggleControlsContainer = this.__$parent_el_container.find('.toggle-controls-container');
            var asyncAPIViewContainer = this.__$parent_el_container.find(_.get(this.__options, 'async_api_view.container'));
            var asyncAPIYamlContainer = this.__$parent_el_container.find(_.get(this.__options, 'async_api_view.yamlContainer'));
            var asyncAPISpecContainer = this.__$parent_el_container.find(_.get(this.__options, 'async_api_view.specContainer'));
            // var asyncAPIViewContainer = this.__$parent_el_container.find(this.__options.async_api_view.container);
            console.log(asyncAPIViewContainer.length);
            canvasContainer.addClass('hide-div');
            previewContainer.addClass('hide-div');
            designContainer.addClass('hide-div');
            sourceContainer.addClass('hide-div');
            toggleControlsContainer.addClass('hide');
            etlWizardContainer.addClass('hide');
            asyncAPIViewContainer.addClass('etl-wizard-view-enabled');
            console.log("@@@@@@@@@@@@@@@@@##########################");
            $(asyncAPIYamlContainer[0]).find("p").text("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
            window.getAsyncAPIUI(asyncAPISpecContainer[0], self.asyncAPIDefYaml);
        };
        return ETLWizard;
    });
