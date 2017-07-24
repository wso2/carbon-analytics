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

/**
 * This class represents the node in the search tree expressions.
 */
public abstract class ExpressionNode implements Serializable {

    private static final long serialVersionUID = -8570752546981747287L;
    
    private String id;
    private ExpressionNode leftExpression;
    private ExpressionNode rightExpression;
    private static final String REGEX_FOR_ID = "^[1-2]{1}([.][1-2]{1})*$";
    static final String ROOT_EXPRESSION_NODE_ID = "0";
    static final String LEFT_EXPRESSION_NODE_ID = "1";
    static final String RIGHT_EXPRESSION_NODE_ID = "2";

    public ExpressionNode() { }
    
    public ExpressionNode(String id) throws InvalidExpressionNodeException {
        if (validateID(id)) {
            this.id = id.trim();
        } else {
            throw new InvalidExpressionNodeException("Expression node id : " + id +
                    " doesn't match the expected format :" + REGEX_FOR_ID + ".");
        }
    }

    public ExpressionNode getLeftExpression() {
        return leftExpression;
    }

    public void setLeftExpression(ExpressionNode leftExpression) {
        this.leftExpression = leftExpression;
    }

    public ExpressionNode getRightExpression() {
        return rightExpression;
    }

    public void setRightExpression(ExpressionNode rightExpression) {
        this.rightExpression = rightExpression;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public static boolean validateID(String id) {
        return id.equals(ROOT_EXPRESSION_NODE_ID) || id.matches(REGEX_FOR_ID);
    }

    public static String getParentId(String id) throws InvalidExpressionNodeException {
        if (validateID(id)) {
            switch (id) {
                case ROOT_EXPRESSION_NODE_ID:
                    return id;
                case LEFT_EXPRESSION_NODE_ID:
                case RIGHT_EXPRESSION_NODE_ID:
                    return ROOT_EXPRESSION_NODE_ID;
                default:
                    int lastSeperator = id.lastIndexOf(".");
                    return id.substring(0, lastSeperator);
            }
        }
        throw new InvalidExpressionNodeException("Expression node id :" + id + " is not in the expected format!");
    }

    public static String generateLeftExpressionNodeId(String id) {
        if (id.equals(ROOT_EXPRESSION_NODE_ID)) {
            return LEFT_EXPRESSION_NODE_ID;
        } else {
            return id += "." + LEFT_EXPRESSION_NODE_ID;
        }
    }

    public static String generateRightExpressionNodeId(String id) {
        if (id.equals(ROOT_EXPRESSION_NODE_ID)) {
            return RIGHT_EXPRESSION_NODE_ID;
        } else {
            return id += "." + RIGHT_EXPRESSION_NODE_ID;
        }
    }
}
