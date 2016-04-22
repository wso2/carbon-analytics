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
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="org.wso2.carbon.event.execution.manager.ui.ExecutionManagerUIUtils" %>
<%@ page import="org.wso2.carbon.event.execution.manager.stub.ExecutionManagerAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.execution.manager.admin.dto.configuration.xsd.TemplateConfigurationDTO" %>
<%@ page import="org.wso2.carbon.event.execution.manager.admin.dto.domain.xsd.TemplateDTO" %>
<%@ page import="org.wso2.carbon.event.execution.manager.admin.dto.domain.xsd.ParameterDTO" %>
<%@ page import="org.wso2.carbon.event.execution.manager.admin.dto.domain.xsd.TemplateDomainDTO" %>
<%@ page import="org.wso2.carbon.event.execution.manager.admin.dto.configuration.xsd.ParameterDTOE" %>
<%@ page import="org.apache.axis2.AxisFault" %>

<fmt:bundle basename="org.wso2.carbon.event.execution.manager.ui.i18n.Resources">
<!doctype html>
<html>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>CEP - Execution Manager</title>

    <link rel="icon" href="../admin/images/favicon.ico" type="image/x-icon"/>
    <link rel="shortcut icon" href="../admin/images/favicon.ico" type="image/x-icon"/>

    <link href="css/bootstrap.min.css" rel="stylesheet">
    <link href="css/common.css" rel="stylesheet">
    <link href="css/custom.css" rel="stylesheet">
    <script src="js/jquery.min.js"></script>
    <script type="text/javascript" src="js/domain_config_update.js"></script>
    <!--[if lt IE 9]>
    <script src="js/html5shiv.min.js"></script>
    <script src="js/respond.min.js"></script>
    <![endif]-->

    <script type="application/javascript">
        //create redirect URL to dashboard in session log outs
        createCookie("requestedURI", "../../carbon/execution-manager/domains_ajaxprocessor.jsp", 1);
    </script>
</head>
<body>

<div class="container col-lg-12 col-md-12 col-sm-12">

<!-- header -->
<header>
    <div class="row wr-global-header">
        <div class="col-sm-8 app-logo"><img src="images/logo.png"/>

            <h2 class="app-title">
                <fmt:message key='application.name'/></h2>
        </div>
        <div class="col-sm-4 wr-auth-container">
            <div class="wr-auth pull-right">
                <a href="#" data-toggle="dropdown" class="" aria-expanded="false">
                    <div class="auth-img">
                        <span><%=session.getAttribute("logged-user") + "@" + session.getAttribute("tenantDomain") %>
                        </span>&nbsp;&nbsp;<i class="glyphicon glyphicon-user"></i>
                    </div>
                </a>

                <div class="dropdown-menu">
                    <div class="cu-arrow"></div>
                    <div class="dropdown-menu-content">
                        <a class="filter-item" href="logout_ajaxprocessor.jsp"> Sign out</a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</header>
<!-- /header -->


