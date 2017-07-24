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

import java.io.StringWriter
import java.util.Properties
import javax.xml.transform.{TransformerException, TransformerFactory}
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

import org.apache.commons.logging.{Log, LogFactory}
import org.apache.spark.Partition
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.execution.datasources.jdbc.{JDBCPartition, JDBCPartitioningInfo}
import org.apache.spark.sql.sources._
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, Row, SQLContext}
import org.w3c.dom.Node
import org.wso2.carbon.analytics.spark.core.exception.AnalyticsExecutionException
import org.wso2.carbon.analytics.spark.core.sources.AnalyticsDatasourceWrapper
import org.wso2.carbon.context.PrivilegedCarbonContext
import org.wso2.carbon.ndatasource.core.{CarbonDataSource, DataSourceManager}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.util.{Success, Try}

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

/**
  * Class representing the Relation Provider for the Carbon Spark JDBC implementation.
  */
class AnalyticsJDBCRelationProvider extends RelationProvider {

  import CarbonJDBCConstants._

  private final val log: Log = LogFactory.getLog(classOf[AnalyticsJDBCRelationProvider])
  var isTenantRegistryLoaded = false

  override def createRelation(
                               sqlContext: SQLContext,
                               parameters: Map[String, String]): BaseRelation = {

    val dataSource = parameters.getOrElse(DATASOURCE, sys.error("Option 'dataSource' not specified"))
    val tableName = parameters.getOrElse(TABLE_NAME, sys.error("Option 'tableName' not specified"))
    val schemaString = parameters.getOrElse(SCHEMA, sys.error("Option 'schema' not specified"))
    val primaryKeys = parameters.getOrElse(PRIMARY_KEYS, "")
    val partitionColumn = parameters.getOrElse(PARTITION_COLUMN, null)
    val lowerBound = parameters.getOrElse(LOWER_BOUND, null)
    val upperBound = parameters.getOrElse(UPPER_BOUND, null)
    val numPartitions = parameters.getOrElse(NUM_PARTITIONS, null)

    val tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext.getTenantId()
    val dsDefinition = resolveDataSource(tenantId, dataSource).get

    if (partitionColumn != null && (lowerBound == null || upperBound == null || numPartitions == null)) {
      sys.error("Partitioning improperly specified")
    }
    val formattedSchema = CarbonJDBCUtils.constructElementList(schemaString, primaryKeys)
    try {
      CarbonJDBCUtils.checkAndCreateTable(tenantId, dataSource, dsDefinition, tableName, formattedSchema, primaryKeys)
    }
    catch {
      case e: Exception =>
        log.error("Error in checking/creating table " + tableName + " : ", e.getMessage, e)
        throw new RuntimeException(e.getMessage, e)
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
    CarbonJDBCRelation(tenantId, dataSource, dsDefinition, tableName, formattedSchema, primaryKeys, parts)(sqlContext)
  }

  /**
    * This method expands the provided data source to its constituent XML definition.
    *
    * @param tenantId The tenant to whom this datasource belongs to
    * @param dsName   The name of the datasource
    * @return A Scala Try representation of the datasource definition
    */
  private def resolveDataSource(tenantId: Int, dsName: String): Try[String] = {
    import org.wso2.carbon.analytics.datasource.core.internal.{ServiceHolder => DSServiceHolder}
    if (!isTenantRegistryLoaded) {
      initializeTenantDS(tenantId)
    }
    def datasource: Option[CarbonDataSource] = Option(DSServiceHolder.getDataSourceService.getDataSource(dsName))
    if (datasource.isDefined) {
      val xmlConfig = datasource.get.getDSMInfo.getDefinition.getDsXMLConfiguration.asInstanceOf[Node]
      val writer = new StringWriter()
      try {
        val domSource = new DOMSource(xmlConfig)
        val result = new StreamResult(writer)
        TransformerFactory.newInstance().newTransformer().transform(domSource, result)
        Success(writer.toString)
      }
      catch {
        case e: TransformerException =>
          throw new AnalyticsExecutionException(s"Cannot process the definition for specified datasource $dsName", e)
      } finally {
        writer.close()
      }
    } else {
      throw new AnalyticsExecutionException(s"Datasource $dsName not found for tenant $tenantId")
    }
  }

  /**
    * This method forces the initialisation of tenant datasources, if applicable.
    *
    * @param tenantId The tenant whose datasources need to be initialised
    */
  def initializeTenantDS(tenantId: Int): Unit = {
    import org.wso2.carbon.analytics.spark.core.internal.{ServiceHolder => SparkServiceHolder}
    try {
      SparkServiceHolder.getTenantRegistryLoader.loadTenantRegistry(tenantId)
      DataSourceManager.getInstance().initTenant(tenantId)
      isTenantRegistryLoaded = true
    } catch {
      case e: Exception =>
        log.error("Error initializing tenant registry: " + e.getMessage, e)
        throw new AnalyticsExecutionException(e.getMessage, e)
    }
  }
}

case class CarbonJDBCRelation(
                               tenantID: Int,
                               dataSource: String,
                               dsDefinition: String,
                               tableName: String,
                               formattedSchema: mutable.MutableList[(String, String, Boolean, Boolean)],
                               primaryKeys: String,
                               parts: Array[Partition])
                             (@transient val sqlContext: SQLContext)
  extends BaseRelation
    with PrunedFilteredScan
    with InsertableRelation {

  private final val log: Log = LogFactory.getLog(classOf[CarbonJDBCRelation])

  override val needConversion: Boolean = false

  override val schema: StructType = {
    try {
      CarbonJDBCUtils.resolveSchema(formattedSchema)
    }
    catch {
      case e: Exception =>
        log.error(e.getMessage, e)
        throw new RuntimeException(e.getMessage, e)
    }
  }

  override def buildScan(requiredColumns: Array[String], filters: Array[Filter]): RDD[Row] = {
    try {
      CarbonJDBCScan.scanTable(
        sqlContext.sparkContext,
        schema,
        tenantID,
        dataSource,
        dsDefinition,
        tableName,
        requiredColumns,
        filters,
        parts).asInstanceOf[RDD[Row]]
    }
    catch {
      case e: Exception =>
        log.error(e.getMessage, e)
        throw new RuntimeException(e.getMessage, e)
    }
  }

  override def insert(data: DataFrame, overwrite: Boolean): Unit = {
    try {
      val dsWrapper = new AnalyticsDatasourceWrapper(tenantID, dataSource, dsDefinition)
      val conn = dsWrapper.getConnection
      conn.setAutoCommit(false)
      try {
        if (overwrite) {
          CarbonJDBCUtils.truncateTable(conn, tableName)
        }
      } finally {
        conn.close()
      }
      CarbonJDBCWrite.saveTable(data, tenantID, dataSource, dsDefinition, tableName, primaryKeys)
    }
    catch {
      case e: Exception =>
        log.error(e.getMessage, e)
        throw new RuntimeException(e.getMessage, e)
    }
  }
}
