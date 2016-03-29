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
<%@ page import="org.apache.axis2.AxisFault" %>
<%@ page import="org.wso2.carbon.event.execution.manager.admin.dto.domain.xsd.TemplateDomainInfoDTO" %>

<fmt:bundle basename="org.wso2.carbon.event.execution.manager.ui.i18n.Resources">

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
        <script src="js/domain_config_update.js"></script>
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


        <!-- content/body -->
        <div class="container c-both">

            <div class="row">
                <div class="container col-md-12">
                    <div class="wr-head"><h2>Domains</h2></div>
                </div>
            </div>

            <div class="row">
            <%
                ExecutionManagerAdminServiceStub proxy = ExecutionManagerUIUtils.getExecutionManagerAdminService(config, session);
                try {
                    TemplateDomainInfoDTO[] domainDTOs = proxy.getAllDomainsInfo();

                    if (domainDTOs != null && domainDTOs.length > 0) {

                        for (TemplateDomainInfoDTO domainDTO : domainDTOs) {
            %>



                <div class="col-sm-6">
                    <a href="domain_configurations_ajaxprocessor.jsp?ordinal=1&domainName=<%=domainDTO.getName()%>">
                        <div class="tile green">
                            <h3 class="title"><%=domainDTO.getName()%>
                            </h3>

                            <p><%=domainDTO.getDescription()%>
                            </p>
                            <!--   <div class="edit-domain tright"><i class="glyphicon glyphicon-cog"></i> Edit</div>-->
                        </div>
                    </a>
                </div>
                <%
                    }
                } else {
                %>
                <table class="styledLeft">
                    <tbody>
                    <tr>
                        <td class="formRaw">
                            <table id="noEventReceiverInputTable" class="normal-nopadding" style="width:100%">
                                <tbody>
                                <tr>
                                    <td class="leftCol-med" colspan="2"><fmt:message key='main.empty.text'/>
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
                    } catch (AxisFault e) {
                        response.sendRedirect("domain_session_handler_ajaxprocessor.jsp");
                    }
                %>


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
    </body>
    </html>
</fmt:bundle>