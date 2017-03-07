<%--
  ~ Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License"); you may not
  ~ use this file except in compliance with the License. You may obtain a copy
  ~ of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software distributed
  ~ under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
  ~ CONDITIONS OF ANY KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations under the License.
  --%>
<%@ page import="org.wso2.carbon.event.publisher.stub.EventPublisherAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.publisher.stub.types.BasicOutputAdapterPropertyDto" %>
<%@ page import="org.wso2.carbon.event.publisher.stub.types.EventMappingPropertyDto" %>
<%@ page import="org.wso2.carbon.event.publisher.ui.EventPublisherUIConstants" %>
<%@ page import="org.wso2.carbon.event.publisher.ui.EventPublisherUIUtils" %>
<%

    if (!"post".equalsIgnoreCase(request.getMethod())) {
        response.sendError(405);
        return;
    }

    String msg = null;
    try {
        EventPublisherAdminServiceStub stub = EventPublisherUIUtils.getEventPublisherAdminService(config, session, request);

        String eventPublisherName = request.getParameter("eventPublisher");
        String streamNameWithVersion = request.getParameter("streamNameWithVersion");
        String eventAdapterType = request.getParameter("eventAdapterInfo");

        String customMapping = request.getParameter("customMappingValue");
        boolean customMappingEnabled = EventPublisherUIConstants.STRING_LITERAL_ENABLE.equalsIgnoreCase(customMapping);
        String toStreamNameWithVersion = "";

        if (eventAdapterType == null ) {
            throw new Exception("Could not retrieve event adapter type information properly");
        }
        String outputParameterSet = request.getParameter("outputParameters");
        String mappingType = request.getParameter("mappingType");

        BasicOutputAdapterPropertyDto[] eventPublisherProperties = null;
        msg = "While setting output parameters";
        if (outputParameterSet != null && !outputParameterSet.equals("")) {
            String[] properties = outputParameterSet.split("\\|=");
            if (properties != null) {
                // construct property array for each property
                eventPublisherProperties = new BasicOutputAdapterPropertyDto[properties.length];
                int index = 0;
                for (String property : properties) {
                    String[] propertyNameAndValue = property.split("\\$=");
                    if (propertyNameAndValue != null) {
                        eventPublisherProperties[index] = new BasicOutputAdapterPropertyDto();
                        eventPublisherProperties[index].setKey(propertyNameAndValue[0].trim());
                        eventPublisherProperties[index].setValue(propertyNameAndValue[1].trim());
                        index++;
                    }
                }

            }
        }

        if (mappingType.equals("wso2event")) {
            EventMappingPropertyDto[] metaWSO2EventConfiguration = null;
            EventMappingPropertyDto[] correlationWSO2EventConfiguration = null;
            EventMappingPropertyDto[] payloadWSO2EventConfiguration = null;

            if(customMappingEnabled){
                toStreamNameWithVersion = request.getParameter("toStreamName") + EventPublisherUIConstants.STREAM_VERSION_DELIMITER + request.getParameter("toStreamVersion");

                String metaDataSet = request.getParameter("metaData");
                if (metaDataSet != null && !metaDataSet.equals("")) {
                    String[] properties = metaDataSet.split("\\$=");
                    if (properties != null) {
                        // construct property array for each property
                        metaWSO2EventConfiguration = new EventMappingPropertyDto[properties.length];
                        int index = 0;
                        for (String property : properties) {
                            String[] propertyConfiguration = property.split("\\^=");
                            if (propertyConfiguration != null) {
                                metaWSO2EventConfiguration[index] = new EventMappingPropertyDto();
                                metaWSO2EventConfiguration[index].setName(propertyConfiguration[0].trim());
                                metaWSO2EventConfiguration[index].setValueOf(propertyConfiguration[1].trim());
                                index++;
                            }
                        }

                    }
                }

                String correlationDataSet = request.getParameter("correlationData");
                if (correlationDataSet != null && !correlationDataSet.equals("")) {
                    String[] properties = correlationDataSet.split("\\$=");
                    if (properties != null) {
                        // construct property array for each property
                        correlationWSO2EventConfiguration = new EventMappingPropertyDto[properties.length];
                        int index = 0;
                        for (String property : properties) {
                            String[] propertyConfiguration = property.split("\\^=");
                            if (propertyConfiguration != null) {
                                correlationWSO2EventConfiguration[index] = new EventMappingPropertyDto();
                                correlationWSO2EventConfiguration[index].setName(propertyConfiguration[0].trim());
                                correlationWSO2EventConfiguration[index].setValueOf(propertyConfiguration[1].trim());
                                index++;
                            }
                        }

                    }
                }

                String payloadDataSet = request.getParameter("payloadData");
                if (payloadDataSet != null && !payloadDataSet.equals("")) {
                    String[] properties = payloadDataSet.split("\\$=");
                    if (properties != null) {
                        // construct property array for each property
                        payloadWSO2EventConfiguration = new EventMappingPropertyDto[properties.length];
                        int index = 0;
                        for (String property : properties) {
                            String[] propertyConfiguration = property.split("\\^=");
                            if (propertyConfiguration != null) {
                                payloadWSO2EventConfiguration[index] = new EventMappingPropertyDto();
                                payloadWSO2EventConfiguration[index].setName(propertyConfiguration[0].trim());
                                payloadWSO2EventConfiguration[index].setValueOf(propertyConfiguration[1].trim());
                                index++;
                            }
                        }

                    }
                }
            }
            // add event adapter via admin service
            stub.deployWSO2EventPublisherConfiguration(eventPublisherName, streamNameWithVersion, eventAdapterType, metaWSO2EventConfiguration, correlationWSO2EventConfiguration, payloadWSO2EventConfiguration, eventPublisherProperties, customMappingEnabled, toStreamNameWithVersion);
            msg = "true";
        } else if (mappingType.equals("text")) {
            String dataSet = request.getParameter("textData");
            String dataFrom = request.getParameter("dataFrom");
            long cacheTimeoutDuration = Long.parseLong(request.getParameter("cacheTimeoutDuration"));

            // add event adapter via admin service
            stub.deployCacheableTextEventPublisherConfiguration(eventPublisherName, streamNameWithVersion, eventAdapterType, dataSet, eventPublisherProperties, dataFrom, cacheTimeoutDuration, customMappingEnabled);
            msg = "true";

        } else if (mappingType.equals("xml")) {
            String dataSet = request.getParameter("textData");
            String dataFrom = request.getParameter("dataFrom");
            long cacheTimeoutDuration = Long.parseLong(request.getParameter("cacheTimeoutDuration"));

            // add event adapter via admin service
            stub.deployCacheableXmlEventPublisherConfiguration(eventPublisherName, streamNameWithVersion, eventAdapterType, dataSet, eventPublisherProperties, dataFrom, cacheTimeoutDuration, customMappingEnabled);
            msg = "true";

        } else if (mappingType.equals("map")) {

            String mapDataSet = request.getParameter("mapData");
            EventMappingPropertyDto[] eventOutputPropertyConfiguration = null;

            if (mapDataSet != null && !mapDataSet.equals("")) {
                String[] properties = mapDataSet.split("\\$=");
                if (properties != null) {
                    // construct property array for each property
                    eventOutputPropertyConfiguration = new EventMappingPropertyDto[properties.length];
                    int index = 0;
                    for (String property : properties) {
                        String[] propertyConfiguration = property.split("\\^=");
                        if (propertyConfiguration != null) {
                            eventOutputPropertyConfiguration[index] = new EventMappingPropertyDto();
                            eventOutputPropertyConfiguration[index].setName(propertyConfiguration[0].trim());
                            eventOutputPropertyConfiguration[index].setValueOf(propertyConfiguration[1].trim());
                            index++;
                        }
                    }

                }
            }

            // add event adapter via admin service
            stub.deployMapEventPublisherConfiguration(eventPublisherName, streamNameWithVersion, eventAdapterType, eventOutputPropertyConfiguration, eventPublisherProperties, customMappingEnabled);
            msg = "true";

        } else if (mappingType.equals("json")) {
            String dataSet = request.getParameter("jsonData");
            String dataFrom = request.getParameter("dataFrom");
            long cacheTimeoutDuration = Long.parseLong(request.getParameter("cacheTimeoutDuration"));

            // add event adapter via admin service
            stub.deployCacheableJsonEventPublisherConfiguration(eventPublisherName, streamNameWithVersion, eventAdapterType, dataSet, eventPublisherProperties, dataFrom, cacheTimeoutDuration, customMappingEnabled);
            msg = "true";
        }


    } catch (Exception e) {
        msg = e.getMessage();
    }

%>
<%=msg%>
