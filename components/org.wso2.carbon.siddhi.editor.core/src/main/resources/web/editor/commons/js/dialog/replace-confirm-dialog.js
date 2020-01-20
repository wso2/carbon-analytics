/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['jquery', './modal-dialog'], function ($, ModalDialog) {

    var ReplaceConfirmDialog = function (options) {
        this._options = options;
        this._$container = $(_.get(options, 'container', 'body'));
    };

    ReplaceConfirmDialog.prototype = Object.create(ModalDialog.prototype);
    ReplaceConfirmDialog.prototype.constructor = ReplaceConfirmDialog;

    ReplaceConfirmDialog.prototype.askConfirmation = function (options) {
        var self = this;

        this.setSubmitBtnText('Replace');
        this.setCloseBtnText('Cancel');
        this._$modalContainer.addClass("replace-confirm-dialog");
        this.setTitle("Replace File?");

        var path = options.path;
        var body = this.getBody();
        body.empty();
        body.append($("<p><br>File '" + path + "' already exists. Do you want to overwrite its contents?</p>"))

        this.getSubmitBtn().unbind('click');

        this.getSubmitBtn().click(function () {
            if (_.has(options, 'handleConfirm')) {
                self.hide();
                options.handleConfirm(true);
            }
        });

        this.show();
    }

    return ReplaceConfirmDialog;
});
