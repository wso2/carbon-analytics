/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.spark.sql.jdbc.carbon

import java.sql.{Connection, SQLException}

import org.apache.commons.logging.{Log, LogFactory}
import org.apache.spark.sql.types._
import org.wso2.carbon.analytics.spark.core.exception.AnalyticsExecutionException
import org.wso2.carbon.analytics.spark.core.jdbc.{SparkJDBCQueryConfigEntry, SparkJDBCUtils}
import org.wso2.carbon.analytics.spark.core.sources.AnalyticsDatasourceWrapper

import scala.collection.mutable
import scala.util.Try

/**
  * Util functions for Spark JDBC tables.
  */
object CarbonJDBCUtils {

  import CarbonJDBCConstants._

  private final val log: Log = LogFactory.getLog(getClass.getName)

  def quoteIdentifier(colName: String, conn: Connection, table: String): String = {
    val quote = getQueryConfigEntry(conn, table).getQuoteMark
    s"$quote$colName$quote"
  }

  /**
    * Returns true if the table already exists in the JDBC database.
    */
  def tableExists(conn: Connection, table: String): Boolean = {
    val qConfEntry = getQueryConfigEntry(conn, table)
    val query = qConfEntry.getTableCheckQuery.replace("{{TABLE_NAME}}", table)
    val result = Try(conn.prepareStatement(query).executeQuery().next())
    if (result.isFailure) {
      conn.rollback()
    }
    result.isSuccess
  }

  /**
    * Returns the matching Spark JDBC query configuration entry JAXB instance for the connection
    */
  def getQueryConfigEntry(conn: Connection, table: String): SparkJDBCQueryConfigEntry = {
    val qConf = SparkJDBCUtils.loadQueryConfiguration()
    val dmd = conn.getMetaData
    var dbType = dmd.getDatabaseProductName
    val dbVersion = dmd.getDatabaseMajorVersion + "." + dmd.getDatabaseMinorVersion
    if (dbType.startsWith("DB2")) {
      dbType = "DB2.*"
    }
    val entries = qConf.getDatabases.filter(entry => entry.getDatabaseName.equalsIgnoreCase(dbType) && (entry.getVersion.isEmpty
      || entry.getVersion.equalsIgnoreCase(dbVersion)))
    if (entries.isEmpty) {
      throw new AnalyticsExecutionException(s"No suitable query configuration entry found for creating '$table'")
    }
    entries(0)
  }

  /**
    * Checks the existence of, and creates if necessary, a table in the given Carbon datasource
    * using the relevant configuration entries. Also creates DB indices as specified.
    *
    * @param dataSource  The Carbon datasource the table resides in
    * @param tableName   The name of the table to be checked and created if necessary
    * @param schema      The formatted schema of the table
    * @param primaryKeys The unique keys to be defined for the table
    */

  def checkAndCreateTable(
                           tenantId: Int,
                           dataSource: String,
                           dsDefinition: String,
                           tableName: String,
                           schema: mutable.MutableList[(String, String, Boolean, Boolean)],
                           primaryKeys: String): Unit = {
    val dsWrapper = new AnalyticsDatasourceWrapper(tenantId, dataSource, dsDefinition)
    val conn: Connection = dsWrapper.getConnection
    //Setting autocommit to false so that table and index creation take place as a single transaction
    conn.setAutoCommit(false)
    var committed = false
    try {
      if (tableExists(conn, tableName)) {
        log.debug(s"Returning without action since '$tableName' already exists.")
        return
      }
      val queryConfig = getQueryConfigEntry(conn, tableName)
      val statements = constructStatements(tableName, schema, queryConfig, primaryKeys)
      conn.prepareStatement(statements._1).execute()
      if (statements._2.nonEmpty) {
        conn.prepareStatement(statements._2).execute()
      }
      conn.commit()
      committed = true
    } catch {
      case e: SQLException => throw new
          AnalyticsExecutionException(s"Error while creating table '$tableName' in $dataSource: " + e.getMessage, e)
      case e: Exception => throw new
          AnalyticsExecutionException(s"Error while resolving the table '$tableName' in $dataSource: " + e.getMessage, e)
    } finally {
      if (!committed) {
        conn.rollback()
      }
      conn.close()
    }
  }

