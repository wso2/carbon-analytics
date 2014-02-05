<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ page
        import="org.wso2.carbon.event.input.adaptor.manager.ui.InputEventAdaptorUIUtils" %>
<%@ page
        import="org.wso2.carbon.event.input.adaptor.manager.stub.types.InputEventAdaptorConfigurationInfoDto" %>
<%@ page
        import="org.wso2.carbon.event.input.adaptor.manager.stub.InputEventAdaptorManagerAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.input.adaptor.manager.stub.types.InputEventAdaptorFileDto" %>

<fmt:bundle basename="org.wso2.carbon.event.input.adaptor.manager.ui.i18n.Resources">

<carbon:breadcrumb
        label="add.input.event.adaptor"
        resourceBundle="org.wso2.carbon.event.input.adaptor.manager.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>

<script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../admin/js/cookies.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>

<script type="text/javascript">
    var ENABLE = "enable";
    var DISABLE = "disable";
    var STAT = "statistics";
    var TRACE = "Tracing";

    function doDelete(eventName) {
        var theform = document.getElementById('deleteForm');
        theform.eventname.value = eventName;
        theform.submit();
    }

    function disableStat(eventAdaptorName) {
        jQuery.ajax({
                   type:'POST',
                   url:'stat_tracing-ajaxprocessor.jsp',
                   data:'eventAdaptorName=' + eventAdaptorName + '&action=disableStat',
                   async:false,
                   success:function (msg) {
                       handleCallback(eventAdaptorName, DISABLE, STAT);
                   },
                   error:function (msg) {
                       CARBON.showErrorDialog('<fmt:message key="stat.disable.error"/>' +
                                              ' ' + eventAdaptorName);
                   }
               });
    }

    function enableStat(eventAdaptorName) {
        jQuery.ajax({
                   type:'POST',
                   url:'stat_tracing-ajaxprocessor.jsp',
                   data:'eventAdaptorName=' + eventAdaptorName + '&action=enableStat',
                   async:false,
                   success:function (msg) {
                       handleCallback(eventAdaptorName, ENABLE, STAT);
                   },
                   error:function (msg) {
                       CARBON.showErrorDialog('<fmt:message key="stat.enable.error"/>' +
                                              ' ' + eventAdaptorName);
                   }
               });
    }

    function handleCallback(eventAdaptor, action, type) {
        var element;
        if (action == "enable") {
            if (type == "statistics") {
                element = document.getElementById("disableStat" + eventAdaptor);
                element.style.display = "";
                element = document.getElementById("enableStat" + eventAdaptor);
                element.style.display = "none";
            } else {
                element = document.getElementById("disableTracing" + eventAdaptor);
                element.style.display = "";
                element = document.getElementById("enableTracing" + eventAdaptor);
                element.style.display = "none";
            }
        } else {
            if (type == "statistics") {
                element = document.getElementById("disableStat" + eventAdaptor);
                element.style.display = "none";
                element = document.getElementById("enableStat" + eventAdaptor);
                element.style.display = "";
            } else {
                element = document.getElementById("disableTracing" + eventAdaptor);
                element.style.display = "none";
                element = document.getElementById("enableTracing" + eventAdaptor);
                element.style.display = "";
            }
        }
    }

    function enableTracing(eventAdaptorName) {
        jQuery.ajax({
                   type:'POST',
                   url:'stat_tracing-ajaxprocessor.jsp',
                   data:'eventAdaptorName=' + eventAdaptorName + '&action=enableTracing',
                   async:false,
                   success:function (msg) {
                       handleCallback(eventAdaptorName, ENABLE, TRACE);
                   },
                   error:function (msg) {
                       CARBON.showErrorDialog('<fmt:message key="trace.enable.error"/>' +
                                              ' ' + eventAdaptorName);
                   }
               });
    }

    function disableTracing(eventAdaptorName) {
        jQuery.ajax({
                   type:'POST',
                   url:'stat_tracing-ajaxprocessor.jsp',
                   data:'eventAdaptorName=' + eventAdaptorName + '&action=disableTracing',
                   async:false,
                   success:function (msg) {
                       handleCallback(eventAdaptorName, DISABLE, TRACE);
                   },
                   error:function (msg) {
                       CARBON.showErrorDialog('<fmt:message key="trace.disable.error"/>' +
                                              ' ' + eventAdaptorName);
                   }
               });
    }

