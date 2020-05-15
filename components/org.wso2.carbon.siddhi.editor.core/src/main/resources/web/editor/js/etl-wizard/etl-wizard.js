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
define(['require', 'jquery', 'lodash', 'log','smart_wizard'],

    function (require, $, _, log, smartWizard) {

        /**
         * Constants used by the wizard
         */
        var constants = {
            CLASS_WIZARD_MODAL_HEADER: ".modal-header",
            CLASS_WIZARD_MODAL_BODY: ".modal-body",
            ID_ETL_WIZARD_BODY: "#form-modal"
        };

        var ETLWizard = function (initOpts) {
            this.__options = initOpts;
            this.__app = initOpts.application;
            this.__$parent_el_container = $(initOpts.container);
            this.__parentWizardForm = this.constructWizardHTMLElements($('#FormWizard').clone());
        };

        //Constructor for the ETLWizard
        ETLWizard.prototype.constructor = ETLWizard;

        //Construct and return wizard skeleton
        ETLWizard.prototype.constructWizardHTMLElements =  function(wizardObj){
            var modalHeader = wizardObj.find(constants.CLASS_WIZARD_MODAL_HEADER);
            var headerTitle = '<h4 class="modal-title form-wizard-title" id="idHeaderTitle"> ETL Wizard </h4>';
            var lineDivider = '<hr class="form-wizard-hr">';
            modalHeader.append(headerTitle).append(lineDivider);
            this.__etlWizardBody = wizardObj.find(constants.ID_ETL_WIZARD_BODY);
            var wizardStepsHTMLContent = '<ul id="form-steps" class="docker_export_header">' +
                '<li><a href="#step-1" class="link-disabled">Step 1<br/><small>Select Siddhi Apps</small></a>' +
                '</li>' +
                '<li><a href="#step-2" class="link-disabled">Step 2<br/><small>Template Siddhi Apps</small></a>' +
                '</li>' +
                '<li><a href="#step-3" class="link-disabled">Step 3<br/>' +
                '<small>Update Streaming Integrator<br/>Configurations</small></a>' +
                '</li>' +
                '<li><a href="#step-4" class="link-disabled">Step 4<br/><small>Populate Template' +
                '<br/>Arguments</small></a></li>' +
                '</ul>';

            var wizardFormStepContent = '<div id="form-containers"></div>';
            this.__etlWizardBody.append(wizardStepsHTMLContent).append(wizardFormStepContent);
            return wizardObj;
        };

        ETLWizard.prototype.render =  function(){
            var self = this;
            var etlWizardContainer = this.__$parent_el_container.find(_.get(this.__options, 'etl_wizard.container'));
            var canvasContainer = this.__$parent_el_container.find(_.get(this.__options, 'canvas.container'));
            var sourceContainer = this.__$parent_el_container.find(_.get(this.__options, 'source.container'));
            var designContainer = this.__$parent_el_container.find(_.get(this.__options, 'design_view.container'));
            var previewContainer = this.__$parent_el_container.find(_.get(this.__options, 'preview.container'));

            etlWizardContainer.append(this.__parentWizardForm);

            this.__etlWizardBody.smartWizard({
                selected: 0,
                keyNavigation: false,
                autoAdjustHeight: false,
                theme: 'default',
                transitionEffect: 'slideleft',
                showStepURLhash: false,
                contentCache: false,
                toolbarSettings: {
                    toolbarPosition: 'bottom'
                }
            });



            canvasContainer.removeClass('show-div').addClass('hide-div');
            previewContainer.removeClass('show-div').addClass('hide-div');
            designContainer.removeClass('show-div').addClass('hide-div');
            sourceContainer.removeClass('show-div').addClass('hide-div');
            etlWizardContainer.addClass('etl-wizard-view-enabled');

        };

        return ETLWizard;

    });