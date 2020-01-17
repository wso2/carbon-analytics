/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['jquery', 'lodash', 'log', 'handlebar', 'designViewUtils', 'app/source-editor/completion-engine', 'alerts'],
    function ($, _, log, Handlebars, DesignViewUtils, CompletionEngine, alerts) {

        var TemplateDeploy = function (options) {
            this._options = options;
            this._application = options.application;
            this._activateBtn = $(options.activateBtn);
            this._container = $(options.container);
            this._containerToAdjust = $(this._options.containerToAdjust);
            this._verticalSeparator = $(this._options.separator);
            // Register event handler to toggle operator finder.
            this._application.commandManager.registerCommand(options.command.id, {shortcuts: options.command.shortcuts});
            this._application.commandManager.registerHandler(options.command.id, this.toggleTemplateDeploy, this);
            // Compile Handlebar templates.
            this._templates = {
                container: Handlebars.compile($('#template-deploy-side-panel-template').html()),
            };

            if (!localStorage.getItem('templatedAttributeList')) {
                localStorage.setItem('templatedAttributeList', JSON.stringify({}));
            }
            this.lastEdit = 0;
        };

        TemplateDeploy.prototype.render = function () {
            var self = this;

            this._container.append(this._templates.container());
            self._activateBtn.on('click', function (e) {
                e.preventDefault();
                e.stopPropagation();
                if (!$(this).hasClass('disabled')) {
                    self._application.commandManager.dispatch(self._options.command.id);
                }

                self._application.utils.retrieveSiddhiAppNames(self.populateTemplateApps, self.handleErrorMsg, self);

            });

            $("#refreshAttributes").on("click", function (e) {
                self._application.utils.retrieveSiddhiAppNames(self.populateTemplateApps, self.handleErrorMsg, self);
            })

        };

        TemplateDeploy.prototype.populateTemplateApps = function (data, context) {
            var self = context;
            var templateAttrList = [];
            var unusedTemplateAttr = [];

            data.forEach(function (app) {
                var openServiceURL = self._application.config.services.workspace.endpoint + "/read";
                var path = "workspace" + self._application.getPathSeperator() + app.siddhiAppName + '.siddhi';

                $.ajax({
                    url: openServiceURL,
                    type: "POST",
                    data: path,
                    contentType: "text/plain; charset=utf-8",
                    async: false,
                    success: function (data, textStatus, xhr) {
                        var attributes = data.content.match(/\${([^(\\$\\|\\{\\]+)}/g);
                        if (attributes && attributes.length > 0) {
                            templateAttrList = _.uniq(templateAttrList.concat(attributes));


                        } else {
                            // alerts.info("No template placeholders found in the Siddhi App.");
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

            templateAttrList.sort();

            var localStorageTemplatedAttributes = JSON.parse(localStorage.getItem('templatedAttributeList'));

            if (localStorageTemplatedAttributes) {
                unusedTemplateAttr = _.differenceWith(Object.keys(localStorageTemplatedAttributes),
                    templateAttrList, _.isEqual);

            }

            self.showAttributes(self, templateAttrList, unusedTemplateAttr);
        };

        TemplateDeploy.prototype.showAttributes = function (context, templateAttrList, unusedTemplateAttr) {
            var self = context;
            var localStorageTemplatedAttributes = JSON.parse(localStorage.getItem('templatedAttributeList'));

            if (templateAttrList.length > 0) {
                $("#used-template-attr-container").removeClass('hidden');
                $("#template-attr-table-body").empty();

                templateAttrList.forEach(function (attr) {
                    if (!localStorageTemplatedAttributes[attr]) {
                        localStorageTemplatedAttributes[attr] = "";
                    }

                    $("#template-attr-table-body").append(
                        '<tr>' +
                        '   <td>' +
                        '       <label>' + attr + '</label>' +
                        '       <input id="' + justGetName(attr) + '-input" type="text" class="form-control" data-element-type="attribute" name="item-attr" data-type="STRING" aria-required="true" value="' + localStorageTemplatedAttributes[attr] + '">' +
                        '   </td>' +
                        '</tr>'
                    );

                    $("#" + justGetName(attr) + "-input").on('keyup', function (e) {
                        var attrLocalStore = JSON.parse(localStorage.getItem('templatedAttributeList'));
                        attrLocalStore['${' + e.target.id.split('-input')[0] + '}'] = e.target.value;

                        setTimeout(function () {
                            if (Date.now() - self.lastEdit >= 1000 - 100) {
                                localStorage.setItem("templatedAttributeList", JSON.stringify(attrLocalStore));
                                // Check for semantic errors by sending a validate request to the server
                                var activeTab = self._application.tabController.getActiveTab();
                                if (activeTab._title !== "welcome-page" && !activeTab.getFile().getRunStatus()) {
                                    activeTab.getSiddhiFileEditor().getSourceView().onEnvironmentChange();
                                }
                            }
                        }, 1000);
                        self.lastEdit = Date.now();
                    })
                });
            } else {
                $("#used-template-attr-container").addClass('hidden');
            }

            if (unusedTemplateAttr.length > 0) {
                $("#unused-template-attr-container").removeClass('hidden');
                $("#unused-template-attr-table-body").empty();

                unusedTemplateAttr.forEach(function (attr) {
                    $("#unused-template-attr-table-body").append(
                        '<tr>' +
                        '   <td>' +
                        '       <label>' + attr + '</label>' +
                        '       <input id="' + justGetName(attr) + '-input" type="text" class="form-control" data-element-type="attribute" name="item-attr" data-type="STRING" aria-required="true" value="' + localStorageTemplatedAttributes[attr] + '">' +
                        '   </td>' +
                        '   <td>' +
                        '       <button type="button" class="btn btn-secondary" id="' + justGetName(attr) + '-clear" style="margin-top: 26px">' +
                        '           <i class="fw fw-delete fw-lg"></i>' +
                        '       </button>' +
                        '   </td>' +
                        '</tr>'
                    );

                    $("#" + justGetName(attr) + "-input").on('keyup', function (e) {
                        var attrLocalStore = JSON.parse(localStorage.getItem('templatedAttributeList'));
                        attrLocalStore['${' + e.target.id.split('-input')[0] + '}'] = e.target.value;

                        localStorage.setItem("templatedAttributeList", JSON.stringify(attrLocalStore));
                    });

                    $("#" + justGetName(attr) + "-clear").on('click', function (e) {
                        var attrLocalStore = JSON.parse(localStorage.getItem('templatedAttributeList'));
                        delete attrLocalStore['${' + e.currentTarget.id.split('-clear')[0] + '}'];

                        localStorage.setItem("templatedAttributeList", JSON.stringify(attrLocalStore));
                        self._application.utils.retrieveSiddhiAppNames(self.populateTemplateApps, self.handleErrorMsg, self);
                    });
                });

                $("#template-clear-all").on('click', function () {
                    var unusedTemplateAttrs = unusedTemplateAttr;
                    var attrLocalStore = JSON.parse(localStorage.getItem('templatedAttributeList'));

                    unusedTemplateAttrs.forEach(function (elem) {
                        delete attrLocalStore[elem];
                    });

                    localStorage.setItem("templatedAttributeList", JSON.stringify(attrLocalStore));
                    self._application.utils.retrieveSiddhiAppNames(self.populateTemplateApps, self.handleErrorMsg, self);
                });
            } else {
                $("#unused-template-attr-container").addClass('hidden');
            }

            if (templateAttrList.length === 0 && unusedTemplateAttr.length === 0) {
                $("#unused-template-attr-container").addClass('hidden');
                $("#used-template-attr-container").addClass('hidden');
                $("#template-attr-info").removeClass('hidden');
            } else {
                $("#template-attr-info").addClass('hidden');
            }

            localStorage.setItem('templatedAttributeList', JSON.stringify(localStorageTemplatedAttributes));
        };


        function justGetName(name) {
            var textMatch = name.match("\\$\\{(.*?)\\}");

            if (textMatch) {
                return textMatch[1];
            } else {
                return '';
            }
        }

        function isJsonString(str) {
            try {
                JSON.parse(str);
            } catch (e) {
                return false;
            }
            return true;
        }


        TemplateDeploy.prototype.handleErrorMsg = function (msg) {
            alerts.error(msg);
        };


        TemplateDeploy.prototype.toggleTemplateDeploy = function () {
            if (this._activateBtn.parent('li').hasClass('active')) {
                this._container.parent().width('0px');
                this._containerToAdjust.css('padding-left', this._options.leftOffset);
                this._verticalSeparator.css('left', this._options.leftOffset - this._options.separatorOffset);
                this._activateBtn.parent('li').removeClass("active");
            } else {
                this._activateBtn.tab('show');
                this._container.parent().width(this._options.defaultWidth);
                this._containerToAdjust.css('padding-left', this._options.defaultWidth);
                this._verticalSeparator.css('left', this._options.defaultWidth - this._options.separatorOffset);
            }
        };


        var toggleAddToSource = function (disable) {
            var elements = $('#template-deploy').find('.result-content a.add-to-source');
            if (disable) {
                elements.addClass('disabled');
            } else {
                elements.removeClass('disabled');
            }
        };


        return {
            TemplateDeploy: TemplateDeploy,
            toggleAddToSource: toggleAddToSource
        }

    });
