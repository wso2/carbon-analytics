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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.wso2.carbon.messageconsole.ui.beans.Column;
import org.wso2.carbon.messageconsole.ui.beans.ResponseRecord;

import java.lang.reflect.Type;

/**
 * This json serializer class covert ResponseRecord object to JTable required json format.
 */
public class ResponseRecordSerializer implements JsonSerializer<ResponseRecord> {

    private static final String RESULT = "Result";
    private static final String MESSAGE = "Message";
    private static final String RECORD = "Record";

    public ResponseRecordSerializer() {
        super();
    }

    @Override
    public JsonElement serialize(ResponseRecord responseRecord, Type type,
                                 JsonSerializationContext jsonSerializationContext) {

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(RESULT, responseRecord.getResult());
        jsonObject.addProperty(MESSAGE, responseRecord.getMessage());

        JsonObject jsonRecord = new JsonObject();
        if (responseRecord.getRecord() != null) {
            for (Column column : responseRecord.getRecord().getColumns()) {
                jsonRecord.addProperty(column.getKey(), column.getValue());
            }
        }
        jsonObject.add(RECORD, jsonRecord);
        return jsonObject;
    }
}
