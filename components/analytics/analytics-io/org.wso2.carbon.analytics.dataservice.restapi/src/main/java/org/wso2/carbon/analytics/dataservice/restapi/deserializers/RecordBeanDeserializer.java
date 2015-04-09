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
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.analytics.dataservice.restapi.deserializers;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import org.wso2.carbon.analytics.dataservice.io.commons.beans.RecordBean;
import org.wso2.carbon.analytics.dataservice.io.commons.beans.RecordValueEntryBean;
import org.wso2.carbon.analytics.dataservice.restapi.Constants;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsCategoryPath;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class is used to deserialize the json objects to java objects
 */
public class RecordBeanDeserializer implements JsonDeserializer<RecordBean> {

    @Override
    public RecordBean deserialize(JsonElement jsonElement, Type type,
                                  JsonDeserializationContext jsonDeserializationContext)
            throws JsonParseException {
        RecordBean recordBean = new RecordBean();
        List<RecordValueEntryBean> valueEntryBeans = new ArrayList<>(0);
        JsonObject jsonRecord = (JsonObject) jsonElement;
        if (jsonRecord.get(Constants.JsonElements.ID) != null) {
            recordBean.setId(jsonRecord.get(Constants.JsonElements.ID).getAsString());
        }
        if (jsonRecord.get(Constants.JsonElements.TABLENAME) != null) {
            recordBean.setTableName(jsonRecord.get(Constants.JsonElements.TABLENAME).getAsString());
        }
        JsonObject values = jsonRecord.getAsJsonObject(Constants.JsonElements.VALUES);
        for (Map.Entry<String, JsonElement> entry : values.entrySet()) {
            RecordValueEntryBean bean = new RecordValueEntryBean();
            bean.setFieldName(entry.getKey());
            bean.setValue(validateObject(entry.getValue()));
            valueEntryBeans.add(bean);
        }
        recordBean.setValues(valueEntryBeans.toArray(new RecordValueEntryBean[valueEntryBeans.size()]));
        return recordBean;
    }

    private Object validateObject(JsonElement value) {
        if (value.isJsonObject()) {
            JsonObject jsonValue = (JsonObject)value;
            JsonArray path = jsonValue.getAsJsonArray(Constants.JsonElements.PATH);
            List<String> pathArray = new ArrayList<>(0);
            for (int i = 0; i < path.size(); i ++) {
                pathArray.add(path.get(i).getAsString());
            }
            if (jsonValue.get(Constants.JsonElements.WEIGHT) != null) {
                float weight = jsonValue.getAsJsonPrimitive(Constants.JsonElements.WEIGHT).getAsFloat();
                return new AnalyticsCategoryPath(weight, pathArray.toArray(new String[pathArray.size()]));
            } else {
                return new AnalyticsCategoryPath(pathArray.toArray(new String[pathArray.size()]));
            }
        } else if (value.isJsonPrimitive()){
            JsonPrimitive jsonValue = value.getAsJsonPrimitive();
            if (jsonValue.isBoolean()) {
                return  jsonValue.getAsBoolean();
            } else if (jsonValue.isNumber()) {
                return validateNumber(jsonValue.getAsNumber());
            } else if (jsonValue.isString()) {
                return jsonValue.getAsString();
            }
        }
        return value.getAsString();
    }

    private Object validateNumber(Number number) {
        if (number instanceof Double) {
            return number.doubleValue();
        } else if (number instanceof Float) {
            return number.floatValue();
        } else if (number instanceof Long) {
            return number.longValue();
        } else if (number instanceof Integer) {
            return number.intValue();
        } else if (number instanceof Short) {
            return number.shortValue();
        } else {
            return number.toString();
        }
    }
}
