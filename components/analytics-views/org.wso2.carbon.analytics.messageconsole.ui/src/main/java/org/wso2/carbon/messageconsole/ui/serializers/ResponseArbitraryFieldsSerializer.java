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
import org.wso2.carbon.messageconsole.ui.beans.ResponseArbitraryField;

import java.lang.reflect.Type;

/**
 * This json serializer class covert ResponseArbitraryField object to JTable required json format.
 */
public class ResponseArbitraryFieldsSerializer implements JsonSerializer<ResponseArbitraryField> {

    private static final String RESULT = "Result";
    private static final String MESSAGE = "Message";
    private static final String RECORDS = "Records";
    private static final String NAME = "Name";
    private static final String VALUE = "Value";
    private static final String TYPE = "Type";

    public ResponseArbitraryFieldsSerializer() {
        super();
    }

    @Override
    public JsonElement serialize(ResponseArbitraryField responseArbitraryField, Type type,
                                 JsonSerializationContext jsonSerializationContext) {

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(RESULT, responseArbitraryField.getResult());
        jsonObject.addProperty(MESSAGE, responseArbitraryField.getMessage());

        JsonArray records = new JsonArray();
        if (responseArbitraryField.getColumns() != null) {
            for (Column column : responseArbitraryField.getColumns()) {
                JsonObject jsonRecord = new JsonObject();
                jsonRecord.addProperty(NAME, column.getKey());
                jsonRecord.addProperty(VALUE, StringEscapeUtils.escapeXml11(column.getValue()));
                jsonRecord.addProperty(TYPE, column.getType());
                records.add(jsonRecord);
            }
        }

        jsonObject.add(RECORDS, records);

        return jsonObject;
    }
}
