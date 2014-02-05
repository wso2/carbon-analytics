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
package org.wso2.carbon.bam.notification.task;

import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.beans.OrderedRows;
import me.prettyprint.hector.api.beans.Row;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.QueryResult;
import me.prettyprint.hector.api.query.RangeSlicesQuery;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.datasource.utils.DataSourceUtils;
import org.wso2.carbon.bam.notification.task.internal.NotificationDispatchComponent;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.databridge.agent.thrift.Agent;
import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.databridge.agent.thrift.conf.AgentConfiguration;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.thrift.utils.HostAddressFinder;
import org.wso2.carbon.event.stream.manager.core.EventStreamService;
import org.wso2.carbon.ndatasource.rdbms.RDBMSConfiguration;
import org.wso2.carbon.ntask.core.AbstractTask;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.SocketException;
import java.util.*;

/**
 * BAM notification message dispatcher task implementation.
 */
public class NotificationDispatchTask extends AbstractTask {

    private static final String DATA_BRIDGE_CONFIG_XML = "data-bridge-config.xml";

    private static final String DATA_BRIDGE_DIR = "data-bridge";

    private static final String STREAM_ID = "streamId";

    public static final String BAM_NOTIFICATION_CF = "bam_notification_messages";

    public static final String TASK_TYPE = "BAM_NOTIFICATION_DISPATCHER_TASK";
    
    public static final String TASK_NAME = "NOTIFIER";
    
    public static final int TASK_INTERVAL = 5000;
    
    public static final String BAM_CASSANDRA_UTIL_DATASOURCE = "WSO2BAM_UTIL_DATASOURCE";
    
    private static StringSerializer STRING_SERIALIZER = new StringSerializer();
    
    private static DataPublisher publisher;
    
    private static Keyspace keyspace;
    
    private static final Log log = LogFactory.getLog(NotificationDispatchTask.class);
    
    private static Map<String, StreamDefinition> streamDefs = new HashMap<String, StreamDefinition>();
    
    private static Set<String> streamsDefinedInDataBridge = new HashSet<String>();
    
    private void initPublisherKS() throws Exception {
        if (publisher == null) {
            Agent agent = new Agent(new AgentConfiguration());
            String[] urls = this.getAgentURLs();
            String[] creds = this.getCredentials();
            String username = creds[0];
            String password = creds[1];
            publisher = new DataPublisher(urls[0], urls[1], username, password, agent);
        }
        keyspace = (Keyspace) DataSourceUtils.getClusterKeyspaceFromRDBMSDataSource(
                MultitenantConstants.SUPER_TENANT_ID, BAM_CASSANDRA_UTIL_DATASOURCE)[1];
        if (keyspace == null) {
            log.warn("Unable to retrieve keyspace in initializing NotificationDispatchTask");
        }
    }
    
