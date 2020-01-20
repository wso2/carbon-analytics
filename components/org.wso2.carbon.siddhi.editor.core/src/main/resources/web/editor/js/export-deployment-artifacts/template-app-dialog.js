/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['require', 'lodash', 'jquery', 'log', 'ace/ace', 'app/source-editor/editor', 'alerts'],
    function (require, _, $, log, ace, SiddhiEditor, alerts) {

        // todo rename to template-app-dialog.js
        var TemplateAppDialog = function (options) {
            this.app = options.app;
            this.appNames = options.siddhiAppNames;
            this.templateContainer = options.templateContainer;
            this.appObjectArrayList = this.readSiddhiApps(this.appNames);
            this.editorObjectArrayList = [];
        };

        TemplateAppDialog.prototype.constructor = TemplateAppDialog;

        TemplateAppDialog.prototype.render = function () {
            var self = this;

            self.appObjectArrayList.forEach(function (entry, index) {
                var divId = "siddhi-app-content-id".concat(index.toString());

                var heading = $('<h3 class="siddhi-app-template-header"></h3>').text(entry.name);
                var div = $('<div class="siddhi-app-template-container"></div>').attr("id", divId);

                self.templateContainer.append(heading);
                self.templateContainer.append(div);

                this._mainEditor = new SiddhiEditor({
                    divID: divId,
                    realTimeValidation: false,
                    autoCompletion: false
                });

                this._editor = ace.edit(divId);
                this._editor.getSession().setValue(entry.content);
                this._editor.resize(true);
                var obj = {
                    name: entry.name,
                    content: this._editor
                };
                self.editorObjectArrayList.push(obj);
            });
            self.templateContainer.accordion({
                collapsible: true
            });
        };

        TemplateAppDialog.prototype.readSiddhiApps = function (appNames) {
            var self = this;
            var apps = [];

            appNames.forEach(function (fileName) {
                var openServiceURL = self.app.config.services.workspace.endpoint + "/read";
                var path = "workspace" + self.app.getPathSeperator() +fileName;

                $.ajax({
                    url: openServiceURL,
                    type: "POST",
                    data: path,
                    contentType: "text/plain; charset=utf-8",
                    async: false,
                    success: function (data, textStatus, xhr) {
                        if (xhr.status == 200) {
                            var pathArray = _.split(path, self.app.getPathSeperator()),
                                fileName = _.last(pathArray);
                            var siddhiApp = {
                                name: fileName,
                                content: data.content
                            };
                            apps.push(siddhiApp);
                        } else {
                            alerts.error("Failed to read Siddhi Application" + data.error);
                        }
                    },
                    error: function (res, errorCode, error) {
                        var msg = _.isString(error) ? error : res.statusText;
                        if (isJsonString(res.responseText)) {
                            var resObj = JSON.parse(res.responseText);
                            if (_.has(resObj, 'Error')) {
                                msg = _.get(resObj, 'Error');
                            }
                        }
                        alerts.error(msg);
                    }
                });
            });
            return apps;
        };

        function isJsonString(str) {
            try {
                JSON.parse(str);
            } catch (e) {
                return false;
            }
            return true;
        }

        TemplateAppDialog.prototype.getTemplatedApps = function () {
            var self = this;
            var templatedApps = [];
            self.editorObjectArrayList.forEach(function(editorObj) {
                var appEntry = {
                    appName: editorObj.name,
                    appContent: editorObj.content.session.getValue()
                };
                templatedApps.push(appEntry);
            });
            return templatedApps;
        };
        return TemplateAppDialog;
    });


