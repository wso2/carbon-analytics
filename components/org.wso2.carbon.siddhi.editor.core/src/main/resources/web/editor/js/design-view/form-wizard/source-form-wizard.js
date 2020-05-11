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
define(['require', 'lodash', 'jquery', 'log', 'appData','configurationData','edge', 'designViewUtils','dropElements' ,
    'smart_wizard','stream', 'formBuilder','formUtils', 'stream','sourceOrSinkAnnotation', 'mapAnnotation',
    'attribute','jsonValidator','constants', 'payloadOrAttribute'],
    function (require, _ , $, log, AppData, ConfigurationData, Edge, DesignViewUtils,DropElements,
        smartWizard, Stream , FormBuilder, FormUtils, Stream , SourceOrSinkAnnotation, MapAnnotation,
        Attribute, JSONValidator, Constants, PayloadOrAttribute) {

        var self;

        var SourceFormWizard = function (option){
            self = this;
            this.option = option;
            this._openFormModal = $('#FormWizard').clone();

            this.configurationData = option.configurationData;
            this.application = option.application;
            this.jsPlumbInstance = option.jsPlumbInstance;
            this.designGrid = option.designGrid;
            this.formUtils = new FormUtils(this.application, this.configurationData, this.jsPlumbInstance);
            this._uniqueStreamId = option.uniqueStreamId;
            this._uniqueSourceId = option.uniqueSourceId;
            this._isEditflow = option.isEditflow;
            this._isStreamDeleteflow = option.isStreamDeleteflow;
            this._sourceOptions = {};
            this._streamObject = {};
            this._formOptions = {
                'configurationData':self.configurationData,
                'application':self.application,
                'formUtils':self.formUtils,
                'dropElementInstance':this,
                'jsPlumbInstance':self.jsPlumbInstance,
                'designGrid':self.designGrid
            };
            this.formBuilder = new FormBuilder(this._formOptions);
            this._btnSubmitForm = $('<form><button type="button" class="btn btn-primary hidden" id="submit-btn">Submit'+
                                        '</button></form>' );
        };

        SourceFormWizard.prototype.constructor = SourceFormWizard;

        SourceFormWizard.prototype.render = function () {
            var self = this;
            var uniqueStreamId = this._uniqueStreamId;
            var uniqueSourceId = this._uniqueSourceId;
            var streamObject = this._streamObject;
            var sourceObject = this._sourceOptions;
            var isEditflow = this._isEditflow;
            var isStreamDeleteflow = this._isStreamDeleteflow;
            var form = self._openFormModal.find('#form-modal');
            // Toolbar extra buttons
            var btnSubmitForm = this._btnSubmitForm;

            var FormWizardforSourceAndStream = $('<ul id="form-step" class="form_wizard_item">'+
                '<li><a href="#step1" class="link-disabled"><br><small> Create/Configure the Stream</small></a></li>'+
                '<li><a href="#step2" class="link-disabled"><br><small> Configure the Source</small></a></li>'+
                '<li><a href="#step3" class="link-disabled"><br><small> Configure the Map Property</small></a></li>'+
                '</ul>'+
                '<div id="form-container" class="sw-container tab-content">'+
                '<div id="step1">'+
                '<div id = "stream-config"></div></div>'+
                '<div id="step2">'+
                '<div id = "source-config">'+
                '<div class="form-group">'+
                '<div class="form-wizard-form-scrollable-block-list">'+
                '<div id="source-view" style="display: block">'+
                '<div class = "clearfix">'+
                '<div class="form-wrapper">'+
                '<div id = "sourceNano" class = "nano">'+
                '<div class = "nano-content">'+
                '<div id="design-view-source-form-wizard-contentId" class="design-view-form-content">'+
                '</div></div></div></div></div></div></div></div></div></div>'+
                '<div id = "step3">'+
                '<div id = "map-config">'+
                '<div class="form-group">'+
                '<div class="form-wizard-form-scrollable-block-list">'+
                '<div style="display: block">'+
                '<div class = "clearfix">'+
                '<div class="form-wrapper">'+
                '<div id = "mapNano" class = "nano">'+
                '<div class = "nano-content">'+
                '<div id="design-view-source-map-propertyID" class="design-view-form-content">'+
                '</div></div></div></div></div></div></div></div></div></div></div>');

            form.html(FormWizardforSourceAndStream);

            btnSubmitForm.find('#submit-btn').on('click', function () {
                if(isEditflow === undefined && isStreamDeleteflow === undefined){
                    var isError = self.validateMappingProperty();
                    if(isError !== true) {
                        var source = new SourceOrSinkAnnotation(sourceObject);
                        self.designGrid.handleSourceAnnotation(100, 50, false, sourceObject.type,uniqueSourceId,source);
                        self.configurationData.getSiddhiAppConfig().addSource(source);
                        self.setTheValueToSource();

                        var streamList = self.configurationData.getSiddhiAppConfig().getStreamList();
                        if (self._openFormModal.find('#exist-stream-checkbox').is(':checked')){
                            if(self._openFormModal.find('#exist-stream-list :selected')) {
                                var j = self._openFormModal.find('#exist-stream-list :selected').val();
                                var ExistStreamId = streamList[j].getId();
                                addEdges(ExistStreamId, uniqueSourceId);
                            }
                        } else {
                            var scratchStreamName = streamObject.name;
                            var stream = new Stream(streamObject);
                            self.configurationData.getSiddhiAppConfig().addStream(stream);
                            self.designGrid.handleStreamForWizard
                                   (100, 200, false,uniqueStreamId,scratchStreamName,stream);
                            self.setTheValueToStream();
                            var scratchStreamObject = self.configurationData.getSiddhiAppConfig().
                                                                                           getStream(uniqueStreamId);
                            scratchStreamObject.setAttributeList(streamObject.attributeList);
                            scratchStreamObject.setAnnotationList(streamObject.annotationList);
                            addEdges(uniqueStreamId, uniqueSourceId);
                        }
                        self._openFormModal.modal('hide');
                    }
                } else if(isEditflow === true) {
                     var isError = self.validateMappingPropertyForEdit();
                     var id;
                     var streamList = self.configurationData.getSiddhiAppConfig().getStreamList();
                     if(isError !== true){
                        if(self._openFormModal.find('#exist-stream-checkbox').is(':checked')){
                            var j = self._openFormModal.find('#exist-stream-list :selected').val();
                            id = streamList[j].getId();
                            if(uniqueStreamId !== undefined){
                                if(id !== uniqueStreamId){
                                    removeEdges(uniqueStreamId, uniqueSourceId);
                                    addEdges(id, uniqueSourceId);
                                }
                            } else {
                                addEdges(id, uniqueSourceId);
                            }
                            self.setTheValueToStreamForEdit();
                            self.setTheValueToSourceForEdit();
                            self._openFormModal.modal('hide');
                        } else {
                            if(uniqueStreamId !== undefined) {
                                 removeEdges(uniqueStreamId,uniqueSourceId);
                            }
                            var scratchStreamName = streamObject.name;
                            var streamId = streamObject.id;
                            var stream = new Stream(streamObject);
                            self.configurationData.getSiddhiAppConfig().addStream(stream);
                            self.designGrid.handleStreamForWizard
                                        (180, 200, false,streamId,scratchStreamName,stream);
                            self.setTheValueToStream();
                            var scratchStreamObject = self.configurationData.getSiddhiAppConfig().getStream(streamId);
                            scratchStreamObject.setAttributeList(streamObject.attributeList);
                            scratchStreamObject.setAnnotationList(streamObject.annotationList);

                            self.setTheValueToSourceForEdit();
                            addEdges(streamId, uniqueSourceId);
                            self._openFormModal.modal('hide');
                        }
                     }
                }else if(isStreamDeleteflow !== undefined){
                     var isError = self.validateMappingPropertyForEdit();
                     if(isError !== true){
                         self.setTheValueToSourceForEdit();
                         var streamList = self.configurationData.getSiddhiAppConfig().getStreamList();
                         if (self._openFormModal.find('#exist-stream-checkbox').is(':checked')){
                                 var j = self._openFormModal.find('#exist-stream-list :selected').val();
                                 var ExistStreamId = streamList[j].getId();
                                 addEdges(ExistStreamId, uniqueSourceId);
                         } else {
                             var scratchStreamName = streamObject.name;
                             var stream = new Stream(streamObject);
                             self.configurationData.getSiddhiAppConfig().addStream(stream);
                             self.designGrid.handleStreamForWizard
                                        (100, 200, false, uniqueStreamId, scratchStreamName, stream);
                             self.setTheValueToStream();
                             var scratchStreamObject = self.configurationData.getSiddhiAppConfig().getStream
                                                                                                    (uniqueStreamId);
                             scratchStreamObject.setAttributeList(streamObject.attributeList);
                             scratchStreamObject.setAnnotationList(streamObject.annotationList);
                             addEdges(uniqueStreamId, uniqueSourceId);
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
                    toolbarExtraButtons: [btnSubmitForm]
                }
            });
            form.on("leaveStep", function (e, anchorObject, stepNumber, stepDirection) {
                if (stepDirection === 'forward') {
                    if((isEditflow ===undefined && isStreamDeleteflow ===undefined) || isStreamDeleteflow !==undefined){
                        if (stepNumber === 0) {
                            if (self._openFormModal.find('#exist-stream-checkbox').is(':checked')){
                                return self.validateListOfStream();
                            } else {
                                return self.validateStreamForm();
                            }
                        }else if(stepNumber === 1){
                            return self.validateTransportProperty();
                        }
                    }else if(isEditflow !== undefined){
                        if (stepNumber === 0) {
                            if (self._openFormModal.find('#exist-stream-checkbox').is(':checked')){
                                  return self.validateListOfStream();
                            } else {
                                return self.validateStreamForm();
                            }
                        } else if(stepNumber === 1) {
                            return self.validateTransportPropertyForEdit();
                        }
                    }
                }
            });
            // Step is passed successfully
           form.on("showStep", function (e, anchorObject, stepNumber, stepDirection, stepPosition) {
                if(stepPosition === 'first') {
                     form.find(".sw-btn-prev").addClass('disabled');
                     form.find(".sw-btn-prev").addClass('hidden');
                     form.find(".sw-btn-prev").parent().removeClass("sw-btn-group-final");
                } else if(stepPosition === 'final') {
                    form.find(".sw-btn-next").addClass('hidden disabled');
                    form.find(".sw-btn-next").parent().addClass("sw-btn-group-final");
                    form.find("#submit-btn").removeClass('hidden');
                } else {
                    form.find(".sw-btn-next").removeClass('disabled');
                    form.find(".sw-btn-next").removeClass('hidden');
                    form.find(".sw-btn-prev").removeClass('disabled');
                    form.find(".sw-btn-prev").removeClass('hidden');
                    form.find(".sw-btn-prev").parent().removeClass("sw-btn-group-final");
                    form.find("#submit-btn").addClass('hidden');
                }
                if (stepDirection === 'forward') {
                    if (stepNumber === 1) {
                        return self.configureTransportProperty();
                    }else if(stepNumber === 2){
                        return self.configureMappingProperty();
                    }
                }
           });
           self._openFormModal.modal('show');
           self.configureStream();
        };

        SourceFormWizard.prototype.configureMappingProperty = function(){
            var self = this;
            var uniqueStreamId = this._uniqueStreamId;
            var streamId = this._streamId;
            var uniqueSourceId = this._uniqueSourceId;
            var streamObject = this._streamObject;
            var isEditflow = this._isEditflow;
            var isStreamDeleteflow = this._isStreamDeleteflow;
            var sourceObject = this._sourceOptions;
            var form = self._openFormModal.find('#form-modal');
            var connectedElement;
            var streamAttributes;
            var sourceMapContainer = self._openFormModal.find("#design-view-source-map-propertyID");
            var streamName = self._openFormModal.find('#streamName').val();
            var streamList = self.configurationData.getSiddhiAppConfig().getStreamList();
            self._openFormModal.find("#mapNano").nanoScroller();

            if((isEditflow === undefined && isStreamDeleteflow === undefined)){
                if (self._openFormModal.find('#exist-stream-checkbox').is(':checked')){
                    var j = self._openFormModal.find('#exist-stream-list :selected').val();
                    var id = streamList[j].getId();
                    var existStreamObject = self.configurationData.getSiddhiAppConfig().getStream(id);
                    connectedElement = existStreamObject.getName();
                    streamAttributes = existStreamObject.getAttributeList();
                } else {
                    connectedElement = streamName;
                    streamAttributes = streamObject.attributeList;
                }
                _.set(sourceObject, 'id', uniqueSourceId);
                _.set(sourceObject, 'annotationType',  'SOURCE');
                _.set(sourceObject, 'connectedElementName',connectedElement);
                self.formBuilder.GeneratePropertiesFormForSourceMapWizard
                                (sourceMapContainer, uniqueSourceId, sourceObject, connectedElement, streamAttributes);
            } else if(isEditflow === true){
                sourceObject = self.configurationData.getSiddhiAppConfig().getSource(uniqueSourceId);
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
                    streamAttributes = streamObject.attributeList;
                    connectedElement = streamObject.name;
                }
                _.set(sourceObject, 'connectedElementName',connectedElement);
                self.formBuilder.GeneratePropertiesFormForSourceMapWizardForEdit
                                                    (sourceMapContainer, uniqueSourceId, streamAttributes, connectedElement);
            } else if(isStreamDeleteflow === true ) {
                 sourceObject = self.configurationData.getSiddhiAppConfig().getSource(uniqueSourceId);
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
                 self.formBuilder.GeneratePropertiesFormForSourceMapWizardForEdit
                                            (sourceMapContainer, uniqueSourceId, streamAttributes, connectedElement);
            }
        };

        SourceFormWizard.prototype.configureTransportProperty = function(){
            var self = this;
            var sourceObject = this._sourceOptions;
            var form = self._openFormModal.find('#form-modal');
            var uniqueSourceId = this._uniqueSourceId;
            var isEditflow = this._isEditflow;
            var isStreamDeleteflow = this._isStreamDeleteflow;

            var sourceContainer = self._openFormModal.find("#design-view-source-form-wizard-contentId");
            self._openFormModal.find("#sourceNano").nanoScroller();

            if((isEditflow === undefined && isStreamDeleteflow === undefined) ){
                _.set(sourceObject, 'id', uniqueSourceId);
                _.set(sourceObject, 'annotationType',  'SOURCE');
                self.formBuilder.GeneratePropertiesFormForSourceWizard(sourceContainer, uniqueSourceId, sourceObject);
            }else if(isEditflow !== undefined || isStreamDeleteflow !== undefined){
                self.formBuilder.GeneratePropertiesFormForSourceEditWizard(sourceContainer, uniqueSourceId);
            }
        };

        SourceFormWizard.prototype.configureStream = function(){
            var self = this;
            var form = self._openFormModal.find('#form-modal');
            var uniqueStreamId = this._uniqueStreamId;
            var uniqueSourceId = this._uniqueSourceId;
            var isEditflow = this._isEditflow;
            var isStreamDeleteflow = this._isStreamDeleteflow;
            var streamObject = this._streamObject;

            var stream_config = '<div class = "form-group"><div class = "form-wizard-form-scrollable-block-list">' +
                '<div id = "stream-list" style = "display:block"><div class = "clearfix">'+
                '<div id = "streamNano" class = "nano"><div class = "nano-content">' +
                '<input type = "checkbox" class="option-checkbox" id="exist-stream-checkbox"'+
                ' name = "streamView" style = "margin:10px 10px 10px 15px">' +
                '<label id = "labelForStreamList" for = "exist-stream-checkbox">Using existing stream</label>'+
                '<div id="exist-stream"></div>'+
                '<div id = "form-template"><div class = "form-wrapper">'+
                '<div id = "design-view-form-wizard-contentId" class = "design-view-form-content">'+
                '</div></div></div></div></div></div></div></div></div>';

            self._openFormModal.find("#stream-config").append(stream_config);

            var existStreamList = '<select name = "stream-name" class = "form-control" id = "exist-stream-list"'+
            ' style = "width:50%; margin: 0px 10px 0px 15px">'+
            '<option value = -1> ----Please select a stream---- </option></select>'+
            '<label class = "error" id = "streamListErrorMessage"> </label>';

            var streamContainer = self._openFormModal.find("#design-view-form-wizard-contentId");
            self._openFormModal.find("#streamNano").nanoScroller();
            if(isEditflow === true){
                self._openFormModal.find('#exist-stream-checkbox').attr('checked', 'checked');
                self._openFormModal.find('#exist-stream').append(existStreamList);
                self.ListOfStream();
                if(uniqueStreamId !== undefined){
                    self._openFormModal.find("#labelForStreamList").html('Stream Name');
                    var existStreamObject = self.configurationData.getSiddhiAppConfig().getStream(uniqueStreamId);
                    var streamName = existStreamObject.getName();
                    var s = self._openFormModal.find('#exist-stream-list');
                    self.setSelectedIndex(s,streamName);
                    self.formBuilder.GeneratePropertiesFormForStreamEditWizard(streamContainer, uniqueStreamId);
                } else {
                     var streamId = self.designGrid.getNewAgentId();
                     _.set(streamObject, 'id', streamId);
                     self._openFormModal.find('#exist-stream-checkbox').prop('checked', false);
                     self._openFormModal.find('#exist-stream').empty();
                    self.formBuilder.GeneratePropertiesFormForStreamWizard(streamContainer, streamId, streamObject);
                }
                self._openFormModal.find('#stream-list').on('change', '#exist-stream-checkbox', function () {
                   if ($(this).is(':checked')) {
                       self._openFormModal.find("#labelForStreamList").html('Stream Name');
                       self._openFormModal.find('#exist-stream').append(existStreamList);
                       self.ListOfStream();
                       var existStreamObject = self.configurationData.getSiddhiAppConfig().getStream(uniqueStreamId);
                       var streamName = existStreamObject.getName();
                       var s = self._openFormModal.find('#exist-stream-list');
                       self.setSelectedIndex(s,streamName);
                       self.formBuilder.GeneratePropertiesFormForStreamEditWizard(streamContainer, uniqueStreamId);
                   } else {
                        var streamId = self.designGrid.getNewAgentId();
                        _.set(streamObject, 'id', streamId);
                       self._openFormModal.find('#exist-stream').empty();
                       self.formBuilder.GeneratePropertiesFormForStreamWizard(streamContainer, streamId, streamObject);
                   }
                });
            }
            if(isEditflow === undefined || isStreamDeleteflow !== undefined){
                _.set(streamObject, 'id', uniqueStreamId);
                self._openFormModal.find('#stream-list').on('change', '#exist-stream-checkbox', function () {
                   if ($(this).is(':checked')) {
                       self._openFormModal.find('#exist-stream').append(existStreamList);
                       self.ListOfStream();
                   } else {
                       self._openFormModal.find('#exist-stream').empty();
                       self.formBuilder.GeneratePropertiesFormForStreamWizard
                                    (streamContainer, uniqueStreamId, streamObject);
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

        SourceFormWizard.prototype.validateTransportProperty = function () {
             var self = this;
             var sourceObject = this._sourceOptions;
             var id = this._uniqueSourceId;

             var sourceContainer = self._openFormModal.find("#design-view-source-form-wizard-contentId");
            var currentSourceOptions = [];

            var predefinedSources = _.orderBy(this.configurationData.rawExtensions["source"], ['name'], ['asc']);
            self.formUtils.removeErrorClass();
            var isErrorOccurred = false;

            var selectedSourceType = self._openFormModal.find('#define-source #source-type').val();;
            if (selectedSourceType === null) {
                DesignViewUtils.prototype.errorAlert("Select a source type to submit.");
                return false;
            } else {
                currentSourceOptions = self.formUtils.getSelectedTypeParameters(selectedSourceType, predefinedSources);
                 if (self.formUtils.validateOptionsForWizard(currentSourceOptions, Constants.SOURCE, sourceContainer)){
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

        SourceFormWizard.prototype.setTheValueToSource = function () {
            var self = this;
            var sourceObject = this._sourceOptions;
            var id = this._uniqueSourceId;

            var predefinedSources = _.orderBy(this.configurationData.rawExtensions["source"], ['name'], ['asc']);

            var selectedSourceType = sourceObject.type;
            currentSourceOptions = self.formUtils.getSelectedTypeParameters(selectedSourceType, predefinedSources);

            var textNode = $('#' + id).find('.sourceNameNode');
            textNode.html(selectedSourceType);

            $('#' + id).removeClass('incomplete-element');
            var sourceToolTip = self.formUtils.getTooltipForWizard(sourceObject, Constants.SOURCE);
            $('#' + id).prop('title', sourceToolTip);

            self.formBuilder.designGrid.dropElements.generateSpecificSourceConnectionElements
                                                (selectedSourceType,self.jsPlumbInstance, id, $('#' + id));
            // set the isDesignViewContentChanged to true
            self.configurationData.setIsDesignViewContentChanged(true);
        };

        SourceFormWizard.prototype.validateTransportPropertyForEdit = function () {
            var self = this;
            var uniqueSourceId = this._uniqueSourceId;
            var sourceObject = self.configurationData.getSiddhiAppConfig().getSource(uniqueSourceId);
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

        SourceFormWizard.prototype.setTheValueToSourceForEdit = function () {
            var self = this;
            var id = this._uniqueSourceId;
            var sourceObject = self.configurationData.getSiddhiAppConfig().getSource(id);
            var sourceContainer = self._openFormModal.find("#design-view-source-form-wizard-contentId");

            var selectedSourceType = self._openFormModal.find('#define-source #source-type').val();
            sourceObject.setType(selectedSourceType);
            var textNode = $('#' + id).find('.sourceNameNode');
            textNode.html(selectedSourceType);

            var annotationOptions = [];
            var sourceOptionsValue = sourceObject.getOptions();
            self.formUtils.buildOptionsForWizard(annotationOptions, Constants.SOURCE, sourceContainer);
            self.formUtils.buildCustomizedOptionForWizard(annotationOptions, Constants.SOURCE, sourceContainer);
            if (annotationOptions.length == 0) {
                sourceObject.setOptions(undefined);
            } else {
                sourceObject.setOptions(annotationOptions);
            }
            $('#' + id).removeClass('error-element');
            //Send source element to the backend and generate tooltip
            var sourceToolTip = self.formUtils.getTooltip(sourceObject, Constants.SOURCE);
            $('#' + id).prop('title', sourceToolTip);

            self.formBuilder.designGrid.dropElements.generateSpecificSourceConnectionElements
                                    (selectedSourceType,self.jsPlumbInstance, id, $('#' + id));
            // set the isDesignViewContentChanged to true
            self.configurationData.setIsDesignViewContentChanged(true);
        };

        SourceFormWizard.prototype.validateStreamForm = function(){
            var self = this;
            var uniqueStreamId = this._uniqueStreamId;
            var isEditflow = this._isEditflow;
            var streamObject = this._streamObject;
            var id;
            if(isEditflow === true) {
                id = streamObject.id;
            } else {
                id = uniqueStreamId;
            }
            var streamContainer = self._openFormModal.find("#design-view-form-wizard-contentId");
            self.formUtils.removeErrorClass();
            var previouslySavedStreamName = streamObject.name;
            var configName = self._openFormModal.find('#streamName').val().trim();
            var streamName;
            var firstCharacterInStreamName;
            var isStreamNameUsed;
            var isErrorOccurred = false;
            var predefinedAnnotationList =
                               _.cloneDeep(self.configurationData.application.config.stream_predefined_annotations);
            /*
            * check whether the stream is inside a partition and if yes check whether it begins with '#'.
            *  If not add '#' to the beginning of the stream name.
            * */
            var isStreamSavedInsideAPartition
                = self.configurationData.getSiddhiAppConfig().getStreamSavedInsideAPartition(id);
            if (!isStreamSavedInsideAPartition) {
                firstCharacterInStreamName = (configName).charAt(0);
                if (firstCharacterInStreamName === '#') {
                    self.formUtils.addErrorClass(self._openFormModal.find("#streamName"));
                    self._openFormModal.find('#streamNameErrorMessage').text
                                                ("'#' is used to define inner streams only.")
                    isErrorOccurred = true;
                    return false;
                } else {
                    streamName = configName;
                }
                isStreamNameUsed = self.formUtils.isDefinitionElementNameUsed(streamName, id);
                if (isStreamNameUsed) {
                    self.formUtils.addErrorClass(self._openFormModal.find("#streamName"));
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
                    self.formUtils.addErrorClass(self._openFormModal.find("#streamName"));
                    self._openFormModal.find('#streamNameErrorMessage').text
                                ("Stream name is already defined in the partition.")
                    isErrorOccurred = true;
                    return false;
                }
            }
            //check if stream name is empty
            if (streamName == "") {
                self.formUtils.addErrorClass(self._openFormModal.find("#streamName"));
                self._openFormModal.find('#streamNameErrorMessage').text("Stream name is required.")
                isErrorOccurred = true;
                return false;
            }
            var previouslySavedName = streamObject.name;
            if (previouslySavedName === undefined) {
                previouslySavedName = "";
            }
            if (previouslySavedName !== streamName) {
                if (self.formUtils.validateAttributeOrElementNameForWizard
                            ("#streamName", Constants.STREAM, streamName,streamContainer)) {
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
                self.formUtils.addErrorClass(self._openFormModal.find('.attribute:eq(0)').find('.attr-name'));
                self._openFormModal.find('.attribute:eq(0)').find('.error-message').text
                                                                ("Minimum one attribute is required.")
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
                _.set(streamObject, 'name', streamName);
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
                _.set(streamObject, 'attributeList', attributeList);
                var annotationStringList = [];
                var annotationObjectList = [];
                self.formUtils.buildAnnotationForWizard
                                    (annotationNodes, annotationStringList, annotationObjectList, streamContainer);
                var annotationList = [];
                var annotationListObjects = []
                _.forEach(annotationStringList, function (annotation) {
                       annotationList.push(annotation);
                });
                _.forEach(annotationObjectList, function (annotation) {
                    annotationListObjects.push(annotation);
                });
                _.set(streamObject, 'annotationList', annotationList);
                _.set(streamObject, 'annotationListObjects', annotationListObjects);
            }
        };

        SourceFormWizard.prototype.setTheValueToStream = function(){
            var self = this;
            var uniqueStreamId = this._uniqueStreamId;
            var isEditflow = this._isEditflow;
            var streamObject = this._streamObject;
            var id;
            if(isEditflow === true){
                id = streamObject.id;
            }else{
                id = uniqueStreamId;
            }
            var streamName = self._openFormModal.find('#streamName').val().trim();
            var textNode = $('#' + id).find('.streamNameNode');
            textNode.html(streamName);

           $('#' + id).removeClass('incomplete-element');
           //Send stream element to the backend and generate tooltip
           var streamToolTip = self.formUtils.getTooltipForWizard(streamObject, Constants.STREAM);
           $('#' + id).prop('title', streamToolTip);
           // set the isDesignViewContentChanged to true
           self.configurationData.setIsDesignViewContentChanged(true)
        };

        SourceFormWizard.prototype.setTheValueToStreamForEdit = function(){
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
            var previouslySavedStreamName = streamObject.getName();
            var isErrorOccurred = false;
            var predefinedAnnotationList =
                            _.cloneDeep(self.configurationData.application.config.stream_predefined_annotations);
            var annotationNodes = [];
            if (self.formUtils.validateAnnotationsForWizard(predefinedAnnotationList,annotationNodes,streamContainer)) {
                isErrorOccurred = true;
                return false;
            }
            if (!isErrorOccurred) {
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
                streamObject.clearAnnotationList();
                streamObject.clearAnnotationListObjects();
                self.formUtils.buildAnnotationForWizard
                                        (annotationNodes, annotationStringList, annotationObjectList,streamContainer);
                _.forEach(annotationStringList, function (annotation) {
                    streamObject.addAnnotation(annotation);
                });
                _.forEach(annotationObjectList, function (annotation) {
                    streamObject.addAnnotationObject(annotation);
                });
                self.formBuilder.designGrid.dropElements.toggleFaultStreamConnector
                                            (streamObject, self.jsPlumbInstance, previouslySavedStreamName);
                $('#' + id).removeClass('incomplete-element');
                //Send stream element to the backend and generate tooltip
                var streamToolTip = self.formUtils.getTooltip(streamObject, Constants.STREAM);
                $('#' + id).prop('title', streamToolTip);
                // set the isDesignViewContentChanged to true
                self.configurationData.setIsDesignViewContentChanged(true)
            }
        };

        SourceFormWizard.prototype.validateMappingProperty = function(){
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
                return isErrorOccurred;
            }
            if (self.formUtils.validateCustomizedOptionsForWizard(Constants.MAPPER, mapContainer)) {
                isErrorOccurred = true;
                return false;
            }
            if (mapContainer.find('#define-attribute #attributeMap-checkBox').is(":checked")) {
                //if attribute section is checked
                var mapperAttributeValuesArray = {};
                var isHasValue;
                mapContainer.find('#mapper-attributes .attribute').each(function () {
                    //validate mapper  attributes if value is not filled
                    var key = $(this).find('.attr-key').val().trim();
                    var value = $(this).find('.attr-value').val().trim();
                    if (value == "") {
                        $(this).find('.error-message').text('Attribute Value is required.');
                        self.formUtils.addErrorClass($(this).find('.attr-value'));
                        isErrorOccurred = true;
                        isHasValue = false;
                    } else {
                        mapperAttributeValuesArray[key] = value;
                        isHasValue = true;
                    }
                });
                return isHasValue;
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
            }
        };

        SourceFormWizard.prototype.validateMappingPropertyForEdit = function(){
            var self = this;
            var uniqueSourceId = this._uniqueSourceId;
            var sourceObject = self.configurationData.getSiddhiAppConfig().getSource(uniqueSourceId);
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

        function addEdges(uniqueStreamId, uniqueSourceId) {
            var sourceId = uniqueSourceId;
            var targetId = uniqueStreamId;
            var edgeId = sourceId + "_" + targetId;
            var sourceType = self.configurationData.getSiddhiAppConfig().getDefinitionElementById
                                                                                        (sourceId, true, true).type;
            var targetType = self.configurationData.getSiddhiAppConfig().getDefinitionElementById
                                                                                (targetId, true, true).type;
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
