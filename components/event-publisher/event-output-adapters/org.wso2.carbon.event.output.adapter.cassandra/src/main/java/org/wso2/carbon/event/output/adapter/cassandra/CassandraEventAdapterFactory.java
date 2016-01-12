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
package org.wso2.carbon.event.output.adapter.cassandra;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.output.adapter.core.*;
import org.wso2.carbon.event.output.adapter.cassandra.internal.util.CassandraEventAdapterConstants;

import java.util.*;

/**
 * The cassandra event adapter factory class to create a cassandra output adapter
 */
public class CassandraEventAdapterFactory extends OutputEventAdapterFactory {
    private static final Log log = LogFactory.getLog(CassandraEventAdapterFactory.class);

    private ResourceBundle resourceBundle =
            ResourceBundle.getBundle("org.wso2.carbon.event.output.adapter.cassandra.i18n.Resources", Locale.getDefault());

    @Override
    public String getType() {
        return CassandraEventAdapterConstants.ADAPTER_TYPE_CASSANDRA;
    }

    @Override
    public List<String> getSupportedMessageFormats() {
        List<String> supportedMessageFormats = new ArrayList<String>();
        supportedMessageFormats.add(MessageType.MAP);
        return supportedMessageFormats;
    }

    @Override
    public List<Property> getStaticPropertyList() {
        List<Property> propertyList = new ArrayList<Property>();

        // set host name
        Property hosts = new Property(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_HOSTS);
        hosts.setDisplayName(
                resourceBundle.getString(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_HOSTS));
        hosts.setRequired(true);
        hosts.setHint(resourceBundle.getString(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_HOSTS_HINT));
        propertyList.add(hosts);

        // set port
        Property port = new Property(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_PORT);
        port.setDisplayName(
                resourceBundle.getString(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_PORT));
        port.setHint(resourceBundle.getString(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_PORT_HINT));
        propertyList.add(port);

        // set user name
        Property userName = new Property(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_USER_NAME);
        userName.setDisplayName(
                resourceBundle.getString(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_USER_NAME));
        propertyList.add(userName);

        // set password
        Property password = new Property(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_PASSWORD);
        password.setDisplayName(
                resourceBundle.getString(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_PASSWORD));
        password.setSecured(true);
        propertyList.add(password);

        // key space
        Property keySpace = new Property(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_KEY_SPACE_NAME);
        keySpace.setDisplayName(
                resourceBundle.getString(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_KEY_SPACE_NAME));
        keySpace.setRequired(true);
        propertyList.add(keySpace);

        // column family
        Property columnFamily = new Property(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_COLUMN_FAMILY_NAME);
        columnFamily.setDisplayName(
                resourceBundle.getString(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_COLUMN_FAMILY_NAME));
        columnFamily.setRequired(true);
        propertyList.add(columnFamily);

        // strategy class
        Property strategyClass = new Property(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_STRATEGY_CLASS);
        strategyClass.setDisplayName(
                resourceBundle.getString(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_STRATEGY_CLASS));
        strategyClass.setHint(resourceBundle.getString(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_STRATEGY_CLASS_HINT));
        propertyList.add(strategyClass);

        // replication factor
        Property replicationFactor = new Property(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_REPLICATION_FACTOR);
        replicationFactor.setDisplayName(
                resourceBundle.getString(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_REPLICATION_FACTOR));
        replicationFactor.setHint(resourceBundle.getString(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_REPLICATION_FACTOR_HINT));
        propertyList.add(replicationFactor);

        // indexed columns
        Property indexedColumns = new Property(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_INDEXED_COLUMNS);
        indexedColumns.setDisplayName(
                resourceBundle.getString(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_INDEXED_COLUMNS));
        indexedColumns.setHint(resourceBundle.getString(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_INDEXED_COLUMNS_HINT));
        propertyList.add(indexedColumns);

        return propertyList;
    }

    @Override
    public List<Property> getDynamicPropertyList() {
        return null;
    }

    @Override
    public String getUsageTips() {
        return null;
    }

    @Override
    public OutputEventAdapter createEventAdapter(OutputEventAdapterConfiguration eventAdapterConfiguration, Map<String,
            String> globalProperties) {
        return new CassandraEventAdapter(eventAdapterConfiguration, globalProperties);
    }

}