<%
    if (request.getParameter("domainName") != null) {

        String configurationName = "";
        String templateType = "";
        String domainName = "";
        Boolean isExistingConfig = false;

        if (request.getParameter("configurationName") != null) {
            configurationName = request.getParameter("configurationName");
        }

        if (request.getParameter("domainName") != null) {
            domainName = request.getParameter("domainName");
        }

        if (request.getParameter("templateType") != null) {
            templateType = request.getParameter("templateType");
        }
%>

<%

    try {

        ExecutionManagerAdminServiceStub proxy =
                ExecutionManagerUIUtils.getExecutionManagerAdminService(config, session);
        TemplateDomainDTO domain =
                proxy.getDomain(domainName);

        TemplateConfigurationDTO configurationDTO = proxy.getConfiguration(domainName,
                configurationName);

        if (configurationDTO != null) {
            isExistingConfig = true;
        }

        TemplateDTO currentTemplate = null;
        String saveButtonText = "template.add.button.text";
        String parameterString = "";

%>


<!-- content/body -->
<div class="container c-both">

    <div class="row">
        <div class="container col-md-12">
            <div class="wr-head"><h2><fmt:message key='template.header.text'/></h2></div>
        </div>
        <div class="container col-md-12">
            <ol class="breadcrumb">
                <li><a href="domains_ajaxprocessor.jsp"><fmt:message key='application.name'/></a></li>
                <li><a href="domain_configurations_ajaxprocessor.jsp?domainName=<%=domainName%>"><%=domainName%>
                </a></li>
                <li class="active"><fmt:message key='domain.navigation.text'/></li>
            </ol>
        </div>
    </div>
    <div class="row">
        <div class="container col-md-12 marg-top-20">
            <%

                if (domain.getTemplateDTOs() != null && !templateType.equals("")) {
                    for (TemplateDTO template : domain.getTemplateDTOs()) {
                        if (configurationName == null || template.getName().equals(templateType)) {
                            currentTemplate = template;
                            break;
                        }
                    }
                } else if (domain.getTemplateDTOs() != null && domain.getTemplateDTOs().length > 0) {
                    currentTemplate = domain.getTemplateDTOs()[0];
                }



                if (currentTemplate != null) {
                    String parameterValue = "";
                    String description = "";
            %>
            <label class="input-label col-md-5"><fmt:message key='template.label.text'/></label>

            <div class="input-control input-full-width col-md-7 text">
                <select id="cBoxTemplates"
                        onchange="document.location.href=document.getElementById('cBoxTemplates').options[document.getElementById('cBoxTemplates').selectedIndex].value">
                    <%
                        for (TemplateDTO templateDTO : domain.getTemplateDTOs()) {

                            String selectedValue = "";
                            if (templateDTO.getName().trim().equals(currentTemplate.getName())) {
                                selectedValue = "selected=true";
                            }
                    %>
                    <option <%=selectedValue%>
                            value="template_configurations_ajaxprocessor.jsp?configurationName=<%=configurationName%>&domainName=<%=domainName%>&templateType=<%=templateDTO.getName()%>">
                        <%=templateDTO.getName()%>
                    </option>
                    <%}%>
                </select>

                <div class="sectionHelp"><%=currentTemplate.getDescription()%>
                </div>
            </div>

            <%

                if (isExistingConfig) {
                    configurationName = configurationDTO.getName().trim();
                    saveButtonText = "template.update.button.text";
                }
            %>
            <label class="input-label col-md-5"><fmt:message key='template.label.configuration.name'/></label>

            <div class="input-control input-full-width col-md-7 text">
                <input type="text" id="txtName"
                       value="<%=configurationName%>"
                        <% if (isExistingConfig) {
                            out.print("readOnly");
                        }%>/>
            </div>


            <%

                if (isExistingConfig) {
                    description = configurationDTO.getDescription().trim();
                }
            %>
            <label class="input-label col-md-5"><fmt:message key='template.label.configuration.description'/></label>

            <div class="input-control input-full-width col-md-7 text">
                <input type="text" id="txtDescription"
                       value="<%=description%>"/>
            </div>

            <br class="c-both"/>
            <br class="c-both"/>
            <h4><fmt:message key='template.parameter.header.text'/></h4>


            <%
              int indexParam = 0;

              if (currentTemplate.getParameterDTOs() != null) {
                for (ParameterDTO parameter : currentTemplate.getParameterDTOs()) {

                    if (parameter == null) {
                        continue;
                    }
                    if (!isExistingConfig) {
                        parameterValue = parameter.getDefaultValue().trim();
                    } else if (configurationDTO.getParameterDTOs() != null) {

                        for (ParameterDTOE param : configurationDTO.getParameterDTOs()) {
                            if (param.getName().equals(parameter.getName())) {
                                parameterValue = param.getValue().trim();
                                break;
                            }
                        }
                    }
            %>


            <label class="input-label col-md-5"><%
                if (parameter.getDisplayName() == null) {
                    out.print(parameter.getName());
                } else {
                    out.print(parameter.getDisplayName());
                }
            %></label>


            <div class="input-control input-full-width col-md-7 text">

                <%
                    if (parameter.getOptions() != null && !parameter.getOptions().trim().equals("")) {
                %>

                <select id="<%=parameter.getName()%>">
                    <%
                        String[] options = parameter.getOptions().split(",");
                        for (String option : options) {

                            String selectedValue = "";

                            if (option.trim().equals(parameterValue)) {
                                selectedValue = "selected=true";
                            }
                    %>
                    <option <%=selectedValue%> value=<%=option%>>
                        <%=option%>
                    </option>
                    <%}%>
                </select>

                <%
                } else {
                %>
                <input type="text" id="<%=parameter.getName()%>"
                       value="<%=parameterValue%>"/>
                <%
                    }

                    if (!parameter.getDescription().equals("")) {
                %>
                <div class="sectionHelp"><%=parameter.getDescription()%>
                </div>
                <%}%>
            </div>
            <%
                        parameterString += "'" + parameter.getName() +
                                "::' + document.getElementById('"
                                + parameter.getName() + "').value";

                        indexParam++;
                        if (indexParam < currentTemplate.getParameterDTOs().length) {
                            parameterString += "+ ',' +";
                        }

                    }
                }
              }
            %>

            <br class="c-both"/>
            <hr class="wr-separate"/>

<%
String executionParamString = null;
if ("batch".equals(currentTemplate.getExecutionType())) {
%>
           <h4><fmt:message key='template.scheduler.header.text'/></h4>

 <label class="input-label col-md-5">
 <fmt:message key='template.scheduler.label.text'/>
 </label>
 <div class="input-control input-full-width col-md-7 text">

<%
if (isExistingConfig && (configurationDTO.getExecutionParameters() != null)) {
%>

                    <input type="text" id="cronExpressionValue"
                                           value="<%=configurationDTO.getExecutionParameters()%>"/>

                    <%
                } else {
                %>
 <input type="text" id="cronExpressionValue"
                       value=""/>

<%
}
%>
               </div>

<%
    executionParamString =  "document.getElementById('"
                                + "cronExpressionValue" + "').value";

    if (parameterString.length() < 1) {
        parameterString = "''";
    }
}
%>

            <div class="action-container">
                <button type="button" class="btn btn-default btn-add col-md-2 col-xs-12 pull-right marg-right-15"
                        onclick="saveConfiguration('<%=domainName%>',
                                document.getElementById('cBoxTemplates').options[document.getElementById('cBoxTemplates').selectedIndex].text,
                                document.getElementById('txtName').value, document.getElementById('txtDescription').value,'domain_configurations_ajaxprocessor.jsp?domainName=<%=domainName%>',
                                <%=parameterString%>, <%=executionParamString%>)">
                    <fmt:message key='<%=saveButtonText%>'/>
                </button>
                <br class="c-both"/>
            </div>
        </div>
    </div>
    <div class="row pad-bot-50">
        <div class="container col-md-8">
            &nbsp;
        </div>
        <div class="container col-md-4">
            &nbsp;
        </div>
        <br class="c-both "/>
    </div>

</div>
<!-- /content/body -->

</div>
</div>

<div id="dialogBox"></div>

<footer class="footer">
    <p>&copy; 2015 WSO2 Inc. All Rights Reserved</p>
</footer>

<script src="js/bootstrap.min.js"></script>


<script type="text/javascript">

    $(document).ready(function () {

        $('[data-toggle="tooltip"]').tooltip();

        $("[data-toggle=popover]").popover();

        $(".ctrl-asset-type-switcher").popover({
            html: true,
            content: function () {
                return $('#content-asset-types').html();
            }
        });

        $(".ctrl-filter-type-switcher").popover({
            html: true,
            content: function () {
                return $('#content-filter-types').html();
            }
        });

        $('#nav').affix({
            offset: {
                top: $('header').height()
            }
        });
    });

</script>

<%
        } catch (AxisFault e) {
            response.sendRedirect("domain_session_handler_ajaxprocessor.jsp");
        }
    }

%>


</body>
</html>

</fmt:bundle>