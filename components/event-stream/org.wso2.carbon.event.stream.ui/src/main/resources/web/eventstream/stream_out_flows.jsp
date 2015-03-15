<%--
  ~ Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:bundle basename="org.wso2.carbon.event.stream.ui.i18n.Resources">

    <carbon:breadcrumb
            label="eventstream.list"
            resourceBundle="org.wso2.carbon.event.stream.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>

    <script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../admin/js/cookies.js"></script>
    <script type="text/javascript" src="../admin/js/main.js"></script>

    <%
        String eventStreamWithVersion = request.getParameter("eventStreamWithVersion");
        String loadingCondition = "importedStreams";
    %>

    <div id="middle">
        <h2><fmt:message key="title.event.out.flow"/> (<a
                href="eventStreamDetails.jsp?ordinal=1&eventStreamWithVersion=<%=eventStreamWithVersion%>"><%=eventStreamWithVersion%>
        </a>) </h2>

        <div id="workArea">

            <table style="width:100%" id="outFlowDetails" class="styledLeft">

                <tbody>

                <tr>
                    <td class="formRaw">
                        <p><b><fmt:message key="external.event.outflows"/></b></p>
                        <jsp:include page="../eventformatter/event_formatter_outFlows.jsp"
                                     flush="true">
                            <jsp:param name="eventStreamWithVersion"
                                       value="<%=eventStreamWithVersion%>"/>
                        </jsp:include>
                        <p><b><fmt:message key="internal.event.outflows"/></b></p>
                        <jsp:include page="../eventprocessor/inner_index.jsp" flush="true">
                            <jsp:param name="eventStreamWithVersion"
                                       value="<%=eventStreamWithVersion%>"/>
                            <jsp:param name="loadingCondition" value="<%=loadingCondition%>"/>
                        </jsp:include>

                    </td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>

</fmt:bundle>
