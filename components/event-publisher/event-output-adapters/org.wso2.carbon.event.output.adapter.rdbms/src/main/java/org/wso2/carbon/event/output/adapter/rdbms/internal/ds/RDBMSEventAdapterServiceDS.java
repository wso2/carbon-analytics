/*
 *
 *   Copyright (c) 2014-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */

package org.wso2.carbon.event.output.adapter.rdbms.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterFactory;
import org.wso2.carbon.event.output.adapter.rdbms.RDBMSEventAdapterFactory;
import org.wso2.carbon.ndatasource.core.DataSourceService;

/**
 * @scr.component name="output.RDBMSEventAdapterService.component" immediate="true"
 * @scr.reference name="org.wso2.carbon.ndatasource" interface="org.wso2.carbon.ndatasource.core.DataSourceService"
 * cardinality="1..1" policy="dynamic" bind="setDataSourceService" unbind="unsetDataSourceService"
 */
public class RDBMSEventAdapterServiceDS {

    private static final Log log = LogFactory.getLog(RDBMSEventAdapterServiceDS.class);

    /**
     * initialize the agent service here service here.
     *
     * @param context
     */
    protected void activate(ComponentContext context) {

        try {
            OutputEventAdapterFactory rdbmsEventAdapterFactory = new RDBMSEventAdapterFactory();
            context.getBundleContext().registerService(OutputEventAdapterFactory.class.getName(), rdbmsEventAdapterFactory, null);
            if (log.isDebugEnabled()) {
                log.debug("Successfully deployed the output rdbms event adapter service");
            }
        } catch (RuntimeException e) {
            log.error("Can not create the output rdbms event adapter service ", e);
        }
    }

    protected void setDataSourceService(DataSourceService dataSourceService) {
        RDBMSEventAdapterServiceValueHolder.setDataSourceService(dataSourceService);
    }

    protected void unsetDataSourceService(DataSourceService dataSourceService) {
        RDBMSEventAdapterServiceValueHolder.setDataSourceService(null);
    }
}
