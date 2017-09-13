/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.status.dashboard.core.persistence.store.impl;


import org.wso2.carbon.status.dashboard.core.persistence.store.impl.util.RDBMSTableConstants;
import org.wso2.siddhi.core.table.record.ExpressionVisitor;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.expression.condition.Compare;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;


/**
 * Class which is used by the Siddhi runtime for instructions on converting the SiddhiQL condition to the condition
 * format understood by the underlying RDBMS data store.
 */
public class RDBMSConditionVisitor {

    private StringBuilder condition;
    private String finalCompiledCondition;
    private String tableName;

    private Map<String, Object> placeholders;
    private SortedMap<Integer, Object> parameters;

    private int streamVarCount;
    private int constantCount;

    public RDBMSConditionVisitor(String tableName) {
        this.tableName = tableName;
        this.condition = new StringBuilder();
        this.streamVarCount = 0;
        this.constantCount = 0;
        this.placeholders = new HashMap<>();
        this.parameters = new TreeMap<>();
    }

    private RDBMSConditionVisitor() {
        //preventing initialization
    }

    public String returnCondition() {
        this.parametrizeCondition();
        return this.finalCompiledCondition.trim();
    }

    public SortedMap<Integer, Object> getParameters() {
        return this.parameters;
    }

    
    public void beginVisitAnd() {
        condition.append(RDBMSTableConstants.OPEN_PARENTHESIS);
    }


    public void endVisitAnd() {
        condition.append(RDBMSTableConstants.CLOSE_PARENTHESIS);
    }


    public void beginVisitAndLeftOperand() {
        //Not applicable
    }


    public void endVisitAndLeftOperand() {
        //Not applicable
    }


    public void beginVisitAndRightOperand() {
        condition.append(RDBMSTableConstants.SQL_AND).append(RDBMSTableConstants.WHITESPACE);
    }


    public void endVisitAndRightOperand() {
        //Not applicable
    }


    public void beginVisitOr() {
        condition.append(RDBMSTableConstants.OPEN_PARENTHESIS);
    }


    public void endVisitOr() {
        condition.append(RDBMSTableConstants.CLOSE_PARENTHESIS);
    }


    public void beginVisitOrLeftOperand() {
        //Not applicable
    }


    public void endVisitOrLeftOperand() {
        //Not applicable
    }


    public void beginVisitOrRightOperand() {
        condition.append(RDBMSTableConstants.SQL_OR).append(RDBMSTableConstants.WHITESPACE);
    }


    public void endVisitOrRightOperand() {
        //Not applicable
    }


    public void beginVisitNot() {
        condition.append(RDBMSTableConstants.SQL_NOT).append(RDBMSTableConstants.WHITESPACE);
    }


    public void endVisitNot() {
        //Not applicable
    }


    public void beginVisitCompare(Compare.Operator operator) {
        condition.append(RDBMSTableConstants.OPEN_PARENTHESIS);
    }


    public void endVisitCompare(Compare.Operator operator) {
        condition.append(RDBMSTableConstants.CLOSE_PARENTHESIS);
    }


    public void beginVisitCompareLeftOperand(Compare.Operator operator) {
        //Not applicable
    }


    public void endVisitCompareLeftOperand(Compare.Operator operator) {
        //Not applicable
    }


    public void beginVisitCompareRightOperand(Compare.Operator operator) {
        switch (operator) {
            case EQUAL:
                condition.append(RDBMSTableConstants.SQL_COMPARE_EQUAL);
                break;
            case GREATER_THAN:
                condition.append(RDBMSTableConstants.SQL_COMPARE_GREATER_THAN);
                break;
            case GREATER_THAN_EQUAL:
                condition.append(RDBMSTableConstants.SQL_COMPARE_GREATER_THAN_EQUAL);
                break;
            case LESS_THAN:
                condition.append(RDBMSTableConstants.SQL_COMPARE_LESS_THAN);
                break;
            case LESS_THAN_EQUAL:
                condition.append(RDBMSTableConstants.SQL_COMPARE_LESS_THAN_EQUAL);
                break;
            case NOT_EQUAL:
                condition.append(RDBMSTableConstants.SQL_COMPARE_NOT_EQUAL);
                break;
        }
        condition.append(RDBMSTableConstants.WHITESPACE);
    }


    public void endVisitCompareRightOperand(Compare.Operator operator) {
        //Not applicable
    }


    public void beginVisitIsNull(String streamId) {
        condition.append(RDBMSTableConstants.SQL_IS_NULL).append(RDBMSTableConstants.WHITESPACE);
    }


    public void endVisitIsNull(String streamId) {
        //Not applicable
    }


    public void beginVisitIn(String storeId) {
        condition.append(RDBMSTableConstants.SQL_IN).append(RDBMSTableConstants.WHITESPACE);
    }


    public void endVisitIn(String storeId) {
        //Not applicable
    }


