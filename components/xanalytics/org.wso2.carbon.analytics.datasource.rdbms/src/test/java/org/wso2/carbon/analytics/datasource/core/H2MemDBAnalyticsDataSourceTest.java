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

import java.util.HashMap;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.testng.annotations.BeforeSuite;
import org.wso2.carbon.analytics.datasource.rdbms.QueryConfiguration;
import org.wso2.carbon.analytics.datasource.rdbms.RDBMSAnalyticsDataSource;

/**
 * H2 implementation of analytics data source tests.
 */
public class H2MemDBAnalyticsDataSourceTest extends AnalyticsDataSourceTest {

    @BeforeSuite
    public void setup() throws NamingException, AnalyticsDataSourceException {
        this.initDS("jdbc:h2:mem:bam_test_db", "wso2carbon", "wso2carbon");
        RDBMSAnalyticsDataSource ads = new RDBMSAnalyticsDataSource(this.generateQueryConfiguration());
        Map<String, String> props = new HashMap<String, String>();
        props.put("datasource", "DS");
        ads.init(props);
        this.init("H2MemDBAnalyticsDataSource", ads);
    }
    
    private void initDS(String url, String username, String password) throws NamingException {
        PoolProperties pps = new PoolProperties();
        pps.setDriverClassName("org.h2.Driver");
        pps.setUrl(url);
        pps.setUsername(username);
        pps.setPassword(password);
        DataSource dsx = new DataSource(pps);
        new InitialContext().bind("DS", dsx);
    }
    
    private QueryConfiguration generateQueryConfiguration() {
        QueryConfiguration conf = new QueryConfiguration();
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
