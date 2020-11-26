/**
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
define(['require', 'lodash', 'jquery', 'constants', 'react', 'react_dom'],
    function (require, _, $, Constants, React, ReactDOM) {
        return function (app) {
            /**
             * initialize function for ExtensionInstallDialog.
             */
            this.initialize = function (options) {
                // console.log("Async-API-Def Loaded")
                // this.dialog_containers = $(_.get(options.config.dialog, 'container'));
                // this.extensionList = app.utils.extensionData;
                this.app = options;
            }
            /**
             * Show function to display the ExtensionInstallDialog.
             */
            this.show = function () {
                this._extensionListModal.modal('show');
            }

            /**
             * Render function for rendering all the contents of ExtensionInstallDialog.
             */
            this.render = function () {
                var self = this;
                if (!_.isNil(this._extensionListModal)) {
                    this._extensionListModal.remove();
                }
                console.log("Async api generator render()");
                var extensionModelOpen = $(
                    "<div class='modal fade' id='extensionInstallConfigModalId' tabindex='-1' role='dialog' aria-hidden='true'>" +
                        "<div class='modal-dialog file-dialog' role='document'>" +
                            "<div class='modal-content' id='sampleDialog'>" +
                                "<div class='modal-header'>" +
                                    "<button type='button' class='close' data-dismiss='modal' aria-label='Close'>" +
                                        "<i class=\"fw fw-cancel  about-dialog-close\"></i>" +
                                    "</button>" +
                                    "<h4 class='modal-title file-dialog-title'>Async API Definition</h4>" +
                                    "<hr class='style1'>" +
                                "</div>" +
                                "<div class='modal-body async-api-modal-body'>" +
                                    "<div class='async-api-def-yaml'>" +
                                        "<p>Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Nisl condimentum id venenatis a condimentum vitae. Pellentesque sit amet porttitor eget dolor. Ut sem nulla pharetra diam sit amet. Et malesuada fames ac turpis egestas. Leo in vitae turpis massa sed elementum tempus egestas. Pellentesque diam volutpat commodo sed egestas egestas fringilla phasellus faucibus. Vel orci porta non pulvinar neque laoreet suspendisse. Tortor aliquam nulla facilisi cras. Id faucibus nisl tincidunt eget nullam non nisi est.Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Nisl condimentum id venenatis a condimentum vitae. Pellentesque sit amet porttitor eget dolor. Ut sem nulla pharetra diam sit amet. Et malesuada fames ac turpis egestas. Leo in vitae turpis massa sed elementum tempus egestas. Pellentesque diam volutpat commodo sed egestas egestas fringilla phasellus faucibus. Vel orci porta non pulvinar neque laoreet suspendisse. Tortor aliquam nulla facilisi cras. Id faucibus nisl tincidunt eget nullam non nisi est.Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Nisl condimentum id venenatis a condimentum vitae. Pellentesque sit amet porttitor eget dolor. Ut sem nulla pharetra diam sit amet. Et malesuada fames ac turpis egestas. Leo in vitae turpis massa sed elementum tempus egestas. Pellentesque diam volutpat commodo sed egestas egestas fringilla phasellus faucibus. Vel orci porta non pulvinar neque laoreet suspendisse. Tortor aliquam nulla facilisi cras. Id faucibus nisl tincidunt eget nullam non nisi est.Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Nisl condimentum id venenatis a condimentum vitae. Pellentesque sit amet porttitor eget dolor. Ut sem nulla pharetra diam sit amet. Et malesuada fames ac turpis egestas. Leo in vitae turpis massa sed elementum tempus egestas. Pellentesque diam volutpat commodo sed egestas egestas fringilla phasellus faucibus. Vel orci porta non pulvinar neque laoreet suspendisse. Tortor aliquam nulla facilisi cras. Id faucibus nisl tincidunt eget nullam non nisi est.Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Nisl condimentum id venenatis a condimentum vitae. Pellentesque sit amet porttitor eget dolor. Ut sem nulla pharetra diam sit amet. Et malesuada fames ac turpis egestas. Leo in vitae turpis massa sed elementum tempus egestas. Pellentesque diam volutpat commodo sed egestas egestas fringilla phasellus faucibus. Vel orci porta non pulvinar neque laoreet suspendisse. Tortor aliquam nulla facilisi cras. Id faucibus nisl tincidunt eget nullam non nisi est.</p>" +
                                    "</div>" +
                                    "<iframe class='async-api-def-gen' scrolling='yes' id='asyncApiIFrame' src='/editor/asyncAPI.html'></iframe>" +
                                    "<div class='clear-fix'></div>" +
                                "</div>" +
                            "</div>" +
                        "</div>" +
                    "</div>");
                // var apiAsyncBody = extensionModelOpen.find("#asyncApiBodyId");
                // window.getAsyncAPIUI(apiAsyncBody[0]);
                $(this.dialog_containers).append(extensionModelOpen);
                this._extensionListModal = extensionModelOpen;
            }
        };
    });