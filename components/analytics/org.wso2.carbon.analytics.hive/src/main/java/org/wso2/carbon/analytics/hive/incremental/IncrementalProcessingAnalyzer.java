package org.wso2.carbon.analytics.hive.incremental;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hive.conf.HiveConf;
import org.wso2.carbon.analytics.hive.exception.HiveIncrementalProcessException;
import org.wso2.carbon.analytics.hive.extension.AbstractHiveAnalyzer;
import org.wso2.carbon.analytics.hive.incremental.metadb.IncrementalMetaStoreManager;
import org.wso2.carbon.analytics.hive.incremental.util.IncrementalProcessingConstants;
import org.wso2.carbon.analytics.hive.incremental.util.TimeProcessorUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
public class IncrementalProcessingAnalyzer extends AbstractHiveAnalyzer {
    private static Log log = LogFactory.getLog(IncrementalProcessingAnalyzer.class);

    private HashMap<String, String> params = new HashMap<String, String>();

    private Set<String> propsAdded = new HashSet<String>();

    private boolean isValid;

    private String markerName;
    private long bufferTime;
    private String scriptName;
    private int tenantId;

    private long fromTime;
    private long toTime;

    private long lastAccessedTime;
    private boolean hasNonIndexedData;
    private boolean skipIncrementalProcess;


    public IncrementalProcessingAnalyzer(int tenantId) {
        this.tenantId = tenantId;
        this.fromTime = -1;
        this.toTime = -1;
    }


    @Override
    public void execute() throws HiveIncrementalProcessException {
        init();
        try {
            HashMap<String, String> props = IncrementalMetaStoreManager.getInstance().
                    getMetaStoreProperties(scriptName, markerName, bufferTime, tenantId, fromTime, toTime);

            validateCurrentProcessIncremental(props);
            setLastAccessedTime(props);

            for (String aKey : props.keySet()) {
                setProperty(aKey, props.get(aKey));
            }
            isValid = Boolean.parseBoolean(props.get(
                    HiveConf.ConfVars.HIVE_INCREMENTAL_VALID_TO_RUN_HIVE_QUERY.toString()));
        } catch (HiveIncrementalProcessException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    private void setLastAccessedTime(Map<String, String> props){
        lastAccessedTime = Long.parseLong(props.
                get(IncrementalProcessingConstants.LAST_ACCESSED_TIME));
        props.remove(IncrementalProcessingConstants.LAST_ACCESSED_TIME);
    }

    private void validateCurrentProcessIncremental(Map<String, String> props){
        skipIncrementalProcess = Boolean.parseBoolean(props.
                get(IncrementalProcessingConstants.SKIP_INCREMENTAL_PROCESS));
        if(hasNonIndexedData & skipIncrementalProcess){
            props.put(HiveConf.ConfVars.HIVE_INCREMENTAL_PROCESS_ENABLE.toString(), Boolean.FALSE.toString());
        }
        props.remove(IncrementalProcessingConstants.SKIP_INCREMENTAL_PROCESS);
    }


    public void setParameters(HashMap<String, String> properties) {
        this.params = properties;
    }

    private void init() throws HiveIncrementalProcessException {

        String tableNames = params.get(IncrementalProcessingConstants
                .INCREMENTAL_MARKER_TABLES);

        String bufferTimeStr = params.get(IncrementalProcessingConstants.
                INCREMENTAL_BUFFER_TIME);

        bufferTime = 0;

        if (null != bufferTimeStr) {
            bufferTime = Integer.parseInt(bufferTimeStr);
        }

        fromTime = TimeProcessorUtil.getTimeStamp(params.
                get(IncrementalProcessingConstants.INCREMENTAL_FROM_TIME));
        toTime = TimeProcessorUtil.getTimeStamp(params.
                get(IncrementalProcessingConstants.INCREMENTAL_TO_TIME));

        scriptName = params.get(IncrementalProcessingConstants.SCRIPT_NAME);

        markerName = scriptName+"_"+tenantId+"_"+params.get(IncrementalProcessingConstants.
                INCREMENTAL_MARKER_NAME_PROPERY);

        hasNonIndexedData = Boolean.
                parseBoolean(params.get(IncrementalProcessingConstants.HAS_NON_INDEXED_DATA));

        params.put(IncrementalProcessingConstants.INCREMENTAL_BUFFER_TIME,
                String.valueOf(bufferTime));

        setProperty(HiveConf.ConfVars.CURRENT_HIVE_SCRIPT_NAME.toString(),
                scriptName);

        setProperty(HiveConf.ConfVars.HIVE_INCREMENTAL_PROCESS_ENABLE.toString(),
                IncrementalProcessingConstants.ENABLED);

        setProperty(HiveConf.ConfVars.HIVE_INCREMENTAL_TABLE_NAMES.toString(),
                tableNames.trim());
        setProperty(HiveConf.ConfVars.HIVE_INCREMENTAL_MARKER_NAME.toString(),
                markerName);
    }

    public void setProperty(String key, String value) {
        super.setProperty(key, value);
        propsAdded.add(key);
    }


    public void cleanUp() {
        for (String property : propsAdded) {
            removeProperty(property);
        }
        removeProperty(HiveConf.ConfVars.HIVE_INCREMENTAL_CASSANDRA_TABLES.toString());
    }

    public void finalizeExecution() throws HiveIncrementalProcessException {
      if (skipIncrementalProcess){
          lastAccessedTime = System.currentTimeMillis()-bufferTime;
      }
      IncrementalMetaStoreManager.getInstance().
              updateMetaStoreProperties(scriptName, markerName, tenantId, lastAccessedTime);
    }

    public boolean isValidToRunQuery(){
      return isValid;
    }



}
