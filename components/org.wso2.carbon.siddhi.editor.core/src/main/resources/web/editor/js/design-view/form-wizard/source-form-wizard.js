/**
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
define(['require', 'lodash', 'jquery', 'log', 'appData','configurationData','edge', 'designViewUtils','dropElements' ,'smart_wizard','stream', 'formBuilder','formUtils', 'stream','sourceOrSinkAnnotation', 'mapAnnotation' ,'attribute','jsonValidator','constants', 'payloadOrAttribute'],
    function (require, _ , $, log, AppData, ConfigurationData, Edge, DesignViewUtils,DropElements, smartWizard, Stream , FormBuilder, FormUtils, Stream , SourceOrSinkAnnotation, MapAnnotation, Attribute, JSONValidator, Constants, PayloadOrAttribute) {

        var self;

        var SourceFormWizard = function (option){
            self = this;
            this.option = option;
            this._openFormModal = $('#FormWizard').clone();

            this.configurationData = option.configurationData;
            this.application = option.application;
            this.jsPlumbInstance = option.jsPlumbInstance;
            this.designGrid = option.designGrid;
            this.formUtils = option.formUtils;
            this.formUtils = new FormUtils(this.application, this.configurationData, this.jsPlumbInstance);
            this._uniqueStreamId = option.uniqueStreamId;
            this._uniqueId = option.uniqueId;
            this._isEditflow = option.isEditflow;
            this._isDeleteflow = option.isDeleteflow;
            this._StreamId = option.StreamId;
            this._settingIcon = "" + this._uniqueId + "-dropSourceSettingsId";

            var formOptions = {};
            _.set(formOptions, 'configurationData', self.configurationData);
            _.set(formOptions, 'application', self.application);
            _.set(formOptions, 'formUtils', self.formUtils);
            _.set(formOptions, 'dropElementInstance', this);
            _.set(formOptions, 'jsPlumbInstance', self.jsPlumbInstance);
            _.set(formOptions, 'designGrid', self.designGrid);
            this.formBuilder = new FormBuilder(formOptions);

            this._btnSubmitForm =  $('' + '<form><button type="button" class="btn btn-primary hidden" id="submit-btn">Submit</button></form>' );

            if(this._isEditflow === undefined){
                if(this._isDeleteflow === undefined){
                    this._sourceOptions = {};
                    _.set(this._sourceOptions, 'id', this._uniqueId);
                    _.set(this._sourceOptions, 'previousCommentSegment',  self.previousCommentSegment);
                    _.set(this._sourceOptions, 'connectedElementName',  self.connectedElementName);
                    _.set(this._sourceOptions, 'connectedRightElementName',  self.connectedRightElementName);
                    _.set(this._sourceOptions, 'annotationType',  'SOURCE');
                    _.set(this._sourceOptions, 'type',  self.type);
                    _.set(this._sourceOptions, 'options',  self.options);
                    _.set(this._sourceOptions, 'map',  self.map);
                    var source = new SourceOrSinkAnnotation(this._sourceOptions);
                }
                this._streamObject = {};
                _.set(this._streamObject, 'id', this._uniqueStreamId);
                _.set(this._streamObject, 'previousCommentSegment', self.previousCommentSegment);
                _.set(this._streamObject, 'name', self.name);
                _.set(this._streamObject, 'connectedSourceID', self.connectedSourceID);
                _.set(this._streamObject, 'attributeList', self.attributeList);
                _.set(this._streamObject, 'annotationList', self.annotationList);
                _.set(this._streamObject, 'annotationListObjects', self.annotationListObjects);
                var stream = new Stream(this._streamObject);
            }else if(this._isDeleteflow !== undefined){
                  this._streamObject = {};
                  _.set(this._streamObject, 'id', this._uniqueStreamId);
                  _.set(this._streamObject, 'previousCommentSegment', self.previousCommentSegment);
                  _.set(this._streamObject, 'name', self.name);
                  _.set(this._streamObject, 'attributeList', self.attributeList);
                  _.set(this._streamObject, 'annotationList', self.annotationList);
                  _.set(this._streamObject, 'annotationListObjects', self.annotationListObjects);
                  var stream = new Stream(this._streamObject);
            }else if(this._isEditflow !== undefined){
                if(this._openFormModal.find('#exist-stream-checkbox').is(':checked') === false){

                    this._streamOption = {};
                    _.set(this._streamOption, 'id',this._StreamId);
                    _.set(this._streamOption, 'previousCommentSegment', self.previousCommentSegment);
                    _.set(this._streamOption, 'name', self.name);
                    _.set(this._streamOption, 'attributeList', self.attributeList);
                    _.set(this._streamOption, 'annotationList', self.annotationList);
                    _.set(this._streamOption, 'annotationListObjects', self.annotationListObjects);
                    var stream = new Stream(this._streamOption);
                }
            }
        };

        SourceFormWizard.prototype.constructor = SourceFormWizard;

        SourceFormWizard.prototype.render = function () {
            var self = this;
            var uniqueStreamId = this._uniqueStreamId;
            var StreamId = this._StreamId;
            var uniqueId = this._uniqueId;
            var streamObject = this._streamObject;
            var streamOption = this._streamOption;
            var sourceObject = this._sourceOptions;
            var isEditflow = this._isEditflow;
            var isDeleteflow = this._isDeleteflow;
            var form = self._openFormModal.find('#source-form');
            var settingIcon = this._settingIcon;

            // Toolbar extra buttons
            var btnSubmitForm = this._btnSubmitForm;

            var sourceObj = sourceObject;

            btnSubmitForm.find('#submit-btn').on('click', function () {
                if(isEditflow === undefined && isDeleteflow === undefined){
                    var errorMap = self.validateSourceMap();
                    if(errorMap === undefined || errorMap === true){
                        self.designGrid.handleSourceForWizard(100, 50, false, "Source",uniqueId);
                        self.savedSourceForm();
                        var source = new SourceOrSinkAnnotation(sourceObj);
                        self.configurationData.getSiddhiAppConfig().addSource(source);

                        var streamList = self.configurationData.getSiddhiAppConfig().getStreamList();
                        if (self._openFormModal.find('#exist-stream-checkbox').is(':checked')){
                            if(self._openFormModal.find('#exist-stream-list :selected')) {
                                var j = self._openFormModal.find('#exist-stream-list :selected').val();
                                var ExistStreamId = streamList[j].getId();
                                addEdges(ExistStreamId, uniqueId);
                            }
                        }else{
                            var scratchStreamName = streamObject.name;
                            var streamToolTip;
                            self.designGrid.handleStreamForWizard(100, 200, false,uniqueStreamId,scratchStreamName,streamToolTip,streamObject);
                            self.SaveStreamForm();

                            var stream = new Stream(streamObject);
                            self.configurationData.getSiddhiAppConfig().addStream(stream);

                            var scratchStreamObject = self.configurationData.getSiddhiAppConfig().getStream(uniqueStreamId);
                            scratchStreamObject.setAttributeList(streamObject.attributeList);
                            scratchStreamObject.setAnnotationList(streamObject.annotationList);

                            addEdges(uniqueStreamId, uniqueId);
                        }
                        self._openFormModal.modal('hide');
                    }
                }else if(isEditflow === true){
                     var errorMap = self.validateSourceMapForEdit();
                     var id;
                     var streamList = self.configurationData.getSiddhiAppConfig().getStreamList();
                     if(errorMap === undefined){
                        if(self._openFormModal.find('#exist-stream-checkbox').is(':checked')){
                            var j = self._openFormModal.find('#exist-stream-list :selected').val();
                            id = streamList[j].getId();

                            if(uniqueStreamId !== undefined){
                                if(id !== uniqueStreamId){
                                    removeEdges(uniqueStreamId, uniqueId);
                                }
                            }
                            self.SaveStreamFormForEdit();

                            addEdges(id, uniqueId);

                            self.savedSourceFormForEdit();

                            self._openFormModal.modal('hide');

                        }else{
                            if(uniqueStreamId !== undefined){
                                removeEdges(uniqueStreamId,uniqueId);
                            }

                            var scratchStreamName = streamOption.name;

                            var streamToolTip;
                            self.designGrid.handleStreamForWizard(180, 200, false,StreamId,scratchStreamName,streamToolTip,streamOption);
                            self.SaveStreamForm();

                            var stream = new Stream(streamOption);
                            self.configurationData.getSiddhiAppConfig().addStream(stream);

                            var scratchStreamObject = self.configurationData.getSiddhiAppConfig().getStream(StreamId);
                            scratchStreamObject.setAttributeList(streamOption.attributeList);
                            scratchStreamObject.setAnnotationList(streamOption.annotationList);

                            self.savedSourceFormForEdit();
                            addEdges(StreamId, uniqueId);

                            var sourceObject = self.configurationData.getSiddhiAppConfig().getSource(uniqueId);

                            self._openFormModal.modal('hide');
                        }
                     }
                }else if(isDeleteflow !== undefined){
                     var errorMap = self.validateSourceMapForEdit();
                     if(errorMap === undefined){
                         self.savedSourceFormForEdit();

                         var streamList = self.configurationData.getSiddhiAppConfig().getStreamList();
                         if (self._openFormModal.find('#exist-stream-checkbox').is(':checked')){
                                 var j = self._openFormModal.find('#exist-stream-list :selected').val();
                                 var ExistStreamId = streamList[j].getId();
                                 addEdges(ExistStreamId, uniqueId);
                         }else{
                             var scratchStreamName = streamObject.name;
                             var streamToolTip;

                             self.designGrid.handleStreamForWizard(100, 200, false,uniqueStreamId,scratchStreamName,streamToolTip,streamObject);
                             self.SaveStreamForm();

                             var stream = new Stream(streamObject);
                             self.configurationData.getSiddhiAppConfig().addStream(stream);

                             var scratchStreamObject = self.configurationData.getSiddhiAppConfig().getStream(uniqueStreamId);
                             scratchStreamObject.setAttributeList(streamObject.attributeList);
                             scratchStreamObject.setAnnotationList(streamObject.annotationList);
                             addEdges(uniqueStreamId, uniqueId);
                         }
                         self._openFormModal.modal('hide');
                     }
                 }
            });

            form.smartWizard({
                selected: 0,
                keyNavigation: false,
                autoAdjustHeight: false,
                theme: 'default',
                transitionEffect: 'slideleft',
                showStepURLhash: false,
                contentCache: false,
                toolbarSettings: {
                    toolbarPosition: 'bottom',
                    toolbarExtraButtons: [btnSubmitForm],
                    toolbarExtraButtonPosition: 'left'
                }
            });

            form.on("leaveStep", function (e, anchorObject, stepNumber, stepDirection) {
                if (stepDirection === 'forward') {
                    if((isEditflow === undefined && isDeleteflow === undefined) || isDeleteflow !== undefined){
                        if (stepNumber === 0) {
                            if (self._openFormModal.find('#exist-stream-checkbox').is(':checked')){
                                return self.validateListOfStream();
                            }else{
                                return self.validateStreamForm();
                            }
                        }else if(stepNumber === 1){
                            return self.validateSourceForm();
                        }
                    }else if(isEditflow !== undefined){
                        if (stepNumber === 0) {
                            if (self._openFormModal.find('#exist-stream-checkbox').is(':checked')){
                                 return self.validateListOfStream();
                            }else{
                                return self.validateStreamForm();
                            }
                        }else if(stepNumber === 1){
                            return self.validateSourceFormForEdit();
                        }
                    }
                }
            });
            // Step is passed successfully
           form.on("showStep", function (e, anchorObject, stepNumber, stepDirection, stepPosition) {
                if(stepPosition === 'first'){
                     form.find(".sw-btn-prev").addClass('disabled');
                     form.find(".sw-btn-prev").addClass('hidden');
                     form.find(".sw-btn-prev").parent().removeClass("sw-btn-group-final");
                }else if(stepPosition === 'final'){
                    form.find(".sw-btn-next").addClass('hidden disabled');
                    form.find(".sw-btn-next").parent().addClass("sw-btn-group-final");
                    form.find("#submit-btn").removeClass('hidden');
                }else{
                    form.find(".sw-btn-next").removeClass('disabled');
                    form.find(".sw-btn-next").removeClass('hidden');
                    form.find(".sw-btn-prev").removeClass('disabled');
                    form.find(".sw-btn-prev").removeClass('hidden');
                    form.find(".sw-btn-prev").parent().removeClass("sw-btn-group-final");
                    form.find("#submit-btn").addClass('hidden');
                }
                if (stepDirection === 'forward') {
                    if (stepNumber === 1) {
                        return self.SourceConfigStep();
                    }else if(stepNumber === 2){
                        return self.SourceMapConfigStep();
                    }
                }
           });
           self._openFormModal.modal('show');
           self.StreamConfigStep();
        };

        SourceFormWizard.prototype.SourceMapConfigStep = function(){
            var self = this;
            var uniqueStreamId = this._uniqueStreamId;
            var StreamId = this._StreamId;
            var uniqueId = this._uniqueId;
            var streamObject = this._streamObject;
            var streamOption = this._streamOption;
            var isEditflow = this._isEditflow;
            var isDeleteflow = this._isDeleteflow;
            var sourceObject = this._sourceOptions;

            var form = self._openFormModal.find('#source-form');
            var connectedElement;
            var streamAttributes;
            var sourceMapContainer = self._openFormModal.find("#design-view-source-map-propertyID");
            var streamName = self._openFormModal.find('#streamName').val();
            var streamList = self.configurationData.getSiddhiAppConfig().getStreamList();
            self._openFormModal.find("#mapNano").nanoScroller();

            if((isEditflow === undefined && isDeleteflow === undefined)){
                if (self._openFormModal.find('#exist-stream-checkbox').is(':checked')){
                    var j = self._openFormModal.find('#exist-stream-list :selected').val();
                    var id = streamList[j].getId();
                    var existStreamObject = self.configurationData.getSiddhiAppConfig().getStream(id);
                    connectedElement = existStreamObject.getName();
                    streamAttributes = existStreamObject.getAttributeList();
                }else{
                    connectedElement = streamName;
                    streamAttributes = streamObject.attributeList;
                }
                _.set(sourceObject, 'connectedElementName',connectedElement);
                var source = new SourceOrSinkAnnotation(sourceObject);

                self.formBuilder.GeneratePropertiesFormForSourceMapWizard(sourceMapContainer, uniqueId , sourceObject , connectedElement, streamAttributes);

            }else if(isEditflow === true){
                sourceObject = self.configurationData.getSiddhiAppConfig().getSource(uniqueId);
                sourceObject.setConnectedElementName(streamName);

                if(self._openFormModal.find('#exist-stream-checkbox').is(':checked')){
                      var streamList = self.configurationData.getSiddhiAppConfig().getStreamList();
                      var j = self._openFormModal.find('#exist-stream-list :selected').val();
                      var id = streamList[j].getId();
                      var existStreamObject = self.configurationData.getSiddhiAppConfig().getStream(id);
                      connectedElement = existStreamObject.getName();
                      streamAttributes = existStreamObject.getAttributeList();
                }
                else{
                    streamAttributes = streamOption.attributeList;
                    connectedElement = streamOption.name;
                }
                _.set(sourceObject, 'connectedElementName',connectedElement);
                var source = new SourceOrSinkAnnotation(sourceObject);

                self.formBuilder.GeneratePropertiesFormForSourceMapWizardForEdit(sourceMapContainer, uniqueId, streamAttributes, connectedElement);

            }else if(isDeleteflow === true ){
                 sourceObject = self.configurationData.getSiddhiAppConfig().getSource(uniqueId);
                 sourceObject.setConnectedElementName(streamName);

                 if(self._openFormModal.find('#exist-stream-checkbox').is(':checked')){
                     var j = self._openFormModal.find('#exist-stream-list :selected').val();
                     var id = streamList[j].getId();
                     streamObject = self.configurationData.getSiddhiAppConfig().getStream(id);
                     streamAttributes = streamObject.getAttributeList();
                     connectedElement = sourceObject.getConnectedElementName();
                 }
                 else{
                     streamAttributes = streamObject.attributeList;
                     connectedElement = streamObject.name;
                 }
                 _.set(sourceObject, 'connectedElementName',connectedElement);
                 var source = new SourceOrSinkAnnotation(sourceObject);

                 self.formBuilder.GeneratePropertiesFormForSourceMapWizardForEdit(sourceMapContainer, uniqueId, streamAttributes, connectedElement);
            }
        };

        SourceFormWizard.prototype.SourceConfigStep = function(){
            var self = this;
            var sourceObject = this._sourceOptions;
            var form = self._openFormModal.find('#source-form');
            var uniqueId = this._uniqueId;
            var isEditflow = this._isEditflow;
            var isDeleteflow = this._isDeleteflow;

            var sourceContainer = self._openFormModal.find("#design-view-source-form-wizard-contentId");
            self._openFormModal.find("#sourceNano").nanoScroller();

            if((isEditflow === undefined && isDeleteflow === undefined) ){
                self.formBuilder.GeneratePropertiesFormForSourceWizard(sourceContainer, uniqueId, sourceObject);
            }else if(isEditflow !== undefined || isDeleteflow !== undefined){
                self.formBuilder.GeneratePropertiesFormForSourceEditWizard(sourceContainer, uniqueId);
            }
        };

        SourceFormWizard.prototype.StreamConfigStep = function(){
            var self = this;
            var form = self._openFormModal.find('#source-form');
            var uniqueStreamId = this._uniqueStreamId;
            var StreamId = this._StreamId;
            var uniqueId = this._uniqueId;
            var isEditflow = this._isEditflow;
            var isDeleteflow = this._isDeleteflow;
            var streamObject = this._streamObject;
            var streamOption = this._streamOption;

            var stream_config = '<div class = "form-group"><div class = "form-wizard-form-scrollable-block-list">' +
                    '<div id = "stream-list" style="display: block"><div class = "clearfix"><div id= "streamNano" class = "nano"><div class = "nano-content">' +
                    '<input type = "checkbox" class = "option-checkbox" id = "exist-stream-checkbox" name = "streamView">' +
                    '&nbsp;&nbsp;<label for = "exist-stream-checkbox"> Using existing stream </label><div id = "exist-stream" ></div> <br>' +
                    '<div id = "form-template"><div class = "form-wrapper">'+
                    '<div id = "design-view-form-wizard-contentId" class = "design-view-form-content">'+
                    '</div></div></div></div></div></div></div></div></div>';

            self._openFormModal.find("#stream-config").append(stream_config);

            var example = '<select name="stream-name" class="form-control" id = "exist-stream-list" style = "width:50%">'+
            '<option value = -1> ----Please select a stream---- </option></select>'+
            '<label class="error" id="streamListErrorMessage"> </label>';

            var streamContainer = self._openFormModal.find("#design-view-form-wizard-contentId");
            self._openFormModal.find("#streamNano").nanoScroller();

            if(isEditflow === true){
                self._openFormModal.find('#exist-stream-checkbox').attr('checked', 'checked');
                self._openFormModal.find('#exist-stream').append(example);
                self.ListOfStream();
                if(uniqueStreamId !== undefined){
                    var existStreamObject = self.configurationData.getSiddhiAppConfig().getStream(uniqueStreamId);
                    var streamName = existStreamObject.getName();
                    var s = self._openFormModal.find('#exist-stream-list');
                    self.setSelectedIndex(s,streamName);

                    self.formBuilder.GeneratePropertiesFormForStreamEditWizard(streamContainer, uniqueStreamId);
                }else{
                    self.formBuilder.GeneratePropertiesFormForStreamWizard(streamContainer, StreamId, streamOption);
                }


                self._openFormModal.find('#stream-list').on('change', '#exist-stream-checkbox', function () {
                   if ($(this).is(':checked')) {
                       self._openFormModal.find('#exist-stream').append(example);
                       self.ListOfStream();
                       var existStreamObject = self.configurationData.getSiddhiAppConfig().getStream(uniqueStreamId);
                       var streamName = existStreamObject.getName();
                       var s = self._openFormModal.find('#exist-stream-list');
                       self.setSelectedIndex(s,streamName);
                       self.formBuilder.GeneratePropertiesFormForStreamEditWizard(streamContainer, uniqueStreamId);
                   } else {
                       self._openFormModal.find('#exist-stream').empty();
                       self.formBuilder.GeneratePropertiesFormForStreamWizard(streamContainer, StreamId, streamOption);
                   }
                });
            }

            if(isEditflow === undefined || isDeleteflow !== undefined){
                self._openFormModal.find('#stream-list').on('change', '#exist-stream-checkbox', function () {
                   if ($(this).is(':checked')) {
                       self._openFormModal.find('#exist-stream').append(example);
                       self.ListOfStream();
                   } else {
                       self._openFormModal.find('#exist-stream').empty();
                       self.formBuilder.GeneratePropertiesFormForStreamWizard(streamContainer, uniqueStreamId, streamObject);
                   }
                });
                self.formBuilder.GeneratePropertiesFormForStreamWizard(streamContainer, uniqueStreamId, streamObject);
            }

            self._openFormModal.find('#stream-list').on('change', '#exist-stream-list', function () {
               var j = self._openFormModal.find('#exist-stream-list :selected').val();
               var streamList = self.configurationData.getSiddhiAppConfig().getStreamList();
               var id = streamList[j].getId();
               self.formBuilder.GeneratePropertiesFormForStreamEditWizard(streamContainer, id);
            });

        };

        SourceFormWizard.prototype.ListOfStream = function(){
            var self = this;
            var form = self._openFormModal.find('#exist-stream-list');
            var streamList = self.configurationData.getSiddhiAppConfig().getStreamList();

            var i;
            var streamNameList;
            var optionValues;
            for(i = 0; i < streamList.length; i++){
                if(streamList[i].getName() !== undefined){
                    streamNameList = streamList[i].getName();
                    optionValues = '<option value = ' + i + '>' + streamNameList + '</option>';
                    form.append(optionValues);
                }
            }
        };

        SourceFormWizard.prototype.validateListOfStream = function(){
            var self = this;
            self.formUtils.removeErrorClass();
            var streamValue = $('#exist-stream-list :selected').val();
            if(streamValue === "-1"){
                self.formUtils.addErrorClass("#exist-stream-list");
                $('#streamListErrorMessage').text("Please select a stream name.")
                return false;
            }
        };

        SourceFormWizard.prototype.validateSourceForm = function () {
             var self = this;
             var sourceObject = this._sourceOptions;
             var uniqueId = this._uniqueId;
             var id = uniqueId;
             var sourceContainer = self._openFormModal.find("#design-view-source-form-wizard-contentId");

            var currentSourceOptions = [];
            var attributes = [];

            var predefinedSources = _.orderBy(this.configurationData.rawExtensions["source"], ['name'], ['asc']);

            self.formUtils.removeErrorClass();
            var isErrorOccurred = false;

            var selectedSourceType = self._openFormModal.find('#define-source #source-type').val();;

            if (selectedSourceType === null) {
                DesignViewUtils.prototype.errorAlert("Select a source type to submit.");
                return false;
            }else {
                currentSourceOptions = self.formUtils.getSelectedTypeParameters(selectedSourceType, predefinedSources);
                 if (self.formUtils.validateOptionsForWizard(currentSourceOptions, Constants.SOURCE, sourceContainer)) {
                     isErrorOccurred = true;
                     return false;
                 }
                 if (self.formUtils.validateCustomizedOptionsForWizard(Constants.SOURCE, sourceContainer)) {
                     isErrorOccurred = true;
                     return false;
                 }
            }
            if (!isErrorOccurred) {
                _.set(sourceObject, 'type', selectedSourceType);
                var source = new SourceOrSinkAnnotation(sourceObject);

                var annotationOptions = [];
                self.formUtils.buildOptionsForWizard(annotationOptions, Constants.SOURCE, sourceContainer);
                self.formUtils.buildCustomizedOptionForWizard(annotationOptions, Constants.SOURCE, sourceContainer);
                if (annotationOptions.length == 0) {
                    _.set(sourceObject, 'options', undefined);

                } else {
                    _.set(sourceObject, 'options', annotationOptions);

                }
                var source = new SourceOrSinkAnnotation(sourceObject);
            }
        };

        SourceFormWizard.prototype.savedSourceForm = function () {
             var self = this;
             var sourceObject = this._sourceOptions;
             var form = self._openFormModal.find('#source-form');
             var uniqueId = this._uniqueId;
             var id = uniqueId;
             var sourceContainer = self._openFormModal.find("#design-view-source-form-wizard-contentId");

            self.formUtils.removeErrorClass();
            var isErrorOccurred = false;
            var predefinedSources = _.orderBy(this.configurationData.rawExtensions["source"], ['name'], ['asc']);

            var selectedSourceType = sourceObject.type;
            currentSourceOptions = self.formUtils.getSelectedTypeParameters(selectedSourceType, predefinedSources);

            var textNode = $('#' + id).find('.sourceNameNode');
            textNode.html(selectedSourceType);

            $('#' + id).removeClass('incomplete-element');

            var sourceToolTip = self.formUtils.getTooltipForWizard(sourceObject, Constants.SOURCE);
            $('#' + id).prop('title', sourceToolTip);

            self.formBuilder.designGrid.dropElements.generateSpecificSourceConnectionElements(selectedSourceType,self.jsPlumbInstance, id, $('#' + id));

            // set the isDesignViewContentChanged to true
            self.configurationData.setIsDesignViewContentChanged(true);

        };

        SourceFormWizard.prototype.validateSourceFormForEdit = function () {
            var self = this;
            var uniqueId = this._uniqueId;
            var sourceObject = self.configurationData.getSiddhiAppConfig().getSource(uniqueId);

            var sourceContainer = self._openFormModal.find("#design-view-source-form-wizard-contentId");

            self.formUtils.removeErrorClass();
            var isErrorOccurred = false;
            var predefinedSources = _.orderBy(this.configurationData.rawExtensions["source"], ['name'], ['asc']);

            var selectedSourceType = self._openFormModal.find('#define-source #source-type').val();
            currentSourceOptions = self.formUtils.getSelectedTypeParameters(selectedSourceType, predefinedSources);

            if (self.formUtils.validateOptionsForWizard(currentSourceOptions, Constants.SOURCE, sourceContainer)) {
                 isErrorOccurred = true;
                 return false;
            }
            if (self.formUtils.validateCustomizedOptionsForWizard(Constants.SOURCE, sourceContainer)) {
                 isErrorOccurred = true;
                 return false;
            }
        };

        SourceFormWizard.prototype.savedSourceFormForEdit = function () {
             var self = this;
             var uniqueId = this._uniqueId;
             var id = uniqueId;
             var sourceObject = self.configurationData.getSiddhiAppConfig().getSource(id);
             var sourceContainer = self._openFormModal.find("#design-view-source-form-wizard-contentId");

            self.formUtils.removeErrorClass();

            var selectedSourceType = self._openFormModal.find('#define-source #source-type').val();

            sourceObject.setType(selectedSourceType);
            var textNode = $('#' + id).find('.sourceNameNode');
            textNode.html(selectedSourceType);

            var annotationOptions = [];
            var sourceOptionsValue = sourceObject.getOptions();
            self.formUtils.buildOptionsForWizard(annotationOptions, Constants.SOURCE,sourceContainer);
            self.formUtils.buildCustomizedOptionForWizard(annotationOptions, Constants.SOURCE,sourceContainer);
            if (annotationOptions.length == 0) {
                sourceObject.setOptions(undefined);

            } else {
                sourceObject.setOptions(annotationOptions);
            }

            $('#' + id).removeClass('error-element');

            var sourceToolTip = self.formUtils.getTooltip(sourceObject, Constants.SOURCE);
            $('#' + id).prop('title', sourceToolTip);

            self.formBuilder.designGrid.dropElements.generateSpecificSourceConnectionElements(selectedSourceType,self.jsPlumbInstance, id, $('#' + id));

            // set the isDesignViewContentChanged to true
            self.configurationData.setIsDesignViewContentChanged(true);

        };

        SourceFormWizard.prototype.validateStreamForm = function(){
            var self = this;
            var form = self._openFormModal.find('#source-form');
            var uniqueStreamId = this._uniqueStreamId;
            var StreamId = this._StreamId;
            var isEditflow = this._isEditflow;
            var streamObject = this._streamObject;
            var streamOption = this._streamOption;

            var id; var streamObj;
            if(isEditflow === true){
                id = StreamId;
                streamObj = streamOption;
            }else{
                id = uniqueStreamId;
                streamObj = streamObject;
            }

            var streamContainer = self._openFormModal.find("#design-view-form-wizard-contentId");
            self.formUtils.removeErrorClass();

            var previouslySavedStreamName = streamObj.name;

            var configName = self._openFormModal.find('#streamName').val().trim();
            var streamName;
            var firstCharacterInStreamName;
            var isStreamNameUsed;
            var isErrorOccurred = false;

            var predefinedAnnotationList = _.cloneDeep(self.configurationData.application.config.stream_predefined_annotations);
            /*
            * check whether the stream is inside a partition and if yes check whether it begins with '#'.
            *  If not add '#' to the beginning of the stream name.
            * */
            var isStreamSavedInsideAPartition
                = self.configurationData.getSiddhiAppConfig().getStreamSavedInsideAPartition(id);
            if (!isStreamSavedInsideAPartition) {
                firstCharacterInStreamName = (configName).charAt(0);
                if (firstCharacterInStreamName === '#') {
                    self.formUtils.addErrorClass("#streamName");
                    self._openFormModal.find('#streamNameErrorMessage').text("'#' is used to define inner streams only.")
                    //$('#streamNameErrorMessage').text("'#' is used to define inner streams only.")
                    isErrorOccurred = true;
                    return false;
                } else {
                    streamName = configName;
                }
                isStreamNameUsed
                    = self.formUtils.isDefinitionElementNameUsed(streamName, id);
                if (isStreamNameUsed) {
                    self.formUtils.addErrorClass("#streamName");
                    self._openFormModal.find('#streamNameErrorMessage').text("Stream name is already defined.")
                    //$('#streamNameErrorMessage').text("Stream name is already defined.")
                    isErrorOccurred = true;
                    return false;
                }
            } else {
                firstCharacterInStreamName = (configName).charAt(0);
                if (firstCharacterInStreamName !== '#') {
                    streamName = '#' + configName;
                } else {
                    streamName = configName;
                }
                var partitionWhereStreamIsSaved
                    = self.configurationData.getSiddhiAppConfig().getPartitionWhereStreamIsSaved(id);
                var partitionId = partitionWhereStreamIsSaved.getId();
                isStreamNameUsed
                    = self.formUtils.isStreamDefinitionNameUsedInPartition(partitionId, streamName, id);
                if (isStreamNameUsed) {
                    self.formUtils.addErrorClass("#streamName");
                    self._openFormModal.find('#streamNameErrorMessage').text("Stream name is already defined in the partition.")
                    //$('#streamNameErrorMessage').text("Stream name is already defined in the partition.")
                    isErrorOccurred = true;
                    return false;
                }
            }
            //check if stream name is empty
            if (streamName == "") {
                self.formUtils.addErrorClass("#streamName");
                self._openFormModal.find('#streamNameErrorMessage').text("Stream name is required.")
                //$('#streamNameErrorMessage').text("Stream name is required.")
                isErrorOccurred = true;
                return false;
            }
            var previouslySavedName = streamObj.name;
            if (previouslySavedName === undefined) {
                previouslySavedName = "";
            }
            if (previouslySavedName !== streamName) {
                if (self.formUtils.validateAttributeOrElementNameForWizard("#streamName", Constants.STREAM, streamName, streamContainer)) {
                    isErrorOccurred = true;
                    return false;
                }
            }

            var attributeNameList = [];
            if (self.formUtils.validateAttributesForWizard(attributeNameList,streamContainer)) {
                isErrorOccurred = true;
                return false;
            }
            if (attributeNameList.length == 0) {
                self.formUtils.addErrorClass($('.attribute:eq(0)').find('.attr-name'));
                self._openFormModal.find('.attribute:eq(0)').find('.error-message').text("Minimum one attribute is required.")
                isErrorOccurred = true;
                return false;
            }

            var annotationNodes = [];
            if (self.formUtils.validateAnnotationsForWizard(predefinedAnnotationList, annotationNodes , streamContainer)) {
                isErrorOccurred = true;
                return false;
            }
            // If this is an inner stream perform validation
            var streamSavedInsideAPartition
                = self.configurationData.getSiddhiAppConfig().getStreamSavedInsideAPartition(id);
            // if streamSavedInsideAPartition is undefined then the stream is not inside a partition
            if (streamSavedInsideAPartition !== undefined) {
                var isValid = JSONValidator.prototype.validateInnerStream(streamObject, self.jsPlumbInstance,
                    false);
                if (!isValid) {
                    isErrorOccurred = true;
                    return false;
                }
            }

            if (!isErrorOccurred) {
                // update selected stream model
                _.set(streamObj, 'name', streamName);

                //add the attributes to the attribute list
                var attributeList = [];
                self._openFormModal.find('.attribute .attr-content').each(function () {
                    var nameValue = $(this).find('.attr-name').val().trim();
                    var typeValue = $(this).find('.attr-type').val();

                    if (nameValue != "") {
                        attributeObject = new Attribute({name: nameValue, type: typeValue});
                        attributeList.push(attributeObject);
                    }
                });
                _.set(streamObj, 'attributeList', attributeList);

                var annotationStringList = [];
                var annotationObjectList = [];

                self.formUtils.buildAnnotationForWizard(annotationNodes, annotationStringList, annotationObjectList, streamContainer);
                var annotationList = [];
                var annotationListObjects = []
                _.forEach(annotationStringList, function (annotation) {
                       annotationList.push(annotation);
                });
                _.forEach(annotationObjectList, function (annotation) {
                    annotationListObjects.push(annotation);
                });
                _.set(streamObj, 'annotationList', annotationList);
                _.set(streamObj, 'annotationListObjects', annotationListObjects);
                var stream = new Stream(streamObj);
            }
        };

        SourceFormWizard.prototype.SaveStreamForm = function(){
            var self = this;
            var form = self._openFormModal.find('#source-form');
            var uniqueStreamId = this._uniqueStreamId;
            var StreamId = this._StreamId;
            var isEditflow = this._isEditflow;
            var streamObject = this._streamObject;
            var streamOption = this._streamOption;
            var id,streamObj;
            if(isEditflow === true){
                id = StreamId;
                streamObj = streamOption;
            }else{
                id = uniqueStreamId;
                streamObj = streamObject;
            }

            var streamName = self._openFormModal.find('#streamName').val().trim();

            var textNode = $('#' + id).find('.streamNameNode');
            textNode.html(streamName);

           $('#' + id).removeClass('incomplete-element');
           //Send stream element to the backend and generate tooltip
           var streamToolTip = self.formUtils.getTooltipForWizard(streamObj, Constants.STREAM);
           $('#' + id).prop('title', streamToolTip);

           // set the isDesignViewContentChanged to true
           self.configurationData.setIsDesignViewContentChanged(true)

        };

        SourceFormWizard.prototype.SaveStreamFormForEdit = function(){
            var self = this;
            var uniqueStreamId = this._uniqueStreamId;
            var streamList = self.configurationData.getSiddhiAppConfig().getStreamList();
            var id;
            if(self._openFormModal.find('#exist-stream-list :selected')) {
                var j = self._openFormModal.find('#exist-stream-list :selected').val();
                 id = streamList[j].getId();
            }

            var streamObject = self.configurationData.getSiddhiAppConfig().getStream(id);

            var previousStreamObject = _.cloneDeep(streamObject);

            var streamContainer = self._openFormModal.find("#design-view-form-wizard-contentId");

            self.formUtils.removeErrorClass();
            var previouslySavedStreamName = streamObject.getName();
            var annotateVaue = streamObject.getAnnotationList();

            var configName = self._openFormModal.find('#streamName').val().trim();
            var streamName;
            var firstCharacterInStreamName;
            var isStreamNameUsed;
            var isErrorOccurred = false;

            var predefinedAnnotationList = _.cloneDeep(self.configurationData.application.config.stream_predefined_annotations);
            /*
            * check whether the stream is inside a partition and if yes check whether it begins with '#'.
            *  If not add '#' to the beginning of the stream name.
            * */
            var isStreamSavedInsideAPartition
                = self.configurationData.getSiddhiAppConfig().getStreamSavedInsideAPartition(id);
            if (!isStreamSavedInsideAPartition) {
                firstCharacterInStreamName = (configName).charAt(0);
                if (firstCharacterInStreamName === '#') {
                    self.formUtils.addErrorClass("#streamName");
                    self._openFormModal.find('#streamNameErrorMessage').text("'#' is used to define inner streams only.")
                    isErrorOccurred = true;
                    return false;
                } else {
                    streamName = configName;
                }
                isStreamNameUsed
                    = self.formUtils.isDefinitionElementNameUsed(streamName, id);
                if (isStreamNameUsed) {
                    self.formUtils.addErrorClass("#streamName");
                    self._openFormModal.find('#streamNameErrorMessage').text("Stream name is already defined.")
                    isErrorOccurred = true;
                    return false;
                }
            } else {
                firstCharacterInStreamName = (configName).charAt(0);
                if (firstCharacterInStreamName !== '#') {
                    streamName = '#' + configName;
                } else {
                    streamName = configName;
                }
                var partitionWhereStreamIsSaved
                    = self.configurationData.getSiddhiAppConfig().getPartitionWhereStreamIsSaved(id);
                var partitionId = partitionWhereStreamIsSaved.getId();
                isStreamNameUsed
                    = self.formUtils.isStreamDefinitionNameUsedInPartition(partitionId, streamName, id);
                if (isStreamNameUsed) {
                    self.formUtils.addErrorClass("#streamName");
                    self._openFormModal.find('#streamNameErrorMessage').text("Stream name is already defined in the partition.")
                    isErrorOccurred = true;
                    return false;
                }
            }

            //check if stream name is empty
            if (streamName == "") {
                self.formUtils.addErrorClass("#streamName");
                self._openFormModal.find('#streamNameErrorMessage').text("Stream name is required.")
                isErrorOccurred = true;
                return false;
            }
            var previouslySavedName = streamObject.name;
            if (previouslySavedName === undefined) {
                previouslySavedName = "";
            }
            if (previouslySavedName !== streamName) {
                if (self.formUtils.validateAttributeOrElementNameForWizard("#streamName", Constants.STREAM, streamName, streamContainer)) {
                    isErrorOccurred = true;
                    return false;
                }
            }

            var attributeNameList = [];
            if (self.formUtils.validateAttributesForWizard(attributeNameList, streamContainer)) {
                isErrorOccurred = true;
                return false;
            }

            if (attributeNameList.length == 0) {
                self.formUtils.addErrorClass($('.attribute:eq(0)').find('.attr-name'));
                self._openFormModal.find('.attribute:eq(0)').find('.error-message').text("Minimum one attribute is required.")
                isErrorOccurred = true;
                return false;
            }

            var annotationNodes = [];
            if (self.formUtils.validateAnnotationsForWizard(predefinedAnnotationList, annotationNodes,streamContainer)) {
                isErrorOccurred = true;
                return;
            }

            // If this is an inner stream perform validation
            var streamSavedInsideAPartition
                = self.configurationData.getSiddhiAppConfig().getStreamSavedInsideAPartition(id);
            // if streamSavedInsideAPartition is undefined then the stream is not inside a partition
            if (streamSavedInsideAPartition !== undefined) {
                var isValid = JSONValidator.prototype.validateInnerStream(streamObject, self.jsPlumbInstance,
                    false);
                if (!isValid) {
                    isErrorOccurred = true;
                    return false;
                }
            }

            if (!isErrorOccurred) {
                // update selected stream model
                streamObject.setName(streamName);
                var textNode = $('#' + id).find('.streamNameNode');
                textNode.html(streamName);

                streamObject.clearAttributeList();

                //add the attributes to the attribute list
                self._openFormModal.find('.attribute .attr-content').each(function () {
                    var nameValue = $(this).find('.attr-name').val().trim();
                    var typeValue = $(this).find('.attr-type').val();
                    if (nameValue != "") {
                        var attributeObject = new Attribute({name: nameValue, type: typeValue});
                        streamObject.addAttribute(attributeObject);
                    }
                });

                var annotationStringList = [];
                var annotationObjectList = [];

                //clear the saved annotations
                //if(annotateVaue !== undefined){
                    streamObject.clearAnnotationList();
                    streamObject.clearAnnotationListObjects();
                //}

                self.formUtils.buildAnnotationForWizard(annotationNodes, annotationStringList, annotationObjectList,streamContainer);
                _.forEach(annotationStringList, function (annotation) {
                    streamObject.addAnnotation(annotation);
                });
                _.forEach(annotationObjectList, function (annotation) {
                    streamObject.addAnnotationObject(annotation);
                });

                self.formBuilder.designGrid.dropElements.toggleFaultStreamConnector(streamObject,self.jsPlumbInstance,previouslySavedStreamName);

                $('#' + id).removeClass('incomplete-element');
                //Send stream element to the backend and generate tooltip
                var streamToolTip = self.formUtils.getTooltip(streamObject, Constants.STREAM);
                $('#' + id).prop('title', streamToolTip);

                // set the isDesignViewContentChanged to true
                self.configurationData.setIsDesignViewContentChanged(true)
            }
        };

        SourceFormWizard.prototype.validateSourceMap = function(){
            var self = this;
            var sourceObject = this._sourceOptions;
            var mapContainer = self._openFormModal.find("#design-view-source-map-propertyID");

            self.formUtils.removeErrorClass();

            var isErrorOccurred = false;

            var selectedMapType = mapContainer.find('#define-map #map-type').val();

            var predefinedSourceMaps = _.orderBy(this.configurationData.rawExtensions["sourceMaps"], ['name'], ['asc']);

            var currentMapperOptions = [];

            currentMapperOptions = self.formUtils.getSelectedTypeParameters(selectedMapType, predefinedSourceMaps);

            if (self.formUtils.validateOptionsForWizard(currentMapperOptions, Constants.MAPPER, mapContainer)) {
                isErrorOccurred = true;
                return false;
            }
            if (self.formUtils.validateCustomizedOptionsForWizard(Constants.MAPPER, mapContainer)) {
                isErrorOccurred = true;
                return false;
            }

            if (mapContainer.find('#define-attribute #attributeMap-checkBox').is(":checked")) {
                //if attribute section is checked
                var mapperAttributeValuesArray = {};
                var boolean;
                mapContainer.find('#mapper-attributes .attribute').each(function () {
                    //validate mapper  attributes if value is not filled
                    var key = $(this).find('.attr-key').val().trim();
                    var value = $(this).find('.attr-value').val().trim();
                    if (value == "") {
                        $(this).find('.error-message').text('Attribute Value is required.');
                        self.formUtils.addErrorClass($(this).find('.attr-value'));
                        isErrorOccurred = true;
                        boolean = false;
                    } else {
                        mapperAttributeValuesArray[key] = value;
                        boolean = true;
                    }
                });
                return boolean;
            }

            if (!isErrorOccurred) {
                var mapper = {};
                var mapperAnnotationOptions = [];
                self.formUtils.buildOptionsForWizard(mapperAnnotationOptions, Constants.MAPPER, mapContainer);
                self.formUtils.buildCustomizedOptionForWizard(mapperAnnotationOptions, Constants.MAPPER, mapContainer);
                _.set(mapper, 'type', selectedMapType);
                if (mapperAnnotationOptions.length == 0) {
                    _.set(mapper, 'options', undefined);
                } else {
                    _.set(mapper, 'options', mapperAnnotationOptions);
                }

                if (mapContainer.find('#define-attribute #attributeMap-checkBox').is(":checked")) {
                    payloadOrAttributeOptions = {};
                    _.set(payloadOrAttributeOptions, 'annotationType', 'ATTRIBUTES');
                    _.set(payloadOrAttributeOptions, 'type', Constants.MAP);
                    _.set(payloadOrAttributeOptions, 'value', mapperAttributeValuesArray);
                    var payloadOrAttributeObject = new PayloadOrAttribute(payloadOrAttributeOptions);
                    _.set(mapper, 'payloadOrAttribute', payloadOrAttributeObject);
                } else {
                    _.set(mapper, 'payloadOrAttribute', undefined);
                }
                var mapperObject = new MapAnnotation(mapper);
                _.set(sourceObject,'map',mapperObject);
                var source = new SourceOrSinkAnnotation(sourceObject);
            }
        };

        SourceFormWizard.prototype.validateSourceMapForEdit = function(){
            var self = this;
            var uniqueId = this._uniqueId;
            var sourceObject = self.configurationData.getSiddhiAppConfig().getSource(uniqueId);

            var mapContainer = self._openFormModal.find("#design-view-source-map-propertyID");
            self.formUtils.removeErrorClass();

            var isErrorOccurred = false;

            var predefinedSourceMaps = _.orderBy(this.configurationData.rawExtensions["sourceMaps"], ['name'], ['asc']);
            var currentMapperOptions = [];

            var selectedMapType = mapContainer.find('#define-map #map-type').val();

            currentMapperOptions = self.formUtils.getSelectedTypeParameters(selectedMapType, predefinedSourceMaps);

            if (self.formUtils.validateOptionsForWizard(currentMapperOptions, Constants.MAPPER, mapContainer)) {
                isErrorOccurred = true;
                return false;
            }
            if (self.formUtils.validateCustomizedOptionsForWizard(Constants.MAPPER, mapContainer)) {
                isErrorOccurred = true;
                return false;
            }

            if (mapContainer.find('#define-attribute #attributeMap-checkBox').is(":checked")) {
                //if attribute section is checked
                var mapperAttributeValuesArray = {};
                var boolean;
                mapContainer.find('#mapper-attributes .attribute').each(function () {
                    //validate mapper  attributes if value is not filled
                    var key = $(this).find('.attr-key').val().trim();
                    var value = $(this).find('.attr-value').val().trim();
                    if (value == "") {
                        $(this).find('.error-message').text('Attribute Value is required.');
                        self.formUtils.addErrorClass($(this).find('.attr-value'));
                        isErrorOccurred = true;
                        boolean = false;
                    } else {
                        mapperAttributeValuesArray[key] = value;
                        boolean = true;
                    }
                });
                return boolean;
            }

            if (!isErrorOccurred) {
                var mapper = {};
                var mapperAnnotationOptions = [];
                self.formUtils.buildOptionsForWizard(mapperAnnotationOptions, Constants.MAPPER, mapContainer);
                self.formUtils.buildCustomizedOptionForWizard(mapperAnnotationOptions, Constants.MAPPER, mapContainer);
                _.set(mapper, 'type', selectedMapType);
                if (mapperAnnotationOptions.length == 0) {
                    _.set(mapper, 'options', undefined);
                } else {
                    _.set(mapper, 'options', mapperAnnotationOptions);
                }

                if (mapContainer.find('#define-attribute #attributeMap-checkBox').is(":checked")) {
                    payloadOrAttributeOptions = {};
                    _.set(payloadOrAttributeOptions, 'annotationType', 'ATTRIBUTES');
                    _.set(payloadOrAttributeOptions, 'type', Constants.MAP);
                    _.set(payloadOrAttributeOptions, 'value', mapperAttributeValuesArray);
                    var payloadOrAttributeObject = new PayloadOrAttribute(payloadOrAttributeOptions);
                    _.set(mapper, 'payloadOrAttribute', payloadOrAttributeObject);
                } else {
                    _.set(mapper, 'payloadOrAttribute', undefined);
                }
                var mapperObject = new MapAnnotation(mapper);
                _.set(sourceObject,'map',mapperObject);
                sourceObject.setMap(mapperObject);
            }
        };

        SourceFormWizard.prototype.setSelectedIndex = function(s,v) {
            var self = this;
             s.find('option').filter(function () {
                 if ($(this).text() == v){
                    return this;
                 }
             }).attr('selected', 'selected');
        };

        function addEdges(uniqueStreamId, uniqueId) {
            var sourceId = uniqueId;
            var targetId = uniqueStreamId;
            var edgeId = sourceId + "_" + targetId;
            var sourceType = self.configurationData.getSiddhiAppConfig().getDefinitionElementById(sourceId, true, true).type;
            var targetType = self.configurationData.getSiddhiAppConfig().getDefinitionElementById(targetId, true, true).type;
            var edgeOptions = {};
                _.set(edgeOptions, 'id', edgeId);
                _.set(edgeOptions, 'childId', targetId);
                _.set(edgeOptions, 'childType', targetType);
                _.set(edgeOptions, 'parentId', sourceId);
                _.set(edgeOptions, 'parentType', sourceType);
                var edge = new Edge(edgeOptions);

            if (edge.parentType === 'STREAM') {
                // check if this is originating from the fault stream and get the correct parent id and set
                var stream = getStreambyId(self.configurationData.siddhiAppConfig.getStreamList(), newParentId);
                if (stream.isFaultStream()) {
                    var correspondingStream = getStreamByName(self.configurationData.siddhiAppConfig.getStreamList(),
                        stream.getName().substr(1));
                    edgeOptions.parentId = correspondingStream.getId();
                    edgeOptions.fromFaultStream = true;
                }
            }
            self.configurationData.addEdge(new Edge(edgeOptions));

            self.configurationData.setIsStillDrawingGraph(true);
            var targetId;
            var sourceId;
            var paintStyle = {
                strokeWidth: 2,
                stroke: '#424242',
                outlineStroke: "transparent",
                outlineWidth: "3"
            };

            if (edge.getChildType() === 'PARTITION') {
                targetId = edge.getChildId();
                sourceId = edge.getParentId() + '-out';
            } else if (edge.getParentType() === 'PARTITION') {
                targetId = edge.getChildId() + '-in';
                sourceId = edge.getParentId();
            } else if (edge.getParentType() === 'SINK' && edge.getChildType() === 'SOURCE') {
                targetId = edge.getChildId() + '-in';
                sourceId = edge.getParentId() + '-out';
                paintStyle = {
                    strokeWidth: 2, stroke: "#424242", dashstyle: "2 3", outlineStroke: "transparent",
                    outlineWidth: "3"
                }
            } else {
                // check if the edge is originating from a fault stream. if so get the corresponding event stream
                // and draw the edge from the -err-out connector.
                if (edge.isFromFaultStream()) {
                    sourceId = edge.getParentId() + '-err-out';
                    paintStyle = {
                        strokeWidth: 2, stroke: "#FF0000", dashstyle: "2 3", outlineStroke: "transparent",
                        outlineWidth: "3"
                    };
                } else {
                    sourceId = edge.getParentId() + '-out';
                }
                targetId = edge.getChildId() + '-in';
            }

            self.jsPlumbInstance.connect({
                source: sourceId,
                target: targetId,
                paintStyle: paintStyle
            });
        };

        function removeEdges(targetId ,sourceId){
            var targetElement = $('#' + targetId);

            var outConnection = self.jsPlumbInstance.getConnections({source: sourceId + '-out'})[0];

            if (outConnection.connector !== null) {
                self.jsPlumbInstance.deleteConnection(outConnection);
            }
        };

    return SourceFormWizard;
});
