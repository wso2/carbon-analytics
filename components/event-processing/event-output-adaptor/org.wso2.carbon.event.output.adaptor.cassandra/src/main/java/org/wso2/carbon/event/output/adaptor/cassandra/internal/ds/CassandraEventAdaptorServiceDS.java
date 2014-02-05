/*
 * Copyright 2004,2005 The Apache Software Foundation.
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

package org.wso2.carbon.event.output.adaptor.cassandra.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.output.adaptor.cassandra.CassandraEventAdaptorFactory;
import org.wso2.carbon.event.output.adaptor.core.OutputEventAdaptorFactory;


/**
 * @scr.component name="output.CassandraEventAdaptorService.component" immediate="true"
 */


public class CassandraEventAdaptorServiceDS {

    private static final Log log = LogFactory.getLog(CassandraEventAdaptorServiceDS.class);

    /**
     * initialize the agent service here service here.
     *
     * @param context
     */
    protected void activate(ComponentContext context) {

        try {
            OutputEventAdaptorFactory cassandraEventAdaptorFactory = new CassandraEventAdaptorFactory();
            context.getBundleContext().registerService(OutputEventAdaptorFactory.class.getName(), cassandraEventAdaptorFactory, null);
            log.info("Successfully deployed the output cassandra event adaptor service");
        } catch (RuntimeException e) {
            log.error("Can not create the output cassandra event adaptor service ", e);
        }
    }


}
