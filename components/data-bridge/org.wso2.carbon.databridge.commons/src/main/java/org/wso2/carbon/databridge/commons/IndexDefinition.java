/**
 *
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.databridge.commons;

import org.wso2.carbon.databridge.commons.utils.IndexDefinitionConverterUtils;

import java.util.*;

public class IndexDefinition {
    private static final String STREAM_VERSION_KEY = "Version";
    private static final String STREAM_TIMESTAMP_KEY = "Timestamp";

    private List<Attribute> secondaryIndexData;
    private List<Attribute> customIndexData;
    private List<Attribute> fixedSearchData;
    private List<Attribute> arbitraryIndexData;

    private Map<String, Attribute> metaCustomIndex;
    private Map<String, Attribute> payloadCustomIndex;
    private Map<String, Attribute> correlationCustomIndex;
    private Map<String, Attribute> generalCustomIndex;
    private Map<String, Attribute> fixedPropertiesMap;
    private Map<String, Attribute> arbitraryIndex;

    private Set<String> metaFixProps;
    private Set<String> payloadFixProps;
    private Set<String> correlationFixProps;
    private Set<String> generalFixProps;

    private boolean isIndexTimestamp;
    private boolean isIncrementalIndex;


    public IndexDefinition() {
    }

    public void setIndexData(String indexDefnStr, StreamDefinition streamDefinition) {
        if (indexDefnStr == null) {
            return;
        }

        String secondaryIndex = IndexDefinitionConverterUtils
                .getSecondaryIndexString(indexDefnStr);
        String customIndex = IndexDefinitionConverterUtils.
                getCustomIndexString(indexDefnStr);
        String fixedIndex = IndexDefinitionConverterUtils.
                getFixedIndexString(indexDefnStr);
        String increIndex = IndexDefinitionConverterUtils.
                getIncrementalIndexString(indexDefnStr);
        String arbitraryIndex = IndexDefinitionConverterUtils.
                getArbitraryIndexString(indexDefnStr);

        Set<String> secIndexSet = new HashSet<String>();
        Set<String> custIndexSet = new HashSet<String>();
        Set<String> fixedPropertiesList = new HashSet<String>();
        Set<String> arbitraryIndexList = new HashSet<String>();
         isIncrementalIndex = false;

        if(secondaryIndex != null && !secondaryIndex.trim().isEmpty()){
            secIndexSet.addAll(Arrays.asList(secondaryIndex.split(",")));
        }

        if(customIndex != null && !customIndex.trim().isEmpty()){
            custIndexSet.addAll(Arrays.asList(customIndex.split(",")));
        }

        if(fixedIndex != null && !fixedIndex.trim().isEmpty()){
            fixedPropertiesList.addAll(Arrays.asList(fixedIndex.split(",")));
        }

        if(increIndex != null && !increIndex.trim().isEmpty()){
           isIncrementalIndex = Boolean.parseBoolean(increIndex);
        }

        if (arbitraryIndex != null && !arbitraryIndex.isEmpty()) {
            arbitraryIndexList.addAll(Arrays.asList(arbitraryIndex.split(",")));
        }

        //order matters
        Map<String, Attribute> tempFixProperties = null;

        if (secIndexSet.size() > 0) {
            secondaryIndexData = new ArrayList<Attribute>(secIndexSet.size());
        }

        if (custIndexSet.size() > 0) {
            customIndexData = new ArrayList<Attribute>(custIndexSet.size());
        }

        if (fixedPropertiesList.size() > 0) {
            fixedSearchData = new ArrayList<Attribute>(fixedPropertiesList.size());
            tempFixProperties = new HashMap<String, Attribute>(fixedPropertiesList.size());
        }

        if (streamDefinition.getMetaData() != null) {
            for (Attribute attribute : streamDefinition.getMetaData()) {
                if (secIndexSet.contains(attribute.getName())) {
                    secondaryIndexData.add(new Attribute("meta_" + attribute.getName(), attribute.getType()));
                }
                if (custIndexSet.contains(attribute.getName())) {
                    customIndexData.add(new Attribute("meta_" + attribute.getName(), attribute.getType()));
                    custIndexSet.remove(attribute.getName());
                }
                if (fixedPropertiesList.contains(attribute.getName())) {
                    tempFixProperties.put(attribute.getName(),
                                          new Attribute("meta_" + attribute.getName(), attribute.getType()));
                }
            }
        }

        if (streamDefinition.getCorrelationData() != null) {
            for (Attribute attribute : streamDefinition.getCorrelationData()) {
                if (secIndexSet.contains(attribute.getName())) {
                    secondaryIndexData.add(new Attribute("correlation_" + attribute.getName(), attribute.getType()));
                }
                if (custIndexSet.contains(attribute.getName())) {
                    customIndexData.add(new Attribute("correlation_" + attribute.getName(), attribute.getType()));
                    custIndexSet.remove(attribute.getName());
                }
                if (fixedPropertiesList.contains(attribute.getName())) {
                    tempFixProperties.put(attribute.getName(),
                                          new Attribute("correlation_" + attribute.getName(), attribute.getType()));
                }
            }
        }

        if (streamDefinition.getPayloadData() != null) {
            for (Attribute attribute : streamDefinition.getPayloadData()) {
                if (secIndexSet.contains(attribute.getName())) {
                    secondaryIndexData.add(new Attribute("payload_" + attribute.getName(), attribute.getType()));
                }
                if (custIndexSet.contains(attribute.getName())) {
                    customIndexData.add(new Attribute("payload_" + attribute.getName(), attribute.getType()));
                    custIndexSet.remove(attribute.getName());
                }
                if (fixedPropertiesList.contains(attribute.getName())) {
                    tempFixProperties.put(attribute.getName(),
                                          new Attribute("payload_" + attribute.getName(), attribute.getType()));
                }
            }
        }

        //to avoid below code block by indexing timestamp by defualt
        if (custIndexSet.contains(STREAM_TIMESTAMP_KEY)) {
            customIndexData.add(new Attribute(STREAM_TIMESTAMP_KEY, AttributeType.LONG));
            custIndexSet.remove(STREAM_TIMESTAMP_KEY);
        }

        //Non arbitary Index fieilds that are not in stream def
        for (String generalIndex : custIndexSet) {
            if(!generalIndex.trim().equals("")){
            customIndexData.add(new Attribute(generalIndex, AttributeType.STRING));
            }
        }

        if (arbitraryIndexList != null) {
            arbitraryIndexData = new ArrayList<Attribute>(arbitraryIndexList.size());
            for (String arbitraryIndexField : arbitraryIndexList) {
                String[] data = arbitraryIndexField.split(":");
                AttributeType attributeType = null;

                try {
                    attributeType = AttributeType.valueOf(data[1].trim());
                } catch (Exception e) {
                    attributeType = AttributeType.STRING;
                }
                arbitraryIndexData.add(new Attribute(data[0], attributeType));
            }
        }

        for (String property : fixedPropertiesList) {
            if (!property.equalsIgnoreCase(STREAM_VERSION_KEY)) {
                if (tempFixProperties.get(property) != null) {
                    fixedSearchData.add(tempFixProperties.get(property));
                }
            } else {
                fixedSearchData.add(new Attribute(STREAM_VERSION_KEY, AttributeType.STRING));
            }
        }
    }

    public void setIndexDataFromStore(String indexDefnStr) {
        if (indexDefnStr == null) {
            return;
        }
        List<String> secIndexList = new ArrayList<String>();
        List<String> custIndexList = new ArrayList<String>();
        List<String> fixedSearchProps = new ArrayList<String>();
        List<String> arbitraryIndexList = new ArrayList<String>();

        String secondaryIndex = IndexDefinitionConverterUtils
                .getSecondaryIndexString(indexDefnStr);
        String customIndex = IndexDefinitionConverterUtils.
                getCustomIndexString(indexDefnStr);
        String fixedIndex = IndexDefinitionConverterUtils.
                getFixedIndexString(indexDefnStr);
        String increIndex = IndexDefinitionConverterUtils.
                getIncrementalIndexString(indexDefnStr);
        String arbitraryIndexStr = IndexDefinitionConverterUtils.
                getArbitraryIndexString(indexDefnStr);

        if (secondaryIndex != null && !secondaryIndex.trim().isEmpty()) {
            secIndexList =Arrays.asList(secondaryIndex.split(","));
        }

        if (customIndex != null && !customIndex.trim().isEmpty()) {
            custIndexList = Arrays.asList(customIndex.split(","));
        }

        if (fixedIndex != null && !fixedIndex.trim().isEmpty()) {
            fixedSearchProps = Arrays.asList(fixedIndex.split(","));
        }

        if (increIndex != null && !increIndex.trim().isEmpty()) {
            isIncrementalIndex = Boolean.parseBoolean(increIndex);
        }

        if (arbitraryIndexStr != null && !arbitraryIndexStr.isEmpty()) {
            arbitraryIndexList = Arrays.asList(arbitraryIndexStr.split(","));
        }


        if (secIndexList != null) {
            secondaryIndexData = new ArrayList<Attribute>(secIndexList.size());
            for (String secIndex : secIndexList) {
                String[] secIndexData = secIndex.split(":");
                secondaryIndexData.add(new Attribute(secIndexData[0], AttributeType.valueOf(secIndexData[1].trim())));
            }
        }

        if (custIndexList != null) {
            customIndexData = new ArrayList<Attribute>(custIndexList.size());
            for (String custIndex : custIndexList) {
                String[] custIndexData = custIndex.split(":");
                String name = custIndexData[0];
                AttributeType attributeType = AttributeType.valueOf(custIndexData[1].trim());

                customIndexData.add(new Attribute(name, attributeType));

                String attributeName = name.substring(name.indexOf("_") + 1);
                if (name.startsWith("meta_")) {
                    if (metaCustomIndex == null) {
                        metaCustomIndex = new HashMap<String, Attribute>();
                    }

                    metaCustomIndex.put(attributeName,
                                        new Attribute(attributeName, attributeType));
                } else if (name.startsWith("correlation_")) {
                    if (correlationCustomIndex == null) {
                        correlationCustomIndex = new HashMap<String, Attribute>();
                    }
                    correlationCustomIndex.put(attributeName,
                                               new Attribute(attributeName, attributeType));
                } else if (name.startsWith("payload_")) {
                    if (payloadCustomIndex == null) {
                        payloadCustomIndex = new HashMap<String, Attribute>();
                    }
                    payloadCustomIndex.put(attributeName,
                                           new Attribute(attributeName, attributeType));
                } else {
                    if (generalCustomIndex == null) {
                        generalCustomIndex = new HashMap<String, Attribute>();
                    }
                    generalCustomIndex.put(name,
                                           new Attribute(name, attributeType));

                    if (attributeName.equals(STREAM_TIMESTAMP_KEY)) {
                        isIndexTimestamp = true;
                    }
                }
            }
        }

        if (custIndexList != null) {
            fixedSearchData = new ArrayList<Attribute>(fixedSearchProps.size());
            fixedPropertiesMap = new LinkedHashMap<String, Attribute>();
            for (String fixedProperty : fixedSearchProps) {
                if(!fixedProperty.isEmpty()){
                String[] fixedPropertyData = fixedProperty.split(":");
                String name = fixedPropertyData[0];
                AttributeType attributeType = AttributeType.valueOf(fixedPropertyData[1].trim());
                Attribute attribute = new Attribute(name, attributeType);

                fixedSearchData.add(attribute);
                String attributeName = name.substring(name.indexOf("_") + 1);

                fixedPropertiesMap.put(attributeName, attribute);
                if (name.startsWith("meta_")) {
                    if (metaFixProps == null) {
                        metaFixProps = new LinkedHashSet<String>();
                    }

                    metaFixProps.add(attributeName);
                } else if (name.startsWith("correlation_")) {
                    if (correlationFixProps == null) {
                        correlationFixProps = new LinkedHashSet<String>();
                    }
                    correlationFixProps.add(attributeName);
                } else if (name.startsWith("payload_")) {
                    if (payloadFixProps == null) {
                        payloadFixProps = new LinkedHashSet<String>();
                    }
                    payloadFixProps.add(attributeName);
                } else {
                    if (generalFixProps == null) {
                        generalFixProps = new LinkedHashSet<String>();
                    }
                    generalFixProps.add(name);
                }
            }
            }
        }


        if (arbitraryIndexList != null) {          //added later
            arbitraryIndexData = new ArrayList<Attribute>(arbitraryIndexList.size());
            for (String arbitraryIndexString : arbitraryIndexList) {
                String[] custIndexData = arbitraryIndexStr.split(":");
                String name = custIndexData[0];
                AttributeType attributeType = AttributeType.valueOf(custIndexData[1].trim().split(",")[0]);

                Attribute attribute = new Attribute(name, attributeType);
                arbitraryIndexData.add(attribute);
                if (arbitraryIndex == null) {
                    arbitraryIndex = new HashMap<String, Attribute>();
                }
                arbitraryIndex.put(name, attribute);
            }
        }
    }

    public List<Attribute> getSecondaryIndexData() {
        return secondaryIndexData;
    }

    public List<Attribute> getCustomIndexData() {
        return customIndexData;
    }

    public List<Attribute> getArbitraryIndexData() {
        return arbitraryIndexData;
    }

    public String getSecondaryIndexDefn() {
        return indexDefnString(secondaryIndexData);
    }

    public String getCustomIndexDefn() {
        return indexDefnString(customIndexData);
    }

    public String getFixedSearchDefn() {
        return indexDefnString(fixedSearchData);
    }

    public String getArbitraryIndexDefn() {
        return indexDefnString(arbitraryIndexData);
    }

    public String indexDefnString(List<Attribute> dataList) {
        if (dataList == null || dataList.size() == 0) {
            return null;
        }

        StringBuilder indexStringBuilder = new StringBuilder();

        boolean isRecordAdded = false;
        for (Attribute attribute : dataList) {
            if (isRecordAdded) {
                indexStringBuilder.append(",");
            }
            indexStringBuilder.append(attribute.getName())
                    .append(":").append(attribute.getType());
            isRecordAdded = true;
        }

        return indexStringBuilder.toString();
    }

    public boolean isIncrementalIndex() {
        return isIncrementalIndex;
    }

    public AttributeType getAttributeTypeforProperty(String property) {
        if (payloadCustomIndex != null && payloadCustomIndex.containsKey(property)) {
            return payloadCustomIndex.get(property).getType();
        } else if (correlationCustomIndex != null && correlationCustomIndex.containsKey(property)) {
            return correlationCustomIndex.get(property).getType();
        } else if (metaCustomIndex != null && metaCustomIndex.containsKey(property)) {
            return metaCustomIndex.get(property).getType();
        } else if (generalCustomIndex != null && generalCustomIndex.containsKey(property)) {
            return generalCustomIndex.get(property).getType();
        } else if (arbitraryIndex != null && arbitraryIndex.containsKey(property)) {
            return arbitraryIndex.get(property).getType();
        }
        return null;
    }

    public String getAttributeNameforProperty(String property) {
        if (payloadCustomIndex != null && payloadCustomIndex.containsKey(property)) {
            return "payload_" + property;
        } else if (correlationCustomIndex != null && correlationCustomIndex.containsKey(property)) {
            return "correlation_" + property;
        } else if (metaCustomIndex != null && metaCustomIndex.containsKey(property)) {
            return "meta_" + property;
        } else if (generalCustomIndex != null && generalCustomIndex.containsKey(property)) {
            return property;
        } else if (arbitraryIndex != null && arbitraryIndex.containsKey(property)) {
            return property;
        }
        return null;
    }

    public AttributeType getAttributeTypeforFixedProperty(String property) {
        if (fixedPropertiesMap == null) {
            return null;
        }
        return fixedPropertiesMap.get(property).getType();
    }

    public void clearIndexInformation() {
        if (this.secondaryIndexData != null) {
            this.secondaryIndexData.clear();
            this.secondaryIndexData = null;
        }
        if (this.customIndexData != null) {
            this.customIndexData.clear();
            this.customIndexData = null;
        }
    }

    public Map<String, Attribute> getMetaCustomIndex() {
        return metaCustomIndex;
    }

    public Map<String, Attribute> getPayloadCustomIndex() {
        return payloadCustomIndex;
    }

    public Map<String, Attribute> getCorrelationCustomIndex() {
        return correlationCustomIndex;
    }

    public Map<String, Attribute> getFixedPropertiesMap() {
        return fixedPropertiesMap;
    }

    public List<Attribute> getFixedSearchData() {
        return fixedSearchData;
    }

    public void setFixedSearchData(List<Attribute> fixedSearchData) {
        this.fixedSearchData = fixedSearchData;
    }

    public Map<String, Attribute> getGeneralCustomIndex() {
        return generalCustomIndex;
    }

    public Map<String, Attribute> getArbitraryIndex() {
        return arbitraryIndex;
    }

    public boolean isIndexTimestamp() {
        return isIndexTimestamp;
    }

    public void setIndexTimestamp(boolean indexTimestamp) {
        isIndexTimestamp = indexTimestamp;
    }

    public Set<String> getMetaFixProps() {
        return metaFixProps;
    }

    public Set<String> getPayloadFixProps() {
        return payloadFixProps;
    }

    public Set<String> getCorrelationFixProps() {
        return correlationFixProps;
    }

    public Set<String> getGeneralFixProps() {
        return generalFixProps;
    }



}
