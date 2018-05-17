/**
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

define(['require', 'log', 'jquery', 'lodash'],
    function (require, log, $, _) {

        /**
         * @class PartitionForm Creates a forms to collect data from a partition
         * @constructor
         * @param {Object} options Rendering options for the view
         */
        var PartitionForm = function (options) {
            if (options !== undefined) {
                this.configurationData = options.configurationData;
                this.application = options.application;
                this.consoleListManager = options.application.outputController;
                this.jsPlumbInstance = options.self.jsPlumbInstance;
                var currentTabId = this.application.tabController.activeTab.cid;
                this.designViewContainer = $('#design-container-' + currentTabId);
                this.toggleViewButton = $('#toggle-view-button-' + currentTabId);
            }
        };

        /**
         * @function generate form for Partition
         * @param element selected element(partition)
         * @param formConsole Console which holds the form
         * @param formContainer Container which holds the form
         */
        PartitionForm.prototype.generatePropertiesForm = function (element, formConsole, formContainer) {
            var self = this;
            var id = $(element.target).parent().attr('id');
            var partition = self.configurationData.getSiddhiAppConfig().getPartition(id);
            var connections = self.jsPlumbInstance.getConnections(element);
            var connected= false;
            var connectedStream = null;
            $.each(connections, function (index, connection) {
                var target = connection.targetId;
                if(target.substr(0, target.indexOf('-')) == id){
                    connected = true;
                    var source = connection.sourceId;
                    connectedStream = source.substr(0, source.indexOf('-'));
                }
            });
            if(!(connected)){
                alert('Connect a stream for partitioning');
                // design view container and toggle view button are enabled
                self.designViewContainer.removeClass('disableContainer');
                self.toggleViewButton.removeClass('disableContainer');
            }
            else{
                var fillWith= {};
                var partitionKeys = partition.getPartition().with;
                $.each(partitionKeys, function ( index , key) {
                    if( key.stream == connectedStream){
                        fillWith ={
                            stream : (self.configurationData.getSiddhiAppConfig().getStream(connectedStream)).getName(),
                            property : key.property
                        }
                    }
                });

                // design view container and toggle view button are enabled
                self.designViewContainer.addClass('disableContainer');
                self.toggleViewButton.addClass('disableContainer');

                var editor = new JSONEditor(formContainer[0], {
                    ajax: true,
                    schema: {
                        type: 'object',
                        title: 'Partition Key',
                        properties: {
                            stream: {
                                type: 'string',
                                title: 'Stream',
                                required: true,
                                propertyOrder: 1,
                                template: (self.configurationData.getSiddhiAppConfig().getStream(connectedStream)).getName()
                            },
                            property: {
                                type: 'string',
                                title: 'Property',
                                required: true,
                                propertyOrder: 2
                            }
                        }
                    },
                    startval: fillWith,
                    disable_properties: true
                });
                formContainer.append('<div id="form-submit"><button type="button" ' +
                    'class="btn btn-default">Submit</button></div>' +
                    '<div id="form-cancel"><button type="button" class="btn btn-default">Cancel</button></div>');

                // 'Submit' button action
                var submitButtonElement = $(formContainer).find('#form-submit')[0];
                submitButtonElement.addEventListener('click', function () {

                    var config = editor.getValue();
                    $.each(partitionKeys, function ( index , key) {
                        if( key.stream == connectedStream){
                            key.property = config.property
                        }
                        else {
                            var key = { stream : connectedStream , property : config.property};
                            partitionKeys['with'].push(key);
                        }
                    });
                });
                // 'Cancel' button action
                var cancelButtonElement = $(formContainer).find('#form-cancel')[0];
                cancelButtonElement.addEventListener('click', function () {
                    self.designViewContainer.removeClass('disableContainer');
                    self.toggleViewButton.removeClass('disableContainer');

                    // close the form window
                    self.consoleListManager.removeConsole(formConsole);
                    self.consoleListManager.hideAllConsoles();
                });
            }
        };

        return PartitionForm;
    });
