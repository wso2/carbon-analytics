/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['log', 'backbone'], function (log, Backbone) {

    var Tab = Backbone.View.extend(
        /** @lends Tab.prototype */
        {
            /**
             * @augments Backbone.View
             * @constructs
             * @class TabView represents the view for tab.
             */
            initialize: function (options) {
                var errMsg, template;
                _.set(this, 'id', this.cid);
                _.set(this, '_title', _.get(options, 'title'));
                if (!_.has(options, 'template')) {
                    errMsg = 'unable to find config template ' + _.toString(options);
                    log.error(errMsg);
                    throw errMsg;
                }
                template = $(_.get(options, 'template'));
                if (!template.length > 0) {
                    errMsg = 'unable to find template with id ' + _.get(options, 'template');
                    log.error(errMsg);
                    throw errMsg;
                }
                this._template = template;
                this.options = options;
                this._isActive = false;
                this._lastActiveConsole = undefined;

                if (_.has(options, 'parent')) {
                    this.setParent(_.get(options, 'parent'));
                }

                // create the tab template
                var tab = this._template.children('div').clone();
                this.getParent().getTabContainer().append(tab);
                var tabClass = _.get(this.options, 'cssClass.tab');
                tab.addClass(tabClass);
                tab.attr('id', this.cid);
                this.$el = tab;
            },
            setActive: function (isActive) {
                if (_.isBoolean(isActive)) {
                    this._isActive = isActive;
                    if (isActive) {
                        this.$el.addClass(_.get(this.options, 'cssClass.tab_active'));
                    } else {
                        this.$el.removeClass(_.get(this.options, 'cssClass.tab_active'));
                    }
                }
            },
            isActive: function () {
                return this._isActive;
            },
            setHeader: function (header) {
                this._tabHeader = header;
            },
            getHeader: function () {
                return this._tabHeader;
            },
            getContentContainer: function () {
                return this.$el.get(0);
            },
            getParent: function () {
                return this._parentTabList;
            },
            setParent: function (parentTabList) {
                this._parentTabList = parentTabList;
            },
            getTitle: function () {
                return _.isNil(this._title) ? "untitled" : this._title;
            },
            setTitle: function (title) {
                this._title = title;
                this.trigger('title-changed', title);
            },
            setRunMode: function () {
                this._tabHeader.addClass(_.get(this.options, 'cssClass.run_state'));
            },
            setDebugMode: function () {
                this._tabHeader.addClass(_.get(this.options, 'cssClass.debug_state'));
            },
            setNonRunningMode: function () {
                var debugClass = _.get(this.options, 'cssClass.debug_state');
                if (this._tabHeader.hasClass(debugClass)) {
                    this._tabHeader.removeClass(debugClass);
                }
                var runClass = _.get(this.options, 'cssClass.run_state');
                if (this._tabHeader.hasClass(runClass)) {
                    this._tabHeader.removeClass(runClass);
                }
            }
        });

    return Tab;
});
