/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.event.output.adaptor.hbase;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.wso2.carbon.event.output.adaptor.core.AbstractOutputEventAdaptor;
import org.wso2.carbon.event.output.adaptor.core.MessageType;
import org.wso2.carbon.event.output.adaptor.core.Property;
import org.wso2.carbon.event.output.adaptor.core.config.OutputEventAdaptorConfiguration;
import org.wso2.carbon.event.output.adaptor.core.exception.OutputEventAdaptorEventProcessingException;
import org.wso2.carbon.event.output.adaptor.core.message.config.OutputEventAdaptorMessageConfiguration;
import org.wso2.carbon.event.output.adaptor.hbase.internal.util.HbaseEventAdaptorConstants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

public final class HbaseEventAdaptorType extends AbstractOutputEventAdaptor {

    private static final Log log = LogFactory.getLog(HbaseEventAdaptorType.class);

    private static HbaseEventAdaptorType hbaseEventAdaptor = new HbaseEventAdaptorType();
    private ResourceBundle resourceBundle;
    private ConcurrentHashMap<Integer, ConcurrentHashMap<String, Configuration>> hbaseConfigurationMap = new ConcurrentHashMap<Integer, ConcurrentHashMap<String, Configuration>>();

    private HbaseEventAdaptorType() {

    }

    @Override
    protected List<String> getSupportedOutputMessageTypes() {
        List<String> supportOutputMessageTypes = new ArrayList<String>();
        supportOutputMessageTypes.add(MessageType.MAP);
        return supportOutputMessageTypes;
    }

    /**
     * @return hbase event adaptor instance
     */
    public static HbaseEventAdaptorType getInstance() {

        return hbaseEventAdaptor;
    }

    /**
     * @return name of the hbase event adaptor
     */
    @Override
    protected String getName() {
        return HbaseEventAdaptorConstants.ADAPTOR_TYPE_HBASE;
    }

    /**
     * Initialises the resource bundle
     */
    @Override
    protected void init() {
        resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.event.output.adaptor.hbase.i18n.Resources", Locale.getDefault());
    }


    /**
     * @return output adaptor configuration property list
     */
    @Override
    public List<Property> getOutputAdaptorProperties() {

        List<Property> propertyList = new ArrayList<Property>();

        // set cluster name
        Property clusterName = new Property(HbaseEventAdaptorConstants.ADAPTOR_HBASE_CONF_PATH);
        clusterName.setDisplayName(
                resourceBundle.getString(HbaseEventAdaptorConstants.ADAPTOR_HBASE_CONF_PATH));
        clusterName.setRequired(true);
        clusterName.setHint(resourceBundle.getString(HbaseEventAdaptorConstants.ADAPTOR_HBASE_CONF_PATH_HINT));
        propertyList.add(clusterName);

        return propertyList;

    }

    /**
     * @return output message configuration property list
     */
    @Override
    public List<Property> getOutputMessageProperties() {

        List<Property> propertyList = new ArrayList<Property>();

        // key space
        Property tableName = new Property(HbaseEventAdaptorConstants.ADAPTOR_HBASE_TABLE_NAME);
        tableName.setDisplayName(
                resourceBundle.getString(HbaseEventAdaptorConstants.ADAPTOR_HBASE_TABLE_NAME));
        tableName.setRequired(true);
        propertyList.add(tableName);

//        // column family
//        Property columnFamily = new Property(HbaseEventAdaptorConstants.TRANSPORT_HBASE_COLUMN_FAMILY_NAME);
//        columnFamily.setDisplayName(
//                resourceBundle.getString(HbaseEventAdaptorConstants.TRANSPORT_HBASE_COLUMN_FAMILY_NAME));
//        columnFamily.setRequired(true);
//        propertyList.add(columnFamily);
//
//        // column qualifier
//        Property columnQualifier = new Property(HbaseEventAdaptorConstants.TRANSPORT_HBASE_COLUMN_QUALIFIER);
//        columnQualifier.setDisplayName(
//                resourceBundle.getString(HbaseEventAdaptorConstants.TRANSPORT_HBASE_COLUMN_QUALIFIER));
//        columnQualifier.setRequired(true);
//        propertyList.add(columnQualifier);

        return propertyList;
    }

