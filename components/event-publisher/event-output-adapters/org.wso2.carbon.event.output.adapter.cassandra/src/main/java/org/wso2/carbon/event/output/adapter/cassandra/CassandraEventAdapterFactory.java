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


        // set cluster name
        Property clusterName = new Property(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_CLUSTER_NAME);
        clusterName.setDisplayName(
                resourceBundle.getString(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_CLUSTER_NAME));
        clusterName.setRequired(true);
        clusterName.setHint(resourceBundle.getString(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_CLUSTER_NAME_HINT));
        propertyList.add(clusterName);

        // set user name
        Property userName = new Property(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_USER_NAME);
        userName.setDisplayName(
                resourceBundle.getString(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_USER_NAME));
        userName.setRequired(true);
        propertyList.add(userName);

        // set password
        Property password = new Property(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_PASSWORD);
        password.setDisplayName(
                resourceBundle.getString(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_PASSWORD));
        password.setRequired(true);
        password.setSecured(true);
        propertyList.add(password);

        // set host name
        Property hostName = new Property(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_HOSTNAME);
        hostName.setDisplayName(
                resourceBundle.getString(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_HOSTNAME));
        hostName.setRequired(true);
        propertyList.add(hostName);

        // set index all columns
        Property indexAllColumns = new Property(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_INDEX_ALL_COLUMNS);
        indexAllColumns.setDisplayName(
                resourceBundle.getString(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_INDEX_ALL_COLUMNS));
        indexAllColumns.setOptions(new String[]{"true", "false"});
        indexAllColumns.setDefaultValue("false");
        indexAllColumns.setHint(resourceBundle.getString(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_INDEX_ALL_COLUMNS_HINT));
        propertyList.add(indexAllColumns);

        // set port
        Property port = new Property(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_PORT);
        port.setDisplayName(
                resourceBundle.getString(CassandraEventAdapterConstants.ADAPTER_CASSANDRA_PORT));
        port.setRequired(true);
        propertyList.add(port);

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

        return propertyList;
    }

    @Override
    public List<Property> getDynamicPropertyList() {
        return null;
    }

    @Override
    public OutputEventAdapter createEventAdapter(OutputEventAdapterConfiguration eventAdapterConfiguration, Map<String,
            String> globalProperties) {
        return new CassandraEventAdapter(eventAdapterConfiguration, globalProperties);
    }

}
