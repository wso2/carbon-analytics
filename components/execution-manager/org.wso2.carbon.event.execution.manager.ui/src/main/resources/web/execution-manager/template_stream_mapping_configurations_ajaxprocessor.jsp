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
<%@ page import="org.wso2.carbon.event.stream.stub.EventStreamAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.execution.manager.admin.dto.configuration.xsd.TemplateConfigurationDTO" %>
<%@ page import="org.wso2.carbon.event.execution.manager.admin.dto.domain.xsd.TemplateDTO" %>
<%@ page import="org.wso2.carbon.event.execution.manager.admin.dto.domain.xsd.ParameterDTO" %>
<%@ page import="org.wso2.carbon.event.execution.manager.admin.dto.domain.xsd.TemplateDomainDTO" %>
<%@ page import="org.wso2.carbon.event.execution.manager.admin.dto.configuration.xsd.ParameterDTOE" %>
<%@ page import="org.apache.axis2.AxisFault" %>
<%@ page import="com.sun.xml.internal.bind.v2.TODO" %>
<%@ page import="java.util.Arrays" %>

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
/*
                ExecutionManagerAdminServiceStub proxy =
                        ExecutionManagerUIUtils.getExecutionManagerAdminService(config, session);
                TemplateDomainDTO domain =
                        proxy.getDomain(domainName);

                TemplateConfigurationDTO configurationDTO = proxy.getConfiguration(domainName,
                        configurationName);

                if (configurationDTO != null) {
                    isExistingConfig = true;
                }*/

//                TemplateDTO currentTemplate = null;
                String saveButtonText = "template.add.button.text";

                //Stream Mapping Configuration
                String toSteamNameID="org.wso2.event.test.stream:1.0.0";
                EventStreamAdminServiceStub eventStreamAdminServiceStub = ExecutionManagerUIUtils.getEventStreamAdminService(config, session, request);
                String[] streamIds = eventStreamAdminServiceStub.getStreamNames();
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

                        <%--Adding Stream Mapping configurations--%>
                    <h4><fmt:message key='template.stream.header.text'/></h4>

                    <label class="input-label col-md-5"><fmt:message key='template.label.to.stream.name'/></label>

                    <div class="input-control input-full-width col-md-7 text">
                        <input type="text" id="toStreamID"
                               value="<%=toSteamNameID%>" readonly="true"/>
                    </div>

                    <label class="input-label col-md-5"><fmt:message key='template.label.from.stream.name'/></label>

                    <div class="input-control input-full-width col-md-7 text">
                        <select id="fromStreamID" onchange="loadMappingFromStreamAttributes()">
                            <option selected disabled>Choose from here</option>
                            <%
                                if (streamIds != null) {
                                    Arrays.sort(streamIds);
                                    for (String aStreamId : streamIds) {
                            %>
                            <option id="fromStreamOptionID"><%=aStreamId%>
                            </option>
                            <%
                                    }
                                }
                            %>

                        </select>
                    </div>

                    <div id="MapAttributesTable" class="input-label col-md-5">
                        <div id="outerDiv">
                        </div>
                    </div>

                    <%
                        String executionParamString = null;
                        executionParamString = "document.getElementById('"
                                + "toStreamID" + "').value";

                    %>

                    <div class="action-container">
                        <button type="button"
                                class="btn btn-default btn-add col-md-2 col-xs-12 pull-right marg-right-15"
                                onclick="saveStreamConfiguration(<%=executionParamString%>,'domain_configurations_ajaxprocessor.jsp?domainName=<%=domainName%>')">
                            <fmt:message key='<%=saveButtonText%>'/>
                        </button>
                    </div>

                </div>
            </div>

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