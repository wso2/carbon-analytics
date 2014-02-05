/*
 * Copyright 2004,2013 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.event.output.adaptor.mysql.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.output.adaptor.core.OutputEventAdaptorFactory;
import org.wso2.carbon.event.output.adaptor.mysql.MysqlEventAdaptorFactory;
import org.wso2.carbon.ndatasource.core.DataSourceService;


/**
 * @scr.component name="output.MysqlEventAdaptorService.component" immediate="true"
 * @scr.reference name="org.wso2.carbon.ndatasource" interface="org.wso2.carbon.ndatasource.core.DataSourceService"
 * cardinality="1..1" policy="dynamic" bind="setDataSourceService" unbind="unsetDataSourceService"
 */


public class MysqlEventAdaptorServiceDS {

    private static final Log log = LogFactory.getLog(MysqlEventAdaptorServiceDS.class);

    /**
     * initialize the agent service here service here.
     *
     * @param context
     */
    protected void activate(ComponentContext context) {

        try {
            OutputEventAdaptorFactory mysqlEventAdaptorFactory = new MysqlEventAdaptorFactory();
            context.getBundleContext().registerService(OutputEventAdaptorFactory.class.getName(), mysqlEventAdaptorFactory, null);
            log.info("Successfully deployed the output mysql event adaptor service");
        } catch (RuntimeException e) {
            log.error("Can not create the output mysql event adaptor service ", e);
        }
    }

    protected void setDataSourceService(DataSourceService dataSourceService) {
        EventAdaptorValueHolder.setDataSourceService(dataSourceService);
    }

    protected void unsetDataSourceService(DataSourceService dataSourceService) {
        EventAdaptorValueHolder.setDataSourceService(null);
    }

}
