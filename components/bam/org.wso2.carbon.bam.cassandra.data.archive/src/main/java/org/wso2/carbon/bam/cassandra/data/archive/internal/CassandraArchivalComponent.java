package org.wso2.carbon.bam.cassandra.data.archive.internal;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.analytics.hive.service.HiveExecutorService;
import org.wso2.carbon.analytics.hive.web.HiveScriptStoreService;
import org.wso2.carbon.bam.cassandra.data.archive.util.CassandraArchiveUtil;
import org.wso2.carbon.cassandra.dataaccess.DataAccessService;
import org.wso2.carbon.databridge.core.DataBridgeReceiverService;


/**
 * @scr.component name="databridge.cassandra.archive.comp" immediate="true"
 * @scr.reference name="dataaccess.service" interface="org.wso2.carbon.cassandra.dataaccess.DataAccessService"
 * cardinality="1..1" policy="dynamic" bind="setDataAccessService" unbind="unsetDataAccessService"
 * @scr.reference name="bam.hive.service" interface="org.wso2.carbon.analytics.hive.service.HiveExecutorService"
 * cardinality="1..1" policy="dynamic" bind="setHiveExecutorService" unbind="unsetHiveExecutorService"
 * @scr.reference name="bam.hive.store.service" interface="org.wso2.carbon.analytics.hive.web.HiveScriptStoreService"
 * cardinality="1..1" policy="dynamic" bind="setHiveScriptStoreService" unbind="unsetHiveScriptStoreService"
 * @scr.reference name="bam.data.bridge.core" interface="org.wso2.carbon.databridge.core.DataBridgeReceiverService"
 * cardinality="1..1" policy="dynamic" bind="setDataBridgeReceiverService"  unbind="unsetDataBridgeReceiverService
 * */
public class CassandraArchivalComponent {

    private static final Log log = LogFactory.getLog(CassandraArchivalComponent.class);

    protected void activate(ComponentContext componentContext) {

        if (log.isDebugEnabled()) {
            log.debug("Started the Cassandra archival component");
        }
    }

    protected void deactivate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Stopped the Cassandra archival component");
        }
    }

    protected void setDataAccessService(DataAccessService dataAccessService) {
        CassandraArchiveUtil.setDataAccessService(dataAccessService);
    }

    protected void unsetDataAccessService(DataAccessService dataAccessService) {
        CassandraArchiveUtil.setDataAccessService(null);
    }

    protected void setHiveExecutorService(HiveExecutorService hiveExecutorService){
        CassandraArchiveUtil.setHiveExecutorService(hiveExecutorService);
    }

    protected void unsetHiveExecutorService(HiveExecutorService hiveExecutorService){
        CassandraArchiveUtil.setHiveExecutorService(null);
    }

    protected void setHiveScriptStoreService(HiveScriptStoreService storeService){
        CassandraArchiveUtil.setHiveScriptStoreService(storeService);
    }

    protected void unsetHiveScriptStoreService(HiveScriptStoreService scriptStoreService){
        CassandraArchiveUtil.setHiveScriptStoreService(null);
    }

    protected void setDataBridgeReceiverService(DataBridgeReceiverService dataBridgeReceiverService) {
        CassandraArchiveUtil.setDataBridgeReceiverService(dataBridgeReceiverService);
    }

    protected void unsetDataBridgeReceiverService(DataBridgeReceiverService dataBridgeReceiverService) {
        CassandraArchiveUtil.setDataBridgeReceiverService(null);
    }
}
