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

define(['require', 'log', 'jquery', 'lodash', 'attribute', 'stream', 'designViewUtils', 'jsonValidator', 'handlebar',
    'js_tree'],
    function (require, log, $, _, Attribute, Stream, DesignViewUtils, JSONValidator, Handlebars, jstree) {

        /**
         * @class StreamForm Creates a forms to collect data from a stream
         * @constructor
         * @param {Object} options Rendering options for the view
         */
        var StreamForm = function (options) {
            if (options !== undefined) {
                this.configurationData = options.configurationData;
                this.application = options.application;
                this.formUtils = options.formUtils;
                this.jsPlumbInstance = options.jsPlumbInstance;
                this.consoleListManager = options.application.outputController;
                var currentTabId = this.application.tabController.activeTab.cid;
                this.designViewContainer = $('#design-container-' + currentTabId);
                this.toggleViewButton = $('#toggle-view-button-' + currentTabId);
            }
        };

        //attribute delete button action
        var delAttribute = function () {
            $(this).closest('li').remove();
            changeAtrributeNavigation();
        };

        //attribute add button action
        var addAttribute = function () {
            $("#attribute-div").append('<li class="attribute"><div class="attr-content">' +
                '<input type="text"  class="attr-name"/> &nbsp; ' +
                '<select class="attr-type">' +
                '<option value="string">string</option>' +
                '<option value="int">int</option>' +
                '<option value="long">long</option>' +
                '<option value="float">float</option>' +
                '<option value="double">double</option>' +
                '<option value="bool">bool</option>' +
                '<option value="object">object</option>' +
                '</select>' +
                '</div> <div class="attr-nav"> </div></li>');
            changeAtrributeNavigation();
        };

        //attribute move up button action
        var moveUpAttribute = function () {
            var $current = $(this).closest('li')
            var $previous = $current.prev('li');
            if ($previous.length !== 0) {
                $current.insertBefore($previous);
            }
            changeAtrributeNavigation();
            return false;
        };

        //attribute move down function
        var moveDownAttribute = function () {
            var $current = $(this).closest('li')
            var $next = $current.next('li');
            if ($next.length !== 0) {
                $current.insertAfter($next);
            }
            changeAtrributeNavigation();
            return false;
        };

        var isPredefinedAnnotation = function (annotationName) {

            var predefinedAnnotationList = loadPredefinedAnnotations();
            var predefinedObject = null;
            _.forEach(predefinedAnnotationList, function (predefinedAnnotation) {
                if (predefinedAnnotation.name == annotationName) {
                    predefinedObject = predefinedAnnotation
                    return;
                }
            });
            return predefinedObject;
        };

        var validatePredefinedAnnotations = function () {

            var predefinedAnnotationjsTreeList = $('#annotation-div').jstree('get_checked');
            var predefinedAnnotationList = loadPredefinedAnnotations();
            var isValid = false;
            mainLoop:
            for (var jsTreePredefinedAnnotation of predefinedAnnotationjsTreeList) {
                var node_info = $('#annotation-div').jstree("get_node", jsTreePredefinedAnnotation);
                var predefinedObject = isPredefinedAnnotation(node_info.text.trim())
                if (predefinedObject != null) {
                    for (var jsTreePredefinedAnnotationElement of node_info.children) {
                        var annotation_key_info = $('#annotation-div').jstree("get_node",
                            jsTreePredefinedAnnotationElement);
                        var annotation_value_info = $('#annotation-div').jstree("get_node", annotation_key_info
                            .children[0])
                        for (var predefinedObjectElement of predefinedObject.elements) {
                            if (annotation_key_info.text.trim() == predefinedObjectElement.key) {
                                if (predefinedObjectElement.ismandaory == "true") {
                                    if (annotation_value_info.text.trim() == "") {
                                        DesignViewUtils.prototype.errorAlert("Propery '" + predefinedObjectElement.key +
                                            "' is mandaory");
                                        isValid = true;
                                        break mainLoop;
                                    }
                                }
                            }
                        }

                    }
                }

            }
            return isValid;
        };

        var checkPredefinedAnnotations = function (checkedAnnotations) {

            _.forEach(checkedAnnotations, function (annotationName) {
                $('#annotation-div').jstree("search", annotationName)
            });

        };

        var annotation = "";
        var buildAnnotationString = function () {

            var annotationList = [];
            var parentAnnotations = $('#annotation-div').jstree(true)._model.data['#'].children;
            var returned = false;
            parentAnnotations.forEach(function (node) {
                var node_info = $('#annotation-div').jstree("get_node", node);
                var childArray = node_info.children
                if (childArray.length != 0) {
                    annotation += "@" + node_info.text.trim() + "( "
                    traverseChildAnnotations(childArray)
                    annotation = annotation.substring(0, annotation.length - 1);
                    annotation += ")"
                    annotationList.push(annotation);
                    annotation = "";
                }
            });
            if (returned) {
                annotation = "";
                return;
            }
            return annotationList;
        };

        var traverseChildAnnotations = function (childAnnotations) {

            childAnnotations.forEach(function (node) {

                node_info = $('#annotation-div').jstree("get_node", node);
                if ((node_info.original != null && node_info.original.class == "annotation") ||
                    (node_info.li_attr != null && node_info.li_attr.class == "annotation")) {
                    if (node_info.children.length != 0) {
                        annotation += "@" + node_info.text.trim() + "( "
                        traverseChildAnnotations(node_info.children)
                        annotation = annotation.substring(0, annotation.length - 1);
                        annotation += "),"

                    }
                }
                else {
                    //do the validation if the value is empty dont append the key and value
                    annotation += node_info.text.trim() + "="
                    var node_value = $('#annotation-div').jstree("get_node", node_info.children[0]).text.trim();
                    annotation += "'" + node_value + "' ,"
                }
            });
        };

        var loadPredefinedAnnotations = function () {
            var predefinedAnnotationList = [];
            var asyncAnnotationObject = {
                name: "Async", class: "predefined", ismandaory: "false",
                elements: [{ key: "buffer.size", value: "256", ismandaory: "true" }, {
                    key: "workers", value: "2", ismandaory: "true"
                },
                { key: "batch.size.max", value: "5", ismandaory: "true" }]
            };
            predefinedAnnotationList.push(asyncAnnotationObject);
            return predefinedAnnotationList;
        };

        var loadAnnotation = function () {
            //initialise jstree
            $("#annotation-div").jstree({
                "core": {
                    "check_callback": true
                },
                "themes": {
                    "theme": "default",
                    "url": "editor/commons/lib/js-tree-v3.3.2/themes/default7777777/style.css"
                },
                "checkbox": {
                    "three_state": false,
                    "whole_node": false,//Used to check/uncheck node only if clicked on checkbox icon, and not on the whole node including label
                    "tie_selection": false
                },
                "search": {
                    "case_insensitive": true
                },

                "plugins": ["themes", "checkbox", "search"]

            }).on('search.jstree', function (nodes, data, res) {

                $("#annotation-div").jstree(true).check_node(data.res[0])
            })
            var tree = $('#annotation-div').jstree(true);
            //to add key-value for annotation node
            $("#btn-add-key-val").on("click", function () {
                var selectedNode = $("#annotation-div").jstree("get_selected");
                var node_info = $('#annotation-div').jstree("get_node", selectedNode[0]);

                //                if ((node_info.original != null && node_info.original.class == "annotation") ||
                //                    (node_info.data != null && (node_info.data.jstree.class == "annotation") )) {
                tree.create_node(selectedNode,
                    {
                        text: "property", class: "annotation-key", state: { "opened": true },
                        "a_attr": { "class": "annotation-key" }, icon: "/editor/commons/images/properties.png",
                        children: [{
                            text: "value", class: "annotation-value", "a_attr": { "class": "annotation-value" },
                            icon: "/editor/commons/images/value.png"
                        }]
                    }
                );

                tree.deselect_all();
            });

            //to add annotation node
            $("#btn-add-annotation").on("click", function () {
                var selectedNode = $("#annotation-div").jstree("get_selected");
                if (selectedNode == "") {
                    selectedNode = "#"
                }
                tree.create_node(selectedNode, {
                    text: "Annotation", class: "annotation", state: { "opened": true },
                    "a_attr": { "class": "annotation" }, icon: "/editor/commons/images/annotation.png",
                    children: [{
                        text: "property", class: "annotation-key", icon: "/editor/commons/images/properties.png",
                        "a_attr": { "class": "annotation-key" },
                        children: [{
                            text: "value", class: "annotation-value", "a_attr": { "class": "annotation-value" },
                            icon: "/editor/commons/images/value.png"
                        }]
                    }]

                });
                tree.deselect_all();
            });

            //to delete a annotation or a key-value node
            $("#btn-del-annotation").on("click", function () {
                var selectedNode = $("#annotation-div").jstree("get_selected");
                var node_info = $('#annotation-div').jstree("get_node", selectedNode[0]);

                //                if ((node_info.original != null && node_info.original.class != "annotation-value") ||
                //                    (node_info.data != null && node_info.data.jstree.class != "annotation-value")) {
                tree.delete_node([selectedNode]);
                //      }
                tree.deselect_all();
            })

            //to edit the selected node
            $('#annotation-div').on("select_node.jstree", function (e, data) {
                var node_info = $('#annotation-div').jstree("get_node", data.node)
                console.log(node_info)
                if (node_info.li_attr != null && (node_info.li_attr.class == "predefined-annotation" ||
                    node_info.li_attr.class == "predefined-key")) {
                    // tree.edit(data.node);
                    //					$("#btn-del-annotation").show();
                    //					$("#btn-add-annotation").hide();
                    //					$("#btn-add-key-val").hide();

                }
                else if ((node_info.original != null && (node_info.original.class == "annotation-key")) ||
                    (node_info.li_attr != null && (node_info.li_attr.class == "annotation-key"))) {
                    tree.edit(data.node);
                    $("#btn-del-annotation").show();
                    $("#btn-add-annotation").hide();
                    $("#btn-add-key-val").hide();
                }
                else if ((node_info.original != null && (node_info.original.class == "annotation-value")) ||
                    (node_info.li_attr != null && (node_info.li_attr.class == "annotation-value"))) {
                    $("#btn-del-annotation").hide();
                    $("#btn-add-annotation").hide();
                    $("#btn-add-key-val").hide();
                    tree.edit(data.node);
                }
                else {
                    $("#btn-del-annotation").show();
                    $("#btn-add-annotation").show();
                    $("#btn-add-key-val").show();
                    tree.edit(data.node);
                }
            });

            $(document).on('click', function (e) {
                if ($(e.target).closest('.jstree').length) {
                    $("#btn-del-annotation").hide();
                    $("#btn-add-annotation").show();
                    $("#btn-add-key-val").hide();
                    tree.deselect_all();
                }
            });

        };

        //to manage the attribute navigations
        var changeAtrributeNavigation = function () {
            $('.attr-nav').empty();
            var attrLength = $('#attribute-div li').length;
            if (attrLength == 1) {
                $('.attribute:eq(0)').find('.attr-nav').empty();
            }
            if (attrLength == 2) {
                $('.attribute:eq(0)').find('.attr-nav').append('<button type="button" class="reorder-down">' +
                    '<img src="/editor/commons/images/down.png"></button>');
                $('.attribute:eq(1)').find('.attr-nav').append('<button type="button" class="reorder-up">' +
                    '<img src="/editor/commons/images/up.png"></button><button type="button"' +
                    'class="btn-del-attr"><img src="/editor/commons/images/delete.png"></button>');
            }
            if (attrLength > 2) {
                var lastIndex = attrLength - 1;
                for (var i = 0; i < attrLength; i++) {
                    $('.attribute:eq(' + i + ')').find('.attr-nav').append('<button type="button"' +
                        'class="reorder-up"><img src="/editor/commons/images/up.png"></button>' +
                        '<button type="button" class="reorder-down"><img src="/editor/commons/images/down.png">' +
                        '</button> <button type="button" class="btn-del-attr">' +
                        '<img src="/editor/commons/images/delete.png"></button>');
                }
                $('.attribute:eq(0)').find('.attr-nav button:eq(0)').remove();
                $('.attribute:eq(0)').find('.attr-nav button:eq(1)').remove();
                $('.attribute:eq(' + lastIndex + ')').find('.attr-nav button:eq(1)').remove();
            }
        };

        /**
         * @function generate form when defining a form
         * @param i id for the element
         * @param formConsole Console which holds the form
         * @param formContainer Container which holds the form
         */
        StreamForm.prototype.generateDefineForm = function (i, formConsole, formContainer) {
            var self = this;
            var propertyDiv = $('<div id="property-header"><h3>Stream Configuration</h3></div>' +
                '<div id="stream-form"> <h3>Name: </h3> <input type="text" id="streamName"> <div ' +
                'id="define-attribute"></div><div id="define-annotation"></div>' +
                '<button id="submit" type="button" class="btn btn-default">Submit </button></div>');
            formContainer.append(propertyDiv);
            self.designViewContainer.addClass('disableContainer');
            self.toggleViewButton.addClass('disableContainer');


            var attributeFormTemplate = Handlebars.compile($('#attribute-form-template').html());
            var wrappedHtml = attributeFormTemplate("");
            $('#define-attribute').html(wrappedHtml);

            var raw_partial = document.getElementById('recursiveAnnotationPartial').innerHTML;
            Handlebars.registerPartial('recursiveAnnotation', raw_partial);
            var predefinedAnnotations = loadPredefinedAnnotations();

            var annotationFormTemplate = Handlebars.compile($('#annotation-form-template').html());
            var wrappedHtml = annotationFormTemplate(predefinedAnnotations);
            $('#define-annotation').html(wrappedHtml);

            //add event listeners for the buttons
            $("#btn-add-attribute").click(addAttribute)
            $("#attribute-div").on('click', '.btn-del-attr', delAttribute)
            $("#attribute-div").on('click', '.reorder-up', moveUpAttribute)
            $("#attribute-div").on('click', '.reorder-down', moveDownAttribute)
            //onload of the attribute div arrange the navigations of the attribute
            $('#attribute-div').ready(changeAtrributeNavigation);
            $('#stream-form-template').ready(loadAnnotation);

            var streamName = "";
            // 'Submit' button action
            $('#submit').on('click', function () {

                streamName = $('#streamName').val();

                //to check if stream name is already existing
                var isStreamNameUsed = self.formUtils.isDefinitionElementNameUsed(streamName);
                if (isStreamNameUsed) {
                    DesignViewUtils.prototype.errorAlert("Stream name \"" + streamName + "\" is already used.");
                    return;
                }
                // to check if stream name is empty
                if (streamName == "") {
                    DesignViewUtils.prototype.errorAlert("Stream name is required");
                    return;
                }
                //to check if stream name contains white spaces
                if ((streamName.indexOf(' ') >= 0) && (streamName.indexOf(' ') != streamName.length - 1)) {
                    DesignViewUtils.prototype.errorAlert("Stream name \"" + streamName + "\" " +
                        "cannot have white space.");
                    return;
                }
                //to check if stream name starts with an alphabetic character
                if (!(/^([a-zA-Z])$/).test(streamName.charAt(0))) {
                    DesignViewUtils.prototype.errorAlert("Stream name \"" + streamName + "\" " +
                        "must start with an alphabetic character.");
                    return;
                }

                //validate the attribute names
                var attrError = false;
                $('.attr-name').each(function () {
                    var attributeName = $(this).val();
                    if (attributeName != "") {
                        if ((attributeName.indexOf(' ') >= 0) && (attributeName.indexOf(' ') != attributeName.length - 1)) {
                            DesignViewUtils.prototype.errorAlert("Attribute name \"" + attributeName + "\" " +
                                "cannot have white space.");
                            attrError = true;
                            return;
                        }
                        if ((!(/^([a-zA-Z])$/).test(attributeName.charAt(0))) && (attributeName.trim().length > 0)) {
                            DesignViewUtils.prototype.errorAlert("Attribute name \"" + attributeName + "\" " +
                                "must start with an alphabetic character.");
                            attrError = true;
                            return;
                        }
                    }
                });
                if (attrError) {
                    return;
                }

                // set the isDesignViewContentChanged to true
                self.configurationData.setIsDesignViewContentChanged(true);

                //add the new out stream to the stream array
                var streamOptions = {};
                var attributeLength = 0;
                _.set(streamOptions, 'id', i);
                _.set(streamOptions, 'name', $('#streamName').val());
                var stream = new Stream(streamOptions);
                $('.attribute .attr-content').each(function () {
                    var nameValue = $(this).find('.attr-name').val().trim();
                    var typeValue = $(this).find('.attr-type').val();
                    if (nameValue != "") {
                        attributeLength++;
                        var attributeObject = new Attribute({ name: nameValue, type: typeValue });
                        stream.addAttribute(attributeObject);
                    }
                });
                if (attributeLength == 0) {
                    DesignViewUtils.prototype.errorAlert("Minimum one attribute is required");
                    return;
                }

                if (validatePredefinedAnnotations()) {
                    return;
                }

                var annotationList = buildAnnotationString();
                annotationList.forEach(function (annotation) {
                    stream.addAnnotation(annotation);
                });

                self.configurationData.getSiddhiAppConfig().addStream(stream);

                var textNode = $('#' + i).find('.streamNameNode');
                textNode.html(streamName);

                // If this is an inner stream perform validation
                var streamSavedInsideAPartition
                    = self.configurationData.getSiddhiAppConfig().getStreamSavedInsideAPartition(i);
                // if streamSavedInsideAPartition is undefined then the stream is not inside a partition
                if (streamSavedInsideAPartition !== undefined) {
                    JSONValidator.prototype.validateInnerStream(stream, self.jsPlumbInstance, true);
                }

                // close the form window
                self.consoleListManager.removeFormConsole(formConsole);

                self.designViewContainer.removeClass('disableContainer');
                self.toggleViewButton.removeClass('disableContainer');

            }); //closure of the submit button
            return streamName;
        };

        /**
         * @function generate properties form for a stream
         * @param element selected element(stream)
         * @param formConsole Console which holds the form
         * @param formContainer Container which holds the form
         */
        StreamForm.prototype.generatePropertiesForm = function (element, formConsole, formContainer) {

            var self = this;
            var propertyDiv = $('<div id="property-header"><h3>Stream Configuration</h3></div>' +
                '<div id="stream-form"> <h3>Name: </h3> <input type="text" id="streamName"> <div ' +
                'id="define-attribute"></div><div id="define-annotation"></div>' +
                '<button id="submit" type="button" class="btn btn-default">Submit </button></div>');
            formContainer.append(propertyDiv);
            self.designViewContainer.addClass('disableContainer');
            self.toggleViewButton.addClass('disableContainer');

            var id = $(element).parent().attr('id');
            // retrieve the stream information from the collection
            var clickedElement = self.configurationData.getSiddhiAppConfig().getStream(id);
            if (!clickedElement) {
                var errorMessage = 'unable to find clicked element';
                log.error(errorMessage);
                throw errorMessage;
            }
            var name = clickedElement.getName();
            var savedAttributes = clickedElement.getAttributeList();
            var attributes = [];
            _.forEach(savedAttributes, function (savedAttribute) {
                var attributeObject = {
                    name: savedAttribute.getName(),
                    type: (savedAttribute.getType()).toLowerCase()
                };
                attributes.push(attributeObject);
            });

            var savedAnnotations = clickedElement.getAnnotationListObjects();
            var annotations = [];
            var checkedAnnotations = [];
            var predefinedAnnotationList = loadPredefinedAnnotations();
            _.forEach(predefinedAnnotationList, function (predefinedAnnotation) {
                var foundPredefined = false;
                _.forEach(savedAnnotations, function (savedAnnotation) {
                    if (savedAnnotation.name == predefinedAnnotation.name) {
                        checkedAnnotations.push(savedAnnotation.name);
                        foundPredefined = true;
                        //predefinedAnnotation.class = "jstree-checked"
                        //put a tick for the annotation
                        _.forEach(predefinedAnnotation.elements, function (predefinedAnnotationElement) {
                            _.forEach(savedAnnotation.elements, function (savedAnnotationElement) {
                                if (predefinedAnnotationElement.key == savedAnnotationElement.key) {
                                    predefinedAnnotationElement.value = savedAnnotationElement.value

                                }
                            })
                        })

                        annotations.push(predefinedAnnotation)
                    }
                    else {

                        annotations.push(savedAnnotation)
                    }
                });

                if (!foundPredefined)
                    annotations.push(predefinedAnnotation)
            });

            $('#streamName').val(name);
            var attributeFormTemplate = Handlebars.compile($('#attribute-form-template').html());
            var wrappedHtml = attributeFormTemplate(attributes);
            $('#define-attribute').html(wrappedHtml);

            var raw_partial = document.getElementById('recursiveAnnotationPartial').innerHTML;
            Handlebars.registerPartial('recursiveAnnotation', raw_partial);
            var annotationFormTemplate = Handlebars.compile($('#annotation-form-template').html());
            var wrappedHtml = annotationFormTemplate(annotations);
            $('#define-annotation').html(wrappedHtml);
            loadAnnotation();
            checkPredefinedAnnotations(checkedAnnotations);
            validatePredefinedAnnotations();

            $("#btn-add-attribute").click(addAttribute)
            $("#attribute-div").on('click', '.btn-del-attr', delAttribute)
            $("#attribute-div").on('click', '.reorder-up', moveUpAttribute)
            $("#attribute-div").on('click', '.reorder-down', moveDownAttribute)
            //onload of the attribute div arrange the navigations of the attribute
            $('#attribute-div').ready(changeAtrributeNavigation);
            //$('#stream-form-template').ready(loadAnnotation(json));
            //formContainer.append(self.formUtils.buildFormButtons(true));

            $('#submit').on('click', function () {
                // set the isDesignViewContentChanged to true
                self.configurationData.setIsDesignViewContentChanged(true);

                var configName = $('#streamName').val();
                var streamName;
                var firstCharacterInStreamName;
                var isStreamNameUsed;
                /*
                * check whether the stream is inside a partition and if yes check whether it begins with '#'. If not add
                * '#' to the beginning of the stream name.
                * */
                var isStreamSavedInsideAPartition
                    = self.configurationData.getSiddhiAppConfig().getStreamSavedInsideAPartition(id);
                if (!isStreamSavedInsideAPartition) {
                    firstCharacterInStreamName = (configName).charAt(0);
                    if (firstCharacterInStreamName === '#') {
                        DesignViewUtils.prototype.errorAlert("'#' is used to define inner streams only.");
                        return;
                    } else {
                        streamName = configName;
                    }
                    isStreamNameUsed
                        = self.formUtils.isDefinitionElementNameUsed(streamName, id);
                    if (isStreamNameUsed) {
                        DesignViewUtils.prototype.errorAlert("Stream name \"" + streamName + "\" is already defined.");
                        return;
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
                        DesignViewUtils.prototype
                            .errorAlert("Stream name \"" + streamName + "\" is already defined in the partition.");
                        return;
                    }
                }

                var previouslySavedName = clickedElement.getName();
                // update connection related to the element if the name is changed
                if (previouslySavedName !== streamName) {
                    //check if stream name is empty
                    if (streamName == "") {
                        DesignViewUtils.prototype
                            .errorAlert("Stream name is required");
                        return;
                    }
                    if ((streamName.indexOf(' ') >= 0) && (streamName.indexOf(' ') != streamName.length - 1)) {
                        DesignViewUtils.prototype.errorAlert("Stream name \"" + streamName + "\" " +
                            "cannot have white space.");
                        return;
                    }
                    if (!(/^([a-zA-Z])$/).test(streamName.charAt(0))) {
                        DesignViewUtils.prototype.errorAlert("Stream name \"" + streamName + "\" " +
                            "must start with an alphabetic character.");
                        return;
                    }
                    // update selected stream model
                    clickedElement.setName(streamName);
                    self.formUtils.updateConnectionsAfterDefinitionElementNameChange(id);
                }
                // removing all elements from attribute list
                clickedElement.clearAttributeList();

                //validate the attribute names
                $('.attr-name').each(function () {
                    var attributeName = $(this).val().trim();
                    if (attributeName != "") {
                        if ((attributeName.indexOf(' ') >= 0) && (attributeName.indexOf(' ') != attributeName.length - 1)) {
                            DesignViewUtils.prototype.errorAlert("Attribute name \"" + attributeName + "\" " +
                                "cannot have white space.");
                            return;
                        }

                        if ((!(/^([a-zA-Z])$/).test(attributeName.charAt(0))) && (attributeName.trim().length > 0)) {
                            DesignViewUtils.prototype.errorAlert("Attribute name \"" + attributeName + "\" " +
                                "must start with an alphabetic character.");
                            return;
                        }
                    }
                });

                var attributeLength = 0;
                $('.attribute .attr-content').each(function () {
                    var nameValue = $(this).find('.attr-name').val().trim();
                    var typeValue = $(this).find('.attr-type').val();
                    if (nameValue != "") {
                        attributeLength++;
                        var attributeObject = new Attribute({ name: nameValue, type: typeValue });
                        clickedElement.addAttribute(attributeObject);
                    }
                });
                if (attributeLength == 0) {
                    DesignViewUtils.prototype.errorAlert("Minimum one attribute is required");
                    return;
                }

                clickedElement.clearAnnotationList();
                if (validatePredefinedAnnotations()) {
                    return;
                }
                var annotationList = buildAnnotationString();
                annotationList.forEach(function (annotation) {
                    clickedElement.addAnnotation(annotation);
                });

                var textNode = $(element).parent().find('.streamNameNode');
                textNode.html(streamName);

                // If this is an inner stream perform validation
                var streamSavedInsideAPartition
                    = self.configurationData.getSiddhiAppConfig().getStreamSavedInsideAPartition(id);
                // if streamSavedInsideAPartition is undefined then the stream is not inside a partition
                if (streamSavedInsideAPartition !== undefined) {
                    JSONValidator.prototype.validateInnerStream(clickedElement, self.jsPlumbInstance, true);
                }

                self.designViewContainer.removeClass('disableContainer');
                self.toggleViewButton.removeClass('disableContainer');
                // close the form window
                self.consoleListManager.removeFormConsole(formConsole);

            });

        };
        return StreamForm;
    });