</script>
<%
    String eventName = request.getParameter("eventname");
    int totalEventAdaptors = 0;
    int totalNotDeployedEventAdaptors = 0;
    if (eventName != null) {
        InputEventAdaptorManagerAdminServiceStub stub = InputEventAdaptorUIUtils.getInputEventManagerAdminService(config, session, request);
        stub.undeployActiveInputEventAdaptorConfiguration(eventName);
%>
<script type="text/javascript">CARBON.showInfoDialog('Event adaptor successfully deleted.');</script>
<%
    }

    InputEventAdaptorManagerAdminServiceStub stub = InputEventAdaptorUIUtils.getInputEventManagerAdminService(config, session, request);
    InputEventAdaptorConfigurationInfoDto[] eventDetailsArray = stub.getAllActiveInputEventAdaptorConfiguration();
    if (eventDetailsArray != null) {
        totalEventAdaptors = eventDetailsArray.length;
    }

    InputEventAdaptorFileDto[] notDeployedEventAdaptorConfigurationFiles = stub.getAllInactiveInputEventAdaptorConfigurationFile();
    if (notDeployedEventAdaptorConfigurationFiles != null) {
        totalNotDeployedEventAdaptors = notDeployedEventAdaptorConfigurationFiles.length;
    }

%>

<div id="middle">
<h2><fmt:message key="available.input.event.adaptors"/></h2>
<a href="create_eventAdaptor.jsp?ordinal=1"
   style="background-image:url(images/add.gif);"
   class="icon-link">
    Add Input Event Adaptor
