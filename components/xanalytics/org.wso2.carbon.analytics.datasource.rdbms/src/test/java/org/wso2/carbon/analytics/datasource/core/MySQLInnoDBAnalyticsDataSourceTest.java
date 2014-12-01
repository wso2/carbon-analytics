/*
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.analytics.datasource.core;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Parameters;
import org.wso2.carbon.analytics.datasource.core.AnalyticsException;
import org.wso2.carbon.analytics.datasource.rdbms.QueryConfigurationEntry;
import org.wso2.carbon.analytics.datasource.rdbms.RDBMSAnalyticsDataSource;

/**
 * MySQL implementation of analytics data source tests.
 */
public class MySQLInnoDBAnalyticsDataSourceTest extends AnalyticsDataSourceTest {

    @BeforeSuite
    @Parameters({"mysql.url", "mysql.username", "mysql.password"})
    public void setup(String url, String username, 
            String password) throws NamingException, AnalyticsException, SQLException {
        this.initDS(url, username, password);
        RDBMSAnalyticsDataSource ads = new RDBMSAnalyticsDataSource(this.generateQueryConfiguration());
        Map<String, String> props = new HashMap<String, String>();
        props.put("datasource", "DS");
        ads.init(props);
        this.init("MySQLInnoDBAnalyticsDataSource", ads);
    }
    
    private void initDS(String url, String username, String password) throws NamingException, SQLException {
        PoolProperties pps = new PoolProperties();
        pps.setDriverClassName("com.mysql.jdbc.Driver");
        pps.setUrl(url);
        pps.setUsername(username);
        pps.setPassword(password);
        DataSource dsx = new DataSource(pps);
        new InitialContext().bind("DS", dsx);
        this.dropSystemTables(dsx);
    }
    
    private void dropSystemTables(javax.sql.DataSource ds) throws SQLException {
        Connection conn = null;
        try {
            conn = ds.getConnection();
            conn.prepareStatement("DROP TABLE IF EXISTS AN_FS_DATA").executeUpdate();
            conn.prepareStatement("DROP TABLE IF EXISTS AN_FS_PATH").executeUpdate();
            conn.prepareStatement("DROP TABLE IF EXISTS AN_TABLE_RECORD").executeUpdate();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ignore) { } 
            }
        }
    }
    
//    public static void main(String[] args) throws Exception {
//        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/bam3", "root", "root");
//        PreparedStatement stmt;
//        long t1 = System.currentTimeMillis();
//        for (int i = 0; i < 1000; i++) {
//            //stmt = conn.prepareStatement("UPDATE ABC SET length = 5400 WHERE path = '/mydir'");
//            stmt = conn.prepareStatement("INSERT INTO ABC (path,length,ppath) VALUES (?,?,?)");
//            stmt.setString(1, "/mydir/" + i);
//            stmt.setLong(2, 40500);
//            stmt.setString(3, "/");
//            stmt.executeUpdate();
//            stmt.close();
//        }
//        long t2 = System.currentTimeMillis();
//        conn.close();
//        System.out.println("Time: " + (t2 - t1));
//    }
    
    private QueryConfigurationEntry generateQueryConfiguration() {
        QueryConfigurationEntry conf = new QueryConfigurationEntry();
        String[] recordTableInitQueries = new String[2];
        recordTableInitQueries[0] = "CREATE TABLE {{TABLE_NAME}} (record_id VARCHAR(50), timestamp BIGINT, data BLOB, PRIMARY KEY(record_id))";
        recordTableInitQueries[1] = "CREATE INDEX {{TABLE_NAME}}_TIMESTAMP ON AN_TABLE_RECORD(timestamp)";        
        String[] fsTableInitQueries = new String[3];
        fsTableInitQueries[0] = "CREATE TABLE AN_FS_PATH (path VARCHAR(256), is_directory BOOLEAN, length BIGINT, parent_path VARCHAR(256), PRIMARY KEY(path), FOREIGN KEY (parent_path) REFERENCES AN_FS_PATH(path) ON DELETE CASCADE)";
        fsTableInitQueries[1] = "CREATE TABLE AN_FS_DATA (path VARCHAR(256), sequence BIGINT, data BLOB, PRIMARY KEY (path,sequence), FOREIGN KEY (path) REFERENCES AN_FS_PATH(path) ON DELETE CASCADE)";
        fsTableInitQueries[2] = "CREATE INDEX index_parent_id ON AN_FS_PATH(parent_path)";        
        conf.setRecordTableInitQueries(recordTableInitQueries);
        conf.setFsTableInitQueries(fsTableInitQueries);        
        conf.setFsTablesCheckQuery("SELECT record_id FROM AN_FS_PATH WHERE path = '/'");
        conf.setRecordInsertQuery("INSERT INTO {{TABLE_NAME}} (record_id, timestamp, data) VALUES (?, ?, ?)");
        conf.setRecordRetrievalQuery("SELECT record_id, timestamp, data FROM {{TABLE_NAME}} WHERE timestamp >= ? AND timestamp < ? LIMIT ?,?");
        conf.setRecordRetrievalWithIdsQuery("SELECT record_id, timestamp, data FROM {{TABLE_NAME}} WHERE AND record_id IN (:record_ids)");
        conf.setRecordDeletionWithIdsQuery("DELETE FROM {{TABLE_NAME}} WHERE record_id IN (:record_ids)");
        conf.setRecordDeletionQuery("DELETE FROM {{TABLE_NAME}} WHERE timestamp >= ? AND timestamp < ? AND record_id != ?");
        conf.setPaginationFirstZeroIndexed(true);
        conf.setPaginationFirstInclusive(true);
        conf.setPaginationSecondLength(true);
        conf.setFsPathRetrievalQuery("SELECT * FROM AN_FS_PATH WHERE path = ?");
        conf.setFsListFilesQuery("SELECT path FROM AN_FS_PATH WHERE parent_path = ?");
        conf.setFsInsertPathQuery("INSERT INTO AN_FS_PATH (path,is_directory,length,parent_path) VALUES (?,?,?,?)");
        conf.setFsFileLengthRetrievalQuery("SELECT length FROM AN_FS_PATH WHERE path = ?");
        conf.setFsFileLengthRetrievalQuery("SELECT length FROM AN_FS_PATH WHERE path = ?");
        conf.setFsSetFileLengthQuery("UPDATE AN_FS_PATH SET length = ? WHERE path = ?");
        conf.setFsReadDataChunkQuery("SELECT data FROM AN_FS_DATA WHERE path = ? AND sequence = ?");
        conf.setFsWriteDataChunkQuery("INSERT INTO AN_FS_DATA (path,sequence,data) VALUES (?,?,?)");
        conf.setFsUpdateDataChunkQuery("UPDATE AN_FS_DATA SET data = ? WHERE path = ? AND sequence = ?");
        conf.setFsDeletePathQuery("DELETE FROM AN_FS_PATH WHERE path = ?");
        conf.setFsDataChunkSize(10240);
        return conf;
    }
    
}
