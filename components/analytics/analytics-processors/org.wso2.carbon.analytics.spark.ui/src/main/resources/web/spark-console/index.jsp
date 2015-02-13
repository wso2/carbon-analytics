<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<!--
~ Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
~ KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<fmt:bundle basename="org.wso2.carbon.analytics.spark.ui.i18n.Resources">
    <carbon:breadcrumb label="spark.conosle.menu"
                       resourceBundle="org.wso2.carbon.analytics.spark.ui.i18n.Resources"
                       topPage="true" request="<%=request%>"/>

    <link type="text/css" href="css/main.css" rel="stylesheet"/>


    <script type="text/javascript">

        $(document).ready(function () {
            console.log("Document loaded...")
        });

    </script>

    <div id="middle">
        <h2>Interactive Spark Console</h2>

        <div id="workArea">
            <!-- <p>Some stuff will go there bae!</p> <br/> -->

            <table class="styledLeft">
                <tbody>
                <tr>
                    <td>
                        <div id="resultsPane">
                                <pre id="pre">

                                Welcome to interactive Spark SQL shell

                                This interactive shell lets you execute Spark SQL commands against a local Spark cluster

                                Initialzing Spark client...

                                </pre>
                        </div>
                    </td>
                </tr>
                </tbody>
            </table>

            <br/><br/>

            <table class="styledLeft">
                <thead>
                <tr>
                    <th>Type your Spark query below</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td>
                        <textarea id="queryPane" rows="5"
                                  style="margin-top:5px;margin-bottom:5px;width:100%"></textarea>
                    </td>
                </tr>
                <tr>
                    <td class="buttonRow">
                        <input type="button" value="Reset" id="btnReset" onclick="resetQueryPane()">
                        <input type="button" value="Execute!" id="btnSubmit"
                               onclick="submitQuery()">
                    </td>
                </tr>
                </tbody>
            </table>


        </div>
    </div>

    <script type="text/javascript">

        function resetQueryPane() {
            document.getElementById("queryPane").value = "";
        }

        function submitQuery() {
            var resultsPane = document.getElementById("resultsPane");
            var query = document.getElementById("queryPane").value;

            jQuery.ajax({

                            url: '../spark-console/execute_spark_ajaxprocessor.jsp',
                            type: 'POST',
                            data: {
                                'query': query
                            },
                            dataType: 'json',
                            success: function (data) {
                                // alert('Data: '+data.response.items.length);

                                var resultDiv = document.createElement("div");
                                resultDiv.className = "response";
                                resultDiv.appendChild(document.createTextNode(data.meta.responseMessage));
                                resultsPane.appendChild(resultDiv);
                                resultsPane.appendChild(document.createElement("br"));

                                resultsPane.scrollTop = resultsPane.scrollHeight;

                                var results = data.response.items;
                                if (!results.isEmpty) {
                                    var columns = data.meta.columns;

                                    var table = document.createElement('table');

                                    //append table headers
                                    var header = document.createElement('tr');
                                    for (var i = 0; i < columns.length; i++) {
                                        var td = document.createElement('td');
                                        var text = document.createTextNode(columns[i]);

                                        td.appendChild(text);
                                        header.appendChild(td);
                                    }
                                    table.appendChild(header);

                                    for (var i = 0; i < results.length; i++) {
                                        var element = results[i];


                                        var tr = document.createElement('tr');

                                        for (var j = 0; j < columns.length; j++) {
                                            var td = document.createElement('td');
                                            var text = document.createTextNode(element[j]);

                                            td.appendChild(text);
                                            tr.appendChild(td);
                                        }
                                        ;
                                        table.appendChild(tr);
                                    }

                                    resultsPane.appendChild(table);
                                    resultsPane.appendChild(document.createElement("br"));


                                    resultsPane.scrollTop = resultsPane.scrollHeight;
                                }
                            },
                            error: function (xhr, error, errorThrown) {

                                var errorDiv = document.createElement("div");
                                var errorMsg = xhr.responseText;
                                var errorText = document.createTextNode("ERROR : " + $(errorMsg)[6].innerText);

//                                $(errorDiv).addClass("error");
                                errorDiv.style.color = "#ff0000";
                                errorDiv.appendChild(errorText);

                                resultsPane.appendChild(errorDiv);
                                resultsPane.appendChild(document.createElement("br"));

                                resultsPane.scrollTop = resultsPane.scrollHeight;
                            }

                        }); //end of ajax

        }


    </script>


</fmt:bundle>
