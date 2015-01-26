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

package org.wso2.carbon.analytics.spark.ui.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.api.java.StructField;
import org.wso2.carbon.analytics.spark.core.AnalyticsExecutionContext;
import org.wso2.carbon.analytics.spark.core.AnalyticsExecutionException;
import org.wso2.carbon.analytics.spark.core.AnalyticsQueryResult;

import java.util.List;

/**
 * This is client will be talking to the backend spark server and output a result as a JSON string
 */
public class SparkExecutionClient {

    private static Log log = LogFactory.getLog(SparkExecutionClient.class);

    public String execute(int tenantID, String query) {
        String resultString = null;
        try {
            AnalyticsQueryResult result = AnalyticsExecutionContext.executeQuery(tenantID, query);
            if (result != null) {
                resultString = JsonResult(result);
            } else {
                resultString = JsonResult(query);
            }
        } catch (AnalyticsExecutionException e) {
//            e.printStackTrace();
            resultString = JsonError(e);
        } catch (RuntimeException e){
            resultString = JsonError(e);
        }
        return resultString;
    }

    private String JsonResult(AnalyticsQueryResult res) {
        JsonObject resObj = new JsonObject();

        JsonObject meta = new JsonObject();
        meta.addProperty("code", 200);
        meta.addProperty("errorMessage", "");
        JsonArray colArray = new JsonArray();
        for (StructField col : res.getColumns()) {
            colArray.add(new JsonPrimitive(col.getName()));
        }
        meta.add("columns", colArray);
        resObj.add("meta", meta);

        JsonObject response = new JsonObject();
        JsonArray rows = new JsonArray();
        for (List<Object> row : res.getRows()) {
            JsonArray singleRow = new JsonArray();
            for (Object elm : row) {
                singleRow.add(new JsonPrimitive(elm.toString()));
            }
            rows.add(singleRow);
        }
        response.add("items", rows);
        resObj.add("response", response);

        return resObj.toString();
    }


    private String JsonResult(String query) {
        JsonObject resObj = new JsonObject();

        JsonObject meta = new JsonObject();
        meta.addProperty("code", 200);
        meta.addProperty("errorMessage", "");
        JsonArray colArray = new JsonArray();
        colArray.add(new JsonPrimitive("Message"));
        meta.add("columns", colArray);
        resObj.add("meta", meta);

        JsonObject response = new JsonObject();
        JsonArray rows = new JsonArray();
        rows.add(new JsonPrimitive(query));
        response.add("items", rows);
        resObj.add("response", response);

        return resObj.toString();
    }

    private String JsonError(Exception e) {
        JsonObject resObj = new JsonObject();

        JsonObject meta = new JsonObject();
        meta.addProperty("code", 500);
        meta.addProperty("errorMessage", e.getMessage());
        JsonArray colArray = new JsonArray();
        meta.add("columns", colArray);
        resObj.add("meta", meta);

        JsonObject response = new JsonObject();
        response.add("items", new JsonArray());
        resObj.add("response", response);

        return resObj.toString();
    }
}
