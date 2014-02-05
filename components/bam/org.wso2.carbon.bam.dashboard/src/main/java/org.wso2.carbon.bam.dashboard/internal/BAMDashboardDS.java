package org.wso2.carbon.bam.dashboard.internal;

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
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;

/**
 * @scr.component name="org.wso2.stratos.permission.update.PermissionUpdateServiceComponent"
 * immediate="true"
 */

public class BAMDashboardDS {
     private static Log log = LogFactory.getLog(BAMDashboardDS.class);

    protected void activate(ComponentContext ctxt) {
    // register a listener for tenant creation events to initialize the dashboard files
        ctxt.getBundleContext().registerService(Axis2ConfigurationContextObserver.class.getName(),
                                                new BAMDashboardAxisConfigurationObserverImp(),
                                                null);
        if(log.isDebugEnabled()) log.debug("Started the BAM Dashboard");
    }
}
