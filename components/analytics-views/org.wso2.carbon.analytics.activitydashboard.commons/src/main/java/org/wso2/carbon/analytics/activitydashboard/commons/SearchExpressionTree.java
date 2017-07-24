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
package org.wso2.carbon.analytics.activitydashboard.commons;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

/**
 * This class represents the entire search expression tree structure.
 */
public class SearchExpressionTree implements Serializable {

    private static final long serialVersionUID = -5717170446779888253L;
    
    private ExpressionNode root;
    
    public SearchExpressionTree() { }

    public void setRoot(ExpressionNode root) {
        this.root = root;
    }

    public ExpressionNode getRoot() {
        return root;
    }

    public ExpressionNode getExpression(String id) throws InvalidExpressionNodeException {
        if (id != null) {
            if (ExpressionNode.validateID(id)) {
                if (id.equals(ExpressionNode.ROOT_EXPRESSION_NODE_ID)) {
                    return root;
                }
                String[] idElements = id.split("\\.");
                ExpressionNode expressionNode = root;
                for (String idElement : idElements) {
                    if (idElement.trim().equals("1")) {
                        expressionNode = expressionNode.getLeftExpression();
                    } else {
                        expressionNode = expressionNode.getRightExpression();
                    }
                }
                return expressionNode;
            } else {
                throw new InvalidExpressionNodeException("Expression node id :"
                        + id + " is not in the expected format!");
            }
        }
        return null;
    }

    public void putExpressionNode(ExpressionNode expressionNode) throws InvalidExpressionNodeException {
        String expressionNodeId = expressionNode.getId();
        switch (expressionNodeId) {
            case ExpressionNode.ROOT_EXPRESSION_NODE_ID:
                setRoot(expressionNode);
                break;
            case ExpressionNode.LEFT_EXPRESSION_NODE_ID:
                root.setLeftExpression(expressionNode);
                break;
            case ExpressionNode.RIGHT_EXPRESSION_NODE_ID:
                root.setRightExpression(expressionNode);
                break;
            default:
                String parentNodeId = ExpressionNode.getParentId(expressionNodeId);
                ExpressionNode parentNode = getExpression(parentNodeId);
                int lastSeparatorIndex = expressionNodeId.lastIndexOf(".");
                String nodeId = expressionNodeId.substring(lastSeparatorIndex + 1, expressionNodeId.length());
                if (nodeId.equals(ExpressionNode.LEFT_EXPRESSION_NODE_ID)) {
                    parentNode.setLeftExpression(expressionNode);
                } else {
                    parentNode.setRightExpression(expressionNode);
                }
                break;
        }
    }

    public boolean isCompleted() {
        return checkExpressionNodeCompleted(root);
    }

    private boolean checkExpressionNodeCompleted(ExpressionNode expressionNode) {
        if (expressionNode instanceof Operation) {
            //It's a operation
            if (expressionNode.getLeftExpression() != null) {
                if (!checkExpressionNodeCompleted(expressionNode.getLeftExpression())) {
                    return false;
                }
            } else {
                return false;
            }
            if (expressionNode.getRightExpression() != null) {
                return checkExpressionNodeCompleted(expressionNode.getRightExpression());
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    public String[] getUniqueTableNameInvolved() {
        if (root != null) {
            Set<String> tableNames = new TreeSet<>();
            filterTableNames(root, tableNames);
            return tableNames.toArray(new String[tableNames.size()]);
        }else {
            return new String[0];
        }
    }

    private void filterTableNames(ExpressionNode expressionNode, Set<String> tableNames) {
        if (expressionNode instanceof Operation) {
            //It's a operation
            if (expressionNode.getLeftExpression() != null) {
                filterTableNames(expressionNode.getLeftExpression(), tableNames);
            }
            if (expressionNode.getRightExpression() != null) {
                filterTableNames(expressionNode.getRightExpression(), tableNames);
            }
        } else {
            Query query = (Query) expressionNode;
            tableNames.add(query.getTableName().toUpperCase());
        }
    }
}
