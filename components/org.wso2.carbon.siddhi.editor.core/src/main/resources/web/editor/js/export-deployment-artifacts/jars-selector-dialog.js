/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['jquery', 'lodash', 'log', 'file_browser', 'js_tree'],
    function ($, _, log, FileBrowser, jsTree) {
        var JarsSelectorDialog = function (application, form) {
            if (form.find("#k8s-path-bundles-tree").length > 0) {
                bundlesSelector = form.find("#k8s-path-bundles-tree");
            } else {
                bundlesSelector = form.find("#docker-path-bundles-tree");
            }
            var bundlesBrowser = new FileBrowser({
                container: bundlesSelector,
                application: application,
                fetchFiles: true,
                showBundles: true,
                multiSelect: true
            });

            this.bundlesSelector = bundlesSelector;
            this.bundlesBrowser = bundlesBrowser;
            this.pathSeparator = application.getPathSeperator();

        };

        JarsSelectorDialog.prototype.constructor = JarsSelectorDialog;

        JarsSelectorDialog.prototype.render = function () {
            this.bundlesBrowser.render();

            var bundlesSelector = this.bundlesSelector;
            bundlesSelector.on('ready.jstree', function () {
                bundlesSelector.jstree("open_all");
            });
        };

        JarsSelectorDialog.prototype.getSelected = function (directoryName) {
            var bundles = [];
            var files = this.bundlesBrowser.getBottomSelected();
            for (var i = 0; i < files.length; i++) {
                if (files[i].id !== directoryName && files[i].id.startsWith(directoryName)) {
                    var fileName = _.last(files[i].id.split(this.pathSeparator));
                    bundles.push(fileName);
                }
            }
            return bundles;
        };
        return JarsSelectorDialog;
    });
