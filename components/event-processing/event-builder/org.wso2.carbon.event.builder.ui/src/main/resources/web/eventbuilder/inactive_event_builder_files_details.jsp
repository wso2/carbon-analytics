<%@ page
        import="org.wso2.carbon.event.builder.stub.EventBuilderAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.builder.ui.EventBuilderUIUtils" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>


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

<fmt:bundle basename="org.wso2.carbon.event.builder.ui.i18n.Resources">

    <carbon:breadcrumb
            label="event.builder.inactivelist.breadcrumb"
            resourceBundle="org.wso2.carbon.event.builder.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>

    <script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../admin/js/cookies.js"></script>
    <script type="text/javascript" src="../admin/js/main.js"></script>

    <script type="text/javascript">
        function doDelete(ebFilename) {
            var theform = document.getElementById('deleteForm');
            theform.filename.value = ebFilename;
            theform.submit();
        }
    </script>

    <%
        String filename = request.getParameter("filename");
        if (filename != null) {
            EventBuilderAdminServiceStub stub = EventBuilderUIUtils.getEventBuilderAdminService(config, session, request);
            stub.undeployInactiveEventBuilderConfiguration(filename);
    %>
    <script type="text/javascript">CARBON.showInfoDialog('Event Builder file successfully deleted.');</script>
    <%
        }
    %>


    <div id="middle">
        <h2><fmt:message key="not.deployed.event.builders"/></h2>

        <div id="workArea">
            <table class="styledLeft">
                <%
                    EventBuilderAdminServiceStub stub = EventBuilderUIUtils.getEventBuilderAdminService(config, session, request);
                    String[] undeployedFileNames = stub.getAllInactiveEventBuilderConfigurations();
                    if (undeployedFileNames != null) {
                %>
                <thead>
                <tr>
                    <th><fmt:message key="filename.header"/></th>
                    <th><fmt:message key="inactive.reason.header"/></th>
                    <th><fmt:message key="actions.header"/></th>
                </tr>
                </thead>
                <%
                    for (String eventBuilderConfigurationFilename : undeployedFileNames) {
                %>
                <tbody>
                <tr>
                    <td>
                        <%=eventBuilderConfigurationFilename%>
                    </td>
                    <td>
                        <%=stub.getEventBuilderStatusAsString(eventBuilderConfigurationFilename)%>
                    </td>
                    <td>
                        <a style="background-image: url(../admin/images/delete.gif);"
                           class="icon-link"
                           onclick="doDelete('<%=eventBuilderConfigurationFilename%>')"><font
                                color="#4682b4">Delete</font></a>
                        <a style="background-image: url(../admin/images/edit.gif);"
                           class="icon-link"
                           href="edit_inactive_event_builder_details.jsp?ordinal=1&eventBuilderFilename=<%=eventBuilderConfigurationFilename%>"><font
                                color="#4682b4">Source View</font></a>
                    </td>

                </tr>
                <%
                        }
                    }
                %>
                </tbody>
            </table>
            <div>
                <form id="deleteForm" name="input" action="" method="get"><input type="HIDDEN"
                                                                                 name="filename"
                                                                                 value=""/></form>
            </div>
        </div>


        <script type="text/javascript">
            alternateTableRows('expiredsubscriptions', 'tableEvenRow', 'tableOddRow');
            alternateTableRows('validsubscriptions', 'tableEvenRow', 'tableOddRow');
        </script>
</fmt:bundle>