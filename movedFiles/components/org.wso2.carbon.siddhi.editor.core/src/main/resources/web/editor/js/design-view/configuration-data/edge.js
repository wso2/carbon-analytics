/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(
    function () {

        /**
         * @class Edge
         * @constructor
         * @class Edge  Creates an Edge
         * @param {Object} options Rendering options for the view
         */
        var Edge = function (options) {
            /*
             Data storing structure as follows
                id: '',  ex: 'parentId_childId'
                parentId: '',
                parentType: '',
                childId: '',
                childType: '',
            */
            if (options !== undefined) {
                this.id = options.id;
                this.parentId = options.parentId;
                this.parentType = options.parentType;
                this.childId = options.childId;
                this.childType = options.childType;
                this.fromFaultStream = options.fromFaultStream;
            }
        };

        Edge.prototype.getId = function () {
            return this.id;
        };

        Edge.prototype.getParentId = function () {
            return this.parentId;
        };

        Edge.prototype.getParentType = function () {
            return this.parentType;
        };

        Edge.prototype.getChildId = function () {
            return this.childId;
        };

        Edge.prototype.getChildType = function () {
            return this.childType;
        };

        Edge.prototype.isFromFaultStream = function() {
            return this.fromFaultStream;
        };

        Edge.prototype.setId = function (id) {
            this.id = id;
        };

        Edge.prototype.setParentId = function (parentId) {
            this.parentId = parentId;
        };

        Edge.prototype.setParentType = function (parentType) {
            this.parentType = parentType;
        };

        Edge.prototype.setChildId = function (childId) {
            this.childId = childId;
        };

        Edge.prototype.setChildType = function (childType) {
            this.childType = childType;
        };

        Edge.prototype.setFromFaultStream = function(fromFaultStream) {
            this.fromFaultStream = fromFaultStream;
        };

        return Edge;

    });
