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

package org.wso2.carbon.siddhi.error.handler.core.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import io.siddhi.core.event.ComplexEvent;
import io.siddhi.core.event.Event;
import io.siddhi.core.util.error.handler.model.ErrorEntry;
import io.siddhi.core.util.error.handler.util.ErroneousEventType;

import java.lang.reflect.Type;
import java.util.*;

/**
 * Contains utility methods related to Siddhi Error Handler.
 */
public class SiddhiErrorHandlerUtils {
    private static final Map<ErroneousEventType, Type> mapTypes = new EnumMap<>(ErroneousEventType.class);

    static {
        mapTypes.put(ErroneousEventType.COMPLEX_EVENT, new TypeToken<ErrorEntry<ComplexEvent>>() {}.getType());
        mapTypes.put(ErroneousEventType.EVENT, new TypeToken<ErrorEntry<Event>>() {}.getType());
        mapTypes.put(ErroneousEventType.EVENT_ARRAY, new TypeToken<ErrorEntry<Event[]>>() {}.getType());
        mapTypes.put(ErroneousEventType.EVENT_LIST, new TypeToken<ErrorEntry<List<Event>>>() {}.getType());
        mapTypes.put(ErroneousEventType.PAYLOAD_STRING, new TypeToken<ErrorEntry<String>>() {}.getType());
    }

    private SiddhiErrorHandlerUtils() {}

    public static List<ErrorEntry> convertToList(JsonArray errorEntriesBody) {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        List<ErrorEntry> errorEntries = new ArrayList<>();
        for (JsonElement errorEntry : errorEntriesBody) {
            String eventTypeString = errorEntry.getAsJsonObject().get("eventType").getAsString();
            ErroneousEventType eventType = ErroneousEventType.valueOf(eventTypeString);
            errorEntries.add(gson.fromJson(errorEntry, mapTypes.get(eventType)));
        }
        return errorEntries;
    }
}
