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
package org.wso2.carbon.status.dashboard.core.persistence.store.impl.util;

import org.wso2.siddhi.query.api.annotation.Element;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import static org.wso2.carbon.status.dashboard.core.persistence.store.impl.util.RDBMSTableConstants.PLACEHOLDER_CONDITION;
import static org.wso2.carbon.status.dashboard.core.persistence.store.impl.util.RDBMSTableConstants.SQL_WHERE;
import static org.wso2.carbon.status.dashboard.core.persistence.store.impl.util.RDBMSTableConstants.WHITESPACE;

//import org.wso2.carbon.stream.processor.core.persistence.dto.RDBMSQueryConfigurationEntry;


/**
 * Class which holds the utility methods which are used by various units in the RDBMS Event Table implementation.
 */
public class RDBMSTableUtils {

    private RDBMSTableUtils() {
        //preventing initialization
    }

    /**
     * Utility method which can be used to check if a given string instance is null or empty.
     *
     * @param field the string instance to be checked.
     * @return true if the field is null or empty.
     */
    public static boolean isEmpty(String field) {
        return (field == null || field.trim().length() == 0);
    }

    /**
     * Util method which is used to populate a {@link PreparedStatement} instance with a single element.
     *
     * @param stmt    the statement to which the element should be set.
     * @param ordinal the ordinal of the element in the statement (its place in a potential list of places).
     * @param type    the type of the element to be set, adheres to
     *                {@link org.wso2.siddhi.query.api.definition.Attribute.Type}.
     * @param value   the value of the element.
     * @throws SQLException if there are issues when the element is being set.
     */
    public static PreparedStatement populateStatementWithSingleElement(PreparedStatement stmt, int ordinal, String type,
                                                                       Object value) throws SQLException {
        switch (type) {
            case "BOOL":
                stmt.setBoolean(ordinal, (Boolean) value);
                break;
            case "DOUBLE":
                stmt.setDouble(ordinal, (Double) value);
                break;
            case "FLOAT":
                stmt.setFloat(ordinal, (Float) value);
                break;
            case "INT":
                stmt.setInt(ordinal, (Integer) value);
                break;
            case "LONG":
                stmt.setLong(ordinal, (Long) value);
                break;
            case "OBJECT":
                stmt.setObject(ordinal, value);
                break;
            case "VARCHAR":
                stmt.setString(ordinal, (String) value);
                break;
        }
        return stmt;
    }


    /**
     * Util method used to convert a list of elements in an annotation to a comma-separated string.
     *
     * @param elements the list of annotation elements.
     * @return a comma-separated string of all elements in the list.
     */
    public static String flattenAnnotatedElements(List<Element> elements) {
        StringBuilder sb = new StringBuilder();
        elements.forEach(elem -> {
            sb.append(elem.getValue());
            if (elements.indexOf(elem) != elements.size() - 1) {
                sb.append(RDBMSTableConstants.SEPARATOR);
            }
        });
        return sb.toString();
    }


    /**
     * Method for replacing the placeholder for conditions with the SQL Where clause and the actual condition.
     *
     * @param query     the SQL query in string format, with the "{{CONDITION}}" placeholder present.
     * @param condition the actual condition (originating from the ConditionVisitor).
     * @return the formatted string.
     */
    public static String formatQueryWithCondition(String query, String condition) {
        return query.replace(PLACEHOLDER_CONDITION, SQL_WHERE + WHITESPACE + condition);
    }

}
