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

        var linkSample = $("<a href=''></a>");
        linkSample.text(this._sampleName);
        linkSample.bind('click', config.clickEventCallback);
        var description = "";
            //TODO these will be hard coded for now. ifs
        if(this._sampleName == "ReceiveAndCount"){
            description = "<span class='description'> ReceiveAndCount Siddhi application helps you to understand " +
                "how to configure Siddhi to receive events via HTTP transport and to view output on the console </span>"
        } else if(this._sampleName == "AlertsAndThresholds"){
            description = "<span class='description'> AlertsAndThresholds siddhi application helps you to " +
                "understand how to do single event simulation and receive alerts as e-mail. It further explains " +
                 "how filters can be used to generate alerts when a threshold value is exceeded </span>";
        } else if(this._sampleName == "AggregateOverTime"){
            description = "<span class='description'> AggregateOverTime siddhi application helps you to understand " +
                "how to simulate multiple random events and calculate running aggregations such as min, avg, etc. " +
                 "with group by. This further introduces you to the concept of time window in Siddhi </span>";
        } else if(this._sampleName == "PatternMatching"){
            description = "<span class='description'> PatternMatching siddhi application helps you to understand how "+
                "event patterns can be identified </span>";
        } else if(this._sampleName == "OnlineLearning"){
            description = "<span class='description'> OnlineLearning siddhi application helps you to understand " +
                "how to train a machine learning model and subsequently do predictions using that model. " +
                "Furthermore, it explains how to simulate events with a CSV file </span>";
        } else if(this._sampleName == "JoinWithStoredData"){
            description = "<span class='description'> JoinWithStoredData siddhi application helps you to understand " +
                "how to perform join on streaming data with stored data. Furthermore,  " +
                "it describes how we can use RDBMS datasources within Siddhi";
        } else if(this._sampleName == "HelloKafka"){
            description = "<span class='description'>  HelloKafka siddhi application helps you to understand " +
                "how to use Kafka transport in siddhi to consume events from a Kafka Topic and publish events   " +
                "to a different Kafka Topic.It further explores how data can be converted from one type to another " +
                "(e.g, JSON to XML)";
        } else if(this._sampleName == "DataPreprocessing"){
            description = "<span class='description'>  HelloKafka siddhi application helps you to understand " +
                "how to use Kafka transport in siddhi to consume events from a Kafka Topic and publish events   " +
                "to a different Kafka Topic.It further explores how data can be converted from one type to another " +
             "(e.g, JSON to XML)";
        }
        linkSample.append(description);
        previewLi.append(linkSample);
        parentContainer.append(previewLi);
    };

    return ServicePreviewView;

});
