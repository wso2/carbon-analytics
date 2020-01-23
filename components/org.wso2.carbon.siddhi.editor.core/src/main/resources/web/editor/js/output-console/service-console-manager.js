/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['require', 'log', 'jquery', 'lodash', 'console','workspace','toolEditor', 'perfect_scrollbar'],
    function (require, log, jquery, _, Console, Workspace,ToolEditor, PerfectScrollbar) {
        var ServiceConsole;

        ServiceConsole = Console.extend({
            initialize: function (options) {
                Console.prototype.initialize.call(this, options);
                this.consolePerfectScroller = (function() {
                    if (!$('.output-console-content').hasClass('ps')) {
                        return new PerfectScrollbar('.output-console-content');
                    }
                })();
                this.app = options.application;
            },
            getFile: function () {
                return this._file;
            },

            getTitle: function () {
                return this._title;
            },

            getContentContainer: function () {
                return this.$el;
            },

            updateHeader: function () {
            },

            showInitialStartingMessage: function (message) {
                this.$el.append('<span class="INFO">' + message + '<span>');
                this.$el.append("<br />");
                this.$el.scrollTop(100000);
            },

            println: function (message) {
                this.$el.append('<span class="' + message.type + '">' + message.message + '<span>');
                this.$el.append("<br />");
                this.$el.scrollTop(100000);
                this.consolePerfectScroller.update();
                var parentDiv = this.$el.parent()[0];
                parentDiv.scrollTop = parentDiv.scrollHeight;
                var childLength = this.$el.children().size();
                //console.log("Count="+);
                if (childLength > 2500) {
                    $(this.$el).children().first().remove();
                }
            },

            addRunningPlan: function (executionPlan) {
                this.addRunningPlanToList(executionPlan);
            },

            clear: function () {
                this.$el.empty();
            }

        });

        return ServiceConsole;
    });
