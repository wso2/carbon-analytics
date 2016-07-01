/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.analytics.activitydashboard.ui;

import org.wso2.carbon.analytics.activitydashboard.commons.ExpressionNode;
import org.wso2.carbon.analytics.activitydashboard.commons.InvalidExpressionNodeException;
import org.wso2.carbon.analytics.activitydashboard.commons.Operation;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This is the util class to create the search expression graph for the current state of expressions
 * tree which has been entered by the user.
 *
 */
public class SearchTreeUIUtil {

    public static String getSearchGraghHTML(ExpressionNode expressionNode) throws InvalidExpressionNodeException {
        Set<String> graphElements = new LinkedHashSet<>();
        searchGraghHTML(expressionNode, graphElements);
        StringBuilder fullHTML = new StringBuilder();
        for (String element : graphElements) {
            fullHTML.append(element);
        }
        return fullHTML.toString();
    }

    private static void searchGraghHTML(ExpressionNode expressionNode, Set<String> graphElements)
            throws InvalidExpressionNodeException {
        if (expressionNode instanceof Operation) {
                graphElements.add("g.addNode(\'" + expressionNode.getId() + "\', " +
                        "{label: \'<div onmouseover=\"\" " +
                        "style=\"cursor: pointer;\"><span name=\"nameElement\" class=\"name-info op-name-info\">"
                        + expressionNode.toString() + "</span></div>\'});\n");

            if (!expressionNode.getId().equals("0")) {
                graphElements.add("g.addEdge(null, \"" + ExpressionNode.getParentId(expressionNode.getId()) + "\", " +
                        "\"" + expressionNode.getId() + "\", {style: 'stroke: #7a0177; stroke-width: 2px;'});\n");
            }
            //It's a operation
            if (expressionNode.getLeftExpression() != null) {
                searchGraghHTML(expressionNode.getLeftExpression(), graphElements);
            } else {
                String leftExpNodeId = ExpressionNode.generateLeftExpressionNodeId(expressionNode.getId());
                graphElements.add("g.addNode(\"AddExpression" + leftExpNodeId + "\", {label: " +
                        "'<div onclick=\"createPopupAddExpression(\\'" + leftExpNodeId + "\\');" +
                        "\" onmouseover=\"\" style=\"cursor: pointer;\"><span name=" +
                        "\"nameElement\" class=\"name-info exp-name-info\">" +
                        "Add Expression</span></div>'});\n");
                if (!leftExpNodeId.equals("0")) {
                    graphElements.add("g.addEdge(null, \"" + expressionNode.getId() + "\", " +
                            "\"AddExpression" + leftExpNodeId + "\", {style: 'stroke: #7a0177; stroke-width: 2px;'});\n");
                }
            }
            if (expressionNode.getRightExpression() != null) {
                searchGraghHTML(expressionNode.getRightExpression(), graphElements);
            } else {
                String rightExpNodeId = ExpressionNode.generateRightExpressionNodeId(expressionNode.getId());
                graphElements.add("g.addNode(\"AddExpression" + rightExpNodeId + "\", {label: " +
                        "'<div onclick=\"createPopupAddExpression(\\'" + rightExpNodeId + "\\');" +
                        "\" onmouseover=\"\" style=\"cursor: pointer;\"><span name=" +
                        "\"nameElement\" class=\"name-info exp-name-info\">" +
                        "Add Expression</span></div>'});\n");
                if (!rightExpNodeId.equals("0")) {
                    graphElements.add("g.addEdge(null, \"" + expressionNode.getId() + "\", " +
                            "\"AddExpression" + rightExpNodeId + "\", {style: 'stroke: #7a0177; stroke-width: 2px;'});\n");
                }
            }
        } else {
            graphElements.add("g.addNode(\'" + expressionNode.getId() + "\', " +
                    "{label: \'<div onmouseover=\"\" " +
                    "style=\"cursor: pointer;\"><span name=\"nameElement\" class=\"name-info qr-name-info\">" + expressionNode.toString() + "</span></div>\'});\n");
            if (!expressionNode.getId().equals("0")) {
                graphElements.add("g.addEdge(null, \"" + ExpressionNode.getParentId(expressionNode.getId()) + "\", " +
                        "\"" + expressionNode.getId() + "\", {style: 'stroke: #7a0177; stroke-width: 2px;'});\n");
            }
        }
    }
}
