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

package org.wso2.carbon.siddhi.editor.core.util.designview.deserializers.types;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.aggregation.aggregationbytimeperiod.aggregationbytimerange.AggregateByTimeInterval;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.aggregation.aggregationbytimeperiod.aggregationbytimerange.AggregateByTimeRange;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.AggregationByTimeType;

import java.lang.reflect.Type;

/**
 * De-serializer for AggregateByTimePeriod class
 */
public class AggregateByTimePeriodDeSerializer implements JsonDeserializer {
    private static final String TYPE = "type";

    @Override
    public Object deserialize(
            JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonPrimitive jsonPrimitive = (JsonPrimitive) jsonObject.get(TYPE);
        String attributesSelectionType = jsonPrimitive.getAsString();
        if (attributesSelectionType.equalsIgnoreCase(AggregationByTimeType.RANGE.toString())) {
            return jsonDeserializationContext.deserialize(jsonObject, AggregateByTimeRange.class);
        } else if (attributesSelectionType.equalsIgnoreCase(AggregationByTimeType.INTERVAL.toString())) {
            return jsonDeserializationContext.deserialize(jsonObject, AggregateByTimeInterval.class);
        }
        throw new JsonParseException("Unable to de-serialize the AggregateByTimePeriod JSON since its type is unknown");
    }
}
