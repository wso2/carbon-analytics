<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<fmt:bundle basename="org.wso2.carbon.event.formatter.ui.i18n.Resources">

    <carbon:breadcrumb
            label="add"
            resourceBundle="org.wso2.carbon.event.formatter.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>

    <link type="text/css" href="css/eventFormatter.css" rel="stylesheet"/>
    <script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../admin/js/cookies.js"></script>
    <script type="text/javascript" src="../admin/js/main.js"></script>
    <script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
    <script type="text/javascript" src="../yui/build/connection/connection-min.js"></script>
    <script type="text/javascript" src="../eventformatter/js/event_formatter.js"></script>
    <script type="text/javascript"
            src="../eventformatter/js/create_eventFormatter_helper.js"></script>
    <script type="text/javascript" src="../ajax/js/prototype.js"></script>


    <script type="text/javascript">
        jQuery(document).ready(function () {
            showMappingContext();
        });
    </script>


    <div id="middle">
        <h2><fmt:message key="title.event.formatter.create"/></h2>

        <div id="workArea">

            <form name="inputForm" action="index.jsp?ordinal=1" method="get" id="addEventFormatter">
                <table style="width:100%" id="eventFormatterAdd" class="styledLeft">
                    <% EventFormatterAdminServiceStub eventFormatterstub = EventFormatterUIUtils.getEventFormatterAdminService(config, session, request);
                        String[] outputStreamNames = eventFormatterstub.getAllEventStreamNames();
                        OutputEventAdaptorInfoDto[] outputEventAdaptorInfoDtos = eventFormatterstub.getOutputEventAdaptorInfo();
                        if (outputStreamNames != null && outputEventAdaptorInfoDtos != null) {
                    %>
                    <thead>
                    <tr>
                        <th><fmt:message key="title.event.formatter.details"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td class="formRaw">
                            <%@include file="inner_eventFormatter_ui.jsp" %>
                        </td>
                    </tr>
                    <tr>
                        <td class="buttonRow">
                            <input type="button" value="<fmt:message key="add.event.formatter"/>"
                                   onclick="addEventFormatter(document.getElementById('addEventFormatter'))"/>
                        </td>
                    </tr>
                    </tbody>
                    <%
                    } else { %>

                    <tbody>
                    <tr>
                        <td class="formRaw">
                            <table id="noEventBuilderInputTable" class="normal-nopadding"
                                   style="width:100%">
                                <tbody>

                                <tr>
                                    <td class="leftCol-med" colspan="2">Event Streams and/or Output
                                        Event Adaptors are not available
                                    </td>
                                </tr>
                                </tbody>
                            </table>
                        </td>
                    </tr>
                    </tbody>


                    <% }

                    %>
                </table>


            </form>
        </div>
    </div>
</fmt:bundle>
