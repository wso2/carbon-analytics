<%@ page
        import="org.wso2.carbon.event.input.adaptor.manager.ui.InputEventAdaptorUIUtils" %>
<%@ page
        import="org.wso2.carbon.event.input.adaptor.manager.stub.InputEventAdaptorManagerAdminServiceStub" %>
<%@ page
        import="org.wso2.carbon.event.input.adaptor.manager.stub.types.InputEventAdaptorPropertiesDto" %>
<%@ page
        import="org.wso2.carbon.event.input.adaptor.manager.stub.types.InputEventAdaptorPropertyDto" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>


<fmt:bundle basename="org.wso2.carbon.event.input.adaptor.manager.ui.i18n.Resources">

    <carbon:breadcrumb
            label="details"
            resourceBundle="org.wso2.carbon.event.input.adaptor.manager.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>

    <script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../admin/js/cookies.js"></script>
    <script type="text/javascript" src="../admin/js/main.js"></script>


    <div id="middle">
        <h2><fmt:message key="input.event.adaptor.details"/> </h2>

        <div id="workArea">
            <table id="eventInputTable" class="styledLeft"
                   style="width:100%">
                <tbody>
                <%
                    String eventName = request.getParameter("eventName");
                    String eventType = request.getParameter("eventType");
                    if (eventName != null) {
                        InputEventAdaptorManagerAdminServiceStub stub = InputEventAdaptorUIUtils.getInputEventManagerAdminService(config, session, request);


                        InputEventAdaptorPropertiesDto eventAdaptorPropertiesDto = stub.getActiveInputEventAdaptorConfiguration(eventName);
                        InputEventAdaptorPropertyDto[] inputEventProperties = eventAdaptorPropertiesDto.getInputEventAdaptorPropertyDtos();


                %>
                <tr>
                    <td class="leftCol-small">Event Adaptor Name</td>
                    <td><input type="text" name="eventName" id="eventNameId"
                               value=" <%=eventName%>"
                               disabled="true"
                               style="width:75%"/></td>

                    </td>
                </tr>
                <tr>
                    <td>Event Adaptor Type</td>
                    <td><select name="eventTypeFilter"
                                disabled="true">
                        <option><%=eventType%>
                        </option>
                    </select>
                    </td>
                </tr>
                <%

                    if (inputEventProperties != null) {
                        for (InputEventAdaptorPropertyDto eventAdaptorPropertyDto : inputEventProperties) {

                %>

                <tr>
                    <td><%=eventAdaptorPropertyDto.getDisplayName()%>
                    </td>
                    <%
                        if (!eventAdaptorPropertyDto.getSecured()) {
                    %>
                    <td><input type="input" value="<%=eventAdaptorPropertyDto.getValue()%>"
                               disabled="true"
                               style="width:75%"/>
                    </td>
                    <%
                    } else { %>
                    <td><input type="password" value="<%=eventAdaptorPropertyDto.getValue()%>"
                               disabled="true"
                               style="width:75%"/>
                    </td>
                    <%
                        }
                    %>
                </tr>
                <%

                            }
                        }
                    }

                %>

                </tbody>
            </table>


        </div>
    </div>
</fmt:bundle>