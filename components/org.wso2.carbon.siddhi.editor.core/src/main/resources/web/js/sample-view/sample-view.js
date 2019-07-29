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

define(['require', 'jquery', 'lodash', 'log'], function (require, $, _, log) {
    var ServiceView = function (config) {
        this._config = config;
    };
    ServiceView.prototype.constructor = ServiceView;
    ServiceView.prototype.render = function () {
        var config = this._config;
        var errMsg;
        if (!_.has(config, 'parentContainer')) {
            errMsg = 'unable to find configuration for parentContainer';
            log.error(errMsg);
            throw errMsg;
        }
        // get parent container which is innerSamples div
        var parentContainer = _.get(config, 'parentContainer');
        this._sampleName = config.sampleName;
        this._content = config.sampleDes;
        this._id = config.sampleName;
        this._sampleCategories = config.sampleCategories;

        //create the parent for drawn svg
        var sampleLi = $("<li class='col-md-6'></li>");
        var linkSample = $("<a href='#' style='display: block;'></a>");
        linkSample.text(this._sampleName);
        linkSample.attr('id', this._sampleName);
        linkSample.bind('click', config.clickEventCallback);

        var description = "<span class='description'>" + this._content + "</span>";
        var category = "<span class='category'>Category : " + this._sampleCategories + "</span>";
        linkSample.append(description);
        linkSample.append(category);
        sampleLi.append(linkSample);
        parentContainer.append(sampleLi);
    };
    return ServiceView;
});
