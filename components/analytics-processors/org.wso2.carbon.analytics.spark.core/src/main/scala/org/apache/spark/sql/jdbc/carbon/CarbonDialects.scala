package org.apache.spark.sql.jdbc.carbon

import java.sql.Types

import org.apache.spark.annotation.DeveloperApi
import org.apache.spark.sql.jdbc.{JdbcDialects, JdbcDialect, JdbcType}
import org.apache.spark.sql.types._

/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

case object DialectRegister {
  def register(): Unit = {
    JdbcDialects.registerDialect(PostgresDialect)
    JdbcDialects.registerDialect(OracleDialect)
    JdbcDialects.registerDialect(MySQLDialect)
    JdbcDialects.registerDialect(MSSQLDialect)
    JdbcDialects.registerDialect(DB2Dialect)
  }
}

/**
 * Custom postgres dialect
 */
case object PostgresDialect extends JdbcDialect {
  override def canHandle(url: String): Boolean = url.startsWith("jdbc:postgresql") || url.contains("postgre")

  override def getCatalystType(sqlType: Int, typeName: String, size: Int, md: MetadataBuilder): Option[DataType] = {
    if (sqlType == Types.BIT && typeName.equals("bit") && size != 1) {
      Some(BinaryType)
    } else if (sqlType == Types.OTHER && typeName.equals("cidr")) {
      Some(StringType)
    } else if (sqlType == Types.OTHER && typeName.equals("inet")) {
      Some(StringType)
    } else None
  }

  override def getJDBCType(dt: DataType): Option[JdbcType] = dt match {
    case StringType => Some(JdbcType("TEXT", java.sql.Types.VARCHAR))
    case BinaryType => Some(JdbcType("BYTEA", java.sql.Types.BINARY))
    case IntegerType => Some(JdbcType("INTEGER", java.sql.Types.INTEGER))
    case LongType => Some(JdbcType("BIGINT", java.sql.Types.BIGINT))
    case DoubleType => Some(JdbcType("DOUBLE PRECISION", java.sql.Types.DOUBLE))
    case FloatType => Some(JdbcType("REAL", java.sql.Types.REAL))
    case ShortType => Some(JdbcType("SMALLINT", java.sql.Types.SMALLINT))
    case ByteType => Some(JdbcType("TINYINT", java.sql.Types.TINYINT))
    case BooleanType => Some(JdbcType("BOOLEAN", java.sql.Types.BIT))
    case TimestampType => Some(JdbcType("TIMESTAMP", java.sql.Types.TIMESTAMP))
    case DateType => Some(JdbcType("DATE", java.sql.Types.DATE))
    case DecimalType.Unlimited => Some(JdbcType("DECIMAL", java.sql.Types.DECIMAL))
    case _ => None
  }
}

/**
 * Custom mysql dialect.
 */
case object MySQLDialect extends JdbcDialect {
  override def canHandle(url: String): Boolean = url.startsWith("jdbc:mysql") || url.contains("mysql")

  override def getCatalystType(
                                sqlType: Int, typeName: String, size: Int, md: MetadataBuilder): Option[DataType] = {
    if (sqlType == Types.VARBINARY && typeName.equals("BIT") && size != 1) {
      // This could instead be a BinaryType if we'd rather return bit-vectors of up to 64 bits as
      // byte arrays instead of longs.
      md.putLong("binarylong", 1)
      Some(LongType)
    } else if (sqlType == Types.BIT && typeName.equals("TINYINT")) {
      Some(BooleanType)
    } else None
  }

  override def getJDBCType(dt: DataType): Option[JdbcType] = dt match {
    case StringType => Some(JdbcType("TEXT", java.sql.Types.VARCHAR))
    case BooleanType => Some(JdbcType("TINYINT(1)", java.sql.Types.BIT))
    case IntegerType => Some(JdbcType("INTEGER", java.sql.Types.INTEGER))
    case LongType => Some(JdbcType("BIGINT", java.sql.Types.BIGINT))
    case DoubleType => Some(JdbcType("DOUBLE", java.sql.Types.DOUBLE))
    case FloatType => Some(JdbcType("FLOAT", java.sql.Types.FLOAT))
    case ByteType => Some(JdbcType("SMALLINT", java.sql.Types.SMALLINT))
    case BinaryType => Some(JdbcType("BLOB", java.sql.Types.BLOB))
    case TimestampType => Some(JdbcType("DATETIME", java.sql.Types.DATE))
    case DateType => Some(JdbcType("DATE", java.sql.Types.DATE))
    case DecimalType.Unlimited => Some(JdbcType("DECIMAL(40,20)", java.sql.Types.DECIMAL))
    case _ => None
  }

  override def quoteIdentifier(colName: String): String = {
    s"`$colName`"
  }
}

/**
 * Custom oracle dialect.
 */
