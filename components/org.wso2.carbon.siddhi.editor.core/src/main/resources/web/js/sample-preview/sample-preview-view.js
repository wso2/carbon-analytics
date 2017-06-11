/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

define(['require', 'jquery', 'lodash','log'], function (require, $,  _,log) {

    var ServicePreviewView = function(config){
        this._config = config;
    };

    ServicePreviewView.prototype.constructor = ServicePreviewView;

    ServicePreviewView.prototype.render = function () {
        var config = this._config;
        var errMsg;
        if (!_.has(config, 'parentContainer')) {
            errMsg = 'unable to find configuration for parentContainer';
            log.error(errMsg);
            throw errMsg;
        }
        // get parent container which is innerSamples div
        var parentContainer = $(_.get(config, 'parentContainer'));

        this._sampleName = config.sampleName;
        //create the parent for drawn svg
        var previewLi = $("<li class='preview-li'></li>");
        //var image = $("<img id='previewImg' class='preview-img' src='images/preview_"+config.sampleName.split('.')[0]+".png'/>");
        //previewDiv.prepend(image);

//        var options =
//        {
//            "container": previewLi
//        }
//        options.container = options.container[0];
//        this._container = options.container;
//        // the first item need to active for bootstrap carousel
//        if (_.has(config, 'firstItem')) {
//            var listItem = $("<div class='item active'></div>");
//        }
//        else{
//            var listItem = $("<div class='item'></div>");
//        }
        var linkSample = $("<a href=''></a>");
        linkSample.text(this._sampleName);
        linkSample.bind('click', config.clickEventCallback);
        previewLi.append(linkSample);
        parentContainer.append(previewLi);
    };

    return ServicePreviewView;

});
