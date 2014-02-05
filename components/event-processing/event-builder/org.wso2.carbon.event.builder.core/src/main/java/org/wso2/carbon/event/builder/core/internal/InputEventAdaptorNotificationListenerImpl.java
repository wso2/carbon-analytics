/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.event.builder.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.builder.core.exception.EventBuilderConfigurationException;
import org.wso2.carbon.event.builder.core.internal.ds.EventBuilderServiceValueHolder;
import org.wso2.carbon.event.input.adaptor.core.config.InputEventAdaptorConfiguration;
import org.wso2.carbon.event.input.adaptor.manager.core.InputEventAdaptorNotificationListener;


public class InputEventAdaptorNotificationListenerImpl implements
        InputEventAdaptorNotificationListener {

    private static final Log log = LogFactory.getLog(InputEventAdaptorNotificationListenerImpl.class);

    @Override
    public void configurationAdded(int tenantId, String eventAdaptorName) {
        CarbonEventBuilderService carbonEventBuilderService = EventBuilderServiceValueHolder.getCarbonEventBuilderService();
        try {
            carbonEventBuilderService.activateInactiveEventBuilderConfigurationsForAdaptor(eventAdaptorName, tenantId);
        } catch (EventBuilderConfigurationException e) {
            log.error("Exception occurred while deploying the Event Builder configuration files : " + e.getMessage(), e);
        } catch (RuntimeException e) {
            log.error("Runtime exception while deploying the Event Builder configuration files : " + e.getMessage(), e);
        }
    }

    @Override
    public void configurationRemoved(int tenantId,
                                     InputEventAdaptorConfiguration inputEventAdaptorConfiguration) {
        CarbonEventBuilderService carbonEventBuilderService = EventBuilderServiceValueHolder.getCarbonEventBuilderService();
        try {
            carbonEventBuilderService.deactivateActiveEventBuilderConfigurationsForAdaptor(inputEventAdaptorConfiguration, tenantId);
        } catch (EventBuilderConfigurationException e) {
            log.error("Exception occurred while undeploying the Event Builder configuration files : " + e.getMessage(), e);
        } catch (RuntimeException e) {
            log.error("Runtime exception while undeploying the Event Builder configuration files : " + e.getMessage(), e);
        }
    }

}
