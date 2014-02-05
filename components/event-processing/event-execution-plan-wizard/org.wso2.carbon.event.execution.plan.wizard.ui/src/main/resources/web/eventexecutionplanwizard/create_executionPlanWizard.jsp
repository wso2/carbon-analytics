<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<fmt:bundle basename="org.wso2.carbon.event.execution.plan.wizard.ui.i18n.Resources">

    <carbon:breadcrumb
            label="event.executionplan.wizard"
            resourceBundle="org.wso2.carbon.event.execution.plan.wizard.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>

    <link type="text/css" href="../eventexecutionplanwizard/css/cep.css" rel="stylesheet"/>
    <script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../admin/js/cookies.js"></script>
    <script type="text/javascript" src="../admin/js/main.js"></script>
    <script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
    <script type="text/javascript" src="../yui/build/connection/connection-min.js"></script>
    <script type="text/javascript" src="../ajax/js/prototype.js"></script>

    <script type="text/javascript">

        function loadUIElements(uiId) {
            var uiElementTd = document.getElementById("uiElement");
            uiElementTd.innerHTML = "";

            jQuery.ajax({
                            type: "POST",
                            url: "../eventexecutionplanwizard/get_ui_ajaxprocessor.jsp?uiId=" + uiId + "",
                            data: {},
                            contentType: "text/html; charset=utf-8",
                            dataType: "text",
                            async:false,
                            success: function (ui_content) {
                                if (ui_content != null) {
                                    jQuery(uiElementTd).html(ui_content);

                                    if (uiId == 'builder') {
                                        var innertTD = document.getElementById('addEventAdaptorTD');
                                        jQuery(innertTD).html('<a onclick=\'loadUIElements("inputAdaptor") \'style=\'background-image:url(images/add.gif);\' class="icon-link" href="#" \'> Add New Input Event Adaptor </a>')
                                    }

                                    else if (uiId == 'formatter') {
                                        var innertTD = document.getElementById('addOutputEventAdaptorTD');
                                        jQuery(innertTD).html('<a onclick=\'loadUIElements("outputAdaptor") \'style=\'background-image:url(images/add.gif);\' class="icon-link" href="#" \'> Add New Out Event Adaptor </a>')
                                    }

                                    else if (uiId == 'processor') {
                                        var innertTD = document.getElementById('addEventStreamTD');
                                        jQuery(innertTD).html('<a onclick=\'loadUIElements("builder") \'style=\'background-image:url(images/add.gif);\' class="icon-link" href="#" \'> Add New Event Stream </a>')
                                    }

                                }
                            }
                        });


        }
    </script>



        <div class="formRaw" id="uiElement">
            <%@include file="create_execution_plan.jsp" %>
        </div>


    <script type="text/javascript">
        var innerTD = document.getElementById('addEventStreamTD');
        jQuery(innerTD).html('<a onclick=\'loadUIElements("builder") \'style=\'background-image:url(images/add.gif);\' class="icon-link" href="#" \'> Add New Event Stream </a>');
    </script>
</fmt:bundle>
