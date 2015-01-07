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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.analytics.datasource.core.AnalyticsException;

/**
 * This class represents the analytics data service declarative services component.
 * @scr.component name="analytics.component" immediate="true"
 */
public class AnalyticsDataServiceComponent {
    
    private static final Log log = LogFactory.getLog(AnalyticsDataServiceComponent.class);
    
    protected void activate(ComponentContext ctx) {
        if (log.isDebugEnabled()) {
            log.debug("Starting AnalyticsDataServiceComponent#activate");
        }
        BundleContext bundleContext = ctx.getBundleContext();
        try {
            AnalyticsDataServiceConfiguration config = this.loadAnalyticsDataServiceConfig();
            bundleContext.registerService(AnalyticsDataService.class, new AnalyticsDataServiceImpl(config), null);
            if (log.isDebugEnabled()) {
                log.debug("Finished AnalyticsDataServiceComponent#activate");
            }
        } catch(AnalyticsException e) {
            log.error("Error in registering analytics data service: " + e.getMessage(), e);
        }        
    }
    
    private AnalyticsDataServiceConfiguration loadAnalyticsDataServiceConfig() throws AnalyticsException {
        return null;
    }

}
