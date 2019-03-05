/**
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

define(['require', 'elementUtils'],
    function (require, ElementUtils) {

        /**
         * @class ConfigurationData
         * @constructor
         * @class ConfigurationData  Holds the configuration data for a given Siddhi app
         * @param {Object} siddhiAppConfig Siddhi App Data
         * @param {object} application Current Application data
         */
        var ConfigurationData = function (siddhiAppConfig, application, rawExtensions) {
            this.siddhiAppConfig = siddhiAppConfig;
            this.edgeList = [];
            // checks whether still the graph is drawing from the JSON sent from backend when switching from code
            // to design
            this.isStillDrawingGraph = false;
            this.isDesignViewContentChanged = false;
            this.application = application;
            this.rawExtensions = rawExtensions;
        };

        ConfigurationData.prototype.addEdge = function (edge) {
            this.edgeList.push(edge);
        };

        ConfigurationData.prototype.removeEdge = function (edgeId) {
            ElementUtils.prototype.removeElement(this.edgeList, edgeId);
        };

        ConfigurationData.prototype.getSiddhiAppConfig = function () {
            return this.siddhiAppConfig;
        };

        ConfigurationData.prototype.getEdge = function (edgeId) {
            return ElementUtils.prototype.getElement(this.edgeList, edgeId);
        };
        ConfigurationData.prototype.getEdgeList = function () {
            return this.edgeList;
        };

        ConfigurationData.prototype.getIsStillDrawingGraph = function () {
            return this.isStillDrawingGraph;
        };

        ConfigurationData.prototype.getIsDesignViewContentChanged = function () {
            return this.isDesignViewContentChanged;
        };

        ConfigurationData.prototype.setSiddhiAppConfig = function (siddhiAppConfig) {
            this.siddhiAppConfig = siddhiAppConfig;
        };

        ConfigurationData.prototype.setIsStillDrawingGraph= function (isStillDrawingGraph) {
            this.isStillDrawingGraph = isStillDrawingGraph;
        };

        ConfigurationData.prototype.setIsDesignViewContentChanged = function (isDesignViewContentChanged) {
            var self = this;
            self.isDesignViewContentChanged = isDesignViewContentChanged;
            var activeTab = self.application.tabController.getActiveTab();
            var file = activeTab.getFile();
            // If the graph is not drawing from the JSON sent from the backend when switching from code to design, then
            // if a change is done in the design then app is stop if it is still running or debugging.
            if(!self.isStillDrawingGraph && isDesignViewContentChanged
                && (file.getRunStatus() || file.getDebugStatus())){
                var launcher = activeTab.getSiddhiFileEditor().getLauncher();
                launcher.stopApplication(self.application.workspaceManager, false);
            }
        };

        return ConfigurationData;
    });
