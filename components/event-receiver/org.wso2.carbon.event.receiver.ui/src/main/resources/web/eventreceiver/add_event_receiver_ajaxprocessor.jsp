<%--
~ Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~    http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied.  See the License for the
~ specific language governing permissions and limitations
~ under the License.
--%>
<%@ page import="org.wso2.carbon.event.receiver.stub.EventReceiverAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.receiver.stub.types.BasicInputAdapterPropertyDto" %>
<%@ page import="org.wso2.carbon.event.receiver.stub.types.EventMappingPropertyDto" %>
<%@ page import="org.wso2.carbon.event.receiver.stub.types.EventReceiverConfigurationInfoDto" %>
<%@ page import="org.wso2.carbon.event.receiver.ui.EventReceiverUIConstants" %>
<%@ page import="org.wso2.carbon.event.receiver.ui.EventReceiverUIUtils" %>

<%
    // get required parameters to add a event receiver to back end.
    EventReceiverAdminServiceStub stub = EventReceiverUIUtils.getEventReceiverAdminService(config, session, request);
    String eventReceiverName = request.getParameter("eventReceiverName");
    String msg = null;
    EventReceiverConfigurationInfoDto[] eventReceiverConfigurationInfoDtoArray = null;
    if (stub != null) {
        try {
            eventReceiverConfigurationInfoDtoArray = stub.getAllActiveEventReceiverConfigurations();
        } catch (Exception e) {
%>
<script type="text/javascript">
    location.href = 'index.jsp?ordinal=1';</script>
<%
            return;
        }
    }
    if (eventReceiverConfigurationInfoDtoArray != null) {
        for (EventReceiverConfigurationInfoDto eventReceiverConfiguration : eventReceiverConfigurationInfoDtoArray) {
            if (eventReceiverConfiguration.getEventReceiverName().equals(eventReceiverName)) {
                msg = eventReceiverName + " already exists.";
                break;
            }
        }
    }
    if (stub != null) {
        try {
            if (msg == null) {
                String inputMappingType = request.getParameter("mappingType");
                String toStreamName = request.getParameter("toStreamName");
                String toStreamVersion = request.getParameter("toStreamVersion");
                String streamNameWithVersion = toStreamName + EventReceiverUIConstants.STREAM_VERSION_DELIMITER + toStreamVersion;
                String eventAdapterInfo = request.getParameter("eventAdapterInfo");
                // property set contains a set of properties, eg; userName$myName|url$http://wso2.org|
                String propertySet = request.getParameter("propertySet");
                String fromStreamNameWithVersion = "";
                BasicInputAdapterPropertyDto[] basicInputAdapterPropertyDtos = null;
                if (propertySet != null) {
                    String[] properties = propertySet.split("\\|=");
                    if (properties != null && !propertySet.equals("")) {
                        // construct event receiver property array for each event receiver property
                        basicInputAdapterPropertyDtos = new BasicInputAdapterPropertyDto[properties.length];
                        int index = 0;
                        for (String property : properties) {
                            String[] propertyNameAndValue = property.split("\\$=");
                            if (propertyNameAndValue != null) {
                                basicInputAdapterPropertyDtos[index] = new BasicInputAdapterPropertyDto();
                                basicInputAdapterPropertyDtos[index].setKey(propertyNameAndValue[0].trim());
                                basicInputAdapterPropertyDtos[index].setValue(propertyNameAndValue[1].trim());
                                index++;
                            }
                        }
                    }
                }
                if (inputMappingType.equals("wso2event")) {
                    EventMappingPropertyDto[] metaEbProperties = null;
                    EventMappingPropertyDto[] correlationEbProperties = null;
                    EventMappingPropertyDto[] payloadEbProperties = null;
                    String customMapping = request.getParameter("customMappingValue");
                    if (customMapping.equalsIgnoreCase(EventReceiverUIConstants.STRING_LITERAL_ENABLE)) {
                        fromStreamNameWithVersion = request.getParameter("fromStreamName") + EventReceiverUIConstants.STREAM_VERSION_DELIMITER + request.getParameter("fromStreamVersion");
                        String metaPropertySet = request.getParameter("metaData");
                        if (metaPropertySet != null && !metaPropertySet.isEmpty()) {
                            String[] properties = metaPropertySet.split("\\$=");
                            if (properties != null) {
                                // construct event receiver property array for each event receiver property
                                metaEbProperties = new EventMappingPropertyDto[properties.length];
                                int index = 0;
                                for (String property : properties) {
                                    String[] propertyNameValueAndType = property.split("\\^=");
                                    if (propertyNameValueAndType != null) {
                                        metaEbProperties[index] = new EventMappingPropertyDto();
                                        metaEbProperties[index].setName(propertyNameValueAndType[0].trim());
                                        metaEbProperties[index].setValueOf(propertyNameValueAndType[1].trim());
                                        metaEbProperties[index].setType(propertyNameValueAndType[2].trim());
                                        index++;
                                    }
                                }
                            }
                        }
                        String correlationPropertySet = request.getParameter("correlationData");
                        if (correlationPropertySet != null && !correlationPropertySet.isEmpty()) {
                            String[] properties = correlationPropertySet.split("\\$=");
                            if (properties != null) {
                                // construct event receiver property array for each event receiver property
                                correlationEbProperties = new EventMappingPropertyDto[properties.length];
                                int index = 0;
                                for (String property : properties) {
                                    String[] propertyNameValueAndType = property.split("\\^=");
                                    if (propertyNameValueAndType != null) {
                                        correlationEbProperties[index] = new EventMappingPropertyDto();
                                        correlationEbProperties[index].setName(propertyNameValueAndType[0].trim());
                                        correlationEbProperties[index].setValueOf(propertyNameValueAndType[1].trim());
                                        correlationEbProperties[index].setType(propertyNameValueAndType[2].trim());
                                        index++;
                                    }
                                }
                            }
                        }
                        String payloadPropertySet = request.getParameter("payloadData");
                        if (payloadPropertySet != null && !payloadPropertySet.isEmpty()) {
                            String[] properties = payloadPropertySet.split("\\$=");
                            if (properties != null) {
                                // construct event receiver property array for each event receiver property
                                payloadEbProperties = new EventMappingPropertyDto[properties.length];
                                int index = 0;
                                for (String property : properties) {
                                    String[] propertyNameValueAndType = property.split("\\^=");
                                    if (propertyNameValueAndType != null) {
                                        payloadEbProperties[index] = new EventMappingPropertyDto();
                                        payloadEbProperties[index].setName(propertyNameValueAndType[0].trim());
                                        payloadEbProperties[index].setValueOf(propertyNameValueAndType[1].trim());
                                        payloadEbProperties[index].setType(propertyNameValueAndType[2].trim());
                                        index++;
                                    }
                                }
                            }
                        }
                    }
                    stub.deployWso2EventReceiverConfiguration(eventReceiverName, streamNameWithVersion, eventAdapterInfo, metaEbProperties, correlationEbProperties, payloadEbProperties,
                            basicInputAdapterPropertyDtos, EventReceiverUIConstants.STRING_LITERAL_ENABLE.equalsIgnoreCase(customMapping), fromStreamNameWithVersion);
                } else if (inputMappingType.equals("xml")) {
                    String prefixPropertySet = request.getParameter("prefixData");
                    String parentSelectorXpath = request.getParameter("parentSelectorXpath");
                    EventMappingPropertyDto[] namespaceProperties = null;
                    EventMappingPropertyDto[] xpathProperties = null;
                    String customMapping = request.getParameter("customMappingValue");
                    if (customMapping.equalsIgnoreCase("enable")) {
                        if (prefixPropertySet != null && !prefixPropertySet.isEmpty()) {
                            String[] properties = prefixPropertySet.split("\\$=");
                            if (properties != null) {
                                // construct event receiver property array for each event receiver property
                                namespaceProperties = new EventMappingPropertyDto[properties.length];
                                int index = 0;
                                for (String property : properties) {
                                    String[] prefixAndNs = property.split("\\^=");
                                    if (prefixAndNs != null) {
                                        namespaceProperties[index] = new EventMappingPropertyDto();
                                        namespaceProperties[index].setName(prefixAndNs[0].trim());
                                        namespaceProperties[index].setValueOf(prefixAndNs[1].trim());
                                        index++;
                                    }
                                }
                            }
                        }
                        String xpathPropertySet = request.getParameter("xpathData");
                        if (xpathPropertySet != null && !xpathPropertySet.isEmpty()) {
                            String[] properties = xpathPropertySet.split("\\$=");
                            if (properties != null) {
                                // construct event receiver property array for each event receiver property
                                xpathProperties = new EventMappingPropertyDto[properties.length];
                                int index = 0;
                                for (String property : properties) {
                                    String[] propertyStringArr = property.split("\\^=");
                                    if (propertyStringArr != null) {
                                        xpathProperties[index] = new EventMappingPropertyDto();
                                        xpathProperties[index].setName(propertyStringArr[0].trim());
                                        xpathProperties[index].setValueOf(propertyStringArr[1].trim());
                                        xpathProperties[index].setType(propertyStringArr[2].trim());
                                        if (propertyStringArr.length >= 4) {
                                            xpathProperties[index].setDefaultValue(propertyStringArr[3].trim());
                                        }
                                        index++;
                                    }
                                }
                            }
                        }
                    }
                    stub.deployXmlEventReceiverConfiguration(eventReceiverName, streamNameWithVersion, eventAdapterInfo,
                            parentSelectorXpath, namespaceProperties, xpathProperties, basicInputAdapterPropertyDtos,
                            EventReceiverUIConstants.STRING_LITERAL_ENABLE.equalsIgnoreCase(customMapping));
                } else if (inputMappingType.equals("map")) {
                    String payloadPropertySet = request.getParameter("mapData");
                    EventMappingPropertyDto[] mapEbProperties = null;
                    String customMapping = request.getParameter("customMappingValue");
                    if (payloadPropertySet != null && !payloadPropertySet.isEmpty()) {
                        String[] properties = payloadPropertySet.split("\\$=");
                        if (properties != null) {
                            // construct event receiver property array for each event receiver property
                            mapEbProperties = new EventMappingPropertyDto[properties.length];
                            int index = 0;
                            for (String property : properties) {
                                String[] propertyNameValueAndType = property.split("\\^=");
                                if (propertyNameValueAndType != null) {
                                    mapEbProperties[index] = new EventMappingPropertyDto();
                                    mapEbProperties[index].setName(propertyNameValueAndType[0].trim());
                                    mapEbProperties[index].setValueOf(propertyNameValueAndType[1].trim());
                                    mapEbProperties[index].setType(propertyNameValueAndType[2].trim());
                                    mapEbProperties[index].setDefaultValue(propertyNameValueAndType[3].trim());
                                    index++;
                                }
                            }
                        }
                    }
                    stub.deployMapEventReceiverConfiguration(eventReceiverName, streamNameWithVersion, eventAdapterInfo,
                            mapEbProperties, basicInputAdapterPropertyDtos,
                            EventReceiverUIConstants.STRING_LITERAL_ENABLE.equalsIgnoreCase(customMapping));
                } else if (inputMappingType.equals("text")) {
                    String textPropertySet = request.getParameter("textData");
                    EventMappingPropertyDto[] textEbProperties = null;
                    String customMapping = request.getParameter("customMappingValue");
                    if (textPropertySet != null && !textPropertySet.isEmpty()) {
                        String[] properties = textPropertySet.split("\\$=");
                        if (properties != null) {
                            // construct event receiver property array for each event receiver property
                            textEbProperties = new EventMappingPropertyDto[properties.length];
                            int index = 0;
                            for (String property : properties) {
                                String[] propertyStringArr = property.split("\\^=");
                                if (propertyStringArr != null) {
                                    textEbProperties[index] = new EventMappingPropertyDto();
                                    textEbProperties[index].setName(propertyStringArr[1].trim());
                                    textEbProperties[index].setValueOf(propertyStringArr[0].trim());
                                    textEbProperties[index].setType(propertyStringArr[2].trim());
                                    if (propertyStringArr.length >= 4) {
                                        textEbProperties[index].setDefaultValue(propertyStringArr[3].trim());
                                    }
                                    index++;
                                }
                            }
                        }
                    }
                    stub.deployTextEventReceiverConfiguration(eventReceiverName, streamNameWithVersion, eventAdapterInfo,
                            textEbProperties, basicInputAdapterPropertyDtos,
                            EventReceiverUIConstants.STRING_LITERAL_ENABLE.equalsIgnoreCase(customMapping));
                } else if (inputMappingType.equals("json")) {
                    String customMapping = request.getParameter("customMappingValue");
                    String jsonPropertySet = request.getParameter("jsonData");
                    EventMappingPropertyDto[] jsonEbProperties = null;
                    if (jsonPropertySet != null && !jsonPropertySet.isEmpty()) {
                        String[] properties = jsonPropertySet.split("\\*=");
                        if (properties != null) {
                            // construct event receiver property array for each event receiver property
                            jsonEbProperties = new EventMappingPropertyDto[properties.length];
                            int index = 0;
                            for (String property : properties) {
                                String[] propertyStringArr = property.split("\\^=");
                                if (propertyStringArr != null) {
                                    jsonEbProperties[index] = new EventMappingPropertyDto();
                                    jsonEbProperties[index].setName(propertyStringArr[0].trim());
                                    jsonEbProperties[index].setValueOf(propertyStringArr[1].trim());
                                    jsonEbProperties[index].setType(propertyStringArr[2].trim());
                                    if (propertyStringArr.length >= 4) {
                                        jsonEbProperties[index].setDefaultValue(propertyStringArr[3].trim());
                                    }
                                    index++;
                                }
                            }
                        }
                    }
                    stub.deployJsonEventReceiverConfiguration(eventReceiverName, streamNameWithVersion, eventAdapterInfo,
                            jsonEbProperties, basicInputAdapterPropertyDtos,
                            EventReceiverUIConstants.STRING_LITERAL_ENABLE.equalsIgnoreCase(customMapping));
                }
                msg = "true";
            }
        } catch (Throwable t) {
            msg = t.getMessage();
        }
    }
%><%=msg%>