/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.analytics.datasource.cassandra;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.RecordGroup;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsTableNotAvailableException;
import org.wso2.carbon.analytics.datasource.core.rs.AnalyticsRecordStore;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

/**
 * This class represents the Cassandra implementation of {@link AnalyticsRecordStore}.
 */
public class CassandraAnalyticsRecordStore implements AnalyticsRecordStore {

    private Session session;
    
    @Override
    public void init(Map<String, String> properties) throws AnalyticsException {
        String servers = properties.get(CassandraUtils.CASSANDRA_SERVERS);
        if (servers == null) {
            throw new AnalyticsException("The Cassandra connector property '" + CassandraUtils.CASSANDRA_SERVERS + "' is mandatory");
        }
        Cluster cluster = Cluster.builder().addContactPoints(servers.split(",")).build();
        this.session = cluster.connect();
        this.session.execute("CREATE KEYSPACE IF NOT EXISTS ARS WITH REPLICATION = "
                + "{'class':'SimpleStrategy', 'replication_factor':3};");
        this.session.execute("CREATE TABLE IF NOT EXISTS ARS.META (tenantId INT, "
                + "tableName VARCHAR, tableSchema BLOB, PRIMARY KEY (tenantId, tableName))");
    }
    
    private String generateTargetDataTableName(int tenantId, String tableName) {
        return "DATA_" + tenantId + "_" + GenericUtils.normalizeTableName(tableName);
    }
    
    private String generateTargetTSTableName(int tenantId, String tableName) {
        return "TS_" + tenantId + "_" + GenericUtils.normalizeTableName(tableName);
    }

    @Override
    public void createTable(int tenantId, String tableName) throws AnalyticsException {
        String tsTable = this.generateTargetTSTableName(tenantId, tableName);
        String dataTable = this.generateTargetDataTableName(tenantId, tableName);
        this.session.execute("CREATE TABLE IF NOT EXISTS ARS." + tsTable +
                " (timestamp BIGINT, id VARCHAR, PRIMARY KEY (timestamp, id))");
        this.session.execute("CREATE TABLE IF NOT EXISTS ARS." + dataTable +
                " (id VARCHAR, timestamp BIGINT, data MAP<VARCHAR, BLOB>, PRIMARY KEY (id))");
        this.session.execute("INSERT INTO ARS.META (tenantId, tableName) VALUES (?, ?)", 
                tenantId, GenericUtils.normalizeTableName(tableName));
    }

    @Override
    public void delete(int tenantId, String tableName, long timeFrom, long timeTo) 
            throws AnalyticsException, AnalyticsTableNotAvailableException {
        
    }

    @Override
    public void delete(int tenantId, String tableName, List<String> ids) 
            throws AnalyticsException, AnalyticsTableNotAvailableException {
    }

    @Override
    public void deleteTable(int tenantId, String tableName) throws AnalyticsException {
        this.session.execute("DELETE FROM ARS.META WHERE tenantId = ? AND tableName = ?", tenantId, tableName);
        String tsTable = this.generateTargetTSTableName(tenantId, tableName);
        String dataTable = this.generateTargetDataTableName(tenantId, tableName);
        this.session.execute("DROP TABLE IF EXISTS ARS." + dataTable);
        this.session.execute("DROP TABLE IF EXISTS ARS." + tsTable);
    }

    @Override
    public void destroy() throws AnalyticsException {
        if (this.session != null) {
            this.session.close();
        }
    }

    @Override
    public RecordGroup[] get(int tenantId, String tableName, int numPartitionsHint, List<String> columns, long timeFrom, 
            long timeTo, int recordsFrom, int recordsCount) 
            throws AnalyticsException, AnalyticsTableNotAvailableException {
        return null;
    }

    @Override
    public RecordGroup[] get(int tenantId, String tableName, int numPartitionsHint, List<String> columns, 
            List<String> ids) throws AnalyticsException, AnalyticsTableNotAvailableException {
        return null;
    }
    
    @Override
    public Iterator<Record> readRecords(RecordGroup recordGroup) throws AnalyticsException {
        return null;
    }

    @Override
    public long getRecordCount(int tenantId, String tableName, long timeFrom, long timeTo) 
            throws AnalyticsException, AnalyticsTableNotAvailableException {
        return -1;
    }

    @Override
    public AnalyticsSchema getTableSchema(int tenantId, String tableName) throws AnalyticsTableNotAvailableException,
            AnalyticsException {
        ResultSet rs = this.session.execute("SELECT tableSchema FROM ARS.META WHERE tenantId = ? AND tableName = ?", 
                tenantId, GenericUtils.normalizeTableName(tableName));
        Row row = rs.one();
        if (row == null) {
            throw new AnalyticsTableNotAvailableException(tenantId, tableName);
        }
        ByteBuffer byteBuffer = row.getBytes(0);
        if (byteBuffer == null || byteBuffer.remaining() == 0) {
            return new AnalyticsSchema();
        }
        byte[] data = new byte[byteBuffer.remaining()];
        byteBuffer.get(data);
        return (AnalyticsSchema) GenericUtils.deserializeObject(data);
    }

    @Override
    public boolean isPaginationSupported() {
        return false;
    }

    @Override
    public List<String> listTables(int tenantId) throws AnalyticsException {
        ResultSet rs = this.session.execute("SELECT tableName FROM ARS.META WHERE tenantId = ?", tenantId);
        List<String> result = new ArrayList<String>();
        Iterator<Row> itr = rs.iterator();
        Row row;
        while (itr.hasNext()) {
            row = itr.next();
            result.add(row.getString(0));
        }
        return result;
    }

    @Override
    public void put(List<Record> records) throws AnalyticsException, AnalyticsTableNotAvailableException {        
    }

    @Override
    public void setTableSchema(int tenantId, String tableName, AnalyticsSchema schema) throws AnalyticsTableNotAvailableException,
            AnalyticsException {
        try {
            this.session.execute("UPDATE ARS.META SET tableSchema = ? WHERE tenantId = ? AND tableName = ?", 
                ByteBuffer.wrap(GenericUtils.serializeObject(schema)), tenantId, tableName);
        } catch (Exception e) {
            if (!tableExists(tenantId, tableName)) {
                throw new AnalyticsTableNotAvailableException(tenantId, tableName);
            } else {
                throw new AnalyticsException("Error in setting table schema: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public boolean tableExists(int tenantId, String tableName) throws AnalyticsException {
        ResultSet rs = this.session.execute("SELECT tableSchema FROM ARS.META WHERE tenantId = ? AND tableName = ?", 
                tenantId, tableName);
        return rs.iterator().hasNext();
    }
    
    public static void main(String[] args) throws Exception {
        AnalyticsRecordStore x = new CassandraAnalyticsRecordStore();
        Map<String, String> props = new HashMap<String, String>();
        props.put("servers", "localhost");
        x.init(props);
        System.out.println("Start..");
        
        x.createTable(1, "T1");
        x.createTable(1, "T2");
        x.createTable(1, "T3");
        x.createTable(2, "TX");
        x.setTableSchema(1, "T2", new AnalyticsSchema());
        System.out.println("A:" + x.getTableSchema(1, "T2"));
        x.deleteTable(1, "T3");
        System.out.println("B:" + x.tableExists(1, "T3"));
        System.out.println("X:" + x.listTables(1));
        System.out.println("Y:" + x.listTables(2));
        System.out.println("End.");
        System.exit(0);
    }

}
