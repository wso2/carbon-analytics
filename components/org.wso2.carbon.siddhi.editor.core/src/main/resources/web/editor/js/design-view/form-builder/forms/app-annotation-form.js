/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['require', 'log', 'jquery', 'lodash'],
    function (require, log, $, _) {

        /**
         * @class AppAnnotationForm Creates a forms to collect app level annotations and siddhi app name
         * @constructor
         * @param {Object} options Rendering options for the view
         */
        var AppAnnotationForm = function (options) {
            if (options !== undefined) {
                this.configurationData = options.configurationData;
                this.application = options.application;
                this.consoleListManager = options.application.outputController;
                this.formUtils = options.formUtils;
                this.currentTabId = this.application.tabController.activeTab.cid;
                this.designViewContainer = $('#design-container-' + this.currentTabId);
                this.toggleViewButton = $('#toggle-view-button-' + this.currentTabId);
            }
        };

        /**
         * @function generate form for Partition
         * @param element selected element(partition)
         * @param formConsole Console which holds the form
         * @param formContainer Container which holds the form
         */
        AppAnnotationForm.prototype.generatePropertiesForm = function (element, formConsole, formContainer) {
            var self = this;
            var propertyDiv = $('<div class="clearfix form-min-width"> <div class= "siddhi-app-form-container"> ' +
                '<div id = "define-app-name"> <label> <span class="mandatory-symbol"> * </span> Name </label> ' +
                '<input type="text" id="app-name" class="clearfix name"><label class = "error-message"> </label></div>' +
                '<div id = "define-app-description"> <label> <span class="mandatory-symbol"> * </span>Description ' +
                '</label> <textarea id="app-description" class="clearfix"> </textarea> <label class = "error-message"> ' +
                '</label> </div> </div>' +
                '<div class = "siddhi-app-form-container"> <div class = "define-annotation" </div> </div> </div> ');

            formContainer.html(propertyDiv);
            self.formUtils.buildFormButtons(formConsole.cid);
            $(".overlayed-container").fadeTo(200, 1);
            // design view container and toggle view button are enabled
            self.designViewContainer.addClass('disableContainer');
            self.toggleViewButton.addClass('disableContainer');

            var siddhiAppConfig = self.configurationData.getSiddhiAppConfig();
            var siddhiAppName = siddhiAppConfig.getSiddhiAppName();
            var siddhiAppDescription = siddhiAppConfig.getSiddhiAppDescription();
            var appAnnotationObjects = siddhiAppConfig.getAppAnnotationListObjects();

            $('#app-name').val(siddhiAppName);
            $('#app-description').val(siddhiAppDescription);
            self.formUtils.renderAnnotationTemplate("define-annotation", appAnnotationObjects);

            self.formUtils.addEventListenerToRemoveRequiredClass();
            self.formUtils.addEventListenerToShowInputContentOnHover();

            self.formUtils.initPerfectScroller(formConsole.cid);

            // 'Submit' button action
            $('#' + formConsole.cid).on('click', '#btn-submit', function () {

                self.formUtils.removeErrorClass();

                var appName = $('#app-name').val().trim();
                var appDescription = $('#app-description').val().trim();
                var isErrorOccurred = false;

                if (appName === "") {
                    self.formUtils.addErrorClass($('#define-app-name #app-name'));
                    $('#define-app-name').find('.error-message').text("Siddhi App name is required");
                    isErrorOccurred = true;
                    return;
                }
                if (appDescription === "") {
                    self.formUtils.addErrorClass($('#define-app-description #app-description'));
                    $('#define-app-description').find('.error-message').text("Siddhi App description is required");
                    isErrorOccurred = true;
                    return;
                }

                if (!isErrorOccurred) {
                    siddhiAppConfig.setSiddhiAppName(appName);
                    siddhiAppConfig.setSiddhiAppDescription(appDescription);
                    siddhiAppConfig.clearAppAnnotationList();
                    siddhiAppConfig.clearAppAnnotationListObjects()

                    var annotationStringList = [];
                    var annotationObjectList = [];
                    var annotationNodes = $('#annotation-div').jstree(true)._model.data['#'].children;
                    self.formUtils.buildAnnotation(annotationNodes, annotationStringList, annotationObjectList);
                    _.forEach(annotationStringList, function (annotation) {
                        //remove the @ and add @App: from the annotation
                        var appAnnotation = "@App:" + annotation.slice(1);
                        siddhiAppConfig.addAppAnnotation(appAnnotation);
                    });
                    _.forEach(annotationObjectList, function (annotation) {
                        siddhiAppConfig.addAppAnnotationObject(annotation);
                    });

                    // update the siddhi app name displayed on the canvas
                    var siddhiAppNameNode = $('#' + self.currentTabId + '-siddhiAppNameId');
                    siddhiAppNameNode.html(appName);
                    //update the siddhi app desc displayed on the canvas
                    var siddhiAppDescriptionNode = $('#siddhi-app-desc-node');
                    siddhiAppDescriptionNode.html(appDescription);

                    // set the isDesignViewContentChanged to true
                    self.configurationData.setIsDesignViewContentChanged(true);
                    // close the form window
                    self.consoleListManager.removeFormConsole(formConsole);
                }

            });
            // 'Cancel' button action
            $('#' + formConsole.cid).on('click', '#btn-cancel', function () {
                // close the form window
                self.consoleListManager.removeFormConsole(formConsole);
            });

        };

        return AppAnnotationForm;
    });