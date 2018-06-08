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
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.output.QueryOutputConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.output.types.DeleteOutputConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.output.types.InsertOutputConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.output.types.UpdateInsertIntoOutputConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.query.QueryOutputType;

import java.lang.reflect.Type;

/**
 * De-serializer for QueryOutputConfig class
 */
public class QueryOutputConfigDeSerializer implements JsonDeserializer {
    private static final String TYPE = "type";
    private static final String OUTPUT = "output";
    private static final String TARGET = "target";

    @Override
    public Object deserialize(
            JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonPrimitive jsonPrimitive = (JsonPrimitive) jsonObject.get(TYPE);
        String queryOutputType = jsonPrimitive.getAsString();
        if (jsonObject.get(TARGET) == null) {
            return null;
        }
        String target = jsonObject.get(TARGET).getAsString();
        if (queryOutputType.equalsIgnoreCase(QueryOutputType.INSERT.toString())) {
            return new QueryOutputConfig(
                    queryOutputType,
                    jsonDeserializationContext.deserialize(jsonObject.get(OUTPUT), InsertOutputConfig.class),
                    target);
        } else if (queryOutputType.equalsIgnoreCase(QueryOutputType.UPDATE.toString())) {
            return new QueryOutputConfig(
                    queryOutputType,
                    jsonDeserializationContext.deserialize(jsonObject.get(OUTPUT), UpdateInsertIntoOutputConfig.class),
                    target);
        } else if (queryOutputType.equalsIgnoreCase(QueryOutputType.UPDATE_OR_INSERT_INTO.toString())) {
            return new QueryOutputConfig(
                    queryOutputType,
                    jsonDeserializationContext.deserialize(
                            jsonObject.get(OUTPUT), UpdateInsertIntoOutputConfig.class),
                    target);
        } else if (queryOutputType.equalsIgnoreCase(QueryOutputType.DELETE.toString())) {
            return new QueryOutputConfig(
                    queryOutputType,
                    jsonDeserializationContext.deserialize(jsonObject.get(OUTPUT), DeleteOutputConfig.class),
                    target);
        }
        throw new JsonParseException("Unable to parse the QueryOutputConfig JSON since its type is unknown");
    }
}
