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

import java.util.Properties
import javax.sql.DataSource

import org.apache.commons.logging.{LogFactory, Log}
import org.apache.spark.Partition
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.catalyst.expressions.Row
import org.apache.spark.sql.jdbc.{JDBCPartition, JDBCPartitioningInfo}
import org.apache.spark.sql.sources._
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, SQLContext}
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils
import org.wso2.carbon.analytics.spark.core.exception.AnalyticsExecutionException

import scala.collection.mutable.ArrayBuffer

object JDBCRelation {
  /**
   * Given a partitioning schematic (a column of integral type, a number of
   * partitions, and upper and lower bounds on the column's value), generate
   * WHERE clauses for each partition so that each row in the table appears
   * exactly once.  The parameters minValue and maxValue are advisory in that
   * incorrect values may cause the partitioning to be poor, but no data
   * will fail to be represented.
   */
  def columnPartition(partitioning: JDBCPartitioningInfo): Array[Partition] = {
    if (partitioning == null) {
      return Array[Partition](JDBCPartition(null, 0))
    }

    val numPartitions = partitioning.numPartitions
    val column = partitioning.column
    if (numPartitions == 1) {
      return Array[Partition](JDBCPartition(null, 0))
    }
    // Overflow and silliness can happen if you subtract then divide.
    // Here we get a little roundoff, but that's (hopefully) OK.
    val stride: Long = (partitioning.upperBound / numPartitions
                        - partitioning.lowerBound / numPartitions)
    var i: Int = 0
    var currentValue: Long = partitioning.lowerBound
    var ans = new ArrayBuffer[Partition]()
    while (i < numPartitions) {
      val lowerBound = if (i != 0) {
        s"$column >= $currentValue"
      } else {
        null
      }
      currentValue += stride
      val upperBound = if (i != numPartitions - 1) {
        s"$column < $currentValue"
      } else {
        null
      }
      val whereClause =
        if (upperBound == null) {
          lowerBound
        } else if (lowerBound == null) {
          upperBound
        } else {
          s"$lowerBound AND $upperBound"
        }
      ans += JDBCPartition(whereClause, i)
      i = i + 1
    }
    ans.toArray
  }
}

class AnalyticsJDBCRelationProvider extends RelationProvider {
  /** Returns a new base relation with the given parameters. */
  override def createRelation(
                               sqlContext: SQLContext,
                               parameters: Map[String, String]): BaseRelation = {
    val dataSource = parameters.getOrElse("dataSource", sys.error("Option 'dataSource' not specified"))
    val tableName = parameters.getOrElse("tableName", sys.error("Option 'tableName' not specified"))
    val partitionColumn = parameters.getOrElse("partitionColumn", null)
    val lowerBound = parameters.getOrElse("lowerBound", null)
    val upperBound = parameters.getOrElse("upperBound", null)
    val numPartitions = parameters.getOrElse("numPartitions", null)

    if (partitionColumn != null
        && (lowerBound == null || upperBound == null || numPartitions == null)) {
      sys.error("Partitioning incompletely specified")
    }

    val partitionInfo = if (partitionColumn == null) {
      null
    } else {
      JDBCPartitioningInfo(
        partitionColumn,
        lowerBound.toLong,
        upperBound.toLong,
        numPartitions.toInt)
    }
    val parts = JDBCRelation.columnPartition(partitionInfo)
    val properties = new Properties() // Additional properties that we will pass to getConnection
    parameters.foreach(kv => properties.setProperty(kv._1, kv._2))
    JDBCRelation(dataSource, tableName, parts)(sqlContext)
  }
}

case class JDBCRelation(
                         dataSource: String,
                         tableName: String,
                         parts: Array[Partition])
                       (@transient val sqlContext: SQLContext)
  extends BaseRelation
          with PrunedFilteredScan
          with InsertableRelation {

  override val needConversion: Boolean = false

  private final val log: Log = LogFactory.getLog(classOf[JDBCRelation])

  override val schema: StructType = {
    try {
      JDBCRDDCarbonUtils.resolveTable(dataSource, tableName)
    }
    catch {
      case e: Exception =>
        log.error(e.getMessage, e)
        throw new RuntimeException(e.getMessage, e)
    }
  }

  override def buildScan(requiredColumns: Array[String], filters: Array[Filter]): RDD[Row] = {
    try {
      JDBCRDDCarbonUtils.scanTable(
        sqlContext.sparkContext,
        schema,
        dataSource,
        tableName,
        requiredColumns,
        filters,
        parts)
    }
    catch {
      case e: Exception =>
        log.error(e.getMessage, e)
        throw new RuntimeException(e.getMessage, e)
    }
  }

  override def insert(data: DataFrame, overwrite: Boolean): Unit = {
    try {
      val conn = GenericUtils.loadGlobalDataSource(dataSource).asInstanceOf[DataSource].getConnection
      conn.setAutoCommit(false)

      try {
        var tableExists = JdbcUtils.tableExists(conn, tableName)

        if (overwrite && tableExists) {
          JdbcUtils.dropTable(conn, tableName)
          tableExists = false
        }

        // Create the table if the table didn't exist.
        if (!tableExists) {
          val schema = JDBCWriteDetails.schemaString(data, conn.getMetaData.getURL)
          val sql = s"CREATE TABLE $tableName ($schema)"
          conn.prepareStatement(sql).executeUpdate()
          conn.commit()
        }
      } finally {
        conn.close()
      }

      JDBCWriteDetails.saveTable(data, dataSource, tableName)
    }
    catch {
      case e: Exception =>
        log.error(e.getMessage, e)
        throw new RuntimeException(e.getMessage, e)
    }
  }
}
