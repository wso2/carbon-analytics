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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.event.statistics.stub.client.EventStatisticsAdminServiceStub" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.rmi.RemoteException" %>
<script type="text/javascript" src="js/graphs.js"></script>
<script type="text/javascript" src="js/statistics.js"></script>
<script type="text/javascript" src="../admin/js/jquery.flot.js"></script>
<script type="text/javascript" src="../admin/js/excanvas.js"></script>
<script type="text/javascript" src="global-params.js"></script>
<fmt:bundle basename="org.wso2.carbon.event.statistics.ui.i18n.Resources">
<carbon:breadcrumb label="Proxyservice Statistics"
		resourceBundle="org.wso2.carbon.event.statistics.ui.i18n.Resources"
		topPage="false" request="<%=request%>" />
    <%
        response.setHeader("Cache-Control", "no-cache");

        String category = request.getParameter("category");
        String deployment = request.getParameter("deployment");
        String element = request.getParameter("element");

//        System.out.println(category+" "+deployment+" "+element);

        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        org.wso2.carbon.event.statistics.ui.EventStatisticsAdminClient client = new org.wso2.carbon.event.statistics.ui.EventStatisticsAdminClient(cookie,backendServerURL,configContext,request.getLocale());

        EventStatisticsAdminServiceStub.StatsDTO count ;
        String typeName="Server";
        try {
            if(category==null){
                count = client.getGlobalCount();
            } else {
                if(deployment==null){
                    typeName=category;
                    count = client.getCategoryCount(category);
                } else {
                    if(element==null){
                        typeName=deployment;
                        count = client.getDeploymentCount(category,deployment);
                    } else {
                        count = client.getElementCount(category,deployment,element);
                    }
                }
            }
        } catch (RemoteException e) {
    %>
            <jsp:forward page="../admin/error.jsp?<%=e.getMessage()%>"/>
    <%
            return;
        }

    %>

    <script type="text/javascript">
        jQuery.noConflict();
        initStats('50');
    </script>
    <div id="middle">
        <%
            String requestString="";
            if (category == null) {
        %>
        <h2><fmt:message key="event.statistics"/> (<fmt:message key="all.events"/>)</h2>
        <%
            } else {

                if (deployment == null) {
                    requestString="?category="+category;
        %>
        <h2><fmt:message key="event.statistics"/> (<fmt:message key="all.events"/> <%=category%>)</h2>
        <%
                } else {
                    if (element == null) {
                        requestString="?category="+category+"&deployment="+deployment;

        %>
        <h2><fmt:message key="event.statistics"/> (<%=deployment%> <%=category%>)</h2>
        <%
                    } else {
                        requestString="?category="+category+"&deployment="+deployment+"&element="+element;
        %>
        <h2><fmt:message key="event.statistics"/> (<%=element%> <fmt:message key="event.statistics.events"/> <%=deployment%> <%=category%>)</h2>
        <%
                    }
                }
            }
        %>
        <div id="workArea">
                <%
                    if (count != null) {
                %>
            <table width="100%">
                <tr>
                    <td>
                       <div id="result"></div>
                            <script type="text/javascript">
                                jQuery.noConflict();
                                var refresh;
                                function refreshStats() {
                                    var url = "graph_ajaxprocessor.jsp<%=requestString.replaceAll(" ","%20")%>";
                                    jQuery("#result").load(url, null, function (responseText, status, XMLHttpRequest) {
                                        if (status != "success") {
                                            stopRefreshStats();
                                        }
                                    });
                                }
                                function stopRefreshStats() {
                                    if (refresh) {
                                        clearInterval(refresh);
                                    }
                                }
                                jQuery(document).ready(function() {
                                    refreshStats();
                                    refresh = setInterval("refreshStats()", 6000);
                                });
                            </script>
                    </td>
                </tr>
                <tr height="10"/>
                <tr>
                    <td>
                        <% if (count.getChildStats() != null && count.getChildStats().length>0 && count.getChildStats()[0]!=null) { %>
                        <table class="styledLeft" id="subTypeTable" style="width:100% !important;">
                            <thead>
                                <tr>
                                    <th><fmt:message key="sub.types.of"/>&nbsp;<%=typeName%></th>
                                </tr>
                            </thead>

                            <% for (String childStat : count.getChildStats()) { %>
                            <tr>
                                <td>
                                    <%
                                        if (category == null) {
                                    %>
                                    <a href="event_statistics_view.jsp?ordinal=1&category=<%=childStat.replaceAll(" ","%20")%>"><%=childStat%>
                                    </a>

                                    <%
                                    } else {
                                        if (deployment == null) {
                                    %>
                                    <a href="event_statistics_view.jsp?ordinal=1&category=<%=category%>&deployment=<%=childStat.replaceAll(" ","%20")%>"><%=childStat%></a>
                                    <%
                                            } else {
                                                if (element == null) {
                                    %>
                                    <a href="event_statistics_view.jsp?ordinal=1&category=<%=category%>&deployment=<%=deployment%>&element=<%=childStat.replaceAll(" ","%20")%>"><%=childStat%></a>
                                    <%
                                                }
                                            }
                                        }
                                    %>
                                </td>
                            </tr>
                            <%
                                }%>
                        </table>
                        <% }%>
                    </td>
                </tr>
            </table>
                <%
                } else {
                %>
                    <table class="styledLeft"  style="width:100%">
                        <tbody>
                        <tr>
                            <td class="formRaw">
                                <table id="noEventFormatterInputTable" class="normal-nopadding"
                                       style="width:100%">
                                    <tbody>
                                    <tr>
                                        <td class="leftCol-med" colspan="2">
                                            <p style="color:red"><fmt:message key="event.statistics.disabled"/></p>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="leftCol-med" colspan="2"><fmt:message
                                                key="how.to.enable.event.statistics.msg"/>
                                        </td>
                                    </tr>
                                    </tbody>
                                </table>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                <%
                    }
                %>

        </div>
    </div>
<script type="text/javascript">
    alternateTableRows('subTypeTable', 'tableEvenRow', 'tableOddRow');
</script>
</fmt:bundle>