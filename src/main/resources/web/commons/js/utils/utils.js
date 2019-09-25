/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
define(['require', 'jquery'],
    function (require, $) {
        var Utils = function () {
        };

        var rest_client_constants = {
            HTTP_GET: "GET",
            HTTP_POST: "POST",
            HTTP_PUT: "PUT",
            HTTP_DELETE: "DELETE",
            simulatorUrl: window.location.protocol + "//" + window.location.host + "/simulation",
            editorUrl: window.location.protocol + "//" + window.location.host + '/editor'
        };


        Utils.prototype.retrieveSiddhiAppNames = function (successCallback, errorCallback) {
            $.ajax({
                async: true,
                url: rest_client_constants.editorUrl + "/artifact/listSiddhiApps",
                type: rest_client_constants.HTTP_GET,
                success: function (data) {
                    if (typeof successCallback === 'function')
                        successCallback(data)
                },
                error: function (msg) {
                    if (typeof errorCallback === 'function')
                        errorCallback(msg)
                }
            });
        };

        /**
         * Usage:  encode a string in base64 while converting into unicode.
         * @param {string} str - string value to be encoded
         */
        Utils.prototype.base64EncodeUnicode = function (str) {
            // First we escape the string using encodeURIComponent to get the UTF-8 encoding of the characters,
            // then we convert the percent encodings into raw bytes, and finally feed it to btoa() function.
            var utf8Bytes = encodeURIComponent(str).replace(/%([0-9A-F]{2})/g,
                function(match, p1) {
                    return String.fromCharCode('0x' + p1);
                });

            return btoa(utf8Bytes);
        };

        Utils.prototype.b64DecodeUnicode = function (str) {
            // Going backwards: from bytestream, to percent-encoding, to original string.
            return decodeURIComponent(atob(str).split('').map(function(c) {
                return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
            }).join(''));
        };

        return Utils;
    });
