/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

define(['require', 'lodash', 'jquery', 'log', 'backbone'], function
    (require, _, $, log, Backbone) {
  var DataMapperConfigDialog = Backbone.View.extend(
      /** @lends DataMapperConfigDialog.prototype */
      {
        /**
         * @augments Backbone.View
         * @constructs
         * @class DataMapperConfigDialog
         * @param {Object} config configuration options for the DataMapperConfigDialog
         */
        initialize: function (options, queryObjects, possibleAttributes, outputAttributes) {
          this.app = options;
          console.log('Datamapper options', queryObjects);
          console.log('possible input attributes', possibleAttributes);
          console.log('possible output attributes', outputAttributes);
          var datamapperDialog = _.cloneDeep(_.get(options.config, 'data_mapper_form'));
          this._dataMapperContainer = $(_.get(datamapperDialog, 'selector')).clone();



        },

        render: function () {

          // this.jsPlumbInstance.fire("loaded", this.jsPlumbInstance);



        },

        show: function () {
          console.log("show");
          this._dataMapperContainer.modal('show');
          console.log('rendered');

          var container = this._dataMapperContainer.find('#datamapper-jsplumb');

          console.log(container);

          this._instance = jsPlumb.getInstance({
            Container: container
          });

          var d1 = container.find('#datamapper-input');
          var d2 = container.find('#datamapper-output');

          var e1 = this._instance.addEndpoint(d1, {anchor: 'Right'}, {isSource: true});
          var e2 = this._instance.addEndpoint(d2, {anchor: 'Left'}, {isTarget: true});

          this._instance.connect({
            source: e1,
            target: e2
          })




          // var color = "gray";
          //
          // this.jsPlumbInstance = window.j = jsPlumb.getInstance({
          //   // notice the 'curviness' argument to this Bezier curve.  the curves on this page are far smoother
          //   // than the curves on the first demo, which use the default curviness value.
          //   Connector: ["Bezier", {curviness: 50}],
          //   DragOptions: {cursor: "pointer", zIndex: 2000},
          //   PaintStyle: {stroke: color, strokeWidth: 2},
          //   EndpointStyle: {radius: 9, fill: color},
          //   HoverPaintStyle: {stroke: "#ec9f2e"},
          //   EndpointHoverStyle: {fill: "#ec9f2e"},
          //   Container: $('#')
          // });
          //
          // var arrowCommon = {foldback: 0.7, fill: color, width: 14};
          // // use three-arg spec to create two different arrows with the common values:
          // var overlays = [
          //   ["Arrow", {location: 0.7}, arrowCommon],
          //   ["Arrow", {location: 0.3, direction: -1}, arrowCommon]
          // ];
          //
          // var e1 = this.jsPlumbInstance.addEndpoint(this._dataMapperContainer.find('#datamapper-input'), {anchor: 'Right'}, {isSource: true});
          // var e2 = this.jsPlumbInstance.addEndpoint(this._dataMapperContainer.find('#datamapper-output'), {anchor: 'Left'}, {isTarget: true});
          //
          //
          // this.jsPlumbInstance.connect({
          //   source: e1,
          //   target: e2
          // });
        },

        clear: function () {
          if (!_.isNil(this._dataMapperContainer)) {
            this._dataMapperContainer.remove();
          }
          if (!_.isNil(this._btnExportForm)) {
            this._btnExportForm.remove();
          }
          if (!_.isNil(this._exportKubeStepContainer)) {
            this._exportKubeStepContainer.remove();
          }
        }
      });

  return DataMapperConfigDialog;
});
