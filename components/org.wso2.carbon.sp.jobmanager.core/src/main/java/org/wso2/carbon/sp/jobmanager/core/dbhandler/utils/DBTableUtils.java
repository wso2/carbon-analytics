/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.carbon.sp.jobmanager.core.impl.utils.Constants;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Class which holds the utility methods which are used by database implementation.
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
        String intType = statusDashboardQueryManager.getQuery(Constants.INTEGER_TYPE);
        String stringType = statusDashboardQueryManager.getQuery(Constants.STRING_TYPE);
        Map<String, String> attributeSelection = new HashMap<>();
        attributeSelection.put(Constants.MANAGERID, stringType);
        attributeSelection.put(Constants.HOST, stringType);
        attributeSelection.put(Constants.PORT, intType);
        return attributeSelection;
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
    public PreparedStatement populateStatementWithSingleElement(PreparedStatement stmt, int ordinal, String type,
                                                                Object value,
                                                                QueryManager statusDashboardQueryManager) throws
                                                                                                           SQLException {
        String doubleType = statusDashboardQueryManager.getQuery(Constants.DOUBLE_TYPE);
        String longType = statusDashboardQueryManager.getQuery(Constants.LONG_TYPE);
        String stringType = statusDashboardQueryManager.getQuery(Constants.STRING_TYPE);
        String integerType = statusDashboardQueryManager.getQuery(Constants.INTEGER_TYPE);
        String floatType = statusDashboardQueryManager.getQuery(Constants.FLOAT_TYPE);
        String booleanType = statusDashboardQueryManager.getQuery(Constants.BOOL_TYPE);

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
     * Method for populating values to a pre-created SQL prepared statement.
     *
     * @param record the record whose values should be populated.
     * @param stmt   the statement to which the values should be set.
     */
    public PreparedStatement populateInsertStatement(Map<String, Object> record, PreparedStatement stmt, Map<String,
            String> attributesTypeMap, QueryManager statusDashboardQueryManager) {
        int position = 0;
        for (Map.Entry<String, String> attributeEntry : attributesTypeMap.entrySet()) {
            Object value = record.get(attributeEntry.getKey());
            try {
                stmt = instance.populateStatementWithSingleElement(stmt, position + 1,
                                                                   attributeEntry.getValue(), value,
                                                                   statusDashboardQueryManager);
            } catch (SQLException e) {
                throw new RDBMSTableException("Dropping event since value for Attribute name " +
                                                      attributeEntry.getKey() + "cannot be set: " + e.getMessage(), e);
            }
            position++;
        }
        return stmt;
    }
}
