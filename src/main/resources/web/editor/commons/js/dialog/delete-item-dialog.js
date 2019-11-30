/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['require', 'jquery', 'lodash', './modal-dialog', 'log'], function (require, $, _, ModalDialog, log) {

    var DeleteItemDialog = function (options) {
        _.set(options, 'class', 'delete-item-wizard');
        ModalDialog.call(this, options);
        this._serviceClient = _.get(options, 'application.workspaceManager').getServiceClient();
    };

    DeleteItemDialog.prototype = Object.create(ModalDialog.prototype);
    DeleteItemDialog.prototype.constructor = DeleteItemDialog;

    DeleteItemDialog.prototype.onSubmit = function (data) {
        this.clearError();
        var response = this._serviceClient.delete(data.path, data.type);
        if (response.error) {
            this.showError(response.message);
        } else {
            this.hide();
            var successCallBack = _.get(data, 'onSuccess');
            if (_.isFunction(successCallBack)) {
                successCallBack.call();
            }
            log.debug(data.path + " deleted successfully");
        }
    };

    DeleteItemDialog.prototype.displayWizard = function (data) {
        this.setTitle("delete " + data.type);
        this.setSubmitBtnText("delete");
        var body = this.getBody();
        body.empty();
        this.getSubmitBtn().unbind('click');
        this.clearError();
        var modalBody =
            $("<div class='delete-item-dialog'>" +
                "<div class='icon'>" +
                "<i class='fw fw-warning fw-5x'></i>" +
                "</div>" +
                "<div class='text'>" +
                "<h3> Are you sure you want to delete the selected item?</h3>" +
                "<p>You are deleting:</br>" + data.path + "</p>" +
                "</div>" +
                "</div>");
        body.append(modalBody);

        this.show();
        var self = this;
        this.getSubmitBtn().click(function (e) {
            self.onSubmit(data);
        });
    };

    return DeleteItemDialog;
});