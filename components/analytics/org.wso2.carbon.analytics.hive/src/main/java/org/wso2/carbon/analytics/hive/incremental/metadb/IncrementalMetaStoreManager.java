package org.wso2.carbon.analytics.hive.incremental.metadb;

import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.beans.HColumn;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hive.conf.HiveConf;
import org.w3c.dom.Element;
import org.wso2.carbon.analytics.hive.ServiceHolder;
import org.wso2.carbon.analytics.hive.exception.HiveIncrementalProcessException;
import org.wso2.carbon.analytics.hive.incremental.util.IncrementalProcessingConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.ndatasource.core.utils.DataSourceUtils;
import org.wso2.carbon.ndatasource.rdbms.RDBMSConfiguration;
import org.wso2.carbon.ndatasource.rdbms.RDBMSDataSourceReader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class IncrementalMetaStoreManager {
    private static Log log = LogFactory.getLog(IncrementalMetaStoreManager.class);

    private static final String ROW_KEY_SEPERATOR = "@#%%@";

    private static IncrementalMetaStoreManager instance;

    public static IncrementalMetaStoreManager getInstance() {
        if (null == instance) {
            try {
                ServiceHolder.getDataSourceService().
                        getDataSource(IncrementalProcessingConstants.HIVE_INCREMENTAL_METASTORE_DATASOURCE);
                instance = new IncrementalMetaStoreManager();
            } catch (DataSourceException e) {
                log.error("Failed to load the Hive incremental datasource configuration. " + e.getMessage(), e);
            }
        }
        return instance;
    }


    private IncrementalMetaStoreManager() {
    }

    public HashMap<String, String> getMetaStoreProperties(String scriptName, String markerName, long bufferTimeInMilliS, int tenantId,
                                                          long fromTime, long toTime) throws
            HiveIncrementalProcessException {

        HashMap<String, String> hiveExecutionProps = new HashMap<String, String>();
        long fromTimeStamp = 0;
        long toTimeStamp = 0;
        
        String rowKeyName = getRowKeyForMarkerName(scriptName, markerName, tenantId);
        String cfName = IncrementalProcessingConstants.MARKER_CF_NAME;
        
        HashMap<String, String> dbProps = getIncrementalDataSourceProperties(tenantId);
        String userName = dbProps.get(IncrementalProcessingConstants.DATASOURCE_PROPS_USERNAME);
        String password = dbProps.get(IncrementalProcessingConstants.DATASOURCE_PROPS_PASSWORD);
        String ksName = dbProps.get(IncrementalProcessingConstants.DATASOURCE_PROPS_KEYSPACE);
        if (null == ksName) {
            ksName = MetaStore.DEFAULT_KS_NAME;
        }

        MetaStore metaStore = new MetaStore();
        Cluster cluster = metaStore.getCluster(userName, password);

        metaStore.createCFIfNotExists(cluster, ksName, cfName, dbProps);

        if (fromTime == -1) {
            List<HColumn<String, Long>> columns = metaStore.getColumnsOfRow(cluster, ksName, cfName, rowKeyName);

            if (null != columns && columns.size() > 0) {
                fromTimeStamp = columns.get(0).getValue();
            }else {
                hiveExecutionProps.put(IncrementalProcessingConstants.SKIP_INCREMENTAL_PROCESS, Boolean.TRUE.toString());
            }
        } else {
            fromTimeStamp = fromTime;
        }
        hiveExecutionProps.put(getFromTimeStampPropertyName(rowKeyName), String.valueOf(fromTimeStamp));


        if (toTime == -1) {
            toTimeStamp = System.currentTimeMillis() - bufferTimeInMilliS;
        } else {
            toTimeStamp = toTime;
        }

        hiveExecutionProps.put(getToTimeStampPropertyName(rowKeyName), String.valueOf(toTimeStamp));
        hiveExecutionProps.put(IncrementalProcessingConstants.LAST_ACCESSED_TIME, String.valueOf(toTimeStamp));

        if (fromTimeStamp > toTimeStamp) {
            hiveExecutionProps.put(HiveConf.ConfVars.HIVE_INCREMENTAL_VALID_TO_RUN_HIVE_QUERY.toString(),
                    IncrementalProcessingConstants.FALSE);
        } else {
            hiveExecutionProps.put(HiveConf.ConfVars.HIVE_INCREMENTAL_VALID_TO_RUN_HIVE_QUERY.toString(),
                    IncrementalProcessingConstants.TRUE);
        }
        
        hiveExecutionProps.put(HiveConf.ConfVars.HIVE_INCREMENTAL_MARKER_NAME.toString(), rowKeyName);
        return hiveExecutionProps;
    }
    
    public void updateMetaStoreProperties(String scriptName, String markerName, int tenantId, long lastAccessedTime) 
            throws HiveIncrementalProcessException {
        String cfName = IncrementalProcessingConstants.MARKER_CF_NAME;

        HashMap<String, String> dbProps = getIncrementalDataSourceProperties(tenantId);
        String userName = dbProps.get(IncrementalProcessingConstants.DATASOURCE_PROPS_USERNAME);
        String password = dbProps.get(IncrementalProcessingConstants.DATASOURCE_PROPS_PASSWORD);
        String ksName = dbProps.get(IncrementalProcessingConstants.DATASOURCE_PROPS_KEYSPACE);
        if (null == ksName) {
            ksName = MetaStore.DEFAULT_KS_NAME;
        }

        MetaStore metaStore = new MetaStore();
        Cluster cluster = metaStore.getCluster(userName, password);

        String rowKeyName = getRowKeyForMarkerName(scriptName, markerName, tenantId);
        
        metaStore.updateColumn(cluster, ksName, cfName, rowKeyName,
                IncrementalProcessingConstants.LAST_ACCESSED_TIME_COLUMN_NAME, lastAccessedTime);
        
    }


    public HashMap<String, String> getIncrementalDataSourceProperties(int tenantId) throws HiveIncrementalProcessException {
        Element element = null;
        try {

            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
            HashMap<String, String> dataSourceProperties = new HashMap<String, String>();

            element = (Element) ServiceHolder.getDataSourceService().getDataSource(
                    IncrementalProcessingConstants.HIVE_INCREMENTAL_METASTORE_DATASOURCE).
                    getDSMInfo().getDefinition().getDsXMLConfiguration();

            RDBMSConfiguration rdbmsConfiguration = RDBMSDataSourceReader.loadConfig(
                    DataSourceUtils.elementToString(element));

            setDataSourceProperties(dataSourceProperties, rdbmsConfiguration);

            return dataSourceProperties;

        } catch (DataSourceException e) {
            log.error(e.getMessage(), e);
            throw new HiveIncrementalProcessException(e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }


    private void setDataSourceProperties(
            Map<String, String> dataSourceProperties, RDBMSConfiguration rdbmsConfiguration) {

        setProperties(IncrementalProcessingConstants.DATASOURCE_PROPS_USERNAME,
                rdbmsConfiguration.getUsername(), dataSourceProperties);
        setProperties(IncrementalProcessingConstants.DATASOURCE_PROPS_PASSWORD,
                rdbmsConfiguration.getPassword(), dataSourceProperties);

        List<RDBMSConfiguration.DataSourceProperty> dataSourcePropertyList = rdbmsConfiguration.getDataSourceProps();

        for (RDBMSConfiguration.DataSourceProperty property : dataSourcePropertyList) {
            setProperties(property.getName(), property.getValue(), dataSourceProperties);
        }
    }

    private void setProperties(String propertyKey, Object value,
                               Map<String, String> dataSourceProperties) {
        if (value != null) {
            if (value instanceof Boolean) {
                dataSourceProperties.put(propertyKey, Boolean.toString((Boolean) value));
            } else if (value instanceof String) {
                dataSourceProperties.put(propertyKey, (String) value);
            } else if (value instanceof Integer) {
                dataSourceProperties.put(propertyKey, Integer.toString((Integer) value));
            } else if (value instanceof Long) {
                dataSourceProperties.put(propertyKey, Long.toString((Long) value));
            }
        }
    }

    private String getFromTimeStampPropertyName(String markerName) {
        return "hive.marker." + markerName + ".from.timestamp";
    }

    private String getToTimeStampPropertyName(String markerName) {
        return "hive.marker." + markerName + ".to.timestamp";
    }

    private String getSkipCurrentProcessIncrementalProperty(String markerName){
       return "hive.marker." + markerName + ".skip.current.process.incremental";
    }

    private String getRowKeyForMarkerName(String scriptName, String markerName, int tenantId) {
        String rowKey = scriptName + ROW_KEY_SEPERATOR + tenantId + ROW_KEY_SEPERATOR + markerName;
        return rowKey.replace(".", "_");
    }


}
