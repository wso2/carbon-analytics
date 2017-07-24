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
 * This class represents the Operation of an search expression.
 */
public class Operation extends ExpressionNode implements Serializable {

    private static final long serialVersionUID = -1120171493752423626L;
    
    private Operator operator;

    public Operation() { }
    
    public Operation(String id, Operator operator, ExpressionNode leftExpression,
                     ExpressionNode rightExpression) throws InvalidExpressionNodeException {
        super(id);
        this.operator = operator;
        setLeftExpression(leftExpression);
        setRightExpression(rightExpression);
    }

    public Operation(String id, Operator operator) throws InvalidExpressionNodeException {
        super(id);
        this.operator = operator;
    }

    public Operator getOperator() {
        return operator;
    }

    public enum Operator {
        AND, OR
    }

    public String toString() {
        return this.operator.toString();
    }
}
