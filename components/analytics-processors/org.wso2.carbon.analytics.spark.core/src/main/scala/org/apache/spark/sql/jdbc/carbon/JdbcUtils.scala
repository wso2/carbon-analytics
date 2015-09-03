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

import java.sql.{Connection, DriverManager}
import java.util.Properties

import org.wso2.carbon.analytics.datasource.rdbms.{RDBMSQueryConfigurationEntry, RDBMSUtils}

import scala.util.Try

/**
 * Util functions for JDBC tables.
 */
object JdbcUtils {

  /**
   * Establishes a JDBC connection.
   */
  def createConnection(url: String, connectionProperties: Properties): Connection = {
    DriverManager.getConnection(url, connectionProperties)
  }

  /**
   * Returns true if the table already exists in the JDBC database.
   */
  def tableExists(conn: Connection, table: String): Boolean = {
    //using the rdbms-query-config.xml to get the table exists query
    val qConfEntry = getQueryConfigEntry(conn)
    val query = qConfEntry.getRecordTableCheckQuery.replace("{{TABLE_NAME}}", table)

    Try(conn.prepareStatement(query).executeQuery().next()).isSuccess
  }

  /**
   * Drops a table from the JDBC database.
   */
  def dropTable(conn: Connection, table: String): Unit = {
    //using the rdbms-query-config.xml to get the table exists query
    val qConfEntry = getQueryConfigEntry(conn)
    val query = {
      val queries = qConfEntry.getRecordTableDeleteQueries
      queries(queries.length - 1).replace("{{TABLE_NAME}}", table)
    }
    conn.prepareStatement(query).executeUpdate()
    conn.commit()
  }

  private def getQueryConfigEntry(conn: Connection): RDBMSQueryConfigurationEntry = {
    val qConf = RDBMSUtils.loadQueryConfiguration()
    val dbType = conn.getMetaData.getDatabaseProductName

    qConf.getDatabases.find(entry => entry.getDatabaseName.equalsIgnoreCase(dbType)).get
  }
}
