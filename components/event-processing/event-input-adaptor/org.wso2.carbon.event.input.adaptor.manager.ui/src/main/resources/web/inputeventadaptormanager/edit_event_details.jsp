<%@ page import="org.wso2.carbon.event.input.adaptor.manager.ui.InputEventAdaptorUIUtils" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page
        import="org.wso2.carbon.event.input.adaptor.manager.stub.InputEventAdaptorManagerAdminServiceStub" %>


<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>


<fmt:bundle basename="org.wso2.carbon.event.input.adaptor.manager.ui.i18n.Resources">

<carbon:breadcrumb
        label="edit"
        resourceBundle="org.wso2.carbon.event.input.adaptor.manager.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>

<script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../admin/js/cookies.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>

<%--newly added--%>

<script type="text/javascript" src="global-params.js"></script>
<%--<script type="text/javascript" src="configcommon.js"></script>--%>

<script src="../editarea/edit_area_full.js" type="text/javascript"></script>

<link type="text/css" href="../dialog/js/jqueryui/tabs/ui.all.css" rel="stylesheet"/>
<script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery-1.2.6.min.js"></script>
<script type="text/javascript"
        src="../dialog/js/jqueryui/tabs/jquery-ui-1.6.custom.min.js"></script>
<script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery.cookie.js"></script>

<!--Yahoo includes for dom event handling-->
<script src="../yui/build/yahoo-dom-event/yahoo-dom-event.js" type="text/javascript"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>

<%--end of newly added--%>


<%
    String status = request.getParameter("status");
    ResourceBundle bundle = ResourceBundle.getBundle(
            "org.wso2.carbon.event.input.adaptor.manager.ui.i18n.Resources", request.getLocale());

    if ("updated".equals(status)) {
%>
<script type="text/javascript">
    jQuery(document).ready(function () {
        CARBON.showInfoDialog('<%=bundle.getString("activated.configuration")%>');
    });
</script>
<%

    }
%>


<%
    String eventName = request.getParameter("eventName");
    String eventPath = request.getParameter("eventPath");

    String inputEventAdaptorFile = "";
    if (eventName != null) {
        InputEventAdaptorManagerAdminServiceStub stub = InputEventAdaptorUIUtils.getInputEventManagerAdminService(config, session, request);
        inputEventAdaptorFile = stub.getActiveInputEventAdaptorConfigurationContent(eventName);

    } else if (eventPath != null) {
        InputEventAdaptorManagerAdminServiceStub stub = InputEventAdaptorUIUtils.getInputEventManagerAdminService(config, session, request);
        inputEventAdaptorFile = stub.getInactiveInputEventAdaptorConfigurationContent(eventPath);

    }

    Boolean loadEditArea = true;
    String eventAdaptorFileConfiguration = inputEventAdaptorFile;

%>

<% if (loadEditArea) { %>
<script type="text/javascript">
    editAreaLoader.init({
                            id:"rawConfig"        // text area id
                            , syntax:"xml"            // syntax to be uses for highlighting
                            , start_highlight:true  // to display with highlight mode on start-up
                        });
</script>
<% } %>

<script type="text/javascript">
    function updateConfiguration(form, eventName) {
        var newEventAdaptorConfiguration = ""

        if (document.getElementById("rawConfig") != null) {
            newEventAdaptorConfiguration = editAreaLoader.getValue("rawConfig");
        }

        new Ajax.Request('edit_event_ajaxprocessor.jsp', {
            method: 'post',
            asynchronous: false,
            parameters: {eventName: eventName, eventConfiguration: newEventAdaptorConfiguration},
            onSuccess: function (event) {
                if ("true" == event.responseText.trim()) {
                    form.submit();
                } else {
                    if(event.responseText.trim().indexOf("The input stream for an incoming message is null") != -1){
                        CARBON.showErrorDialog("Possible session time out, redirecting to index page",function(){
                            window.location.href = "../admin/index.jsp?ordinal=1";
                        });
                    }else{
                        CARBON.showErrorDialog("Failed to update event adaptor, Exception: " + event.responseText.trim());
                    }
                }
            }
        })
    }

    function updateNotDeployedConfiguration(form, eventPath) {
        var newEventAdaptorConfiguration = ""

        if (document.getElementById("rawConfig") != null) {
            newEventAdaptorConfiguration = editAreaLoader.getValue("rawConfig");
        }


        new Ajax.Request('edit_event_ajaxprocessor.jsp', {
            method: 'post',
            asynchronous: false,
            parameters: {eventPath: eventPath, eventConfiguration: newEventAdaptorConfiguration},
            onSuccess: function (event) {
                if ("true" == event.responseText.trim()) {
                    form.submit();
                } else {
                    if(event.responseText.trim().indexOf("The input stream for an incoming message is null") != -1){
                        CARBON.showErrorDialog("Possible session time out, redirecting to index page",function(){
                            window.location.href = "../admin/index.jsp?ordinal=1";
                        });
                    }else{
                        CARBON.showErrorDialog("Failed to update event adaptor, Exception: " + event.responseText.trim());
                    }
                }
            }
        })

    }


    function resetConfiguration(form) {

        CARBON.showConfirmationDialog(
                "Are you sure you want to reset?", function () {
                    editAreaLoader.setValue("rawConfig", document.getElementById("rawConfig").value.trim());
                });

    }

</script>

<div id="middle">
    <h2><fmt:message key="edit.event.adaptor.configuration"/></h2>

    <div id="workArea">
        <form name="configform" id="configform" action="index.jsp?ordinal=1" method="get">
            <div id="saveConfiguration">
                            <span style="margin-top:10px;margin-bottom:10px; display:block;_margin-top:0px;">
                                <fmt:message key="save.advice"/>
                            </span>
            </div>
            <table class="styledLeft" style="width:100%">
                <thead>
                <tr>
                    <th>
                        <fmt:message key="event.adaptor.configuration"/>
                    </th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td class="formRow">
                        <table class="normal" style="width:100%">
                            <tr>
                                <td id="rawConfigTD">
                                    <textarea name="rawConfig" id="rawConfig"
                                              style="border:solid 1px #cccccc; width: 99%; height: 400px; margin-top:5px;"><%=eventAdaptorFileConfiguration%>
                                    </textarea>

                                    <% if (!loadEditArea) { %>
                                    <div style="padding:10px;color:#444;">
                                        <fmt:message key="syntax.disabled"/>
                                    </div>
                                    <% } %>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
                <tr>
                    <td class="buttonRow">
                        <%
                            if (eventName != null) {
                        %>

                        <button class="button"
                                onclick="updateConfiguration(document.getElementById('configform'),'<%=eventName%>'); return false;">
                            <fmt:message
                                    key="update"/></button>

                        <%
                        } else if (eventPath != null) {
                        %>
                        <button class="button"
                                onclick="updateNotDeployedConfiguration(document.getElementById('configform'),'<%=eventPath%>'); return false;">
                            <fmt:message
                                    key="update"/></button>

                        <%
                            }
                        %>
                        <button class="button"
                                onclick="resetConfiguration(document.getElementById('configform')); return false;">
                            <fmt:message
                                    key="reset"/></button>
                    </td>
                </tr>
                </tbody>

            </table>

        </form>

    </div>
</div>
</fmt:bundle>