    /**
     * @param outputEventMessageConfiguration
     *                - topic name to publish messages
     * @param message - is and Object[]{Event, EventDefinition}
     */
    public void publish(
            OutputEventAdaptorMessageConfiguration outputEventMessageConfiguration,
            Object message,
            OutputEventAdaptorConfiguration outputEventAdaptorConfiguration, int tenantId) {

        if (message instanceof Map) {

            Map<String, Object> messageObject = (Map<String, Object>) message;
            ConcurrentHashMap<String, Configuration> hbaseConfigurationCache = hbaseConfigurationMap.get(tenantId);
            if (null == hbaseConfigurationCache) {
                hbaseConfigurationCache = new ConcurrentHashMap<String, Configuration>();
                if (null != hbaseConfigurationMap.putIfAbsent(tenantId, hbaseConfigurationCache)) {
                    hbaseConfigurationCache = hbaseConfigurationMap.get(tenantId);
                }
            }

            Configuration hBaseConfiguration = hbaseConfigurationCache.get(outputEventAdaptorConfiguration.getName());
            if (null == hBaseConfiguration) {
                hBaseConfiguration = HBaseConfiguration.create();
                hBaseConfiguration.addResource(outputEventAdaptorConfiguration.getOutputProperties().get(HbaseEventAdaptorConstants.ADAPTOR_HBASE_CONF_PATH));
                if (null != hbaseConfigurationCache.putIfAbsent(outputEventAdaptorConfiguration.getName(), hBaseConfiguration)) {
                    hBaseConfiguration = hbaseConfigurationCache.get(outputEventAdaptorConfiguration.getName());
                } else {
                    log.info("Initiated HBase Writer " + outputEventAdaptorConfiguration.getName());
                }

            }

            String[] columnNames = messageObject.keySet().toArray(new String[0]);
            String tableName = outputEventMessageConfiguration.getOutputMessageProperties().get(HbaseEventAdaptorConstants.ADAPTOR_HBASE_TABLE_NAME);
            try {
                creatTable(hBaseConfiguration, tableName, columnNames);
                for (Map.Entry<String, Object> stringObjectEntry : messageObject.entrySet()) {
                    Map.Entry pairs = (Map.Entry) stringObjectEntry;
                    addRecord(hBaseConfiguration, tableName, "test", pairs.getKey().toString(), " ", pairs.getValue().toString());
                }

                }catch(Exception e){
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }


        @Override
        public void testConnection (
                OutputEventAdaptorConfiguration outputEventAdaptorConfiguration,
        int tenantId){
            // no test
        }

    private void creatTable(Configuration conf, String tableName, String[] familys)
            throws Exception {
        HBaseAdmin admin = new HBaseAdmin(conf);
        if (admin.tableExists(tableName)) {
            System.out.println("table already exists!");
        } else {
            HTableDescriptor tableDesc = new HTableDescriptor(tableName);
            for (int i = 0; i < familys.length; i++) {
                tableDesc.addFamily(new HColumnDescriptor(familys[i]));
            }
            admin.createTable(tableDesc);
            System.out.println("create table " + tableName + " ok.");
        }
    }

    private void addRecord(Configuration conf, String tableName, String rowKey, String family,
                           String qualifier, String value)
            throws Exception {
        try {
            HTable table = new HTable(conf, tableName);
            Put put = new Put(Bytes.toBytes(rowKey));
            put.add(Bytes.toBytes(family), Bytes.toBytes(qualifier), Bytes.toBytes(value));
            table.put(put);
            System.out.println("insert recored " + rowKey + " to table " + tableName + " ok.");
        } catch (IOException e) {
            throw new OutputEventAdaptorEventProcessingException(e);
        }
    }


}
