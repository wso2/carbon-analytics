/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 * language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.analytics.dataservice.restapi.serializers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.wso2.carbon.analytics.dataservice.io.commons.beans.AnalyticsCategoryPathBean;
import org.wso2.carbon.analytics.dataservice.io.commons.beans.RecordBean;
import org.wso2.carbon.analytics.dataservice.io.commons.beans.RecordValueEntryBean;
import org.wso2.carbon.analytics.dataservice.restapi.Constants;

import java.lang.reflect.Type;

/**
 * RecordBeanSerializer serializes the RecordBean class to the format required by the REST API.
 * So it will show the values in the format,
 *     "column" : "value"
 */

public class RecordBeanSerializer implements JsonSerializer<RecordBean> {

    @Override
    public JsonElement serialize(RecordBean recordBean, Type type,
                                 JsonSerializationContext jsonSerializationContext) {
        JsonObject record = new JsonObject();
        record.addProperty(Constants.JsonElements.ID, recordBean.getId());
        record.addProperty(Constants.JsonElements.TABLENAME, recordBean.getTableName());
        record.addProperty(Constants.JsonElements.TIMESTAMP, recordBean.getTimestamp());
        JsonObject values = new JsonObject();
        for (RecordValueEntryBean entryBean : recordBean.getValues()) {
            if (!(entryBean.getValue() instanceof AnalyticsCategoryPathBean)) {
                values.addProperty(entryBean.getFieldName(), entryBean.getValue().toString());
            } else {
                JsonObject facetValue = new JsonObject();
                AnalyticsCategoryPathBean bean = (AnalyticsCategoryPathBean)entryBean.getValue();
                JsonArray jsonPath = new JsonArray();
                for (String pathTerm : bean.getPath()) {
                    jsonPath.add(new JsonPrimitive(pathTerm));
                }
                facetValue.add(Constants.JsonElements.PATH, jsonPath);
                facetValue.addProperty(Constants.JsonElements.WEIGHT, bean.getWeight());
                values.add(entryBean.getFieldName(), facetValue);
            }
        }
        record.add(Constants.JsonElements.VALUES, values);
        return record;
    }
}
