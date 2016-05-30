/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
var getConfig, validate, getMode, getSchema, getData, registerCallBackforPush;

(function() {

    var PROVIDERS_LOCATION = '/extensions/providers/';

    var PROVIDER_NAME = 'realtime';
    var HTTPS_TRANSPORT = "https";
    var ANALYTICS_SERVICE = "/services/AnalyticsWebService";
    var EVENT_STREAM_SERVICE = "/services/EventStreamAdminService";
    var EVENT_PUBLISHER_SERVICE = "/services/EventPublisherAdminService";
    var HTTPConstants = Packages.org.apache.axis2.transport.http.HTTPConstants;

    var log = new Log();
    var utils = require('/modules/utils.js');
    var carbon = require("carbon");

    //EventStreamAdminServiceStub related component initialization
    var EventStreamAdminServiceStub = Packages.org.wso2.carbon.event.stream.stub.EventStreamAdminServiceStub;
    var eventStreamAdminServiceWSUrl = utils.getCarbonServerAddress(HTTPS_TRANSPORT) + EVENT_STREAM_SERVICE;
    var eventStreamStub = new EventStreamAdminServiceStub(eventStreamAdminServiceWSUrl);
    var eventsStreamServiceClient = eventStreamStub._getServiceClient();
    var eventStreamOption = eventsStreamServiceClient.getOptions();
    eventStreamOption.setManageSession(true);
    eventStreamOption.setProperty(HTTPConstants.COOKIE_STRING, session.get('authToken'));

    //EventPublisherStub related component initialization
    var EventPublisherAdminServiceStub = Packages.org.wso2.carbon.event.publisher.stub.EventPublisherAdminServiceStub
    var eventPublisherAdminServiceWSUrl = utils.getCarbonServerAddress(HTTPS_TRANSPORT) + EVENT_PUBLISHER_SERVICE;
    var eventpublisherStub = new EventPublisherAdminServiceStub(eventPublisherAdminServiceWSUrl);
    var eventsPublisherServiceClient = eventpublisherStub._getServiceClient();
    var eventPublisherOption = eventsPublisherServiceClient.getOptions();
    eventPublisherOption.setManageSession(true);
    eventPublisherOption.setProperty(HTTPConstants.COOKIE_STRING, session.get('authToken'));


    getConfig = function() {
        var formConfig = require(PROVIDERS_LOCATION + '/' + PROVIDER_NAME + '/config.json');
        var datasources = [];
        try {
            var activeEventPublishers = eventpublisherStub.getAllActiveEventPublisherConfigurations();
            if (activeEventPublishers) {
                for (var i = 0; i < activeEventPublishers.length; i++) {
                    var publisherName = activeEventPublishers[i].getEventPublisherName();
                    var wso2EventMappingDto = eventpublisherStub.getActiveEventPublisherConfiguration(publisherName)
                        .getWso2EventOutputMappingDto();
                    var publisherType = eventpublisherStub.getActiveEventPublisherConfiguration(publisherName)
                        .getToAdapterConfigurationDto().getEventAdapterType();

                    if (wso2EventMappingDto && publisherType.trim() == "ui") {
                        var streamName = wso2EventMappingDto.getOutputStreamName();
                        var streamVersion = wso2EventMappingDto.getOutputStreamVersion();
                        var streamId = streamName + ":" + streamVersion;
                        datasources.push(streamId);
                    }
                }
            }
            var datasourceCfg = {
                "fieldLabel": "Event Stream",
                "fieldName": "streamName",
                "fieldType": "dropDown"
            };
            datasourceCfg['valueSet'] = datasources;
        } catch (e) {
            log.error(e);
        }
        formConfig.config.push(datasourceCfg);
        return formConfig;
    };

    /**
     * validate the user input of provider configuration
     * @param providerConfig
     */
    validate = function (providerConfig){
        /*
         validate the form and return

         */
        return true;
    };

    /**
     * returns the data mode either push or pull
     */
    getMode = function (){
        return 'push';
    };

    /**
     * returns an array of column names & types
     * @param providerConfig
     */
    getSchema = function(providerConfig) {
        var datasource = providerConfig["streamName"];
        var output = [];

        var correlationDataLength = 0;
        var metaDataLength = 0;
        var payloadDataLength = 0;
        var counter = 0;

        var eventStreamDefinitionDto = eventStreamStub.getStreamDefinitionDto(datasource);

        var metaData = eventStreamDefinitionDto.getMetaData();
        var correlationData = eventStreamDefinitionDto.getCorrelationData();
        var payloadData = eventStreamDefinitionDto.getPayloadData();

        if (metaData != null) {
            metaDataLength = metaData.length;
        }
        if (correlationData != null) {
            correlationDataLength = correlationData.length;
        }
        if (payloadData != null) {
            payloadDataLength = payloadData.length;
        }

        output.push({
            fieldName: "TIMESTAMP",
            fieldType: "time"
        });

        var allDataLength = metaDataLength + correlationDataLength + payloadDataLength;
        for (var i = 0; i < metaDataLength; i++) {
            output.push({
                fieldName: metaData[i].getAttributeName(),
                fieldType: metaData[i].getAttributeType()
            });
            counter++;
        }
        for (var i = 0; i < correlationDataLength; i++) {
            output.push({
                fieldName: correlationData[i].getAttributeName(),
                fieldType: correlationData[i].getAttributeType()
            });
            counter++;
        }
        for (var i = 0; i < payloadDataLength; i++) {
            output.push({
                fieldName: payloadData[i].getAttributeName(),
                fieldType: payloadData[i].getAttributeType()
            });
            counter++;
        }
        return output;
    };

    /**
     * returns the actual data
     * @param providerConfig
     * @param schemaPropertyList
     */
    getData = function(providerConfig, schemaPropertyList) {

        /*
         schemaPropertyList - an array of column names
         */
    };

}());
