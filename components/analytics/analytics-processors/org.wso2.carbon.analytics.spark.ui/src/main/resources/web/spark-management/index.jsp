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
    <link rel="stylesheet" type="text/css" href="css/Ptty.css">
    <link rel="stylesheet" type="text/css" href="css/jquery.dataTables.css">
    <script src="js/jquery-2.1.1.min.js"></script>
    <script src="js/jquery.dataTables.min.js"></script>
    <script src="js/Ptty.jquery.js"></script>

    <div id="middle">
        <h2>Interactive Spark Console</h2>

        <div id="workArea">

            <div id="terminalArea">
                <section>
                    <div id="terminal"></div>
                </section>

                <script>
                    $(document).ready(function () {
                        /* Start Ptty terminal */
                        $('#terminal').Ptty();

                        $.register_command(
                                'define',
                                'Defines the schema of a table',
                                'DEFINE TABLE [table]',
                                '../spark-management/execute_sparkquery_ajaxprocessor.jsp'
                        );

                        $.register_command(
                                'select',
                                'Execute a Spark SELECT query',
                                'SELECT [query]',
                                '../spark-management/execute_sparkquery_ajaxprocessor.jsp'
                        );

                        $.register_command(
                                'insert',
                                'Inserts data into tables',
                                'INSERT INTO [query]',
                                '../spark-management/execute_sparkquery_ajaxprocessor.jsp'
                        );

                        $.register_command(
                                'about',
                                'Interactive Spark SQL shell',
                                'about [no options]',
                                function () {
                                    var about = '<p>This interactive shell lets you execute Spark SQL commands against a local Spark cluster.<br>' +
                                            'Running on Spark v 1.2.1</p>';

                                    return {
                                        type: 'print',
                                        out: about
                                    };
                                }
                        );

                        /* Register Commands and Callbacks*/
                        $.register_command(
                                'date',
                                'returns the current date',
                                'date [no options]',
                                function () {
                                    var now = new Date();
                                    return {
                                        type: 'print',
                                        out: now + ' '
                                    };
                                }
                        );

                        // Typewriter effect callback
                        $.register_callback('typewriter', function (data) {
                            var text_input = $('.cmd_terminal_prompt');
                            text_input.hide();
                            if (typeof data.write === 'string') {
                                // decode special entities.
                                var str = $('<div/>').html(data.write + ' ').text(),
                                        typebox = $('<div></div>').appendTo('.cmd_terminal_content'),
                                        i = 0,
                                        isTag,
                                        text;
                                (function typewriter() {
                                    text = str.slice(0, ++i);
                                    if (text === str) return text_input.show();

                                    typebox.html(text);

                                    var char = text.slice(-1);
                                    if (char === '<') isTag = true;
                                    if (char === '>') isTag = false;

                                    if (isTag) return typewriter();
                                    setTimeout(typewriter, 40);
                                }());
                            }
                        });

                    });
                </script>
            </div>
        </div>
    </div>
</fmt:bundle>
