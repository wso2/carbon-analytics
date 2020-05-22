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
            var inputAttributes = [
                {
                    name: 'id',
                    type: 'string',
                },
                {
                    name: 'name',
                    type: 'string',
                },
                {
                    name: 'amount',
                    type: 'int'
                }
            ];
            var outputAttributes = [
                {
                    name: 'name',
                    type: 'string'
                },
                {
                    name: 'count',
                    type: 'string'
                }
            ];

            var color = 'gray';

            this.jsPlumbInstance = window.j = jsPlumb.getInstance({
                Connector: ["Straight", {curviness: 50}],
                DragOptions: {cursor: "pointer", zIndex: 2000},
                PaintStyle: {stroke: color, strokeWidth: 2},
                EndpointStyle: {radius: 3, fill: 'rgba(0, 0, 0, 0)'},
                endpointHoverStyle: {fill: 'rgba(0, 0, 0, 0)'},
                HoverPaintStyle: {stroke: "#ec9f2e"},
                EndpointHoverStyle: {fill: "#ec9f2e"},
                Container: $(container).find('.etl-task-wizard-canvas')
            })

            this.inputAttributeEndpoints = {};
            this.outputAttributeEndpoints = {};
            this.connectionMapRef = {};

            this.inputListContainer = $(container).find('.etl-task-wizard-canvas').find('.inputs').find('.attributeList');
            this.outputListContainer = $(container).find('.etl-task-wizard-canvas').find('.outputs').find('.attributeList');

            this.renderAttributes(inputAttributes, outputAttributes);

        }

        ETLTaskView.prototype.renderAttributes = function (inputAttributes, outputAttributes) {
            var inputListContainer = this.inputListContainer;
            var outputListContainer = this.outputListContainer;
            var jsPlumbInstance = this.jsPlumbInstance;
            var inputEndpointMap = {};
            var outputEndpointMap = {};

            inputAttributes.forEach(function (element) {
                var inputAttribElement = inputListContainer.append(`
                    <li>
                        <div class="attribute" style="">
                            ${element.name}
                            <div class="attrib-type" style="">
                                ${element.type}
                            </div>
                        </div>
                    </li>
                `);

                inputEndpointMap[element.name] = jsPlumbInstance.addEndpoint($(inputAttribElement).children().last(), {anchor: 'Right'}, {isSource: true});
            });

            outputAttributes.forEach(function (element) {
                var outputAttribElement = outputListContainer.append(`
                    <li>
                        <div class="attribute" style="">
                            ${element.name}
                            <div class="clear-icon">
                                <a href="#" title="Clear mapping" href="#" class="icon clear" style="">
                                    <i class="fw fw-clear"></i>
                                </a>
                            </div>
                            <div class="attrib-type" style="">
                                ${element.type}
                            </div>
                            <div class="mapped-expression" style="">
                            </div>
                        </div>
                    </li>
                `)

                outputEndpointMap[element.name] = jsPlumbInstance.addEndpoint($(outputAttribElement).children().last(), {anchor: 'Left'}, {isTarget: true});

            });

            this.inputAttributeEndpoints = inputEndpointMap;
            this.outputAttributeEndpoints = outputEndpointMap;
        }

        return ETLTaskView;
    });
