/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.messageconsole.ui.serializers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.apache.commons.lang3.StringEscapeUtils;
import org.wso2.carbon.messageconsole.ui.beans.Column;
import org.wso2.carbon.messageconsole.ui.beans.Record;
import org.wso2.carbon.messageconsole.ui.beans.ResponseResult;

import java.lang.reflect.Type;

/**
 * This json serializer class covert ResponseResult object to JTable required json format.
 */
public class ResponseResultSerializer implements JsonSerializer<ResponseResult> {

    private static final String RESULT = "Result";
    private static final String MESSAGE = "Message";
    private static final String TOTAL_RECORD_COUNT = "TotalRecordCount";
    private static final String ACTUAL_RECORD_COUNT = "ActualRecordCount";
    private static final String SEARCH_TIME = "SearchTime";
    private static final String RECORDS = "Records";

    public ResponseResultSerializer() {
        super();
    }

    @Override
    public JsonElement serialize(ResponseResult responseResult, Type type,
                                 JsonSerializationContext jsonSerializationContext) {

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(RESULT, responseResult.getResult());
        jsonObject.addProperty(MESSAGE, responseResult.getMessage());
        jsonObject.addProperty(TOTAL_RECORD_COUNT, responseResult.getTotalRecordCount());
        jsonObject.addProperty(ACTUAL_RECORD_COUNT, responseResult.getActualRecordCount());
        jsonObject.addProperty(SEARCH_TIME, responseResult.getSearchTime());
        JsonArray records = new JsonArray();
        if (responseResult.getRecords() != null) {
            for (Record record : responseResult.getRecords()) {
                JsonObject jsonRecord = new JsonObject();
                if (record != null) {
                    for (Column column : record.getColumns()) {
                        jsonRecord.addProperty(column.getKey(), StringEscapeUtils.escapeXml11(column.getValue()));
                    }
                }
                records.add(jsonRecord);
            }
        }
        jsonObject.add(RECORDS, records);
        return jsonObject;
    }
}
