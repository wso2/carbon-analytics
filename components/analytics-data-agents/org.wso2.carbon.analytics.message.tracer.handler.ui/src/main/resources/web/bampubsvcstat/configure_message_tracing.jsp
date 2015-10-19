<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.analytics.message.tracer.handler.stub.conf.EventingConfigData" %>
<%@ page import="org.wso2.carbon.analytics.message.tracer.handler.ui.MessageTracerHandlerAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%!
%><fmt:bundle basename="org.wso2.carbon.analytics.message.tracer.handler.ui.i18n.Resources">

<carbon:breadcrumb
        label="bam.statistics"
        resourceBundle="org.wso2.carbon.analytics.message.tracer.handler.ui.i18n.Resources"
        topPage="true"
        request="<%=request%>"/>
<%
    String setConfig = request.getParameter("setConfig"); // hidden parameter to check if the form is being submitted

    String enableTracing = request.getParameter("enableTracing");
    String enableMsgBodyDump = request.getParameter("enableMsgBodyDump");
    String enableLogging = request.getParameter("enableLogging");
    String enablePublishToBAM = request.getParameter("enablePublishToBAM");

    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    MessageTracerHandlerAdminClient client = new MessageTracerHandlerAdminClient(
            cookie, backendServerURL, configContext, request.getLocale());
    EventingConfigData eventingConfigData = null;

    if (setConfig != null) {    // form submitted request to set eventing config
        eventingConfigData = new EventingConfigData();

        if (enableTracing != null) {
            eventingConfigData.setMessageTracingEnable(true);
        } else {
            eventingConfigData.setMessageTracingEnable(false);
        }
        if (enableMsgBodyDump != null) {
            eventingConfigData.setDumpBodyEnable(true);
        } else {
            eventingConfigData.setDumpBodyEnable(false);
        }
        if (enableLogging != null) {
            eventingConfigData.setLoggingEnable(true);
        } else {
            eventingConfigData.setLoggingEnable(false);
        }
        if (enablePublishToBAM != null) {
            eventingConfigData.setPublishToBAMEnable(true);
        } else {
            eventingConfigData.setPublishToBAMEnable(false);
        }
        try {
            client.setEventingConfigData(eventingConfigData);

%>
<script type="text/javascript">
    /*jQuery(document).init(function () {*/
        function handleOK() {

        }

        CARBON.showInfoDialog("Eventing Configuration Successfully Updated!", handleOK);
    /*});*/
</script>
<%
} catch (Exception e) {
    if (e.getCause().getMessage().toLowerCase().indexOf("you are not authorized") == -1) {
        response.setStatus(500);
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp"/>
<%
        }
    }
} else {
    try {
        eventingConfigData = client.getEventingConfigData();
    } catch (Exception e) {
        if (e.getCause().getMessage().toLowerCase().indexOf("you are not authorized") == -1) {
            response.setStatus(500);
            CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
            session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp"/>
<%
            }
        }
    }

    boolean isMessageTracingEnable = eventingConfigData.getMessageTracingEnable();
    boolean isMsgDumpingEnable = eventingConfigData.getDumpBodyEnable();
    boolean isLoggingEnable = eventingConfigData.getLoggingEnable();
    boolean isPublishToBAMEnable = eventingConfigData.getPublishToBAMEnable();
%>

