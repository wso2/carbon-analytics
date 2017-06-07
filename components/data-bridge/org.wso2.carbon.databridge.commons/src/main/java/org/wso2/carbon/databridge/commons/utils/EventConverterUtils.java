/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.databridge.commons.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedEventException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Event Converter Utils
 */
public class EventConverterUtils {

    private static Logger log = LogManager.getLogger(EventConverterUtils.class);

    private static Gson gson = new Gson();
    /*
     * Sample JSON string expected:
     * [
     * {
     * "streamId" : "foo::1.0.0",
     * "payloadData" : ["val1", "val2"] ,
     * "metaData" : ["val1", "val2", "val3"] ,
     * "correlationData" : ["val1", "val2"],
     * "timeStamp" : 1312345432
     * }
     * ,
     * {
     * "streamId" : "bar::2.1.0",
     * "payloadData" : ["val1", "val2"] ,
     * "metaData" : ["val1", "val2", "val3"] ,
     * "correlationData" : ["val1", "val2"]
     * }
     * 
     * ]
     */

    public static List<Event> convertFromJson(String json) {
        List<Event> eventList = new ArrayList<Event>();
        try {
            JSONArray eventObjects = new JSONArray(json);
            for (int i = 0; i < eventObjects.length(); i++) {
                Event event = gson.fromJson(eventObjects.get(i).toString(), Event.class);
                if (event.getStreamId() == null || event.getStreamId().equals("")) {
                    String errorMsg = "Stream Id cannot be null or empty, for JSON : " + eventObjects.get(i).toString();
                    MalformedEventException malformedEventException = new MalformedEventException();
                    if (log.isDebugEnabled()) {
                        log.error(errorMsg, malformedEventException);
                    } else {
                        log.error(errorMsg);
                    }
                    throw malformedEventException;
                }
                eventList.add(event);
            }
        } catch (JSONException e) {
            String errorMsg = "Error converting JSON to event, for JSON : " + json;
            MalformedEventException malformedEventException = new MalformedEventException(errorMsg, e);
            if (log.isDebugEnabled()) {
                log.error(errorMsg, malformedEventException);
            } else {
                log.error(errorMsg);
            }
            throw malformedEventException;
        }
        return eventList;
    }

    public static List<Event> convertFromJson(String json, String streamId) {
        if ((streamId == null || streamId.equals(""))) {
            String errorMsg = "Stream name cannot be null or empty";
            MalformedEventException malformedEventException = new MalformedEventException();
            if (log.isDebugEnabled()) {
                log.error(errorMsg, malformedEventException);
            } else {
                log.error(errorMsg);
            }
            throw malformedEventException;
        }
        List<Event> eventList = new ArrayList<Event>();
        try {
            JSONArray eventObjects = new JSONArray(json);
            for (int i = 0; i < eventObjects.length(); i++) {
                Event event = gson.fromJson(eventObjects.get(i).toString(), Event.class);
                event.setStreamId(streamId);
                eventList.add(event);
            }
        } catch (JSONException e) {
            String errorMsg = "Error converting JSON to event, for JSON : " + json;
            MalformedEventException malformedEventException = new MalformedEventException(errorMsg, e);
            if (log.isDebugEnabled()) {
                log.error(errorMsg, malformedEventException);
            } else {
                log.error(errorMsg);
            }
            throw malformedEventException;
        }
        return eventList;

    }

    public static List<Event> convertFromJson(String json, String streamId, StreamDefinition streamDefinition) {
        if ((streamId == null || streamId.equals(""))) {
            String errorMsg = "Stream name cannot be null or empty";
            MalformedEventException malformedEventException = new MalformedEventException();
            if (log.isDebugEnabled()) {
                log.error(errorMsg, malformedEventException);
            } else {
                log.error(errorMsg);
            }
            throw malformedEventException;
        }
        List<Event> eventList = new ArrayList<Event>();
        try {
            JsonParser jsonParser = new JsonParser();
            JSONArray eventObjects = new JSONArray(json);
            for (int i = 0; i < eventObjects.length(); i++) {
                Event event = new Event();
                JsonElement jsonElement = jsonParser.parse(eventObjects.get(i).toString());

                JsonObject jsonObject = (JsonObject) jsonElement;

                Set<Map.Entry<String, JsonElement>> entries = jsonObject.entrySet();

                for (Map.Entry<String, JsonElement> entry : entries) {
                    String key = entry.getKey();

                    if (entry.getValue() instanceof JsonArray) {
                        List<Attribute> attributeList = streamDefinition.getAttributeListForKey(key);
                        if (attributeList == null) {
                            continue;
                        }

                        JsonArray jsonArray = (JsonArray) entry.getValue();
                        Iterator it = jsonArray.iterator();

                        List list = new ArrayList();
                        int pos = 0;
                        while (it.hasNext()) {
                            Object value = getValue(jsonParser.parse(it.next().toString()).getAsString(),
                                    attributeList.get(pos).getType());
                            list.add(value);
                            pos++;
                        }
                        event.setData(key, list.toArray());
                    }
                }
                event.setStreamId(streamId);
                eventList.add(event);
            }
        } catch (JSONException e) {
            String errorMsg = "Error converting JSON to event, for JSON : " + json;
            MalformedEventException malformedEventException = new MalformedEventException(errorMsg, e);
            if (log.isDebugEnabled()) {
                log.error(errorMsg, malformedEventException);
            } else {
                log.error(errorMsg);
            }
            throw malformedEventException;
        }
        return eventList;
    }

    public static Object getValue(String val, AttributeType attributeType) {
        switch (attributeType) {
        case BOOL: {
            return Boolean.parseBoolean(val);
        }
        case INT: {
            return Integer.parseInt(val);
        }
        case DOUBLE: {
            return Double.parseDouble(val);
        }
        case FLOAT: {
            return Float.parseFloat(val);
        }
        case LONG: {
            return Long.parseLong(val);
        }
        case STRING: {
            return val;
        }
        }
        return "";
    }

}
