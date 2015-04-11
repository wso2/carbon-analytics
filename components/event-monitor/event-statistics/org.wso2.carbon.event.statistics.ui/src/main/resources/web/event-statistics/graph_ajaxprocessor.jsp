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
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.event.statistics.stub.client.EventStatisticsAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.statistics.ui.EventStatisticsAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.rmi.RemoteException" %>
<script type="text/javascript" src="js/graphs.js"></script>
<script type="text/javascript" src="js/statistics.js"></script>
<fmt:bundle basename="org.wso2.carbon.event.statistics.ui.i18n.Resources">

    <%
        String category = request.getParameter("category");
        String deployment = request.getParameter("deployment");
        String element = request.getParameter("element");

        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        EventStatisticsAdminClient client = new EventStatisticsAdminClient(cookie,backendServerURL,configContext,request.getLocale());

        EventStatisticsAdminServiceStub.StatsDTO count = null;
        try {
            if(category==null){
                count = client.getGlobalCount();
            } else {
                if(deployment==null){
                    count = client.getCategoryCount(category);
                } else {
                    if(element==null){
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

    <%
        if (count != null) {
    %>
   <table width="100%">
       <tr>
           <td colspan="3">
               <table width="100%">
                   <thead>
                   <tr>
                       <th align="left"><u>
                           <fmt:message key="events.vs.time"/>
                       </u></th>
                   </tr>
                   </thead>
                   <tr>
                       <td>
                           <div id="statsGraph" style="height:300px;"></div>
                       </td>
                   </tr>
                   <script type="text/javascript">
                       jQuery.noConflict();
                       graphRequest.add(<%= count.getRequestTotalCount()%>);
                       graphResponse.add(<%= count.getResponseTotalCount()%>);

                       function drawEventStatsGraph() {
                           jQuery.plot(jQuery("#statsGraph"), [
                               {
                                   label:"<fmt:message key="request.count"/>",
                                   data:graphRequest.get(),
                                   lines:{ show:true, fill:true }
                               },
                               {
                                   label:"<fmt:message key="response.count"/>",
                                   data:graphResponse.get(),
                                   lines:{ show:true, fill:true }
                               }
                           ], {
                                           xaxis:{
                                               ticks:graphRequest.tick(),
                                               min:0
                                           },
                                           yaxis:{
                                               ticks:10,
                                               min:0
                                           }
                                       });



                       }

                       drawEventStatsGraph();
                   </script>
               </table>
           </td>
       </tr>
       <tr>
           <td colspan="3"><br/></td>
       </tr>

       <tr>
            <td>
                <table class="styledLeft" id="requestStatsTable" width="100%">
                    <thead>
                    <tr>
                        <th colspan="2" align="left"><fmt:message key="request.statistics"/></th>
                    </tr>
                    </thead>
                    <tr class="tableOddRow">
                        <td width="40%"><fmt:message key="total.count"/></td>
                        <td><%=count.getRequestTotalCount() %>
                        </td>
                    </tr>
                    <tr class="tableEvenRow">
                        <td width="40%"><fmt:message key="updated.time"/></td>
                        <td><%=count.getRequestLastUpdatedTime() %>
                        </td>
                    </tr>
                    <tr class="tableOddRow">
                        <td width="40%"><fmt:message key="max.count.per.sec"/></td>
                        <td><%=count.getRequestMaxCountPerSec() %>
                        </td>
                    </tr>
                    <tr class="tableEvenRow">
                        <td width="40%"><fmt:message key="avg.count.per.sec"/></td>
                        <td><%=count.getRequestAvgCountPerSec() %>
                        </td>
                    </tr>
                    <tr class="tableOddRow">
                        <td width="40%"><fmt:message key="last.sec.count"/></td>
                        <td><%=count.getRequestLastSecCount() %>
                        </td>
                    </tr>
                    <tr class="tableEvenRow">
                        <td width="40%"><fmt:message key="last.min.count"/></td>
                        <td>&#126;&nbsp;<%=count.getRequestLastMinCount() %>
                        </td>
                    </tr>
                    <tr class="tableOddRow">
                        <td width="40%"><fmt:message key="last.15min.count"/></td>
                        <td>&#126;&nbsp;<%=count.getRequestLast15MinCount() %>
                        </td>
                    </tr>
                    <tr class="tableEvenRow">
                        <td width="40%"><fmt:message key="last.hour.count"/></td>
                        <td>&#126;&nbsp;<%=count.getRequestLastHourCount() %>
                        </td>
                    </tr>
                    <tr class="tableOddRow">
                        <td width="40%"><fmt:message key="last.6hour.count"/></td>
                        <td>&#126;&nbsp;<%=count.getRequestLast6HourCount() %>
                        </td>
                    </tr>
                    <tr class="tableEvenRow">
                        <td width="40%"><fmt:message key="last.day.count"/></td>
                        <td>&#126;&nbsp;<%=count.getRequestLastDayCount() %>
                        </td>
                    </tr>
                </table>
           </td>
           <td>&nbsp;<br/></td>
           <td>
                <table class="styledLeft" id="responseStatsTable" width="100%">
                    <thead>
                    <tr>
                        <th colspan="2" align="left"><fmt:message key="response.statistics"/></th>
                    </tr>
                    </thead>
                    <tr class="tableOddRow">
                        <td width="40%"><fmt:message key="total.count"/></td>
                        <td><%=count.getResponseTotalCount() %>
                        </td>
                    </tr>
                    <tr class="tableEvenRow">
                        <td width="40%"><fmt:message key="updated.time"/></td>
                        <td><%=count.getResponseLastUpdatedTime() %>
                        </td>
                    </tr>
                    <tr class="tableOddRow">
                        <td width="40%"><fmt:message key="max.count.per.sec"/></td>
                        <td><%=count.getResponseMaxCountPerSec() %>
                        </td>
                    </tr>
                    <tr class="tableEvenRow">
                        <td width="40%"><fmt:message key="avg.count.per.sec"/></td>
                        <td><%=count.getResponseAvgCountPerSec() %>
                        </td>
                    </tr>
                    <tr class="tableOddRow">
                        <td width="40%"><fmt:message key="last.sec.count"/></td>
                        <td><%=count.getResponseLastSecCount() %>
                        </td>
                    </tr>
                    <tr class="tableEvenRow">
                        <td width="40%"><fmt:message key="last.min.count"/></td>
                        <td>&#126;&nbsp;<%=count.getResponseLastMinCount() %>
                        </td>
                    </tr>
                    <tr class="tableOddRow">
                        <td width="40%"><fmt:message key="last.15min.count"/></td>
                        <td>&#126;&nbsp;<%=count.getResponseLast15MinCount() %>
                        </td>
                    </tr>
                    <tr class="tableEvenRow">
                        <td width="40%"><fmt:message key="last.hour.count"/></td>
                        <td>&#126;&nbsp;<%=count.getResponseLastHourCount() %>
                        </td>
                    </tr>
                    <tr class="tableOddRow">
                        <td width="40%"><fmt:message key="last.6hour.count"/></td>
                        <td>&#126;&nbsp;<%=count.getResponseLast6HourCount() %>
                        </td>
                    </tr>
                    <tr class="tableEvenRow">
                        <td width="40%"><fmt:message key="last.day.count"/></td>
                        <td>&#126;&nbsp;<%=count.getResponseLastDayCount() %>
                        </td>
                    </tr>
                </table>
           </td>
       </tr>
    </table>
    <%
        } else {
    %>
            <p><fmt:message key="no.statistic.data"/></p>
    <%
        }
    %>
</fmt:bundle>