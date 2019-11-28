/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

define(['require', 'elementUtils', 'constants'],
    function (require, ElementUtils, Constants) {

        /**
         * @class Stream
         * @constructor
         * @class Stream  Creates a Stream
         * @param {Object} options Rendering options for the view
         */
        var Stream = function (options) {
            /*
             Data storing structure as follows
                id: '',
                previousCommentSegment:'',
                name: '',
                attributeList: [
                    {
                        name: ‘’,
                        type: ‘’
                    }
                ],
                annotationList: [annotation1, annotation2, ...]
            */
            if (options !== undefined) {
                this.id = options.id;
                this.previousCommentSegment = options.previousCommentSegment;
                this.name = options.name;
            }

            this.annotationListObjects = [];
            this.attributeList = [];
            this.annotationList = [];
        };

        Stream.prototype.addAnnotationObject = function (annotation) {
            this.annotationListObjects.push(annotation)
        };

        Stream.prototype.addAttribute = function (attribute) {
            this.attributeList.push(attribute);
        };

        Stream.prototype.addAnnotation = function (annotation) {
            this.annotationList.push(annotation);
        };

        Stream.prototype.clearAnnotationListObjects = function () {
            ElementUtils.prototype.removeAllElements(this.annotationListObjects);
        };

        Stream.prototype.clearAnnotationList = function () {
            ElementUtils.prototype.removeAllElements(this.annotationList);
        };

        Stream.prototype.clearAttributeList = function () {
            ElementUtils.prototype.removeAllElements(this.attributeList);
        };

        Stream.prototype.getId = function () {
            return this.id;
        };

        Stream.prototype.getName = function () {
            return this.name;
        };

        Stream.prototype.getAttributeList = function () {
            return this.attributeList;
        };

        Stream.prototype.getAnnotationList = function () {
            return this.annotationList;
        };

        Stream.prototype.getAnnotationListObjects = function () {
            return this.annotationListObjects;
        };

        Stream.prototype.setId = function (id) {
            this.id = id;
        };

        Stream.prototype.setName = function (name) {
            this.name = name;
        };

        Stream.prototype.setAttributeList = function (attributeList) {
            this.attributeList = attributeList;
        };

        Stream.prototype.setAnnotationList = function (annotationList) {
            this.annotationList = annotationList;
        };

        Stream.prototype.hasFaultStream = function () {
            // Check if the OnError(action='STREAM') annotation is set. If so enable the fault connector.
            var faultStream = false;
            this.annotationListObjects.forEach(function (annotation) {
                if (annotation.name.toLowerCase() === 'onerror') {
                    annotation.elements.forEach(function (p) {
                        if (p.key.toLowerCase() === 'action' && p.value.toLowerCase() === 'stream') {
                            faultStream = true;
                        }
                    });
                }
            });
            return faultStream;
        };

        Stream.prototype.isFaultStream = function () {
            return this.name && this.name.startsWith(Constants.FAULT_STREAM_PREFIX);
        };


        /**
         * @class Table
         * @constructor
         * @class Table  Creates a Table
         * @param {Object} options Rendering options for the view
         */
        var Table = function (options) {
            /*
             Data storing structure as follows
             id: '',
             previousCommentSegment:'',
             name: '',
             attributeList: [
             {
             name: ‘’,
             type: ‘’
             }
             ],
             store: {},
             annotationList: [annotation1, annotation2, ...]
             */
            if (options !== undefined) {
                this.id = options.id;
                this.previousCommentSegment = options.previousCommentSegment;
                this.name = options.name;
                this.store = options.store;
            }
            this.annotationListObjects = [];
            this.attributeList = [];
            this.annotationList = [];
        };

        Table.prototype.addAnnotationObject = function (annotation) {
            this.annotationListObjects.push(annotation)
        };

        Table.prototype.addAttribute = function (attribute) {
            this.attributeList.push(attribute);
        };

        Table.prototype.addAnnotation = function (annotation) {
            this.annotationList.push(annotation);
        };

        Table.prototype.clearAnnotationList = function () {
            ElementUtils.prototype.removeAllElements(this.annotationList);
        };

        Table.prototype.clearAttributeList = function () {
            ElementUtils.prototype.removeAllElements(this.attributeList);
        };

        Table.prototype.clearAnnotationListObjects = function () {
            ElementUtils.prototype.removeAllElements(this.annotationListObjects);
        };

        Table.prototype.getAnnotationListObjects = function () {
            return this.annotationListObjects;
        };

        Table.prototype.getId = function () {
            return this.id;
        };

        Table.prototype.getName = function () {
            return this.name;
        };

        Table.prototype.getStore = function () {
            return this.store;
        };

        Table.prototype.getAttributeList = function () {
            return this.attributeList;
        };

        Table.prototype.getAnnotationList = function () {
            return this.annotationList;
        };

        Table.prototype.setId = function (id) {
            this.id = id;
        };

        Table.prototype.setName = function (name) {
            this.name = name;
        };

        Table.prototype.setStore = function (store) {
            this.store = store;
        };

        Table.prototype.setAttributeList = function (attributeList) {
            this.attributeList = attributeList;
        };

        Table.prototype.setAnnotationList = function (annotationList) {
            this.annotationList = annotationList;
        };

        /**
         * @class Aggregation
         * @constructor
         * @class Aggregation  Creates an Aggregation definition object
         * @param {Object} options Rendering options for the view
         */
        var Aggregation = function (options) {
            /*
             Data storing structure as follows
                id: '',
                previousCommentSegment:'',
                name*: '',
                from*: ‘’,
                select*: [
                    {
                        type*: 'USER_DEFINED',
                        value*: [
                            {
                                expression*: '',
                                as: ''
                            },
                            ...
                        ]
                        << or >>
                        type*: 'ALL',
                        value*: '*'
                    }
                ],
                groupBy: ['value1',...],
                aggregateByAttribute*: ‘’,
                aggregateByTimePeriod*: {
                    type*: 'RANGE',
                    value*: {
                        min*: '',
                        max*: ''
                    }
                    << or >>
                    type*: 'INTERVAL',
                    value*: ['seconds', 'minutes', ...] // At least one value must be available
                },
                store: {Store JSON},
                annotationList: [annotation1, annotation2, ...]
            */
            if (options !== undefined) {
                this.id = options.id;
                this.previousCommentSegment = options.previousCommentSegment;
                this.name = options.name;
                this.from = options.from;
                this.select = options.select;
                this.groupBy = options.groupBy;
                this.aggregateByAttribute = options.aggregateByAttribute;
                this.aggregateByTimePeriod = options.aggregateByTimePeriod;
                this.store = options.store;
            }
            this.annotationList = [];
            this.annotationListObjects = [];
        };

        Aggregation.prototype.addAnnotation = function (annotation) {
            this.annotationList.push(annotation);
        };

        Aggregation.prototype.addAnnotationObject = function (annotation) {
            this.annotationListObjects.push(annotation);
        };

        Aggregation.prototype.clearAnnotationList = function () {
            ElementUtils.prototype.removeAllElements(this.annotationList);
        };

        Aggregation.prototype.clearAnnotationListObjects = function () {
            ElementUtils.prototype.removeAllElements(this.annotationListObjects);
        };

        Aggregation.prototype.getId = function () {
            return this.id;
        };

        Aggregation.prototype.getName = function () {
            return this.name;
        };

        Aggregation.prototype.getConnectedSource = function () {
            return this.from;
        };

        Aggregation.prototype.getSelect = function () {
            return this.select;
        };

        Aggregation.prototype.getGroupBy = function () {
            return this.groupBy;
        };

        Aggregation.prototype.getAggregateByAttribute = function () {
            return this.aggregateByAttribute;
        };

        Aggregation.prototype.getAggregateByTimePeriod = function () {
            return this.aggregateByTimePeriod;
        };

        Aggregation.prototype.getStore = function () {
            return this.store;
        };

        Aggregation.prototype.getAnnotationList = function () {
            return this.annotationList;
        };

        Aggregation.prototype.getAnnotationListObjects = function () {
            return this.annotationListObjects;
        };

        Aggregation.prototype.setId = function (id) {
            this.id = id;
        };

        Aggregation.prototype.setName = function (name) {
            this.name = name;
        };

        Aggregation.prototype.setConnectedSource = function (from) {
            this.from = from;
        };

        Aggregation.prototype.setSelect = function (select) {
            this.select = select;
        };

        Aggregation.prototype.setGroupBy = function (groupBy) {
            this.groupBy = groupBy;
        };

        Aggregation.prototype.setAggregateByAttribute = function (aggregateByAttribute) {
            this.aggregateByAttribute = aggregateByAttribute;
        };

        Aggregation.prototype.setAggregateByTimePeriod = function (aggregateByTimePeriod) {
            this.aggregateByTimePeriod = aggregateByTimePeriod;
        };

        Aggregation.prototype.setStore = function (store) {
            this.store = store;
        };

        Aggregation.prototype.setAnnotationList = function (annotationList) {
            this.annotationList = annotationList;
        };

        Aggregation.prototype.setAnnotationListObjects = function (annotationListObjects) {
            this.annotationListObjects = annotationListObjects;
        };

        Aggregation.prototype.resetModel = function (model) {
            model.setSelect(undefined);
            var groupBy = model.getGroupBy();
            var aggregateByAttribute = model.getAggregateByAttribute();
            if (groupBy && groupBy.length > 0) {
                model.setGroupBy([" "]);
            }
            if (aggregateByAttribute && aggregateByAttribute != "") {
                model.setAggregateByAttribute(" ");
            }
        };

        /**
         * @class Function
         * @constructor
         * @class Function  Creates a Function Definition
         * @param {Object} options Rendering options for the view
         */
        var FunctionDefinition = function (options) {
            /*
             Data storing structure as follows
             id*: '',
             previousCommentSegment:'',
             name*: '',
             scriptType*: 'JAVASCRIPT | R | SCALA',
             returnType*: 'INT | LONG | DOUBLE | FLOAT | STRING | BOOL | OBJECT',
             body*: ''
             */
            if (options !== undefined) {
                this.id = options.id;
                this.previousCommentSegment = options.previousCommentSegment;
                this.name = options.name;
                this.scriptType = (options.scriptType !== undefined) ? (options.scriptType).toUpperCase() : undefined;
                this.returnType = (options.returnType !== undefined) ? (options.returnType).toUpperCase() : undefined;
                this.body = options.body;
            }
        };

        FunctionDefinition.prototype.getId = function () {
            return this.id;
        };

        FunctionDefinition.prototype.getName = function () {
            return this.name;
        };

        FunctionDefinition.prototype.getScriptType = function () {
            return this.scriptType;
        };

        FunctionDefinition.prototype.getReturnType = function () {
            return this.returnType;
        };

        FunctionDefinition.prototype.getBody = function () {
            return this.body;
        };

        FunctionDefinition.prototype.setId = function (id) {
            this.id = id;
        };

        FunctionDefinition.prototype.setName = function (name) {
            this.name = name;
        };

        FunctionDefinition.prototype.setScriptType = function (scriptType) {
            this.scriptType = scriptType.toUpperCase();
        };

        FunctionDefinition.prototype.setReturnType = function (returnType) {
            this.returnType = returnType.toUpperCase();
        };

        FunctionDefinition.prototype.setBody = function (body) {
            this.body = body;
        };


        /**
         * @class Trigger
         * @constructor
         * @class Trigger  Creates a Trigger
         * @param {Object} options Rendering options for the view
         */
        var Trigger = function (options) {
            /*
             Data storing structure as follows
             id*: '',
             previousCommentSegment:'',
             name*: '',
             at*: ‘’,
             atEvery*: '',
             annotationList: [annotation1, annotation2, ...]
             */
            if (options !== undefined) {
                this.id = options.id;
                this.previousCommentSegment = options.previousCommentSegment;
                this.name = options.name;
                this.criteria = options.criteria;
                this.criteriaType = options.criteriaType;
            }
            this.annotationList = [];
        };

        Trigger.prototype.addAnnotation = function (annotation) {
            this.annotationList.push(annotation);
        };

        Trigger.prototype.clearAnnotationList = function () {
            ElementUtils.prototype.removeAllElements(this.annotationList);
        };

        Trigger.prototype.getId = function () {
            return this.id;
        };

        Trigger.prototype.getName = function () {
            return this.name;
        };

        Trigger.prototype.getCriteria = function () {
            return this.criteria;
        };

        Trigger.prototype.getCriteriaType = function () {
            return this.criteriaType;
        };

        Trigger.prototype.getAnnotationList = function () {
            return this.annotationList;
        };

        Trigger.prototype.setId = function (id) {
            this.id = id;
        };

        Trigger.prototype.setName = function (name) {
            this.name = name;
        };

        Trigger.prototype.setCriteria = function (criteria) {
            this.criteria = criteria;
        };

        Trigger.prototype.setCriteriaType = function (criteriaType) {
            this.criteriaType = criteriaType;
        };

        Trigger.prototype.setAnnotationList = function (annotationList) {
            this.annotationList = annotationList;
        };

        /**
         * @class Window
         * @constructor
         * @class Window  Creates a Window
         * @param {Object} options Rendering options for the view
         */
        var Window = function (options) {
            /*
             Data storing structure as follows
             id: '',
             previousCommentSegment:'',
             name: '',
             attributeList: [
             {
             name: ‘’,
             type: ‘’
             }
             ],
             function*: ‘time|length|timeBatch|lengthBatch...’,
             parameters*: ['value1',...],
             outputEventType: ‘CURRENT_EVENTS|EXPIRED_EVENTS|ALL_EVENTS’,
             annotationList: [annotation1, annotation2, ...]
             */
            if (options !== undefined) {
                this.id = options.id;
                this.previousCommentSegment = options.previousCommentSegment;
                this.name = options.name;
                this.type = options.type;
                this.parameters = options.parameters;
                this.outputEventType
                    = (options.outputEventType !== undefined) ? (options.outputEventType).toUpperCase() : undefined;
            }
            this.annotationListObjects = [];
            this.attributeList = [];
            this.annotationList = [];
        };

        Window.prototype.addAttribute = function (attribute) {
            this.attributeList.push(attribute);
        };

        Window.prototype.addAnnotationObject = function (annotation) {
            this.annotationListObjects.push(annotation)
        };

        Window.prototype.addAnnotation = function (annotation) {
            this.annotationList.push(annotation);
        };

        Window.prototype.clearAnnotationList = function () {
            ElementUtils.prototype.removeAllElements(this.annotationList);
        };

        Window.prototype.clearAnnotationListObjects = function () {
            ElementUtils.prototype.removeAllElements(this.annotationListObjects);
        };

        Window.prototype.clearAttributeList = function () {
            ElementUtils.prototype.removeAllElements(this.attributeList);
        };

        Window.prototype.getId = function () {
            return this.id;
        };

        Window.prototype.getName = function () {
            return this.name;
        };

        Window.prototype.getAttributeList = function () {
            return this.attributeList;
        };

        Window.prototype.getType = function () {
            return this.type;
        };

        Window.prototype.getParameters = function () {
            return this.parameters;
        };

        Window.prototype.getOutputEventType = function () {
            return this.outputEventType;
        };

        Window.prototype.getAnnotationList = function () {
            return this.annotationList;
        };

        Window.prototype.getAnnotationListObjects = function () {
            return this.annotationListObjects;
        };

        Window.prototype.setId = function (id) {
            this.id = id;
        };

        Window.prototype.setName = function (name) {
            this.name = name;
        };

        Window.prototype.setAttributeList = function (attributeList) {
            this.attributeList = attributeList;
        };

        Window.prototype.setType = function (type) {
            this.type = type;
        };

        Window.prototype.setParameters = function (parameters) {
            this.parameters = parameters;
        };

        Window.prototype.setOutputEventType = function (outputEventType) {
            this.outputEventType = outputEventType.toUpperCase();
        };

        Window.prototype.setAnnotationList = function (annotationList) {
            this.annotationList = annotationList;
        };

        /**
         * @class AggregateByTimePeriod
         * @constructor
         * @class AggregateByTimePeriod  Creates a AggregateByTimePeriod in aggregation definition select section
         * @param {Object} options Rendering options for the view
         */
        var AggregateByTimePeriod = function (options) {
            /*
             Data storing structure as follows
             type*: 'RANGE',
             value*: {
             min*: '',
             max*: ''
             }
             << or >>
             type*: 'INTERVAL',
             value*: ['seconds', 'minutes', ...] // At least one value must be available
             */
            if (options !== undefined) {
                this.type = (options.type !== undefined) ? (options.type).toUpperCase() : undefined;
                this.value = options.value;
            }
        };

        AggregateByTimePeriod.prototype.getType = function () {
            return this.type.toUpperCase();
        };

        AggregateByTimePeriod.prototype.getValue = function () {
            return this.value;
        };

        AggregateByTimePeriod.prototype.setType = function (type) {
            this.type = type.toUpperCase();
        };

        AggregateByTimePeriod.prototype.setValue = function (value) {
            this.value = value;
        };

        /**
         * @class Attribute
         * @constructor
         * @class Attribute  Creates an object to hold an attribute
         * @param {Object} options Rendering options for the view
         */
        var Attribute = function (options) {
            /*
             Data storing structure as follows
             {
             name: ‘’,
             type: ‘’
             }
             */
            if (options !== undefined) {
                this.name = options.name;
                this.type = (options.type !== undefined) ? (options.type).toUpperCase() : undefined;
            }
        };

        Attribute.prototype.getName = function () {
            return this.name;
        };

        Attribute.prototype.getType = function () {
            return this.type;
        };

        Attribute.prototype.setName = function (name) {
            this.define = name;
        };

        Attribute.prototype.setType = function (type) {
            this.type = type.toUpperCase();
        };


        return {
            "stream": Stream,
            "table": Table,
            "window" : Window,
            "aggregation": Aggregation,
            "trigger" : Trigger,
            "aggregateByTimePeriod" : AggregateByTimePeriod,
            "functionDefinition" : FunctionDefinition,
            "attribute" : Attribute
        };

    });