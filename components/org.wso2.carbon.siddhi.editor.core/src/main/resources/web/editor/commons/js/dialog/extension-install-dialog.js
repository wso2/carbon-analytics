/**
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
define(['require', 'lodash', 'jquery','constants'],
    function (require, _, $,Constants) {
        return function (app) {

            /**
             * initialize function for ExtensionInstallDialog.
             */
            this.initialize = function (options) {
                this.dialog_containers = $(_.get(options.config.dialog, 'container'));
                this.extensionList = app.utils.getExtensionDetails();
            },
                /**
                 * show function for display the ExtensionInstallDialog.
                 */
                this.show = function () {
                    this._extensionListModal.modal('show');
                },

                /**
                 * render function for rendering all the contents of ExtensionInstallDialog.
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
                        "<h4 class='modal-title file-dialog-title'>Extension Details</h4>" +
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
                        "<div class='file-dialog-form-scrollable-block' style='padding:10px4px; margin-left:35px;'>" +
                        "<div id='noResults' style='display:none;'>No extension has found</div>" +
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


                    //extension array from backend which has details about extensions.
                    var extensionLists = app.utils.getExtensionDetails();
                    var extensionTable = $('<table class="table table-hover data-table"' +
                        ' id="extensionTableId"><tbody></tbody></table>');
                    //define the map to store Partially extension modal based on key
                    var partialExtensionDetailModal = new Map();
                    extensionLists.forEach(function (extension) {
                        var extensionTableBodyData;
                        if (extension.status.trim().toLowerCase() === Constants.EXTENSION_INSTALLED) {
                            extensionTableBodyData = $('<tr><td>' + extension.name + '</td><td>Installed</td><td><button' +
                                ' class="btn btn-block btn' +
                                ' btn-primary">UnInstall</button></td></tr>');
                            extensionTableBodyData.find("button").click(function () {
                                app.utils.extensionUpdate(extension);
                            });
                        } else if (extension.status.trim().toLowerCase() === Constants.EXTENSION_PARTIALLY_INSTALLED) {
                            var partialExtensionIndex = extensionLists.indexOf(extension);
                            extensionTableBodyData = $('<tr><td>' + extension.name + '</td><td>Partially-Installed' +
                                '&nbsp; &nbsp;<a data-toggle="modal"' +
                                ' id="' + partialExtensionIndex + '"><i class="fw' +
                                ' fw-info"></i></a></td><td><button' +
                                ' class="btn btn-block btn' +
                                ' btn-primary">UnInstall</button></td></tr>');

                            var partialModel = $(
                                '<div class="modal fade" id="' + partialExtensionIndex + '">' +
                                '<div class="modal-dialog">' +
                                '<div class="modal-content">' +
                                '<div class="modal-header">' +
                                "<button type='button' class='close' data-dismiss='modal' aria-label='Close'>" +
                                "<i class=\"fw fw-cancel  about-dialog-close\"></i>" +
                                "</button>" +
                                '<h3 class="modal-title file-dialog-title" id="partialExtenName">'
                                + extension.name +
                                '</h3>' +
                                '<hr class="style1">' +
                                '</div>' +
                                '<div class="modal-body">' +
                                '<h3>Description</h3>' +
                                '<div id="partialExtenDescription" style = "text-align:justify">'
                                + extension.info.description +
                                '</div>' +
                                '<h3>Install</h3>' +
                                '<div id="partialExtenInstall" style = "text-align:justify" >'
                                + extension.info.install +
                                '</div>' +
                                '</div>' +
                                '<div class="modal-footer">' +
                                '<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>' +
                                '</div>' +
                                '</div>' +
                                '</div>' +
                                '</div>');

                            partialExtensionDetailModal.set(partialExtensionIndex, partialModel);

                            extensionTableBodyData.find("a").filter("#" + partialExtensionIndex).click(function () {
                                extensionPartialModelDisplay(partialExtensionDetailModal.get(partialExtensionIndex));
                            });

                            extensionTableBodyData.find("button").click(function () {
                                app.utils.extensionUpdate(extension);
                            });

                        } else if (extension.status.trim().toLowerCase() === Constants.EXTENSION_NOT_INSTALLED) {
                            extensionTableBodyData = $('<tr><td>' + extension.name + '</td><td>Not-Installed</td><td><button' +
                                ' class="btn btn-block btn' +
                                ' btn-primary">Install</button></td></tr>');
                            extensionTableBodyData.find("button").click(function () {
                                app.utils.extensionUpdate(extension);
                            });
                        }
                        extensionTable.append(extensionTableBodyData);
                    });

                    extensionContainer.append(extensionTable);
                    extensionSearch.keyup(function () {
                        searchExtension(extensionTable, extensionSearch.val());
                    });

                    $(this.dialog_containers).append(extensionModelOpen);
                    extensionInstallError.hide();
                    this._extensionListModal = extensionModelOpen;

                    /**
                     * search function for seek the extensions.
                     * @param extensionTable
                     * @param locationSearch
                     */
                    function searchExtension(extensionTable, locationSearch) {
                        var unmatchedCount = 0, filter, table, tr, td, i, txtValue;
                        var noResultsElement = extensionModelOpen.find("div").filter("#noResults");
                        filter = locationSearch.toUpperCase();
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
                    }

                    /**
                     * display the inner modal box for the partially installed extension.
                     * @param extensionPartialModel
                     */
                    function extensionPartialModelDisplay(extensionPartialModel) {
                        self._extensionPartialModel = extensionPartialModel;
                        self._extensionPartialModel.modal('show');
                    }
                }
        };
    });