/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.event.stream.core.internal.stream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.stream.core.exception.EventStreamConfigurationException;
import org.wso2.carbon.event.stream.core.internal.CarbonEventStreamService;


public class StreamAddRemoveListenerImpl implements StreamAddRemoveListener {
    private static final Log log = LogFactory.getLog(CarbonEventStreamService.class);
    private CarbonEventStreamService carbonEventStreamService;

    public StreamAddRemoveListenerImpl(CarbonEventStreamService carbonEventStreamService) {
        this.carbonEventStreamService = carbonEventStreamService;
    }

    @Override
    public void streamAdded(int tenantId, String streamId) {
        try {
            carbonEventStreamService.getEventStreamRuntime().loadEventStream(streamId);
        } catch (EventStreamConfigurationException e) {
            log.error("Loading stream from Data Bridge failed, " + e.getMessage(), e);
        }
    }

    @Override
    public void streamRemoved(int tenantId, String streamId) {
        try {
            carbonEventStreamService.getEventStreamRuntime().unloadEventStream(streamId);
        } catch (EventStreamConfigurationException e) {
            log.error("Unloading stream from Data Bridge failed, " + e.getMessage(), e);
        }
    }
}
