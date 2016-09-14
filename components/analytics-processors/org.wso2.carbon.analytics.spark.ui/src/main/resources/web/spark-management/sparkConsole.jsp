<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.analytics.spark.ui.client.AnalyticsExecutionClient" %>
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

    <%
        String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

        AnalyticsExecutionClient client = new AnalyticsExecutionClient(cookie, serverURL, configContext);
        try {
            if (client.isAnalyticsExecutionEnabled()) {
    %>
    <div id="middle">
        <h2>Interactive Analytics Console</h2>

        <div id="workArea">

            <div id="terminalArea">
                <section>
                    <div id="terminal"></div>
                </section>

                <script>
                    var sparkVersion = '1.4.1';
                    $(document).ready(function () {
                        /* Start Ptty terminal */
                        $('#terminal').Ptty();

                        $.register_command(
                                'about',
                                'Interactive Spark SQL shell',
                                'ABOUT [no options]',
                                function () {
                                    var about = '<p>This interactive shell lets you execute Spark SQL commands against a local Spark cluster.<br>' +
                                            'Running on Spark v' + sparkVersion + '</p>';

                                    return {
                                        type: 'print',
                                        out: about
                                    };
                                }
                        );

                        $.register_command(
                                'create temporary table',
                                'Creates a table in the underlying data source. Data source can be specified from the corresponding relation provider class. ' +
                                'Relevant provider options should be specified within the \'OPTIONS\' paranthesis. \n' +
                                'For default DAS Data Sources, use \'CarbonAnalytics\' as the provider class. \n' +
                                'Ex: CREATE TEMPORARY TABLE students_table USING CarbonAnalytics OPTIONS (tableName \"students\", schema \"name STRING, marks INTEGER\")',
                                'CREATE TEMPORARY TABLE [table name] USING [CarbonAnalytics | relation provider class] OPTIONS ( [comma separated options] )',
                                '../spark-management/execute_sparkquery_ajaxprocessor.jsp'
                        );

                        $.register_command(
                                'select',
                                'Execute a Spark SQL SELECT query\n' +
                                'Ex: select * from students_table',
                                'SELECT [sql query]',
                                '../spark-management/execute_sparkquery_ajaxprocessor.jsp'
                        );

                        $.register_command(
                                'with',
                                'Execute a Spark SQL with query\n' +
                                'Ex: with q1 as (select * from students_table) select * from q1',
                                'WITH [sql query]',
                                '../spark-management/execute_sparkquery_ajaxprocessor.jsp'
                        );

                        $.register_command(
                                'insert',
                                'Inserts data into tables. These tables needs to be created before inserting.\n' +
                                'Ex: insert into table temp_table select * from students_table',
                                'INSERT [INTO | OVERWRITE] TABLE [table name] [sql query]',
                                '../spark-management/execute_sparkquery_ajaxprocessor.jsp'
                        );

                        $.register_command(
                                'incremental_table_commit',
                                'Commits the incremental table information to the meta table.\n' +
                                'Ex: incremental_table_commit T1',
                                'INCREMENTAL_TABLE_COMMIT [incremental ID1, incremental ID2, ...]',
                                '../spark-management/execute_sparkquery_ajaxprocessor.jsp'
                        );

                        $.register_command(
                                'incremental_table_show',
                                'Shows the incremental meta information of tables.\n' +
                                'Ex: incremental_table_show T1, T2',
                                'INCREMENTAL_TABLE_SHOW [incremental ID1, incremental ID2, ...]',
                                '../spark-management/execute_sparkquery_ajaxprocessor.jsp'
                        );

                        $.register_command(
                                'incremental_table_reset',
                                'Resets the incremental meta information of tables\n' +
                                'Ex: incremental_table_reset T1, T2',
                                'INCREMENTAL_TABLE_RESET [incremental ID1, incremental ID2, ...]',
                                '../spark-management/execute_sparkquery_ajaxprocessor.jsp'
                        );

                        /* Register Commands and Callbacks*/
//                        $.register_command(
//                                'date',
//                                'returns the current date',
//                                'date [no options]',
//                                function () {
//                                    var now = new Date();
//                                    return {
//                                        type: 'print',
//                                        out: now + ' '
//                                    };
//                                }
//                        );

                        // Typewriter effect callback
//                        $.register_callback('typewriter', function (data) {
//                            var text_input = $('.cmd_terminal_prompt');
//                            text_input.hide();
//                            if (typeof data.write === 'string') {
//                                // decode special entities.
//                                var str = $('<div/>').html(data.write + ' ').text(),
//                                        typebox = $('<div></div>').appendTo('.cmd_terminal_content'),
//                                        i = 0,
//                                        isTag,
//                                        text;
//                                (function typewriter() {
//                                    text = str.slice(0, ++i);
//                                    if (text === str) return text_input.show();
//
//                                    typebox.html(text);
//
//                                    var char = text.slice(-1);
//                                    if (char === '<') isTag = true;
//                                    if (char === '>') isTag = false;
//
//                                    if (isTag) return typewriter();
//                                    setTimeout(typewriter, 40);
//                                }());
//                            }
//                        });

                    });
                </script>
            </div>
            <% } else {%>
            Spark query execution is disabled in this node, therefore console is disabled!
            <% }
            } catch (Exception ex) {
            %>
            <script type="application/javascript">
                CARBON.showErrorDialog("Error while checking the analytics execute operation in this node. "
                + <%=ex.getMessage()%>);
            </script>
            <%} %>
        </div>
    </div>
</fmt:bundle>
