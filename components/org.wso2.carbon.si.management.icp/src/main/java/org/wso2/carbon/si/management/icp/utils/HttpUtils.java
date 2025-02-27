/*
 *  Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.wso2.carbon.si.management.icp.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.List;
import javax.ws.rs.core.Response;

/**
 * Utilities to execute http requests.
 */
public class HttpUtils {

    private HttpUtils() {
    }

    public static JsonArray convertToJsonArray(Response response) {
        Object entity = response.getEntity();

        if (!(entity instanceof List<?>)) {
            return new JsonArray();
        }
        JsonArray payload = new JsonArray();
        Gson gson = new Gson();
        for (Object obj : (List<?>) entity) {
            payload.add(gson.toJsonTree(obj));
        }
        return payload;
    }

    public static JsonObject convertToJsonObject(Response response) {
        Object entity = response.getEntity();
        Gson gson = new Gson();
        JsonElement element = gson.toJsonTree(entity);
        if (element.isJsonObject()) {
            return element.getAsJsonObject();
        }
        return new JsonObject();
    }

    public static JsonObject getJsonResponse(CloseableHttpResponse response) {
        String stringResponse = getStringResponse(response);
        return JsonParser.parseString(stringResponse).getAsJsonObject();
    }

    private static String getStringResponse(CloseableHttpResponse response) {
        HttpEntity entity = response.getEntity();
        try {
            return EntityUtils.toString(entity, "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException("Error occurred while converting Http response to string", e);
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                throw new RuntimeException("Error occurred while closing Http response", e);
            }
        }
    }
}
