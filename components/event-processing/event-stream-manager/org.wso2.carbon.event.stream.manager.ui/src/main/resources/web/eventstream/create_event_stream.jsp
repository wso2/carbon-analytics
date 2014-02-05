<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<fmt:bundle basename="org.wso2.carbon.event.stream.manager.ui.i18n.Resources">

    <carbon:breadcrumb
            label="add"
            resourceBundle="org.wso2.carbon.event.stream.manager.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>

    <link type="text/css" href="css/eventStream.css" rel="stylesheet"/>
    <script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../admin/js/cookies.js"></script>
    <script type="text/javascript" src="../admin/js/main.js"></script>
    <script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
    <script type="text/javascript" src="../yui/build/connection/connection-min.js"></script>
    <script type="text/javascript" src="js/event_stream.js"></script>
    <script type="text/javascript"
            src="js/create_eventStream_helper.js"></script>
    <script type="text/javascript" src="../ajax/js/prototype.js"></script>


    <script type="text/javascript">
        jQuery(document).ready(function () {
            showMappingContext();
        });
    </script>

    <div id="middle">
        <h2><fmt:message key="title.event.stream.create"/></h2>

        <div id="workArea">

            <form name="inputForm" action="index.jsp?ordinal=1" method="get" id="addEventStream">
                <table style="width:100%" id="eventStreamAdd" class="styledLeft">
                    <thead>
                    <tr>
                        <th><fmt:message key="title.event.stream.details"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td class="formRaw">
                            <%@include file="inner_event_stream_ui.jsp" %>
                        </td>
                    </tr>
                    <tr>
                        <td class="buttonRow">
                            <input type="button" value="<fmt:message key="add.event.stream"/>"
                                   onclick="addEventStream(document.getElementById('addEventStream'))"/>
                        </td>
                    </tr>
                    </tbody>
                </table>


            </form>
        </div>
    </div>
</fmt:bundle>
