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
<fmt:bundle basename="org.wso2.carbon.analytics.activitydashboard.ui.i18n.Resources">
    <carbon:breadcrumb
            label="activitydashboard"
            resourceBundle="org.wso2.carbon.analytics.activitydashboard.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>"/>
    <link href="../admin/jsp/registry_styles_ajaxprocessor.jsp" rel="stylesheet" type="text/css" media="all"/>
    <script type="text/javascript" src="../ajax/js/prototype.js"></script>
    <link type="text/css" rel="stylesheet" href="../resources/css/registry.css"/>
    <script type="text/javascript" src="../registry_common/js/registry_validation.js"></script>
    <script type="text/javascript" src="../registry_common/js/registry_common.js"></script>
    <script type="text/javascript" src="../resources/js/resource_util.js"></script>
    <script type="text/javascript" src="../generic/js/genericpagi.js"></script>
    <script type="text/javascript" src="../generic/js/generic.js"></script>
    <script type="text/javascript" src="js/artifacts-list.js"></script>

    <style type="text/css">
        .hidden {
            display: none;
        }
    </style>

    <script type="text/javascript">
        function toggle_visibility(id) {
            var e = document.getElementById(id);
            if (e.style.display == 'block')
                e.style.display = 'none';
            else
                e.style.display = 'block';
        }

        function populateRecords(activityId) {
            if (!activityId) {
                CARBON.showErrorDialog("Cannot proceed to get the list of records, empty activityId provided! ");
            }
            var values = {
                activityId: activityId
            };
            var recordsDivId = "records_".concat(activityId);
            jQuery.ajax({
                async: false,
                type: 'POST',
                url: 'records_populator_ajaxprocessor.jsp',
                data: values,
                success: function (data) {
                    document.getElementById(recordsDivId).innerHTML = data;
                },
                error: function (data) {
                    CARBON.showErrorDialog("Unable to load the records. " + data);
                }
            });
            toggle_visibility(recordsDivId);
        }

        function pageLoad(pageNo) {
            window.location = "activities_list_page.jsp?pageNo=" + pageNo;
        }
    </script>
    <div id="middle">
        <h2>Activities Search Result</h2>

        <div id="workArea">
            <%
                String pageNoStr = request.getParameter("pageNo");
                int pageNo = 1;
                if (pageNoStr != null) {
                    pageNo = Integer.parseInt(pageNoStr);
                }
                Object searchResultObj = request.getSession().getAttribute("ActivitiesSearchResult");
                if (searchResultObj == null) {
            %>
            No search results to show!
            <%
            } else {
                String[] activities = (String[]) searchResultObj;
                int numberPages = (int) (Math.ceil(((double) activities.length) / 10.0));
                ;
                int startActivityIndex = (pageNo - 1) * 10;
                int endActivityIndex = startActivityIndex + 10;
                if (endActivityIndex > activities.length) {
                    endActivityIndex = activities.length;
                }
                for (int i = startActivityIndex; i < endActivityIndex; i++) {
                    String activityId = activities[i];
            %>
            <div class="sectionSeperator"><a href="#" onclick="populateRecords('<%=activityId%>');"><%=activityId%>
            </a>
            </div>
            <div id="records_<%=activityId%>" class="hidden"></div>
            <%
                }
            %>
            <%=startActivityIndex+1%> - <%=endActivityIndex%> of <%=activities.length%>
            <table width="100%" style="text-align:center; padding-top: 10px; margin-bottom: -10px">
                <carbon:resourcePaginator pageNumber="<%=pageNo%>" numberOfPages="<%=numberPages%>"
                                          nextKey="next" prevKey="prev"
                                          paginationFunction="pageLoad({0})"/>
            </table>
            <% }
            %>

        </div>
    </div>
</fmt:bundle>