</a>
<br/> <br/>
<div id="workArea">

    <%=totalEventAdaptors%> <fmt:message
            key="active.event.adaptors"/> <% if (totalNotDeployedEventAdaptors > 0) { %><a
            href="event_adaptor_files_details.jsp?ordinal=1"><%=totalNotDeployedEventAdaptors%>
        <fmt:message
                key="inactive.event.adaptors"/></a><% } else {%><%=totalNotDeployedEventAdaptors%>
        <fmt:message key="inactive.event.adaptors"/> <% } %>
    <br/><br/>
    <table class="styledLeft">
        <%

            if (eventDetailsArray != null) {
        %>

        <thead>
        <tr>
            <th><fmt:message key="event.adaptor.name"/></th>
            <th><fmt:message key="event.adaptor.type"/></th>
            <th width="420px"><fmt:message key="actions"/></th>
        </tr>
        </thead>
        <tbody>
                <%
                for (InputEventAdaptorConfigurationInfoDto eventDetails : eventDetailsArray) {
            %>
        <tr>
            <td>
                <a href="event_details.jsp?ordinal=1&eventName=<%=eventDetails.getEventAdaptorName()%>&eventType=<%=eventDetails.getEventAdaptorType()%>"><%=eventDetails.getEventAdaptorName()%>
                </a>

            </td>
            <td><%=eventDetails.getEventAdaptorType().trim()%>
            </td>
            <td>
                <% if (eventDetails.getEnableStats()) {%>
                <div class="inlineDiv">
                    <div id="disableStat<%= eventDetails.getEventAdaptorName()%>">
                        <a href="#"
                           onclick="disableStat('<%= eventDetails.getEventAdaptorName() %>')"
                           class="icon-link"
                           style="background-image:url(../admin/images/static-icon.gif);"><fmt:message
                                key="stat.disable.link"/></a>
                    </div>
                    <div id="enableStat<%= eventDetails.getEventAdaptorName()%>"
                         style="display:none;">
                        <a href="#"
                           onclick="enableStat('<%= eventDetails.getEventAdaptorName() %>')"
                           class="icon-link"
                           style="background-image:url(../admin/images/static-icon-disabled.gif);"><fmt:message
                                key="stat.enable.link"/></a>
                    </div>
                </div>
                <% } else { %>
                <div class="inlineDiv">
                    <div id="enableStat<%= eventDetails.getEventAdaptorName()%>">
                        <a href="#"
                           onclick="enableStat('<%= eventDetails.getEventAdaptorName() %>')"
                           class="icon-link"
                           style="background-image:url(../admin/images/static-icon-disabled.gif);"><fmt:message
                                key="stat.enable.link"/></a>
                    </div>
                    <div id="disableStat<%= eventDetails.getEventAdaptorName()%>"
                         style="display:none">
                        <a href="#"
                           onclick="disableStat('<%= eventDetails.getEventAdaptorName() %>')"
                           class="icon-link"
                           style="background-image:url(../admin/images/static-icon.gif);"><fmt:message
                                key="stat.disable.link"/></a>
                    </div>
                </div>
                <% }
                    if (eventDetails.getEnableTracing()) {%>
                <div class="inlineDiv">
                    <div id="disableTracing<%= eventDetails.getEventAdaptorName()%>">
                        <a href="#"
                           onclick="disableTracing('<%= eventDetails.getEventAdaptorName() %>')"
                           class="icon-link"
                           style="background-image:url(../admin/images/trace-icon.gif);"><fmt:message
                                key="trace.disable.link"/></a>
                    </div>
                    <div id="enableTracing<%= eventDetails.getEventAdaptorName()%>"
                         style="display:none;">
                        <a href="#"
                           onclick="enableTracing('<%= eventDetails.getEventAdaptorName() %>')"
                           class="icon-link"
                           style="background-image:url(../admin/images/trace-icon-disabled.gif);"><fmt:message
                                key="trace.enable.link"/></a>
                    </div>
                </div>
                <% } else { %>
                <div class="inlineDiv">
                    <div id="enableTracing<%= eventDetails.getEventAdaptorName()%>">
                        <a href="#"
                           onclick="enableTracing('<%= eventDetails.getEventAdaptorName() %>')"
                           class="icon-link"
                           style="background-image:url(../admin/images/trace-icon-disabled.gif);"><fmt:message
                                key="trace.enable.link"/></a>
                    </div>
                    <div id="disableTracing<%= eventDetails.getEventAdaptorName()%>"
                         style="display:none">
                        <a href="#"
                           onclick="disableTracing('<%= eventDetails.getEventAdaptorName() %>')"
                           class="icon-link"
                           style="background-image:url(../admin/images/trace-icon.gif);"><fmt:message
                                key="trace.disable.link"/></a>
                    </div>
                </div>

                <% } %>
                <a style="background-image: url(../admin/images/delete.gif);"
                   class="icon-link"
                   onclick="doDelete('<%=eventDetails.getEventAdaptorName()%>')"><font
                        color="#4682b4">Delete</font></a>
                <a style="background-image: url(../admin/images/edit.gif);"
                   class="icon-link"
                   href="edit_event_details.jsp?ordinal=1&eventName=<%=eventDetails.getEventAdaptorName()%>"><font
                        color="#4682b4">Edit</font></a>

            </td>


        </tr>
                <%
                    }

                } else{  %>

        <tbody>
        <tr>
            <td class="formRaw">
                <table id="noInputEventAdaptorInputTable" class="normal-nopadding"
                       style="width:100%">
                    <tbody>

                    <tr>
                        <td class="leftCol-med" colspan="2"><fmt:message
                                key="empty.event.adaptor.msg"/>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </td>
        </tr>
        </tbody>


        <% }
        %>
        </tbody>
    </table>

    <div>
        <br/>

        <form id="deleteForm" name="input" action="" method="get"><input type="HIDDEN"
                                                                         name="eventname"
                                                                         value=""/></form>
    </div>
</div>


<script type="text/javascript">
    alternateTableRows('expiredsubscriptions', 'tableEvenRow', 'tableOddRow');
    alternateTableRows('validsubscriptions', 'tableEvenRow', 'tableOddRow');
</script>

</fmt:bundle>
