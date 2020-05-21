/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *fString
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

define(['require', 'log', 'lodash', 'jquery', 'appData', 'initialiseData', 'jsonValidator'],
    function (require, log, _, $, AppData, InitialiseData, JSONValidator) {

        var ETLTaskView = function (options, container, callback, appObject) {
            console.log(this);
            // var self = this;
            var inputAttributes = ['id', 'name', 'amount'];
            var outputAttributes = ['name', 'count'];
            var color = 'gray';

            this.jsPlumbInstance = window.j = jsPlumb.getInstance({
                Connector: ["Straight", {curviness: 50}],
                DragOptions: {cursor: "pointer", zIndex: 2000},
                PaintStyle: {stroke: color, strokeWidth: 2},
                EndpointStyle: {radius: 3, fill: color},
                HoverPaintStyle: {stroke: "#ec9f2e"},
                EndpointHoverStyle: {fill: "#ec9f2e"},
                Container: $(container).find('.etl-task-wizard-canvas')
            })


            this.inputAttributeEndpoints = {};
            this.outputAttributeEndpoints = {};
            this.connectionMapRef = {};

            var btnGenerateDirectMapping = $(container).find('.direct-map-btn');
            var btnGenerateComplexMapping = $(container).find('.complex-map-btn');
            this.popupBackdropContainer = $(container).find('.popup-backdrop');
            var inputAttributeListContainer = $(container).find('.input-list-container');
            var outputAttributeListContainer = $(container).find('.output-list-container');

            // function binding
            this.directMapBtnHandle = this.directMapBtnHandle.bind(this);
            this.complexMapBtnHandle = this.complexMapBtnHandle.bind(this);
            this.directMappingSubmitHandler = this.directMappingSubmitHandler.bind(this);
            this.hideMappingDialog = this.hideMappingDialog.bind(this);
            this.complexMappingSubmitHandler = this.complexMappingSubmitHandler.bind(this);
            this.editDirectMapBtnHandle = this.editDirectMapBtnHandle.bind(this);
            this.editDirectMappingSubmit = this.editDirectMappingSubmit.bind(this);
            this.deleteDirectMappingHandle = this.deleteDirectMappingHandle.bind(this);
            this.editComplexMappingBtnHandle = this.editComplexMappingBtnHandle.bind(this);
            this.editComplexMappingSubmit = this.editComplexMappingSubmit.bind(this);

            btnGenerateComplexMapping.on('click', this.complexMapBtnHandle);
            btnGenerateDirectMapping.on('click', this.directMapBtnHandle);

            // Render Attribute endpoints
            this.renderAttributeEndpoints(inputAttributes, outputAttributes,
                inputAttributeListContainer, outputAttributeListContainer, this);


        }

        ETLTaskView.prototype.complexMapBtnHandle = function () {
            this.popupBackdropContainer.show();
            var bodyContentContainer = this.popupBackdropContainer.find('.map-generation-content');
            this.popupBackdropContainer.find('.dialog-heading').text("Create Complex mapping for");
            bodyContentContainer.append(`
                    <div style="">
                        <!--                                Input Attribute-->
                        <label for="complexMapOutput">Output Attribute</label>
                        <select id="complexMapOutput" style="">
                        </select>
                    </div>
                    <div style="">
                        <!--                                Output Attribute-->
                        <label for="complexMapExpression">Expression</label>
                        <input id="complexMapExpression" type="text">
                    </div>
            `);

            var btnSubmit = this.popupBackdropContainer.find('.btn-primary');
            var btnCancel = this.popupBackdropContainer.find('.btn-default');

            var outputAttributeOptions = $(bodyContentContainer).find('#complexMapOutput');
            var connectionMapping = this.connectionMapRef;

            Object.keys(this.outputAttributeEndpoints).forEach(function (el) {
                if (!(connectionMapping[el])) {
                    outputAttributeOptions.append(`<option value="${el}">${el}</option>`);
                }
            });

            btnSubmit.off();
            btnSubmit.on('click', this.complexMappingSubmitHandler);
            btnCancel.on('click', this.hideMappingDialog);
        }

        ETLTaskView.prototype.directMapBtnHandle = function () {
            this.popupBackdropContainer.show();
            this.popupBackdropContainer.find('.dialog-heading').text("Create Direct Mapping between");
            var bodyContentContainer = this.popupBackdropContainer.find('.map-generation-content');
            bodyContentContainer.append(`
                    <div style="">
                        <label for="inputSelect">Input Attribute</label>
                        <select id="inputSelect" style="">
                            
                        </select>
                    </div>
                    <div style="">
                        <label for="outputSelect">Output Attribute</label>
                        <select id="outputSelect" style="">
                            
                        </select>
                    </div>
            `);

            var btnSubmit = this.popupBackdropContainer.find('.btn-primary');
            var btnCancel = this.popupBackdropContainer.find('.btn-default');

            var inputAttributeOptions = $(bodyContentContainer).find('#inputSelect');
            var outputAttributeOptions = $(bodyContentContainer).find('#outputSelect');
            var connectionMapping = this.connectionMapRef;

            Object.keys(this.inputAttributeEndpoints).forEach(function (el) {
                inputAttributeOptions.append(`<option value="${el}">${el}</option>`);
            });

            Object.keys(this.outputAttributeEndpoints).forEach(function (el) {
                if (!(connectionMapping[el])) {
                    outputAttributeOptions.append(`<option value="${el}">${el}</option>`);
                }
            });

            btnSubmit.off();
            btnSubmit.on('click', this.directMappingSubmitHandler);
            btnCancel.on('click', this.hideMappingDialog);
        }

        ETLTaskView.prototype.directMappingSubmitHandler = function () {
            var editBtnHandle = this.editDirectMapBtnHandle;
            var deleteBtnHandle = this.deleteDirectMappingHandle;

            var inputOption = this.popupBackdropContainer.find('#inputSelect').val();
            var outputOption = this.popupBackdropContainer.find('#outputSelect').val();

            var connection = this.jsPlumbInstance.connect({
                source: this.inputAttributeEndpoints[inputOption],
                target: this.outputAttributeEndpoints[outputOption]
            });

            var actionBtnDiv = $(this.outputAttributeEndpoints[outputOption].getElement()).append(`
                                <div class="action-btn-div">
                                    <a title="Edit mapping" href="#" class="icon edit" style="">
                                        <i class="fw fw-edit" style=""></i>
                                    </a>
                                    <a title="Delete mapping" href="#" class="icon delete" style="">
                                        <i class="fw fw-delete" style=""></i>
                                    </a>
                                </div>`);
            // this.connectionMapRef.push({target: outputOption, connections: [connection]});
            this.connectionMapRef[outputOption] = {connections: [connection]};
            this.hideMappingDialog();

            actionBtnDiv.find('.edit').on('click.edit.' + outputOption, function () {
                editBtnHandle(inputOption, outputOption, connection, actionBtnDiv);
            });
            actionBtnDiv.find('.delete').on('click.edit.' + outputOption, function () {
                deleteBtnHandle(connection, outputOption, actionBtnDiv);
            });
        }

        ETLTaskView.prototype.editDirectMapBtnHandle = function (inputOption, outputOption, connection, actionBtnDiv) {
            this.directMapBtnHandle();
            this.popupBackdropContainer.find('.dialog-heading').text("Edit Direct Mapping for " + outputOption);
            var editMappingSubmit = this.editDirectMappingSubmit;

            var inputAttributeOptions = this.popupBackdropContainer.find('#inputSelect');
            var outputAttributeOptions = this.popupBackdropContainer.find('#outputSelect');
            var btnSubmit = this.popupBackdropContainer.find('.btn-primary');
            var btnCancel = this.popupBackdropContainer.find('.btn-default');

            outputAttributeOptions.prepend(`<option value="${outputOption}">${outputOption}</option>`);
            inputAttributeOptions.val(inputOption);
            outputAttributeOptions.val(outputOption);

            btnSubmit.off();
            btnSubmit.on('click', function () {
                editMappingSubmit(inputAttributeOptions.val(), outputAttributeOptions.val(), outputOption, connection, actionBtnDiv);
            });
        }

        ETLTaskView.prototype.editComplexMappingBtnHandle = function (outputOption, expression,
                                                                      connections, actionBtnDiv) {
            console.log('Clicked edit btn for Complex Mapping');
            this.complexMapBtnHandle();
            this.popupBackdropContainer.find('.dialog-heading').text("Edit Complex Mapping for " + outputOption);
            var editMappingSubmit = this.editComplexMappingSubmit;

            var outputAttributeSelectElement = $(this.popupBackdropContainer).find('#complexMapOutput');
            var mappedExpressionElement = $(this.popupBackdropContainer).find('#complexMapExpression');
            var btnSubmit = this.popupBackdropContainer.find('.btn-primary');

            outputAttributeSelectElement.prepend(`<option value="${outputOption}">${outputOption}</option>`);
            outputAttributeSelectElement.val(outputOption);
            mappedExpressionElement.val(expression);

            btnSubmit.off();
            btnSubmit.on('click', function () {
                editMappingSubmit(outputAttributeSelectElement.val(), mappedExpressionElement.val(),
                    outputOption, connections, actionBtnDiv);

            });
        }

        ETLTaskView.prototype.editComplexMappingSubmit = function (outputAttribute, expression, prevAttribute,
                                                                   connections, actionBtnDiv) {

            console.log(outputAttribute, expression, prevAttribute, connections, actionBtnDiv);
            delete this.connectionMapRef[prevAttribute];
            this.jsPlumbInstance.deleteConnectionsForElement(connections[0].target);
            var editBtnHandle = this.editComplexMappingBtnHandle;
            $(actionBtnDiv).parent().find('.expression').remove();
            $(actionBtnDiv).remove();
            var mapEntry = getConnectedEntries.call(this, expression, this.inputAttributeEndpoints,
                this.jsPlumbInstance, this.outputAttributeEndpoints[outputAttribute]);

            mapEntry.expression = expression;
            this.connectionMapRef[outputAttribute] = mapEntry;

            var targetContainer = $(this.outputAttributeEndpoints[outputAttribute].getElement());

            targetContainer.append(`
                                <div class="action-btn-div">
                                    <a title="Edit mapping" href="#" class="icon edit" style="">
                                        <i class="fw fw-edit" style=""></i>
                                    </a>
                                    <a title="Delete mapping" href="#" class="icon delete" style="">
                                        <i class="fw fw-delete" style=""></i>
                                    </a>
                                </div>`);

            targetContainer.append(`<div class="expression">${expression}</div>`);

            targetContainer.find('.edit').on('click.edit.' + outputAttribute, function () {
                editBtnHandle(outputAttribute, expression, mapEntry.connections, targetContainer.find('.action-btn-div'));
            });

            targetContainer.find('.delete').on('click', function () {
                console.log('delete')
            })

            console.log(mapEntry);
            this.hideMappingDialog();
            this.jsPlumbInstance.repaintEverything();
        }

        ETLTaskView.prototype.deleteDirectMappingHandle = function (connection, outputOption, actionBtnDiv) {
            delete this.connectionMapRef[outputOption];
            this.jsPlumbInstance.deleteConnectionsForElement(connection.target);

            $(actionBtnDiv).find('.action-btn-div').remove();
        }

        ETLTaskView.prototype.editDirectMappingSubmit = function (inputOption, outputOption, prevOption, connection, actionBtnDiv) {
            delete this.connectionMapRef[prevOption];
            this.jsPlumbInstance.deleteConnectionsForElement(connection.target);

            this.connectionMapRef[outputOption] = this.jsPlumbInstance.connect({
                source: this.inputAttributeEndpoints[inputOption],
                target: this.outputAttributeEndpoints[outputOption]
            });

            this.hideMappingDialog();
            $(actionBtnDiv).find('.action-btn-div').remove();
        }


        function getConnectedEntries(expression, inputAttributeEndpoints, jsPlumbInstance, targetEndpoint) {
            var connectedInputs = [];

            Object.keys(this.inputAttributeEndpoints).forEach(function (key) {
                if (expression.indexOf(key) > -1) {
                    connectedInputs.push(inputAttributeEndpoints[key]);
                }
            });

            var mapEntry = {connections: []}
            connectedInputs.forEach(function (endpoint) {
                var connection = jsPlumbInstance.connect({
                    source: endpoint,
                    target: targetEndpoint
                });
                mapEntry.connections.push(connection);
            });
            return mapEntry;
        }

        ETLTaskView.prototype.complexMappingSubmitHandler = function () {
            var editBtnHandle = this.editComplexMappingBtnHandle;
            var outputOption = $(this.popupBackdropContainer).find('#complexMapOutput').val();
            var expression = $(this.popupBackdropContainer).find('#complexMapExpression').val();
            var targetEndpoint = this.outputAttributeEndpoints[outputOption];
            var inputAttributeEndpoints = this.inputAttributeEndpoints;
            var jsPlumbInstance = this.jsPlumbInstance;

            var mapEntry = getConnectedEntries.call(this, expression, inputAttributeEndpoints, jsPlumbInstance, targetEndpoint);

            // this.connectionMapRef.push(mapEntry);
            this.connectionMapRef[outputOption] = mapEntry;
            $(targetEndpoint.getElement()).append(`
                                <div class="action-btn-div">
                                    <a title="Edit mapping" href="#" class="icon edit" style="">
                                        <i class="fw fw-edit" style=""></i>
                                    </a>
                                    <a title="Delete mapping" href="#" class="icon delete" style="">
                                        <i class="fw fw-delete" style=""></i>
                                    </a>
                                </div>`);
            $(targetEndpoint.getElement()).append(`<div class="expression">${expression}</div>`);
            this.hideMappingDialog();
            this.jsPlumbInstance.repaintEverything();

            var actionBtnDiv = $(targetEndpoint.getElement()).find('.action-btn-div');

            actionBtnDiv.find('.edit').on('click.edit.' + outputOption, function () {
                editBtnHandle(outputOption, expression, mapEntry.connections, actionBtnDiv);
            });
            actionBtnDiv.find('.delete').on('click.edit.' + outputOption, function () {
                // deleteBtnHandle(connection, outputOption,actionBtnDiv);
            });

        }

        ETLTaskView.prototype.hideMappingDialog = function () {
            this.popupBackdropContainer.find('.map-generation-content').empty();
            this.popupBackdropContainer.hide();
            this.popupBackdropContainer.find('.btn-primary').off();
        }

        ETLTaskView.prototype.renderAttributeEndpoints = function (inputAttributes, outputAttributes,
                                                                   inputListContainer, outputListContainer, context) {
            context.jsPlumbInstance.batch(function () {
                inputAttributes.forEach(function (el) {
                    var attribute = $('<li><div class="attribute input">' + el + '</div></li>');
                    inputListContainer.append(attribute);
                    context.inputAttributeEndpoints[el] =
                        context.jsPlumbInstance.addEndpoint($(attribute).find('.attribute'), {anchor: 'Right'}, {
                            isSource: true,
                            maxConnections: -1
                        });
                });

                outputAttributes.forEach(function (el) {
                    var attribute = $('<li><div class="attribute output">' + el + '</div></li>');
                    outputListContainer.append(attribute);
                    context.outputAttributeEndpoints[el] =
                        context.jsPlumbInstance.addEndpoint($(attribute).find('.attribute'), {anchor: 'Left'}, {
                            isTarget: true,
                            maxConnections: -1
                        });
                })
            });
        }

        return ETLTaskView;
    });
