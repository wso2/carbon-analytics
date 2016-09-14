package org.wso2.carbon.analytics.spark.core.udaf.defaults

import org.apache.spark.sql.Row
import org.apache.spark.sql.expressions.{MutableAggregationBuffer, UserDefinedAggregateFunction}
import org.apache.spark.sql.types._

/**
  * This UDAF could be used to calculate the harmonic mean of the given set of numbers.
  */
class HarmonicMeanUDAF extends UserDefinedAggregateFunction {

  override def inputSchema: org.apache.spark.sql.types.StructType =
    StructType(StructField("value", DoubleType) :: Nil)

  override def bufferSchema: StructType = StructType(
    StructField(
      "count", LongType) :: StructField("harmonicSum", DoubleType) :: StructField("zeroFound", BooleanType) :: Nil
  )

  override def dataType: DataType = DoubleType

  override def deterministic: Boolean = true

  override def initialize(buffer: MutableAggregationBuffer): Unit = {
    buffer(0) = 0L
    buffer(1) = 0.0
    buffer(2) = false
  }

  override def update(buffer: MutableAggregationBuffer, input: Row): Unit = {
    if (input.getAs[Double](0) == 0.toDouble || buffer.getAs[Boolean](2)) {
      buffer(2) = true
    } else {
      buffer(0) = buffer.getAs[Long](0) + 1
      buffer(1) = buffer.getAs[Double](1) + inv(input.getAs[Double](0))
    }
  }

  override def merge(buffer1: MutableAggregationBuffer, buffer2: Row): Unit = {
    if (buffer2.getAs[Double](1) == 0.toDouble || buffer1.getAs[Boolean](2)) {
      buffer1(2) = true
    } else {
      buffer1(0) = buffer1.getAs[Long](0) + buffer2.getAs[Long](0)
      buffer1(1) = buffer1.getAs[Double](1) + buffer2.getAs[Double](1)
    }
  }

  override def evaluate(buffer: Row): Any = {
    if (buffer.getBoolean(2)) {
      0.toDouble
    } else {
      buffer.getLong(0) / buffer.getDouble(1)
    }
  }

  def inv(value: Double): Double = {
    1.toDouble / value
  }

}