<script id="source" type="text/javascript">
    $(document).ready(function(){
        if(!$(".activityConfigurationCheckBox").is(":checked")){
            $('#dumpMsgInput').attr('disabled','disabled');
            $('#enableLogging').attr('disabled','disabled');
            $('#enablePublishToBAM').attr('disabled','disabled');
        }
        if ($(".activityConfigurationCheckBox").is(":checked") && $('#enablePublishToBAM').is(":checked")) {
            $(".activityConfigurationInput").show();
        }
    });

    var rowNum = 1;



    function enableActivityStreamFieldsForBAM() {
        if ($(".activityConfigurationCheckBox").is(":checked") && $('#enablePublishToBAM').is(":checked")) {
            $(".activityConfigurationInput").show();
            $('#dumpMsgInput').removeAttr('disabled');
        } else {
            $(".activityConfigurationInput").hide();
        }
    }

    function enableActivityStreamFields() {
        if ($(".activityConfigurationCheckBox").is(":checked")) {
            $('#dumpMsgInput').removeAttr('disabled');
            $('#enableLogging').removeAttr('disabled');
            $('#enablePublishToBAM').removeAttr('disabled');
        } else {
            $('#dumpMsgInput').attr('disabled','disabled');
            $('#enableLogging').attr('disabled','disabled');
            $('#enablePublishToBAM').attr('disabled','disabled');
        }
    }
</script>

<div id="middle">
    <h2>
        <fmt:message key="message.tracer.handler.config"/>
    </h2>

    <div id="workArea">
        <div id="result"></div>
        <p>&nbsp;</p>

        <form action="configure_message_tracing.jsp" method="post">
            <input type="hidden" name="setConfig" value="on"/>
            <table width="100%" class="styledLeft" style="margin-left: 0px;">
                <thead>
                <tr>
                    <th colspan="4">
                        <fmt:message key="settings"/>
                    </th>
                </tr>
                </thead>
                <tr>
                    <td colspan="4">
                        <% if (isMessageTracingEnable) { %>
                                <input type="checkbox" name="enableTracing" class="activityConfigurationCheckBox" checked="true" onchange="enableActivityStreamFields()" >&nbsp;&nbsp;&nbsp;&nbsp;
                        <% } else { %>
                                <input type="checkbox" name="enableTracing" class="activityConfigurationCheckBox" onchange="enableActivityStreamFields()" >&nbsp;&nbsp;&nbsp;&nbsp;
                        <% } %>
                        <fmt:message key="enable.message.trace"/>
                    </td>
                </tr>
                <tr>
                    <td colspan="4">
                        <% if (isMsgDumpingEnable) { %>
                                <input type="checkbox" id="dumpMsgInput" name="enableMsgBodyDump" checked="true" >&nbsp;&nbsp;&nbsp;&nbsp;
                        <% } else { %>
                                <input type="checkbox" id="dumpMsgInput" name="enableMsgBodyDump">&nbsp;&nbsp;&nbsp;&nbsp;
                        <% } %>
                        <fmt:message key="enable.dump.message.body"/>
                    </td>
                </tr>
                <tr>
                    <td colspan="4">
                        <% if (isLoggingEnable) { %>
                                <input type="checkbox" id="enableLogging" name="enableLogging" checked="true" >&nbsp;&nbsp;&nbsp;&nbsp;
                        <% } else { %>
                                <input type="checkbox" id="enableLogging" name="enableLogging">&nbsp;&nbsp;&nbsp;&nbsp;
                        <% } %>
                        <fmt:message key="enable.logging"/>
                    </td>
                </tr>
                <tr>
                    <td colspan="4">
                        <% if (isPublishToBAMEnable) { %>
                                <input type="checkbox" id="enablePublishToBAM" name="enablePublishToBAM" onchange="enableActivityStreamFieldsForBAM()" checked="true" >&nbsp;&nbsp;&nbsp;&nbsp;
                        <% } else { %>
                                <input type="checkbox" id="enablePublishToBAM" name="enablePublishToBAM" onchange="enableActivityStreamFieldsForBAM()">&nbsp;&nbsp;&nbsp;&nbsp;
                        <% } %>
                        <fmt:message key="enable.bam.event.publishing"/>
                    </td>
                </tr>
                <tr>
                    <td colspan="4" class="buttonRow">
                        <input type="submit" class="button" value="<fmt:message key="update"/>" id="updateStats"/>&nbsp;&nbsp;
                    </td>
                </tr>
            </table>
        </form>
    </div>
</div>


</fmt:bundle>

