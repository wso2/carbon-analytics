/*
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

define(['require', 'log', 'lodash', 'jquery', 'jsplumb', 'tool_palette/tool-palette', 'designViewGrid', 'appData', 'filterQuery',
    'joinQuery', 'partition', 'passThroughQuery', 'patternQuery', 'query', 'stream', 'windowQuery', 'leftStream',
    'rightStream', 'join', 'edge'],
    function (require, log, _, $, _jsPlumb, ToolPalette, DesignViewGrid, AppData, FilterQuery, JoinQuery, Partition,
              PassThroughQuery, PatternQuery, Query, Stream, WindowQuery, LeftStream, RightStream, Join, Edge) {

        /**
         * @class DesignView
         * @constructor
         * @class DesignView  Wraps the Ace editor for design view
         * @param {Object} options Rendering options for the view
         * @param application Application data
         */
        var DesignView = function (options, application) {
            var errorMessage1 = 'unable to find design view container in design-view.js';
            var errorMessage2 = 'unable to find application in design-view.js';
            if (!_.has(options, 'container')) {
                log.error(errorMessage1);
            }
            var container = $(_.get(options, 'container'));
            if (!container.length > 0) {
                log.error(errorMessage1);
            }
            if (!_.has(options, 'application')) {
                log.error(errorMessage2);
            }
            this._$parent_el = container;
            this.options = options;
            this.application = application;
        };

        /**
         * @function drop the query element on the canvas
         */
        DesignView.prototype.initialiseSiddhiAppData = function (siddhiAppContent) {
            var self = this;
            var appData = new AppData();

            // adds annotations from a json object for an element object
            function addAnnotationsForElement(element, newElementObject) {
                _.forEach(element.annotationList, function(annotation){
                    newElementObject.addAnnotation(annotation);
                });
            }

            // adds attributes from a json object for an element object
            function addAttributesForElement(element, newElementObject) {
                _.forEach(element.attributeList, function(attribute){
                    newElementObject.addAttribute(attribute);
                });
            }

            _.forEach(siddhiAppContent.streamList, function(stream){
                var streamObject = new Stream(stream);
                //addAnnotationsForElement(stream, streamObject);
                addAttributesForElement(stream, streamObject);
                appData.addStream(streamObject);
            });
            _.forEach(siddhiAppContent.filterList, function(filterQuery){
                appData.addFilterQuery(new FilterQuery(filterQuery));
            });
            _.forEach(siddhiAppContent.passThroughList, function(passThroughQuery){
                appData.addPassThroughQuery(new PassThroughQuery(passThroughQuery));
            });
            _.forEach(siddhiAppContent.windowQueryList, function(windowQuery){
                appData.addWindowQuery(new WindowQuery(windowQuery));
            });
            _.forEach(siddhiAppContent.queryList, function(query){
                appData.addQuery(new Query(query));
            });
            _.forEach(siddhiAppContent.patternList, function(patternQuery){
                appData.addPatternQuery(new PatternQuery(patternQuery));
            });
            _.forEach(siddhiAppContent.joinQueryList, function(joinQuery){
                var joinQueryObject = new JoinQuery(joinQuery);
                var leftStreamSubElement = new LeftStream(joinQuery.join.leftStream);
                var rightStreamSubElement = new RightStream(joinQuery.join.rightStream);
                var joinSubElement = new new Join(joinQuery.join);
                joinSubElement.setLeftStream(leftStreamSubElement);
                joinSubElement.setRightStream(rightStreamSubElement);
                joinQueryObject.setJoin(joinSubElement);
                appData.addJoinQuery(joinQueryObject);
            });
            _.forEach(siddhiAppContent.partitionList, function(partition){
                appData.addPartition(new Partition(partition));
            });
            _.forEach(siddhiAppContent.edgeList, function(edge){
                appData.addEdge(new Edge(edge));
            });
            self.siddhiAppContent = appData;
        };

        /**
         * @function Renders tool palette in the design container
         */
        DesignView.prototype.renderToolPalette = function () {
            var errMsg = '';
            var toolPaletteContainer = this._$parent_el.find(_.get(this.options, 'design_view.tool_palette.container'))
                .get(0);
            if (toolPaletteContainer === undefined) {
                errMsg = 'unable to find tool palette container with selector: '
                    + _.get(this.options, 'design_view.tool_palette.container');
                log.error(errMsg);
            }
            var toolPaletteOpts = _.clone(_.get(this.options, 'design_view.tool_palette'));
            if (toolPaletteOpts === undefined) {
                errMsg = 'unable to find tool palette with selector: '
                    + _.get(this.options, 'design_view.tool_palette');
                log.error(errMsg);
            }
            toolPaletteOpts.container = toolPaletteContainer;
            this.toolPalette = new ToolPalette(toolPaletteOpts);
            this.toolPalette.render();
        };

        /**
         * @function Renders design view in the design container
         * @param siddhiAppContent Siddhi application content
         */
        DesignView.prototype.renderDesignGrid = function (siddhiAppContent) {
            this.initialiseSiddhiAppData(siddhiAppContent);
            var designViewGridOpts = {};
            _.set(designViewGridOpts, 'container', this.designViewGridContainer);
            _.set(designViewGridOpts, 'appData', this.siddhiAppContent);
            _.set(designViewGridOpts, 'application', this.application);
            var designViewGrid = new DesignViewGrid(designViewGridOpts);
            designViewGrid.render();
        };

        DesignView.prototype.getSiddhiAppContent = function () {
            return this.siddhiAppContent;
        };

        DesignView.prototype.emptyDesignViewGridContainer = function () {
            var errMsg = '';
            this.designViewGridContainer = this._$parent_el.find(_.get(this.options, 'design_view.grid_container'));
            if (!this.designViewGridContainer.length > 0) {
                errMsg = 'unable to find design view grid container with selector: '
                    + _.get(this.options, 'design_view.grid_container');
                log.error(errMsg);
            }
            // remove any child nodes from designViewGridContainer if exists
            this.designViewGridContainer.empty();
            // reset the jsPlumb common instance
            _jsPlumb.reset();
        };

        DesignView.prototype.showToolPalette = function () {
            if (this.toolPalette !== undefined) {
                this.toolPalette.showToolPalette();
            }
        };

        DesignView.prototype.hideToolPalette = function () {
            if (this.toolPalette !== undefined) {
                this.toolPalette.hideToolPalette();
            }
        };

        return DesignView;
    });
