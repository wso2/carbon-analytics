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
    'js_tree', 'annotationObject', 'annotationElement'],
    function (require, log, $, _, Attribute, Stream, DesignViewUtils, JSONValidator, Handlebars, jstree,
        AnnotationObject, AnnotationElement) {

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

        const alphabeticValidatorRegex = /^([a-zA-Z])$/;

        /** attribute delete button action */
        var delAttribute = function () {
            $(this).closest('li').remove();
            changeAtrributeNavigation();
        };

        /** attribute add button action */
        var addAttribute = function () {
            $("#attribute-div").append('<li class="attribute clearfix"><div class="clearfix"> ' +
                '<div class="attr-content">' +
                '<input type="text" value="" class="attr-name"/> ' +
                '<select class="attr-type">' +
                '<option value="string">string</option>' +
                '<option value="int">int</option>' +
                '<option value="long">long</option>' +
                '<option value="float">float</option>' +
                '<option value="double">double</option>' +
                '<option value="bool">bool</option>' +
                '<option value="object">object</option>' +
                '</select>' +
                '</div> <div class="attr-nav"> </div></div>' +
                '<label class="error-message"></label></li>');
            changeAtrributeNavigation();
        };

        /** attribute move up button action */
        var moveUpAttribute = function () {
            var $current = $(this).closest('li')
            var $previous = $current.prev('li');
            if ($previous.length !== 0) {
                $current.insertBefore($previous);
            }
            changeAtrributeNavigation();
        };

        /** attribute move down function */
        var moveDownAttribute = function () {
            var $current = $(this).closest('li')
            var $next = $current.next('li');
            if ($next.length !== 0) {
                $current.insertAfter($next);
            }
            changeAtrributeNavigation();
        };

        /** Function to manage the attribute navigations */
        var changeAtrributeNavigation = function () {
            $('.attr-nav').empty();
            var attrLength = $('#attribute-div li').length;
            if (attrLength == 1) {
                $('.attribute:eq(0)').find('.attr-nav').empty();
            }
            if (attrLength == 2) {
                $('.attribute:eq(0)').find('.attr-nav').append('<a class = "reorder-down"><i class="fw fw-sort-down">' +
                    '</i></a><a class = "btn-del-attr"><i class="fw fw-delete"></i></a>');
                $('.attribute:eq(1)').find('.attr-nav').append('<a class="reorder-up"> <i class="fw fw-sort-up "></i>' +
                    '</a><a class = "btn-del-attr"><i class="fw fw-delete"></i></a>');
            }
            if (attrLength > 2) {
                var lastIndex = attrLength - 1;
                for (var i = 0; i < attrLength; i++) {
                    $('.attribute:eq(' + i + ')').find('.attr-nav').append('<a class="reorder-up"> ' +
                        '<i class="fw fw-sort-up"></i></a>' +
                        '<a class = "reorder-down"><i class="fw fw-sort-down"> </i></a>' +
                        '<a class = "btn-del-attr"><i class="fw fw-delete"></i></a>');
                }
                $('.attribute:eq(0)').find('.attr-nav a:eq(0)').remove();
                $('.attribute:eq(' + lastIndex + ')').find('.attr-nav a:eq(1)').remove();
            }
        };

        /**
         * Function to validate the attribute names
         * @param {Object} attributeNameList to add the valid attribute names
         * @return {boolean} isErrorOccurred
         */
        var validateAttributeNames = function (attributeNameList) {
            var isErrorOccurred = false;
            $('.attr-name').each(function () {
                var attributeName = $(this).val().trim();
                if (attributeName != "") {
                    if (attributeName.indexOf(' ') >= 0) {
                        $(this).parents(".attribute").find(".error-message").text("Name can not have white space")
                        $(this)[0].scrollIntoView();
                        $(this).addClass('required-input-field')
                        isErrorOccurred = true;
                        return;
                    }
                    if (!alphabeticValidatorRegex.test(attributeName.charAt(0))) {
                        $(this).parents(".attribute").find(".error-message").text("Name must start with an" +
                            " alphabetical character");
                        $(this)[0].scrollIntoView();
                        $(this).addClass('required-input-field')
                        isErrorOccurred = true;
                        return;
                    }
                    attributeNameList.push(attributeName)
                }
            });
            return isErrorOccurred;
        };

        /**
         * Function to check if an annotation is predefined using the annotation name
         * @param {Object} predefinedAnnotationList list of predefined annotations
         * @param {String} annotationName the name which needs to be checked
         * @return {Object} predefinedObject
         */
        var isPredefinedAnnotation = function (predefinedAnnotationList, annotationName) {
            var predefinedObject = null;
            _.forEach(predefinedAnnotationList, function (predefinedAnnotation) {
                if (predefinedAnnotation.name.toLowerCase() == annotationName.toLowerCase()) {
                    predefinedObject = predefinedAnnotation
                    return;
                }
            });
            return predefinedObject;
        };

        /**
         * To validate the predefined annotations
         * To store the annotation nodes which needs to be built as a string
         * @param {Object} predefinedAnnotationList List of predefined annotations
         * @param {Object} annotationNodes array to add the nodes which needs to be built as a string
         * @return {boolean} isErrorOccurred
         */
        var validatePredefinedAnnotations = function (predefinedAnnotationList, annotationNodes) {
            //gets all the parent nodes
            var jsTreeAnnotationList = $('#annotation-div').jstree(true)._model.data['#'].children;
            var isErrorOccurred = false;
            mainLoop:
            for (var jsTreeAnnotation of jsTreeAnnotationList) {
                var node_info = $('#annotation-div').jstree("get_node", jsTreeAnnotation);
                var predefinedObject = isPredefinedAnnotation(predefinedAnnotationList, node_info.text.trim())
                if (predefinedObject != null) {
                    //check if predefined annotation is mandatory or optional and checked
                    if ((predefinedObject.isMandatory) || (!predefinedObject.isMandatory && node_info
                        .state.checked == true)) {
                        //validate the elements of the jstree predefined Annotations
                        for (var jsTreePredefinedAnnotationElement of node_info.children) {
                            var annotation_key_info = $('#annotation-div').jstree("get_node",
                                jsTreePredefinedAnnotationElement);
                            var annotation_value_info = $('#annotation-div').jstree("get_node", annotation_key_info
                                .children[0])
                            //validate for checked(optional)properties which has empty values
                            if (annotation_key_info.state.checked && annotation_value_info.text.trim() == "") {
                                DesignViewUtils.prototype.errorAlert("Property '" + annotation_key_info.text.trim() +
                                    "' is empty");
                                isErrorOccurred = true;
                                break mainLoop;
                            }
                            //traverse through the predefined object's element to check if it is mandatory
                            for (var predefinedObjectElement of predefinedObject.elements) {
                                if (annotation_key_info.text.trim().toLowerCase() == predefinedObjectElement.key
                                    .toLowerCase()) {
                                    if (predefinedObjectElement.isMandatory) {
                                        if (annotation_value_info.text.trim() == "") {
                                            DesignViewUtils.prototype.errorAlert("Property '" + predefinedObjectElement
                                                .key + "' is mandatory");
                                            isErrorOccurred = true;
                                            break mainLoop;
                                        }
                                    }
                                }
                            }
                        }
                        annotationNodes.push(jsTreeAnnotation)
                    }
                } else {
                    annotationNodes.push(jsTreeAnnotation)
                }
            }
            return isErrorOccurred;
        };

        /**
         * Function to check the predefined saved annotations
         * @param {Object} checkedBoxes array of saved annotation names
         */
        var checkPredefinedAnnotations = function (checkedBoxes) {
            var jsTreeNodes = $('#annotation-div').jstree(true).get_json('#', { 'flat': true });
            for (var checkedBoxName of checkedBoxes) {
                for (var node of jsTreeNodes) {
                    if (node.text.trim().toLowerCase() == checkedBoxName.toLowerCase()) {
                        $("#annotation-div").jstree(true).check_node(node.id)
                        break;
                    }
                }
            }
        };

        /**
         * Function to build the annotations as a string
         * Function to create the annotation objects
         * @param {Object} annotationNodes array of nodes which needs to be constructed
         * @param {Object} annotationStringList array to add the built annotation strings
         * @param {Object} annotationObjectList array to add the created annotation objects
         */
        var annotation = "";
        var buildAnnotation = function (annotationNodes, annotationStringList, annotationObjectList) {
            _.forEach(annotationNodes, function (node) {
                var node_info = $('#annotation-div').jstree("get_node", node);
                var childArray = node_info.children
                if (childArray.length != 0) {
                    annotation += "@" + node_info.text.trim() + "( "
                    //create annotation object
                    var annotationObject = new AnnotationObject();
                    annotationObject.setName(node_info.text.trim())
                    traverseChildAnnotations(childArray, annotationObject)
                    annotation = annotation.substring(0, annotation.length - 1);
                    annotation += ")"
                    annotationObjectList.push(annotationObject)
                    annotationStringList.push(annotation);
                    annotation = "";
                }
            });
        };

        /**
         * Function to traverse the children of the parent annotaions
         * @param {Object} children the children of a parent annotation node
         * @param {Object} annotationObject the parent's annotation object
         */
        var traverseChildAnnotations = function (children, annotationObject) {
            children.forEach(function (node) {
                node_info = $('#annotation-div').jstree("get_node", node);
                //if the child is a sub annotation
                if ((node_info.original != undefined && node_info.original.class == "annotation") ||
                    (node_info.li_attr != undefined && (node_info.li_attr.class == "annotation" ||
                        node_info.li_attr.class == "optional-annotation" || node_info.li_attr.class ==
                        "mandatory-annotation"))) {
                    if (node_info.children.length != 0) {
                        annotation += "@" + node_info.text.trim() + "( "
                        var childAnnotation = new AnnotationObject();
                        childAnnotation.setName(node_info.text.trim())
                        traverseChildAnnotations(node_info.children, childAnnotation)
                        annotationObject.addAnnotation(childAnnotation)
                        annotation = annotation.substring(0, annotation.length - 1);
                        annotation += "),"
                    }
                } else {
                    //if the child is a property
                    if (node_info.li_attr.class != undefined && (node_info.li_attr.class == "optional-key")
                        && node_info.state.checked == false) {
                        //not to add the child property if it hasn't been checked(predefined optional-key only)
                    } else {
                        annotation += node_info.text.trim() + "="
                        var node_value = $('#annotation-div').jstree("get_node", node_info.children[0]).text.trim();
                        annotation += "'" + node_value + "' ,";
                        var element = new AnnotationElement(node_info.text.trim(), node_value)
                        annotationObject.addElement(element);
                    }
                }
            });
        };

        /**
         * Function to initialize the jstree
         * Function to add the event listeners for the jstree -div
         */
        var loadAnnotation = function () {
            //initialise jstree
            $("#annotation-div").jstree({
                "core": {
                    "check_callback": true
                },
                "themes": {
                    "theme": "default",
                    "url": "editor/commons/lib/js-tree-v3.3.2/themes/default/style.css"
                },
                "checkbox": {
                    "three_state": false,
                    "whole_node": false,
                    "tie_selection": false
                },
                "plugins": ["themes", "checkbox"]
            });

            var tree = $('#annotation-div').jstree(true);

            //to add key-value for annotation node
            $("#btn-add-key-val").on("click", function () {
                var selectedNode = $("#annotation-div").jstree("get_selected");
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
                tree.open_node(selectedNode);
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
                tree.open_node(selectedNode);
                tree.deselect_all();
            });

            //to delete an annotation or a key-value node
            $("#btn-del-annotation").on("click", function () {
                var selectedNode = $("#annotation-div").jstree("get_selected");
                tree.delete_node([selectedNode]);
                tree.deselect_all();
            })

            //to edit the selected node
            //to hide/show the buttons corresponding to the node selected
            $('#annotation-div').on("select_node.jstree", function (e, data) {
                var node_info = $('#annotation-div').jstree("get_node", data.node)
                if ((node_info.original != undefined && (node_info.original.class == "annotation")) ||
                    (node_info.li_attr != undefined && (node_info.li_attr.class == "annotation"))) {
                    tree.edit(data.node)
                    $("#btn-del-annotation").show();
                    $("#btn-add-annotation").show();
                    $("#btn-add-key-val").show();

                } else if ((node_info.original != undefined && (node_info.original.class == "annotation-key")) ||
                    (node_info.li_attr != undefined && (node_info.li_attr.class == "annotation-key"))) {
                    tree.edit(data.node);
                    $("#btn-del-annotation").show();
                    $("#btn-add-annotation").hide();
                    $("#btn-add-key-val").hide();

                } else if ((node_info.original != undefined && (node_info.original.class == "annotation-value")) ||
                    (node_info.li_attr != undefined && (node_info.li_attr.class == "annotation-value"))) {
                    $("#btn-del-annotation").hide();
                    $("#btn-add-annotation").hide();
                    $("#btn-add-key-val").hide();
                    tree.edit(data.node);
                }
            });

            //to unselect the nodes when user clicks other than the nodes in jstree
            $(document).on('click', function (e) {
                if ($(e.target).closest('.jstree').length) {
                    $("#btn-del-annotation").hide();
                    $("#btn-add-annotation").show();
                    $("#btn-add-key-val").hide();
                    tree.deselect_all();
                }
            });

        };

        /**
         * @function generate form when defining a form
         * @param i id for the element
         * @param formConsole Console which holds the form
         * @param formContainer Container which holds the form
         */
        StreamForm.prototype.generateDefineForm = function (i, formConsole, formContainer) {
            var self = this;
            var propertyDiv = $('<div class = "stream-form-container"><div id="property-header"><h3>Stream' +
                ' Configuration</h3></div> <h4>Name: </h4> <input type="text" id="streamName" class="clearfix">' +
                '<label class="error-message" id="streamNameErrorMessage"></label> <div id="define-attribute"></div>' +
                '<button id="btn-submit" type="button" class="btn toggle-view-button">' +
                'Submit </button></div> <div class= "stream-form-container" id="define-annotation"> </div>');
            formContainer.append(propertyDiv);
            self.designViewContainer.addClass('disableContainer');
            self.toggleViewButton.addClass('disableContainer');

            var predefinedAnnotationList = self.configurationData.application.config.stream_predefined_annotations;

            var attributeFormTemplate = Handlebars.compile($('#attribute-form-template').html());
            var wrappedHtml = attributeFormTemplate([{ name: "", type: "string" }]);
            $('#define-attribute').html(wrappedHtml);

            var raw_partial = document.getElementById('recursiveAnnotationPartial').innerHTML;
            Handlebars.registerPartial('recursiveAnnotation', raw_partial);

            var annotationFormTemplate = Handlebars.compile($('#annotation-form-template').html());
            var wrappedHtml = annotationFormTemplate(predefinedAnnotationList);
            $('#define-annotation').html(wrappedHtml);

            //add event listeners for the attribute div buttons
            $("#btn-add-attribute").click(addAttribute)
            $("#attribute-div").on('click', '.btn-del-attr', delAttribute)
            $("#attribute-div").on('click', '.reorder-up', moveUpAttribute)
            $("#attribute-div").on('click', '.reorder-down', moveDownAttribute)
            //onload of the attribute div arrange the navigations of the attribute
            $('#attribute-div').ready(changeAtrributeNavigation);
            $('#annotation-div').ready(loadAnnotation);

            var streamName = "";
            // 'Submit' button action
            var submitButtonElement = $(formContainer).find('#btn-submit')[0];
            submitButtonElement.addEventListener('click', function () {

                $('.error-message').text("")
                $('.required-input-field').removeClass('required-input-field');
                $('#streamNameErrorMessage').text("")
                streamName = $('#streamName').val().trim();

                //to check if stream name is already existing
                var isStreamNameUsed = self.formUtils.isDefinitionElementNameUsed(streamName);
                if (isStreamNameUsed) {
                    $('#streamName').addClass('required-input-field');
                    $('#streamName')[0].scrollIntoView();
                    $('#streamNameErrorMessage').text("Stream name is already used.")
                    return;
                }
                // to check if stream name is empty
                if (streamName == "") {
                    $('#streamName').addClass('required-input-field');
                    $('#streamName')[0].scrollIntoView();
                    $('#streamNameErrorMessage').text("Stream name is required")
                    return;
                }
                //to check if stream name contains white spaces
                if (streamName.indexOf(' ') >= 0) {
                    $('#streamName').addClass('required-input-field');
                    $('#streamName')[0].scrollIntoView();
                    $('#streamNameErrorMessage').text("Stream name cannot have white space.")
                    return;
                }
                //to check if stream name starts with an alphabetic character
                if (!(alphabeticValidatorRegex).test(streamName.charAt(0))) {
                    $('#streamName').addClass('required-input-field');
                    $('#streamName')[0].scrollIntoView();
                    $('#streamNameErrorMessage').text("Stream name must start with an alphabetic character.")
                    return;
                }

                //add the new out stream to the stream array
                var streamOptions = {};
                _.set(streamOptions, 'id', i);
                _.set(streamOptions, 'name', $('#streamName').val());
                var stream = new Stream(streamOptions);

                var attributeNameList = [];
                if (validateAttributeNames(attributeNameList)) { return }

                if (attributeNameList.length == 0) {
                    $('.attribute:eq(0)').find('.attr-name').addClass('required-input-field');
                    $('.attribute:eq(0)').find('.attr-name')[0].scrollIntoView();
                    $('.attribute:eq(0)').find('.error-message').text("Minimum one attribute is required")
                    return;
                } else {
                    $('.attribute .attr-content').each(function () {
                        var nameValue = $(this).find('.attr-name').val().trim();
                        var typeValue = $(this).find('.attr-type').val();
                        if (nameValue != "") {
                            var attributeObject = new Attribute({ name: nameValue, type: typeValue });
                            stream.addAttribute(attributeObject)
                        }
                    });
                }

                var annotationNodes = [];
                var annotationStringList = [];
                var annotationObjectList = [];
                //validate the annotations
                if (validatePredefinedAnnotations(predefinedAnnotationList, annotationNodes)) {
                    return;
                }
                buildAnnotation(annotationNodes, annotationStringList, annotationObjectList);

                _.forEach(annotationStringList, function (annotation) {
                    stream.addAnnotation(annotation);
                });
                _.forEach(annotationObjectList, function (annotation) {
                    stream.addAnnotationObject(annotation)
                });

                // If this is an inner stream perform validation
                var streamSavedInsideAPartition
                    = self.configurationData.getSiddhiAppConfig().getStreamSavedInsideAPartition(i);
                // if streamSavedInsideAPartition is undefined then the stream is not inside a partition
                if (streamSavedInsideAPartition !== undefined) {
                    JSONValidator.prototype.validateInnerStream(stream, self.jsPlumbInstance, true);
                }

                // set the isDesignViewContentChanged to true
                self.configurationData.setIsDesignViewContentChanged(true);
                self.configurationData.getSiddhiAppConfig().addStream(stream);

                var textNode = $('#' + i).find('.streamNameNode');
                textNode.html(streamName);

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
            var propertyDiv = $('<div class = "stream-form-container"><div id="property-header"><h3>Stream' +
                ' Configuration</h3></div> <h4>Name: </h4> <input type="text" id="streamName" class="clearfix">' +
                '<label class="error-message" id="streamNameErrorMessage"></label> <div id="define-attribute"></div>' +
                '<button id="btn-submit" type="button" class="btn toggle-view-button">' +
                'Submit </button></div> <div class= "stream-form-container" id="define-annotation"> </div>');
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
            //load the saved attributes
            var savedAttributes = clickedElement.getAttributeList();

            //load the saved annotations
            var savedAnnotations = clickedElement.getAnnotationListObjects();
            var annotations = [];
            var checkedAnnotations = [];
            var predefinedAnnotationList = self.configurationData.application.config.stream_predefined_annotations
            _.forEach(predefinedAnnotationList, function (predefinedAnnotation) {
                var foundPredefined = false;
                _.forEach(savedAnnotations, function (savedAnnotation) {
                    if (savedAnnotation.name.toLowerCase() == predefinedAnnotation.name.toLowerCase()) {
                        //if an optional annotation is found push it to the checkedAnnotations[]
                        if (!predefinedAnnotation.isMandatory) {
                            checkedAnnotations.push(savedAnnotation.name);
                        }
                        foundPredefined = true;
                        _.forEach(predefinedAnnotation.elements, function (predefinedAnnotationElement) {
                            _.forEach(savedAnnotation.elements, function (savedAnnotationElement) {
                                if (predefinedAnnotationElement.key.toLowerCase() == savedAnnotationElement.key
                                    .toLowerCase()) {
                                    //if an optional property is found push it to the checkedAnnotations[]
                                    if (!predefinedAnnotationElement.isMandatory) {
                                        checkedAnnotations.push(savedAnnotationElement.key);
                                    }
                                    predefinedAnnotationElement.value = savedAnnotationElement.value
                                }
                            })
                        })
                        annotations.push(predefinedAnnotation)
                    } else {
                        annotations.push(savedAnnotation)
                    }
                });
                if (!foundPredefined) {
                    annotations.push(predefinedAnnotation)
                }
            });

            $('#streamName').val(name);
            var attributeFormTemplate = Handlebars.compile($('#attribute-form-template').html());
            var wrappedHtml = attributeFormTemplate(savedAttributes);
            $('#define-attribute').html(wrappedHtml);

            //to select the options(type) of the saved attributes
            var i = 0;
            $('.attribute .attr-content').each(function () {
                $(this).find('.attr-type option').filter(function () {
                    return ($(this).text() == (savedAttributes[i].getType()).toLowerCase());
                }).prop('selected', true);
                i++;
            });

            var raw_partial = document.getElementById('recursiveAnnotationPartial').innerHTML;
            Handlebars.registerPartial('recursiveAnnotation', raw_partial);
            var annotationFormTemplate = Handlebars.compile($('#annotation-form-template').html());
            var wrappedHtml = annotationFormTemplate(annotations);
            $('#define-annotation').html(wrappedHtml);
            loadAnnotation();
            checkPredefinedAnnotations(checkedAnnotations);

            $("#btn-add-attribute").click(addAttribute)
            $("#attribute-div").on('click', '.btn-del-attr', delAttribute)
            $("#attribute-div").on('click', '.reorder-up', moveUpAttribute)
            $("#attribute-div").on('click', '.reorder-down', moveDownAttribute)
            //onload of the attribute div arrange the navigations of the attribute
            $('#attribute-div').ready(changeAtrributeNavigation);

            //submit button action
            var submitButtonElement = $(formContainer).find('#btn-submit')[0];
            submitButtonElement.addEventListener('click', function () {
                $('.error-message').text("")
                $('.required-input-field').removeClass('required-input-field');
                $('#streamNameErrorMessage').text("")
                // set the isDesignViewContentChanged to true
                self.configurationData.setIsDesignViewContentChanged(true);

                var configName = $('#streamName').val().trim();
                var streamName;
                var firstCharacterInStreamName;
                var isStreamNameUsed;
                /*
                * check whether the stream is inside a partition and if yes check whether it begins with '#'.
                *  If not add '#' to the beginning of the stream name.
                * */
                var isStreamSavedInsideAPartition
                    = self.configurationData.getSiddhiAppConfig().getStreamSavedInsideAPartition(id);
                if (!isStreamSavedInsideAPartition) {
                    firstCharacterInStreamName = (configName).charAt(0);
                    if (firstCharacterInStreamName === '#') {
                        $('#streamName').addClass('required-input-field');
                        $('#streamName')[0].scrollIntoView();
                        $('#streamNameErrorMessage').text("'#' is used to define inner streams only.")
                        return;
                    } else {
                        streamName = configName;
                    }
                    isStreamNameUsed
                        = self.formUtils.isDefinitionElementNameUsed(streamName, id);
                    if (isStreamNameUsed) {
                        $('#streamName').addClass('required-input-field');
                        $('#streamName')[0].scrollIntoView();
                        $('#streamNameErrorMessage').text("Stream name is already defined.")
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
                        $('#streamName').addClass('required-input-field');
                        $('#streamName')[0].scrollIntoView();
                        $('#streamNameErrorMessage').text("Stream name is already defined in the partition.")
                        return;
                    }
                }

                var previouslySavedName = clickedElement.getName().trim();
                // update connection related to the element if the name is changed
                if (previouslySavedName !== streamName) {
                    //check if stream name is empty
                    if (streamName == "") {
                        $('#streamName').addClass('required-input-field');
                        $('#streamName')[0].scrollIntoView();
                        $('#streamNameErrorMessage').text("Stream name is required.")
                        return;
                    }
                    if ((streamName.indexOf(' ') >= 0)) {
                        $('#streamName').addClass('required-input-field');
                        $('#streamName')[0].scrollIntoView();
                        $('#streamNameErrorMessage').text("Stream name cannot have white space.")
                        return;
                    }
                    if (!alphabeticValidatorRegex.test(streamName.charAt(0))) {
                        $('#streamName').addClass('required-input-field');
                        $('#streamName')[0].scrollIntoView();
                        $('#streamNameErrorMessage').text("Stream name must start with an alphabetic character.")
                        return;
                    }
                    // update selected stream model
                    clickedElement.setName(streamName);
                    self.formUtils.updateConnectionsAfterDefinitionElementNameChange(id);
                }

                var attributeNameList = [];
                if (validateAttributeNames(attributeNameList)) { return }

                if (attributeNameList.length == 0) {
                    $('.attribute:eq(0)').find('.attr-name').addClass('required-input-field');
                    $('.attribute:eq(0)').find('.attr-name')[0].scrollIntoView();
                    $('.attribute:eq(0)').find('.error-message').text("Minimum one attribute is required")
                    return;
                } else {
                    //clear the previously saved attribute list
                    clickedElement.clearAttributeList();
                    //add the attributes to the attribute list
                    $('.attribute .attr-content').each(function () {
                        var nameValue = $(this).find('.attr-name').val().trim();
                        var typeValue = $(this).find('.attr-type').val();
                        if (nameValue != "") {
                            var attributeObject = new Attribute({ name: nameValue, type: typeValue });
                            clickedElement.addAttribute(attributeObject)
                        }
                    });
                }

                var annotationNodes = [];
                var annotationStringList = [];
                var annotationObjectList = [];

                if (validatePredefinedAnnotations(predefinedAnnotationList, annotationNodes)) {
                    return;
                }

                clickedElement.clearAnnotationList();
                clickedElement.clearAnnotationListObjects();

                buildAnnotation(annotationNodes, annotationStringList, annotationObjectList);

                _.forEach(annotationStringList, function (annotation) {
                    clickedElement.addAnnotation(annotation);
                });
                _.forEach(annotationObjectList, function (annotation) {
                    clickedElement.addAnnotationObject(annotation);
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
