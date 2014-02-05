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
<%@ page import="org.wso2.carbon.bam.toolbox.deployer.stub.BAMToolboxDepolyerServiceStub" %>
<%@ page import="org.wso2.carbon.bam.toolbox.deployer.stub.BAMToolboxDepolyerServiceStub.BasicToolBox" %>
<%@ page import="org.wso2.carbon.bam.toolbox.deployer.stub.BAMToolboxDepolyerServiceStub.ToolBoxStatusDTO" %>
<%@ page import="org.wso2.carbon.bam.toolbox.deployer.ui.client.BAMToolBoxDeployerClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<fmt:bundle basename="org.wso2.carbon.bam.toolbox.deployer.ui.i18n.Resources">
<script src="../editarea/edit_area_full.js" type="text/javascript"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<link rel="stylesheet" type="text/css" href="css/toolbox-styles.css" />

<carbon:breadcrumb label="available.bam.tools"
                   resourceBundle="org.wso2.carbon.bam.toolbox.deployer.ui.i18n.Resources"
                   topPage="false" request="<%=request%>"/>


<%
    String success = request.getParameter("success");
    String message = "";
    if (null != success && success.equalsIgnoreCase("false")) {
        message = request.getParameter("message");
    }
    if (!message.equals("")) {
%>
<script type="text/javascript">
    CARBON.showErrorDialog("Error installing uploaded toolbox. <%=message%>");
</script>

<% }

    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    BAMToolBoxDeployerClient client = new BAMToolBoxDeployerClient(cookie, serverURL, configContext);
    BasicToolBox[] toolBoxes = null;
    ToolBoxStatusDTO toolsInRepo = null;
    try {
        toolBoxes = client.getAllBasicTools();
        toolsInRepo = client.getToolBoxStatus("1", null);
    } catch (Exception e) {
%>
<script type="text/javascript">
    CARBON.showErrorDialog("Error while getting the status of BAM ToolBox");
</script>
<%
    }
%>

<%!
    private boolean isToolInRepo(String toolName, ToolBoxStatusDTO statusDTO) {
        if (isInList(toolName, statusDTO.getDeployedTools())) {
            return true;
        } else if (isInList(toolName, statusDTO.getToBeDeployedTools())) {
            return true;
        } else if (isInList(toolName, statusDTO.getToBeUndeployedTools())) {
            return true;
        } else {
            return false;
        }
    }


    private boolean isInList(String name, String[] searchList) {
        name = name.replaceAll(".tbox", "");
        if (null != searchList) {
            for (String aName : searchList) {
                if (name.equalsIgnoreCase(aName)) {
                    return true;
                }
            }
            return false;
        } else {
            return false;
        }
    }
%>

