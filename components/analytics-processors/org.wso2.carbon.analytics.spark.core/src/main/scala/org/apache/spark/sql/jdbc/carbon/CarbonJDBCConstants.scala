package org.apache.spark.sql.jdbc.carbon

object CarbonJDBCConstants {

  val TABLE_NAME_PLACEHOLDER = "{{TABLE_NAME}}"
  val COLUMNS_KEYS_PLACEHOLDER = "{{COLUMNS, PRIMARY_KEYS}}"
  val INDICES_PLACEHOLDER = "{{INDEX_COLUMNS}}"
  val COLUMNS_PLACEHOLDER = "{{COLUMN}}"
  val Q_PLACEHOLDER = "{{Q}}"
  val COLUMN_EQUALS_VALUES_COLUMN_PLACEHOLDER = "{{COLUMN=VALUES(COLUMN) - KEY}}"
  val D_COLUMN_EQUALS_S_COLUMN_PLACEHOLDER = "{{D.COLUMN=S.COLUMN - KEY}}"
  val Q_COLUMN_PLACEHOLDER = "{{Q COLUMN}}"
  val S_COLUMN_PLACEHOLDER = "{{S.COLUMN}}"
  val COLUMN_EQUALS_S_COLUMN_PLACEHOLDER = "{{COLUMN=S.COLUMN}}"
  val D_KEY_EQUALS_S_KEY_PLACEHOLDER = "{{D.KEY=S.KEY}}"
  val KEYS_PLACEHOLDER = "{{KEYS}}"
  val COLUMN_EQUALS_EXCLUDED_COLUMN_PLACEHOLDER = "{{COLUMN=EXCLUDED.COLUMN - KEY}}"

  val DATASOURCE = "dataSource"
  val TABLE_NAME = "tableName"
  val SCHEMA = "schema"
  val PRIMARY_KEYS = "primaryKeys"
  val PARTITION_COLUMN = "partitionColumn"
  val LOWER_BOUND = "lowerBound"
  val UPPER_BOUND = "upperBound"
  val NUM_PARTITIONS = "numPartitions"

  val PRIMARY_KEY_DEF = "PRIMARY KEY"
  val NOT_NULL_DEF = "NOT NULL"

  val SPARK_BINARY_TYPE = "binary"
  val SPARK_BOOLEAN_TYPE = "boolean"
  val SPARK_BYTE_TYPE = "byte"
  val SPARK_DATE_TYPE = "date"
  val SPARK_DOUBLE_TYPE = "double"
  val SPARK_FLOAT_TYPE = "float"
  val SPARK_INTEGER_TYPE = "integer"
  val SPARK_LONG_TYPE = "long"
  val SPARK_NULL_TYPE = "null"
  val SPARK_SHORT_TYPE = "short"
  val SPARK_STRING_TYPE = "string"
  val SPARK_TIMESTAMP_TYPE = "timestamp"

  val SRC_STRING = "src."
  val DEST_STRING = "dest."
  val VALUES_STRING = "VALUES"
  val EXCLUDED_STRING = "EXCLUDED."

  val EMPTY_STRING = ""
  val WHITESPACE = " "
  val QUESTION_MARK = "?"
  val EQUALS_SIGN = "="
  val SEPARATOR = ", "
  val AND_SEPARATOR = " AND "
  val OPEN_PARENTHESIS = "("
  val CLOSE_PARENTHESIS = ")"

}
