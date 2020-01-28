/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['require', 'jquery','constants'],
    function (require, $,Constants) {
        var self = this;
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

        /**
         * Get the extension details array from back end.
         */
        Utils.prototype.getExtensionDetails = function () {
            // extension details array need to be retrieve from backend.
            return [{name: "ex1", status: "installed"},
                {name: "ex2fgdfgdfg", status: "installed"},
                {
                    name: "ex7tyjyjyukuu", status: "partially-installed",
                    info: {
                        description: "this ex7 extension gives the string conversion features to Siddhi" +
                            " app",
                        install: "To install this ex7 extension you have to set  all dependency of it."
                    }
                },
                {name: "grpc", status: "not-installed"},
                {
                    name: "ex4aerertrtrt", status: "partially-installed",
                    info: {
                        description: " this ex4 extension gives the string conversion features to Siddhi" +
                            " app.This ex4 extension gives the string conversion features to Siddhi" + " this ex4 extension gives the string conversion features to Siddhi" +
                            " app.This ex4 extension gives the string conversion features to Siddhi" + " this ex4 extension gives the string conversion features to Siddhi" +
                            " app.This ex4 extension gives the string conversion features to Siddhi" + " this ex4 extension gives the string conversion features to Siddhi" +
                            " app.This ex4 extension gives the string conversion features to Siddhi" +
                            " app.This ex4 extension gives the string conversion features to Siddhi" + " this ex4 extension gives the string conversion features to Siddhi" +
                            " app.This ex4 extension gives the string conversion features to Siddhi" + " this ex4 extension gives the string conversion features to Siddhi" +
                            " app.This ex4 extension gives the string conversion features to Siddhi" + " this ex4 extension gives the string conversion features to Siddhi" +
                            " app.This ex4 extension gives the string conversion features to Siddhi"
                        ,
                        install: "To install this ex4 extension you have to set  all dependency of it" +
                            " app.This ex4 extension gives the string conversion features to Siddhi" + " this ex4 extension gives the string conversion features to Siddhi" +
                            " app.This ex4 extension gives the string conversion features to Siddhi" + " this ex4 extension gives the string conversion features to Siddhi" +
                            " app.This ex4 extension gives the string conversion features to Siddhi" + " this ex4 extension gives the string conversion features to Siddhi" +
                            " app.This ex4 extension gives the string conversion features to Siddhi" +
                            " app.This ex4 extension gives the string conversion features to Siddhi" + " this ex4 extension gives the string conversion features to Siddhi" +
                            " app.This ex4 extension gives the string conversion features to Siddhi" + " this ex4 extension gives the string conversion features to Siddhi" +
                            " app.This ex4 extension gives the string conversion features to Siddhi" + " this ex4 extension gives the string conversion features to Siddhi" +
                            " app.This ex4 extension gives the string conversion features to Siddhi"
                        ,
                    }
                },
                {name: "kafka", status: "not-installed"},
                {
                    name: "ex6tyjyjyukuu", status: "partially-installed",
                    info: {
                        description: "this ex6 extension gives the string conversion features to Siddhi" +
                            " app",
                        install: "To install this ex6 extension you have to set  all dependency of it."
                    }
                },
            ];
        };

        /**
         * provide the update details about the extension
         * @param extension
         */
        Utils.prototype.extensionUpdate = function (extension) {
            self.extensionInstallUninstallAlertModal = $(
                "<div class='modal fade' id='extensionAlertModal' tabindex='-1' role='dialog'" +
                " aria-tydden='true'>" +
                "<div class='modal-dialog file-dialog' role='document'>" +
                "<div class='modal-content'>" +
                "<div class='modal-header'>" +
                "<button type='button' class='close' data-dismiss='modal' aria-label='Close'>" +
                "<i class='fw fw-cancel about-dialog-close'> </i> " +
                "</button>" +
                "<h4 class='modal-title file-dialog-title' id='newConfigModalLabel'>Confirmation<" +
                "/h4>" +
                "<hr class='style1'>" +
                "</div>" +
                "<div class='modal-body'>" +
                "<div class='container-fluid'>" +
                "<form class='form-horizontal' onsubmit='return false'>" +
                "<div class='form-group'>" +
                "<label for='configName' class='col-sm-9 file-dialog-label'>" +
                "Are you sure to " + ((extension.status === "not-installed") ? 'install' : 'unInstall') + " " + extension.name + " ?" +
                "</label>" +
                "</div>" +
                "<div class='form-group'>" +
                "<div class='file-dialog-form-btn'>" +
                "<button id='installUninstallId' type='button' class='btn btn-primary'>" + ((extension.status === Constants.EXTENSION_NOT_INSTALLED) ? 'install' : 'unInstall') +
                "</button>" +
                "<div class='divider'/>" +
                "<button type='cancelButton' class='btn btn-default' data-dismiss='modal'>cancel</button>" +
                "</div>" +
                "</form>" +
                "</div>" +
                "</div>" +
                "</div>" +
                "</div>" +
                "</div>"
            ).modal('show');

            self.extensionInstallUninstallAlertModal.find("button").filter("#installUninstallId").click(function () {
                self.extensionInstallUninstallAlertModal.modal('hide');
                var updateData = {
                    "name": extension.name,
                    "action": (extension.status === Constants.EXTENSION_NOT_INSTALLED) ? 'install' : 'unInstall'
                };
                alert(updateData.name + " " + updateData.action);
                //this updateData goes to back end.
            });
        };

        Utils.prototype.retrieveSiddhiAppNames = function (successCallback, errorCallback, context) {
            $.ajax({
                async: true,
                url: rest_client_constants.editorUrl + "/artifact/listSiddhiApps",
                type: rest_client_constants.HTTP_GET,
                success: function (data) {
                    if (typeof successCallback === 'function')
                        successCallback(data, context)
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

        Utils.prototype.retrieveEnvVariables = function () {
            var variableMap = {};
            var localVarObj = JSON.parse(localStorage.getItem("templatedAttributeList"));
            Object.keys(localVarObj).forEach(function(key, index, array) {
                var name = extractPlaceHolderName(key);
                if (localVarObj[key] !== undefined   && localVarObj[key] !== '') {
                    variableMap[name] = localVarObj[key];
                }
            });
            return variableMap;
        };

        function extractPlaceHolderName(name) {
            var textMatch = name.match("\\$\\{(.*?)\\}");
            if (textMatch) {
                return textMatch[1];
            } else {
                return '';
            }
        }

        return Utils;
    });
