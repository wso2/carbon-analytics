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

define(
    function () {

        /**
         * @class SourceOrSinkAnnotation
         * @constructor
         * @class SourceOrSinkAnnotation  Creates a Source Or Sink Annotation
         * @param {Object} options Rendering options for the view
         */
        var SourceOrSinkAnnotation = function (options) {
            /*
             Data storing structure as follows
                id*: ‘’,
                annotationType*: 'SOURCE | SINK',
                type*: ‘’,
                options: ['option1', 'option2=value2',...],
                map: {
                    type*: ‘’,
                    options: ['option1', 'option2=value2',...],
                    attributes: {
                        type*: ‘MAP’
                        value*: {Key-Value Pair JSON}
                    }
                    << or >>
                    attributes: {
                        type*: ‘LIST’
                        value*: ['value1',...]
                    }
                }
            */
            if (options !== undefined) {
                this.id = options.id;
                this.annotationType
                    = (options.annotationType !== undefined)? (options.annotationType).toUpperCase() : undefined;
                this.type = options.type;
                this.options = options.options;
                this.map = options.map;
            }
        };

        SourceOrSinkAnnotation.prototype.getId = function () {
            return this.id;
        };

        SourceOrSinkAnnotation.prototype.getAnnotationType = function () {
            return this.annotationType;
        };

        SourceOrSinkAnnotation.prototype.getType = function () {
            return this.type;
        };

        SourceOrSinkAnnotation.prototype.getOptions = function () {
            return this.options;
        };

        SourceOrSinkAnnotation.prototype.getMap = function () {
            return this.map;
        };

        SourceOrSinkAnnotation.prototype.setId = function (id) {
            this.id = id;
        };

        SourceOrSinkAnnotation.prototype.setAnnotationType = function (annotationType) {
            this.annotationType = annotationType;
        };

        SourceOrSinkAnnotation.prototype.setType = function (type) {
            this.type = type;
        };

        SourceOrSinkAnnotation.prototype.setOptions = function (options) {
            this.options = options;
        };

        SourceOrSinkAnnotation.prototype.setMap = function (map) {
            this.map = map;
        };

        return SourceOrSinkAnnotation;

    });