<script type="text/javascript">
    enableCustomToolBox();

    function deployToolBox() {
        var opt = document.getElementsByName('typeToolbox');
        var selected = '';
        for (var i = 0, length = opt.length; i < length; i++) {
            if (opt[i].checked) {
                selected = opt[i].value;
            }
        }
        document.getElementById('selectedToolType').value = selected;

        if (selected == 0) {
            var toolbox = document.getElementById('toolbox').value;
            if ('' == toolbox) {
                CARBON.showErrorDialog('No ToolBox has been selected!');
            } else if (toolbox.indexOf('.tbox') == -1) {
                CARBON.showErrorDialog('The ToolBox should be \'tbox\' artifact');
            } else {
                document.getElementById('uploadBar').submit();
            }
        } else if (selected == -1) {
            var urltoolbox = document.getElementById('urltoolbox').value;
            if ('' == urltoolbox) {
                CARBON.showErrorDialog('No ToolBox has been selected!');
            } else if (urltoolbox.indexOf('.tbox') == -1) {
                CARBON.showErrorDialog('The ToolBox should be \'tbox\' artifact');
            } else if (!isValidURL(urltoolbox, document.getElementById('urltoolbox'))) {
                CARBON.showErrorDialog('The URL is not valid! Please enter a valid url!');
            } else {
                document.getElementById('uploadBar').submit();
            }
        } else {
            document.getElementById('uploadBar').submit();
        }
    }

    function isValidURL(url, element) {
        // if user has not entered http:// https:// or ftp:// assume they mean http://
        if (!/^(http|https|ftp):\/\//i.test(url)) {
            url = 'http://' + url; // set both the value
            $(element).val(url);
        }
        // now check if valid url
        return /^(http|https|ftp):\/\/(((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&amp;'\(\)\*\+,;=]|:)*@)?(((\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5]))|((([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.)+(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.?)(:\d*)?)(\/((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&amp;'\(\)\*\+,;=]|:|@)+(\/(([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&amp;'\(\)\*\+,;=]|:|@)*)*)?)?(\?((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&amp;'\(\)\*\+,;=]|:|@)|[\uE000-\uF8FF]|\/|\?)*)?(\#((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&amp;'\(\)\*\+,;=]|:|@)|\/|\?)*)?$/i.test(url);
    }

    function cancelDeploy() {
        location.href = "listbar.jsp?region=region1&item=list_toolbox_menu";
    }

    function enableCustomToolBox() {
        var opt = document.getElementsByName('typeToolbox');
        var selected = '';
        for (var i = 0, length = opt.length; i < length; i++) {
            if (opt[i].checked) {
                selected = opt[i].value;
            }
        }
        if (selected == '0' || selected == '') {
            document.getElementById('toolbox').disabled = false;
            document.getElementById('urltoolbox').disabled = true;
        }
        else if (selected == '-1') {
            document.getElementById('toolbox').disabled = true;
            document.getElementById('urltoolbox').disabled = false;
        } else {
            document.getElementById('toolbox').disabled = true;
            document.getElementById('urltoolbox').disabled = true;
        }
    }
</script>

<div id="middle">
    <h2>Add Tool Box</h2>

    <div id="workArea">
        <form id="uploadBar" name="uploadBar" enctype="multipart/form-data"
              action="../../fileupload/bamToolboxDeploy" method="POST">

         <%   boolean checked = false;
             if(null != toolBoxes && toolBoxes.length>0){%>
            <table class="styledLeft">

                <thead>
                <tr>
                    <th colspan="4">
                        <fmt:message key="inbuilt.toolbox"/>
                    </th>
                </tr>
                </thead>
                <tbody>


                <% int count = 1;

                    if (null != toolBoxes) {
                        for (BasicToolBox aToolbox : toolBoxes) {
                            String isDisabled = "";
                            if (isToolInRepo(aToolbox.getTBoxFileName(), toolsInRepo)) {
                                isDisabled = "disabled=\"true\"";
                            } else {
                                isDisabled = "";
                            }
                %>

                <tr>
                    <td width="10px">
                        <%=count%>.
                    </td>
                    <td width="10px">
                        <%
                            if (isDisabled.equals("") && !checked) {
                                checked = true;
                        %>
                        <input type="radio" name="typeToolbox" value="<%=aToolbox.getSampleId()%>"
                               onclick="enableCustomToolBox();" checked="true" <%=isDisabled%>/>
                        <%
                        } else {
                        %>
                        <input type="radio" name="typeToolbox" value="<%=aToolbox.getSampleId()%>"
                               onclick="enableCustomToolBox();" <%=isDisabled%>/>
                        <%
                            }
                        %>
                    </td>
                    <td>
                        <%=aToolbox.getDisplayName()%> <fmt:message key="toolbox"/>
                    <div class="toolBoxInfo">
                        <%=aToolbox.getDescription()%>
                    </div>
                    </td>
                    <%
                        count++;
                    %>
                </tr>

                <%
                        }
                    }
                %>



                </tbody>
            </table>
            <%
                }
            %>
            <br />


            <table class="styledLeft tool-box-table-view">
                <thead>
                <tr>
                    <th colspan="4">
                        <fmt:message key="custom.toolbox"/>
                    </th>
                </tr>
                </thead>
                <tbody>


                <tr>
                    <td width="10px">
                    </td>
                    <td width="10px">
                        <%
                            if (!checked) {
                        %>
                        <input type="radio" name="typeToolbox" value="0" checked="true"
                               onclick="enableCustomToolBox();"/>
                        <%
                        } else {
                        %>
                        <input type="radio" name="typeToolbox" value="0" onclick="enableCustomToolBox();"/>
                        <%
                            }
                        %>
                    </td>

                    <%
                        if (!checked) {
                    %>
                    <td>
                        <nobr><fmt:message key="toolbox.path.from.file.system"/> <span
                                class="required">*</span>&nbsp;&nbsp;&nbsp;
                        </nobr>
                    </td>
                    <td>
                        <nobr>
                            <input type="file" name="toolbox"
                                   id="toolbox" size="80px"/>
                        </nobr>
                    </td>

                    <%
                    } else {
                    %>
                    <td>
                        <nobr><fmt:message key="toolbox.path.from.file.system"/> <span
                                class="required">*</span>&nbsp;&nbsp;&nbsp;
                        </nobr>
                    </td>
                    <td>
                        <nobr>
                            <input type="file" name="toolbox"
                                   id="toolbox" size="80px" disabled="true"/>
                        </nobr>
                    </td>
                    <%
                        }
                    %>

                </tr>

                <tr>
                    <td width="10px">
                    </td>
                    <td width="10px">
                        <input type="radio" name="typeToolbox" value="-1"
                               onclick="enableCustomToolBox();"/>
                    </td>
                    <td>

                        <nobr><fmt:message key="toolbox.path.from.url"/> <span
                                class="required">*</span>&nbsp;&nbsp;&nbsp;
                        </nobr>
                    </td>
                    <td>
                        <nobr>
                            <input name="urltoolbox"
                                   id="urltoolbox" size="80px" value="http://" disabled="true"/>
                        </nobr>
                    </td>

                </tr>
                </tbody>
                </table>
            <br />
        <table class="styledLeft">
            <tbody>
                <tr>
                    <td class="buttonRow" colspan="3">
                                    <input type="button" value="<fmt:message key="deploy"/>"
                                           class="button" name="deploy"
                                           onclick="javascript:deployToolBox();"/>
                                    <input type="button" value="<fmt:message key="cancel"/>"
                                           name="cancel" class="button"
                                           onclick="javascript:cancelDeploy();"/>
                    </td>
                </tr>
                <input type="hidden" id="selectedToolType" name="selectedToolType"/>
                </tbody>
        </table>
        </form>
    </div>
</div>


</fmt:bundle>
