/*
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.analytics.dataservice;

import com.hazelcast.core.HazelcastInstance;
import org.apache.axis2.engine.ListenerManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.analytics.dataservice.clustering.AnalyticsClusterManager;
import org.wso2.carbon.analytics.dataservice.clustering.AnalyticsClusterManagerImpl;
import org.wso2.carbon.analytics.dataservice.config.AnalyticsDataServiceConfiguration;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.AnalyticsDataSourceConstants;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

/**
 * This class represents the analytics data service declarative services component.
 * @scr.component name="analytics.component" immediate="true"
 * @scr.reference name="listener.manager.service" interface="org.apache.axis2.engine.ListenerManager"
 * cardinality="1..1" policy="dynamic"  bind="setListenerManager" unbind="unsetListenerManager"
 * @scr.reference name="user.realmservice.default" interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService" unbind="unsetRealmService"
 */
public class AnalyticsDataServiceComponent {

    private static final Log log = LogFactory.getLog(AnalyticsDataServiceComponent.class);

    private static final String ANALYTICS_DS_CONFIG_FILE = "analytics-dataservice-config.xml";

    protected void activate(ComponentContext ctx) {
        if (log.isDebugEnabled()) {
            log.debug("Starting AnalyticsDataServiceComponent#activate");
        }
        BundleContext bundleContext = ctx.getBundleContext();
        try {
            AnalyticsDataServiceConfiguration config = this.loadAnalyticsDataServiceConfig();
            this.loadHazelcast();
            AnalyticsClusterManager clusterManager = new AnalyticsClusterManagerImpl();
            bundleContext.registerService(AnalyticsClusterManager.class, clusterManager, null);
            AnalyticsServiceHolder.setAnalyticsClusterManager(clusterManager);
            AnalyticsDataService analyticsDataService = new AnalyticsDataServiceImpl(config);
            SecureAnalyticsDataServiceImpl secureAnalyticsDataService = new SecureAnalyticsDataServiceImpl(analyticsDataService);
            bundleContext.registerService(AnalyticsDataService.class, analyticsDataService, null);
            bundleContext.registerService(SecureAnalyticsDataService.class, secureAnalyticsDataService, null);
            if (log.isDebugEnabled()) {
                log.debug("Finished AnalyticsDataServiceComponent#activate");
            }
        } catch(Throwable e) {
            log.error("Error in activating analytics data service: " + e.getMessage(), e);
        }
    }

    protected void deactivate(ComponentContext ctx) {
        try {
            AnalyticsServiceHolder.getAnalyticsDataService().destroy();
        } catch (Throwable e) {
            log.error("Error in deactivating analytics data service: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void loadHazelcast() {
        BundleContext ctx = FrameworkUtil.getBundle(AnalyticsServiceHolder.class).getBundleContext();
        ServiceReference ref = ctx.getServiceReference(HazelcastInstance.class);
        if (ref != null) {
            AnalyticsServiceHolder.setHazelcastInstance((HazelcastInstance) ctx.getService(ref));
        }
    }

    private AnalyticsDataServiceConfiguration loadAnalyticsDataServiceConfig() throws AnalyticsException {
        try {
            File confFile = new File(CarbonUtils.getCarbonConfigDirPath() +
                    File.separator + AnalyticsDataSourceConstants.ANALYTICS_CONF_DIR +
                    File.separator + ANALYTICS_DS_CONFIG_FILE);
            if (!confFile.exists()) {
                throw new AnalyticsException("Cannot initalize analytics data service, " +
                        "the analytics data service configuration file cannot be found at: " +
                        confFile.getPath());
            }
            JAXBContext ctx = JAXBContext.newInstance(AnalyticsDataServiceConfiguration.class);
            Unmarshaller unmarshaller = ctx.createUnmarshaller();
            return (AnalyticsDataServiceConfiguration) unmarshaller.unmarshal(confFile);
        } catch (JAXBException e) {
            throw new AnalyticsException(
                    "Error in processing analytics data service configuration: " + e.getMessage(), e);
        }
    }

    protected void setListenerManager(ListenerManager lm) {
        /* we don't really need this, the listener manager service is acquired
         * to make sure, as a workaround, that the task component is initialized 
         * after the axis2 clustering agent is initialized */
    }

    protected void unsetListenerManager(ListenerManager lm) {
        /* empty */
    }

    protected void setRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.info("Setting the Realm Service");
        }
        AnalyticsServiceHolder.setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.info("Unsetting the Realm Service");
        }
    }
}
