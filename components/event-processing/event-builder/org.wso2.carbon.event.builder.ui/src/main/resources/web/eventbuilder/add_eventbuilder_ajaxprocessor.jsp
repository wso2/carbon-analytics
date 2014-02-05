<%@ page import="org.wso2.carbon.event.builder.stub.EventBuilderAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.builder.stub.types.EventBuilderConfigurationDto" %>
<%@ page import="org.wso2.carbon.event.builder.stub.types.EventBuilderPropertyDto" %>
<%@ page import="org.wso2.carbon.event.builder.ui.EventBuilderUIUtils" %>
<%--
  ~ Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~  WSO2 Inc. licenses this file to you under the Apache License,
  ~  Version 2.0 (the "License"); you may not use this file except
  ~  in compliance with the License.
  ~  You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~  Unless required by applicable law or agreed to in writing,
  ~  software distributed under the License is distributed on an
  ~  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~  KIND, either express or implied.  See the License for the
  ~  specific language governing permissions and limitations
  ~  under the License.
  --%>

<%
    // get required parameters to add a event builder to back end.
    EventBuilderAdminServiceStub stub = EventBuilderUIUtils.getEventBuilderAdminService(config, session, request);
    String eventBuilderName = request.getParameter("eventBuilderName");
    String msg = null;

    EventBuilderConfigurationDto[] eventBuilderConfigurationDtoArray = null;
    if (stub != null) {
        try {
            eventBuilderConfigurationDtoArray = stub.getAllActiveEventBuilderConfigurations();
        } catch (Exception e) {
%>
<script type="text/javascript">
    location.href = 'index.jsp?ordinal=1';</script>
<%
            return;
        }
    }

    if (eventBuilderConfigurationDtoArray != null) {
        for (EventBuilderConfigurationDto eventBuilderConfiguration : eventBuilderConfigurationDtoArray) {
            if (eventBuilderConfiguration.getEventBuilderConfigName().equals(eventBuilderName)) {
                msg = eventBuilderName + " already exists.";
                break;
            }
        }
    }

    if (stub != null) {
        if (msg == null) {
            String inputMappingType = request.getParameter("mappingType");
            String toStreamName = request.getParameter("toStreamName");
            String toStreamVersion = request.getParameter("toStreamVersion");
            String eventAdaptorInfo = request.getParameter("eventAdaptorInfo");
            String[] eventAdaptorNameAndType = eventAdaptorInfo.split("\\$=");
            // property set contains a set of properties, eg; userName$myName|url$http://wso2.org|
            String msgConfigPropertySet = request.getParameter("msgConfigPropertySet");
            EventBuilderPropertyDto[] msgConfigProperties = null;
            EventBuilderPropertyDto[] allConfigProperties;

            if (msgConfigPropertySet != null) {
                String[] properties = msgConfigPropertySet.split("\\|=");
                if (properties != null) {
                    // construct event builder property array for each event builder property
                    msgConfigProperties = new EventBuilderPropertyDto[properties.length];
                    int index = 0;
                    for (String property : properties) {
                        String[] propertyNameAndValue = property.split("\\$=");
                        if (propertyNameAndValue != null) {
                            msgConfigProperties[index] = new EventBuilderPropertyDto();
                            msgConfigProperties[index].setKey(propertyNameAndValue[0].trim());
                            msgConfigProperties[index].setValue(propertyNameAndValue[1].trim());
                            index++;
                        }
                    }
                }
            }

            if (msgConfigProperties == null) {
                msg = "No message configuration properties found.";
%>
<%=msg%>
<%
                return;
            }
            EventBuilderPropertyDto[] mappingProperties = null;
            if (inputMappingType.equals("wso2event")) {
                int mappingPropertyCount = 0;
                EventBuilderPropertyDto[] metaEbProperties = null;
                EventBuilderPropertyDto[] correlationEbProperties = null;
                EventBuilderPropertyDto[] payloadEbProperties = null;
                String customMapping = request.getParameter("customMappingValue");
                EventBuilderPropertyDto customMappingValuePropertyDto = null;
                if(customMapping != null) {
                    customMappingValuePropertyDto = new EventBuilderPropertyDto();
                    customMappingValuePropertyDto.setKey("specific_customMappingValue");
                    customMappingValuePropertyDto.setValue(customMapping);
                    mappingPropertyCount++;
                }
                if (customMapping.equalsIgnoreCase("enable")) {
                    String metaPropertySet = request.getParameter("metaData");

                    if (metaPropertySet != null && !metaPropertySet.isEmpty()) {
                        String[] properties = metaPropertySet.split("\\$=");
                        if (properties != null) {
                            // construct event builder property array for each event builder property
                            metaEbProperties = new EventBuilderPropertyDto[properties.length];
                            mappingPropertyCount += properties.length;
                            int index = 0;
                            for (String property : properties) {
                                String[] propertyNameValueAndType = property.split("\\^=");
                                if (propertyNameValueAndType != null) {
                                    metaEbProperties[index] = new EventBuilderPropertyDto();
                                    metaEbProperties[index].setKey("meta_" + propertyNameValueAndType[0].trim());
                                    metaEbProperties[index].setValue(propertyNameValueAndType[1].trim());
                                    metaEbProperties[index].setPropertyType(propertyNameValueAndType[2].trim());
                                    index++;
                                }
                            }
                        }

                    }
                    String correlationPropertySet = request.getParameter("correlationData");

                    if (correlationPropertySet != null && !correlationPropertySet.isEmpty()) {
                        String[] properties = correlationPropertySet.split("\\$=");
                        if (properties != null) {
                            // construct event builder property array for each event builder property
                            correlationEbProperties = new EventBuilderPropertyDto[properties.length];
                            mappingPropertyCount += properties.length;
                            int index = 0;
                            for (String property : properties) {
                                String[] propertyNameValueAndType = property.split("\\^=");
                                if (propertyNameValueAndType != null) {
                                    correlationEbProperties[index] = new EventBuilderPropertyDto();
                                    correlationEbProperties[index].setKey("correlation_" + propertyNameValueAndType[0].trim());
                                    correlationEbProperties[index].setValue(propertyNameValueAndType[1].trim());
                                    correlationEbProperties[index].setPropertyType(propertyNameValueAndType[2].trim());
                                    index++;
                                }
                            }
                        }

                    }
                    String payloadPropertySet = request.getParameter("payloadData");

                    if (payloadPropertySet != null && !payloadPropertySet.isEmpty()) {
                        String[] properties = payloadPropertySet.split("\\$=");
                        if (properties != null) {
                            // construct event builder property array for each event builder property
                            payloadEbProperties = new EventBuilderPropertyDto[properties.length];
                            mappingPropertyCount += properties.length;
                            int index = 0;
                            for (String property : properties) {
                                String[] propertyNameValueAndType = property.split("\\^=");
                                if (propertyNameValueAndType != null) {
                                    payloadEbProperties[index] = new EventBuilderPropertyDto();
                                    payloadEbProperties[index].setKey(propertyNameValueAndType[0].trim());
                                    payloadEbProperties[index].setValue(propertyNameValueAndType[1].trim());
                                    payloadEbProperties[index].setPropertyType(propertyNameValueAndType[2].trim());
                                    index++;
                                }
                            }
                        }

                    }
                }
                mappingProperties = new EventBuilderPropertyDto[mappingPropertyCount];
                int i = 0;
                if(customMappingValuePropertyDto != null) {
                    mappingProperties[i++] = customMappingValuePropertyDto;
                }
                if (metaEbProperties != null) {
                    for (EventBuilderPropertyDto eventBuilderPropertyDto : metaEbProperties) {
                        mappingProperties[i++] = eventBuilderPropertyDto;
                    }
                }
                if (correlationEbProperties != null) {
                    for (EventBuilderPropertyDto eventBuilderPropertyDto : correlationEbProperties) {
                        mappingProperties[i++] = eventBuilderPropertyDto;
                    }
                }
                if (payloadEbProperties != null) {
                    for (EventBuilderPropertyDto eventBuilderPropertyDto : payloadEbProperties) {
                        mappingProperties[i++] = eventBuilderPropertyDto;
                    }
                }
            } else if (inputMappingType.equals("xml")) {
                int mappingPropertyCount = 0;
                String prefixPropertySet = request.getParameter("prefixData");
                String parentSelectorXpath = request.getParameter("parentSelectorXpath");
                EventBuilderPropertyDto parentSelectorXpathProperty = null;
                if (parentSelectorXpath != null && !parentSelectorXpath.isEmpty()) {
                    parentSelectorXpathProperty = new EventBuilderPropertyDto();
                    parentSelectorXpathProperty.setKey("specific_parentSelectorXpath");
                    parentSelectorXpathProperty.setValue(parentSelectorXpath);
                    mappingPropertyCount += 1;
                }
                EventBuilderPropertyDto[] prefixEbProperties = null;
                if (prefixPropertySet != null && !prefixPropertySet.isEmpty()) {
                    String[] properties = prefixPropertySet.split("\\$=");
                    if (properties != null) {
                        // construct event builder property array for each event builder property
                        prefixEbProperties = new EventBuilderPropertyDto[properties.length];
                        mappingPropertyCount += properties.length;
                        int index = 0;
                        for (String property : properties) {
                            String[] xpathPrefixAndNs = property.split("\\^=");
                            if (xpathPrefixAndNs != null) {
                                prefixEbProperties[index] = new EventBuilderPropertyDto();
                                prefixEbProperties[index].setKey("prefix_" + xpathPrefixAndNs[0].trim());
                                prefixEbProperties[index].setValue(xpathPrefixAndNs[1].trim());
                                index++;
                            }
                        }
                    }
                }
                String xpathPropertySet = request.getParameter("xpathData");
                EventBuilderPropertyDto[] xpathEbProperties = null;
                if (xpathPropertySet != null && !xpathPropertySet.isEmpty()) {
                    String[] properties = xpathPropertySet.split("\\$=");
                    if (properties != null) {
                        // construct event builder property array for each event builder property
                        xpathEbProperties = new EventBuilderPropertyDto[properties.length];
                        mappingPropertyCount += properties.length;
                        int index = 0;
                        for (String property : properties) {
                            String[] propertyStringArr = property.split("\\^=");
                            if (propertyStringArr != null) {
                                xpathEbProperties[index] = new EventBuilderPropertyDto();
                                xpathEbProperties[index].setKey(propertyStringArr[1].trim());
                                xpathEbProperties[index].setValue(propertyStringArr[0].trim());
                                xpathEbProperties[index].setPropertyType(propertyStringArr[2].trim());
                                if (propertyStringArr.length >= 4) {
                                    xpathEbProperties[index].setDefaultValue(propertyStringArr[3].trim());
                                }
                                index++;
                            }
                        }
                    }

                }
                mappingProperties = new EventBuilderPropertyDto[mappingPropertyCount];
                int i = 0;
                if (parentSelectorXpathProperty != null) {
                    mappingProperties[i++] = parentSelectorXpathProperty;
                }
                if (prefixEbProperties != null) {
                    for (EventBuilderPropertyDto eventBuilderPropertyDto : prefixEbProperties) {
                        mappingProperties[i++] = eventBuilderPropertyDto;
                    }
                }
                if (xpathEbProperties != null) {
                    for (EventBuilderPropertyDto eventBuilderPropertyDto : xpathEbProperties) {
                        mappingProperties[i++] = eventBuilderPropertyDto;
                    }
                }
            } else if (inputMappingType.equals("map")) {
                int mappingPropertyCount = 0;
                String payloadPropertySet = request.getParameter("mapData");
                EventBuilderPropertyDto[] mapEbProperties = null;
                if (payloadPropertySet != null && !payloadPropertySet.isEmpty()) {
                    String[] properties = payloadPropertySet.split("\\$=");
                    if (properties != null) {
                        // construct event builder property array for each event builder property
                        mapEbProperties = new EventBuilderPropertyDto[properties.length];
                        mappingPropertyCount += properties.length;
                        int index = 0;
                        for (String property : properties) {
                            String[] propertyNameValueAndType = property.split("\\^=");
                            if (propertyNameValueAndType != null) {
                                mapEbProperties[index] = new EventBuilderPropertyDto();
                                mapEbProperties[index].setKey(propertyNameValueAndType[0].trim());
                                mapEbProperties[index].setValue(propertyNameValueAndType[1].trim());
                                mapEbProperties[index].setPropertyType(propertyNameValueAndType[2].trim());
                                index++;
                            }
                        }
                    }

                }
                mappingProperties = new EventBuilderPropertyDto[mappingPropertyCount];
                int i = 0;
                if (mapEbProperties != null) {
                    for (EventBuilderPropertyDto eventBuilderPropertyDto : mapEbProperties) {
                        mappingProperties[i++] = eventBuilderPropertyDto;
                    }
                }
            } else if (inputMappingType.equals("text")) {
                int mappingPropertyCount = 0;
                String textPropertySet = request.getParameter("textData");
                EventBuilderPropertyDto[] textEbProperties = null;
                if (textPropertySet != null && !textPropertySet.isEmpty()) {
                    String[] properties = textPropertySet.split("\\$=");
                    if (properties != null) {
                        // construct event builder property array for each event builder property
                        textEbProperties = new EventBuilderPropertyDto[properties.length];
                        mappingPropertyCount += properties.length;
                        int index = 0;
                        for (String property : properties) {
                            String[] propertyStringArr = property.split("\\^=");
                            if (propertyStringArr != null) {
                                textEbProperties[index] = new EventBuilderPropertyDto();
                                textEbProperties[index].setKey(propertyStringArr[0].trim());
                                textEbProperties[index].setValue(propertyStringArr[1].trim());
                                textEbProperties[index].setPropertyType(propertyStringArr[2].trim());
                                if (propertyStringArr.length >= 4) {
                                    textEbProperties[index].setDefaultValue(propertyStringArr[3].trim());
                                }
                                index++;
                            }
                        }
                    }

                }
                mappingProperties = new EventBuilderPropertyDto[mappingPropertyCount];
                int i = 0;
                if (textEbProperties != null) {
                    for (EventBuilderPropertyDto eventBuilderPropertyDto : textEbProperties) {
                        mappingProperties[i++] = eventBuilderPropertyDto;
                    }
                }
            } else if (inputMappingType.equals("json")) {
                int mappingPropertyCount = 0;
                String parentSelectorJsonPath = request.getParameter("parentSelectorJsonPath");
                EventBuilderPropertyDto parentSelectorJsonPathPropertyDto = null;
                if (parentSelectorJsonPath != null && !parentSelectorJsonPath.isEmpty()) {
                    parentSelectorJsonPathPropertyDto = new EventBuilderPropertyDto();
                    parentSelectorJsonPathPropertyDto.setKey("specific_parentSelectorJsonPath");
                    parentSelectorJsonPathPropertyDto.setValue(parentSelectorJsonPath);
                    mappingPropertyCount += 1;
                }
                String jsonPropertySet = request.getParameter("jsonData");
                EventBuilderPropertyDto[] jsonEbProperties = null;
                if (jsonPropertySet != null && !jsonPropertySet.isEmpty()) {
                    String[] properties = jsonPropertySet.split("\\*=");
                    if (properties != null) {
                        // construct event builder property array for each event builder property
                        jsonEbProperties = new EventBuilderPropertyDto[properties.length];
                        mappingPropertyCount += properties.length;
                        int index = 0;
                        for (String property : properties) {
                            String[] propertyStringArr = property.split("\\^=");
                            if (propertyStringArr != null) {
                                jsonEbProperties[index] = new EventBuilderPropertyDto();
                                jsonEbProperties[index].setKey(propertyStringArr[1].trim());
                                jsonEbProperties[index].setValue(propertyStringArr[0].trim());
                                jsonEbProperties[index].setPropertyType(propertyStringArr[2].trim());
                                if (propertyStringArr.length >= 4) {
                                    jsonEbProperties[index].setDefaultValue(propertyStringArr[3].trim());
                                }
                                index++;
                            }
                        }
                    }

                }
                mappingProperties = new EventBuilderPropertyDto[mappingPropertyCount];
                int i = 0;
                if (parentSelectorJsonPathPropertyDto != null) {
                    mappingProperties[i++] = parentSelectorJsonPathPropertyDto;
                }
                if (jsonEbProperties != null) {
                    for (EventBuilderPropertyDto eventBuilderPropertyDto : jsonEbProperties) {
                        mappingProperties[i++] = eventBuilderPropertyDto;
                    }
                }
            }

            if (mappingProperties == null) {
                allConfigProperties = msgConfigProperties;
            } else {
                allConfigProperties = new EventBuilderPropertyDto[msgConfigProperties.length + mappingProperties.length];
                int i = 0;
                for (EventBuilderPropertyDto eventBuilderPropertyDto : msgConfigProperties) {
                    allConfigProperties[i++] = eventBuilderPropertyDto;
                }
                for (EventBuilderPropertyDto eventBuilderPropertyDto : mappingProperties) {
                    eventBuilderPropertyDto.setKey(eventBuilderPropertyDto.getKey() + "_mapping");
                    allConfigProperties[i++] = eventBuilderPropertyDto;
                }
            }

            try {
                EventBuilderConfigurationDto eventBuilderConfigurationDto = new EventBuilderConfigurationDto();
                eventBuilderConfigurationDto.setInputMappingType(inputMappingType);
                eventBuilderConfigurationDto.setEventBuilderConfigName(eventBuilderName);
                if (eventAdaptorNameAndType != null) {
                    eventBuilderConfigurationDto.setInputEventAdaptorName(eventAdaptorNameAndType[0]);
                    eventBuilderConfigurationDto.setInputEventAdaptorType(eventAdaptorNameAndType[1]);
                } else {
                    throw new Exception("Event adaptor name and type not set properly.");
                }
                eventBuilderConfigurationDto.setToStreamName(toStreamName);
                eventBuilderConfigurationDto.setToStreamVersion(toStreamVersion);
                eventBuilderConfigurationDto.setEventBuilderProperties(allConfigProperties);
                stub.deployEventBuilderConfiguration(eventBuilderConfigurationDto);
                // add event builder via admin service
                msg = "true";

            } catch (Exception e) {
                msg = e.getMessage();
            }
        }
    }
%><%=msg%>
