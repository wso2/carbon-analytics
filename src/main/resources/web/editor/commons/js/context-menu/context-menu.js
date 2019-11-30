/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['jquery', 'lodash', 'log', 'event_channel', 'jquery_context_menu'],
    function ($, _, log, EventChannel) {

        /**
         * @class ContextMenu
         * @augments EventChannel
         * @param args {Object}
         * @constructor
         */
        var ContextMenu = function (args) {
            _.assign(this, args);
            this.init();
        };

        ContextMenu.prototype = Object.create(EventChannel.prototype);
        ContextMenu.prototype.constructor = ContextMenu;

        ContextMenu.prototype.init = function () {
            if (_.isFunction(this.provider)) {
                this.container.contextMenu({
                    selector: this.selector,
                    build: this.provider,
                    zIndex: 4
                });
            } else {
                this.container.contextMenu({
                    selector: this.selector,
                    callback: this.callback,
                    items: this.items,
                    zIndex: 4
                });
            }
        };

        return ContextMenu;

    });