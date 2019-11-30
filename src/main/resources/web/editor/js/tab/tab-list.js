/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['log', 'jquery', 'lodash', 'backbone', './tab', 'bootstrap'],
    function (log, $, _, Backbone, Tab) {

        var TabList = Backbone.View.extend(
            /** @lends TabList.prototype */
            {
                /**
                 * @augments Backbone.View
                 * @constructs
                 * @class TabList represents a list of tabs.
                 */
                initialize: function (options) {
                    var errMsg;
                    // check whether a custom Tab type is set
                    if (_.has(options, 'tabModel')) {
                        this.TabModel = _.get(options, 'tabModel');
                        // check whether the custom type is of type Tab
                        if (!this.TabModel instanceof Tab) {
                            errMsg = 'custom tab model is not a sub type of Tab: ' + Tab;
                            log.error(errMsg);
                            throw errMsg;
                        }
                    } else {
                        this.TabModel = Tab;
                    }
                    this._tabs = [];

                    if (!_.has(options, 'container')) {
                        errMsg = 'unable to find configuration for container';
                        log.error(errMsg);
                        throw errMsg;
                    }
                    var container = $(_.get(options, 'container'));
                    // check whether container element exists in dom
                    if (!container.length > 0) {
                        errMsg = 'unable to find container for tab list with selector: ' + _.get(options, 'container');
                        log.error(errMsg);
                        throw errMsg;
                    }
                    this._$parent_el = container;
                    if (!_.has(options, 'tabs.container')) {
                        errMsg = 'unable to find configuration for container';
                        log.error(errMsg);
                        throw errMsg;
                    }
                    var tabContainer = this._$parent_el.find(_.get(options, 'tabs.container'));
                    // check whether container element exists in dom
                    if (!tabContainer.length > 0) {
                        errMsg = 'unable to find container for tab list with selector: ' + _.get(options, 'tabs.container');
                        log.error(errMsg);
                        throw errMsg;
                    }
                    this.options = options;
                    this._closeAllFile = $('<a></a>');
                    this._closeAllFile.attr('href', '#');
                    this._closeAllFile.addClass('close-all pull-right');
                    this._closeAllFile.text('Close all files');
                    this._$parent_el.find(_.get(this.options, 'headers.container')).append(this._closeAllFile);
                    this._$tab_container = tabContainer;
                },

                render: function () {
                    var self = this;
                    var tabHeaderContainer = this._$parent_el.children(_.get(this.options, 'headers.container'));
                    var tabList = $('<ul></ul>');
                    tabHeaderContainer.append(tabList);

                    var tabListClass = _.get(this.options, 'headers.cssClass.list');
                    tabList.addClass(tabListClass);
                    this._$tabList = tabList;
                    this.el = tabList.get();
                    var closeAllButton = $("a.close-all")

                    closeAllButton.on('click',self._closeAllFile,function(e){
                        self.options.application.commandManager.dispatch("close-all");
                    });

//               if(_.has(this.options, 'toolPalette')){
//                   _.get(this.options, 'toolPalette').render();
//               }
                },

                hideTabComponents: function () {
                    var tabHeaderContainer = this._$parent_el.children(_.get(this.options, 'headers.container'));
                    tabHeaderContainer.hide();
                    // Hide the tool palette
                    //_.get(this.options, 'toolPalette').hideToolPalette();
                },

                showTabComponents: function () {
                    var tabHeaderContainer = this._$parent_el.children(_.get(this.options, 'headers.container'));
                    tabHeaderContainer.show();
                    // Show the tool palette
                    //_.get(this.options, 'toolPalette').showToolPalette();
                },

                createHeaderForTab: function(tab){
                    var tabHeader = $('<li></li>');
                    this._$tabList.append(tabHeader);

                    var tabHeaderClass = _.get(this.options, 'headers.cssClass.item');
                    tabHeader.addClass(tabHeaderClass);

                    var tabHeaderLink = $('<a></a>');
                    tabHeader.append(tabHeaderLink);
                    tabHeader.link = tabHeaderLink;
                    tabHeaderLink.attr('href', '#' + tab.cid);
                    tabHeaderLink.attr('id', tab.getTitle());
                    tabHeaderLink.text(tab.getTitle());

                    tabHeader.setText = function(text){
                        if(text.lastIndexOf(".siddhi") != -1){
                            tabHeaderLink.text(text.substring(0, text.lastIndexOf(".siddhi")));
                        } else{
                            tabHeaderLink.text(text);
                        }
                    };

                    var self = this;
                    tabHeaderLink.click(function(e){
                        //tabHeaderLink.tab('show');
                        self.setActiveTab(tab);
                        e.preventDefault();
                        e.stopPropagation();
                    });

                    var tabCloseBtn = $('<button type="button" >×</button>');
                    tabHeader.append(tabCloseBtn);
                    tabCloseBtn.addClass( _.get(this.options, 'tabs.tab.cssClass.tab_close_btn'));
                    tabCloseBtn.click(function(e){
                        self.removeTab(tab);
                        e.preventDefault();
                        e.stopPropagation();
                    });

                    tab.on('title-changed', function(title){
                        tabHeaderLink.text(title);
                    });

                    tab.setHeader(tabHeader);
                },

                getTabContainer: function(){
                    return this._$tab_container;
                },
                /**
                 * add a tab to the tab list.
                 *
                 * @param {Tab} tab an object of tab
                 * @fires TabList#tab-added
                 */
                addTab: function (tab) {
                    tab.setParent(this);
                    this.createHeaderForTab(tab);
                    this._tabs.push(tab);
                    /**
                     * tab added event.
                     * @event TabList#tab-added
                     * @type {Tab}
                     */
                    this.trigger("tab-added", tab);
                },
                /**
                 * gets tab
                 * @param {string} tab id
                 * @returns {*}
                 */
                getTab: function (tabId) {
                    return _.find(this._tabs, ['id', tabId]);
                },
                /**
                 * removes a tab
                 * @param {Tab} tab the tab instance
                 * @fires TabList#tab-removed
                 */
                removeTab: function (tab) {
                    var self = this;
                    if (!_.includes(this._tabs, tab)) {
                        var errMsg = 'tab : ' + tab.id + 'is not part of this tab list.';
                        log.error(errMsg);
                        throw errMsg;
                    }
                    var tabIndex = _.findIndex(this._tabs, tab);

                    _.remove(this._tabs, tab);
                    tab.getHeader().remove();
                    tab.remove();
                    /**
                     * tab removed event.
                     * @event TabList#tab-removed
                     * @type {Tab}
                     */
                    this.trigger("tab-removed", tab);

                    //switch to tab at last or next index
                    //make sure there are remaining tabs
                    if(this._tabs.length > 0 && !_.isEqual(tabIndex, -1)){
                        // if removing tab is 0th tab, next tab is also the 0th
                        var nextTabIndex = 0;
                        if(!_.isEqual(tabIndex, 0)){
                            nextTabIndex = tabIndex - 1;
                        }
                        var nextTab = this._tabs[nextTabIndex];
                        this.setActiveTab(nextTab);
                    } else {
                        self.options.application.workspaceManager.updateMenuItems();
                    }
                },
                /**
                 * set selected tab
                 * @param {Tab} tab the tab instance
                 * @fires TabList#active-tab-changed
                 */
                setActiveTab: function (tab) {
                    if (!_.isEqual(this.activeTab, tab)) {
                        if(!_.includes(this._tabs, tab)) {
                            var errMsg = 'tab : ' + tab.cid + 'is not part of this tab list.';
                            log.error(errMsg);
                            throw errMsg;
                        }
                        var lastActiveTab = this.activeTab;
                        this.activeTab = tab;
                        var activeTabHeaderClass = _.get(this.options, 'headers.cssClass.active');

                        if(!_.isUndefined(lastActiveTab)){
                            lastActiveTab.getHeader().removeClass(activeTabHeaderClass);
                            lastActiveTab.setActive(false);
                        }
                        this.activeTab.getHeader().addClass(activeTabHeaderClass);
                        this.activeTab.setActive(true);

                        //this.activeTab.getHeader().tab('show');
                        /**
                         * Active tab changed event.
                         * @event TabList#active-tab-changed
                         * @type {object}
                         * @property {Tab} lastActiveTab - last active tab.
                         * @property {Tab} newActiveTab - new active tab.
                         */
                        var evt = {lastActiveTab: lastActiveTab, newActiveTab: tab};
                        this.trigger("active-tab-changed", evt);
                    }
                },
                /**
                 * active tab
                 * @returns {Tab}
                 */
                getActiveTab: function () {
                    return this.activeTab;
                },
                getTabList: function() {
                    return this._tabs;
                },
                /**
                 * Creates a new tab.
                 * @param opts
                 *          switchToNewTab: indicate whether to switch to new tab after creation
                 *          tabOptions: constructor args for the tab
                 * @returns {Tab} created tab instance
                 * @event TabList#tab-added
                 * @fires TabList#active-tab-changed
                 */
                newTab: function (opts) {
                    var tabOptions = _.get(opts, 'tabOptions') || {};
                    _.set(tabOptions, 'application', this.options.application);
                    // merge view options from app config
                    _.assign(tabOptions, _.get(this.options, 'tabs.tab'));
                    _.set(tabOptions, 'tabs_container',_.get(this.options, 'tabs.container'));
                    _.set(tabOptions, 'parent', this);
                    var newTab;
                    // user provided a custom tab type
                    if (_.has(opts, 'tabModel')) {
                        _.set(opts, 'tabModel', Tab);
                        var TabModel = _.get(opts, 'tabModel');
                        newTab = new TabModel(tabOptions);
                    } else {
                        newTab = new this.TabModel(tabOptions);
                        if(newTab.getTitle() !== undefined){
                            _.set(newTab, '_title', newTab.getTitle());
                        }else {
                            _.set(newTab, '_title', _.get(tabOptions, 'title'))
                        }
                    }
                    this.addTab(newTab);
                    // check whether switch to new tab set to false
                    if (_.has(opts, 'switchToNewTab')) {
                        if (_.isBoolean(_.get(opts, 'switchToNewTab')) && _.get(opts, 'switchToNewTab')) {
                            this.setActiveTab(newTab);
                        }
                    } else {
                        // activate by default
                        this.setActiveTab(newTab);
                    }
                    newTab.render();
                    return newTab;
                },

                forEach: function(callback){
                    this._tabs.forEach(callback);
                }
            });

        return TabList;
    });
