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
package org.wso2.carbon.event.input.adapter.mqtt.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterFactory;
import org.wso2.carbon.event.input.adapter.mqtt.MQTTEventAdapterFactory;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component component.name="input.Mqtt.AdapterService.component" immediate="true"
 * @scr.reference name="configurationcontext.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="1..1"
 * policy="dynamic" bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 */

public class MQTTEventAdapterServiceDS {

    private static final Log log = LogFactory.getLog(MQTTEventAdapterServiceDS.class);

    protected void activate(ComponentContext context) {

        try {
            InputEventAdapterFactory mqttEventAdapterFactory = new MQTTEventAdapterFactory();
            context.getBundleContext().registerService(InputEventAdapterFactory.class.getName(), mqttEventAdapterFactory, null);
            if (log.isDebugEnabled()) {
                log.debug("Successfully deployed the input MQTT adapter service");
            }

        } catch (RuntimeException e) {
            log.error("Can not create the input MQTT adapter service ", e);
        } catch (Exception ex) {
            log.error(ex);
        }
    }

    protected void setConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        MQTTEventAdapterValueHolder.registerConfigurationContextService(configurationContextService);
    }

    protected void unsetConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        MQTTEventAdapterValueHolder.unregisterConfigurationContextService(configurationContextService);
    }
}
