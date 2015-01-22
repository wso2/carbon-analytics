package org.wso2.carbon.analytics.spark.core.sources

import org.apache.spark.sql._
import org.wso2.carbon.analytics.spark.core.AnalyticsSparkConstants
import AnalyticsSparkConstants._

case class DefineSchema(schemaString: String)(){

  /**
   *  Generates the schema based on the string of schema
   */
  def getSchema =
    StructType(
      schemaString.split(",").map(fieldName  => StructField(fieldName.trim.split(" ")(0),
                                                            resolveType(fieldName.trim.split(" ")(1)), true)))

  def resolveType(typeString: String): DataType =
    typeString.toLowerCase match {
      case STRING_TYPE => StringType
      case INT_TYPE => IntegerType
      case INTEGER_TYPE => IntegerType
      case DOUBLE_TYPE => DoubleType
      case FLOAT_TYPE => FloatType
      case LONG_TYPE => LongType
      case BOOLEAN_TYPE => BooleanType

      case other => sys.error(s"Unsupported type $other")
     }

}