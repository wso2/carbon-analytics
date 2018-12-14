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

define(['require', 'log', 'jquery', 'lodash', 'partitionWith', 'designViewUtils', 'jsonValidator', 'handlebar',
    'annotationObject', 'annotationElement'],
    function (require, log, $, _, PartitionWith, DesignViewUtils, JSONValidator, Handlebars, AnnotationObject,
        AnnotationElement) {

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
                this.formUtils = options.formUtils;
                this.jsPlumbInstance = options.jsPlumbInstance;
                var currentTabId = this.application.tabController.activeTab.cid;
                this.designViewContainer = $('#design-container-' + currentTabId);
                this.toggleViewButton = $('#toggle-view-button-' + currentTabId);
            }
        };

        /**
         * Function to add the error class
         * @param {Object} id object where the errors needs to be displayed
         */
        var addErrorClass = function (id) {
            $(id)[0].scrollIntoView();
            $(id).addClass('required-input-field')
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
        * Function to build the annotations as a string
        * Function to create the annotation objects
        * @param {Object} annotationStringList array to add the built annotation strings
        * @param {Object} annotationObjectList array to add the created annotation objects
        */
        var annotation = "";
        var buildAnnotation = function (annotationStringList, annotationObjectList) {
            var jsTreeNodes = $('#annotation-div').jstree(true)._model.data['#'].children;
            _.forEach(jsTreeNodes, function (node) {
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
         * Function to check if the connected streams are filled
         * @param {Object} partitionWithList
         * @return {boolean} isFilled
         */
        var ifStreamsAreFilled = function (partitionWithList) {
            var isFilled = false;
            for (var partitionKey of partitionWithList) {
                if (partitionKey.getStreamName()) {
                    isFilled = true;
                    break;
                }
            }
            return isFilled;
        };

        /**
         * @function generate form for Partition
         * @param element selected element(partition)
         * @param formConsole Console which holds the form
         * @param formContainer Container which holds the form
         */
        PartitionForm.prototype.generatePropertiesForm = function (element, formConsole, formContainer) {
            var self = this;
            var id = $(element).parent().attr('id');

            var clickedElement = self.configurationData.getSiddhiAppConfig().getPartition(id);
            var partitionWithList = clickedElement.getPartitionWith();

            if (!partitionWithList || partitionWithList.length === 0) {
                // design view container and toggle view button are enabled
                self.designViewContainer.removeClass('disableContainer');
                self.toggleViewButton.removeClass('disableContainer');

                // close the form window
                self.consoleListManager.removeFormConsole(formConsole);

                $("#" + id).addClass('incomplete-element');
                $('#' + id).prop('title', 'Connect a stream for partitioning');

            } else {

                if (!ifStreamsAreFilled(partitionWithList)) {

                    // design view container and toggle view button are enabled
                    self.designViewContainer.removeClass('disableContainer');
                    self.toggleViewButton.removeClass('disableContainer');

                    // close the form window
                    self.consoleListManager.removeFormConsole(formConsole);

                    $("#" + id).addClass('incomplete-element');
                    $('#' + id).prop('title', 'To edit partition configuration, fill the connected stream.');
                } else {
                    $('#' + id).addClass('selected-element');
                    $(".overlayed-container").fadeTo(200, 1);
                    // design view container and toggle view button are enabled
                    self.designViewContainer.addClass('disableContainer');
                    self.toggleViewButton.addClass('disableContainer');
                    var propertyDiv = $('<div id="property-header"><h3>Partition Configuration</h3></div>' +
                        '<div class = "partition-form-container"> <div id = "define-partition-keys"> </div> ' +
                        '<button id="btn-submit" type="button" class="btn toggle-view-button"> Submit </button> ' +
                        '<button id="btn-cancel" type="button" class="btn btn-default"> Cancel </button> </div>' +
                        '<div class = "partition-form-container"> <div id = "define-annotation"> </div> </div>');
                    formContainer.append(propertyDiv);

                    var savedAnnotations = [];
                    savedAnnotations = clickedElement.getAnnotationListObjects();
                    //render the user defined annotations form template
                    var raw_partial = document.getElementById('recursiveAnnotationPartial').innerHTML;
                    Handlebars.registerPartial('recursiveAnnotation', raw_partial);
                    var annotationFormTemplate = Handlebars.compile($('#annotation-form-template').html());
                    var wrappedHtml = annotationFormTemplate(savedAnnotations);
                    $('#define-annotation').html(wrappedHtml);
                    loadAnnotation();

                    var partitionKeys = [];
                    for (var i = 0; i < partitionWithList.length; i++) {
                        if (partitionWithList[i].getStreamName()) {
                            var partitionKey = {
                                expression: partitionWithList[i].getExpression(),
                                streamName: partitionWithList[i].getStreamName()
                            };
                            partitionKeys.push(partitionKey);
                        }
                    }

                    var partitionFormTemplate = Handlebars.compile($('#partition-by-template').html());
                    var wrappedHtml = partitionFormTemplate(partitionKeys);
                    $('#define-partition-keys').html(wrappedHtml);

                    // 'Submit' button action
                    var submitButtonElement = $(formContainer).find('#btn-submit')[0];
                    submitButtonElement.addEventListener('click', function () {

                        //clear the error classes
                        $('.error-message').text("")
                        $('.required-input-field').removeClass('required-input-field');
                        var isErrorOccurred = false;

                        var partitionKeys = [];
                        $('#partition-by-content .partition-key').each(function () {
                            var expression = $(this).find('.partition-by-expression').val().trim();
                            if (expression === "") {
                                addErrorClass($(this).find('.partition-by-expression'));
                                $(this).find('.error-message').text("Expression value is required.")
                                isErrorOccurred = true;
                                return false;
                            } else {
                                var streamName = $(this).find('.partition-by-stream-name').val().trim();
                                var partitionKey = {
                                    expression: expression,
                                    streamName: streamName
                                };
                                partitionKeys.push(partitionKey);
                            }
                        });

                        if (!isErrorOccurred) {
                            clickedElement.clearPartitionWith();
                            _.forEach(partitionKeys, function (partitionKey) {
                                var partitionWithObject = new PartitionWith(partitionKey);
                                clickedElement.addPartitionWith(partitionWithObject);
                            });

                            // perform JSON validation
                            JSONValidator.prototype.validatePartition(clickedElement, self.jsPlumbInstance, false);

                            clickedElement.clearAnnotationList();
                            clickedElement.clearAnnotationListObjects();
                            var annotationStringList = [];
                            var annotationObjectList = [];

                            buildAnnotation(annotationStringList, annotationObjectList);
                            _.forEach(annotationStringList, function (annotation) {
                                clickedElement.addAnnotation(annotation);
                            });
                            _.forEach(annotationObjectList, function (annotation) {
                                clickedElement.addAnnotationObject(annotation);
                            });

                            // set the isDesignViewContentChanged to true
                            self.configurationData.setIsDesignViewContentChanged(true);
                            $('#' + id).removeClass('incomplete-element');
                            $('#' + id).prop('title', '');

                            // design view container and toggle view button are enabled
                            self.designViewContainer.removeClass('disableContainer');
                            self.toggleViewButton.removeClass('disableContainer');

                            // close the form window
                            self.consoleListManager.removeFormConsole(formConsole);
                        }
                    });

                    // 'Cancel' button action
                    var cancelButtonElement = $(formContainer).find('#btn-cancel')[0];
                    cancelButtonElement.addEventListener('click', function () {
                        self.designViewContainer.removeClass('disableContainer');
                        self.toggleViewButton.removeClass('disableContainer');
                        // close the form window
                        self.consoleListManager.removeFormConsole(formConsole);
                    });
                }
            }
        };

        return PartitionForm;
    });
