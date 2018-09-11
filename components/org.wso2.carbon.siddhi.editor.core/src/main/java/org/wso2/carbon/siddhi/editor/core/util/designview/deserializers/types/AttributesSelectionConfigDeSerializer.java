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
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.attributesselection.AllSelectionConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.attributesselection.UserDefinedSelectionConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.AttributeSelection;

import java.lang.reflect.Type;

/**
 * De-serializer for AttributesSelectionConfig class
 */
public class AttributesSelectionConfigDeSerializer implements JsonDeserializer {
    private static final String TYPE = "type";

    @Override
    public Object deserialize(
            JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonPrimitive jsonPrimitive = (JsonPrimitive) jsonObject.get(TYPE);
        String attributesSelectionType = jsonPrimitive.getAsString();
        if (attributesSelectionType.equalsIgnoreCase(AttributeSelection.TYPE_USER_DEFINED)) {
            return jsonDeserializationContext.deserialize(jsonObject, UserDefinedSelectionConfig.class);
        } else if (attributesSelectionType.equalsIgnoreCase(AttributeSelection.TYPE_ALL)) {
            return jsonDeserializationContext.deserialize(jsonObject, AllSelectionConfig.class);
        }
        throw new JsonParseException(
                "Unable to de-serialize the AttributesSelectionConfig JSON since its type is unknown");
    }
}
