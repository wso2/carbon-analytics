/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

define(['require', 'jquery', 'lodash', 'log', 'alerts', 'csvMapper', 'xmlMapper', 'jsonMapper'],

    function (require, $, _, log, Alerts, CSVMapper, XMLMapper, JSONMapper ) {

        var SourceSinkMapper = function (sourceOrSinkType, container, config) {
            this.__sourceOrSinkType = sourceOrSinkType;
            this.__container = container;
            this.__config = config;

        }

        SourceSinkMapper.prototype.constructor = SourceSinkMapper;

        SourceSinkMapper.prototype.render  = function () {
            var mapper = null;

            switch (this.__config.mapping.type) {
                case 'csv':
                    mapper = new CSVMapper(this.__sourceOrSinkType, this.__container, this.__config);
                    break;
                case 'xml':
                    mapper = new XMLMapper(this.__sourceOrSinkType, this.__container, this.__config);
                    break;
                case 'json':
                    mapper = new JSONMapper(this.__sourceOrSinkType, this.__container, this.__config);
                    break;
            }

            if(mapper !== null) {
                mapper.render();
            }
        }

        return SourceSinkMapper;
    });