case object OracleDialect extends JdbcDialect {
  override def canHandle(url: String): Boolean = url.startsWith("jdbc:oracle") || url.contains("oracle")

  override def getJDBCType(dt: DataType): Option[JdbcType] = dt match {
    case StringType => Some(JdbcType("VARCHAR2(255)", java.sql.Types.VARCHAR))
    case BooleanType => Some(JdbcType("NUMBER(1)", java.sql.Types.NUMERIC))
    case IntegerType => Some(JdbcType("NUMBER(10)", java.sql.Types.NUMERIC))
    case LongType => Some(JdbcType("NUMBER(19)", java.sql.Types.NUMERIC))
    case DoubleType => Some(JdbcType("NUMBER(19,4)", java.sql.Types.NUMERIC))
    case FloatType => Some(JdbcType("NUMBER(19,4)", java.sql.Types.NUMERIC))
    case ShortType => Some(JdbcType("NUMBER(5)", java.sql.Types.NUMERIC))
    case ByteType => Some(JdbcType("NUMBER(3)", java.sql.Types.NUMERIC))
    case BinaryType => Some(JdbcType("BLOB", java.sql.Types.BLOB))
    case TimestampType => Some(JdbcType("DATE", java.sql.Types.DATE))
    case DateType => Some(JdbcType("DATE", java.sql.Types.DATE))
    case DecimalType.Fixed(precision, scale) => Some(JdbcType("NUMBER(" + precision + "," + scale + ")", java.sql.Types.NUMERIC))
    case DecimalType.Unlimited => Some(JdbcType("NUMBER(38,4)", java.sql.Types.NUMERIC))
    case _ => None
  }
}

  /**
   * Custom DB2 dialect.
   */
  case object DB2Dialect extends JdbcDialect {
    override def canHandle(url: String): Boolean = url.startsWith("jdbc:db2") || url.contains("db2") || url.contains("ibm")

    override def getJDBCType(dt: DataType): Option[JdbcType] = dt match {
      case StringType => Some(JdbcType("VARCHAR(255)", java.sql.Types.VARCHAR))
      case BooleanType => Some(JdbcType("SMALLINT", java.sql.Types.SMALLINT))
      case IntegerType => Some(JdbcType("INTEGER", java.sql.Types.INTEGER))
      case LongType => Some(JdbcType("BIGINT", java.sql.Types.BIGINT))
      case DoubleType => Some(JdbcType("DOUBLE", java.sql.Types.DOUBLE))
      case FloatType => Some(JdbcType("REAL", java.sql.Types.REAL))
      case ShortType => Some(JdbcType("SMALLINT", java.sql.Types.SMALLINT))
      case ByteType => Some(JdbcType("SMALLINT", java.sql.Types.SMALLINT))
      case BinaryType => Some(JdbcType("BLOB(64000)", java.sql.Types.BLOB))
      case TimestampType => Some(JdbcType("TIMESTAMP", java.sql.Types.TIMESTAMP))
      case DateType => Some(JdbcType("DATE", java.sql.Types.DATE))
      case DecimalType.Fixed(precision, scale) => Some(JdbcType("DECIMAL(" + precision + "," + scale + ")", java.sql.Types.NUMERIC))
      case DecimalType.Unlimited => Some(JdbcType("DECIMAL(15)", java.sql.Types.NUMERIC))
      case _ => None
    }
  }

    /**
     * Custom mssql dialect.
     */
    case object MSSQLDialect extends JdbcDialect {
      override def canHandle(url: String): Boolean = url.startsWith("jdbc:jtds:sqlserver") ||
        url.startsWith("jdbc:sqlserver") || url.contains("sqlserver")

      override def getJDBCType(dt: DataType): Option[JdbcType] = dt match {
        case StringType => Some(JdbcType("TEXT", java.sql.Types.LONGVARCHAR))
        case BooleanType => Some(JdbcType("BIT", java.sql.Types.BIT))
        case IntegerType => Some(JdbcType("INTEGER", java.sql.Types.INTEGER))
        case LongType => Some(JdbcType("BIGINT", java.sql.Types.BIGINT))
        case DoubleType => Some(JdbcType("FLOAT(32)", java.sql.Types.DOUBLE))
        case FloatType => Some(JdbcType("REAL", java.sql.Types.REAL))
        case ShortType => Some(JdbcType("SMALLINT", java.sql.Types.SMALLINT))
        case ByteType => Some(JdbcType("SMALLINT", java.sql.Types.SMALLINT))
        case BinaryType => Some(JdbcType("VARBINARY(max)", java.sql.Types.VARBINARY))
        case TimestampType => Some(JdbcType("DATETIME", java.sql.Types.TIMESTAMP))
        case DateType => Some(JdbcType("DATETIME", java.sql.Types.TIMESTAMP))
        case DecimalType.Fixed(precision, scale) => Some(JdbcType("NUMERIC(" + precision + "," + scale + ")", java.sql.Types.NUMERIC))
        case DecimalType.Unlimited => Some(JdbcType("NUMERIC(28)", java.sql.Types.NUMERIC))
        case _ => None
      }
    }


