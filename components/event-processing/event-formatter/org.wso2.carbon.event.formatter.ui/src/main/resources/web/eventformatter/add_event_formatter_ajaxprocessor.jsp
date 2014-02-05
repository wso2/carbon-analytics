<%@ page import="org.wso2.carbon.event.formatter.stub.EventFormatterAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.formatter.stub.types.EventFormatterPropertyDto" %>
<%@ page import="org.wso2.carbon.event.formatter.stub.types.EventOutputPropertyConfigurationDto" %>
<%@ page import="org.wso2.carbon.event.formatter.ui.EventFormatterUIConstants" %>
<%@ page import="org.wso2.carbon.event.formatter.ui.EventFormatterUIUtils" %>
<%


    String msg = null;
    try {
        EventFormatterAdminServiceStub stub = EventFormatterUIUtils.getEventFormatterAdminService(config, session, request);

        String eventFormatterName = request.getParameter("eventFormatter");
        String streamNameWithVersion = request.getParameter("streamNameWithVersion");
        String eventAdaptorInfo = request.getParameter("eventAdaptorInfo");
        String[] eventAdaptorNameAndType = eventAdaptorInfo.split("\\$=");
        String eventAdaptorName = null;
        String eventAdaptorType = null;
        if (eventAdaptorNameAndType != null && eventAdaptorNameAndType.length >= 2) {
            eventAdaptorName = eventAdaptorNameAndType[0];
            eventAdaptorType = eventAdaptorNameAndType[1];
        } else {
            throw new Exception("Could not retrieve event adaptor information properly");
        }
        String outputParameterSet = request.getParameter("outputParameters");
        String mappingType = request.getParameter("mappingType");

        EventFormatterPropertyDto[] eventFormatterProperties = null;
        msg="While setting output parameters";
        if (outputParameterSet != null && !outputParameterSet.equals("")) {
            String[] properties = outputParameterSet.split("\\|=");
            if (properties != null) {
                // construct property array for each property
                eventFormatterProperties = new EventFormatterPropertyDto[properties.length];
                int index = 0;
                for (String property : properties) {
                    String[] propertyNameAndValue = property.split("\\$=");
                    if (propertyNameAndValue != null) {
                        eventFormatterProperties[index] = new EventFormatterPropertyDto();
                        eventFormatterProperties[index].setKey(propertyNameAndValue[0].trim());
                        eventFormatterProperties[index].setValue(propertyNameAndValue[1].trim());
                        index++;
                    }
                }

            }
        }

        if (mappingType.equals("wso2event")) {
            String customMapping = request.getParameter("customMappingValue");
            String metaDataSet = request.getParameter("metaData");

            boolean customMappingEnabled = EventFormatterUIConstants.STRING_LITERAL_ENABLE.equalsIgnoreCase(customMapping);
            EventOutputPropertyConfigurationDto[] metaWSO2EventConfiguration = null;

            if (metaDataSet != null && !metaDataSet.equals("")) {
                String[] properties = metaDataSet.split("\\$=");
                if (properties != null) {
                    // construct property array for each property
                    metaWSO2EventConfiguration = new EventOutputPropertyConfigurationDto[properties.length];
                    int index = 0;
                    for (String property : properties) {
                        String[] propertyConfiguration = property.split("\\^=");
                        if (propertyConfiguration != null) {
                            metaWSO2EventConfiguration[index] = new EventOutputPropertyConfigurationDto();
                            metaWSO2EventConfiguration[index].setName(propertyConfiguration[0].trim());
                            metaWSO2EventConfiguration[index].setValueOf(propertyConfiguration[1].trim());
                            index++;
                        }
                    }

                }
            }

            String correlationDataSet = request.getParameter("correlationData");
            EventOutputPropertyConfigurationDto[] correlationWSO2EventConfiguration = null;

            if (correlationDataSet != null && !correlationDataSet.equals("")) {
                String[] properties = correlationDataSet.split("\\$=");
                if (properties != null) {
                    // construct property array for each property
                    correlationWSO2EventConfiguration = new EventOutputPropertyConfigurationDto[properties.length];
                    int index = 0;
                    for (String property : properties) {
                        String[] propertyConfiguration = property.split("\\^=");
                        if (propertyConfiguration != null) {
                            correlationWSO2EventConfiguration[index] = new EventOutputPropertyConfigurationDto();
                            correlationWSO2EventConfiguration[index].setName(propertyConfiguration[0].trim());
                            correlationWSO2EventConfiguration[index].setValueOf(propertyConfiguration[1].trim());
                            index++;
                        }
                    }

                }
            }

            String payloadDataSet = request.getParameter("payloadData");
            EventOutputPropertyConfigurationDto[] payloadWSO2EventConfiguration = null;

            if (payloadDataSet != null && !payloadDataSet.equals("")) {
                String[] properties = payloadDataSet.split("\\$=");
                if (properties != null) {
                    // construct property array for each property
                    payloadWSO2EventConfiguration = new EventOutputPropertyConfigurationDto[properties.length];
                    int index = 0;
                    for (String property : properties) {
                        String[] propertyConfiguration = property.split("\\^=");
                        if (propertyConfiguration != null) {
                            payloadWSO2EventConfiguration[index] = new EventOutputPropertyConfigurationDto();
                            payloadWSO2EventConfiguration[index].setName(propertyConfiguration[0].trim());
                            payloadWSO2EventConfiguration[index].setValueOf(propertyConfiguration[1].trim());
                            index++;
                        }
                    }

                }
            }
            // add event adaptor via admin service
            stub.deployWSO2EventFormatterConfiguration(eventFormatterName, streamNameWithVersion, eventAdaptorName, eventAdaptorType, metaWSO2EventConfiguration, correlationWSO2EventConfiguration, payloadWSO2EventConfiguration, eventFormatterProperties,customMappingEnabled);
            msg = "true";
        } else if (mappingType.equals("text")) {
            String dataSet = request.getParameter("textData");
            String dataFrom = request.getParameter("dataFrom");

            // add event adaptor via admin service
            stub.deployTextEventFormatterConfiguration(eventFormatterName, streamNameWithVersion, eventAdaptorName, eventAdaptorType, dataSet, eventFormatterProperties, dataFrom);
            msg = "true";

        } else if (mappingType.equals("xml")) {
            String dataSet = request.getParameter("textData");
            String dataFrom = request.getParameter("dataFrom");

            // add event adaptor via admin service
            stub.deployXmlEventFormatterConfiguration(eventFormatterName, streamNameWithVersion, eventAdaptorName, eventAdaptorType, dataSet, eventFormatterProperties, dataFrom);
            msg = "true";

        } else if (mappingType.equals("map")) {

            String mapDataSet = request.getParameter("mapData");
            EventOutputPropertyConfigurationDto[] eventOutputPropertyConfiguration = null;

            if (mapDataSet != null && !mapDataSet.equals("")) {
                String[] properties = mapDataSet.split("\\$=");
                if (properties != null) {
                    // construct property array for each property
                    eventOutputPropertyConfiguration = new EventOutputPropertyConfigurationDto[properties.length];
                    int index = 0;
                    for (String property : properties) {
                        String[] propertyConfiguration = property.split("\\^=");
                        if (propertyConfiguration != null) {
                            eventOutputPropertyConfiguration[index] = new EventOutputPropertyConfigurationDto();
                            eventOutputPropertyConfiguration[index].setName(propertyConfiguration[0].trim());
                            eventOutputPropertyConfiguration[index].setValueOf(propertyConfiguration[1].trim());
                            index++;
                        }
                    }

                }
            }

            // add event adaptor via admin service
            stub.deployMapEventFormatterConfiguration(eventFormatterName, streamNameWithVersion, eventAdaptorName, eventAdaptorType, eventOutputPropertyConfiguration, eventFormatterProperties);
            msg = "true";

        } else if (mappingType.equals("json")) {
            String dataSet = request.getParameter("jsonData");
            String dataFrom = request.getParameter("dataFrom");
            // add event adaptor via admin service
            stub.deployJsonEventFormatterConfiguration(eventFormatterName, streamNameWithVersion, eventAdaptorName, eventAdaptorType, dataSet, eventFormatterProperties, dataFrom);
            msg = "true";
        }


    } catch (Exception e) {
        msg = e.getMessage();
    }

%>
<%=msg%>
