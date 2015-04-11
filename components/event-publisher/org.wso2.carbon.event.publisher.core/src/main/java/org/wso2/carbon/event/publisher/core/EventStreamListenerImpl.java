/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.wso2.carbon.event.publisher.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.publisher.core.exception.EventPublisherConfigurationException;
import org.wso2.carbon.event.publisher.core.internal.CarbonEventPublisherService;
import org.wso2.carbon.event.publisher.core.internal.ds.EventPublisherServiceValueHolder;
import org.wso2.carbon.event.stream.core.EventStreamListener;

public class EventStreamListenerImpl implements EventStreamListener {

    private static final Log log = LogFactory.getLog(EventStreamListenerImpl.class);

    @Override
    public void removedEventStream(int tenantId, String streamName, String streamVersion) {

        CarbonEventPublisherService carbonEventPublisherService = EventPublisherServiceValueHolder.getCarbonEventPublisherService();
        String streamNameWithVersion = streamName + ":" + streamVersion;
        try {
            carbonEventPublisherService.deactivateActiveEventPublisherConfigurationsForStream(streamNameWithVersion, tenantId);
        } catch (EventPublisherConfigurationException e) {
            log.error("Exception occurred while un-deploying the Event publisher configuration files");
        }

    }

    @Override
    public void addedEventStream(int tenantId, String streamName, String streamVersion) {

        CarbonEventPublisherService carbonEventPublisherService = EventPublisherServiceValueHolder.getCarbonEventPublisherService();
        String streamNameWithVersion = streamName + ":" + streamVersion;
        try {
            carbonEventPublisherService.activateInactiveEventPublisherConfigurationsForStream(streamNameWithVersion, tenantId);
        } catch (EventPublisherConfigurationException e) {
            log.error("Exception occurred while un-deploying the Event publisher configuration files");
        }

    }
}
