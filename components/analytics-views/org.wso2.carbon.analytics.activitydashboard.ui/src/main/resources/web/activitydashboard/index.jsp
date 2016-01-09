<%--
  ~ Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~ WSO2 Inc. licenses this file to you under the Apache License,
  ~ Version 2.0 (the "License"); you may not use this file except
  ~ in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  --%>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<fmt:bundle basename="org.wso2.carbon.analytics.activitydashboard.ui.i18n.Resources">
    <carbon:breadcrumb
            label="activitydashboard"
            resourceBundle="org.wso2.carbon.analytics.activitydashboard.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>"/>

    <script type="text/javascript" src="js/d3.v3.min.js"></script>
    <script type="text/javascript" src="js/dagre-d3.min.js"></script>
    <script type="text/javascript" src="js/graphlib-dot.min.js"></script>
    <link href="css/activity.css" type="text/css" rel="stylesheet"/>

    <%--<script type="text/javascript" src="js/jquery-ui-sliderAccess.js"></script>--%>

    <%--<style>--%>

        <%--.type-OP {--%>
            <%--background-color: #3d35d5;--%>
        <%--}--%>

        <%--.type-QR {--%>
            <%--background-color: #0a722b;--%>
        <%--}--%>

        <%--.type-EXP {--%>
            <%--background-color: #d50e1b;--%>
        <%--}--%>

        <%--.node g div {--%>
            <%--height: 30px;--%>
            <%--line-height: 30px;--%>
        <%--}--%>

        <%--.name-info {--%>
            <%--white-space: nowrap;--%>
            <%--font-weight: bold;--%>
        <%--}--%>

        <%--.name {--%>
            <%--white-space: nowrap;--%>
        <%--}--%>

        <%--.type {--%>
            <%--height: 30px;--%>
            <%--width: 10px;--%>
            <%--display: block;--%>
            <%--float: left;--%>
            <%--border-top-left-radius: 5px;--%>
            <%--border-bottom-left-radius: 5px;--%>
            <%--margin-right: 4px;--%>
        <%--}--%>

        <%--.infoType {--%>
            <%--background-repeat: no-repeat;--%>
            <%--background-position: 5px 50%;--%>
            <%--width: 25px;--%>
        <%--}--%>

        <%--svg {--%>
            <%--overflow: hidden;--%>
        <%--}--%>

        <%--text {--%>
            <%--font-weight: 300;--%>
            <%--font-family: "Helvetica Neue", Helvetica, Arial, sans-serf;--%>
        <%--}--%>

        <%--.node rect {--%>
            <%--stroke-width: 1px;--%>
            <%--stroke: #707070;--%>
            <%--fill: #F0F0F0;--%>
        <%--}--%>

        <%--.edgeLabel rect {--%>
            <%--fill: #fff;--%>
        <%--}--%>

        <%--.edgePath path {--%>
            <%--stroke: #333;--%>
            <%--stroke-width: 1.5px;--%>
            <%--fill: none;--%>
        <%--}--%>

    <%--</style>--%>

    <script type="text/javascript">

        function createPopupAddExpression(nodeId) {
            var values = {
                nodeId: nodeId
            };
            jQuery.ajax({
                async: false,
                type: 'POST',
                url: 'popup_create_expression_ajaxprocessor.jsp',
                data: values,
                success: function (data) {
                    showCustomPopup(data, 'Add Activities Search Expression', "40%", true, drawSearchQuery, "60%");
                },
                error: function (data) {
                    CARBON.showErrorDialog(data);
                }
            });
        }

        var drawSearchQuery = function () {
            var values = {};
            jQuery.ajax({
                async: false,
                type: 'POST',
                url: 'searchTree_ajaxprocessor.jsp',
                data: values,
                success: function (data) {
                    tryDrawProcessingFlowInfo(data);
                }
            });
        };

        function tryDrawProcessingFlowInfo(commands) {
            var g = new dagreD3.Digraph();
            var renderer = new dagreD3.Renderer();
            eval(commands);
            var svg = d3.select("#flowInfo");
            var svgGroup = d3.select("#flowInfo g");
            renderer.zoom(false);
            var layout = dagreD3.layout()
                    .nodeSep(15)
                    .edgeSep(30)
                    .rankSep(40)
            renderer.layout(layout);
            layout = renderer.run(g, svgGroup);
            svg.attr("width", layout.graph().width).attr("height", layout.graph().height);

        }

        function removeMargins() {
            var nameElements = document.getElementsByName("nameElement");
            for (var i = 0; i < nameElements.length; i++) {
                nameElements[i].style.cssText = "margin-right: 0px; ";
            }
        }

        window.onload = function () {
            drawSearchQuery();
        };

        function showCustomPopup(message, title, windowHight, okButton, callback, windowWidth) {
            var strDialog = "<div id='dialog' title='" + title + "'><div id='popupDialog'></div>" + message + "</div>";
            var requiredWidth = 750;
            if (windowWidth) {
                requiredWidth = windowWidth;
            }
            var func = function () {
                jQuery("#dcontainer").html(strDialog);
                if (okButton) {
                    jQuery("#dialog").dialog({
                        close: function () {
                            jQuery(this).dialog('destroy').remove();
                            jQuery("#dcontainer").empty();
                            return false;
                        },
                        buttons: {
                            "OK": function () {
                                addExpression();
                                jQuery(this).dialog('destroy').remove();
                                jQuery("#dcontainer").empty();
                                if (callback && typeof callback == "function") {
                                    callback();
                                }
                                return false;
                            }
                        },
                        height: windowHight,
                        width: requiredWidth,
                        minHeight: windowHight,
                        minWidth: requiredWidth,
                        modal: true
                    });
                } else {
                    jQuery("#dialog").dialog({
                        close: function () {
                            jQuery(this).dialog('destroy').remove();
                            jQuery("#dcontainer").empty();
                            if (callback && typeof callback == "function") {
                                callback();
                            }
                            return false;
                        },
                        height: windowHight,
                        width: requiredWidth,
                        minHeight: windowHight,
                        minWidth: requiredWidth,
                        modal: true
                    });
                }

                jQuery('.ui-dialog-titlebar-close').click(function () {
                    jQuery('#dialog').dialog("destroy").remove();
                    jQuery("#dcontainer").empty();
                    jQuery("#dcontainer").html('');
                });

            };
            if (!pageLoaded) {
                jQuery(document).ready(func);
            } else {
                func();
            }
        }

        function searchActivities(fromTime, toTime) {
            var values = {
                fromTime: fromTime,
                toTime: toTime
            };
            jQuery.ajax({
                async: false,
                type: 'POST',
                url: 'search_activities_ajaxprocessor.jsp',
                data: values,
                success: function (data) {
                    loadSearchResultPage();
                },
                error: function (data) {
                    CARBON.showErrorDialog("Activity search could not be completed due to an error. Please check your input fields and try again.");
                }
            });
        }

        function loadSearchResultPage() {
            location.href = "activities_list_page.jsp?page=1";
        }

        function clearSearch() {
            var values = {};
            jQuery.ajax({
                async: false,
                type: 'POST',
                url: 'clear_search_ajaxprocessor.jsp',
                data: values,
                success: function (data) {
                    window.location = "index.jsp";
                },
                error: function (data) {
                }
            });
        }
    </script>

    <link href="js/jquery-ui.min.css" type="text/css" rel="stylesheet"/>
    <link href="js/jquery-ui.css" type="text/css" rel="stylesheet"/>
    <script type="text/javascript" src="js/jquery-1.11.2.min.js"></script>
    <script type="text/javascript" src="js/jquery-ui.min.js"></script>
    <script type="text/javascript" src="js/jquery-ui.js"></script>
    <script type="text/javascript" src="js/jquery-ui-timepicker-addon.min.js"></script>
    <script type="text/javascript" src="js/jquery-ui-timepicker-addon.js"></script>
    <link href="js/jquery-ui-timepicker-addon.min.css" type="text/css" rel="stylesheet"/>
    <link href="js/jquery-ui-timepicker-addon.css" type="text/css" rel="stylesheet"/>

    <script type="text/javascript">
        var jQuery_new = $.noConflict(true);
        $(document).ready(function () {
            jQuery_new("#fromTime").datetimepicker();
            jQuery_new("#toTime").datetimepicker();
        });
    </script>

    <div id="middle">
        <h2>Activity Explorer</h2>

        <div id="workArea">
            <div class="sectionSeperator togglebleTitle">Time Search</div>
            <div class="sectionSub">
                <table class="carbonFormTable">
                    <tr>
                        <td class="leftCol-med labelField">
                            From Time:
                        </td>
                        <td>
                            <input type="text" id="fromTime" size="60"/>

                            <div class="sectionHelp">
                                The time from which activities you are interested on.
                            </div>
                        </td>
                    </tr>
                    <tr>
                        <td class="leftCol-med labelField">
                            To Time:
                        </td>
                        <td>
                            <input type="text" id="toTime" size="60"/>

                            <div class="sectionHelp">
                                The time to which activities you are interested on.
                            </div>
                        </td>
                    </tr>
                </table>
                <div class="sectionSeperator togglebleTitle">Advanced Search</div>

                <table class="carbonFormTable">
                    <tr>
                        <td class="leftCol-med labelField">
                            Search Query:
                        </td>
                        <td>
                            <div id="flowdivInfo">
                                <svg id="flowInfo" width=100% height=100%>
                                    <g transform="translate(0,0)scale(0.75)"></g>
                                </svg>
                            </div>
                        </td>
                    </tr>
                </table>

                <div class="buttonRow">
                    <input class="button" type="button" onclick="convertTimeAndSearch();"
                           value="Search"/>
                    <input class="button" type="button" onclick="clearSearch()"
                           value="Clear Search"/>
                </div>
            </div>
        </div>
    </div>

    <script type="text/javascript">
        function convertTimeAndSearch() {
            var fromTime = document.getElementById('fromTime').value;
            var toTime = document.getElementById('toTime').value;
            var fromTimeStamp, toTimeStamp;
            if (fromTime != '') {
                fromTimeStamp = jQuery_new("#fromTime").datetimepicker("getDate").getTime();
            }
            if (toTime != '') {
                toTimeStamp = jQuery_new("#toTime").datetimepicker("getDate").getTime();
            }
            searchActivities(fromTimeStamp, toTimeStamp);
        }
    </script>
</fmt:bundle>
