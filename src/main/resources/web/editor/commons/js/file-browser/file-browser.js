/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['jquery', 'backbone', 'lodash', 'log', /** void module - jquery plugin **/ 'js_tree'], function ($, Backbone, _, log) {

    var FileBrowser = Backbone.View.extend({

        initialize: function (config) {
            var errMsg;
            if (!_.has(config, 'container')) {
                errMsg = 'unable to find configuration for container';
                log.error(errMsg);
                throw errMsg;
            }
            var container = $(_.get(config, 'container'));
            // check whether container element exists in dom
            if (!container.length > 0) {
                errMsg = 'unable to find container for file browser with selector: ' + _.get(config, 'container');
                log.error(errMsg);
                throw errMsg;
            }
            this._$parent_el = container;

            if (!_.has(config, 'application')) {
                log.error('Cannot init file browser. config: application not found.')
            }

            this.application = _.get(config, 'application');
            this._options = config;
            this._workspaceServiceURL = _.get(this._options, 'application.config.services.workspace.endpoint');
            this._serviceURL = _.get(this._options, 'application.config.baseUrl');
            this._isActive = false;
            this._fetchFiles = _.get(config, 'fetchFiles', false);
            this._root = _.get(config, 'root');
            this._showWorkspace = _.get(config, 'showWorkspace');
            this._showSamples = _.get(config, 'showSamples');
            this._showBundles = _.get(config, 'showBundles');
            this._multiSelect = _.get(config, 'multiSelect', false);

            this._treeConfig = {
                'core': {
                    'data': {
                        'url': this.getURLProvider(),
                        'dataType': "json",
                        'data': function (node) {
                            return {'id': node.id};
                        }
                    },
                    'multiple': this._multiSelect,
                    'check_callback': false,
                    'force_text': true,
                    'expand_selected_onload': true,
                    'themes': {
                        'responsive': false,
                        'variant': 'small',
                        'stripes': false,
                        'dots': false
                    }
                },
                'types': {
                    'default': {
                        'icon': 'fw fw-folder'
                    },
                    'folder': {
                        'icon': 'fw fw-folder'
                    },
                    'file': {
                        'icon': 'fw-document'
                    }
                },
                'sort': function (a, b) {
                    return this.get_text(a).toLowerCase() > this.get_text(b).toLowerCase() ? 1 : -1;
                }
            };

            this._plugins = ['types', 'wholerow', 'sort'];
            if (this._multiSelect) {
                this._plugins.push('checkbox');
            }
            this._contextMenuProvider = _.get(config, 'contextMenuProvider');
            if(!_.isNil(this._contextMenuProvider)){
                this._plugins.push('contextmenu');
                _.set(this._treeConfig, 'contextmenu.items', this._contextMenuProvider);
                _.set(this._treeConfig, 'contextmenu.show_at_node', false);
            }
            _.set(this._treeConfig, 'plugins', this._plugins);
        },

        getURLProvider: function(){
            var self = this;
            return function (node) {

                if (self._showBundles) {
                    if (node.id === '#') {
                        return self._serviceURL + "/listDirectoriesInPath" +
                            "?path=" + self.application.utils.base64EncodeUnicode("") +
                            "&directoryList=" + self.application.utils.base64EncodeUnicode("bundles,jars");
                    } else {
                        return self._serviceURL + "/listFilesInPath?path=" + self.application.utils.
                        base64EncodeUnicode(node.id);
                    }
                } else if(self._showWorkspace && node.id === '#'){
                    return self._workspaceServiceURL + "/listFilesInPath?path=" + self.application.utils.
                    base64EncodeUnicode("");
                } else if(self._showSamples && node.id === '#'){
                    var samplesRelativeUrl = "";
                    return self._workspaceServiceURL + "/listFiles/samples?path=" + self.application.utils.
                    base64EncodeUnicode(samplesRelativeUrl);
                } else if (node.id === '#') {
                    if(!_.isNil(self._root)){
                        if (self._fetchFiles) {
                            return self._workspaceServiceURL + "/listFiles/workspace?path=" + self.application.utils.
                            base64EncodeUnicode(self._root);
                        }
                    }
                    return self._workspaceServiceURL + "/root";
                } else {
                    if (self._fetchFiles) {
                        return self._workspaceServiceURL + "/listFiles?path=" + self.application.utils.
                        base64EncodeUnicode(node.id);
                    } else {
                        return self._workspaceServiceURL + "/list?path=" + self.application.utils.
                        base64EncodeUnicode(node.id);
                    }
                }
            }
        },

        /**
         * @param path a single path or an array of folder paths to select
         */
        select: function(path){
            this._$parent_el.jstree(true).deselect_all();
            var pathSeparator = this.application.getPathSeperator(),
                pathParts = _.split(path, pathSeparator),
                currentPart = "/",
                self = this;
            pathParts.forEach(function(part){
                currentPart += part;
                self._$parent_el.jstree(true).open_node(currentPart);
                currentPart += pathSeparator;
            });

            this._$parent_el.jstree(true).select_node(path);
        },

        refresh: function(node){
            this._$parent_el.jstree(true).load_node(node);
        },

        getNode: function(id){
            return this._$parent_el.jstree(true).get_node(id);
        },

        getSelected: function() {
            return this._$parent_el.jstree(true).get_selected(true);
        },

        getBottomSelected: function() {
            return this._$parent_el.jstree(true).get_bottom_selected(true);
        },

        render: function () {
            var self = this;
            this._$parent_el
                .jstree(self._treeConfig).on('changed.jstree', function (e, data) {
                if (data && data.selected && data.selected.length) {
                    if (self._multiSelect) {
                        self.selected = data.selected;
                        self.trigger("selected", data.selected);
                    } else {
                        self.selected = data.selected[0];
                        self.trigger("selected", data.selected[0]);
                    }
                } else {
                    self.selected = false;
                    self.trigger("selected", self._multiSelect ? [] : null);
                }
            }).on('open_node.jstree', function (e, data) {
                data.instance.set_icon(data.node, "fw fw-folder");
            }).on('close_node.jstree', function (e, data) {
                data.instance.set_icon(data.node, "fw fw-folder");
            }).on('ready', function(){
                self.trigger("ready");
            }).on('hover_node.jstree', function (e, data) {
                var linkId = data.node.a_attr.id;
                var fileName = data.node.text;
                $("a[id='"+linkId+"']").attr('title', fileName);
            }).on('select_node.jstree', function (e, data) {
                data.instance.toggle_node(data.node);
            }).on("dblclick.jstree", function (event) {
                if("folder-tree_-1" !== event.currentTarget.id){
                    return;
                }
                var item = $(event.target).closest("li");
                var node = self._$parent_el.jstree(true).get_node(item[0].id);
                var path = node.id;
                var fileName = _.last(path.split(self.application.getPathSeperator()));
                node.id = "workspace" + self.application.getPathSeperator() + fileName;
                self.trigger("double-click-node", node);
            });
            return this;
        }
    });

    return FileBrowser;

});
