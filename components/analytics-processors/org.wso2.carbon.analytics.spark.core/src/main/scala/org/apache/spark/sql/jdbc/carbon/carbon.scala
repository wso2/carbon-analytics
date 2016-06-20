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

import java.sql.{Connection, PreparedStatement}

import org.apache.spark.Logging
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Row}
import org.wso2.carbon.analytics.spark.core.exception.AnalyticsExecutionException
import org.wso2.carbon.analytics.spark.core.jdbc.SparkJDBCQueryConfigEntry
import org.wso2.carbon.analytics.spark.core.sources.AnalyticsDatasourceWrapper

package object carbon {

  object JDBCWriteDetails extends Logging {

    /**
      * Saves the RDD to the database in a single transaction.
      */
    def saveTable(
                   df: DataFrame,
                   dataSource: String,
                   tableName: String,
                   primaryKeys: String,
                   overwrite: Boolean) {
      val rddSchema = df.schema
      val dsWrapper = new AnalyticsDatasourceWrapper(dataSource)
      try {
        val conn = dsWrapper.getConnection
        try {
          val nullTypes: Array[Int] = df.schema.fields.map { field =>
            field.dataType match {
              case IntegerType => java.sql.Types.INTEGER
              case LongType => java.sql.Types.BIGINT
              case DoubleType => java.sql.Types.DOUBLE
              case FloatType => java.sql.Types.REAL
              case ShortType => java.sql.Types.INTEGER
              case ByteType => java.sql.Types.INTEGER
              case BooleanType => java.sql.Types.BIT
              case StringType => java.sql.Types.CLOB
              case BinaryType => java.sql.Types.BLOB
              case TimestampType => java.sql.Types.TIMESTAMP
              case DateType => java.sql.Types.DATE
              case DecimalType.Fixed(precision, scale) => java.sql.Types.NUMERIC
              case DecimalType.Unlimited => java.sql.Types.NUMERIC
              case _ => throw new IllegalArgumentException(
                s"Can't translate null value for field $field")
            }
          }
          df.foreachPartition { iterator =>
            JDBCWriteDetails.savePartition(dsWrapper.getConnection, tableName, primaryKeys, overwrite, iterator, rddSchema, nullTypes)
          }
        } finally {
          conn.close()
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
                       overwrite: Boolean,
                       iterator: Iterator[Row],
                       rddSchema: StructType,
                       nullTypes: Array[Int]): Iterator[Byte] = {
      val conn = getConnection()
      conn.setAutoCommit(false) // Everything in the same db transaction.
      var committed = false
      try {
        val qConf = CarbonJDBCUtils.getQueryConfigEntry(conn)
        var stmt: PreparedStatement = null
        if (primaryKeys.length > 0) {
          stmt = mergeStatement(conn, table, primaryKeys, rddSchema, qConf)
        } else {
          stmt = insertStatement(conn, table, rddSchema, qConf)
        }
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
            // TODO (future): Check if batching is really necessary in this case
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
    def insertStatement(conn: Connection, table: String, rddSchema: StructType, qConf: SparkJDBCQueryConfigEntry):
    PreparedStatement = {
      import CarbonJDBCConstants._
      var sql = qConf.getRecordInsertQuery.replace(TABLE_NAME_PLACEHOLDER, table)
      val params = new StringBuilder

      var fieldsLeft = rddSchema.fields.length
      while (fieldsLeft > 0) {
        params.append(QUESTION_MARK)
        if (fieldsLeft > 1) {
          params.append(SEPARATOR)
        }
        fieldsLeft = fieldsLeft - 1
      }
      sql = sql.replace(Q_PLACEHOLDER, params)
      conn.prepareStatement(sql)
    }

    /**
      * Returns a PreparedStatement that merges a row into table via conn.
      * Used in situations where unique key constraints limit the use of a generic row append operation.
      */
    def mergeStatement(conn: Connection, table: String, primaryKeys: String, rddSchema: StructType,
                       qConf: SparkJDBCQueryConfigEntry):
    PreparedStatement = {
      import CarbonJDBCConstants._
      var sql = qConf.getRecordMergeQuery.replace(TABLE_NAME_PLACEHOLDER, table)

      val pKeys = primaryKeys.split("\\s*,\\s*")          // {{KEYS}}
      val columns = new StringBuilder                     // {{COLUMNS}}
      val q = new StringBuilder                           // {{Q}}
      val columnEqualsValuesColumn = new StringBuilder    // {{COLUMN=VALUES(COLUMN) - KEY}}
      val dColumnEqualsSColumn = new StringBuilder        // {{D.COLUMN=S.COLUMN - KEY}}
      val qColumn = new StringBuilder                     // {{Q COLUMN}}
      val sColumn = new StringBuilder                     // {{S.COLUMN}}
      val columnEqualsSColumn = new StringBuilder         // {{COLUMN=S.COLUMN}}
      val dKeyEqualsSKey = new StringBuilder              // {{D.KEY=S.KEY}}

      var columnPrefix = EMPTY_STRING
      var columnEqualsValuesColumnPrefix = EMPTY_STRING
      var dKeyEqualsSKeyPrefix = EMPTY_STRING

      for (key <- pKeys) {
        dKeyEqualsSKey.append(dKeyEqualsSKeyPrefix).append(DEST_STRING)
          .append(key).append(EQUALS_SIGN).append(SRC_STRING).append(key)
        dKeyEqualsSKeyPrefix = AND_SEPARATOR
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
          columnEqualsValuesColumn.append(columnEqualsValuesColumnPrefix).append(column)
            .append(EQUALS_SIGN).append(VALUES_STRING)
            .append(OPEN_PARENTHESIS).append(column).append(CLOSE_PARENTHESIS)
          dColumnEqualsSColumn.append(columnEqualsValuesColumnPrefix).append(DEST_STRING)
            .append(column).append(EQUALS_SIGN).append(SRC_STRING).append(column)
          columnEqualsValuesColumnPrefix = SEPARATOR
        }
        columnPrefix = SEPARATOR
      }

      sql = sql.replace(COLUMNS_PLACEHOLDER, columns)
        .replace(Q_PLACEHOLDER, q)
        .replace(COLUMN_EQUALS_VALUES_COLUMN_PLACEHOLDER, columnEqualsValuesColumn)
        .replace(D_COLUMN_EQUALS_S_COLUMN_PLACEHOLDER, dColumnEqualsSColumn)
        .replace(Q_COLUMN_PLACEHOLDER, qColumn)
        .replace(S_COLUMN_PLACEHOLDER, sColumn)
        .replace(COLUMN_EQUALS_S_COLUMN_PLACEHOLDER, columnEqualsSColumn)
        .replace(D_KEY_EQUALS_S_KEY_PLACEHOLDER, dKeyEqualsSKey)
        .replace(KEYS_PLACEHOLDER, primaryKeys)

      conn.prepareStatement(sql)
    }
  }

}