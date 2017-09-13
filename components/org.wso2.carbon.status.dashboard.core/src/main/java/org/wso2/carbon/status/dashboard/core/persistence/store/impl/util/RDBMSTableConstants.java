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

/**
 * Class which holds the constants required by the RDBMS Event Table implementation.
 */
public class RDBMSTableConstants {

    //Constants for retrieving database metadata information
    public static final String VERSION = "Version";
    public static final String DATABASE_PRODUCT_NAME = "Database Product Name";

    //Placeholder strings needed for processing the query configuration file
    public static final String RDBMS_QUERY_CONFIG_FILE = "rdbms-table-config.xml";
    public static final String PLACEHOLDER_COLUMNS = "{{COLUMNS, PRIMARY_KEYS}}";
    public static final String PLACEHOLDER_CONDITION = "{{CONDITION}}";
    public static final String PLACEHOLDER_COLUMNS_VALUES = "{{COLUMNS_AND_VALUES}}";
    public static final String PLACEHOLDER_TABLE_NAME = "{{TABLE_NAME}}";
    public static final String PLACEHOLDER_INDEX = "{{INDEX_COLUMNS}}";
    public static final String PLACEHOLDER_Q = "{{Q}}";

    //Miscellaneous SQL constants
    public static final String SQL_MATH_ADD = "+";
    public static final String SQL_MATH_DIVIDE = "/";
    public static final String SQL_MATH_MULTIPLY = "*";
    public static final String SQL_MATH_SUBTRACT = "-";
    public static final String SQL_MATH_MOD = "%";
    public static final String SQL_COMPARE_LESS_THAN = "<";
    public static final String SQL_COMPARE_GREATER_THAN = ">";
    public static final String SQL_COMPARE_LESS_THAN_EQUAL = "<=";
    public static final String SQL_COMPARE_GREATER_THAN_EQUAL = ">=";
    public static final String SQL_COMPARE_EQUAL = "=";
    public static final String SQL_COMPARE_NOT_EQUAL = "<>"; //Using the ANSI SQL-92 standard over '!=' (non-standard)
    public static final String SQL_AND = "AND";
    public static final String SQL_OR = "OR";
    public static final String SQL_NOT = "NOT";
    public static final String SQL_IN = "IN";
    public static final String SQL_IS_NULL = "IS NULL";
    public static final String SQL_NOT_NULL = "NOT NULL";
    public static final String SQL_PRIMARY_KEY_DEF = "PRIMARY KEY";
    public static final String SQL_WHERE = "WHERE";
    public static final String WHITESPACE = " ";
    public static final String SEPARATOR = ", ";
    public static final String EQUALS = "=";
    public static final String QUESTION_MARK = "?";
    public static final String OPEN_PARENTHESIS = "(";
    public static final String CLOSE_PARENTHESIS = ")";

    //Annotation field names
    public static final String ANNOTATION_ELEMENT_URL = "jdbc.url";
    public static final String ANNOTATION_ELEMENT_USERNAME = "username";
    public static final String ANNOTATION_ELEMENT_PASSWORD = "password";
    public static final String ANNOTATION_ELEMENT_TABLE_NAME = "table.name";
    public static final String ANNOTATION_ELEMENT_FIELD_LENGTHS = "field.length";
    public static final String ANNOTATION_ELEMENT_POOL_PROPERTIES = "pool.properties";
    public static final String ANNOTATION_ELEMENT_JNDI_RESOURCE = "jndi.resource";
    public static final String ANNOTATION_DRIVER_CLASS_NAME = "jdbc.driver.name";
    public static final String ANNOTATION_ELEMENT_DATASOURCE = "datasource";

    //Configurable System Parameters
    public static final String PROPERTY_SEPARATOR = ".";
    public static final String MIN_VERSION = "minVersion";
    public static final String MAX_VERSION = "maxVersion";
    public static final String TABLE_CHECK_QUERY = "tableCheckQuery";
    public static final String TABLE_CREATE_QUERY = "tableCreateQuery";
    public static final String INDEX_CREATE_QUERY = "indexCreateQuery";
    public static final String RECORD_INSERT_QUERY = "recordInsertQuery";
    public static final String RECORD_UPDATE_QUERY = "recordUpdateQuery";
    public static final String RECORD_SELECT_QUERY = "recordSelectQuery";
    public static final String RECORD_EXISTS_QUERY = "recordExistsQuery";
    public static final String RECORD_DELETE_QUERY = "recordDeleteQuery";
    public static final String STRING_SIZE = "stringSize";
    public static final String TYPE_MAPPING = "typeMapping";
    public static final String BINARY_TYPE = "binaryType";
    public static final String BOOLEAN_TYPE = "booleanType";
    public static final String DOUBLE_TYPE = "doubleType";
    public static final String FLOAT_TYPE = "floatType";
    public static final String INTEGER_TYPE = "integerType";
    public static final String LONG_TYPE = "longType";
    public static final String STRING_TYPE = "stringType";
    public static final String BATCH_SIZE = "batchSize";
    public static final String BATCH_ENABLE = "batchEnable";

    private RDBMSTableConstants() {
        //preventing initialization
    }

}
