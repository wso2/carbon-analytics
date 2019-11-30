/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['jquery', './modal-dialog'], function ($, ModalDialog) {

    var CloseAllConfirmDialog = function (options) {
        this._options = options;
        this._$container = $(_.get(options, 'container', 'body'));
        this._initialized = false;
    };

    CloseAllConfirmDialog.prototype = Object.create(ModalDialog.prototype);
    CloseAllConfirmDialog.prototype.constructor = CloseAllConfirmDialog;

    CloseAllConfirmDialog.prototype.init = function () {
        if (this._initialized) {
            return;
        }

        var close = $("<button type='button' class='btn btn-default" +
            " close-all-file-confirm-dialog-btn'>Close</button>");
        var cancelBtn = $("<button type='button' class='btn btn-default'" +
            " data-dismiss='modal'>Cancel</button>");
        this._close = close;

        this.getFooter().empty();
        this.getFooter().append(close, cancelBtn);

        this._initialized = true;

        this._$modalContainer.addClass("close-all-confirm-dialog");
    }

    CloseAllConfirmDialog.prototype.askConfirmation = function (options) {
        var self = this;
        this.init();
        this.setTitle("Close All Unsaved Files?");

        var body = this.getBody();
        body.empty();
        body.append($("<p><br>Files contains changes, do you want to close them ? Your changes will " +
            "be lost if you close all without saving.</p>"))

        this._close.unbind('click');

        this._close.click(function (e) {
            if (_.has(options, 'tabList')) {
                self.hide();
                _.each(options.tabList, function (tab) {
                    options.tabController.removeTab(tab, true);
                });
            }
        });
        this.show();
    }

    return CloseAllConfirmDialog;
});
