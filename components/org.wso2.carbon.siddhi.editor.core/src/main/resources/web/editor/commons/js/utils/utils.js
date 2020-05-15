/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['require', 'jquery', 'constants'],
    function (require, $, Constants) {
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

        var global_constants = {
            VIEW_ETL_FLOW_WIZARD: "etl-wizard-view"
        };

        Utils.prototype.getGlobalConstnts = function () {
            return global_constants;
        };

        /**
         * Installs or un-installs an extension.
         *
         * @param extension             Extension object.
         * @param app                   Reference of the app.
         * @param handleLoading         Function which handles the 'loading' state of the installation.
         * @param handleCallback        Callback function after a successful installation.
         * @param callerObject          The object which calls this method.
         * @param requestedActionText   The requested action, i.e: either install or un-install.
         * @param callerScope           Scope of the caller.
         */
        Utils.prototype.installOrUnInstallExtension = function (extension,
                                                                app,
                                                                handleLoading,
                                                                handleCallback,
                                                                callerObject,
                                                                requestedActionText,
                                                                callerScope) {
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
                "<label for='configName' class='col-sm-9 file-dialog-label'>" + "Are you sure you want to " +
                requestedActionText.toLowerCase() + " <b>" + extension.extensionInfo.name + "</b>?" +
                "</label>" +
                "</div>" +
                "<div class='form-group'>" +
                "<div class='file-dialog-form-btn'>" +
                "<button id='installUninstallId' type='button' class='btn btn-primary'>" +
                requestedActionText + "</button>" +
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
                var action = requestedActionText;

                if (action === Constants.INSTALL) {
                    self.performInstallOrUnInstall(
                        extension, app, handleLoading, handleCallback, callerObject, action, callerScope);
                } else if (action === Constants.UNINSTALL) {
                    var serviceUrl = app.config.services.extensionsInstallation.endpoint;
                    var getDependencySharingExtensionsUrl =
                        serviceUrl + "/" + extension.extensionInfo.name + "/dependency-sharing-extensions";
                    $.ajax({
                        type: "GET",
                        contentType: "json",
                        url: getDependencySharingExtensionsUrl,
                        async: false,
                        success: function (response) {
                            if (response.doesShareDependencies) {
                                // Some dependencies are shared. Proceed or not based on user's selection.
                                self.performDependencySharingUnInstallation(
                                    response.sharesWith,
                                    extension,
                                    app,
                                    handleLoading,
                                    handleCallback,
                                    callerObject,
                                    action,
                                    callerScope);
                            } else {
                                // No dependencies are shared. Proceed with un-installation.
                                self.performInstallOrUnInstall(
                                    extension, app, handleLoading, handleCallback, callerObject, action, callerScope);
                            }
                        },
                        error: function (e) {
                            var message = 'Unable to get dependency sharing information for the extension. ' +
                                'Please check editor console for further information.';
                            alerts.error(message);
                            throw message;
                        }
                    });
                }
            });
        };

        /**
         * Displays a popup with the information about extensions - that share dependencies with the given
         * extension (if any), during un-installation.
         * Un-installation will be continued or not, respectively when the user confirms or cancels the popup.
         *
         * @param sharesWith        Information about dependency sharing extensions, obtained from the response.
         * @param extension         Configuration of an extension.
         * @param app               Reference of the app.
         * @param handleLoading     Function which handles the 'loading' state of the installation.
         * @param handleCallback    Callback function after a successful installation.
         * @param callerObject      The object which calls this method.
         * @param action            The requested action, i.e: either install or un-install.
         * @param callerScope       Scope of the caller.
         */
        this.performDependencySharingUnInstallation = function (sharesWith,
                                                                extension,
                                                                app,
                                                                handleLoading,
                                                                handleCallback,
                                                                callerObject,
                                                                action,
                                                                callerScope) {
            var extensionListElements = "";
            for (var extensionName in sharesWith) {
                var dependencies = "";
                if (sharesWith.hasOwnProperty(extensionName)) {
                    var sharedDependencies = sharesWith[extensionName];
                    sharedDependencies.forEach(function(dependency) {
                        dependencies += `<li>&nbsp;${dependency.name} ${dependency.version}</li>`;
                    });
                }
                extensionListElements += `<li><b>${extensionName}</b><ul>${dependencies}</ul></li><br/>`;
            }

            self.extensionInstallUninstallAlertModal = $(
                "<div class='modal fade' id='extensionAlertModal' tabindex='-1' role='dialog'" +
                " aria-tydden='true'>" +
                "<div class='modal-dialog file-dialog' role='document'>" +
                "<div class='modal-content'>" +
                "<div class='modal-header'>" +
                "<button type='button' class='close' data-dismiss='modal' aria-label='Close'>" +
                "<i class='fw fw-cancel about-dialog-close'> </i> " +
                "</button>" +
                "<h4 class='modal-title file-dialog-title' id='newConfigModalLabel'>Shared Dependencies Exist</h4>" +
                "<hr class='style1'>" +
                "</div>" +
                "<div class='modal-body'>" +
                "<div class='container-fluid'>" +
                "<form class='form-horizontal' onsubmit='return false'>" +
                "<div class='form-group'>" +
                "<label for='configName' class='col-sm-9 file-dialog-label'>" +
                "The extension shares some of its dependencies with the following extensions. " +
                "Are you sure you want to un-install?" +
                "</label>" +
                "<label for='configName' class='col-sm-9 file-dialog-label'>" +
                `<ul>${extensionListElements}</ul>` +
                "</label>" +
                "</div>" +
                "<div class='form-group'>" +
                "<div class='file-dialog-form-btn'>" +
                "<button id='confirmUnInstall' type='button' class='btn btn-primary'>Confirm</button>" +
                "<div class='divider'/>" +
                "<button type='cancel' class='btn btn-default' data-dismiss='modal'>Cancel</button>" +
                "</div>" +
                "</form>" +
                "</div>" +
                "</div>" +
                "</div>" +
                "</div>" +
                "</div>"
            ).modal('show');

            // Confirm un-installation.
            self.extensionInstallUninstallAlertModal.find("button").filter("#confirmUnInstall").click(function () {
                self.extensionInstallUninstallAlertModal.modal('hide');
                self.performInstallOrUnInstall(
                    extension, app, handleLoading, handleCallback, callerObject, action, callerScope);
            });
        };

        this.performInstallOrUnInstall = function (extension,
                                                   app,
                                                   handleLoading,
                                                   handleCallback,
                                                   callerObject,
                                                   action,
                                                   callerScope) {
            // Wait until installation completes.
            if (handleLoading) {
                var actionStatus = action + 'ing';
                actionStatus = actionStatus.charAt(0).toUpperCase() + actionStatus.substr(1).toLowerCase();
                handleLoading(callerObject, extension, actionStatus, callerScope);
            }

            var serviceUrl = app.config.services.extensionsInstallation.endpoint;
            var installUninstallUrl = serviceUrl + "/" + extension.extensionInfo.name;
            var requestType;
            if (action.toLowerCase() === 'install') {
                requestType = 'POST';
            } else if (action.toLowerCase() === 'uninstall') {
                requestType = 'DELETE';
            }

            $.ajax({
                type: requestType,
                contentType: "json",
                url: installUninstallUrl,
                async: true,
                success: function (response) {
                    handleCallback(extension, response.status, callerObject, response, callerScope);
                    app.utils.extensionStatusListener.markAsRestartRequired(extension);
                    self.displayRestartPopup(extension, action);
                },
                error: function (e) {
                    var message = `Unable to ${action.toLowerCase()} the extension. ` +
                        `Please check editor console for further information.`;
                    alerts.error(message);
                    throw message;
                }
            });
        };

        /**
         * Displays a popup that instructs to restart the editor.
         *
         * @param extension
         * @param action
         */
        this.displayRestartPopup = function (extension, action) {
            self.extensionInstallUninstallAlertModal = $(
                "<div class='modal fade' id='extensionAlertModal' tabindex='-1' role='dialog'" +
                " aria-tydden='true'>" +
                "<div class='modal-dialog file-dialog' role='document'>" +
                "<div class='modal-content'>" +
                "<div class='modal-header'>" +
                "<button type='button' class='close' data-dismiss='modal' aria-label='Close'>" +
                "<i class='fw fw-cancel about-dialog-close'> </i> " +
                "</button>" +
                "<h4 class='modal-title file-dialog-title' id='newConfigModalLabel'>Restart Required</h4>" +
                "<hr class='style1'>" +
                "</div>" +
                "<div class='modal-body'>" +
                "<div class='container-fluid'>" +
                "<form class='form-horizontal' onsubmit='return false'>" +
                "<div class='form-group'>" +
                "<label for='configName' class='col-sm-9 file-dialog-label'>" +
                `Extension was successfully ${action.toLowerCase()}ed. Please restart the editor.` +
                "</label>" +
                "</div>" +
                "<div class='form-group'>" +
                "<div class='file-dialog-form-btn'>" +
                "<button type='cancelButton' class='btn btn-primary' data-dismiss='modal'>Close</button>" +
                "</div>" +
                "</form>" +
                "</div>" +
                "</div>" +
                "</div>" +
                "</div>" +
                "</div>"
            ).modal('show');
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
