<!--
~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->


<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.bam.toolbox.deployer.stub.BAMToolboxDepolyerServiceStub.ToolBoxStatusDTO" %>
<%@ page import="org.wso2.carbon.bam.toolbox.deployer.ui.client.BAMToolBoxDeployerClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<fmt:bundle basename="org.wso2.carbon.bam.toolbox.deployer.ui.i18n.Resources">


<script type="text/javascript">
    function deleteRow(name, msg) {
        CARBON.showConfirmationDialog(msg + "' " + name + " ' ?", function() {
            document.location.href = "undeploy.jsp?" + "toolBoxName=" + name;
        });
    }
</script>

<%
    String toolBoxSearchString = request.getParameter("toolboxSearchString");
%>

<script type="text/javascript">
    window.onload = setupRefresh;
    var isMessageBox = false;
    var lastSearchStr = <% if(null== toolBoxSearchString){
    %> '';
    <%
    }else {
    %>
    '<%=toolBoxSearchString%>';
    <%
    }
    %>


    function setupRefresh() {
        setInterval("refreshPage();", 10000); // milliseconds
    }
    function refreshPage() {
        if (!isSelected() && !isMessageBox && !isSearchStringTyped()) {
            window.location = location.href;
        }
    }

    function isSelected() {
        var counter = 0,
                i = 0,
                fieldsStr = '',
                input_obj = document.getElementsByTagName('input');

        for (i = 0; i < input_obj.length; i++) {
            if (input_obj[i].type === 'checkbox' && input_obj[i].checked === true) {
                counter++;
                fieldsStr = fieldsStr + ',' + input_obj[i].value;
            }
        }
        if (counter > 0) {
            return true;
        } else {
            return false;
        }
    }

    function isSearchStringTyped() {
        lastSearchStr = trimText(lastSearchStr);
        if (null != document.getElementById('toolboxSearchString')) {
            var currSearchStr = trimText(document.searchForm.toolboxSearchString.value);
            if (lastSearchStr != currSearchStr) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

</script>

<carbon:breadcrumb label="available.bam.tools"
                   resourceBundle="org.wso2.carbon.bam.toolbox.deployer.ui.i18n.Resources"
                   topPage="false" request="<%=request%>"/>

<%
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    String toolBoxType = request.getParameter("toolType");

    BAMToolBoxDeployerClient client = new BAMToolBoxDeployerClient(cookie, serverURL, configContext);
    String[] deployedTools = null;
    String[] toBeDeployedTools = null;
    String[] toBeUndeployedTools = null;

    String requestUrl = request.getHeader("Referer");
    String success = null;
    if (null != requestUrl && requestUrl.contains("undeploy.jsp")) {
        success = request.getParameter("undeploysuccess");
    }
    String message = request.getParameter("message");


    try {
        ToolBoxStatusDTO statusDTO = client.getToolBoxStatus(toolBoxType, toolBoxSearchString);
        deployedTools = statusDTO.getDeployedTools();
        toBeDeployedTools = statusDTO.getToBeDeployedTools();
        toBeUndeployedTools = statusDTO.getToBeUndeployedTools();

    } catch (Exception e) {
%>
<script type="text/javascript">
    CARBON.showErrorDialog("Error while getting the status of BAM ToolBox");
</script>
<%
    }
%>


<%
    if (null != success && !success.equals("")) {
        if (success.equalsIgnoreCase("true")) {
%>
<script type="text/javascript">
    CARBON.showInfoDialog('<%=message%>');
</script>
<%
} else {
%>
<script type="text/javascript">
    CARBON.showErrorDialog('<%=message%>');
</script>
<%
        }
    }
%>


<script type="text/javascript">
    function selectAllTools() {
        var i = 0,
                input_obj = document.getElementsByTagName('input');

        for (i = 0; i < input_obj.length; i++) {
            if (input_obj[i].type === 'checkbox') {
                input_obj[i].checked = true;
            }
        }
    }

    function selectNoneTools() {
        var i = 0,
                input_obj = document.getElementsByTagName('input');

        for (i = 0; i < input_obj.length; i++) {
            if (input_obj[i].type === 'checkbox') {
                input_obj[i].checked = false;
            }
        }
    }

    function undeployTools() {
        var counter = 0,
                i = 0,
                fieldsStr = '',
                input_obj = document.getElementsByTagName('input');

        for (i = 0; i < input_obj.length; i++) {
            if (input_obj[i].type === 'checkbox' && input_obj[i].checked === true) {
                counter++;
                fieldsStr = fieldsStr + ',' + input_obj[i].value;
            }
        }
        if (counter > 0) {
            fieldsStr = fieldsStr.substr(1);
        }
        if ('' == fieldsStr) {
            CARBON.showErrorDialog("No tools has been selected to undeploy!!");
        } else {
            isMessageBox = true;
            CARBON.showConfirmationDialog("Do you want to undeploy selected tools?", function() {
                document.location.href = "undeploy.jsp?" + "toolBoxNames=" + fieldsStr;
            });
        }
    }

    function searchServices() {
        var searchStr = document.searchForm.toolboxSearchString.value;
        document.searchForm.toolboxSearchString.value = trimText(searchStr);
        document.searchForm.submit();
    }

    function trimText(text) {
        return text.replace(/(?:(?:^|\n)\s+|\s+(?:$|\n))/g, '').replace(/\s+/g, ' ');
    }

</script>


<div id="middle">
<h2><fmt:message key="available.bam.tools"/></h2>

<div id="workArea">
<form action="listbar.jsp" name="searchForm">
    <table class="styledLeft">
        <tr>
            <td style="border:0; !important">
                <nobr>
                    <%
                        if (null == toolBoxType || toolBoxType.equals("") || toolBoxType.equals("1")) {
                    %>
                    <a href="listbar.jsp?toolType=2"><%=deployedTools != null ? deployedTools.length : 0%> <fmt:message
                            key="deployed.toolboxes"/></a>.&nbsp;
                    <a href="listbar.jsp?toolType=3"><%=toBeDeployedTools != null ? toBeDeployedTools.length : 0%>
                        <fmt:message
                                key="awaiting.to.deploy.toolboxes"/></a>.&nbsp;
                    <a href="listbar.jsp?toolType=4"><%=toBeUndeployedTools != null ? toBeUndeployedTools.length : 0%>
                        <fmt:message
                                key="awaiting.to.undeploy.toolboxes"/></a>
                    <%
                    } else if (toolBoxType.equals("2")) {
                    %>
                    <%=deployedTools != null ? deployedTools.length : 0%> <fmt:message
                        key="deployed.toolboxes"/>&nbsp;
                    <%
                    } else if (toolBoxType.equals("3")) {
                    %>
                    <%=toBeDeployedTools != null ? toBeDeployedTools.length : 0%> <fmt:message
                        key="awaiting.to.deploy.toolboxes"/>&nbsp;
                    <%
                    } else {
                    %>
                    <%=toBeUndeployedTools != null ? toBeUndeployedTools.length : 0%> <fmt:message
                        key="awaiting.to.undeploy.toolboxes"/>
                    <%
                        }
                    %>
                </nobr>
            </td>
        </tr>
        <% if ((null != deployedTools && deployedTools.length != 0) ||
                (null != toBeDeployedTools && toBeDeployedTools.length != 0) ||
                (null != toBeUndeployedTools && toBeUndeployedTools.length != 0)) { %>
        <tr>
            <td style="border:0; !important">&nbsp;</td>
        </tr>
        <tr>
            <td>
                <table style="border:0; !important">
                    <tbody>
                    <tr style="border:0; !important">
                        <td style="border:0; !important">
                            <nobr>
                                <fmt:message key="toolbox.status"/>:&nbsp;&nbsp;
                                <select name="toolType" id="tooType">
                                    <option value="1" <%= (null == toolBoxType || toolBoxType.equals("1")) ? "selected=\'selected\'" : ""%>>
                                        <fmt:message key="all"/>
                                    </option>
                                    <option value="2" <%= (null != toolBoxType && toolBoxType.equals("2")) ? "selected=\'selected\'" : ""%>>
                                        <fmt:message key="bam.tool.status.deployed"/>
                                    </option>
                                    <option value="3" <%= (null != toolBoxType && toolBoxType.equals("3")) ? "selected=\'selected\'" : ""%>>
                                        <fmt:message key="bam.tool.status.tobedeployed"/>
                                    </option>
                                    <option value="4" <%= (null != toolBoxType && toolBoxType.equals("4")) ? "selected=\'selected\'" : ""%>>
                                        <fmt:message key="bam.tool.status.tobeundeployed"/>
                                    </option>
                                </select>
                                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                                <fmt:message key="search.toolbox"/>
                                <input type="text" name="toolboxSearchString"
                                       id ="toolboxSearchString" value="<%= toolBoxSearchString != null? toolBoxSearchString : ""%>"/>&nbsp;
                            </nobr>
                        </td>
                        <td style="border:0; !important">
                            <a class="icon-link" href="#" style="background-image: url(images/search.gif);"
                               onclick="javascript:searchServices(); return false;"
                               alt="<fmt:message key='search'/>"></a>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </td>
        </tr>
    </table>
</form>

<table class="normal-nopadding">
    <tbody>
    <tr>
        <td width="25px">

        </td>
        <td width="25px">
            <nobr>
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;

                <a style="cursor:pointer" onclick="selectAllTools()"><font color="#2F7ABD"><fmt:message
                        key="select.all"/></font></a>

                &nbsp;&nbsp;<b>|</b> &nbsp;&nbsp;
                <a style="cursor:pointer" onclick="selectNoneTools()"><font color="#2F7ABD"><fmt:message
                        key="select.none"/></font></a>
                &nbsp;&nbsp;&nbsp;&nbsp;
            </nobr>
        </td>
        <td width="800px">
            <nobr>
                <a href="javascript:undeployTools();"
                   class="icon-link" style="background-image:url(images/undeploy.png);"
                   target="_self">
                    <fmt:message key="bam.undeploy"/>
                </a>
            </nobr>
        </td>
    </tr>


    </tbody>
</table>

<form id="listTools" name="listTools" action="" method="POST">
    <table class="styledLeft">
        <thead>
        <tr>
            <th></th>
            <th><span style="float: left; position: relative; margin-top: 2px;">
                            <fmt:message key="bam.toolboxes"/></span>
            </th>
            <th><span style="float: left; position: relative; margin-top: 2px;">
                            <fmt:message key="tools.status"/></span>
            </th>
            <th><span style="float: left; position: relative; margin-top: 2px;">
                            <fmt:message key="operation"/></span>
            </th>
        </tr>
        </thead>
        <tbody>

        <% int position = 0;
            if (null != deployedTools) {
                for (String aName : deployedTools) {
                    String bgColor = ((position % 2) == 1) ? "#EEEFFB" : "white";
                    position++;
        %>
        <tr bgcolor="<%=bgColor%>">
            <td width="10px" style="text-align:center; !important">
                <input type="checkbox" id="toolboxes_<%=position%>" name=toolboxes_<%=position%>
                       value="<%=aName%>"
                       class="chkBox"/>
            </td>
            <td><label>
                <%=aName%>
            </label>
            </td>
            <td><fmt:message key="bam.tool.status.deployed"></fmt:message>
            </td>
            <td>
                <nobr>
                    <a href="download-ajaxprocessor.jsp?toolboxName=<%=aName%>"
                       class="icon-link" style="background-image:url(images/download.gif);"
                       target="_self">
                        <fmt:message key="download"/>
                    </a>
                </nobr>
            </td>
        </tr>
        <%
                }
            }
        %>

        <% if (null != toBeDeployedTools) {
            for (String aName : toBeDeployedTools) {
                String bgColor = ((position % 2) == 1) ? "#EEEFFB" : "white";
                position++;
        %>
        <tr bgcolor="<%=bgColor%>">
            <td width="10px" style="text-align:center; !important">
                <input type="checkbox" id="toolboxes_<%=position%>" name=toolboxes_<%=position%>
                       value="<%=aName%>"
                       class="chkBox"/>
            </td>
            <td><label>
                <%=aName%>
            </label>
            </td>
            <td><fmt:message key="bam.tool.status.tobedeployed"></fmt:message>
            </td>
            <td>
            </td>

        </tr>
        <input type="hidden" name="finalIdToolsToUndeply" id="finalIdToolsToUndeply" value="<%=position%>"/>
        <%
                }
            }
        %>

        <% if (null != toBeUndeployedTools) {
            for (String aName : toBeUndeployedTools) {
                String bgColor = ((position % 2) == 1) ? "#EEEFFB" : "white";
                position++;
        %>
        <tr bgcolor="<%=bgColor%>">
            <td width="10px" style="text-align:center; !important">
                <input type="checkbox" id="toolboxes_<%=position%>" name=toolboxes_<%=position%>
                       value="<%=aName%>"
                       class="chkBox" disabled="true"/>
            </td>
            <td><label>
                <%=aName%>
            </label>
            </td>
            <td><fmt:message key="bam.tool.status.tobeundeployed"></fmt:message>
            </td>
            <td>
            </td>

        </tr>
        <%
                    }
                }
            }
        %>
        </tbody>
    </table>
</form>

<table>
    <tbody>
    <tr>
        <td></td>
    </tr>
    <tr>
        <td><a class="icon-link" style="background-image:url(images/add.gif);" href="uploadbar.jsp"><fmt:message
                key="add.new.toolbox"/></a></td>
    </tr>
    </tbody>
</table>

</div>
</div>

</fmt:bundle>
