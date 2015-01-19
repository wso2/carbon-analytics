package org.wso2.carbon.analytics.spark.sources

import org.apache.spark.sql._
import org.wso2.carbon.analytics.spark.util.CarbonSparkConstants._

case class DefineSchema(schemaString: String)(){

  /**
   *  Generates the schema based on the string of schema
   */
  def getSchema =
    StructType(
      schemaString.split(";").map(fieldName  => StructField(fieldName.trim.split(" ")(0),
                                                            resolveType(fieldName.trim.split(" ")(1)), true)))

  def resolveType(typeString: String): DataType =
    typeString.toLowerCase match {
      case STRING_TYPE => StringType
      case INT_TYPE => IntegerType
      case DOUBLE_TYPE => DoubleType
      case FLOAT_TYPE => FloatType
      case BOOLEAN_TYPE => BooleanType

      case other => sys.error(s"Unsupported type $other")
     }

}