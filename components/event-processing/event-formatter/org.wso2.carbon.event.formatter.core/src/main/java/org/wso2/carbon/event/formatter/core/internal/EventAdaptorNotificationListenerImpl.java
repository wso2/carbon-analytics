package org.wso2.carbon.event.formatter.core.internal;

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


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.formatter.core.exception.EventFormatterConfigurationException;
import org.wso2.carbon.event.formatter.core.internal.ds.EventFormatterServiceValueHolder;
import org.wso2.carbon.event.output.adaptor.manager.core.OutputEventAdaptorNotificationListener;


public class EventAdaptorNotificationListenerImpl
        implements OutputEventAdaptorNotificationListener {

    private static final Log log = LogFactory.getLog(EventAdaptorNotificationListenerImpl.class);

    @Override
    public void configurationAdded(int tenantId, String eventAdaptorName) {

        CarbonEventFormatterService carbonEventFormatterService = EventFormatterServiceValueHolder.getCarbonEventFormatterService();

        try {
            carbonEventFormatterService.activateInactiveEventFormatterConfiguration(tenantId, eventAdaptorName);
        } catch (EventFormatterConfigurationException e) {
            log.error("Exception occurred while re-deploying the Event formatter configuration files");
        }
    }

    @Override
    public void configurationRemoved(int tenantId, String eventAdaptorName) {
        CarbonEventFormatterService carbonEventFormatterService = EventFormatterServiceValueHolder.getCarbonEventFormatterService();

        try {
            carbonEventFormatterService.deactivateActiveEventFormatterConfiguration(tenantId, eventAdaptorName);
        } catch (EventFormatterConfigurationException e) {
            log.error("Exception occurred while un-deploying the Event formatter configuration files");
        }
    }
}
