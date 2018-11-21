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
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.streamhandler.FilterConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.streamhandler.FunctionWindowConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.query.StreamHandlerType;

import java.lang.reflect.Type;

/**
 * De-serializer for StreamHandlerConfig class
 */
public class StreamHandlerConfigDeserializer implements JsonDeserializer {
    private static final String TYPE = "type";
    @Override
    public Object deserialize(
            JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonPrimitive jsonPrimitive = (JsonPrimitive) jsonObject.get(TYPE);
        String streamHandlerType = jsonPrimitive.getAsString();
        if (streamHandlerType.equalsIgnoreCase(StreamHandlerType.FILTER.toString())) {
            return jsonDeserializationContext.deserialize(jsonObject, FilterConfig.class);
        } else if (streamHandlerType.equalsIgnoreCase(StreamHandlerType.FUNCTION.toString()) ||
                streamHandlerType.equalsIgnoreCase(StreamHandlerType.WINDOW.toString())) {
            return jsonDeserializationContext.deserialize(jsonObject, FunctionWindowConfig.class);
        }
        throw new JsonParseException(
                "Unable to de-serialize the StreamHandlerConfig JSON since its type is unknown");
    }
}