    public void beginVisitConstant(Object value, Attribute.Type type) {
        String name = this.generateConstantName();
        this.placeholders.put(name, new Constant(value, type));
        condition.append("[").append(name).append("]").append(RDBMSTableConstants.WHITESPACE);
    }


    public void endVisitConstant(Object value, Attribute.Type type) {
        //Not applicable
    }


    public void beginVisitMath(MathOperator mathOperator) {
        condition.append(RDBMSTableConstants.OPEN_PARENTHESIS);
    }


    public void endVisitMath(MathOperator mathOperator) {
        condition.append(RDBMSTableConstants.CLOSE_PARENTHESIS);
    }


    public void beginVisitMathLeftOperand(MathOperator mathOperator) {
        //Not applicable
    }


    public void endVisitMathLeftOperand(MathOperator mathOperator) {
        //Not applicable
    }


    public void beginVisitMathRightOperand(ExpressionVisitor.MathOperator mathOperator) {
        switch (mathOperator) {
            case ADD:
                condition.append(RDBMSTableConstants.SQL_MATH_ADD);
                break;
            case DIVIDE:
                condition.append(RDBMSTableConstants.SQL_MATH_DIVIDE);
                break;
            case MOD:
                condition.append(RDBMSTableConstants.SQL_MATH_MOD);
                break;
            case MULTIPLY:
                condition.append(RDBMSTableConstants.SQL_MATH_MULTIPLY);
                break;
            case SUBTRACT:
                condition.append(RDBMSTableConstants.SQL_MATH_SUBTRACT);
                break;
        }
        condition.append(RDBMSTableConstants.WHITESPACE);
    }


    public void endVisitMathRightOperand(MathOperator mathOperator) {
        //Not applicable
    }


    public void beginVisitAttributeFunction(String namespace, String functionName) {
        if (RDBMSTableUtils.isEmpty(namespace)) {
            condition.append(functionName).append(RDBMSTableConstants.OPEN_PARENTHESIS);
        } else {
            throw new RDBMSTableException("The RDBMS Event table does not support function namespaces, but namespace '"
                    + namespace + "' was specified. Please use functions supported by the defined RDBMS data store.");
        }
    }


    public void endVisitAttributeFunction(String namespace, String functionName) {
        if (RDBMSTableUtils.isEmpty(namespace)) {
            condition.append(RDBMSTableConstants.OPEN_PARENTHESIS).append(RDBMSTableConstants.WHITESPACE);
        } else {
            throw new RDBMSTableException("The RDBMS Event table does not support function namespaces, but namespace '"
                    + namespace + "' was specified. Please use functions supported by the defined RDBMS data store.");
        }
    }


    public void beginVisitParameterAttributeFunction(int index) {
        //Not applicable
    }


    public void endVisitParameterAttributeFunction(int index) {
        //Not applicable
    }


    public void beginVisitStreamVariable(String id, String streamId, String attributeName, Attribute.Type type) {
        String name = this.generateStreamVarName();
        this.placeholders.put(name, new Attribute(id, type));
        condition.append("[").append(name).append("]").append(RDBMSTableConstants.WHITESPACE);
    }


    public void endVisitStreamVariable(String id, String streamId, String attributeName, Attribute.Type type) {
        //Not applicable
    }


    public void beginVisitStoreVariable(String storeId, String attributeName, Attribute.Type type) {
        condition.append(this.tableName).append(".").append(attributeName).append(RDBMSTableConstants.WHITESPACE);
    }


    public void endVisitStoreVariable(String storeId, String attributeName, Attribute.Type type) {
        //Not applicable
    }

    /**
     * Util method for walking through the generated condition string and isolating the parameters which will be filled
     * in later as part of building the SQL statement. This method will:
     * (a) eliminate all temporary placeholders and put "?" in their places.
     * (b) build and maintain a sorted map of ordinals and the coresponding parameters which will fit into the above
     * places in the PreparedStatement.
     */
    private void parametrizeCondition() {
        String query = this.condition.toString();
        String[] tokens = query.split("\\[");
        int ordinal = 1;
        for (String token : tokens) {
            if (token.contains("]")) {
                String candidate = token.substring(0, token.indexOf("]"));
                if (this.placeholders.containsKey(candidate)) {
                    this.parameters.put(ordinal, this.placeholders.get(candidate));
                    ordinal++;
                }
            }
        }
        for (String placeholder : this.placeholders.keySet()) {
            query = query.replace("[" + placeholder + "]", "?");
        }
        this.finalCompiledCondition = query;
    }

    /**
     * Method for generating a temporary placeholder for stream variables.
     *
     * @return a placeholder string of known format.
     */
    private String generateStreamVarName() {
        String name = "strVar" + this.streamVarCount;
        this.streamVarCount++;
        return name;
    }

    /**
     * Method for generating a temporary placeholder for constants.
     *
     * @return a placeholder string of known format.
     */
    private String generateConstantName() {
        String name = "const" + this.constantCount;
        this.constantCount++;
        return name;
    }

}
