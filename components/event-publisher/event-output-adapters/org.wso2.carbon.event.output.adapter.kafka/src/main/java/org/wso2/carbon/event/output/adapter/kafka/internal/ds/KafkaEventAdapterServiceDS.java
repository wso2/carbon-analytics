/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.event.output.adapter.kafka.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterFactory;
import org.wso2.carbon.event.output.adapter.kafka.KafkaEventAdapterFactory;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name="output.email.EventAdaptorService.component" immediate="true"
 */
public class KafkaEventAdapterServiceDS {

    private static final Log log = LogFactory.getLog(KafkaEventAdapterServiceDS.class);

    /**
     * initialize the kafka service here service here.
     *
     * @param context
     */
    protected void activate(ComponentContext context) {

        try {
            OutputEventAdapterFactory testOutEventAdaptorFactory = new KafkaEventAdapterFactory();
            context.getBundleContext().registerService(OutputEventAdapterFactory.class.getName(), testOutEventAdaptorFactory, null);
            log.info("Successfully deployed the Kafka output event adaptor service");
        } catch (RuntimeException e) {
            log.error("Can not create the Kafka output event adaptor service ", e);
        }
    }

    protected void setConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        KafkaEventAdapterServiceValueHolder.registerConfigurationContextService(configurationContextService);
    }

    protected void unsetConfigurationContextService(
            ConfigurationContextService configurationContextService) {

    }


}
