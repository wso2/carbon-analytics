/*
 *  Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.bam.messagestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.ColumnSlice;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.ddl.ComparatorType;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.QueryResult;
import me.prettyprint.hector.api.query.SliceQuery;

import org.wso2.carbon.bam.datasource.utils.DataSourceUtils;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

/**
 * This class represents a persisted message store.
 */
public class MessageStore {

    private static final String PAYLOAD = "payload";

    private static final String TIMESTAMP = "timestamp";

    private static final String BAM_MESSAGE_STORE_CF = "bam_message_store";

    private static final String WSO2BAM_CASSANDRA_DATASOURCE = "WSO2BAM_UTIL_DATASOURCE";

    private Map<String, Message> dataMap = new HashMap<String, Message>();
    
    private static MessageStore instance;
    
    private Keyspace keyspace;
    
    private Cluster cluster;
    
    private static StringSerializer STRING_SERIALIZER = new StringSerializer();
    
    private MessageStore() {
        Object[] objs = DataSourceUtils.getClusterKeyspaceFromRDBMSDataSource(
                MultitenantConstants.SUPER_TENANT_ID, WSO2BAM_CASSANDRA_DATASOURCE);
        this.cluster = (Cluster) objs[0];
        this.keyspace = (Keyspace) objs[1];
        DataSourceUtils.createColumnFamilyIfNotExist(this.getCluster(), 
                this.getKeyspace().getKeyspaceName(), 
                BAM_MESSAGE_STORE_CF, ComparatorType.UTF8TYPE);
    }
    
    public static synchronized MessageStore getInstance() {
        if (instance == null) {
            instance = new MessageStore();
        }
        return instance;
    }
    
    private Keyspace getKeyspace() {
        return keyspace;
    }
    
    private Cluster getCluster() {
        return cluster;
    }
    
    private String generateTenantMessageTypeId(String type) {
        return type + "_" + CarbonContext.getThreadLocalCarbonContext().getTenantId();
    }
    
    public void setData(String type, String data) {
        Mutator<String> mutator = HFactory.createMutator(this.keyspace, STRING_SERIALIZER);
        String id = this.generateTenantMessageTypeId(type);
        mutator.insert(id, BAM_MESSAGE_STORE_CF, HFactory.createStringColumn(
                TIMESTAMP, "" + Calendar.getInstance().getTimeInMillis()));
        mutator.insert(id, BAM_MESSAGE_STORE_CF, HFactory.createStringColumn(PAYLOAD, data));
        mutator.execute();
    }
    
    public Message getData(String type) {
        SliceQuery<String, String, String> sliceQuery = HFactory.createSliceQuery(this.keyspace, 
                STRING_SERIALIZER, STRING_SERIALIZER, STRING_SERIALIZER);
        String id = this.generateTenantMessageTypeId(type);
        sliceQuery.setColumnFamily(BAM_MESSAGE_STORE_CF).setKey(id).setColumnNames(TIMESTAMP, PAYLOAD);
        QueryResult<ColumnSlice<String, String>> result = sliceQuery.execute();
        if (result == null) {
            return null;
        }
        ColumnSlice<String, String> cslice = result.get();
        if (cslice == null) {
            return null;
        }
        HColumn<String, String> tsColumn = cslice.getColumnByName(TIMESTAMP);
        HColumn<String, String> payloadColumn = cslice.getColumnByName(PAYLOAD);
        if (tsColumn == null || payloadColumn == null) {
            return null;
        }
        Message msg = new Message();
        msg.setTimestamp(Long.parseLong(tsColumn.getValue()));
        msg.setPayload(payloadColumn.getValue());
        return msg;
    }
    
    public void clearData(String type) {
        this.dataMap.remove(type);
    }
    
}
