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

import java.sql.{Connection, DriverManager, SQLException}
import java.util.Properties
import javax.sql.DataSource

import org.apache.commons.logging.{Log, LogFactory}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.catalyst.expressions._
import org.apache.spark.{Partition, SparkContext}
import org.apache.spark.sql.jdbc.JDBCRDD
import org.apache.spark.sql.sources.Filter
import org.apache.spark.sql.types.{ByteType, _}
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils
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

  def quoteIdentifier(colName: String, conn: Connection): String = {
    val quote = getQueryConfigEntry(conn).getQuoteMark
    s"$quote$colName$quote"
  }

  /**
    * Establishes a JDBC connection.
    */
  def createConnection(url: String, connectionProperties: Properties): Connection = {
    DriverManager.getConnection(url, connectionProperties)
  }

  /**
    * Returns true if the table already exists in the JDBC database.
    */
  def tableExists(conn: Connection, table: String): Boolean = {
    val qConfEntry = getQueryConfigEntry(conn)
    val query = qConfEntry.getTableCheckQuery.replace("{{TABLE_NAME}}", table)

    Try(conn.prepareStatement(query).executeQuery().next()).isSuccess
  }

  /**
    * Returns the matching Spark JDBC query configuration entry JAXB instance for the connection
    */
  def getQueryConfigEntry(conn: Connection): SparkJDBCQueryConfigEntry = {
    val qConf = SparkJDBCUtils.loadQueryConfiguration()
    var dbType = conn.getMetaData.getDatabaseProductName

    if (dbType.startsWith("DB2")) {
      dbType = "DB2.*"
    }

    qConf.getDatabases.find(entry => entry.getDatabaseName.equalsIgnoreCase(dbType)).get
  }

  /**
    * Checks the existence of, and creates if necessary, a table in the given Carbon datasource
    * using the relevant configuration entries. Also creates DB indices as specified.
    * @param dataSource   - The Carbon datasource the table resides in
    * @param tableName    - The name of the table to be checked and created if necessary
    * @param schema       - The formatted schema of the table
    * @param primaryKeys  - The unique keys to be defined for the table
    */

  def checkAndCreateTable(dataSource: String, tableName: String,
                          schema: mutable.MutableList[(String, String, Boolean, Boolean)],
                          primaryKeys: String): Unit = {
    val conn: Connection = GenericUtils.loadGlobalDataSource(dataSource).asInstanceOf[DataSource].getConnection
    //Setting autocommit to false so that table and index creation take place as a single transaction
    conn.setAutoCommit(false)
    var committed = false
    try {
      val queryConfig = getQueryConfigEntry(conn)
      val typeMapping = queryConfig.getSparkJDBCTypeMapping
      if (tableExists(conn, tableName)) {
        log.debug(s"Returning without action since table $tableName already exists.")
        return
      }
      var rootStmt = queryConfig.getTableCreateQuery.replace(TABLE_NAME_PLACEHOLDER, tableName)
      var indexStmt = queryConfig.getIndexCreateQuery.replace(TABLE_NAME_PLACEHOLDER, tableName)
      val columns = new StringBuilder
      val indices = new StringBuilder
      var field_prefix = ""
      var index_prefix = ""

      for (element <- schema) {
        columns.append(field_prefix)
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
        }
        if (element._4) {
          indices.append(index_prefix)
          indices.append(element._1)
          index_prefix = SEPARATOR
        }
        field_prefix = SEPARATOR
      }
      if (primaryKeys.length > 0) {
        columns.append(SEPARATOR).append(PRIMARY_KEY_DEF).append(OPEN_PARENTHESIS).append(primaryKeys).append(CLOSE_PARENTHESIS)
      }

      rootStmt = rootStmt.replace(COLUMNS_KEYS_PLACEHOLDER, columns)

      conn.prepareStatement(rootStmt).execute()
      if (indices.nonEmpty) {
        indexStmt = indexStmt.replace(INDICES_PLACEHOLDER, indices)
        conn.prepareStatement(indexStmt).execute()
      }
      conn.commit()
      committed = true
    } catch {
      case e: SQLException => throw new
          AnalyticsExecutionException(s"Error while creating table $tableName on datasource $dataSource : " + e.getMessage, e)
      case e: Exception => throw new
          AnalyticsExecutionException(s"Error while resolving the table $tableName in datasource  $dataSource : " + e.getMessage, e)
    } finally {
      if (!committed) {
        conn.rollback()
      }
      conn.close()
    }
  }

  /**
    * Truncates a specified table using the connection provided.
    */
  def truncateTable(conn: Connection, table: String) = {
    val qConfEntry = getQueryConfigEntry(conn)
    val query = qConfEntry.getTableTruncateQuery.replace("{{TABLE_NAME}}", table)
    try {
      conn.prepareStatement(query).execute()
      conn.commit()
    } catch {
      case e: SQLException => throw new
          AnalyticsExecutionException(s"Error while truncating table $table : " + e.getMessage, e)
    }

  }

  /**
    * Composes a list of tuples given a schema. This data structure is used acros the implementation to infer
    * schema-related information on the fly.
    * @param schema       - The schema in question, in string form
    * @param primaryKeys  - The unique keys required for the table
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
    * @param schema   - The formatted schema already built based on input when the relation is created.
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
    * @param definedType  - The type specified in the schema specification, during creation of a relation
    * @return The matching Spark catalyst type
    */
  def resolveType(definedType: String): DataType = {
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

  /**
    * Build and return JDBCRDD from the given information.
    *
    * @param sc              - Your SparkContext.
    * @param schema          - The Catalyst schema of the underlying database table.
    * @param dataSource      - The class name of the JDBC driver for the given url.
    * @param fqTable         - The fully-qualified table name (or paren'd SQL query) to use.
    * @param requiredColumns - The names of the columns to SELECT.
    * @param filters         - The filters to include in all WHERE clauses.
    * @param parts           - An array of JDBCPartitions specifying partition ids and
    *                        per-partition WHERE clauses.
    * @return An RDD representing "SELECT requiredColumns FROM fqTable".
    */
  def scanTable(
                 sc: SparkContext,
                 schema: StructType,
                 dataSource: String,
                 fqTable: String,
                 requiredColumns: Array[String],
                 filters: Array[Filter],
                 parts: Array[Partition]): RDD[Row] = {

    val dsWrapper = new AnalyticsDatasourceWrapper(dataSource)
    val conn = dsWrapper.getConnection
    try {
      val quotedColumns = requiredColumns.map(colName => quoteIdentifier(colName, conn))
      new JDBCRDD(
        sc,
        dsWrapper.getConnection,
        pruneSchema(schema, requiredColumns),
        fqTable,
        quotedColumns,
        filters,
        parts,
        new Properties)
    } catch {
      case e: SQLException => throw new
          AnalyticsExecutionException(s"Error while connecting to datasource $dataSource : " + e.getMessage, e)
      case e: Exception => throw new
          AnalyticsExecutionException(s"Error while scanning the table $fqTable in datasource $dataSource : " + e.getMessage, e)
    }
    finally {
      conn.close()
    }
  }

  /**
    * Prune all but the specified columns from the specified Catalyst schema.
    *
    * @param schema  - The Catalyst schema of the master table
    * @param columns - The list of desired columns
    * @return A Catalyst schema corresponding to columns in the given order.
    */
  private def pruneSchema(schema: StructType, columns: Array[String]): StructType = {
    val fieldMap = Map(schema.fields map { x => x.metadata.getString("name") -> x }: _*)
    new StructType(columns map { name => fieldMap(name) })
  }

}
