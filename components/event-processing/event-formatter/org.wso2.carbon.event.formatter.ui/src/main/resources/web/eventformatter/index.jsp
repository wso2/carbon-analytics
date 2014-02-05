<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ page
        import="org.wso2.carbon.event.formatter.stub.EventFormatterAdminServiceStub" %>
<%@ page
        import="org.wso2.carbon.event.formatter.stub.types.EventFormatterConfigurationFileDto" %>
<%@ page import="org.wso2.carbon.event.formatter.stub.types.EventFormatterConfigurationInfoDto" %>
<%@ page import="org.wso2.carbon.event.formatter.ui.EventFormatterUIUtils" %>

<fmt:bundle basename="org.wso2.carbon.event.formatter.ui.i18n.Resources">

    <carbon:breadcrumb
            label="eventformatter.list"
            resourceBundle="org.wso2.carbon.event.formatter.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>

    <script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../admin/js/cookies.js"></script>
    <script type="text/javascript" src="../admin/js/main.js"></script>


    <div id="middle">
        <h2><fmt:message key="title.event.formatter.create"/></h2>

        <%@include file="inner_index.jsp" %>
    </div>
</fmt:bundle>
