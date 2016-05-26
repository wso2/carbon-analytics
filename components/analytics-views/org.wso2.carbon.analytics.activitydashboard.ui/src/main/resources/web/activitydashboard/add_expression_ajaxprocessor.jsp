<%--
~ Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~    http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied.  See the License for the
~ specific language governing permissions and limitations
~ under the License.
--%>
<%@ page import="org.wso2.carbon.analytics.activitydashboard.commons.*" %>

<%
    if (!"post".equalsIgnoreCase(request.getMethod())) {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        return;
    }

    boolean isQueryExpression = Boolean.parseBoolean(request.getParameter("isQueryType"));
    Object searchExpressionObj = request.getSession().getAttribute("SearchExpression");
    String expressionNodeId = request.getParameter("nodeId");
    SearchExpressionTree searchExpressionTree;
    if (searchExpressionObj == null) {
        searchExpressionTree = new SearchExpressionTree();
    } else {
        searchExpressionTree = (SearchExpressionTree) searchExpressionObj;
    }
    try {
        if (isQueryExpression) {
            String tableName = request.getParameter("tableName");
            String searchQuery = request.getParameter("searchQuery");
            Query query = new Query(expressionNodeId, tableName, searchQuery);
            searchExpressionTree.putExpressionNode(query);
        } else {
            String operationType = request.getParameter("operationType");
            Operation.Operator operator;
            if (operationType.equalsIgnoreCase(Operation.Operator.AND.toString())) {
                operator = Operation.Operator.AND;
            } else {
                operator = Operation.Operator.OR;
            }
            Operation operation = new Operation(expressionNodeId, operator);
            searchExpressionTree.putExpressionNode(operation);
        }
        request.getSession().setAttribute("SearchExpression", searchExpressionTree);
    } catch (InvalidExpressionNodeException e) {
        response.getWriter().print(e.getMessage());
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
%>