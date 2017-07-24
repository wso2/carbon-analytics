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

package org.apache.spark.sql.jdbc

import java.sql.{Connection, SQLException}
import java.util.Properties

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.execution.datasources.jdbc.JDBCRDD
import org.apache.spark.sql.sources.Filter
import org.apache.spark.{Logging, Partition, SparkContext}
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Row}
import org.wso2.carbon.analytics.spark.core.exception.AnalyticsExecutionException
import org.wso2.carbon.analytics.spark.core.jdbc.SparkJDBCQueryConfigEntry
import org.wso2.carbon.analytics.spark.core.sources.AnalyticsDatasourceWrapper

package object carbon {

  object CarbonJDBCWrite extends Logging {

    /**
      * Saves the RDD to the database in a single transaction.
      */
    def saveTable(
                   frame: DataFrame,
                   tenantId: Int,
                   dataSource: String,
                   dsDefinition: String,
                   tableName: String,
                   primaryKeys: String) {
      val rddSchema = frame.schema
      val dsWrapper = new AnalyticsDatasourceWrapper(tenantId, dataSource, dsDefinition)
      try {
        try {
          val nullTypes: Array[Int] = frame.schema.fields.map { field =>
            field.dataType match {
              case IntegerType => java.sql.Types.INTEGER
              case LongType => java.sql.Types.BIGINT
              case DoubleType => java.sql.Types.DOUBLE
              case FloatType => java.sql.Types.REAL
              case ShortType => java.sql.Types.INTEGER
              case ByteType => java.sql.Types.INTEGER
              case BooleanType => java.sql.Types.BIT
              case StringType => java.sql.Types.VARCHAR
              case BinaryType => java.sql.Types.BLOB
              case TimestampType => java.sql.Types.TIMESTAMP
              case DateType => java.sql.Types.DATE
              case DecimalType.Fixed(precision, scale) => java.sql.Types.NUMERIC
              case DecimalType.Unlimited => java.sql.Types.NUMERIC
              case _ => throw new IllegalArgumentException(s"Can't translate null value for field $field")
            }
          }
          frame.foreachPartition { iterator =>
            CarbonJDBCWrite.savePartition(dsWrapper.getConnection, tableName, primaryKeys, iterator, rddSchema, nullTypes)
          }
        }
      }
      catch {
        case e: Exception =>
          throw new AnalyticsExecutionException("Error while saving data to the table "
            + tableName + " : " + e.getMessage, e)
      }
    }

    /**
      * Saves a partition of a DataFrame to the JDBC database.  This is done in
      * a single database transaction in order to avoid repeatedly inserting
      * data as much as possible.
      *
      * It is still theoretically possible for rows in a DataFrame to be
      * inserted into the database more than once if a stage somehow fails after
      * the commit occurs but before the stage can return successfully.
      *
      * This is not a closure inside saveTable() because apparently cosmetic
      * implementation changes elsewhere might easily render such a closure
      * non-java.io.Serializable.  Instead, we explicitly close over all variables that
      * are used.
      */
    def savePartition(
                       getConnection: () => Connection,
                       table: String,
                       primaryKeys: String,
                       iterator: Iterator[Row],
                       rddSchema: StructType,
                       nullTypes: Array[Int]): Iterator[Byte] = {
      val conn = getConnection()
      conn.setAutoCommit(false) // Everything in the same db transaction.
      var committed = false
      try {
        val qConf = CarbonJDBCUtils.getQueryConfigEntry(conn, table)
        var sql = ""
        if (primaryKeys.length > 0) {
          sql = mergeStatement(table, primaryKeys, rddSchema, qConf)
        } else {
          sql = insertStatement(table, rddSchema, qConf)
        }
        val stmt = conn.prepareStatement(sql)
        try {
          while (iterator.hasNext) {
            val row = iterator.next()
            val numFields = rddSchema.fields.length
            var i = 0
            while (i < numFields) {
              if (row.isNullAt(i)) {
                stmt.setNull(i + 1, nullTypes(i))
              } else {
                rddSchema.fields(i).dataType match {
                  case BinaryType => stmt.setBytes(i + 1, row.getAs[Array[Byte]](i))
                  case BooleanType => stmt.setBoolean(i + 1, row.getBoolean(i))
                  case ByteType => stmt.setInt(i + 1, row.getByte(i))
                  case DateType => stmt.setDate(i + 1, row.getAs[java.sql.Date](i))
                  case DoubleType => stmt.setDouble(i + 1, row.getDouble(i))
                  case FloatType => stmt.setFloat(i + 1, row.getFloat(i))
                  case IntegerType => stmt.setInt(i + 1, row.getInt(i))
                  case LongType => stmt.setLong(i + 1, row.getLong(i))
                  case ShortType => stmt.setInt(i + 1, row.getShort(i))
                  case StringType => stmt.setString(i + 1, row.getString(i))
                  case TimestampType => stmt.setTimestamp(i + 1, row.getAs[java.sql.Timestamp](i))
                  case DecimalType.Fixed(precision, scale) => stmt.setBigDecimal(i + 1,
                    row.getAs[java.math.BigDecimal](i))
                  case DecimalType.Unlimited => stmt.setBigDecimal(i + 1,
                    row.getAs[java.math.BigDecimal](i))
                  case _ => throw new IllegalArgumentException(
                    s"Can not translate non-null value for field $i")
                }
              }
              i = i + 1
            }
            stmt.addBatch()
          }
          stmt.executeBatch();
        } finally {
          stmt.close()
        }
        conn.commit()
        committed = true
      } finally {
        if (!committed) {
          // The stage must fail.  We got here through an exception path, so
          // let the exception through unless rollback() or close() want to
          // tell the user about another problem.
          conn.rollback()
          conn.close()
        } else {
          // The stage must succeed.  We cannot propagate any exception close() might throw.
          try {
            conn.close()
          } catch {
            case e: Exception => logWarning("Transaction succeeded, but closing failed", e)
          }
        }
      }
      Array[Byte]().iterator
    }

    /**
      * Returns a PreparedStatement that inserts a row into table via conn.
      * Used in situations where unique key constraints are not a concern, or when the table has been created afresh.
      */
    def insertStatement(
                         table: String,
                         rddSchema: StructType,
                         qConf: SparkJDBCQueryConfigEntry): String = {
      import CarbonJDBCConstants._
      val sql = qConf.getRecordInsertQuery.replace(TABLE_NAME_PLACEHOLDER, table)
      val params = new StringBuilder

      var fieldsLeft = rddSchema.fields.length
      while (fieldsLeft > 0) {
        params.append(QUESTION_MARK)
        if (fieldsLeft > 1) {
          params.append(SEPARATOR)
        }
        fieldsLeft = fieldsLeft - 1
      }
      sql.replace(Q_PLACEHOLDER, params)
    }

    /**
      * Returns a PreparedStatement that merges a row into table via conn.
      * Used in situations where unique key constraints limit the use of a generic row append operation.
      */
    def mergeStatement(
                        table: String,
                        primaryKeys: String,
                        rddSchema: StructType,
                        qConf: SparkJDBCQueryConfigEntry): String = {
      import CarbonJDBCConstants._
      val sql = qConf.getRecordMergeQuery.replace(TABLE_NAME_PLACEHOLDER, table)

      val pKeys = primaryKeys.split("\\s*,\\s*") // {{KEYS}}
      val columns = new StringBuilder // {{COLUMNS}}
      val q = new StringBuilder // {{Q}}
      val columnEqualsValuesColumn = new StringBuilder // {{COLUMN=VALUES(COLUMN) - KEY}}
      val dColumnEqualsSColumn = new StringBuilder // {{D.COLUMN=S.COLUMN - KEY}}
      val qColumn = new StringBuilder // {{Q COLUMN}}
      val sColumn = new StringBuilder // {{S.COLUMN}}
      val columnEqualsSColumn = new StringBuilder // {{COLUMN=S.COLUMN}}
      val dKeyEqualsSKey = new StringBuilder // {{D.KEY=S.KEY}}
      val columnEqualsExcludedColumn = new StringBuilder // {{COLUMN=EXCLUDED.COLUMN - KEY}}

      var columnPrefix = EMPTY_STRING
      var columnLessKeyPrefix = EMPTY_STRING
      var keyPrefix = EMPTY_STRING

      for (key <- pKeys) {
        dKeyEqualsSKey.append(keyPrefix).append(DEST_STRING)
          .append(key).append(EQUALS_SIGN).append(SRC_STRING).append(key)
        keyPrefix = AND_SEPARATOR
      }

      for (field <- rddSchema.fields) {
        val column = field.name
        columns.append(columnPrefix).append(column)
        q.append(columnPrefix).append(QUESTION_MARK)
        qColumn.append(columnPrefix).append(QUESTION_MARK)
          .append(WHITESPACE).append(column)
        sColumn.append(columnPrefix).append(SRC_STRING).append(column)
        columnEqualsSColumn.append(columnPrefix).append(column).append(EQUALS_SIGN)
          .append(SRC_STRING).append(column)

        if (!pKeys.contains(column)) {
          columnEqualsValuesColumn.append(columnLessKeyPrefix).append(column).append(EQUALS_SIGN).append(VALUES_STRING)
            .append(OPEN_PARENTHESIS).append(column).append(CLOSE_PARENTHESIS)
          dColumnEqualsSColumn.append(columnLessKeyPrefix).append(DEST_STRING)
            .append(column).append(EQUALS_SIGN).append(SRC_STRING).append(column)
          columnEqualsExcludedColumn.append(columnLessKeyPrefix).append(column).append(EQUALS_SIGN)
            .append(EXCLUDED_STRING).append(column)
          columnLessKeyPrefix = SEPARATOR
        }

        columnPrefix = SEPARATOR
      }

      sql.replace(COLUMNS_PLACEHOLDER, columns)
        .replace(Q_PLACEHOLDER, q)
        .replace(COLUMN_EQUALS_VALUES_COLUMN_PLACEHOLDER, columnEqualsValuesColumn)
        .replace(D_COLUMN_EQUALS_S_COLUMN_PLACEHOLDER, dColumnEqualsSColumn)
        .replace(Q_COLUMN_PLACEHOLDER, qColumn)
        .replace(S_COLUMN_PLACEHOLDER, sColumn)
        .replace(COLUMN_EQUALS_S_COLUMN_PLACEHOLDER, columnEqualsSColumn)
        .replace(D_KEY_EQUALS_S_KEY_PLACEHOLDER, dKeyEqualsSKey)
        .replace(KEYS_PLACEHOLDER, primaryKeys)
        .replace(COLUMN_EQUALS_EXCLUDED_COLUMN_PLACEHOLDER, columnEqualsExcludedColumn)
    }
  }

  object CarbonJDBCScan {

    /**
      * Build and return JDBCRDD from the given information.
      *
      * @param sc              Your SparkContext.
      * @param schema          The Catalyst schema of the underlying database table.
      * @param tenantId        The tenant ID of the calling tenant
      * @param dataSource      The class name of the JDBC driver for the given url.
      * @param dsDefinition    The Datasource definition in text form
      * @param fqTable         The fully-qualified table name (or paren'd SQL query) to use.
      * @param requiredColumns The names of the columns to SELECT.
      * @param filters         The filters to include in all WHERE clauses.
      * @param parts           An array of JDBCPartitions specifying partition ids and per-partition WHERE clauses.
      * @return An RDD representing "SELECT requiredColumns FROM fqTable".
      */
    def scanTable(
                   sc: SparkContext,
                   schema: StructType,
                   tenantId: Int,
                   dataSource: String,
                   dsDefinition: String,
                   fqTable: String,
                   requiredColumns: Array[String],
                   filters: Array[Filter],
                   parts: Array[Partition]): RDD[InternalRow] = {

      val dsWrapper = new AnalyticsDatasourceWrapper(tenantId, dataSource, dsDefinition)
      val conn = dsWrapper.getConnection
      try {
        val quotedColumns = requiredColumns.map(colName => CarbonJDBCUtils.quoteIdentifier(colName, conn, fqTable))
        new JDBCRDD(
          sc,
          dsWrapper.getConnection,
          CarbonJDBCScan.pruneSchema(schema, requiredColumns),
          fqTable,
          quotedColumns,
          filters,
          parts,
          conn.getMetaData.getURL,
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
      * @param schema  The Catalyst schema of the master table
      * @param columns The list of desired columns
      * @return A Catalyst schema corresponding to columns in the given order.
      */
    def pruneSchema(schema: StructType, columns: Array[String]): StructType = {
      val fieldMap = Map(schema.fields map { x => x.metadata.getString("name") -> x }: _*)
      new StructType(columns map { name => fieldMap(name) })
    }

  }

}