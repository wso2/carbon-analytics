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

import org.apache.axis2.engine.ListenerManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;

import com.hazelcast.core.HazelcastInstance;

/**
 * This class represents the analytics data service declarative services component.
 * @scr.component name="tasks.component" immediate="true"
 * @scr.reference name="listener.manager.service" interface="org.apache.axis2.engine.ListenerManager"
 * cardinality="1..1" policy="dynamic"  bind="setListenerManager" unbind="unsetListenerManager"
 */
public class AnalyticsDataServiceComponent {
    
    private static final Log log = LogFactory.getLog(AnalyticsDataServiceComponent.class);
    
    protected void activate(ComponentContext ctx) {
        if (log.isDebugEnabled()) {
            log.debug("Starting AnalyticsDataServiceComponent#activate");
        }
        BundleContext bundleContext = ctx.getBundleContext();
        bundleContext.registerService(AnalyticsDataService.class, new AnalyticsDataServiceImpl(), null);
        if (log.isDebugEnabled()) {
            log.debug("Finished AnalyticsDataServiceComponent#activate");
        }
    }
    
    public static HazelcastInstance getHazelcastInstance() {
        BundleContext ctx = FrameworkUtil.getBundle(AnalyticsDataServiceComponent.class).getBundleContext();
        ServiceReference<HazelcastInstance> ref = ctx.getServiceReference(HazelcastInstance.class);
        if (ref == null) {
            return null;
        }
        return ctx.getService(ref);
    }
    
    protected void setListenerManager(ListenerManager lm) {
        /* we don't really need this, the listener manager service is acquired
         * to make sure, as a workaround, that the task component is initialized 
         * after the axis2 clustering agent is initialized */
    }
    
    protected void unsetListenerManager(ListenerManager lm) {
        /* empty */
    }

}