  /**
    * Constructs a tuple of statements ready to be processed by the JDBC connection.
    *
    * @param tableName   The name of the table for which the creation/index statements are prepared
    * @param schema      The formatted schema of the table
    * @param queryConfig The matching JAXB bean of the Spark JDBC query configuration
    * @param primaryKeys The unique keys specified for the table (if any)
    * @return A tuple containing the create statement (element 1) and create index statement (element 2)
    */
  private def constructStatements(tableName: String, schema: mutable.MutableList[(String, String, Boolean, Boolean)],
                                  queryConfig: SparkJDBCQueryConfigEntry, primaryKeys: String) = {
    val typeMapping = queryConfig.getSparkJDBCTypeMapping
    val rootStmt = queryConfig.getTableCreateQuery.replace(TABLE_NAME_PLACEHOLDER, tableName)
    val indexStmt = queryConfig.getIndexCreateQuery.replace(TABLE_NAME_PLACEHOLDER, tableName)
    val columns = new StringBuilder
    val indices = new StringBuilder
    val keys = primaryKeys.split("\\s*,\\s*")

    // Appending comma separator as prefixes. Blank string added for first element, separator added to subsequent ones.
    var columnPrefix = EMPTY_STRING
    var indexPrefix = EMPTY_STRING

    for (element <- schema) {
      columns.append(columnPrefix)
      columns.append(element._1).append(WHITESPACE)
      // Disregarding any user-specified sizes when matching
      val dataType = element._2.toLowerCase.split("""\(""")
      dataType(0) match {
        case SPARK_BINARY_TYPE => columns.append(typeMapping.getBinaryType)
        case SPARK_BOOLEAN_TYPE => columns.append(typeMapping.getBooleanType)
        case SPARK_BYTE_TYPE => columns.append(typeMapping.getByteType)
        case SPARK_DATE_TYPE => columns.append(typeMapping.getDateType)
        case SPARK_DOUBLE_TYPE => columns.append(typeMapping.getDoubleType)
        case SPARK_FLOAT_TYPE => columns.append(typeMapping.getFloatType)
        case SPARK_INTEGER_TYPE => columns.append(typeMapping.getIntegerType)
        case SPARK_LONG_TYPE => columns.append(typeMapping.getLongType)
        case SPARK_NULL_TYPE => columns.append(typeMapping.getNullType)
        case SPARK_SHORT_TYPE => columns.append(typeMapping.getShortType)
        case SPARK_TIMESTAMP_TYPE => columns.append(typeMapping.getTimestampType)
        case SPARK_STRING_TYPE =>
          // Checking if the user is specifying a size AND the type mapping accepts custom sizes
          if (dataType.length == 2 && typeMapping.getStringType.contains(OPEN_PARENTHESIS)) {
            val size = dataType(1).replace(CLOSE_PARENTHESIS, "")
            columns.append(typeMapping.getStringType.split("""\(""")(0)).append(OPEN_PARENTHESIS).append(size)
              .append(CLOSE_PARENTHESIS)
          } else {
            columns.append(typeMapping.getStringType)
          }
        case _ => throw new AnalyticsExecutionException("Unrecognized field type found in schema specification.")
      }
      if (queryConfig.isKeyExplicitNotNull && keys.contains(element._1)) {
        columns.append(WHITESPACE).append(NOT_NULL_DEF)
      }
      // populating index list
      if (element._4) {
        indices.append(indexPrefix)
        indices.append(element._1)
        indexPrefix = SEPARATOR
      }
      columnPrefix = SEPARATOR
    }
    if (primaryKeys.nonEmpty) {
      columns.append(SEPARATOR).append(PRIMARY_KEY_DEF).append(OPEN_PARENTHESIS).append(primaryKeys).append(CLOSE_PARENTHESIS)
    }
    if (indices.isEmpty) {
      (rootStmt.replace(COLUMNS_KEYS_PLACEHOLDER, columns), EMPTY_STRING)
    } else {
      (rootStmt.replace(COLUMNS_KEYS_PLACEHOLDER, columns), indexStmt.replace(INDICES_PLACEHOLDER, indices))
    }
  }

