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

import java.sql.{Connection, ResultSetMetaData, SQLException}
import java.util.Properties
import javax.sql.DataSource

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.catalyst.expressions.Row
import org.apache.spark.sql.jdbc.{JDBCRDD, JdbcDialects}
import org.apache.spark.sql.sources._
import org.apache.spark.sql.types._
import org.apache.spark.{Logging, Partition, SparkContext}
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils


object JDBCRDDCarbonUtils extends Logging {

  /**
   * Maps a JDBC type to a Catalyst type.  This function is called only when
   * the JdbcDialect class corresponding to your database driver returns null.
   *
   * @param sqlType - A field of java.sql.Types
   * @return The Catalyst type corresponding to sqlType.
   */
  private def getCatalystType(
                               sqlType: Int,
                               precision: Int,
                               scale: Int,
                               signed: Boolean): DataType = {
    val answer = sqlType match {
      // scalastyle:off
      case java.sql.Types.ARRAY => null
      case java.sql.Types.BIGINT => if (signed) {
        LongType
      } else {
        DecimalType.Unlimited
      }
      case java.sql.Types.BINARY => BinaryType
      case java.sql.Types.BIT => BooleanType // @see JdbcDialect for quirks
      case java.sql.Types.BLOB => BinaryType
      case java.sql.Types.BOOLEAN => BooleanType
      case java.sql.Types.CHAR => StringType
      case java.sql.Types.CLOB => StringType
      case java.sql.Types.DATALINK => null
      case java.sql.Types.DATE => DateType
      case java.sql.Types.DECIMAL
        if precision != 0 || scale != 0 => DecimalType(precision, scale)
      case java.sql.Types.DECIMAL => DecimalType.Unlimited
      case java.sql.Types.DISTINCT => null
      case java.sql.Types.DOUBLE => DoubleType
      case java.sql.Types.FLOAT => FloatType
      case java.sql.Types.INTEGER => if (signed) {
        IntegerType
      } else {
        LongType
      }
      case java.sql.Types.JAVA_OBJECT => null
      case java.sql.Types.LONGNVARCHAR => StringType
      case java.sql.Types.LONGVARBINARY => BinaryType
      case java.sql.Types.LONGVARCHAR => StringType
      case java.sql.Types.NCHAR => StringType
      case java.sql.Types.NCLOB => StringType
      case java.sql.Types.NULL => null
      case java.sql.Types.NUMERIC
        if precision != 0 || scale != 0 => DecimalType(precision, scale)
      case java.sql.Types.NUMERIC => DecimalType.Unlimited
      case java.sql.Types.NVARCHAR => StringType
      case java.sql.Types.OTHER => null
      case java.sql.Types.REAL => DoubleType
      case java.sql.Types.REF => StringType
      case java.sql.Types.ROWID => LongType
      case java.sql.Types.SMALLINT => IntegerType
      case java.sql.Types.SQLXML => StringType
      case java.sql.Types.STRUCT => StringType
      case java.sql.Types.TIME => TimestampType
      case java.sql.Types.TIMESTAMP => TimestampType
      case java.sql.Types.TINYINT => IntegerType
      case java.sql.Types.VARBINARY => BinaryType
      case java.sql.Types.VARCHAR => StringType
      case _ => null
      // scalastyle:on
    }

    if (answer == null) {
      throw new SQLException("Unsupported type " + sqlType)
    }
    answer
  }

  /**
   * Takes a (schema, table) specification and returns the table's Catalyst
   * schema.
   *
   * @param dataSource - The JDBC url to fetch information from.
   * @param table - The table name of the desired table.  This may also be a
   *              SQL query wrapped in parentheses.
   *
   * @return A StructType giving the table's Catalyst schema.
   * @throws SQLException if the table specification is garbage.
   * @throws SQLException if the table contains an unsupported type.
   */
  def resolveTable(dataSource: String, table: String): StructType = {
    val conn: Connection = GenericUtils.loadGlobalDataSource(dataSource).asInstanceOf[DataSource].getConnection
    val dialect = org.apache.spark.sql.jdbc.JdbcDialects.get(conn.getMetaData.getURL)
    try {
      val rs = conn.prepareStatement(s"SELECT * FROM $table WHERE 1=0").executeQuery()
      try {
        val rsmd = rs.getMetaData
        val ncols = rsmd.getColumnCount
        val fields = new Array[StructField](ncols)
        var i = 0
        while (i < ncols) {
          val columnName = rsmd.getColumnLabel(i + 1)
          val dataType = rsmd.getColumnType(i + 1)
          val typeName = rsmd.getColumnTypeName(i + 1)
          val fieldSize = rsmd.getPrecision(i + 1)
          val fieldScale = rsmd.getScale(i + 1)
          val isSigned = rsmd.isSigned(i + 1)
          val nullable = rsmd.isNullable(i + 1) != ResultSetMetaData.columnNoNulls
          val metadata = new MetadataBuilder().putString("name", columnName)
          val columnType =
            dialect.getCatalystType(dataType, typeName, fieldSize, metadata).getOrElse(
              getCatalystType(dataType, fieldSize, fieldScale, isSigned))
          fields(i) = StructField(columnName, columnType, nullable, metadata.build())
          i = i + 1
        }
        return new StructType(fields)
      } finally {
        rs.close()
      }
    } finally {
      conn.close()
    }

    throw new RuntimeException("This line is unreachable.")
  }

  /**
   * Prune all but the specified columns from the specified Catalyst schema.
   *
   * @param schema - The Catalyst schema of the master table
   * @param columns - The list of desired columns
   *
   * @return A Catalyst schema corresponding to columns in the given order.
   */
  private def pruneSchema(schema: StructType, columns: Array[String]): StructType = {
    val fieldMap = Map(schema.fields map { x => x.metadata.getString("name") -> x}: _*)
    new StructType(columns map { name => fieldMap(name)})
  }

  /**
   * Build and return JDBCRDD from the given information.
   *
   * @param sc - Your SparkContext.
   * @param schema - The Catalyst schema of the underlying database table.
   * @param dataSource - The class name of the JDBC driver for the given url.
   * @param fqTable - The fully-qualified table name (or paren'd SQL query) to use.
   * @param requiredColumns - The names of the columns to SELECT.
   * @param filters - The filters to include in all WHERE clauses.
   * @param parts - An array of JDBCPartitions specifying partition ids and
   *              per-partition WHERE clauses.
   *
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
    val ds = GenericUtils.loadGlobalDataSource(dataSource).asInstanceOf[DataSource]

    val conn = ds.getConnection
    try {
      val dialect = JdbcDialects.get(conn.getMetaData.getURL)
      val quotedColumns = requiredColumns.map(colName => dialect.quoteIdentifier(colName))
      new JDBCRDD(
        sc,
        ds.getConnection,
        pruneSchema(schema, requiredColumns),
        fqTable,
        quotedColumns,
        filters,
        parts,
        new Properties)
    } finally {
      conn.close()
    }
  }
}

