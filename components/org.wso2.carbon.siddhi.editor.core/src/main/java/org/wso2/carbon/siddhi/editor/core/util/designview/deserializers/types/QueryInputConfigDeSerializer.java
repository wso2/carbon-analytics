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
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.join.JoinConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.windowfilterprojection.WindowFilterProjectionConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.query.QueryInputType;

import java.lang.reflect.Type;

/**
 * De-serializer for QueryInputConfig class
 */
public class QueryInputConfigDeSerializer implements JsonDeserializer {
    private static final String TYPE = "type";

    @Override
    public Object deserialize(
            JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonPrimitive jsonPrimitive = (JsonPrimitive) jsonObject.get(TYPE);
        String queryInputType = jsonPrimitive.getAsString();
        if (queryInputType.equalsIgnoreCase(QueryInputType.WINDOW.toString()) ||
                queryInputType.equalsIgnoreCase(QueryInputType.FILTER.toString()) ||
                queryInputType.equalsIgnoreCase(QueryInputType.PROJECTION.toString())) {
            return jsonDeserializationContext.deserialize(jsonObject, WindowFilterProjectionConfig.class);
        } else if (queryInputType.equalsIgnoreCase(QueryInputType.JOIN.toString())) {
            return jsonDeserializationContext.deserialize(jsonObject, JoinConfig.class);
        }
        throw new JsonParseException("Unable to parse the QueryInputConfig JSON since its type is unknown");
    }
}
