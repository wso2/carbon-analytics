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

import scala.util.Try

/**
 * Util functions for JDBC tables.
 */
@SerialVersionUID(105L)
object JdbcUtils extends java.io.Serializable {

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
    // Somewhat hacky, but there isn't a good way to identify whether a table exists for all
    // SQL database systems, considering "table" could also include the database name.
    Try(conn.prepareStatement(s"SELECT 1 FROM $table LIMIT 1").executeQuery().next()).isSuccess
  }

  /**
   * Drops a table from the JDBC database.
   */
  def dropTable(conn: Connection, table: String): Unit = {
    conn.prepareStatement(s"DROP TABLE $table").executeUpdate()
  }
}
