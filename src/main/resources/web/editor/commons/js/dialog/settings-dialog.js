/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['require', 'jquery', 'log', 'backbone'], function (require, $, log, Backbone) {
    var SettingsDialog = Backbone.View.extend(
        /** @lends SettingsDialog.prototype */
        {
            /**
             * @augments Backbone.View
             * @constructs
             * @class SettingsDialog
             * @param {Object} options configuration options
             */
            initialize: function (options) {
                this._options = options;
                this.application = _.get(options, "application");
                this._dialogContainer = $(_.get(options, "application.config.dialog.container"));
            },

            show: function () {
                this._modalContainer.modal('show');
            },

            setSelectedFolder: function (path) {
                this._fileBrowser.select(path);
            },

            render: function () {
                var app = this.application,
                    options = this._options;

                if (!_.isNil(this._modalContainer)) {
                    this._modalContainer.remove();
                }

                var settingsModal = $(_.get(options, 'selector')).clone();

                settingsModal.find("select").filter("#sourceViewFontSize").change(function () {
                    var fontSize = $(this).val();
                    var tabList = app.tabController.getTabList();
                    _.each(tabList, function (tab) {
                        if (tab._title != "welcome-page") {
                            tab.getSiddhiFileEditor().getSourceView().setFontSize(fontSize);
                        }
                    });
                    app.browserStorage.put("pref:sourceViewFontSize", fontSize);
                }).val(
                    app.browserStorage.get("pref:sourceViewFontSize")
                );

                settingsModal.find("select").filter("#sourceViewTheme").change(function () {
                    var selectedTheme = $(this).val();
                    var tabList = app.tabController.getTabList();
                    _.each(tabList, function (tab) {
                        if (tab._title != "welcome-page") {
                            tab.getSiddhiFileEditor().getSourceView().setTheme(selectedTheme);
                        }
                    });
                    app.browserStorage.put("pref:sourceViewTheme", selectedTheme);
                }).val(
                    app.browserStorage.get("pref:sourceViewTheme")
                );

                this._dialogContainer.append(settingsModal);
                this._modalContainer = settingsModal;

                if (app.browserStorage.get("pref:sourceViewTheme") == null) {
                    settingsModal.find("select").filter("#sourceViewTheme").val("ace/theme/twilight");
                }

                if (app.browserStorage.get("pref:sourceViewFontSize") == null) {
                    settingsModal.find("select").filter("#sourceViewFontSize").val("12px");
                }
            }
        });

    return SettingsDialog;
});