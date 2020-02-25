/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.siddhi.editor.core.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class MetaInfoRetrieverUtils {

    private MetaInfoRetrieverUtils() {

    }

    public static JsonObject createResponse(String[] attributeNameArray, String[] values) {
        JsonObject response = new JsonObject();
        JsonArray attributes = new JsonArray();
        int count = 0;
        for (String value : values) {
            JsonObject attribute = new JsonObject();
            if (attributeNameArray != null) {
                attribute.addProperty("name", attributeNameArray[count]);
            } else {
                attribute.addProperty("name", "attr" + count + 1);
            }
            attribute.addProperty("value", value);
            attributes.add(attribute);
            count ++;
        }
        response.addProperty("attributes", attributes.toString());
        return response;
    }

    public static boolean isJsonValid(String jsonInString) {
        Gson gson = new Gson();
        try {
            gson.fromJson(jsonInString, Object.class);
            return true;
        } catch (com.google.gson.JsonSyntaxException ex) {
            return false;
        }
    }

}
