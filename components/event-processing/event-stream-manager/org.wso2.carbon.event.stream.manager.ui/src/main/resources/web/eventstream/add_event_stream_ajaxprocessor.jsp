
<%@ page import="org.wso2.carbon.event.stream.manager.ui.EventStreamUIUtils" %>
<%@ page import="org.wso2.carbon.event.stream.manager.stub.EventStreamAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.stream.manager.stub.types.EventStreamAttributeDto" %>

<%

    String msg = null;
    try {
        EventStreamAdminServiceStub stub = EventStreamUIUtils.getEventStreamAdminService(config, session, request);

        String eventStreamName = request.getParameter("eventStreamName");
        String eventStreamVersion = request.getParameter("eventStreamVersion");
        String eventStreamDescription = request.getParameter("eventStreamDescription");
        String eventStreamNickName = request.getParameter("eventStreamNickName");

        String metaDataSet = request.getParameter("metaData");
        EventStreamAttributeDto[] metaWSO2EventAttributeDtos = null;

        if (metaDataSet != null && !metaDataSet.equals("")) {
            String[] properties = metaDataSet.split("\\$=");
            if (properties != null) {
                // construct property array for each property
                metaWSO2EventAttributeDtos = new EventStreamAttributeDto[properties.length];
                int index = 0;
                for (String property : properties) {
                    String[] propertyConfiguration = property.split("\\^=");
                    if (propertyConfiguration != null) {
                        metaWSO2EventAttributeDtos[index] = new EventStreamAttributeDto();
                        metaWSO2EventAttributeDtos[index].setAttributeName(propertyConfiguration[0].trim());
                        metaWSO2EventAttributeDtos[index].setAttributeType(propertyConfiguration[1].trim());
                        index++;
                    }
                }

            }
        }

        String correlationDataSet = request.getParameter("correlationData");
        EventStreamAttributeDto[] correlationWSO2EventAttributeDtos = null;

        if (correlationDataSet != null && !correlationDataSet.equals("")) {
            String[] properties = correlationDataSet.split("\\$=");
            if (properties != null) {
                // construct property array for each property
                correlationWSO2EventAttributeDtos = new EventStreamAttributeDto[properties.length];
                int index = 0;
                for (String property : properties) {
                    String[] propertyConfiguration = property.split("\\^=");
                    if (propertyConfiguration != null) {
                        correlationWSO2EventAttributeDtos[index] = new EventStreamAttributeDto();
                        correlationWSO2EventAttributeDtos[index].setAttributeName(propertyConfiguration[0].trim());
                        correlationWSO2EventAttributeDtos[index].setAttributeType(propertyConfiguration[1].trim());
                        index++;
                    }
                }

            }
        }

        String payloadDataSet = request.getParameter("payloadData");
        EventStreamAttributeDto[] payloadWSO2EventAttributeDtos = null;

        if (payloadDataSet != null && !payloadDataSet.equals("")) {
            String[] properties = payloadDataSet.split("\\$=");
            if (properties != null) {
                // construct property array for each property
                payloadWSO2EventAttributeDtos = new EventStreamAttributeDto[properties.length];
                int index = 0;
                for (String property : properties) {
                    String[] propertyConfiguration = property.split("\\^=");
                    if (propertyConfiguration != null) {
                        payloadWSO2EventAttributeDtos[index] = new EventStreamAttributeDto();
                        payloadWSO2EventAttributeDtos[index].setAttributeName(propertyConfiguration[0].trim());
                        payloadWSO2EventAttributeDtos[index].setAttributeType(propertyConfiguration[1].trim());
                        index++;
                    }
                }

            }
        }

        stub.addEventStreamInfo(eventStreamName, eventStreamVersion,metaWSO2EventAttributeDtos,correlationWSO2EventAttributeDtos,payloadWSO2EventAttributeDtos,eventStreamDescription,eventStreamNickName);
        msg = "true";

    } catch (Exception e) {
        msg = e.getMessage();
    }
%>
<%=msg%>