    private OMElement loadDataBridgeConfig() throws Exception {
        String path = CarbonUtils.getCarbonConfigDirPath() + File.separator + DATA_BRIDGE_DIR + File.separator + DATA_BRIDGE_CONFIG_XML;
        try {
            BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(new File(path)));
            XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            OMElement omElement = builder.getDocumentElement();
            omElement.build();
            return omElement;
        } catch (Exception e) {
            log.error("Error in reading data bridge configuration: " + e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * @return [0] - Secure, [1] - Non-Secure
     */
    private int[] loadThriftReceiverPorts() {
        try {
            OMElement config = this.loadDataBridgeConfig();
            OMElement thriftDR = (OMElement) config.getChildrenWithLocalName("thriftDataReceiver").next();
            OMElement securePort = (OMElement) thriftDR.getChildrenWithLocalName("securePort").next();
            OMElement nsPort = (OMElement) thriftDR.getChildrenWithLocalName("port").next();            
            return new int[] { Integer.parseInt(securePort.getText()), Integer.parseInt(nsPort.getText()) };
        } catch (Exception e) {
            return new int[] { 7611, 7711 };
        }
    }

    /**
     * Returns the hostName mentioned in data-bridge-config.xml
     * @return  String hostName that Thrift receiver is bound to
     */
    private String getThriftReceiverHostName() {
        String receiverHost = null;
        try {
            OMElement config = this.loadDataBridgeConfig();
            OMElement thriftDR = (OMElement) config.getChildrenWithLocalName("thriftDataReceiver").next();
            OMElement hostName = (OMElement) thriftDR.getChildrenWithLocalName("hostName").next();
            if (hostName != null) {
                receiverHost = hostName.getText();
            }
        } catch (Exception e) {
            try {
                receiverHost = HostAddressFinder.findAddress("localhost");
            } catch (SocketException e1) {
                //fallback into ultimate old-school method
                log.warn("Reading hostName from data-bridge-config.xml failed. Using system property carbon.local.ip");
                receiverHost = System.getProperty("carbon.local.ip");
            }
        }
        return receiverHost;
    }
    
    /**
     * @return [0] - Secure URL, [1] - Non-Secure URL
     */
    private String[] getAgentURLs() {
        String host = getThriftReceiverHostName();
        String portOffsetStr = System.getProperty("portOffset");
        int[] ports = this.loadThriftReceiverPorts();
        int securePort = ports[0];
        int nsPort = ports[1];
        if (portOffsetStr != null) {
            int offset = Integer.parseInt(portOffsetStr);
            securePort += offset;
            nsPort += offset;
        } 
        String secureURL = "ssl://" + host + ":" + securePort;
        String nsURL = "tcp://" + host + ":" + nsPort;
        return new String[] { secureURL, nsURL };
    }
    
    private String[] getCredentials() throws Exception {
        RDBMSConfiguration config = DataSourceUtils.getRDBMSDataSourceConfig(
                MultitenantConstants.SUPER_TENANT_ID, 
                BAM_CASSANDRA_UTIL_DATASOURCE);
        if (config == null) {
            throw new Exception("The WSO2 BAM Util Cassandra Data Source is not available");
        }
        return new String[] { config.getUsername(), config.getPassword() };
    }
    
    private static StreamDefinition getStreamDef(String streamId) throws Exception {
        StreamDefinition streamDef = streamDefs.get(streamId);
        if (streamDef == null) {
            EventStreamService esService = NotificationDispatchComponent.getEventStreamService();
            streamDef = esService.getStreamDefinitionFromStore(streamId,
                    MultitenantConstants.SUPER_TENANT_ID);
            if (streamDef != null) {
                streamDefs.put(streamId, streamDef);
            }
        }
        return streamDef;
    }
    
    @Override
    public void execute() {
        try {
            if (publisher == null || keyspace == null) {
                this.initPublisherKS();
            }
            if (publisher == null || keyspace == null) {
                return;
            }
            this.processNotificationRecords();
        } catch (Exception e) {
            log.error("Error executing notification dispatch task: " + e.getMessage(), e);
        }
    }
    
    private void processNotificationRecords() throws Exception {
        RangeSlicesQuery<String, String, String> sliceQuery = HFactory.createRangeSlicesQuery(
                keyspace, STRING_SERIALIZER, STRING_SERIALIZER, STRING_SERIALIZER);
        sliceQuery.setColumnFamily(BAM_NOTIFICATION_CF);
        sliceQuery.setKeys("", "");
        sliceQuery.setRange(null, null, false, Integer.MAX_VALUE);
        QueryResult<OrderedRows<String, String, String>> result  = sliceQuery.execute();
        OrderedRows<String, String, String> rows = result.get();
        Record record;
        for (Row<String, String, String> row : rows.getList()) {
            record = this.recordFromRow(row);
            if (record != null) {
                this.publishData(record);
            }
            this.deleteRow(row.getKey());
        }
    }
    
    private void deleteRow(String key) {
        /* ok so, lets optimize this later */
        Mutator<String> mutator = HFactory.createMutator(keyspace, STRING_SERIALIZER);
        mutator.addDeletion(key, BAM_NOTIFICATION_CF);
        mutator.execute();
    }
    
    private Record recordFromRow(Row<String, String, String> row) {
        HColumn<String, String> streamIdColumn = row.getColumnSlice().getColumnByName(STREAM_ID);
        if (streamIdColumn == null) {
            return null;
        }
        String streamId = streamIdColumn.getValue();
        Map<String, String> data = new HashMap<String, String>();
        for (HColumn<String, String> col : row.getColumnSlice().getColumns()) {
            if (!col.getName().equals(STREAM_ID)) {
                data.put(col.getName(), col.getValue());
            }
        }
        Record record = new Record(streamId, data);
        if (record.getData() == null) {
            return null;
        }
        return record;
    }
    
    private void defineStream(String streamId) throws Exception {
        StreamDefinition streamDef = getStreamDef(streamId);
        if (publisher.findStreamId(streamDef.getName(), streamDef.getVersion()) != null) {
            return;
        }
        publisher.defineStream(streamDef);
    }
    
    private synchronized void publishData(Record record) throws Exception {
        Event event = new Event();
        event.setStreamId(record.getStreamId());
        event.setPayloadData(record.getData());
        event.setTimeStamp(System.currentTimeMillis());
        if (!streamsDefinedInDataBridge.contains(record.getStreamId())) {
            this.defineStream(record.getStreamId());
            streamsDefinedInDataBridge.add(record.getStreamId());
        }
        publisher.publish(event);
    }

    public static class Record {
        
        private String streamId;
        
        private Object[] data;
        
        public Record(String streamId, Map<String, String> data) {
            this.streamId = streamId;
            this.data = this.convertStreamData(streamId, data);
        }
        
        public String getStreamId() {
            return streamId;
        }

        public Object[] getData() {
            return data;
        }
        
        private Object[] convertStreamData(String streamId, Map<String, String> data) {
            try {
                StreamDefinition streamDef = getStreamDef(streamId);
                if (streamDef == null) {
                    return null;
                }
                List<Object> result = new ArrayList<Object>();
                String value;
                for (Attribute attr : streamDef.getPayloadData()) {
                    value = data.get(attr.getName());
                    result.add(this.convert(value, attr.getType()));
                }
                return result.toArray();
            } catch (Exception e) {
                return null;
            }
        }
        
        private Object convert(String value, AttributeType type) {
            if (value == null) {
                return null;
            }
            switch (type) {
            case BOOL:
                return Boolean.parseBoolean(value);
            case DOUBLE:
                return Double.parseDouble(value);
            case FLOAT:
                return Float.parseFloat(value);
            case INT:
                return Integer.parseInt(value);
            case LONG:
                return Long.parseLong(value);
            case STRING:
                return value;
            }
            return value;
        }
        
    }

}
