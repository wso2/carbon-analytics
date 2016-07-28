/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
var getConfig, validate, getMode, getSchema, getData;

(function () {

    var log = new Log();
    var constants = require("/modules/constants.js");
    var restClientConstants = require("constants.js");
    var restClientUtil = require("restClientUtil.jag").restClientUtil;
    var typeMap = {
        "bool": "ordinal",
        "boolean": "ordinal",
        "string": "ordinal",
        "int": "linear",
        "integer": "linear",
        "long": "linear",
        "double": "linear",
        "float": "linear",
        "time": "time"
    };

    /**
     * require the existing config.json and push any dynamic fields that needs to be populated in the UI
     */
    getConfig = function () {
        var formConfig = require(restClientConstants.PROVIDERS_LOCATION + '/' + restClientConstants.PROVIDER_NAME
            + '/' + restClientConstants.GADGET_CONFIG_JSON + '');
        var authorizationMethodsConfig = formConfig.config[1];
        var authorizationMethods = [restClientConstants.NO_AUTH, restClientConstants.BASIC_AUTH
            , restClientConstants.OAUTH2_PASSWORD_GRANT_TYPE, restClientConstants.OAUTH2_CLIENT_CREDENTIALS_GRANT_TYPE];
        try {
            authorizationMethodsConfig[restClientConstants.GADGET_CONFIGURATION] = authorizationMethods;
        } catch (e) {
            log.error(e);
        }
        return formConfig;
    }

    /**
     * validate the user input of provider configuration
     * @param providerConfig
     */
    validate = function (providerConfig) {
        return true;
    }

    /**
     * returns the data mode either push or pull
     */
    getMode = function () {
        return "PULL";
    }

    /**
     * returns an array of column names & types
     * @param providerConfig
     */
    getSchema = function (providerConfig) {
        var url;
        try {
            if (providerConfig[restClientConstants.SCHEMA_RETRIEVE_URL]) {
                url = providerConfig[restClientConstants.SCHEMA_RETRIEVE_URL];
                var xmlHttpRequest = new XMLHttpRequest();
                xmlHttpRequest.open(constants.HTTP_GET, url);
                setHeaders(xmlHttpRequest, function (modifedXMLHttpRequest) {
                    xmlHttpRequest = modifedXMLHttpRequest;
                });
                xmlHttpRequest.send();
                var schema = [];
                if (xmlHttpRequest.status == constants.HTTP_ACCEPTED) {
                    var responseMessage = parse(xmlHttpRequest.responseText)[restClientConstants.RESPONSE_CONTENT];
                    var columns = responseMessage.columns;
                    if (columns) {
                        Object.getOwnPropertyNames(columns).forEach(function (name, idx, array) {
                            schema.push({
                                fieldName: name,
                                fieldType: typeMap[columns[name]['type'].toLowerCase()]
                            });
                        });
                    }
                    return schema;
                } else if (xmlHttpRequest.status == constants.HTTP_UNAUTHORIZED) {
                    schema = [];
                    restClientUtil.refreshAccessToken(providerConfig, function (responseStatus) {
                        if (responseStatus == constants.HTTP_ACCEPTED) {
                            var xmlHttpRequest = new XMLHttpRequest();
                            xmlHttpRequest.open(constants.HTTP_GET, url);
                            setHeaders(xmlHttpRequest, function (modifedXMLHttpRequest) {
                                xmlHttpRequest = modifedXMLHttpRequest;
                            });
                            xmlHttpRequest.send();
                            var responseMessage
                                = parse(xmlHttpRequest.responseText)[restClientConstants.RESPONSE_CONTENT];
                            var columns = responseMessage.columns;
                            if (columns) {
                                Object.getOwnPropertyNames(columns).forEach(function (name, idx, array) {
                                    schema.push({
                                        fieldName: name,
                                        fieldType: typeMap[columns[name]['type'].toLowerCase()]
                                    });
                                });
                            }
                        } else {
                            log.error("Error while requesting schema from backend server(" + url + ") , "
                                + xmlHttpRequest.responseText);
                        }
                    });
                    return schema;
                }
            } else {
                log.error("Schema retrieval URL hasn't been specified yet");
            }
        } catch (error) {
            log.error("Error while requesting schema from backend server(" + url + ") , " + error);
            return [];
        }
    };

    /**
     * returns the actual data
     * @param providerConfig
     * @param limit
     */
    getData = function (providerConfig, limit) {
        var url;
        try {
            if (providerConfig[restClientConstants.DATA_RETRIEVE_URL]) {
                url = providerConfig[restClientConstants.DATA_RETRIEVE_URL];
                var xmlHttpRequest = new XMLHttpRequest();
                xmlHttpRequest.open(constants.HTTP_GET, url);
                setHeaders(xmlHttpRequest, function (modifedXMLHttpRequest) {
                    xmlHttpRequest = modifedXMLHttpRequest;
                });
                xmlHttpRequest.send();
                if (xmlHttpRequest.status == constants.HTTP_ACCEPTED) {
                    var dataSet = parse(xmlHttpRequest.responseText)[restClientConstants.RESPONSE_CONTENT];
                    if (!dataSet.length || dataSet.length == 0) {
                        dataSet = [];
                    }
                    return dataSet;
                } else if (xmlHttpRequest.status == constants.HTTP_UNAUTHORIZED) {
                    restClientUtil.refreshAccessToken(providerConfig, function (responseStatus) {
                        if (responseStatus == constants.HTTP_ACCEPTED) {
                            var xmlHttpRequest = new XMLHttpRequest();
                            xmlHttpRequest.open(constants.HTTP_GET, url);
                            setHeaders(xmlHttpRequest, function (modifedXMLHttpRequest) {
                                xmlHttpRequest = modifedXMLHttpRequest;
                            });
                            xmlHttpRequest.send();
                            var dataSet = parse(xmlHttpRequest.responseText)[restClientConstants.RESPONSE_CONTENT];
                            if (!dataSet.length || dataSet.length == 0) {
                                dataSet = [];
                            }
                            return dataSet;
                        } else {
                            log.error("Error while requesting schema from backend server(" + url + ") , "
                                + xmlHttpRequest.responseText);
                        }
                    });
                    log.error("Error while requesting data from backend server(" + url + ") , "
                        + xmlHttpRequest.responseText);
                }
            } else {
                log.error("Data retrieval URL hasn't been specified yet");
            }
        } catch (error) {
            log.error("Error while requesting data from backend server(" + url + ") , " + error);
        }
    };

    function setHeaders(xmlHttpRequest, callBack) {
        xmlHttpRequest.setRequestHeader(constants.CONTENT_TYPE_IDENTIFIER, constants.APPLICATION_JSON);
        xmlHttpRequest.setRequestHeader(constants.ACCEPT_IDENTIFIER, constants.APPLICATION_JSON);
        if (session.get(restClientConstants.REMOTE_SERVER_CREDENTIALS) != null) {
            xmlHttpRequest.setRequestHeader(constants.AUTHORIZATION_HEADER
                , session.get(restClientConstants.REMOTE_SERVER_CREDENTIALS));
        }
        callBack(xmlHttpRequest);
    }
}());
