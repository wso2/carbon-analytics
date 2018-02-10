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
package org.wso2.carbon.sp.jobmanager.core.dbhandler.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.sp.jobmanager.core.dbhandler.QueryManager;
import org.wso2.carbon.sp.jobmanager.core.exception.RDBMSTableException;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.wso2.carbon.sp.jobmanager.core.dbhandler.utils.SQLConstants.PLACEHOLDER_CONDITION;
import static org.wso2.carbon.sp.jobmanager.core.dbhandler.utils.SQLConstants.PLACEHOLDER_Q;
import static org.wso2.carbon.sp.jobmanager.core.dbhandler.utils.SQLConstants.QUESTION_MARK;
import static org.wso2.carbon.sp.jobmanager.core.dbhandler.utils.SQLConstants.SEPARATOR;
import static org.wso2.carbon.sp.jobmanager.core.dbhandler.utils.SQLConstants.SQL_WHERE;
import static org.wso2.carbon.sp.jobmanager.core.dbhandler.utils.SQLConstants.WHITESPACE;

/**
 * Class which holds the utility methods which are used by various units in the RDBMS Event Table implementation.
 */
public class DBTableUtils {
    private static final Logger logger = LoggerFactory.getLogger(DBTableUtils.class);
    private static DBTableUtils instance = new DBTableUtils();

    private DBTableUtils() {
    }

    public static DBTableUtils getInstance() {
        return instance;
    }

    public Map<String, String> loadManagerConfigTableTuples(QueryManager statusDashboardQueryManager) {
        String intType = statusDashboardQueryManager.getQuery("integerType");
        String stringType = statusDashboardQueryManager.getQuery("stringType");
        Map<String, String> attributeSelection = new HashMap<>();
        attributeSelection.put("MANAGERID", stringType);
        attributeSelection.put("HOST", stringType);
        attributeSelection.put("PORT", intType);
        // attributeSelection.put("dummy",stringType);

        logger.info("first map" + attributeSelection.toString());
        return attributeSelection;
    }

