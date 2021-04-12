/**
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
define(['jquery', './modal-dialog'], function ($, ModalDialog) {
    var ConfirmSwitchToCode = function (options) {
        this._options = options;
        this._$container = $(_.get(options, 'container', 'body'));
        this._initialized = false;
    };
    ConfirmSwitchToCode.prototype = Object.create(ModalDialog.prototype);
    ConfirmSwitchToCode.prototype.constructor = ConfirmSwitchToCode;
    ConfirmSwitchToCode.prototype.init = function () {
        if (this._initialized) {
            return;
        }
        var confirm = $("<button type='button' class='btn btn-default" +
            " confirm-switch-to-code-dialog-btn'>Confirm</button>");
        var cancelBtn = $("<button type='button' class='btn btn-default'" +
            " data-dismiss='modal'>Cancel</button>");
        this._confirm = confirm;
        this.getFooter().empty();
        this.getFooter().append(confirm, cancelBtn);
        this._initialized = true;
        this._$modalContainer.addClass("confirm-switch-to-code-dialog");
    }

    ConfirmSwitchToCode.prototype.askConfirmation = function (options) {
        var self = this;
        this.init();
        this.setTitle("Confirm to switch to code view");
        var body = this.getBody();
        body.empty();
        body.append($("<p><br>Async API spec contains changes, do you want to save them ? <br>Your changes will " +
            "be lost if you switch to source view without saving.</p>"))
        this._confirm.unbind('click');
        this._confirm.click(function (e) {
            options.self.sourceContainer.show();
            options.self.asyncAPIViewContainer.hide();
            $(options.self.toggleControlsContainer[0]).find('.toggle-view-button').removeClass('hide-div');
            $(options.self.toggleControlsContainer[0]).find('.wizard-view-button').removeClass('hide-div');
            var asyncAPIAddUpdateButton = $(options.self.toggleControlsContainer[0]).find('.async-api-add-update-button');
            asyncAPIAddUpdateButton.addClass('hide-div');
            var codeViewButton = $(options.self.toggleControlsContainer[0]).find('#asyncbtn-to-code-view');
            codeViewButton.addClass('hide-div');
            var asyncAPIViewButton = $(options.self.toggleControlsContainer[0]).find('#asyncbtn-asyncapi-view');
            asyncAPIViewButton.removeClass('hide-div');
            self.hide();
        });
        this.show();
    }
    return ConfirmSwitchToCode;
});