  /**
    * Truncates a specified table using the connection provided.
    */
  def truncateTable(conn: Connection, table: String) = {
    val qConfEntry = getQueryConfigEntry(conn, table)
    val query = qConfEntry.getTableTruncateQuery.replace("{{TABLE_NAME}}", table)
    try {
      conn.prepareStatement(query).execute()
      conn.commit()
    } catch {
      case e: SQLException => throw new
          AnalyticsExecutionException(s"Error while truncating '$table': " + e.getMessage, e)
    }

  }

  /**
    * Composes a list of tuples given a schema. This data structure is used acros the implementation to infer
    * schema-related information on the fly.
    *
    * @param schema      The schema in question, in string form
    * @param primaryKeys The unique keys required for the table
    * @return The formatted schema structure
    */
  def constructElementList(schema: String, primaryKeys: String) = {
    // Creating a list of all fields as given: name, type, isPrimaryKey, isIndexed
    var elements: mutable.MutableList[(String, String, Boolean, Boolean)] = new mutable.MutableList
    val pKeys: Array[String] = primaryKeys.split(",")
    val strFields: Array[String] = schema.split("\\s*,\\s*")
    for (strField <- strFields) {
      val tokens: Array[String] = strField.trim.split("\\s+")
      tokens.length match {
        case 2 =>
          if (pKeys.contains(tokens(0))) {
            val element = (tokens(0), tokens(1), true, false)
            elements += element
          } else {
            val element = (tokens(0), tokens(1), false, false)
            elements += element
          }
        case 3 =>
          if (tokens(2).equalsIgnoreCase("-i")) {
            if (pKeys.contains(tokens(0))) {
              val element = (tokens(0), tokens(1), true, true)
              elements += element
            } else {
              val element = (tokens(0), tokens(1), false, true)
              elements += element
            }
          }
          else {
            throw new AnalyticsExecutionException("Invalid attribute for column type: " + tokens(2))
          }
        case _ =>
          throw new AnalyticsExecutionException("Invalid schema definition found in schema " + schema)
      }
    }
    elements
  }

  /**
    * Takes a formatted schema specification and returns the table's Catalyst schema understood by Spark.
    *
    * @param schema The formatted schema already built based on input when the relation is created.
    * @return A StructType giving the table's Catalyst schema.
    */
  def resolveSchema(schema: mutable.MutableList[(String, String, Boolean, Boolean)]): StructType = {
    try {
      val fields = new Array[StructField](schema.length)
      var i = 0
      while (i < schema.length) {
        val metadata = new MetadataBuilder().putString("name", schema(i)._1)
        fields(i) = StructField(schema(i)._1, resolveType(schema(i)._2.toLowerCase.split("""\(""")(0)), !schema(i)._3, metadata.build())
        i = i + 1
      }
      new StructType(fields)
    }
  }

  /**
    * Infers the Spark catalyst type given a pre-specified input type
    *
    * @param definedType - The type specified in the schema specification, during creation of a relation
    * @return The matching Spark catalyst type
    */
  private def resolveType(definedType: String): DataType = {
    val response = definedType match {
      case SPARK_BINARY_TYPE => BinaryType
      case SPARK_BOOLEAN_TYPE => BooleanType
      case SPARK_BYTE_TYPE => ByteType
      case SPARK_DATE_TYPE => DateType
      case SPARK_DOUBLE_TYPE => DoubleType
      case SPARK_FLOAT_TYPE => FloatType
      case SPARK_INTEGER_TYPE => IntegerType
      case SPARK_LONG_TYPE => LongType
      case SPARK_NULL_TYPE => NullType
      case SPARK_SHORT_TYPE => ShortType
      case SPARK_STRING_TYPE => StringType
      case SPARK_TIMESTAMP_TYPE => TimestampType
      case _ => null
    }
    if (response == null) {
      throw new SQLException(s"Unsupported type found in schema: $definedType")
    }
    response
  }

}
