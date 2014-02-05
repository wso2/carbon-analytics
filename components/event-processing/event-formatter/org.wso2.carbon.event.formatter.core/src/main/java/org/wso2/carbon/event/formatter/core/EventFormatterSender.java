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

package org.wso2.carbon.event.formatter.core;

import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.event.formatter.core.config.EventFormatter;
import org.wso2.carbon.event.formatter.core.exception.EventFormatterConfigurationException;
import org.wso2.carbon.event.output.adaptor.core.exception.OutputEventAdaptorEventProcessingException;
import org.wso2.carbon.event.processor.api.send.EventSender;

public class EventFormatterSender implements EventSender {

    private EventFormatter eventFormatter;

    public EventFormatterSender(
            org.wso2.carbon.event.formatter.core.config.EventFormatter eventFormatter) {
        this.eventFormatter = eventFormatter;
    }

    public void sendEventData(Object[] eventData) {
        try {
            eventFormatter.sendEventData(eventData);
        } catch (EventFormatterConfigurationException e) {
            throw new OutputEventAdaptorEventProcessingException("Cannot send create an event from input:", e);
        }
    }

    public void sendEvent(Event event) {
        try {
            eventFormatter.sendEvent(event);
        } catch (EventFormatterConfigurationException e) {
            throw new OutputEventAdaptorEventProcessingException("Cannot send create an event from input:", e);
        }
    }


}
