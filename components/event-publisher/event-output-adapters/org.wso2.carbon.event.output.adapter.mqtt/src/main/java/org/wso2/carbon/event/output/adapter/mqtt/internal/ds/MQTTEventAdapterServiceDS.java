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
package org.wso2.carbon.event.output.adapter.mqtt.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterFactory;
import org.wso2.carbon.event.output.adapter.mqtt.MQTTEventAdapterFactory;


/**
 * @scr.component component.name="output.Mqtt.AdapterService.component" immediate="true"
 */
public class MQTTEventAdapterServiceDS {

    private static final Log log = LogFactory.getLog(MQTTEventAdapterServiceDS.class);

    /**
     * Deployment of the MQTT event adapter service will be done.
     *
     * @param context bundle context where service is registered
     */
    protected void activate(ComponentContext context) {

        try {
            OutputEventAdapterFactory mqttEventAdapterFactory = new MQTTEventAdapterFactory();
            context.getBundleContext().registerService(OutputEventAdapterFactory.class.getName(),
                    mqttEventAdapterFactory, null);
            if (log.isDebugEnabled()) {
                log.debug("Successfully deployed the output MQTT event adapter service ");
            }
        } catch (RuntimeException e) {
            log.error("Can not deployed the output MQTT event adapter service ", e);
        }
    }

}
