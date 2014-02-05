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
package org.wso2.carbon.bam.datasource.utils;

import java.util.List;

import me.prettyprint.cassandra.model.AllOneConsistencyLevelPolicy;
import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.cassandra.service.ThriftKsDef;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
import me.prettyprint.hector.api.ddl.ComparatorType;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.factory.HFactory;

import org.w3c.dom.Element;
import org.wso2.carbon.bam.datasource.utils.internal.DataSourceUtilsComponent;
import org.wso2.carbon.cassandra.dataaccess.ClusterInformation;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.ndatasource.core.CarbonDataSource;
import org.wso2.carbon.ndatasource.rdbms.RDBMSConfiguration;
import org.wso2.carbon.ndatasource.rdbms.RDBMSDataSourceReader;

/**
 * This class represents utilities for data source related operations.
 */
public class DataSourceUtils {

    private static final int REPLICATION_FACTOR = 3;

    public static RDBMSConfiguration getRDBMSDataSourceConfig(int tenantId, String dataSourceName) {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);
            CarbonDataSource cds = DataSourceUtilsComponent.getCarbonDataSourceService().getDataSource(dataSourceName);
            if (cds == null) {
                throw new RuntimeException("The data source: " + dataSourceName + 
                        " does not exist for tenant: " + PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain());
            }
            Element element = (Element) cds.getDSMInfo().getDefinition().getDsXMLConfiguration();
            RDBMSConfiguration rdbmsConfiguration = RDBMSDataSourceReader.loadConfig(
                    org.wso2.carbon.ndatasource.core.utils.DataSourceUtils.elementToString(element));
            return rdbmsConfiguration;
        } catch (Exception e) {
            throw new RuntimeException("Error in getting data source properties: " + e.getMessage(), e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }
    
    private static boolean isKeyspaceExisting(Cluster cluster, String keyspace) {
        try {
            return (cluster.describeKeyspace(keyspace) != null);
        } catch (Exception e) {
            return false;
        }
    }
    
    private static Keyspace createKeyspaceIfNotExist(Cluster cluster, String ksName) {
        if (!isKeyspaceExisting(cluster, ksName)) {
            KeyspaceDefinition ksdef = HFactory.createKeyspaceDefinition(ksName,
                    ThriftKsDef.DEF_STRATEGY_CLASS, REPLICATION_FACTOR, null);
            cluster.addKeyspace(ksdef, true);
        }
        Keyspace ks = HFactory.createKeyspace(ksName, cluster);
        ks.setConsistencyLevelPolicy(new AllOneConsistencyLevelPolicy());
        return ks;
    }
    
    public static Object[] getClusterKeyspaceFromRDBMSConfig(RDBMSConfiguration config) {
        String[] connectionInfo = decodeCassandraConnectionURL(config.getUrl());
        ClusterInformation clusterInfo = new ClusterInformation(config.getUsername(), config.getPassword());
        clusterInfo.setCassandraHostConfigurator(new CassandraHostConfigurator(connectionInfo[0]));
        Cluster cluster = DataSourceUtilsComponent.getDataAccessService().getCluster(clusterInfo);
        return new Object[] { cluster, createKeyspaceIfNotExist(cluster, connectionInfo[1]) };
    }
    
    public static Object[] getClusterKeyspaceFromRDBMSDataSource(int tenantId, String dataSourceName) {
        return getClusterKeyspaceFromRDBMSConfig(getRDBMSDataSourceConfig(tenantId, dataSourceName));
    }
    
    private static String[] decodeCassandraConnectionURL(String url) {
        try {
            String host = url.substring(url.indexOf("//") + 2, url.lastIndexOf('/'));
            String ks = url.substring(url.lastIndexOf('/') + 1);
            return new String[] { host, ks };
        } catch (Exception e) {
            throw new RuntimeException("Invalid Cassandra URL: " + url);
        }
    }
    
    public static void createColumnFamilyIfNotExist(Cluster cluster, String ksName, 
            String cfName, ComparatorType comparator) {
        try {
            ColumnFamilyDefinition CfDef = HFactory.createColumnFamilyDefinition(
                    ksName, cfName, comparator);            
            if (!isColumnFamilyExisting(cluster, ksName, CfDef)) {
                cluster.addColumnFamily(CfDef);
            }
        } catch (Exception e) {
            String msg = "Could not create column family: '" + ksName + "' " + e.toString();
            throw new RuntimeException(msg, e);
        }           
        
    }
    
    private static boolean isColumnFamilyExisting(Cluster cluster, String ksName, 
            ColumnFamilyDefinition cfDef) {
        try {
            List<ColumnFamilyDefinition> cfDefs = cluster.describeKeyspace(ksName).getCfDefs();
            for (ColumnFamilyDefinition tmpCfDef : cfDefs) {
                if (tmpCfDef.getName().equals(cfDef.getName())) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            String msg = "Error while checking the Column Family existance: '" + 
                    cfDef.getName() + "' " + e.getMessage();
            throw new RuntimeException(msg, e);
        }
    }
    
}
