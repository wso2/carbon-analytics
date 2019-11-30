/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['lodash', 'log', 'file_browser', 'event_channel', 'context_menu', 'bootstrap'],
    function (_, log, FileBrowser, EventChannel, ContextMenu) {
        var application;
        var ExplorerItem = function (args) {
            this.app = args.application;
            application=this.app;
            this.pathSeparator = this.app.getPathSeperator();
            _.assign(this, args);
        };

        ExplorerItem.prototype = Object.create(EventChannel.prototype);
        ExplorerItem.prototype.constructor = ExplorerItem;

        ExplorerItem.prototype.getFolderName = function (folderPath) {
            var splitArr = _.split(folderPath, this.application.getPathSeperator());
            return _.gt(_.last(splitArr).length, 0) ? _.last(splitArr) :
                _.nth(splitArr, splitArr.length - 2);
        };

        ExplorerItem.prototype.render = function () {
            var self = this;
            var item = $('<div class="folder-tree"><div>'),
                folderName = $("<span>" + this.getFolderName(this.path) + "</span>"),
                id = "folder-tree_" + this.index,
                header = $('<div class="folder-tree-header" role="button" href="#' + id +
                    '"+ data-toggle="collapse" aria-expanded="true" aria-controls="' +
                    id + '"></div>'),
                body = $('<div class="folder-tree-body" id="' + id +
                    '"></div>'),
                folderIcon = $("<i class='fw fw-folder item-icon'></i>"),
                arrowHeadIcon = $("<i class='fw fw-right expand-icon'></i>");
            this.header = header;
            this.arrowHeadIcon = arrowHeadIcon;

            header.attr("id", this.path);
            header.append(arrowHeadIcon);
            header.append(folderIcon);
            header.append(folderName);
            item.append(header);
            item.append(body);
            this.container.append(item);
            this._itemElement = item;

            header.attr('title', this.path);
            header.tooltip({
                'delay': {show: 1000, hide: 0},
                'placement': 'bottom',
                'container': 'body'
            });

            body.on('show.bs.collapse', function () {
                arrowHeadIcon.addClass("fw-rotate-90");
            });

            body.on('hide.bs.collapse', function () {
                arrowHeadIcon.removeClass("fw-rotate-90");
            });

            body.on('ready.jstree', function () {
                body.jstree("open_all");
                application.workspaceManager.updateExportArtifactsItem();
            });

            var fileBrowser = new FileBrowser({
                container: body,
                application: this.application, root: this.path,
                fetchFiles: true
            });
            fileBrowser.render();
            fileBrowser.on("double-click-node", function (node) {
                var location = node.id;
                var pathAttributes = location.split(self.pathSeparator);
                var fileName = _.last(pathAttributes);

                var tabList = self.app.tabController.getTabList();
                var fileAlreadyOpened = false;
                var openedTab;

                _.each(tabList, function (tab) {
                    if (tab.getTitle() == fileName) {
                        fileAlreadyOpened = true;
                        openedTab = tab;
                    }
                });

                if (fileAlreadyOpened) {
                    self.app.tabController.setActiveTab(openedTab);
                } else {
                    if (_.isEqual('file', node.type)) {
                        this.application.commandManager.dispatch("open-file", node.id);
                    }
                }
            }, this);

            this._contextMenu = new ContextMenu({
                container: item,
                selector: ".folder-tree-header, li",
                provider: this.createContextMenuProvider()
            });
            this._fileBrowser = fileBrowser;
        };

        ExplorerItem.prototype.remove = function () {
            this._itemElement.remove();
        };

        ExplorerItem.prototype.createContextMenuProvider = function () {
            var self = this;
            return function ($trigger, e) {
                var items = {},
                    menu = {items: items},
                    isRoot = $trigger.hasClass("folder-tree-header"),
                    path = $trigger.attr('id'),
                    node = isRoot ? self._fileBrowser.getNode('#') : self._fileBrowser.getNode(path);

                if (isRoot || _.isEqual('folder', node.type)) {
                    items.createNewFile = {
                        name: "new file",
                        icon: "",
                        callback: function () {
                            self.application.commandManager.dispatch("create-new-item-at-path",
                                {
                                    path: path,
                                    type: 'siddhi-file',
                                    onSuccess: function () {
                                        self._fileBrowser.refresh(node);
                                    }
                                });
                        }
                    };
                    items.createNewFolder = {
                        name: "new folder",
                        icon: "",
                        callback: function () {
                            self.application.commandManager.dispatch("create-new-item-at-path",
                                {
                                    path: path,
                                    type: 'folder',
                                    onSuccess: function () {
                                        self._fileBrowser.refresh(node);
                                    }
                                });
                        }
                    };
                    items.refreshBtn = {
                        name: "refresh",
                        icon: "",
                        callback: function () {
                            self._fileBrowser.refresh(node);
                        }
                    };
                    items.deleteFolder = {
                        name: "delete",
                        icon: "",
                        callback: function () {
                            self.application.commandManager.dispatch("remove-from-disk",
                                {
                                    type: "folder",
                                    path: path,
                                    onSuccess: function () {
                                        if (isRoot) {
                                            self.application.commandManager.dispatch("remove-explorer-item", self);
                                        } else {
                                            self._fileBrowser.refresh(node.parent);
                                        }
                                    }
                                });
                        }
                    }
                }
                else if (_.isEqual('file', node.type)) {
                    items.deleteFile = {
                        name: "delete",
                        icon: "",
                        callback: function () {
                            self.application.commandManager.dispatch("remove-from-disk",
                                {
                                    type: "file",
                                    path: node.id,
                                    onSuccess: function () {
                                        self._fileBrowser.refresh(node.parent);
                                    }
                                });
                        }
                    }
                }

                if (isRoot) {
                    items.removeFolderFromExplorer = {
                        name: "remove folder",
                        icon: "",
                        callback: function () {
                            self.application.commandManager.dispatch("remove-explorer-item", self);
                        }
                    };
                }
                return menu;
            };
        };

        return ExplorerItem;

    });