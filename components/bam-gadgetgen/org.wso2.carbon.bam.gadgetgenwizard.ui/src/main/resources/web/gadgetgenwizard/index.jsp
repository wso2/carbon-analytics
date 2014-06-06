<!--
~ Copyright WSO2, Inc. (http://wso2.com)
~
~ Licensed under the Apache License, Version 2.0 (the "License");
~ you may not use this file except in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing, software
~ distributed under the License is distributed on an "AS IS" BASIS,
~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
~ See the License for the specific language governing permissions and
~ limitations under the License.
-->

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>

<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>

<script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../admin/js/cookies.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>

<link type="text/css" href="../dialog/js/jqueryui/tabs/ui.all.css" rel="stylesheet"/>
<link type="text/css" href="../gadgetgenwizard/css/jquery.dataTables.css" rel="stylesheet"/>
<link rel="stylesheet" type="text/css" href="../gadgetgenwizard/css/jquery.jqplot.min.css" />


<script type="text/javascript" src="../admin/js/jquery-1.5.2.min.js"></script>
<script type="text/javascript"
        src="../dialog/js/jqueryui/tabs/jquery-ui-1.6.custom.min.js"></script>
<script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery.cookie.js"></script>
<!--[if lt IE 9]><script language="javascript" type="text/javascript" src="../gadgetgenwizard/js/excanvas.min.js"></script><![endif]-->
<script type="text/javascript" src="../gadgetgenwizard/js/validate.js"></script>

<!--link media="all" type="text/css" rel="stylesheet" href="css/registration.css"/-->
<fmt:bundle basename="org.wso2.carbon.bam.gadgetgenwizard.ui.i18n.Resources">
    <carbon:breadcrumb label="main.gadgetgenwizard"
                       resourceBundle="org.wso2.carbon.bam.gadgetgenwizard.ui.i18n.Resources"
                       topPage="false" request="<%=request%>"/>
      <carbon:jsi18n
		resourceBundle="org.wso2.carbon.bam.gadgetgenwizard.ui.i18n.JSResources"
		request="<%=request%>" />

<script type="text/javascript">


    $(document).ready(function () {

        if ($("#page").val() == "1") {
            $("#back").attr("disabled", true);
        }

        // init
        changeHeading(parseInt($("#page").val()));
        sendAjaxRequest("datasource_ajaxprocessor.jsp");

        $("#generate").hide();

        $("#generate").click(function() {
            if (!validate()) {
                CARBON.showErrorDialog(jsi18n["invalid.details"]);
                return;
            }
            $.post("generate_gadget_ajaxprocessor.jsp", $("form").serialize(), function(html) {
                var success = !(html.toLowerCase().match(/error/));
                if (success) {
                    sendAjaxRequest("goto_dashboard_ajaxprocessor.jsp", "gadgetXMLPath=" + html);
                } else {
                    CARBON.showErrorDialog(html);
                }
            });
        });

        $("#back").click(function() {
            var backURL = "";
            if ($("#page").val() == "2") {
                backURL = "datasource_ajaxprocessor.jsp";
            } else if ($("#page").val() == "3") {
                backURL = "sqlinput_ajaxprocessor.jsp";
            } else if ($("#page").val() == "4") {
                backURL = "pickuielement_ajaxprocessor.jsp";
            } else if ($("#page").val() == "5") {
                backURL = "gadget_details_ajaxprocessor.jsp";
            }

            sendAjaxRequest(backURL);

        });

        $("#next").click(function() {
//            var jdbcurl = $("[name=jdbcurl]").val();
//            var username = $("[name=username]").val();
//            var password = $("[name=password]").val();
//            var driver = $("[name=driver]").val();

            if (!validate()) {
                CARBON.showErrorDialog(jsi18n["invalid.details"]);
                return;
            }

            var nextURL = "";
            if ($("#page").val() == "1") {
                $.post("validate_db_conn_ajaxprocessor.jsp", $("form").serialize(), function(html) {
                    var success = !(html.toLowerCase().match(/error/));
                    if (success) {
                        nextURL = "sqlinput_ajaxprocessor.jsp";
                        sendAjaxRequest(nextURL);
                    } else {
                        CARBON.showErrorDialog(jsi18n["no.next.step.invalid.jdbc"] + html);
                    }
                });

            } else if ($("#page").val() == "2") {
                $.post("execute_sql_ajaxprocessor.jsp", $("form").serialize(), function(html) {
                var success = !(html.toLowerCase().match(/error executing query/));

                if (success) {
                    nextURL = "pickuielement_ajaxprocessor.jsp";
                    sendAjaxRequest(nextURL);
                } else {
                    CARBON.showErrorDialog(jsi18n["no.next.step.invalid.sql"] + html);
                }
                })

            } else if ($("#page").val() == "3") {
               nextURL = "gadget_details_ajaxprocessor.jsp";
                sendAjaxRequest(nextURL);
            }



        });

        $("#validate").click(function() {
            if (!validate()) {
                CARBON.showErrorDialog(jsi18n["invalid.details"]);
                return;
            }
            $.post("validate_db_conn_ajaxprocessor.jsp", $("form").serialize(), function(html) {
               var success = !(html.toLowerCase().match(/error executing query/));
                if (success) {
                    CARBON.showInfoDialog(html);
                } else {
                    CARBON.showErrorDialog(html);
                }
            });

        });



        $("#execute-sql").click(function() {
            if (!validate()) {
                CARBON.showErrorDialog("Please enter data in required fields");
                return;
            }
            $.post("execute_sql_ajaxprocessor.jsp", $("form").serialize(), function(html) {
                var success = !(html.toLowerCase().match(/error/));
                function getaoColumns(columnNames) {
                    var json = [];
                    for (var i = 0; i < columnNames.length; i++) {
                        var columnName = columnNames[i];
                        json.push({ sTitle : columnName});
                    }
                    return json;
                }
                if (success) {
                    var respJson = JSON.parse(html);

                    $("#query-results-holder").html("<table id=\"query-results\"></table>");
                    $("#query-results").dataTable({
                        "aaData" : respJson.Rows,
                        "aoColumns" : getaoColumns(respJson.ColumnNames)
                    });
                    $("#query-results-holder").show();
                } else {
                    CARBON.showErrorDialog(html);
                }
            })
        });



        function changeBackBtnState() {
            if ($("#page").val() == "1") {
                $("#back").attr("disabled", true);
            } else {
                $("#back").removeAttr('disabled');
            }
        }


        function changeGenBtnState() {
            if ($("#page").val() == "4") {
                $("#generate").show();
            } else {
                $("#generate").hide();
            }
        }

        function changeNxtBtnState() {
            if (parseInt($("#page").val()) >= 4) {
                $("#next").hide();
            } else {
                $("#next").show();
            }
        }

        function sendAjaxRequest(url, data) {
            if (typeof data == 'undefined' || data == null) {
                data = $("form").serialize();
            }
            //start the ajax
            $.ajax({
                //this is the php file that processes the data and send mail
                url: url,

                //GET method is used
                type: "POST",

                //pass the data
                data: data,

                //Do not cache the page
                cache: false,

                //success
                success: function (html) {
                    //if process.php returned 1/true (send mail success)
                    //hide the form
                    $('#change-area').fadeOut('fast', function() {

                        $('#change-area').html(html);

                        //show the success message
                        $('#change-area').fadeIn('fast');

                        changeHeading(parseInt($("#page").val()));

                        changeBackBtnState();
                        changeGenBtnState();
                        changeNxtBtnState();
                    });
                }
            });
        }



        function changeHeading(pageNo) {
            var wizardPgTitle = [jsi18n["data.source.heading"], jsi18n["sql.query.heading"],
                jsi18n["ui.elements.heading"], jsi18n["gadget.details.heading"], jsi18n["done.heading"]];
            var stepTitle = "Step " + pageNo + " of 5 : ";
            $("#page-title").html(wizardPgTitle[pageNo - 1]);
            $("#step-title").html(stepTitle + wizardPgTitle[pageNo - 1]);

        }


    });
</script>





    <div id="middle">
        <h2>Gadget Generator Wizard</h2>

        <div id="workArea">
            <h3 id="step-title"></h3>

            <form method="post">
                <table class="styledLeft" id="userAdd" width="60%">
                    <thead>
                    <tr>
                        <th id="page-title">Enter Data Source</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td class="formRaw">
                            <div id="change-area">


                            </div>

                        </td>
                    </tr>
                    <tr>
                        <td class="buttonRow">
                            <input type="button" class="button" id="back" value="< <fmt:message key="back.label"/>">
                            <input type="button" class="button" id="next" value="<fmt:message key="next.label"/> >">
                            <input type="button" class="button" id="generate" value="<fmt:message key="generate.label"/>">

                        </td>
                    </tr>
                    </tbody>
                </table>
            </form>
        </div>

    </div>
</fmt:bundle>