    /**
     * Utility method which can be used to check if a given string instance is null or empty.
     *
     * @param field the string instance to be checked.
     * @return true if the field is null or empty.
     */
    public boolean isEmpty(String field) {
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
    private PreparedStatement populateStatementWithSingleElement(PreparedStatement stmt, int ordinal, String type,
                                                                 Object value,
                                                                 QueryManager statusDashboardQueryManager) throws
                                                                                                           SQLException {
        String doubleType = statusDashboardQueryManager.getQuery("doubleType");
        String longType = statusDashboardQueryManager.getQuery("longType");
        String stringType = statusDashboardQueryManager.getQuery("stringType");
        String integerType = statusDashboardQueryManager.getQuery("integerType");
        String floatType = statusDashboardQueryManager.getQuery("floatType");
        String booleanType = statusDashboardQueryManager.getQuery("booleanType");
        if (doubleType.equalsIgnoreCase(type)) {
            stmt.setDouble(ordinal, (Double) value);
        } else if (stringType.equalsIgnoreCase(type)) {
            stmt.setString(ordinal, (String) value);
        } else if (longType.equalsIgnoreCase(type)) {
            stmt.setLong(ordinal, (Long) value);
        } else if (integerType.equalsIgnoreCase(type)) {
            stmt.setInt(ordinal, (Integer) value);
        } else if (floatType.equalsIgnoreCase(type)) {
            stmt.setFloat(ordinal, (Float) value);
        } else if (booleanType.equalsIgnoreCase(type)) {
            stmt.setBoolean(ordinal, (Boolean) value);
        } else {
            logger.error("Invalid Type of Object ");
        }
        return stmt;
    }

    /**
     * Method for replacing the placeholder for conditions with the SQL Where clause and the actual condition.
     *
     * @param query     the SQL query in string format, with the "{{CONDITION}}" placeholder present.
     * @param condition the actual condition (originating from the ConditionVisitor).
     * @return the formatted string.
     */
    public String formatQueryWithCondition(String query, String condition) {
        return query.replace(PLACEHOLDER_CONDITION, SQL_WHERE + WHITESPACE + condition);
    }

    /**
     * Identify the db type from jdbc metadata.
     *
     * @param connection jdbc connection.
     * @return database type name.
     * @throws RuntimeException
     */
    public String getDBType(Connection connection) {
        try {
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            return databaseMetaData.getDatabaseProductName();
        } catch (SQLException e) {
            throw new RuntimeException("Error occurred while getting the rdbms database type from the meta data.");
        }
    }

    /**
     * Method for populating values to a pre-created SQL prepared statement.
     *
     * @param record the record whose values should be populated.
     * @param stmt   the statement to which the values should be set.
     */
    public PreparedStatement populateInsertStatement(Map<String, Object> record, PreparedStatement stmt, Map<String,
            String>
            attributesTypeMap, QueryManager statusDashboardQueryManager) {
        PreparedStatement populatedStatement = stmt;
        int possition = 0;
        logger.info("mapping" + attributesTypeMap.toString());
        logger.info("PORT" + attributesTypeMap.get("PORT"));
        logger.info("HOST" + attributesTypeMap.get("HOST"));
        for (Map.Entry<String, String> attributeEntry : attributesTypeMap.entrySet()) {
            Object value = record.get(attributeEntry.getKey());
            logger.info("objects" + value.toString());

            try {
                //changed position+1 to position
                populatedStatement = instance.populateStatementWithSingleElement(stmt, possition + 1,
                                                                                 attributeEntry.getValue(), value,
                                                                                 statusDashboardQueryManager);
            } catch (SQLException e) {
                throw new RDBMSTableException("Dropping event since value for Attribute name " +
                                                      attributeEntry.getKey() + "cannot be set: " + e.getMessage(), e);
            }
            possition++;
        }
        return populatedStatement;
    }

    /**
     * Fletch data from the result set.
     *
     * @param rs            result set.
     * @param attributeType Attribute that need to extract.
     * @return result
     * @throws SQLException
     */
    public Object fetchData(ResultSet rs, String attributeName, String attributeType,
                            QueryManager metricsQueryManager) throws SQLException {
        String doubleType = metricsQueryManager.getQuery("doubleType");
        String longType = metricsQueryManager.getQuery("longType");
        String stringType = metricsQueryManager.getQuery("stringType");
        String integerType = metricsQueryManager.getQuery("integerType");
        String floatType = metricsQueryManager.getQuery("floatType");
        String booleanType = metricsQueryManager.getQuery("booleanType");
        if (doubleType.equalsIgnoreCase(attributeType)) {
            return rs.getDouble(attributeName);
        } else if (stringType.equalsIgnoreCase(attributeType)) {
            return rs.getString(attributeName);
        } else if (longType.equalsIgnoreCase(attributeType)) {
            return rs.getLong(attributeName);
        } else if (integerType.equalsIgnoreCase(attributeType)) {
            return rs.getInt(attributeName);
        } else if (floatType.equalsIgnoreCase(attributeType)) {
            return rs.getFloat(attributeName);
        } else if (booleanType.equalsIgnoreCase(attributeType)) {
            return rs.getBoolean(attributeName);
        } else {
            logger.error("Invalid Type of Object " + attributeName + ":" + attributeType);
        }
        return null;
    }

    /**
     * Method for composing the SQL query for INSERT operations with proper placeholders.
     *
     * @return the composed SQL query in string form.
     */
    public String composeInsertQuery(String insertQuery, int attributesSize) {
        StringBuilder params = new StringBuilder();
        int fieldsLeft = attributesSize;
        while (fieldsLeft > 0) {
            params.append(QUESTION_MARK);
            if (fieldsLeft > 1) {
                params.append(SEPARATOR);
            }
            fieldsLeft = fieldsLeft - 1;
        }
        return insertQuery.replace(PLACEHOLDER_Q, params.toString());
    }

}
