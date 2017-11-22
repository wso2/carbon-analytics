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
        var previewLi = $("<li class='col-md-6 col-lg-4'></li>");

        var linkSample = $("<a href='' style='height: 100px;'></a>");
        linkSample.text(this._sampleName);
        linkSample.bind('click', config.clickEventCallback);
        var description = "";
            //TODO these will be hard coded for now.
        if(this._sampleName == "ReceiveAndCount"){
            description = "<span class='description'> Receive events via HTTP transport and view the output on " +
                "the console </span>"
        } else if(this._sampleName == "AlertsAndThresholds"){
            description = "<span class='description'> Simulate single events and receive alerts as e-mail when " +
                "threshold value is exceeded </span>";
        } else if(this._sampleName == "AggregateOverTime"){
            description = "<span class='description'> Simulate multiple random events and calculate aggregations " +
                "over time with group by </span>";
        } else if(this._sampleName == "PatternMatching"){
            description = "<span class='description'> Identify event patterns based on the order of event arrival</span>";
        } else if(this._sampleName == "OnlineLearning"){
            description = "<span class='description'> Train a machine learning model with CSV data and subsequently " +
                "do predictions using that model </span>";
        } else if(this._sampleName == "JoinWithStoredData"){
            description = "<span class='description'> Join streaming data with data stored in an RDBMS table";
        } else if(this._sampleName == "HelloKafka"){
            description = "<span class='description'>  Consume events from a Kafka Topic and publish to a different " +
                "Kafka Topic";
        } else if(this._sampleName == "DataPreprocessing"){
            description = "<span class='description'>  Collect data via TCP transport and pre-process";
        }
        linkSample.append(description);
        previewLi.append(linkSample);
        parentContainer.append(previewLi);
    };

    return ServicePreviewView;

});
