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
<%@ page import="org.wso2.carbon.analytics.activitydashboard.commons.*" %>
<%@ page import="org.wso2.carbon.analytics.activitydashboard.ui.SearchTreeUIUtil" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%
    if (!"post".equalsIgnoreCase(request.getMethod())) {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        return;
    }

    Object searchTreeObj = request.getSession().getAttribute("SearchExpression");
    String responseStr;
    if (searchTreeObj == null) {
        responseStr = "g.addNode(\"AddExpression\"," +
                " {label: '<div onclick=\"createPopupAddExpression(0);" +
                "\" onmouseover=\"\" style=\"cursor: pointer;\">" +
                "<span name=\"nameElement\" " +
                "class=\"name-info exp-name-info\" >Add Expression</span></div>'});";
        response.getWriter().write(responseStr);
    } else {
        SearchExpressionTree searchExpressionTree = (SearchExpressionTree) searchTreeObj;
        ExpressionNode expressionNode = searchExpressionTree.getRoot();
        try {
            responseStr = SearchTreeUIUtil.getSearchGraghHTML(expressionNode);
            response.getWriter().write(responseStr);
        } catch (InvalidExpressionNodeException e) {
            response.getWriter().write("Error occurred while trying to add the search tree. " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
%>