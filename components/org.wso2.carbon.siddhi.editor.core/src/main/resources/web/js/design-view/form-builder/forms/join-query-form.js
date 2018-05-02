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

define(['require', 'log', 'jquery', 'lodash', 'querySelect', 'queryOutputInsert', 'queryOutputDelete',
        'queryOutputUpdate', 'queryOutputUpdateOrInsertInto', 'queryWindow', 'queryOrderByValue'],
    function (require, log, $, _, QuerySelect, QueryOutputInsert, QueryOutputDelete, QueryOutputUpdate,
              QueryOutputUpdateOrInsertInto, QueryWindow, QueryOrderByValue) {

        /**
         * @class JoinQueryForm Creates a forms to collect data from a join query
         * @constructor
         * @param {Object} options Rendering options for the view
         */
        var JoinQueryForm = function (options) {
            this.configurationData = options.configurationData;
            this.application = options.application;
            this.consoleListManager = options.application.outputController;
            this.gridContainer = $("#grid-container");
            this.toolPaletteContainer = $("#tool-palette-container");
        };

        /**
         * @function generate the form for the join query
         * @param element selected element(query)
         * @param formConsole Console which holds the form
         * @param formContainer Container which holds the form
         */
        JoinQueryForm.prototype.generatePropertiesForm = function (element, formConsole, formContainer) {
            var self = this;
            //     // The container and the tool palette are disabled to prevent the user from dropping any elements
            //     self.gridContainer.addClass('disabledbutton');
            //     self.toolPaletteContainer.addClass('disabledbutton');
            //
            //     var id = $(element).parent().attr('id');
            //     var clickedElement = self.configurationData.getSiddhiAppConfig().getJoinQuery(id);
            //     if (!(clickedElement.getFrom()) || clickedElement.getFrom().length !== 2) {
            //         alert('Connect TWO input streams');
            //         self.gridContainer.removeClass('disabledbutton');
            //         self.toolPaletteContainer.removeClass('disabledbutton');
            //
            //         // close the form window
            //         self.consoleListManager.removeConsole(formConsole);
            //         self.consoleListManager.hideAllConsoles();
            //     }
            //     else if (!(clickedElement.getInsertInto())) {
            //         alert('Connect an output stream');
            //         self.gridContainer.removeClass('disabledbutton');
            //         self.toolPaletteContainer.removeClass('disabledbutton');
            //
            //         // close the form window
            //         self.consoleListManager.removeConsole(formConsole);
            //         self.consoleListManager.hideAllConsoles();
            //     }
            //
            //     else {
            //         var streams = [];
            //         $.each(clickedElement.getFrom(), function (index, streamID) {
            //             streams.push((self.configurationData.getSiddhiAppConfig().getStream(streamID)).getName());
            //         });
            //         var projections = [];
            //         var insertInto = self.configurationData.getSiddhiAppConfig().getStream(clickedElement.getInsertInto()).getName();
            //         var outStreamAttributes = (self.configurationData.getSiddhiAppConfig().getStream(clickedElement.getInsertInto())).getAttributeList();
            //         if (!(clickedElement.getProjection())) {
            //             for (var i = 0; i < outStreamAttributes.length; i++) {
            //                 var attr = {select: '', newName: outStreamAttributes[i].attribute};
            //                 projections.push(attr);
            //             }
            //         }
            //         else {
            //             var selectedAttributes = clickedElement.getProjection();
            //             for (var i = 0; i < outStreamAttributes.length; i++) {
            //                 var attr = {select: selectedAttributes[i], newName: outStreamAttributes[i].attribute};
            //                 projections.push(attr);
            //             }
            //         }
            //         var fillWith = {
            //             projection: projections,
            //             outputType: clickedElement.getOutputType(),
            //             insertInto: insertInto
            //         };
            //
            //         if (clickedElement.getJoin()) {
            //             fillWith.type = clickedElement.getJoin().getType();
            //             fillWith.leftStream = clickedElement.getJoin().getLeftStream();
            //             fillWith.rightStream = clickedElement.getJoin().getRightStream();
            //             fillWith.on = clickedElement.getJoin().getOn();
            //         }
            //         var editor = new JSONEditor(formContainer[0], {
            //             ajax: true,
            //             schema: {
            //                 type: 'object',
            //                 title: 'Query',
            //                 properties: {
            //                     type: {
            //                         type: 'string',
            //                         title: 'Type',
            //                         required: true,
            //                         propertyOrder: 1
            //                     },
            //                     leftStream: {
            //                         type: 'object',
            //                         title: 'Left Stream',
            //                         required: true,
            //                         propertyOrder: 2,
            //                         options: {
            //                             disable_collapse: false,
            //                             disable_properties: true,
            //                             collapsed: true
            //                         },
            //                         properties: {
            //                             from: {
            //                                 type: 'string',
            //                                 title: 'From',
            //                                 enum: streams,
            //                                 required: true
            //                             },
            //                             filter: {
            //                                 type: 'string',
            //                                 title: 'Filter',
            //                                 required: true
            //                             },
            //                             window: {
            //                                 type: 'string',
            //                                 title: 'Window',
            //                                 required: true
            //                             },
            //                             postWindowFilter: {
            //                                 type: 'string',
            //                                 title: 'Post Window Filter',
            //                                 required: true
            //                             },
            //                             as: {
            //                                 type: 'string',
            //                                 title: 'As',
            //                                 required: true
            //                             }
            //                         }
            //                     },
            //                     rightStream: {
            //                         type: 'object',
            //                         title: 'Right Stream',
            //                         required: true,
            //                         propertyOrder: 3,
            //                         options: {
            //                             disable_collapse: false,
            //                             disable_properties: true,
            //                             collapsed: true
            //                         },
            //                         properties: {
            //                             from: {
            //                                 type: 'string',
            //                                 title: 'From',
            //                                 enum: streams,
            //                                 required: true
            //                             },
            //                             filter: {
            //                                 type: 'string',
            //                                 title: 'Filter',
            //                                 required: true
            //                             },
            //                             window: {
            //                                 type: 'string',
            //                                 title: 'Window',
            //                                 required: true
            //                             },
            //                             postWindowFilter: {
            //                                 type: 'string',
            //                                 title: 'Post Window Filter',
            //                                 required: true
            //                             },
            //                             as: {
            //                                 type: 'string',
            //                                 title: 'As',
            //                                 required: true
            //                             }
            //                         }
            //                     },
            //                     on: {
            //                         type: 'string',
            //                         title: 'On',
            //                         required: true,
            //                         propertyOrder: 4
            //                     },
            //                     projection: {
            //                         type: 'array',
            //                         title: 'Projection',
            //                         format: 'table',
            //                         required: true,
            //                         propertyOrder: 5,
            //                         options: {
            //                             disable_array_add: true,
            //                             disable_array_delete: true,
            //                             disable_array_reorder: true
            //                         },
            //                         items: {
            //                             type: 'object',
            //                             title: 'Attribute',
            //                             properties: {
            //                                 select: {
            //                                     type: 'string',
            //                                     title: 'select'
            //                                 },
            //                                 newName: {
            //                                     type: 'string',
            //                                     title: 'as'
            //                                 }
            //                             }
            //                         }
            //                     },
            //                     outputType: {
            //                         type: 'string',
            //                         title: 'Output Type',
            //                         enum: ['all events', 'current events', 'expired events'],
            //                         default: 'all events',
            //                         required: true,
            //                         propertyOrder: 6
            //                     },
            //                     insertInto: {
            //                         type: 'string',
            //                         title: 'Insert Into',
            //                         template: insertInto,
            //                         required: true,
            //                         propertyOrder: 7
            //                     }
            //                 }
            //             },
            //             startval: fillWith,
            //             no_additional_properties: true,
            //             disable_properties: true
            //         });
            //         for (var i = 0; i < outStreamAttributes.length; i++) {
            //             editor.getEditor('root.projection.' + i + '.newName').disable();
            //         }
            //         editor.getEditor('root.insertInto').disable();
            //
            //         $(formContainer).append('<div id="form-submit"><button type="button" ' +
            //             'class="btn btn-default">Submit</button></div>' +
            //             '<div id="form-cancel"><button type="button" class="btn btn-default">Cancel</button></div>');
            //
            //         // 'Submit' button action
            //         var submitButtonElement = $('#form-submit')[0];
            //         submitButtonElement.addEventListener('click', function () {
            //             self.gridContainer.removeClass('disabledbutton');
            //             self.toolPaletteContainer.removeClass('disabledbutton');
            //
            //             // close the form window
            //             self.consoleListManager.removeConsole(formConsole);
            //             self.consoleListManager.hideAllConsoles();
            //
            //             var config = editor.getValue();
            //             // update selected query object
            //             var leftStreamOptions = {};
            //             _.set(leftStreamOptions, 'from', config.leftStream.from);
            //             _.set(leftStreamOptions, 'filter', config.leftStream.filter);
            //             _.set(leftStreamOptions, 'window', config.leftStream.window);
            //             _.set(leftStreamOptions, 'postWindowFilter', config.leftStream.postWindowFilter);
            //             _.set(leftStreamOptions, 'as', config.leftStream.as);
            //             var leftStream = new LeftStream(leftStreamOptions);
            //
            //             var rightStreamOptions = {};
            //             _.set(leftStreamOptions, 'from', config.rightStream.from);
            //             _.set(leftStreamOptions, 'filter', config.rightStream.filter);
            //             _.set(leftStreamOptions, 'window', config.rightStream.window);
            //             _.set(leftStreamOptions, 'postWindowFilter', config.rightStream.postWindowFilter);
            //             _.set(leftStreamOptions, 'as', config.rightStream.as);
            //             var rightStream = new RightStream(rightStreamOptions);
            //
            //             var joinOptions = {};
            //             _.set(joinOptions, 'type', config.type);
            //             _.set(joinOptions, 'left-stream', leftStream);
            //             _.set(joinOptions, 'right-stream', rightStream);
            //             _.set(joinOptions, 'on', config.on);
            //             var join =new Join(joinOptions);
            //
            //             clickedElement.setJoin(join);
            //             var projections = [];
            //             $.each(config.projection, function (index, attribute) {
            //                 projections.push(attribute.select);
            //             });
            //             clickedElement.setProjection(projections);
            //             clickedElement.setOutputType(config.outputType);
            //         });
            //
            //         // 'Cancel' button action
            //         var cancelButtonElement = $('#form-cancel')[0];
            //         cancelButtonElement.addEventListener('click', function () {
            //             self.gridContainer.removeClass('disabledbutton');
            //             self.toolPaletteContainer.removeClass('disabledbutton');
            //
            //             // close the form window
            //             self.consoleListManager.removeConsole(formConsole);
            //             self.consoleListManager.hideAllConsoles();
            //         });
            //     }
        };

        return JoinQueryForm;
    });
