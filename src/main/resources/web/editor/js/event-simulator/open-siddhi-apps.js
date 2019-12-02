/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['require', 'lodash','jquery', 'log', 'backbone', 'file_browser', 'workspace/file'],
    function (require, _, $, log, Backbone, FileBrowser, File) {

        var self = {};
        self.init = function (config) {
            self.app = _.get(config, 'application');
            self.workspaceServiceURL = self.app.config.services.workspace.endpoint;
            log.info(self.app.config.services.workspace.endpoint);
            $.ajax({
                url: self.workspaceServiceURL + "/listFilesInPath?path=" + self.app.utils.base64EncodeUnicode(""),
                type: "GET",
                contentType: "text/plain; charset=utf-8",
                async: false,
                success: function (data, textStatus, xhr) {
                    self.workspacePath = data[0].id;
                },
                error: function (res, errorCode, error) {
                    log.info(res);
                }
            });
        };

        self.openFile = function (siddhiFileName) {
            self.openConfiguration("workspace" + self.app.getPathSeperator() + siddhiFileName + ".siddhi");
        };

        self.openConfiguration = function (path) {
            $.ajax({
                url: self.workspaceServiceURL + "/read",
                type: "POST",
                data: path,
                contentType: "text/plain; charset=utf-8",
                async: false,
                success: function (data, textStatus, xhr) {
                    if (xhr.status == 200) {
                        var pathArray = _.split(path, self.app.getPathSeperator()),
                            fileName = _.last(pathArray),
                            folderPath = _.join(_.take(pathArray, pathArray.length -1), self.app.getPathSeperator());

                        var file = new File({
                            name: fileName,
                            path: folderPath,
                            content: data.content,
                            isPersisted: true,
                            isDirty: false
                        });
                        // openConfigModal.modal('hide');
                        self.app.commandManager.dispatch("create-new-tab", {tabOptions: {file: file}});
                        // simulateFunction();
                    } else {
                        // openFileWizardError.text(data.Error);
                        // openFileWizardError.show();
                    }
                },
                error: function (res, errorCode, error) {
                    var msg = _.isString(error) ? error : res.statusText;
                    // if(isJsonString(res.responseText)){
                    //     var resObj = JSON.parse(res.responseText);
                    //     if(_.has(resObj, 'Error')){
                    //         msg = _.get(resObj, 'Error');
                    //     }
                    // }
                    // openFileWizardError.text(msg);
                    // openFileWizardError.show();
                }
            });
        };

        return self;
    });
