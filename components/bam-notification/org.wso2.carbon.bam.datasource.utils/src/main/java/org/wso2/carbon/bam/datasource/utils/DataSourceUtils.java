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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents utilities for data source related operations.
 */
public class DataSourceUtils {

    private static final int REPLICATION_FACTOR = 3;
    private static final String  EXTERNAL_CASSANDRA = "externalCassandra";

    private static final String  PARAM_PROTOCOL = "protocol";
    private static final String  PARAM_HOST = "host";
    private static final String  PARAM_PORT = "port";
    private static final String  PARAM_KS = "keyspace";

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
            checkAndIncrementPortOffset(rdbmsConfiguration);
            return rdbmsConfiguration;
        } catch (Exception e) {
            throw new RuntimeException("Error in getting data source properties: " + e.getMessage(), e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    private static void checkAndIncrementPortOffset(RDBMSConfiguration config) {
        //check if BAM_UTIL_KS connects to external cluster
        String externalCassandraFlag = "false";
        List<RDBMSConfiguration.DataSourceProperty> props = config.getDataSourceProps();
        if(props != null)     {
            while (props.iterator().hasNext())  {
                RDBMSConfiguration.DataSourceProperty property = props.iterator().next();
                if(property.getName().equalsIgnoreCase(EXTERNAL_CASSANDRA)) {
                    externalCassandraFlag = property.getValue();
                    break;
                }
            }
        }
        if(externalCassandraFlag.equalsIgnoreCase("false")) {
            String offsetStr = System.getProperty("portOffset");
            if(offsetStr != null) {
                int portOffset = Integer.parseInt(offsetStr);
                Map<String, String> connectionInfo = decodeCassandraConnectionURL(config.getUrl());

                int port = Integer.parseInt(connectionInfo.get(PARAM_PORT));
                connectionInfo.put(PARAM_PORT,String.valueOf(port + portOffset));
                //finally set the  URL with incremented port
                config.setUrl(encodeCassandraConnectionURL(connectionInfo));
            }
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
        Map<String,String> connectionInfo = decodeCassandraConnectionURL(config.getUrl());
        ClusterInformation clusterInfo = new ClusterInformation(config.getUsername(), config.getPassword());
        clusterInfo.setCassandraHostConfigurator(
                new CassandraHostConfigurator(connectionInfo.get(PARAM_HOST) + ":" + connectionInfo.get(PARAM_PORT)));
        Cluster cluster = DataSourceUtilsComponent.getDataAccessService().getCluster(clusterInfo);
        return new Object[] { cluster, createKeyspaceIfNotExist(cluster, connectionInfo.get(PARAM_KS)) };
    }

    public static Object[] getClusterKeyspaceFromRDBMSDataSource(int tenantId, String dataSourceName) {
        return getClusterKeyspaceFromRDBMSConfig(getRDBMSDataSourceConfig(tenantId, dataSourceName));
    }

    private static Map<String,String> decodeCassandraConnectionURL(String url) {
        try {
            url = url.split(",")[0];    //currently consider only the first host. Need to support multiples
            String protocol = url.substring(0, url.indexOf("//")-1);
            String hostAndPort = url.substring(url.indexOf("//") + 2, url.lastIndexOf('/'));
            String host = hostAndPort.split(":")[0];
            String port = hostAndPort.split(":")[1];
            String ks = url.substring(url.lastIndexOf('/') + 1);

            Map<String,String> params = new HashMap<String, String>();
            params.put(PARAM_PROTOCOL,protocol);
            params.put(PARAM_HOST,host);
            params.put(PARAM_PORT,port);
            params.put(PARAM_KS,ks);

            return params;
        } catch (Exception e) {
            throw new RuntimeException("Invalid Cassandra URL: " + url);
        }
    }

    /*
    Build a Cassandra JDBC URL from given parameters
     */
    private static String encodeCassandraConnectionURL(Map<String,String> connParams) {
        StringBuilder sb = new StringBuilder();
        sb.append(connParams.get(PARAM_PROTOCOL)).append("://");   //e.g jdbc://cassandra://
        sb.append(connParams.get(PARAM_HOST)).append(":");  //e.g localhost:
        sb.append(connParams.get(PARAM_PORT)).append("/");  //e.g 9160
        sb.append(connParams.get(PARAM_KS));  //e.g BAM_UTIL_KS
        return sb.toString();
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
