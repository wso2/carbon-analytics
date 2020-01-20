/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['require', 'lodash', 'jquery', 'log', 'file_browser'],
    function (require, _, $, log, FileBrowser) {
        var inMemoryList = [];
        var siddhiApps = [];
        var payload = {
            serverList: [],
            siddhiFileList: []
        };
        if (localStorage.getItem('items')) {
            inMemoryList = JSON.parse(localStorage.getItem('items'));
        } else {
            inMemoryList = [];
        }
        var DeployFileDialog = function (options) {

            this.app = options;
            this.pathSeparator = this.app.getPathSeperator();
        };

        DeployFileDialog.prototype.constructor = DeployFileDialog;

        DeployFileDialog.prototype.render = function () {
            var self = this;
            var fileBrowser;
            var app = this.app;
            if (!_.isNil(this._fileOpenModal)) {
                this._fileOpenModal.remove();
            }
            var fileOpen = $(
                "<div class='modal fade' id='openConfigModal' tabindex='-1' role='dialog' aria-tydden='true'>" +
                "<div class='modal-dialog file-dialog' role='document'>" +
                "<div class='modal-content'>" +
                "<div class='modal-header'>" +
                "<button type='button' class='close' data-dismiss='modal' aria-label='Close'>" +
                "<i class='fw fw-cancel about-dialog-close'>" +
                "</i>" +
                "</button>" +
                "<h4 class='modal-title file-dialog-title'>Deploy Siddhi Apps To Server</h4>" +
                "<hr class='style1'>" +
                "</div>" +
                "<div class='modal-body'>" +
                "<div class='container-fluid'>" +
                "<form class='form-horizontal' onsubmit='return false'>" +
                "<button class='servers' id='siddhi-apps'>Siddhi Apps To Deploy" +
                "</button>" +
                "<div class='vertical-divider'>" +
                "</div>" +
                "<div class='siddhi-app-list' id='siddhi-app-list' style='display: block'>" +
                "<div class='form-group'>" +
                "<div class='file-dialog-form-scrollable-block-list'>" +
                "<div id='fileTree'>" +
                "</div>" +
                "<div id='file-browser-error' class='alert alert-danger' style='display: none;'>" +
                "</div>" +
                "</div>" +
                "</div>" +
                "</div>" +
                "<button class='servers' id='servers'>Servers" +
                "</button>" +
                "<div class='vertical-divider'>" +
                "</div>" +
                "<div class='server-list' id='server-list' style='display: block'>" +
                "<div class='form-group'>" +
                "<div class='file-dialog-form-scrollable-block-list'>" +
                "<div class='server-container' id='server-container'>" + DeployFileDialog.prototype.viewServerList() +
                "</div>" +
                "</div>" +
                "</div>" +
                "</div>" +
                "<div class='new-server' id='new-server'>" +
                "<div class='add-new-server-title'>Add New Server" +
                "</div>" +
                "<div class='vertical-divider'>" +
                "</div>" +
                "<div class='form-group'>" +
                "<div class='file-dialog-form-new-sever-container'>" +
                "<div class='clearfix'>" +
                "<div class='server-property'> <label class='clearfix'> Host </label> <input class='add-new-server-input'" +
                " id='new_host' placeholder='localhost'> </div>" +
                "<div class='server-property'> <label class='clearfix'> HTTPS Port </label> <input class='add-new-server-input'" +
                " id='new_port' placeholder='9443'> </div>" +
                "<div class='server-property'> <label class='clearfix'> User Name </label> <input " +
                "class='add-new-server-input' id='new_user_name' placeholder='admin'> </div>" +
                "<div class='server-property'> <label class='clearfix'> Password </label> <input " +
                "class='file-dialog-form-toggle-password' id='new_password' placeholder='admin' type='password'> </div>" +
                "</div>" +
                "<button id='addNew' type='button' class='add-new-button'>Add </button>" +
                "<div class='alert-container' id='alert-container'>" +
                "</div>" +
                "</div>" +
                "</div>" +
                "</div>" +
                "<div class='file-dialog-form-divider'>" +
                "</div>" +
                "<div class='button-container' id='button-container'>" +
                "<div class='form-group'>" +
                "<div class='file-dialog-form-btn'>" +
                "<button id='deployButton' type='button' class='btn btn-primary'>deploy" +
                "</button>" +
                "<div class='divider'/>" +
                "<button type='button' class='btn btn-default' data-dismiss='modal'>cancel</button>" +
                "</div>" +
                "</div>" +
                "</div>" +
                "<div class='loader-deployment' id='loader-deployment' style='display: none'>" +
                "</div>" +
                "<div class='deployment-status-title-container' id='deployment-status-title-container' " +
                "style='display: none'>" +
                "<div class='deployment-status-title'>Deployment Status" +
                "</div>" +
                "<div class='vertical-divider'>" +
                "</div>" +
                "</div>" +
                "<div class='deployment-status-container' id='deployment-status-container'>" +
                "</div>" +
                "</form>" +
                "<div id='openFileWizardError-container' class='openFileWizardError-container'>" +
                "<div id='openFileWizardError' class='alert alert-danger'>" +
                "<strong>Error!</strong> Something went wrong." +
                "</div>" +
                "</div>" +
                "</div>" +
                "</div>" +
                "</div>" +
                "</div>" +
                "</div>"
            );
            var openConfigModal = fileOpen.filter("#openConfigModal");
            var treeContainer = fileOpen.find("div").filter("#fileTree");
            var openFileWizardError = fileOpen.find("#openFileWizardError");
            openFileWizardError.hide();
            fileBrowser = new FileBrowser({
                container: treeContainer, application: app, fetchFiles: true,
                showWorkspace: true, multiSelect: true
            });
            $(treeContainer).on('ready.jstree', function () {
                $(treeContainer).jstree("open_all");
            });
            fileBrowser.render();
            this.fileBrowser = fileBrowser;
            this.fileBrowser.on("selected", function () {
            }, this);
            if (siddhiApps.length > 0) {
                siddhiApps = [];
            }

            fileOpen.find("button").filter("#addNew").click(function () {
                var host = document.getElementById("new_host").value;
                var port = document.getElementById("new_port").value;
                var user_name = document.getElementById("new_user_name").value;
                var password = document.getElementById("new_password").value;
                var alertContainer = $('#alert-container');
                var alertHtml = [];
                if (host != "" && port != "" && user_name != "" && password != "") {
                    inMemoryList.push({
                        host: host,
                        port: port,
                        username: user_name,
                        password: password
                    });
                    localStorage.setItem('items', JSON.stringify(inMemoryList));
                    inMemoryList = JSON.parse(localStorage.getItem('items'));
                    document.getElementById("new_host").value = "";
                    document.getElementById("new_port").value = "";
                    document.getElementById("new_user_name").value = "";
                    document.getElementById("new_password").value = "";
                    alertHtml.push("<div" +
                        "</div>");
                    alertContainer.html(alertHtml);
                } else {
                    alertHtml.push("<div class='emptyFieldsAlert'>Some fields are empty!" +
                        "</div>");
                    alertContainer.html(alertHtml);
                }
                var serverContainer = $('#server-container');
                var serverListHtml = [];
                if (inMemoryList.length == 0) {
                    serverListHtml.push("<div id='add-server-alert' " +
                        "class='add-server-alert'>Add one or more servers" +
                        "</div>");
                } else {
                    serverListHtml.push("<div class='select-boxes' id='select-boxes'>" +
                        DeployFileDialog.prototype.selectAll() +
                        "<div class='divider'/>Select All" +
                        "</div>");
                }
                for (var i = 0; i < inMemoryList.length; i++) {
                    serverListHtml.push('<div class="server-block" id="server-block">' +
                        '<input type="checkbox"git  name="server-credentials" id="check' + i + '" ' +
                        'class="server-credentials">' +
                        '<div class="toggle-divider"/>' +
                        '<div class="host">' + inMemoryList[i].host + ' : ' + inMemoryList[i].port +
                        '</div>' +
                        '<div class="toggle-divider"/>' +
                        '<div class="credentials-username">' + inMemoryList[i].username +
                        '</div>' +
                        '<div class="toggle-divider"/>' +
                        '<div class="credentials-password">' + inMemoryList[i].password +
                        '</div>' +
                        '<div class="toggle-divider"/>' + DeployFileDialog.prototype.delete(i) +
                        '</div>' +
                        '<div class="row-divider">' +
                        '</div>');
                }
                serverContainer.html(serverListHtml);
            });

            fileOpen.find("button").filter("#servers").click(function () {
                this.classList.toggle("servers-active");
                var serverList = document.getElementById("server-list");
                var newServer = document.getElementById("new-server");

                if (serverList.style.display === "block") {
                    serverList.style.display = "none";
                    newServer.style.display = "none";
                } else {
                    DeployFileDialog.prototype.viewServerList();
                    serverList.style.display = "block";
                    newServer.style.display = "block";
                }
            });

            fileOpen.find("button").filter("#siddhi-apps").click(function () {
                this.classList.toggle("servers-active");
                var siddhiAppList = document.getElementById("siddhi-app-list");
                if (siddhiAppList.style.display === "block") {
                    siddhiAppList.style.display = "none";
                } else {
                    siddhiAppList.style.display = "block";
                }
            });

            fileOpen.find("button").filter("#deployButton").click(function () {
                openFileWizardError.hide();
                var newServer = document.getElementById("new-server");
                var serverList = document.getElementById("server-list");
                var siddhiAppList = document.getElementById("siddhi-app-list");
                var deploymentStatusContainer = document.getElementById("deployment-status-title-container");
                for (var i = 0; i < inMemoryList.length; i++) {
                    var chkBox = document.getElementById('check' + i);
                    if (chkBox.checked === true) {
                        payload.serverList.push({
                            host: inMemoryList[i].host,
                            port: inMemoryList[i].port,
                            username: inMemoryList[i].username,
                            password: inMemoryList[i].password
                        });
                    }
                }
                var files = self.fileBrowser.getSelected();
                for (var i = 0; i < files.length; i++) {
                    var fileName = _.last(files[i].id.split(self.pathSeparator));
                    if (fileName.lastIndexOf(".siddhi") !== -1) {
                        payload.siddhiFileList.push({
                            fileName: fileName
                        });
                    }
                }
                var client = self.app.workspaceManager.getServiceClient();
                var data = {};
                var workspaceServiceURL = app.config.services.workspace.endpoint;
                var saveServiceURL = workspaceServiceURL + "/deploy";
                if (payload.siddhiFileList.length != 0 && payload.serverList.length != 0) {
                    $.ajax({
                        type: "POST",
                        dataType: "json",
                        data: JSON.stringify(payload),
                        contentType: "application/json; charset=utf-8",
                        url: saveServiceURL,
                        async: false,
                        success: function (response) {
                            data = response;
                            var container = $('#deployment-status-container');
                            var deploymentStatusHtml = [];
                            for (var i = 0; i < data.success.length; i++) {
                                deploymentStatusHtml.push('<div class="success-label">' + data.success[i] +
                                    '</div>'
                                );
                            }
                            for (var i = 0; i < data.failure.length; i++) {
                                deploymentStatusHtml.push('<div class="failure-label">' + data.failure[i] +
                                    '</div>'
                                );
                            }
                            if (data.success.length != 0 || data.failure.length != 0) {
                                deploymentStatusContainer.style.display = 'block';
                            }
                            newServer.style.display = "none";
                            serverList.style.display = "none";
                            siddhiAppList.style.display = "none";
                            container.html(deploymentStatusHtml);
                            payload = {
                                serverList: [],
                                siddhiFileList: []
                            };
                        },
                        error: function (xhr, textStatus, errorThrown) {
                            data = client.getErrorFromResponse(xhr, textStatus, errorThrown);
                            log.error(data.message);
                            openFileWizardError.text(data.message);
                            openFileWizardError.show();
                            payload = {
                                serverList: [],
                                siddhiFileList: []
                            };
                        }
                    });
                    return data;
                } else {
                    if (payload.siddhiFileList.length == 0 && payload.serverList.length != 0) {
                        openFileWizardError.text("Select Siddhi Apps To Deploy");
                        openFileWizardError.show();
                    }
                    else if (payload.siddhiFileList.length != 0 && payload.serverList.length == 0) {
                        openFileWizardError.text("Select Servers To Deploy");
                        openFileWizardError.show();
                    } else {
                        openFileWizardError.text("Select Siddhi Apps and Servers To Deploy");
                        openFileWizardError.show();
                    }
                    payload = {
                        serverList: [],
                        siddhiFileList: []
                    };
                }
            });
            this._fileOpenModal = fileOpen;
            openConfigModal.modal('hide');
        };

        DeployFileDialog.prototype.viewServerList = function () {
            var serverList = '';
            if (inMemoryList.length == 0) {
                serverList = serverList + "<div id='add-server-alert' " +
                    "class='add-server-alert'>Add one or more servers" +
                    "</div>";
            } else {
                serverList = serverList +
                    "<div class='select-boxes' id='select-boxes'>" + DeployFileDialog.prototype.selectAll() +
                    "<div class='divider'/>Select All" +
                    "</div>";
            }
            for (var i = 0; i < inMemoryList.length; i++) {
                serverList = serverList + '<div class="server-block" id="server-block">' +
                    '<input type="checkbox"git  name="server-credentials" id="check' + i + '" ' +
                    'class="server-credentials">' +
                    '<div class="toggle-divider"/>' +
                    '<div class="host">' + inMemoryList[i].host + ' : ' + inMemoryList[i].port +
                    '</div>' +
                    '<div class="toggle-divider"/>' +
                    '<div class="credentials-username">' + inMemoryList[i].username +
                    '</div>' +
                    '<div class="toggle-divider"/>' +
                    '<div class="credentials-password">' + inMemoryList[i].password +
                    '</div>' +
                    '<div class="toggle-divider"/>' + DeployFileDialog.prototype.delete(i) +
                    '</div>' +
                    '<div class="row-divider">' +
                    '</div>';
            }
            return serverList
        };

        DeployFileDialog.prototype.selectAll = function () {
            selectAllServers = function (source) {
                var checkboxes = document.getElementsByName("server-credentials");
                for (var i = 0, n = checkboxes.length; i < n; i++) {
                    checkboxes[i].checked = source.checked;
                }
            };
            var checkBoxContainer = "<input type='checkbox' id='select-all-servers' name='select-all-servers' " +
                "onclick='selectAllServers(this)'/>";
            return checkBoxContainer;
        };

        DeployFileDialog.prototype.delete = function (a) {
            deleteServer = function (i) {
                var id = i;
                var serverContainer = $('#server-container');
                var serverListHtml = [];
                inMemoryList.splice(id, 1);
                localStorage.setItem('items', JSON.stringify(inMemoryList));
                inMemoryList = JSON.parse(localStorage.getItem('items'));
                if (inMemoryList.length == 0) {
                    serverListHtml.push("<div id='add-server-alert' " +
                        "class='add-server-alert'>Add one or more servers" +
                        "</div>");
                } else {

                    serverListHtml.push("<div class='select-boxes' id='select-boxes'>" +
                        DeployFileDialog.prototype.selectAll() +
                        "<div class='divider'/>Select All" +
                        "</div>");
                }
                for (var i = 0; i < inMemoryList.length; i++) {
                    serverListHtml.push('<div class="server-block" id="server-block">' +
                        '<input type="checkbox"git  name="server-credentials" id="check' + i + '" ' +
                        'class="server-credentials">' +
                        '<div class="toggle-divider"/>' +
                        '<div class="host">' + inMemoryList[i].host + ' : ' + inMemoryList[i].port +
                        '</div>' +
                        '<div class="toggle-divider"/>' +
                        '<div class="credentials-username">' + inMemoryList[i].username +
                        '</div>' +
                        '<div class="toggle-divider"/>' +
                        '<div class="credentials-password">' + inMemoryList[i].password +
                        '</div>' +
                        '<div class="toggle-divider"/>' + DeployFileDialog.prototype.delete(i) +
                        '</div>' +
                        '<div class="row-divider">' +
                        '</div>');
                }
                serverContainer.html(serverListHtml);
            };
            var deleteButtonContainer = '<button id="' + a + '" type="button" class="delete-button" ' +
                'onclick="deleteServer(\'' + a + '\')">' +
                '</button>';
            return deleteButtonContainer;
        };

        DeployFileDialog.prototype.show = function () {
            this._fileOpenModal.modal('show');
        };

        return DeployFileDialog;
    });
