<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>
<%@ page import="org.wso2.carbon.event.template.manager.stub.TemplateManagerAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.template.manager.ui.TemplateManagerUIUtils" %>
<%@ page import="org.wso2.carbon.event.template.manager.admin.dto.configuration.xsd.ScenarioConfigurationDTO" %>
<%@ page import="org.wso2.carbon.event.template.manager.admin.dto.configuration.xsd.ConfigurationParameterDTO" %>
<%@ page import="org.wso2.carbon.event.template.manager.admin.dto.configuration.xsd.StreamMappingDTO" %>
<%@ page import="org.wso2.carbon.event.template.manager.admin.dto.configuration.xsd.AttributeMappingDTO" %>
<%@ page import="org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto" %>
<%@ page import="org.wso2.carbon.event.stream.stub.types.EventStreamDefinitionDto" %>
<%@ page import="org.apache.axis2.AxisFault" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="org.wso2.carbon.event.stream.stub.EventStreamAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.template.manager.ui.TemplateManagerUIConstants" %>
<%@ page import="java.util.ArrayList" %>

<%--
  ~ Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>

<fmt:bundle basename="org.wso2.carbon.event.template.manager.ui.i18n.Resources">
    <%

        if (!"post".equalsIgnoreCase(request.getMethod())) {
            response.sendError(405);
            return;
        }

        boolean isUpdate = "true".equals(request.getParameter("isUpdate")) ? true : false;
        String domainName = request.getParameter("domainName");
        String configuration = request.getParameter("configurationName");
        String saveType = request.getParameter("saveType");
        String description = request.getParameter("description");
        String parametersJson = request.getParameter("parameters");
        String templateType = request.getParameter("templateType");
        String valueSeparator = "::";

        ConfigurationParameterDTO[] parameters;

        TemplateManagerAdminServiceStub proxy = TemplateManagerUIUtils.getTemplateManagerAdminService(config, session);
        try {
            if (saveType.equals("delete")) {
                proxy.deleteConfiguration(domainName, configuration);
            } else {

                ScenarioConfigurationDTO scenarioConfigurationDTO = new ScenarioConfigurationDTO();

                scenarioConfigurationDTO.setName(configuration);
                scenarioConfigurationDTO.setDomain(domainName);
                scenarioConfigurationDTO.setDescription(description);
                scenarioConfigurationDTO.setType(templateType);

                if (parametersJson.length() < 1) {
                    parameters = new ConfigurationParameterDTO[0];

                } else {
                    String[] parameterStrings = parametersJson.split(",\n");
                    parameters = new ConfigurationParameterDTO[parameterStrings.length];
                    int index = 0;

                    for (String parameterString : parameterStrings) {
                        String[] parameterEntities = parameterString.trim().split(valueSeparator);
                        ConfigurationParameterDTO parameterDTO = new ConfigurationParameterDTO();
                        parameterDTO.setName(parameterEntities[0]);
                        parameterDTO.setValue(parameterEntities[1]);
                        parameters[index] = parameterDTO;
                        index++;
                    }
                }

                scenarioConfigurationDTO.setConfigurationParameterDTOs(parameters);

                //toStreamIDArray.length defines the number of stream mappings per configuration
                String[] toStreamIDArray;
                if(isUpdate) {
                    toStreamIDArray = proxy.editConfiguration(scenarioConfigurationDTO);
                } else {
                    toStreamIDArray = proxy.saveConfiguration(scenarioConfigurationDTO);
                }

                //when stream mapping is disabled, stub returns a string array with a null element hence need to check for toStreamIDArray[0]
                if (toStreamIDArray[0] != null) {
                    String toStreamNameID = "";
                    String fromStreamNameID = "";
                    StreamMappingDTO[] streamMappingDTOs = null;
                    AttributeMappingDTO[] attributeMappingDTOs=null;
                    boolean isStreamChanged=false;

                    EventStreamAdminServiceStub eventStreamAdminServiceStub = TemplateManagerUIUtils.getEventStreamAdminService(config,
                            session, request);
                    String[] fromStreamIds = eventStreamAdminServiceStub.getStreamNames();

                    //if update then get stream mapping dtos
                    if (((StreamMappingDTO[]) session.getAttribute("streamMappingDTOs")) != null) {
                        streamMappingDTOs = (StreamMappingDTO[]) session.getAttribute("streamMappingDTOs");
                    } else {
                        response.sendError(500);
                        return;
                    }

                    for (int i = 0; i < toStreamIDArray.length; i++) {
                        //Compare return streamID with already saved configuration streamId to check whether streamID has been changed during edit
                        if (streamMappingDTOs[i].getToStream().equals(toStreamIDArray[i])) {
                            toStreamNameID = streamMappingDTOs[i].getToStream();
                            fromStreamNameID = streamMappingDTOs[i].getFromStream();
                            attributeMappingDTOs = streamMappingDTOs[i].getAttributeMappingDTOs();
                        } else {
                            toStreamNameID = toStreamIDArray[i];
                            isStreamChanged = true;
                        }
    %>
        <div class="container col-md-12 marg-top-20" id="streamMappingConfigurationID_<%=i%>">

            <h4><fmt:message key='template.stream.header.text'/></h4>

            <label class="input-label col-md-5"><fmt:message key='template.label.from.stream.name'/></label>

            <div class="input-control input-full-width col-md-7 text">
                <select id="fromStreamID_<%=i%>" onchange="loadMappingFromStreamAttributes(<%=i%>)">
                    <%if (isStreamChanged == false) {%>
                    <option selected><%=fromStreamNameID%>
                    </option>
                    <%} else {%>
                    <option selected disabled>Select an input stream to map</option>
                    <%
                        }
                        if (fromStreamIds != null) {
                            Arrays.sort(fromStreamIds);
                            for (String aStreamId : fromStreamIds) {
                                if(!fromStreamNameID.equals(aStreamId)){
                    %>
                    <option id="fromStreamOptionID"><%=aStreamId%>
                    </option>
                    <%
                                }
                            }
                        }
                    %>
                </select>
            </div>

            <label class="input-label col-md-5"><fmt:message key='template.label.to.stream.name'/></label>

            <div class="input-control input-full-width col-md-7 text">
                <input type="text" id="toStreamID_<%=i%>"
                       value="<%=toStreamNameID%>" readonly="true"/>
            </div>

                <%-- add attribute mapping --%>
            <div id="outerDiv_<%=i%>">

                <%
                    if (isStreamChanged == false) {
                    //process attribute mapping dto to sort meta,correlation and payload attributes
                    ArrayList<AttributeMappingDTO> metaAttributeMappingDTOList = new ArrayList<AttributeMappingDTO>();
                    ArrayList<AttributeMappingDTO> correlationAttributeMappingDTOList = new ArrayList<AttributeMappingDTO>();
                    ArrayList<AttributeMappingDTO> payloadAttributeMappingDTOList = new ArrayList<AttributeMappingDTO>();

                    for (AttributeMappingDTO attributeMappingDTO : attributeMappingDTOs) {
                        if (attributeMappingDTO.getToAttribute().startsWith(TemplateManagerUIConstants.PROPERTY_META_PREFIX)) {
                            //set meta data
                            metaAttributeMappingDTOList.add(attributeMappingDTO);
                        } else if (attributeMappingDTO.getToAttribute().startsWith(TemplateManagerUIConstants.PROPERTY_CORRELATION_PREFIX)) {
                            //set correlation data
                            correlationAttributeMappingDTOList.add(attributeMappingDTO);
                        } else {
                            //set payload data
                            payloadAttributeMappingDTOList.add(attributeMappingDTO);
                        }
                    }

                    //process from stream attributes
                    EventStreamDefinitionDto fromStreamDefinitionDto = eventStreamAdminServiceStub.getStreamDefinitionDto(fromStreamNameID);
                    ArrayList<EventStreamAttributeDto> fromStreamAttributeArray = new ArrayList<EventStreamAttributeDto>();
                    if (fromStreamDefinitionDto.getMetaData() != null) {
                        //get meta data
                        for (EventStreamAttributeDto fromStreamMetaAttribute : fromStreamDefinitionDto.getMetaData()) {
                            fromStreamMetaAttribute.setAttributeName(TemplateManagerUIConstants.PROPERTY_META_PREFIX + fromStreamMetaAttribute.getAttributeName());
                            fromStreamAttributeArray.add(fromStreamMetaAttribute);
                        }
                    }
                    if (fromStreamDefinitionDto.getCorrelationData() != null) {
                        //get correlation data
                        for (EventStreamAttributeDto fromStreamCorrelationAttribute : fromStreamDefinitionDto.getCorrelationData()) {
                            fromStreamCorrelationAttribute.setAttributeName(TemplateManagerUIConstants.PROPERTY_CORRELATION_PREFIX + fromStreamCorrelationAttribute.getAttributeName());
                            fromStreamAttributeArray.add(fromStreamCorrelationAttribute);
                        }
                    }
                    if (fromStreamDefinitionDto.getPayloadData() != null) {
                        //get payload data
                        for (EventStreamAttributeDto fromStreamPayloadAttribute : fromStreamDefinitionDto.getPayloadData()) {
                            fromStreamAttributeArray.add(fromStreamPayloadAttribute);
                        }
                    }
                %>

                <h4><fmt:message
                        key='template.stream.attribute.mapping.header.text'/></h4>
                <table style="width:100%" id="addEventDataTable_<%=i%>">
                    <tbody>

                        <%--get meta data--%>
                    <tr>
                        <td colspan="6">
                            <h6><fmt:message key="meta.attribute.mapping"/></h6>
                        </td>
                    </tr>
                    <%
                        int metaCounter = 0;
                        if (!metaAttributeMappingDTOList.isEmpty()) {
                            for (AttributeMappingDTO metaAttributeMappingDTO : metaAttributeMappingDTOList) {
                    %>
                    <tr id="metaMappingRow_<%=metaCounter%>">
                        <td class="labelCellPadding">Mapped From :
                        </td>
                        <td>
                            <select id="metaEventMappingValue_<%=i%><%=metaCounter%>">
                                <option selected><%=metaAttributeMappingDTO.getFromAttribute()%>
                                </option>
                                <%
                                    boolean isMatchingAttributeType = false;
                                    for (EventStreamAttributeDto fromStreamAttribute : fromStreamAttributeArray) {
                                        if (fromStreamAttribute.getAttributeType().equals(metaAttributeMappingDTO.getAttributeType())) {
                                            isMatchingAttributeType = true;
                                            if (!fromStreamAttribute.getAttributeName().equals(metaAttributeMappingDTO.getFromAttribute())) {
                                %>
                                <option><%=fromStreamAttribute.getAttributeName()%>
                                </option>
                                <%
                                            }
                                        }
                                    }
                                    if (isMatchingAttributeType == false) {
                                %>
                                <option>No matching attribute type to map</option>
                                <%
                                    }
                                %>
                            </select>
                        </td>
                        <td class="labelCellPadding">Mapped To :
                        </td>
                        <td>
                            <input type="text" id="metaEventMappedValue_<%=i%><%=metaCounter%>"
                                   value="<%=metaAttributeMappingDTO.getToAttribute()%>"
                                   readonly="true"/>
                        </td>
                        <td>Attribute Type :
                        </td>
                        <td>
                            <input type="text" id="metaEventType_<%=i%><%=metaCounter%>"
                                   value="<%=metaAttributeMappingDTO.getAttributeType()%>" readonly="true"/>
                        </td>
                    </tr>
                    <%
                            metaCounter++;
                        }
                    } else {
                    %>
                    <tr>
                        <td colspan="6">
                            <div class="noDataDiv-plain" id="noInputMetaEventData">
                                No Meta attributes to define
                            </div>
                        </td>
                    </tr>
                    <%
                        }
                    %>

                        <%--get correlation data--%>
                    <tr>
                        <td colspan="6">
                            <h6><fmt:message key="correlation.attribute.mapping"/></h6>
                        </td>
                    </tr>
                    <%
                        int correlationCounter = 0;
                        if (!correlationAttributeMappingDTOList.isEmpty()) {
                            for (AttributeMappingDTO correlationAttributeMappingDTO : correlationAttributeMappingDTOList) {
                    %>
                    <tr id="correlationMappingRow_<%=correlationCounter%>">
                        <td class="labelCellPadding">Mapped From :
                        </td>
                        <td>
                            <select id="correlationEventMappingValue_<%=i%><%=correlationCounter%>">
                                <option selected><%=correlationAttributeMappingDTO.getFromAttribute()%>
                                </option>
                                <%
                                    boolean isMatchingAttributeType = false;
                                    for (EventStreamAttributeDto fromStreamAttribute : fromStreamAttributeArray) {
                                        if (fromStreamAttribute.getAttributeType().equals(correlationAttributeMappingDTO.getAttributeType())) {
                                            isMatchingAttributeType = true;
                                            if (!fromStreamAttribute.getAttributeName().equals(correlationAttributeMappingDTO.getFromAttribute())) {
                                %>
                                <option><%=fromStreamAttribute.getAttributeName()%>
                                </option>
                                <%
                                            }
                                        }
                                    }
                                    if (isMatchingAttributeType == false) {
                                %>
                                <option>No matching attribute type to map</option>
                                <%
                                    }
                                %>
                            </select>
                        </td>
                        <td class="labelCellPadding">Mapped To :
                        </td>
                        <td>
                            <input type="text" id="correlationEventMappedValue_<%=i%><%=correlationCounter%>"
                                   value="<%=correlationAttributeMappingDTO.getToAttribute()%>"
                                   readonly="true"/>
                        </td>
                        <td>Attribute Type :
                        </td>
                        <td>
                            <input type="text" id="correlationEventType_<%=i%><%=correlationCounter%>"
                                   value="<%=correlationAttributeMappingDTO.getAttributeType()%>" readonly="true"/>
                        </td>
                    </tr>
                    <%
                            correlationCounter++;
                        }
                    } else {
                    %>
                    <tr>
                        <td colspan="6">
                            <div class="noDataDiv-plain" id="noInputCorrelationEventData">
                                No Correlation attributes to define
                            </div>
                        </td>
                    </tr>
                    <%
                        }
                    %>

                        <%--get payload data--%>
                    <tr>
                        <td colspan="6">
                            <h6><fmt:message key="payload.attribute.mapping"/></h6>
                        </td>
                    </tr>
                    <%
                        int payloadCounter = 0;
                        if (!payloadAttributeMappingDTOList.isEmpty()) {
                            for (AttributeMappingDTO payloadAttributeMappingDTO : payloadAttributeMappingDTOList) {
                    %>
                    <tr id="payloadMappingRow_<%=payloadCounter%>">
                        <td class="labelCellPadding">Mapped From :
                        </td>
                        <td>
                            <select id="payloadEventMappingValue_<%=i%><%=payloadCounter%>">
                                <option selected><%=payloadAttributeMappingDTO.getFromAttribute()%>
                                </option>
                                <%
                                    boolean isMatchingAttributeType = false;
                                    for (EventStreamAttributeDto fromStreamAttribute : fromStreamAttributeArray) {
                                        if (fromStreamAttribute.getAttributeType().equals(payloadAttributeMappingDTO.getAttributeType())) {
                                            isMatchingAttributeType = true;
                                            if (!fromStreamAttribute.getAttributeName().equals(payloadAttributeMappingDTO.getFromAttribute())) {
                                %>
                                <option><%=fromStreamAttribute.getAttributeName()%>
                                </option>
                                <%
                                            }
                                        }
                                    }
                                    if (isMatchingAttributeType == false) {
                                %>
                                <option>No matching attribute type to map</option>
                                <%
                                    }
                                %>
                            </select>
                        </td>
                        <td class="labelCellPadding">Mapped To :
                        </td>
                        <td>
                            <input type="text" id="payloadEventMappedValue_<%=i%><%=payloadCounter%>"
                                   value="<%=payloadAttributeMappingDTO.getToAttribute()%>"
                                   readonly="true"/>
                        </td>
                        <td>Attribute Type :
                        </td>
                        <td>
                            <input type="text" id="payloadEventType_<%=i%><%=payloadCounter%>"
                                   value="<%=payloadAttributeMappingDTO.getAttributeType()%>" readonly="true"/>
                        </td>
                    </tr>
                    <%
                            payloadCounter++;
                        }
                    } else {
                    %>
                    <tr>
                        <td colspan="6">
                            <div  class="noDataDiv-plain" id="noInputPayloadEventData">
                                No Payload attributes to define
                            </div>
                        </td>
                    </tr>
                    <%
                        }
                    %>
                    </tbody>
                    <div style="display: none">
                        <input type="text" id="metaRows_<%=i%>"
                               value="<%=metaCounter%>"/>
                        <input type="text" id="correlationRows_<%=i%>"
                               value="<%=correlationCounter%>"/>
                        <input type="text" id="payloadRows_<%=i%>"
                               value="<%=payloadCounter%>"/>
                    </div>
                </table>
                <%}%>
            </div>
        </div>
    <br class="c-both"/>
    <hr class="wr-separate"/>
    <%
        }
    %>

        <div class="action-container">
            <button type="button"
                    class="btn btn-default btn-add col-md-2 col-xs-12 pull-right marg-right-15"
                    onclick="saveStreamConfiguration('<%=toStreamIDArray.length%>','<%=domainName%>','<%=configuration%>')">
                <fmt:message key='template.add.stream.button.text'/>
            </button>
        </div>
    <%
    } else {
    %>
    return
    <%
                }
            }
        } catch (AxisFault e) {
            response.sendError(500);
        }
    %>
</fmt:bundle>