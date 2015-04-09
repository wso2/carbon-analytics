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

package org.wso2.carbon.analytics.dataservice.restapi.serializers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.wso2.carbon.analytics.dataservice.io.commons.beans.IndexConfigurationBean;
import org.wso2.carbon.analytics.dataservice.io.commons.beans.IndexEntryBean;
import org.wso2.carbon.analytics.dataservice.restapi.Constants;

import java.lang.reflect.Type;

/**
 * This class serializes the index definition which contains the index and score param definitions
 */
public class IndexDefinitionSerializer implements JsonSerializer<IndexConfigurationBean> {

    @Override
    public JsonElement serialize(IndexConfigurationBean indexConfigurationBean, Type type,
                                 JsonSerializationContext jsonSerializationContext) {
        JsonObject indexConf = new JsonObject();
        JsonObject indexObject = new JsonObject();
        for (IndexEntryBean indexDef : indexConfigurationBean.getIndices()) {
            indexObject.addProperty(indexDef.getFieldName(), indexDef.getIndexType());
        }
        JsonArray scoreParams = new JsonArray();
        for (String scoreParam : indexConfigurationBean.getScoreParams()) {
            scoreParams.add(new JsonPrimitive(scoreParam));
        }
        indexConf.add(Constants.JsonElements.INDICES, indexObject);
        indexConf.add(Constants.JsonElements.SCORE_PARAMS, scoreParams);
        return indexConf;
    }
}
