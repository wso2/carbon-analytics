/**
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
define(['require', 'lodash', 'jquery', 'constants'],
    function (require, _, $, Constants) {
        return function (app) {

            /**
             * initialize function for ExtensionInstallDialog.
             */
            this.initialize = function (options) {
                this.dialog_containers = $(_.get(options.config.dialog, 'container'));
                this.extensionList = app.utils.extensionData;
            },
                /**
                 * Show function to display the ExtensionInstallDialog.
                 */
                this.show = function () {
                    this._extensionListModal.modal('show');
                },

                /**
                 * Renders all extensions.
                 *
                 * @param extensionContainer    Container in which, extensions are rendered.
                 */
                this.renderExtensions = function (extensionContainer) {
                    var self = this;
                    extensionContainer.empty();
                    extensionTable = $('<table class="table table-hover data-table"' + ' id="extensionTableId">' +
                        '<tbody></tbody></table>');
                    app.utils.extensionData.forEach(function (extension, key) {
                        var extensionTableBodyData = self.renderExtension(extension, key, self);
                        extensionTable.append(extensionTableBodyData);
                    });
                    extensionContainer.append(extensionTable);
                },

                /**
                 * Returns whether the given element exists, and is an array, and contains any element.
                 *
                 * @param element       Element which is checked whether a non empty array or not.
                 * @returns {boolean}   Whether the given element is a non empty array.
                 */
                this.isNonEmptyArray = function (element) {
                    if (element) {
                        return Array.isArray(element) && element.length > 0;
                    }
                    return false;
                },

                /**
                 * Returns an object with texts for the installation status and the action button, on initial load.
                 *
                 * @param extensionStatus   Installation status of the extension.
                 * @param manuallyInstall   Manually installable dependencies of the extension.
                 * @param autoDownloadable  Auto downloadable dependencies of the extension.
                 * @returns {{buttonActionText: string, status: string}}
                 */
                this.getInitialStatusTexts = function (extensionStatus, manuallyInstall, autoDownloadable) {
                    var status;
                    var buttonActionText;
                    switch (extensionStatus.trim().toUpperCase()) {
                        case Constants.EXTENSION_INSTALLED:
                            // All the dependencies are installed.
                            status = Constants.EXTENSION_INSTALLED_TEXT;
                            buttonActionText = Constants.UNINSTALL;
                            break;
                        case Constants.EXTENSION_NOT_INSTALLED:
                            // None of the dependencies are installed.
                            status = Constants.EXTENSION_NOT_INSTALLED_TEXT;
                            if (this.isNonEmptyArray(autoDownloadable)) {
                                // Auto downloadable dependencies are available. Allow to try installing them.
                                buttonActionText = Constants.INSTALL;
                            } else {
                                // No auto downloadable dependency is available. Button action not applicable.
                                buttonActionText = null;
                            }
                            break;
                        case Constants.EXTENSION_PARTIALLY_INSTALLED:
                            // Only some of the dependencies are installed.
                            status = Constants.EXTENSION_PARTIALLY_INSTALLED_TEXT;
                            if (this.isNonEmptyArray(manuallyInstall)) {
                                // Partial because manually installable dependencies are available.
                                /*
                                In this case,
                                Installation failure in at least one auto downloadable dependency,
                                will not be available for a retry.
                                */
                                buttonActionText = Constants.UNINSTALL;
                            } else {
                                // Partial because of previous failures in installing dependencies.
                                buttonActionText = Constants.INSTALL;
                            }
                            break;
                        default:
                            status = Constants.EXTENSION_NOT_INSTALLED_TEXT;
                            buttonActionText = Constants.INSTALL;
                    }
                    return {status: status, buttonActionText: buttonActionText};
                },

                /**
                 * Organizes elements and renders a single extension's visual representation.
                 *
                 * @param extension Extension object.
                 * @param key       Unique key of the extension.
                 * @param selfScope Scope of the caller.
                 */
                this.renderExtension = function (extension, key, selfScope) {
                    var status;
                    var buttonActionText;
                    var statuses = this.getInitialStatusTexts(
                        extension.extensionStatus, extension.manuallyInstall, extension.autoDownloadable);
                    status = statuses.status;
                    buttonActionText = statuses.buttonActionText;

                    return this.renderExtensionRow(extension, key, status, buttonActionText, selfScope);
                },

                /**
                 * Gets representation of the extension, that is shown within an extension row.
                 *
                 * @param extension Extension object.
                 */
                this.getExtensionRepresentation = function (extension) {
                    return (`<td class='file-dialog-label'>${extension.extensionInfo.displayName}</td>`);
                };

            /**
             * Renders a single extension's visual representation.
             *
             * @param extension         Extension object.
             * @param key               Unique key of the extension.
             * @param status            Installation status of the extension.
             * @param buttonActionText  The action text which is shown in the button.
             * @param selfScope         Scope of the caller.
             * @returns {*}
             */
            this.renderExtensionRow = function (extension, key, status, buttonActionText, selfScope) {
                var tr;
                if (extension.hasOwnProperty('manuallyInstall')) {
                    if (buttonActionText) {
                        tr = $('<tr key="' + key + '">' +
                            selfScope.getExtensionRepresentation(extension) +
                            '<td id="status" class="file-dialog-label">' + status + '&nbsp; &nbsp;' +
                            '<a data-toggle="modal" id="' + key + '"><i class="fw fw-info"></i></a></td>' +
                            '<td><button' +
                            ' class="btn btn-block btn' +
                            ' btn-primary">' + buttonActionText + '</button></td></tr>');
                        selfScope.createManuallyInstallableExtensionModal(extension, key, tr, selfScope);
                        selfScope.bindInstallationButtonAction(tr, extension, selfScope.handleInstallationCallback);
                    } else {
                        tr = $('<tr key="' + key + '">' + selfScope.getExtensionRepresentation(extension) +
                            '<td id="status" class="file-dialog-label">' + status +
                            '&nbsp; &nbsp;<a data-toggle="modal"' +
                            ' id="' + key + '"><i class="fw fw-info"></i></a></td><td></td></tr>');
                        selfScope.createManuallyInstallableExtensionModal(extension, key, tr, selfScope);
                    }
                } else {
                    if (buttonActionText) {
                        tr = $('<tr key="' + key + '">' + selfScope.getExtensionRepresentation(extension) +
                            '<td id="status" class="file-dialog-label">' + status +
                            '</td><td><button class="btn btn-block btnn btn-primary">' +
                            buttonActionText + '</button></td></tr>');
                        selfScope.bindInstallationButtonAction(tr, extension, selfScope.handleInstallationCallback);
                    } else {
                        tr = $('<tr key="' + key + '">' + selfScope.getExtensionRepresentation(extension) +
                            '<td id="status" class="file-dialog-label">' + status + '</td><td></td></tr>');
                    }
                }
                return tr;
            },

                /**
                 * Binds the onclick function to the install/un-install button.
                 *
                 * @param extensionRow                  Table row which represents an extension.
                 * @param extension                     Extension object.
                 * @param handleInstallationCallback    Callback function.
                 * @returns {*}
                 */
                this.bindInstallationButtonAction = function (extensionRow, extension, handleInstallationCallback) {
                    var self = this;
                    return extensionRow.find("button").click(function () {
                        app.utils.installOrUnInstallExtension(
                            extension,
                            app,
                            self.handleInstallationLoading,
                            handleInstallationCallback,
                            $(this).parent().parent(),
                            this.innerText,
                            self);
                    });
                },

                /**
                 * Renders an extension's visual representation during 'loading' state.
                 *
                 * @param extension         Extension object.
                 * @param key               Unique key of the extension.
                 * @param actionStatus      The action which is in progress.
                 * @param selfScope         Self scope.
                 * @returns {HTMLElement}
                 */
                this.renderExtensionLoadingRow = function (extension, key, actionStatus, selfScope) {
                    return $('<tr key="' + key + '">' + selfScope.getExtensionRepresentation(extension) +
                        '<td id="status" class="file-dialog-label">' + actionStatus + '</td>' +
                        '<td style="text-align:center"><a class="fw-loader5 fw-spin"></a></td></tr>');
                },

                /**
                 * Triggers the 'loading' state of an extension, during an installation or un-installation.
                 *
                 * @param extensionRow  Table row, which represents an extension.
                 * @param extension     Extension object.
                 * @param actionStatus  The action which is in progress.
                 * @param selfScope     Scope of the caller.
                 */
                this.handleInstallationLoading = function (extensionRow, extension, actionStatus, selfScope) {
                    var key = extensionRow.get(0).getAttribute('key');
                    var newRow = selfScope.renderExtensionLoadingRow(extension, key, actionStatus, selfScope);
                    extensionRow.html(newRow.children());
                },

                /**
                 * The callback, which is triggered after installation/un-installation of an extension.
                 *
                 * @param extension     Extension object.
                 * @param updatedStatus Updated installation status of the extension.
                 * @param extensionRow  Table row, which represents the extension.
                 * @param response      Received response.
                 * @param selfScope     Scope of the caller.
                 */
                this.handleInstallationCallback = function (extension,
                                                            updatedStatus,
                                                            extensionRow,
                                                            response,
                                                            selfScope) {
                    var extensionName = extension.extensionInfo.name;
                    app.utils.extensionData.get(extensionName).extensionStatus = updatedStatus;
                    selfScope.updateExtensionStatus(extension, extensionRow);
                },

                /**
                 * Renders an extension's visual representation after an installation/un-installation,
                 * which informs the user to perform a restart.
                 *
                 * @param extension         Extension object.
                 * @param key               Unique key of the extension.
                 * @returns {HTMLElement}
                 */
                this.renderRestartRequiredExtensionRow = function (extension, key) {
                    var tr = $('<tr key="' + key + '">' + this.getExtensionRepresentation(extension) +
                        '<td id="status" class="file-dialog-label">Restart Required</td><td></td></tr>');
                    return tr;
                },

                /**
                 * Updates the extension's status after an installation or un-installation.
                 *
                 * @param extension     Extension object.
                 * @param extensionRow  Table row, which represents the extension.
                 */
                this.updateExtensionStatus = function (extension, extensionRow) {
                    var key = extensionRow.get(0).getAttribute('key');
                    var newRow = this.renderRestartRequiredExtensionRow(extension, key);
                    extensionRow.html(newRow.children());
                },

                /**
                 * Creates model box for an extension that has manually installable dependencies.
                 *
                 * @param extension                 Extension object.
                 * @param key                       Unique key of the extension.
                 * @param extensionTableBodyData    Table body data for the extension.
                 * @param renderExtensionsScope     The scope, which contains the method to render extensions.
                 */
                this.createManuallyInstallableExtensionModal = function (extension,
                                                                         key,
                                                                         extensionTableBodyData,
                                                                         renderExtensionsScope) {
                    var partialModel = $(
                        '<div class="modal fade" id="' + key + '">' +
                        '<div class="modal-dialog">' +
                        '<div class="modal-content">' +
                        '<div class="modal-header">' +
                        "<button type='button' class='close' data-dismiss='modal' aria-label='Close'>" +
                        "<i class=\"fw fw-cancel  about-dialog-close\"></i>" +
                        "</button>" +
                        '<h2 class="modal-title file-dialog-title" id="partialExtenName">'
                        + extension.extensionInfo.name +
                        '</h2>' +
                        '<hr class="style1">' +
                        '</div>' +
                        '<div id="modalBodyId" class="modal-body">' +
                        '</div>' +
                        '<div class="modal-footer">' +
                        '<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>' +
                        '</div>' +
                        '</div>' +
                        '</div>' +
                        '</div>');

                    var modalBody = partialModel.find("div").filter("#modalBodyId");

                    if (extension.manuallyInstall) {
                        modalBody.append($('<div style="text-align:justify">The following dependencies should be' +
                            ' manually installed.</div>'));
                        extension.manuallyInstall.forEach(function (dependency) {
                            var instructions = dependency.download.instructions ?
                                (`<h4>Instructions</h4><div id="partialExtenDescription" style = "text-align:justify">`
                                    + `${dependency.download.instructions}</div>`) :
                                ('<div id="partialExtenDescription" style="text-align:justify">' +
                                    'No instructions found.</div>');

                            var usages = (`<h4>Installation Locations</h4>` +
                                `<div id="partialExtenDescription" style = "text-align:justify">` +
                                `<ol>${dependency.usages.map(usage =>
                                    `<li>${usage.type.toLowerCase()} in ${usage.usedBy.toLowerCase()}</li>`
                                )}</ol></div>`);

                            modalBody.append($(`<h3>${dependency.name}</h3>` +
                                `<div style="padding-left:10px">${instructions}<br/>${usages}</div>`));
                        });
                    }

                    //define the map to store Partially extension modal based on key
                    var partialExtensionDetailModal = new Map();
                    partialExtensionDetailModal.set(key, partialModel);

                    extensionTableBodyData.find("a").filter("#" + key).click(function () {
                        renderExtensionsScope.displayExtensionManualDownloadInfo(partialExtensionDetailModal.get(key));
                    });
                },

                /**
                 * Displays the inner modal box for a partially installed extension.
                 *
                 * @param extensionPartialModal The modal which represents a partially installed extension.
                 */
                this.displayExtensionManualDownloadInfo = function (extensionPartialModal) {
                    self._extensionPartialModel = extensionPartialModal;
                    self._extensionPartialModel.modal('show');
                },

                /**
                 * Searches and filters extensions.
                 *
                 * @param extensionTable    Table where extensions are rendered.
                 * @param keyword           Search keyword.
                 */
                this.searchExtension = function (extensionModelOpen, extensionTable, keyword) {
                    var unmatchedCount = 0, filter, table, tr, td, i, txtValue;
                    var noResultsElement = extensionModelOpen.find("div").filter("#noResults");
                    filter = keyword.toUpperCase();
                    table = extensionTable[0];
                    tr = table.getElementsByTagName("tr");
                    for (i = 0; i < tr.length; i++) {
                        td = tr[i].getElementsByTagName("td")[0];
                        if (td) {
                            txtValue = td.textContent || td.innerText;
                            if (txtValue.toUpperCase().indexOf(filter) > -1) {
                                tr[i].style.display = "";
                            } else {
                                tr[i].style.display = "none";
                                unmatchedCount += 1;
                            }
                        }
                    }
                    var isMatched = (unmatchedCount === tr.length);
                    noResultsElement.toggle(isMatched);
                },

                /**
                 * Render function for rendering all the contents of ExtensionInstallDialog.
                 */
                this.render = function () {
                    var self = this;
                    if (!_.isNil(this._extensionListModal)) {
                        this._extensionListModal.remove();
                    }

                    var extensionModelOpen = $(
                        "<div class='modal fade' id='extensionInstallConfigModalId' tabindex='-1' role='dialog' " +
                        "aria-hidden='true'>" + "<div class='modal-dialog file-dialog' role='document'>" +
                        "<div class='modal-content' id='sampleDialog'>" +
                        "<div class='modal-header'>" +
                        "<button type='button' class='close' data-dismiss='modal' aria-label='Close'>" +
                        "<i class=\"fw fw-cancel  about-dialog-close\"></i>" +
                        "</button>" +
                        "<h4 class='modal-title file-dialog-title'>Extension Installer</h4>" +
                        "<hr class='style1'>" +
                        "</div>" +
                        "<div class='modal-body'>" +
                        "<div class='container-fluid'>" +
                        "<form class='form-horizontal' onsubmit='return false'>" +
                        "<div class='form-group'>" +
                        "<label for='locationSearch' class='col-sm-2 file-dialog-label'>Search :</label>" +
                        "<input type='text' placeholder='enter the extension name'" +
                        " class='search-file-dialog-form-control'" +
                        " id='extensionSearchId' autofocus>" +
                        "</div>" +
                        "<div class='form-group'>" +
                        "<div class='extension-installer-form-scrollable-block' style='padding:10px4px; margin-left:35px;'>" +
                        "<div id='noResults' style='display:none;'>No extensions found.</div>" +
                        "<div id='extensionTableId' class='samples-pane'>" +
                        "</div>" +
                        "<div id='file-browser-error' class='alert alert-danger' style='display: none;'>" +
                        "</div>" +
                        "</div>" +
                        "</div>" +
                        "<div class='form-group'>" +
                        "<div class='file-dialog-form-btn'>" +
                        "<button type='button' class='btn btn-default' data-dismiss='modal'>cancel</button>" +
                        "</div>" +
                        "</div>" +
                        "</form>" +
                        "<div id='extensionInstallErrorId' class='alert alert-danger'>" +
                        "<strong>Error!</strong>Something went wrong." +
                        "</div>" +
                        "</div>" +
                        "</div>" +
                        "</div>" +
                        "</div>" +
                        "</div>");

                    var extensionInstallConfigModal = extensionModelOpen.filter("#extensionInstallConfigModalId");
                    var extensionInstallError = extensionModelOpen.find("#extensionInstallErrorId");
                    var extensionSearch = extensionModelOpen.find("input").filter("#extensionSearchId");

                    extensionInstallConfigModal.on('shown.bs.modal', function () {
                        extensionSearch.focus();
                    });
                    var extensionContainer = extensionModelOpen.find("div").filter("#extensionTableId");

                    self.renderExtensions(extensionContainer);
                    // var extensionTable;
                    extensionSearch.keyup(function () {
                        self.searchExtension(extensionModelOpen, extensionTable, extensionSearch.val());
                    });
                    $(this.dialog_containers).append(extensionModelOpen);
                    extensionInstallError.hide();
                    this._extensionListModal = extensionModelOpen;
                }
        };
    });