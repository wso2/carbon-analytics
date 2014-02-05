<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>

<fmt:bundle basename="org.wso2.carbon.event.processor.ui.i18n.Resources">

    <carbon:breadcrumb
            label="execution.plans.breadcrumb"
            resourceBundle="org.wso2.carbon.event.processor.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>

    <script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../admin/js/cookies.js"></script>
    <script type="text/javascript" src="../admin/js/main.js"></script>
    <script type="text/javascript" src="../eventprocessor/js/execution_plans.js"></script>


    <div id="middle">
        <h2>Available Execution Plans</h2>
        <a href="../eventprocessor/create_execution_plan.jsp"
           style="background-image:url(images/add.gif);"
           class="icon-link">
            Add Execution Plan
        </a>
        <%@include file="../eventprocessor/inner_index.jsp" %>
    </div>
</fmt:bundle>
