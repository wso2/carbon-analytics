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

define(['require'],
    function (require) {

        //common properties for the JSON editor
        JSONEditor.defaults.options.theme = 'bootstrap3';
        JSONEditor.defaults.options.iconlib = 'bootstrap3';
        JSONEditor.defaults.options.disable_edit_json = true;
        JSONEditor.plugins.sceditor.emoticonsEnabled = true;
        JSONEditor.defaults.options.disable_collapse = true;
        JSONEditor.plugins.selectize.enable = true;

        /**
         * @class AppData
         * @constructor
         * @class DesignView  Wraps the Ace editor for design view
         */
        var FormBuilder = function () {

        };

        /**
         * @function generate the form to define the stream once it is dropped on the canvas
         * @param newAgent
         * @param i
         * @param mouseTop
         * @param mouseLeft
         */
        FormBuilder.prototype.DefineStream = function (newAgent, i, mouseTop, mouseLeft) {
            var propertyWindow = document.getElementsByClassName('property');
            $(propertyWindow).collapse('show');
            $(propertyWindow).html('<div id="property-header"><h3>Define Stream </h3></div>' +
                '<div id="define-stream" class="define-stream"></div>');

            var header = document.getElementById('property-header');
            //generate the form to define a stream
            var editor = new JSONEditor(document.getElementById('define-stream'), {
                schema: {
                    type: "object",
                    title: "Stream",
                    properties: {
                        name: {
                            type: "string",
                            title: "Name"
                        },
                        attributes: {
                            type: "array",
                            format: "table",
                            title: "Attributes",
                            uniqueItems: true,
                            items: {
                                type: "object",
                                title : 'Attribute',
                                properties: {
                                    attribute: {
                                        type: "string"
                                    },
                                    type: {
                                        type: "string",
                                        enum: [
                                            "string",
                                            "int",
                                            "long",
                                            "float",
                                            "double",
                                            "boolean"
                                        ],
                                        default: "string"
                                    }
                                }
                            }
                        }
                    }
                },
                disable_properties: true,
                disable_array_delete_all_rows: true,
                disable_array_delete_last_row: true
            });

            $(propertyWindow).append('<div id="submit"><button>Submit</button></div>');
            //'Submit' button action
            document.getElementById('submit').addEventListener('click', function () {
                // create a new stream model and add to the collection
                var newStream = new app.Stream;
                newStream.set('id', i);
                newStream.set('define', editor.getValue().name);
                newStream.set('attributes', editor.getValue().attributes);
                streamList.add(newStream);
                //close the property window
                $(propertyWindow).html('');
                $(propertyWindow).collapse('hide');
                dropStream(newAgent, i, mouseTop, mouseLeft, editor.getValue().name);
            });
        };

        /**
         * @function generate the property window for an existing stream
         * @param element
         */
        FormBuilder.prototype.GeneratePropertiesFormForStreams = function (element) {
            var propertyWindow = document.getElementsByClassName('property');
            $(propertyWindow).collapse('show');
            //$("#container").addClass('disabledbutton');
            //$("#toolbox").addClass('disabledbutton');
            var id = $(element).parent().attr('id');
            //retrieve the stream information from the collection
            var clickedElement = streamList.get(id);
            var name = clickedElement.get('define');
            var attributes = clickedElement.get('attributes');
            var fillWith = {
                name : name,
                attributes : attributes
            };
            var editor = new JSONEditor(document.getElementById('propertypane'), {
                schema: {
                    type: "object",
                    title: "Stream",
                    properties: {
                        name: {
                            type: "string",
                            title: "Name"
                        },
                        attributes: {
                            type: "array",
                            format: "table",
                            title: "Attributes",
                            uniqueItems: true,
                            items: {
                                type: "object",
                                properties: {
                                    attribute: {
                                        type: "string"
                                    },
                                    type: {
                                        type: "string",
                                        enum: [
                                            "string",
                                            "int",
                                            "long",
                                            "float",
                                            "double",
                                            "boolean"
                                        ],
                                        default: "string"
                                    }
                                }
                            }
                        }
                    }
                },
                disable_properties: true,
                disable_array_delete_all_rows: true,
                disable_array_delete_last_row: true,
                startval: fillWith
            });
            $(propertyWindow).append('<div><button id="form-submit">Submit</button>' +
                '<button id="form-cancel">Cancel</button></div>');

            document.getElementById('form-submit').addEventListener('click', function () {
                $("#container").removeClass('disabledbutton');
                $("#toolbox").removeClass('disabledbutton');
                $(propertyWindow).html('');
                $(propertyWindow).collapse('hide');
                var config = editor.getValue();

                //update selected stream model
                clickedElement.set('define', config.name);
                clickedElement.set('attributes', config.attributes);

                var textNode = $(element).parent().find('.streamnamenode');
                textNode.html(config.name);
            });

            //'Cancel' button action
            document.getElementById('form-cancel').addEventListener('click', function () {
                $("#container").removeClass('disabledbutton');
                $("#toolbox").removeClass('disabledbutton');
                $(propertyWindow).html('');
                $(propertyWindow).collapse('hide');
            });
        };

        /**
         * @function generate the property window for the simple queries ( passthrough, filter and window)
         * @param element selected element(query)
         */
        FormBuilder.prototype.GeneratePropertiesFormForQueries = function (element) {
            var propertyWindow = document.getElementsByClassName('property');
            $(propertyWindow).collapse('show');
            $("#container").addClass('disabledbutton');
            $("#toolbox").addClass('disabledbutton');
            var id = $(element).parent().attr('id');
            var clickedElement = queryList.get(id);
            var queryType = $(element).parent().attr('class');
            var type= $(element).parent();
            if (!(clickedElement.get('from'))) {
                alert('Connect an input stream');
                $("#container").removeClass('disabledbutton');
                $("#toolbox").removeClass('disabledbutton');
            }
            else if (!(clickedElement.get('insert-into'))) {
                //retrieve the query information from the collection
                var name = clickedElement.get('name');
                var inStream = (streamList.get(clickedElement.get('from'))).get('define');
                var filter1 = clickedElement.get('filter');
                var window = clickedElement.get('window');
                var filter2 = clickedElement.get('post-window-filter');
                var fillWith;
                if (type.hasClass(constants.PASS_THROUGH)) {
                    fillWith = {
                        name: name,
                        from: inStream,
                        projection: ''
                    };
                }
                else if (type.hasClass(constants.FILTER)) {
                    fillWith = {
                        name: name,
                        from: inStream,
                        filter: filter1,
                        projection: ''
                    };
                }
                else if (type.hasClass(constants.WINDOW_QUERY)) {
                    fillWith = {
                        name: name,
                        from: inStream,
                        filter: filter1,
                        window: window,
                        postWindowFilter: filter2,
                        projection: ''
                    };
                }
                //generate the form for the query an output stream is not connected
                var editor = new JSONEditor(document.getElementById('propertypane'), {
                    schema: {
                        type: 'object',
                        title: 'Query',
                        properties: {
                            name: {
                                type: 'string',
                                title: 'Name',
                                required: true,
                                propertyOrder: 1
                            },
                            from: {
                                type: 'string',
                                title: 'From',
                                required: true,
                                propertyOrder: 2
                            },
                            filter: {
                                type: 'string',
                                title: 'Filter',
                                propertyOrder: 3
                            },
                            window: {
                                type: 'string',
                                title: 'Window',
                                propertyOrder: 4
                            },
                            postWindowFilter: {
                                type: 'string',
                                title: 'Post Window Filter',
                                propertyOrder: 5
                            },
                            outputType: {
                                type: 'string',
                                title: 'Output Type',
                                enum : ['all events' , 'current events' , 'expired events'],
                                default: 'all events',
                                propertyOrder: 6,
                                required: true
                            },
                            insertInto: {
                                type: 'string',
                                title: 'Output Stream',
                                propertyOrder: 7,
                                required: true
                            },
                            projection: {
                                type: 'array',
                                title: 'Attributes',
                                format: 'table',
                                required: true,
                                propertyOrder: 8,
                                uniqueItems: true,
                                items: {
                                    type: 'object',
                                    title : 'Attribute',
                                    properties: {
                                        select: {
                                            type: 'string',
                                            title: 'select'
                                        },
                                        newName: {
                                            type: 'string',
                                            title: 'as'
                                        },
                                        type: {
                                            type: "string",
                                            enum: [
                                                "string",
                                                "int",
                                                "long",
                                                "float",
                                                "double",
                                                "boolean"
                                            ],
                                            default: "string"
                                        }
                                    }
                                }
                            }
                        }
                    },
                    startval: fillWith,
                    disable_array_add: false,
                    disable_array_delete: false,
                    disable_array_reorder: true,
                    display_required_only: true,
                    no_additional_properties: true
                });
                //disable the uneditable fields
                editor.getEditor('root.from').disable();

                $(propertyWindow).append('<div><button id="form-submit">Submit</button>' +
                    '<button id="form-cancel">Cancel</button></div>');

                //'Save' button action
                document.getElementById('form-submit').addEventListener('click', function () {
                    $("#container").removeClass('disabledbutton');
                    $("#toolbox").removeClass('disabledbutton');
                    $(propertyWindow).html('');
                    $(propertyWindow).collapse('hide');
                    var config = editor.getValue();

                    //change the query icon depending on the fileds filled

                    if (config.window) {
                        $(element).parent().removeClass();
                        $(element).parent().addClass(constants.WINDOW_QUERY + ' jtk-draggable');
                    }
                    else if (config.filter || config.postWindowFilter) {
                        $(element).parent().removeClass();
                        $(element).parent().addClass(constants.FILTER + ' jtk-draggable');
                    }
                    else if (!(config.filter || config.postWindowFilter || config.window )) {
                        $(element).parent().removeClass();
                        $(element).parent().addClass(constants.PASS_THROUGH+ ' jtk-draggable');
                    }
                    //obtain values from the form and update the query model
                    var config = editor.getValue();
                    clickedElement.set('name', config.name);
                    clickedElement.set('filter', config.filter);
                    clickedElement.set('window', config.window);
                    clickedElement.set('post-window-filter', config.postWindowFilter);
                    clickedElement.set('output-type', config.outputType);
                    var streamAttributes = [];
                    var queryAttributes = [];
                    $.each( config.projection, function(key, value ) {
                        streamAttributes.push({ attribute : value.newName , type : value.type});
                        queryAttributes.push(value.select);
                    });
                    clickedElement.set('projection', queryAttributes);
                    var textNode = $(element).parent().find('.queryNameNode');
                    textNode.html(config.name);
                    //generate the stream defined as the output stream
                    var position = $(element).parent().position();
                    dropStreamFromQuery(position, id, config.insertInto, streamAttributes);
                });

                //'Cancel' button action
                document.getElementById('form-cancel').addEventListener('click', function () {
                    $("#container").removeClass('disabledbutton');
                    $("#toolbox").removeClass('disabledbutton');
                    $(propertyWindow).html('');
                    $(propertyWindow).collapse('hide');
                });
            }
            else {
                //retrieve the query information from the collection
                var name = clickedElement.get('name');
                var inStream = (streamList.get(clickedElement.get('from'))).get('define');
                var outStream = (streamList.get(clickedElement.get('insert-into'))).get('define');
                var filter1 = clickedElement.get('filter');
                var window = clickedElement.get('window');
                var filter2 = clickedElement.get('post-window-filter');
                var outputType = clickedElement.get('output-type');
                var attrString = [];
                var outStreamAttributes = (streamList.get(clickedElement.get('insert-into'))).get('attributes');
                if (clickedElement.get('projection') == '') {
                    for (var i = 0; i < outStreamAttributes.length; i++) {
                        var attr = {select: '', newName: outStreamAttributes[i].attribute};
                        attrString.push(attr);
                    }
                }
                else {
                    var selectedAttributes = clickedElement.get('projection');
                    for (var i = 0; i < outStreamAttributes.length; i++) {
                        var attr = {select: selectedAttributes[i], newName: outStreamAttributes[i].attribute};
                        attrString.push(attr);
                    }
                }

                var fillWith;
                if (type.hasClass(constants.PASS_THROUGH)) {
                    fillWith = {
                        name: name,
                        from: inStream,
                        projection: attrString,
                        outputType :outputType,
                        insertInto: outStream
                    };
                }
                else if (type.hasClass(constants.FILTER)) {
                    fillWith = {
                        name: name,
                        from: inStream,
                        filter: filter1,
                        projection: attrString,
                        outputType :outputType,
                        insertInto: outStream
                    };
                }
                else if (type.hasClass(constants.WINDOW_QUERY)) {
                    fillWith = {
                        name: name,
                        from: inStream,
                        filter : filter1,
                        window: window,
                        pstWindowFilter : filter2,
                        projection: attrString,
                        outputType :outputType,
                        insertInto: outStream
                    };
                }
                //generate form for the queries where both input and output streams are defined
                var editor = new JSONEditor(document.getElementById('propertypane'), {
                    schema: {
                        type: 'object',
                        title: 'Query',
                        properties: {
                            name: {
                                type: 'string',
                                title: 'Name',
                                required: true,
                                propertyOrder: 1
                            },
                            from: {
                                type: 'string',
                                title: 'From',
                                template: inStream,
                                required: true,
                                propertyOrder: 2
                            },
                            filter: {
                                type: 'string',
                                title: 'Filter',
                                propertyOrder: 3
                            },
                            window: {
                                type: 'string',
                                title: 'Window',
                                propertyOrder: 4
                            },
                            postWindowQuery: {
                                type: 'string',
                                title: 'Post Window Filter',
                                propertyOrder: 5
                            },
                            projection: {
                                type: 'array',
                                title: 'Attributes',
                                format: 'table',
                                required: true,
                                propertyOrder: 6,
                                items: {
                                    type: 'object',
                                    title : 'Attribute',
                                    properties: {
                                        select: {
                                            type: 'string',
                                            title: 'select'
                                        },
                                        newName: {
                                            type: 'string',
                                            title: 'as'
                                        }
                                    }
                                }
                            },
                            outputType: {
                                type: 'string',
                                title: 'Output Type',
                                enum : ['all events' , 'current events' , 'expired events'],
                                default: 'all events',
                                required : true,
                                propertyOrder: 7
                            },
                            insertInto: {
                                type: 'string',
                                template: outStream,
                                required: true,
                                propertyOrder: 8
                            }
                        }
                    },
                    startval: fillWith,
                    disable_array_add: true,
                    disable_array_delete: true,
                    disable_array_reorder: true,
                    display_required_only: true,
                    no_additional_properties: true
                });

                //disable fields that can not be changed
                editor.getEditor('root.from').disable();
                editor.getEditor('root.insertInto').disable();
                for (var i = 0; i < outStreamAttributes.length; i++) {
                    editor.getEditor('root.projection.' + i + '.newName').disable();
                }

                $(propertyWindow).append('<div><button id="form-submit">Submit</button>' +
                    '<button id="form-cancel">Cancel</button></div>');

                document.getElementById('form-submit').addEventListener('click', function () {
                    $("#container").removeClass('disabledbutton');
                    $("#toolbox").removeClass('disabledbutton');
                    $(propertyWindow).html('');
                    $(propertyWindow).collapse('hide');
                    var config = editor.getValue();

                    //change the query icon depending on the fields(filter, window) filled
                    if (config.window) {
                        $(element).parent().removeClass();
                        $(element).parent().addClass(constants.WINDOW_QUERY + ' jtk-draggable');
                    }
                    else if (config.filter || config.postWindowFilter) {
                        $(element).parent().removeClass();
                        $(element).parent().addClass(constants.FILTER + ' jtk-draggable');
                    }
                    else if (!(config.filter || config.postWindowFilter || config.window )) {
                        $(element).parent().removeClass();
                        $(element).parent().addClass(constants.PASS_THROUGH + ' jtk-draggable');
                    }

                    //update selected query model
                    clickedElement.set('name', config.name);
                    clickedElement.set('filter', config.filter);
                    clickedElement.set('window', config.window);
                    clickedElement.set('post-window-filter', config.postWindowFilter);
                    var projections = [];
                    $.each(config.projection, function (index, attribute) {
                        projections.push(attribute.select);
                    });
                    clickedElement.set('projection', projections);
                    clickedElement.set('output-type', config.outputType);
                    var textNode = $(element).parent().find('.queryNameNode');
                    textNode.html(config.name);
                });

                //'Cancel' button action
                document.getElementById('form-cancel').addEventListener('click', function () {
                    $("#container").removeClass('disabledbutton');
                    $("#toolbox").removeClass('disabledbutton');
                    $(propertyWindow).html('');
                    $(propertyWindow).collapse('hide');
                });
            }
        };

        /**
         * @function generate property window for state machine
         * @param element
         */
        FormBuilder.prototype.GeneratePropertiesFormForPattern = function (element) {
            var propertyWindow = document.getElementsByClassName('property');
            $(propertyWindow).collapse('show');
            $("#container").addClass('disabledbutton');
            $("#toolbox").addClass('disabledbutton');
            var id = $(element).parent().attr('id');
            var clickedElement = patternList.get(id);
            if (clickedElement.get('from').length == 0) {
                alert('Connect input streams');
                $("#container").removeClass('disabledbutton');
                $("#toolbox").removeClass('disabledbutton');
            }
            else if (clickedElement.get('insert-into') == '') {
                alert('Connect an output stream');
                $("#container").removeClass('disabledbutton');
                $("#toolbox").removeClass('disabledbutton');
            }

            else {
                //retrieve the pattern information from the collection
                var streams = [];
                var projections = [];
                $.each(clickedElement.get('from'), function (index, streamID) {
                    streams.push((streamList.get(streamID)).get('define'));
                });

                var insertInto = streamList.get(clickedElement.get('insert-into')).get('define');
                var outStreamAttributes = (streamList.get(clickedElement.get('insert-into'))).get('attributes');
                if (clickedElement.get('projection') == '') {
                    for (var i = 0; i < outStreamAttributes.length; i++) {
                        var attr = {select: '', newName: outStreamAttributes[i].attribute};
                        projections.push(attr);
                    }
                }
                else {
                    var selectedAttributes = clickedElement.get('projection');
                    for (var i = 0; i < outStreamAttributes.length; i++) {
                        var attr = {select: selectedAttributes[i], newName: outStreamAttributes[i].attribute};
                        projections.push(attr);
                    }
                }
                var states = [];
                $.each(clickedElement.get('states'), function (index, state) {
                    for (var event in state) {
                        if (state.hasOwnProperty(event)) {
                            var restoredState = {stateID: event, stream: state[event].stream, filter: state[event].filter};
                            states.push(restoredState);
                        }
                    }
                });

                var fillWith = {
                    name: clickedElement.get('name'),
                    states: states,
                    logic: clickedElement.get('logic'),
                    outputType: clickedElement.get('output-type'),
                    projection: projections
                };
                if (clickedElement.get('filter')) {
                    fillWith.filter = clickedElement.get('filter');
                }
                if (clickedElement.get('window')) {
                    fillWith.window = clickedElement.get('window');
                }
                if (clickedElement.get('post-window-filter')) {
                    fillWith.postWindowFilter = clickedElement.get('post-window-filter');
                }
                if (clickedElement.get('having')) {
                    fillWith.having = clickedElement.get('having');
                }
                if (clickedElement.get('group-by')) {
                    fillWith.groupBy = clickedElement.get('group-by');
                }
                var editor = new JSONEditor(document.getElementById('propertypane'), {
                    ajax: true,
                    schema: {
                        type: 'object',
                        title: 'Query',
                        properties: {
                            name: {
                                type: 'string',
                                title: 'Name',
                                required: true,
                                propertyOrder: 1
                            },
                            states: {
                                type: 'array',
                                title: 'State',
                                format: 'tabs',
                                uniqueItems: true,
                                required: true,
                                propertyOrder: 2,
                                items: {
                                    type: 'object',
                                    title: 'state',
                                    headerTemplate: "State" + "{{i1}}",
                                    options: {
                                        disable_properties: true
                                    },
                                    properties: {
                                        stateID: {
                                            type: 'string',
                                            title: 'State ID',
                                            required: true,
                                            propertyOrder: 1
                                        },
                                        stream: {
                                            type: 'string',
                                            title: 'Stream',
                                            enum: streams,
                                            required: true,
                                            propertyOrder: 2
                                        },
                                        filter: {
                                            type: 'string',
                                            title: 'Filter',
                                            required: true,
                                            propertyOrder: 3
                                        }
                                    }
                                }
                            },
                            logic: {
                                type: 'string',
                                title: 'Logic',
                                required: true,
                                propertyOrder: 3
                            },
                            filter: {
                                type: 'string',
                                title: 'Filter',
                                propertyOrder: 4
                            },
                            window: {
                                type: 'string',
                                title: 'Window',
                                propertyOrder: 5
                            },
                            postWindowFilter: {
                                type: 'string',
                                title: 'Post Window Filter',
                                propertyOrder: 6
                            },
                            having: {
                                type: 'string',
                                title: 'Having',
                                propertyOrder: 7
                            },
                            groupBy: {
                                type: 'string',
                                title: 'Group By',
                                propertyOrder: 8
                            },
                            projection: {
                                type: 'array',
                                title: 'Projection',
                                format: 'table',
                                required: true,
                                propertyOrder: 9,
                                options: {
                                    disable_array_add: true,
                                    disable_array_delete: true
                                },
                                items: {
                                    type: 'object',
                                    title: 'Attribute',
                                    properties: {
                                        select: {
                                            type: 'string',
                                            title: 'select'
                                        },
                                        newName: {
                                            type: 'string',
                                            title: 'as'
                                        }
                                    }
                                }
                            },
                            outputType: {
                                type: 'string',
                                title: 'Output Type',
                                enum: ['all events', 'current events', 'expired events'],
                                default: 'all events',
                                required: true,
                                propertyOrder: 10
                            },
                            insertInto: {
                                type: 'string',
                                title: 'Insert Into',
                                template: insertInto,
                                required: true,
                                propertyOrder: 11
                            }
                        }
                    },
                    startval: fillWith,
                    disable_properties: false,
                    display_required_only: true,
                    no_additional_properties: true,
                    disable_array_delete_all_rows: true,
                    disable_array_delete_last_row: true,
                    disable_array_reorder: true
                });
                //disable fields that can not be changed
                editor.getEditor('root.insertInto').disable();

                for (var i = 0; i < outStreamAttributes.length; i++) {
                    editor.getEditor('root.projection.' + i + '.newName').disable();
                }
                $(propertyWindow).append('<div><button id="form-submit">Submit</button>' +
                    '<button id="form-cancel">Cancel</button></div>');

                document.getElementById('form-submit').addEventListener('click', function () {
                    $("#container").removeClass('disabledbutton');
                    $("#toolbox").removeClass('disabledbutton');
                    $(propertyWindow).html('');
                    $(propertyWindow).collapse('hide');
                    var config = editor.getValue();

                    //update selected query model
                    clickedElement.set('name', config.name);
                    clickedElement.set('logic', config.logic);
                    clickedElement.set('filter', config.filter);
                    clickedElement.set('window', config.window);
                    clickedElement.set('post-window-filter', config.postWindowFilter);
                    clickedElement.set('having', config.having);
                    clickedElement.set('group-by', config.groupBy);
                    var states = [];
                    $.each(config.states, function (index, state) {
                        var stateID = state.stateID;
                        var stream = state.stream;
                        var filter = state.filter;
                        var stateObject = {};
                        stateObject[stateID] = {stream: stream, filter: filter};
                        states.push(stateObject);
                    });
                    clickedElement.set('states', states);
                    var projections = [];
                    $.each(config.projection, function (index, attribute) {
                        projections.push(attribute.select);
                    });
                    clickedElement.set('projection', projections);
                    clickedElement.set('output-type', config.outputType);
                    var textNode = $(element).parent().find('.queryNameNode');
                    textNode.html(config.name);
                });

                //'Cancel' button action
                document.getElementById('form-cancel').addEventListener('click', function () {
                    $("#container").removeClass('disabledbutton');
                    $("#toolbox").removeClass('disabledbutton');
                    $(propertyWindow).html('');
                    $(propertyWindow).collapse('hide');
                });
            }
        };

        /**
         * @function generate property window for Join Query
         * @param element
         */
        FormBuilder.prototype.GeneratePropertiesFormForJoinQuery = function (element) {
            var propertyWindow = document.getElementsByClassName('property');
            $(propertyWindow).collapse('show');
            $("#container").addClass('disabledbutton');
            $("#toolbox").addClass('disabledbutton');
            var id = $(element).parent().attr('id');
            var clickedElement = joinQueryList.get(id);
            if (!(clickedElement.get('from')) || clickedElement.get('from').length != 2) {
                alert('Connect TWO input streams');
                $("#container").removeClass('disabledbutton');
                $("#toolbox").removeClass('disabledbutton');
            }
            else if (!(clickedElement.get('insert-into'))) {
                alert('Connect an output stream');
                $("#container").removeClass('disabledbutton');
                $("#toolbox").removeClass('disabledbutton');
            }

            else {
                var streams = [];
                $.each(clickedElement.get('from'), function (index, streamID) {
                    streams.push((streamList.get(streamID)).get('define'));
                });
                var projections = [];
                var insertInto = streamList.get(clickedElement.get('insert-into')).get('define');
                var outStreamAttributes = (streamList.get(clickedElement.get('insert-into'))).get('attributes');
                if (!(clickedElement.get('projection'))) {
                    for (var i = 0; i < outStreamAttributes.length; i++) {
                        var attr = {select: '', newName: outStreamAttributes[i].attribute};
                        projections.push(attr);
                    }
                }
                else {
                    var selectedAttributes = clickedElement.get('projection');
                    for (var i = 0; i < outStreamAttributes.length; i++) {
                        var attr = {select: selectedAttributes[i], newName: outStreamAttributes[i].attribute};
                        projections.push(attr);
                    }
                }
                var fillWith = {
                    projection: projections,
                    outputType: clickedElement.get('output-type'),
                    insertInto: insertInto
                };

                if (clickedElement.get('join')) {
                    fillWith.type = clickedElement.get('join').type;
                    fillWith.leftStream = clickedElement.get('join')['left-stream'];
                    fillWith.rightStream = clickedElement.get('join')['right-stream'];
                    fillWith.on = clickedElement.get('join').on;
                }
                var editor = new JSONEditor(document.getElementById('propertypane'), {
                    ajax: true,
                    schema: {
                        type: 'object',
                        title: 'Query',
                        properties: {
                            type: {
                                type: 'string',
                                title: 'Type',
                                required: true,
                                propertyOrder: 1
                            },
                            leftStream: {
                                type: 'object',
                                title: 'Left Stream',
                                required: true,
                                propertyOrder: 2,
                                options: {
                                    disable_collapse: false,
                                    disable_properties: true,
                                    collapsed: true
                                },
                                properties: {
                                    from: {
                                        type: 'string',
                                        title: 'From',
                                        enum: streams,
                                        required: true
                                    },
                                    filter: {
                                        type: 'string',
                                        title: 'Filter',
                                        required: true
                                    },
                                    window: {
                                        type: 'string',
                                        title: 'Window',
                                        required: true
                                    },
                                    postWindowFilter: {
                                        type: 'string',
                                        title: 'Post Window Filter',
                                        required: true
                                    },
                                    as: {
                                        type: 'string',
                                        title: 'As',
                                        required: true
                                    }
                                }
                            },
                            rightStream: {
                                type: 'object',
                                title: 'Right Stream',
                                required: true,
                                propertyOrder: 3,
                                options: {
                                    disable_collapse: false,
                                    disable_properties: true,
                                    collapsed: true
                                },
                                properties: {
                                    from: {
                                        type: 'string',
                                        title: 'From',
                                        enum: streams,
                                        required: true
                                    },
                                    filter: {
                                        type: 'string',
                                        title: 'Filter',
                                        required: true
                                    },
                                    window: {
                                        type: 'string',
                                        title: 'Window',
                                        required: true
                                    },
                                    postWindowFilter: {
                                        type: 'string',
                                        title: 'Post Window Filter',
                                        required: true
                                    },
                                    as: {
                                        type: 'string',
                                        title: 'As',
                                        required: true
                                    }
                                }
                            },
                            on: {
                                type: 'string',
                                title: 'On',
                                required: true,
                                propertyOrder: 4
                            },
                            projection: {
                                type: 'array',
                                title: 'Projection',
                                format: 'table',
                                required: true,
                                propertyOrder: 5,
                                options: {
                                    disable_array_add: true,
                                    disable_array_delete: true,
                                    disable_array_reorder: true
                                },
                                items: {
                                    type: 'object',
                                    title: 'Attribute',
                                    properties: {
                                        select: {
                                            type: 'string',
                                            title: 'select'
                                        },
                                        newName: {
                                            type: 'string',
                                            title: 'as'
                                        }
                                    }
                                }
                            },
                            outputType: {
                                type: 'string',
                                title: 'Output Type',
                                enum: ['all events', 'current events', 'expired events'],
                                default: 'all events',
                                required: true,
                                propertyOrder: 6
                            },
                            insertInto: {
                                type: 'string',
                                title: 'Insert Into',
                                template: insertInto,
                                required: true,
                                propertyOrder: 7
                            }
                        }
                    },
                    startval: fillWith,
                    no_additional_properties: true,
                    disable_properties: true
                });
                for (var i = 0; i < outStreamAttributes.length; i++) {
                    editor.getEditor('root.projection.' + i + '.newName').disable();
                }
                editor.getEditor('root.insertInto').disable();
                $(propertyWindow).append('<div><button id="form-submit">Submit</button>' +
                    '<button id="form-cancel">Cancel</button></div>');

                document.getElementById('form-submit').addEventListener('click', function () {
                    $("#container").removeClass('disabledbutton');
                    $("#toolbox").removeClass('disabledbutton');
                    $(propertyWindow).html('');
                    $(propertyWindow).collapse('hide');

                    var config = editor.getValue();
                    //update selected query model
                    var leftStream = {
                        from: config.leftStream.from,
                        filter: config.leftStream.filter,
                        window: config.leftStream.window,
                        postWindowFilter: config.leftStream.postWindowFilter,
                        as: config.leftStream.as
                    };
                    var rightStream = {
                        from: config.rightStream.from,
                        filter: config.rightStream.filter,
                        window: config.rightStream.window,
                        postWindowFilter: config.rightStream.postWindowFilter,
                        as: config.rightStream.as
                    };
                    var join = {'type': config.type, 'left-stream': leftStream, 'right-stream': rightStream, 'on': config.on};
                    clickedElement.set('join', join);
                    var projections = [];
                    $.each(config.projection, function (index, attribute) {
                        projections.push(attribute.select);
                    });
                    clickedElement.set('projection', projections);
                    clickedElement.set('output-type', config.outputType);
                });

                //'Cancel' button action
                document.getElementById('form-cancel').addEventListener('click', function () {
                    $("#container").removeClass('disabledbutton');
                    $("#toolbox").removeClass('disabledbutton');
                    $(propertyWindow).html('');
                    $(propertyWindow).collapse('hide');
                });
            }
        };

        FormBuilder.prototype.GeneratePartitionKeyForm = function (element) {
            var id = $(element.target).parent().attr('id');
            var partition = partitionList.get(id);
            var connections = jsPlumb.getConnections(element);
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
                $("#container").removeClass('disabledbutton');
                $("#toolbox").removeClass('disabledbutton');
            }
            else{
                var fillWith= {};
                var partitionKeys = partition.get('partition').with;
                $.each(partitionKeys, function ( index , key) {
                    if( key.stream == connectedStream){
                        fillWith ={
                            stream : (streamList.get(connectedStream)).get('define'),
                            property : key.property
                        }
                    }
                });
                var propertyWindow = document.getElementsByClassName('property');
                $(propertyWindow).collapse('show');
                $("#container").addClass('disabledbutton');
                $("#toolbox").addClass('disabledbutton');
                var editor = new JSONEditor(document.getElementById('propertypane'), {
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
                                template: (streamList.get(connectedStream)).get('define')
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
                $(propertyWindow).append('<div>'+
                    '<button id="form-submit">Submit</button>' +
                    '<button id="form-cancel">Cancel</button></div>');

                document.getElementById('form-submit').addEventListener('click', function () {
                    $("#container").removeClass('disabledbutton');
                    $("#toolbox").removeClass('disabledbutton');
                    $(propertyWindow).html('');
                    $(propertyWindow).collapse('hide');
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
                //'Cancel' button action
                document.getElementById('form-cancel').addEventListener('click', function () {
                    $("#container").removeClass('disabledbutton');
                    $("#toolbox").removeClass('disabledbutton');
                    $(propertyWindow).html('');
                    $(propertyWindow).collapse('hide');
                });
            }
        };

        return FormBuilder;
    });
