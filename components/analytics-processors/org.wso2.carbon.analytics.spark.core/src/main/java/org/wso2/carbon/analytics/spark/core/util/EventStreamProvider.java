/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.analytics.spark.core.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.sources.RelationProvider;
import org.wso2.carbon.analytics.spark.core.internal.ServiceHolder;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.event.stream.core.exception.EventStreamConfigurationException;
import scala.collection.immutable.Map;
import scala.runtime.AbstractFunction0;

import java.io.Serializable;

public class EventStreamProvider implements RelationProvider, Serializable {
    private static final Log log = LogFactory.getLog(EventStreamProvider.class);
    private static final long serialVersionUID = 9219903158801397937L;

    private int tenantId;
    private String streamName;
    private String version;
    private String description;
    private String nickname;
    private String payload;

    public EventStreamProvider() {
    }

    @Override
    public StreamRelation createRelation(SQLContext sqlContext, Map<String, String> parameters) {
        setParameters(parameters);
        defineStreamIfNotExists();

        return new StreamRelation(tenantId, sqlContext, getStreamId(streamName, version), payload);
    }

    private void setParameters(Map<String, String> parameters) {
        this.tenantId = Integer.parseInt(extractValuesFromMap(AnalyticsConstants.TENANT_ID, parameters, "-1234"));
        this.streamName = extractValuesFromMap(AnalyticsConstants.STREAM_NAME, parameters, "");
        this.version = extractValuesFromMap(AnalyticsConstants.VERSION, parameters, "1.0.0");
        this.description = extractValuesFromMap(AnalyticsConstants.DESCRIPTION, parameters, "");
        this.nickname = extractValuesFromMap(AnalyticsConstants.NICKNAME, parameters, "");
        this.payload = extractValuesFromMap(AnalyticsConstants.PAYLOAD, parameters, "");
    }

    private void defineStreamIfNotExists() {
        if (!this.streamName.isEmpty()) {
            StreamDefinition streamDefinition = null;
            try {
                streamDefinition = new StreamDefinition(streamName, version);
                streamDefinition.setDescription(this.description);
                streamDefinition.setNickName(this.nickname);
                if (payload != null && !payload.isEmpty()) {
                    String[] fields = payload.split(",");
                    String name, type;
                    String[] tokens;
                    for (int i = 0; i < fields.length; i++) {
                        tokens = fields[i].trim().split(" ");
                        name = tokens[0].trim();
                        type = tokens[1].trim().toUpperCase();
                        streamDefinition.addPayloadData(name, AttributeType.valueOf(type));
                    }
                }
                ServiceHolder.getEventStreamService().addEventStreamDefinition(streamDefinition);
            } catch (MalformedStreamDefinitionException e) {
                log.error("An error occurred while creating the stream definition : " + streamName, e);
            } catch (EventStreamConfigurationException e) {
                log.error("Invalid stream configuration", e);
            }
        } else {
            throw new RuntimeException("Empty " + AnalyticsConstants.STREAM_NAME);
        }

    }

    private String getStreamId(String streamName, String version) {
        return streamName + ":" + version;
    }

    private String extractValuesFromMap(String key, Map<String, String> map,
                                        final String defaultVal) {
        return map.getOrElse(key, new AbstractFunction0<String>() {
            public String apply() {
                return defaultVal;
            }
        });
    }


}
