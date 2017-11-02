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
package org.wso2.carbon.status.dashboard.core.dbhandler.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.status.dashboard.core.dbhandler.exceptions.RDBMSTableException;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.wso2.carbon.status.dashboard.core.dbhandler.utils.SQLConstants.PLACEHOLDER_CONDITION;
import static org.wso2.carbon.status.dashboard.core.dbhandler.utils.SQLConstants.PLACEHOLDER_Q;
import static org.wso2.carbon.status.dashboard.core.dbhandler.utils.SQLConstants.QUESTION_MARK;
import static org.wso2.carbon.status.dashboard.core.dbhandler.utils.SQLConstants.SEPARATOR;
import static org.wso2.carbon.status.dashboard.core.dbhandler.utils.SQLConstants.SQL_WHERE;
import static org.wso2.carbon.status.dashboard.core.dbhandler.utils.SQLConstants.WHITESPACE;


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

    public Map<String, Map<String, String>> loadWorkerAttributeTypeMap() {
        String integerType = QueryManager.getInstance().getTypeMap("integerType");
        String longType = QueryManager.getInstance().getTypeMap("longType");
        String stringType = QueryManager.getInstance().getTypeMap("stringType");
        Map<String, Map<String, String>> attributesTypeMaps = new HashMap<>();
        Map<String, String> attributesWorkerConfigTable = new LinkedHashMap<>();
        attributesWorkerConfigTable.put("WORKERID", stringType);
        attributesWorkerConfigTable.put("HOST", stringType);
        attributesWorkerConfigTable.put("PORT", integerType);
        Map<String, String> attributesWorkerDetailsTable = new LinkedHashMap<>();
        attributesWorkerDetailsTable.put("CARBONID", stringType);
        attributesWorkerDetailsTable.put("WORKERID", stringType);
        attributesWorkerDetailsTable.put("JAVARUNTIMENAME", stringType);
        attributesWorkerDetailsTable.put("JAVAVMVERSION", stringType);
        attributesWorkerDetailsTable.put("JAVAVMVENDOR", stringType);
        attributesWorkerDetailsTable.put("JAVAHOME", stringType);
        attributesWorkerDetailsTable.put("JAVAVERSION", stringType);
        attributesWorkerDetailsTable.put("OSNAME", stringType);
        attributesWorkerDetailsTable.put("OSVERSION", stringType);
        attributesWorkerDetailsTable.put("USERHOME", stringType);
        attributesWorkerDetailsTable.put("USERTIMEZONE", stringType);
        attributesWorkerDetailsTable.put("USERNAME", stringType);
        attributesWorkerDetailsTable.put("USERCOUNTRY", stringType);
        attributesWorkerDetailsTable.put("REPOLOCATION", stringType);
        attributesWorkerDetailsTable.put("SERVERSTARTTIME", longType);
        attributesWorkerDetailsTable.put("LASTSNAPSHOTTIME", longType);
        attributesTypeMaps.put("WORKERS_CONFIGURATION", attributesWorkerConfigTable);
        attributesTypeMaps.put("WORKERS_DETAILS", attributesWorkerDetailsTable);
        return attributesTypeMaps;
    }

    public Map<String, String> loadMetricsValueSelection() {
        Map<String, String> attributeSelection = new HashMap<>();
        attributeSelection.put("METRIC_COUNTER", "NAME,TIMESTAMP,COUNT");
        attributeSelection.put("METRIC_GAUGE", "NAME,TIMESTAMP,VALUE");
        attributeSelection.put("METRIC_HISTOGRAM", "NAME,TIMESTAMP,COUNT,MAX,MEAN,MIN,STDDEV,P75,P95,P99,P999");
        attributeSelection.put("METRIC_METER", "NAME,TIMESTAMP,COUNT,MEAN_RATE,M1_RATE,M5_RATE,M15_RATE");
        attributeSelection.put("METRIC_TIMER", "NAME,TIMESTAMP,COUNT,MAX,MEAN,MIN,STDDEV,P75,P95,P99,P999,MEAN_RATE," +
                "M1_RATE,M5_RATE,M15_RATE");
        return attributeSelection;
    }

    public Map<String, Map<String, String>> loadMetricsAttributeTypeMap() {
        String doubleType = QueryManager.getInstance().getTypeMap("doubleType");
        String longType = QueryManager.getInstance().getTypeMap("longType");
        String stringType = QueryManager.getInstance().getTypeMap("stringType");
        Map<String, String> attributesCounterTable = new HashMap<>();
        attributesCounterTable.put("ID", longType);
        attributesCounterTable.put("SOURCE", stringType);
        attributesCounterTable.put("TIMESTAMP", longType);
        attributesCounterTable.put("NAME", stringType);
        attributesCounterTable.put("COUNT", longType);
        Map<String, String> attributesGaugeTable = new HashMap<>();
        attributesGaugeTable.put("ID", longType);
        attributesGaugeTable.put("SOURCE", stringType);
        attributesGaugeTable.put("TIMESTAMP", longType);
        attributesGaugeTable.put("NAME", stringType);
        attributesGaugeTable.put("VALUE", stringType);
        Map<String, String> attributesHistogramTable = new HashMap<>();
        attributesHistogramTable.put("ID", longType);
        attributesHistogramTable.put("SOURCE", stringType);
        attributesHistogramTable.put("TIMESTAMP", longType);
        attributesHistogramTable.put("NAME", stringType);
        attributesHistogramTable.put("COUNT", longType);
        attributesHistogramTable.put("MAX", doubleType);
        attributesHistogramTable.put("MEAN", doubleType);
        attributesHistogramTable.put("MIN", doubleType);
        attributesHistogramTable.put("STDDEV", doubleType);
        attributesHistogramTable.put("P50", doubleType);
        attributesHistogramTable.put("P75", doubleType);
        attributesHistogramTable.put("P95", doubleType);
        attributesHistogramTable.put("P98", doubleType);
        attributesHistogramTable.put("P99", doubleType);
        attributesHistogramTable.put("P999", doubleType);
        Map<String, String> attributesMeterTable = new HashMap<>();
        attributesMeterTable.put("ID", longType);
        attributesMeterTable.put("SOURCE", stringType);
        attributesMeterTable.put("TIMESTAMP", longType);
        attributesMeterTable.put("NAME", stringType);
        attributesMeterTable.put("COUNT", longType);
        attributesMeterTable.put("MEAN_RATE", doubleType);
        attributesMeterTable.put("M1_RATE", doubleType);
        attributesMeterTable.put("M5_RATE", doubleType);
        attributesMeterTable.put("M15_RATE", doubleType);
        attributesMeterTable.put("RATE_UNIT", stringType);
        Map<String, String> attributesTimerTable = new HashMap<>();
        attributesTimerTable.put("ID", longType);
        attributesTimerTable.put("SOURCE", stringType);
        attributesTimerTable.put("TIMESTAMP", longType);
        attributesTimerTable.put("NAME", stringType);
        attributesTimerTable.put("COUNT", longType);
        attributesTimerTable.put("MAX", doubleType);
        attributesTimerTable.put("MEAN", doubleType);
        attributesTimerTable.put("MIN", doubleType);
        attributesTimerTable.put("STDDEV", doubleType);
        attributesTimerTable.put("P50", doubleType);
        attributesTimerTable.put("P75", doubleType);
        attributesTimerTable.put("P95", doubleType);
        attributesTimerTable.put("P98", doubleType);
        attributesTimerTable.put("P99", doubleType);
        attributesTimerTable.put("P999", doubleType);
        attributesTimerTable.put("MEAN_RATE", doubleType);
        attributesTimerTable.put("M1_RATE", doubleType);
        attributesTimerTable.put("M5_RATE", doubleType);
        attributesTimerTable.put("M15_RATE", doubleType);
        attributesTimerTable.put("RATE_UNIT", stringType);
        attributesTimerTable.put("DURATION_UNIT", stringType);

        Map<String, Map<String, String>> attributesTypeMaps = new HashMap<>();
        attributesTypeMaps.put("METRIC_COUNTER", attributesCounterTable);
        attributesTypeMaps.put("METRIC_GAUGE", attributesGaugeTable);
        attributesTypeMaps.put("METRIC_HISTOGRAM", attributesHistogramTable);
        attributesTypeMaps.put("METRIC_METER", attributesMeterTable);
        attributesTypeMaps.put("METRIC_TIMER", attributesTimerTable);
        return attributesTypeMaps;
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
                                                                 Object value) throws SQLException {
        if (QueryManager.getInstance().getTypeMap("doubleType").equalsIgnoreCase(type)) {
            stmt.setDouble(ordinal, (Double) value);
        } else if (QueryManager.getInstance().getTypeMap("stringType").equalsIgnoreCase(type)) {
            stmt.setString(ordinal, (String) value);
        } else if (QueryManager.getInstance().getTypeMap("longType").equalsIgnoreCase(type)) {
            stmt.setLong(ordinal, (Long) value);
        } else if (QueryManager.getInstance().getTypeMap("integerType").equalsIgnoreCase(type)) {
            stmt.setInt(ordinal, (Integer) value);
        } else if (QueryManager.getInstance().getTypeMap("floatType").equalsIgnoreCase(type)) {
            stmt.setFloat(ordinal, (Float) value);
        } else if (QueryManager.getInstance().getTypeMap("booleanType").equalsIgnoreCase(type)) {
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
    public PreparedStatement populateInsertStatement(Object[] record, PreparedStatement stmt, Map<String, String>
            attributesTypeMap) {
        Set<String> attributes = attributesTypeMap.keySet();
        PreparedStatement populatedStatement = stmt;
        int possition = 0;
        for (String attribute : attributes) {
            Object value = record[possition];
            try {
                populatedStatement = instance.populateStatementWithSingleElement(stmt, possition + 1,
                        attributesTypeMap.get(attribute), value);
            } catch (SQLException e) {
                throw new RDBMSTableException("Dropping event since value for Attribute name " + attribute +
                        "cannot be set: " + e.getMessage(), e);
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
    public Object fetchData(ResultSet rs, String attributeName, String attributeType) throws SQLException {
        if (QueryManager.getInstance().getTypeMap("doubleType").equalsIgnoreCase(attributeType)) {
            return rs.getDouble(attributeName);
        } else if (QueryManager.getInstance().getTypeMap("stringType").equalsIgnoreCase(attributeType)) {
            return rs.getString(attributeName);
        } else if (QueryManager.getInstance().getTypeMap("longType").equalsIgnoreCase(attributeType)) {
            return rs.getLong(attributeName);
        } else if (QueryManager.getInstance().getTypeMap("integerType").equalsIgnoreCase(attributeType)) {
            return rs.getInt(attributeName);
        } else if (QueryManager.getInstance().getTypeMap("floatType").equalsIgnoreCase(attributeType)) {
            return rs.getFloat(attributeName);
        } else if (QueryManager.getInstance().getTypeMap("booleanType").equalsIgnoreCase(attributeType)) {
            return rs.getBoolean(attributeName);
        } else {
            logger.error("Invalid Type of Object ");
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

    /**
     * Method for populating values to a pre-created SQL prepared statement.
     *
     * @param record the record whose values should be populated.
     * @param stmt   the statement to which the values should be set.
     */
    public PreparedStatement populateUpdateStatement(Object[] record, PreparedStatement stmt, Map<String, String>
            attributesTypeMap) {
        Set<String> attributes = attributesTypeMap.keySet();
        PreparedStatement populatedStatement = stmt;
        int possition = 0;
        for (String attribute : attributes) {
            Object value = record[possition];
            try {
                populatedStatement = instance.populateStatementWithSingleElement(stmt, possition + 1,
                        attributesTypeMap.get(attribute), value);
            } catch (SQLException e) {
                throw new RDBMSTableException("Dropping event since value for Attribute name " + attribute +
                        "cannot be set: " + e.getMessage(), e);
            }
            possition++;
        }
        return populatedStatement;
    }
}
