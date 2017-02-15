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
import com.google.gson.GsonBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;

import java.util.ArrayList;
import java.util.List;

/**
 * Util class that converts Events and its definitions in to various forms
 */
public final class EventDefinitionConverterUtils {
    public static final String NULL_STRING = "_null";
    private static Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private EventDefinitionConverterUtils() {

    }

    public static AttributeType[] generateAttributeTypeArray(List<Attribute> attributes) {
        if (attributes != null) {
            AttributeType[] attributeTypes = new AttributeType[attributes.size()];
            for (int i = 0, metaDataSize = attributes.size(); i < metaDataSize; i++) {
                Attribute attribute = attributes.get(i);
                attributeTypes[i] = attribute.getType();
            }
            return attributeTypes;
        } else {
            return null; // to improve performance
        }
    }

    public static StreamDefinition convertFromJson(String streamDefinition) throws MalformedStreamDefinitionException {
        try {
            StreamDefinition tempStreamDefinition = gson.fromJson(
                    streamDefinition.replaceAll("('|\")type('|\")\\W*:\\W*('|\")(?i)int('|\")", "'type':'INT'")
                            .replaceAll("('|\")type('|\")\\W*:\\W*('|\")(?i)long('|\")", "'type':'LONG'")
                            .replaceAll("('|\")type('|\")\\W*:\\W*('|\")(?i)float('|\")", "'type':'FLOAT'")
                            .replaceAll("('|\")type('|\")\\W*:\\W*('|\")(?i)double('|\")", "'type':'DOUBLE'")
                            .replaceAll("('|\")type('|\")\\W*:\\W*('|\")(?i)bool('|\")", "'type':'BOOL'")
                            .replaceAll("('|\")type('|\")\\W*:\\W*('|\")(?i)string('|\")", "'type':'STRING'"),
                    StreamDefinition.class);

            String name = tempStreamDefinition.getName();
            String version = tempStreamDefinition.getVersion();

            if (version == null) {
                version = "1.0.0"; // when populating the object using google gson the defaults are getting null values
            }
            if (name == null) {
                throw new MalformedStreamDefinitionException("stream name is null");
            }

            StreamDefinition newStreamDefinition = new StreamDefinition(name, version);

            boolean validAttributeList = false;

            newStreamDefinition.setTags(tempStreamDefinition.getTags());

            List<Attribute> metaList = tempStreamDefinition.getMetaData();
            validAttributeList = EventDefinitionConverterUtils.checkInvalidAttributeType(metaList, "Meta");
            if (metaList != null && metaList.size() > 0 && validAttributeList) {
                newStreamDefinition.setMetaData(metaList);
            }
            List<Attribute> correlationList = tempStreamDefinition.getCorrelationData();
            validAttributeList = EventDefinitionConverterUtils.checkInvalidAttributeType(correlationList,
                    "Correlation");
            if (correlationList != null && correlationList.size() > 0 && validAttributeList) {
                newStreamDefinition.setCorrelationData(correlationList);
            }
            List<Attribute> payloadList = tempStreamDefinition.getPayloadData();
            validAttributeList = EventDefinitionConverterUtils.checkInvalidAttributeType(payloadList, "Payload");
            if (payloadList != null && payloadList.size() > 0 && validAttributeList) {
                newStreamDefinition.setPayloadData(payloadList);
            }

            newStreamDefinition.setNickName(tempStreamDefinition.getNickName());
            newStreamDefinition.setDescription(tempStreamDefinition.getDescription());
            newStreamDefinition.setDescription(tempStreamDefinition.getDescription());
            newStreamDefinition.setTags(tempStreamDefinition.getTags());
            return newStreamDefinition;
        } catch (RuntimeException e) {
            throw new MalformedStreamDefinitionException(" Malformed stream definition " + streamDefinition, e);
        }
    }

    public static String convertToJson(List<StreamDefinition> existingDefinitions) {
        JSONArray jsonDefnArray = new JSONArray();
        for (StreamDefinition existingDefinition : existingDefinitions) {
            jsonDefnArray.put(convertToJson(existingDefinition));
        }

        return gson.toJson(existingDefinitions);
    }

    public static List<StreamDefinition> convertMultipleEventDefns(String jsonArrayOfEventDefns)
            throws MalformedStreamDefinitionException {
        try {
            JSONArray jsonArray = new JSONArray(jsonArrayOfEventDefns);
            List<StreamDefinition> streamDefinitions = new ArrayList<StreamDefinition>();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject definition = (JSONObject) jsonArray.get(i);
                streamDefinitions.add(convertFromJson(definition.toString()));
            }
            return streamDefinitions;
        } catch (JSONException e) {
            throw new MalformedStreamDefinitionException(" Malformed stream definition " + jsonArrayOfEventDefns, e);
        }

    }

    public static String convertToJson(StreamDefinition existingDefinition) {
        return gson.toJson(existingDefinition);
    }

    public static String convertToBasicJson(StreamDefinition existingDefinition) {
        return gson.toJson(new StreamDefinitionTemplate(existingDefinition));

    }

    private static boolean checkInvalidAttributeType(List<Attribute> attributeList, String attributeType)
            throws MalformedStreamDefinitionException {
        if (attributeList != null) {
            for (int i = 0; i < attributeList.size(); i++) {
                if (attributeList.get(i).getType() != null) {
                    return true;
                } else {
                    throw new MalformedStreamDefinitionException(
                            " Malformed stream definition, " + "Invalid type assigned to attribute name \""
                                    + attributeList.get(i).getName() + "\" in " + attributeType + " data attributes");
                }
            }
        }
        return false;
    }

    private static class StreamDefinitionTemplate {

        private String name;
        private String version = "1.0.0";
        private String nickName;
        private String description;
        private List<String> tags;

        private List<Attribute> metaData;
        private List<Attribute> correlationData;
        private List<Attribute> payloadData;

        public StreamDefinitionTemplate(StreamDefinition existingDefinition) {

            this.name = existingDefinition.getName();
            this.version = existingDefinition.getVersion();
            this.nickName = existingDefinition.getNickName();
            this.description = existingDefinition.getDescription();
            this.tags = existingDefinition.getTags();
            this.metaData = existingDefinition.getMetaData();
            this.correlationData = existingDefinition.getCorrelationData();
            this.payloadData = existingDefinition.getPayloadData();
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getNickName() {
            return nickName;
        }

        public void setNickName(String nickName) {
            this.nickName = nickName;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<String> getTags() {
            return tags;
        }

        public void setTags(List<String> tags) {
            this.tags = tags;
        }

        public List<Attribute> getMetaData() {
            return metaData;
        }

        public void setMetaData(List<Attribute> metaData) {
            this.metaData = metaData;
        }

        public List<Attribute> getCorrelationData() {
            return correlationData;
        }

        public void setCorrelationData(List<Attribute> correlationData) {
            this.correlationData = correlationData;
        }

        public List<Attribute> getPayloadData() {
            return payloadData;
        }

        public void setPayloadData(List<Attribute> payloadData) {
            this.payloadData = payloadData;
        }
    }
}
