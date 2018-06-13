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

package org.wso2.carbon.siddhi.editor.core.util.designview.deserializers;

import com.google.gson.GsonBuilder;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.aggregation.aggregationbytimeperiod.AggregateByTimePeriod;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.output.QueryOutputConfig;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.attributesselection.AttributesSelectionConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.QueryInputConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.streamhandler.StreamHandlerConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.deserializers.types.AggregateByTimePeriodDeSerializer;
import org.wso2.carbon.siddhi.editor.core.util.designview.deserializers.types.AttributesSelectionConfigDeSerializer;
import org.wso2.carbon.siddhi.editor.core.util.designview.deserializers.types.QueryInputConfigDeSerializer;
import org.wso2.carbon.siddhi.editor.core.util.designview.deserializers.types.QueryOutputConfigDeSerializer;
import org.wso2.carbon.siddhi.editor.core.util.designview.deserializers.types.StreamHandlerConfigDeserializer;

/**
 * Handles De-serializer registrations with GsonBuilder, for required classes
 */
public class DeserializersRegisterer {
    /**
     * Prevents Instantiation
     */
    private DeserializersRegisterer() {
    }

    /**
     * Returns a GsonBuilder with de-serializers registered as TypeAdapters,
     * for the classes whose concrete types are not directly found in the JSON
     * @return          GsonBuilder object with registered TypeAdapters
     */
    public static GsonBuilder getGsonBuilder() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        // Register de-serializers for required classes
        gsonBuilder.registerTypeAdapter(AttributesSelectionConfig.class, new AttributesSelectionConfigDeSerializer());
        gsonBuilder.registerTypeAdapter(StreamHandlerConfig.class, new StreamHandlerConfigDeserializer());
        gsonBuilder.registerTypeAdapter(AggregateByTimePeriod.class, new AggregateByTimePeriodDeSerializer());
        gsonBuilder.registerTypeAdapter(QueryInputConfig.class, new QueryInputConfigDeSerializer());
        gsonBuilder.registerTypeAdapter(QueryOutputConfig.class, new QueryOutputConfigDeSerializer());
        return gsonBuilder;
    }
}
