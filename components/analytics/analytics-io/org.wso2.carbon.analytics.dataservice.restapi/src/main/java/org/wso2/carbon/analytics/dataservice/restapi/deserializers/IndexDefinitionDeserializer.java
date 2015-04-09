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
import org.wso2.carbon.analytics.dataservice.io.commons.beans.IndexConfigurationBean;
import org.wso2.carbon.analytics.dataservice.io.commons.beans.IndexEntryBean;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class represents the serialization of IndexConfigurationBean class
 */
public class IndexDefinitionDeserializer implements JsonDeserializer<IndexConfigurationBean> {
    @Override
    public IndexConfigurationBean deserialize(JsonElement jsonElement, Type type,
                                              JsonDeserializationContext jsonDeserializationContext)
            throws JsonParseException {
        IndexConfigurationBean indexConf = new IndexConfigurationBean();
        if (jsonElement instanceof JsonObject) {
            JsonObject indexConfObject = (JsonObject) jsonElement;
            JsonElement indices = indexConfObject.get("indices");
            if (indices instanceof JsonObject) {
                JsonObject indicesObj = (JsonObject)indices;
                List<IndexEntryBean> indexEntryBeans = new ArrayList<>(0);
                for (Map.Entry<String, JsonElement> entry : indicesObj.entrySet()) {
                    IndexEntryBean bean = new IndexEntryBean();
                    bean.setFieldName(entry.getKey());
                    bean.setIndexType(entry.getValue().getAsString());
                    indexEntryBeans.add(bean);
                }
                indexConf.setIndices(indexEntryBeans.toArray(new IndexEntryBean[indexEntryBeans.size()]));
            }
            JsonElement scoreParams = indexConfObject.get("scoreParams");
            if (scoreParams instanceof JsonArray) {
                JsonArray scoreParamsArray = (JsonArray)scoreParams;
                List<String> scoreParamBeans = new ArrayList<>(0);
                for (int i = 0; i < scoreParamsArray.size(); i ++) {
                    scoreParamBeans.add(scoreParamsArray.get(i).getAsString());
                }
                indexConf.setScoreParams(scoreParamBeans.toArray(new String[scoreParamsArray.size()]));
            }
        } else {
            throw new JsonParseException("Cannot parse Index configuration provided");
        }
        return indexConf;
    }
}
