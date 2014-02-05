
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<fmt:bundle basename="org.wso2.carbon.event.input.adaptor.manager.ui.i18n.Resources">

<carbon:breadcrumb
        label="add"
        resourceBundle="org.wso2.carbon.event.input.adaptor.manager.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>

<script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../admin/js/cookies.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<script type="text/javascript" src="../inputeventadaptormanager/js/create_event_adaptor_helper.js"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>


    <div id="middle">
    <h2><fmt:message key="event.adaptor.create"/></h2>
    <div id="workArea">

<form name="inputForm" action="index.jsp?ordinal=1" method="get" id="addEvent">
<table style="width:100%" id="eventAdd" class="styledLeft">
    <thead>
    <tr>
        <th><fmt:message key="event.adaptor.details"/></th>
    </tr>
    </thead>
    <tbody>
    <tr>
        <td class="formRaw">
            <%@include file="inner_event_adaptor_ui.jsp" %>
        </td>
    </tr>
    <tr>
        <td class="buttonRow">
            <input type="button" value="<fmt:message key="add.event.adaptor"/>"
                   onclick="addEvent(document.getElementById('addEvent'))"/>
        </td>
    </tr>
    </tbody>
</table>


</form>
</div>
</div>
</fmt:bundle>
