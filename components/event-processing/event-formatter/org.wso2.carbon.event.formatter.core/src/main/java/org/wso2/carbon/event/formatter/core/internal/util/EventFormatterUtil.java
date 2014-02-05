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

package org.wso2.carbon.event.formatter.core.internal.util;

import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;
import org.wso2.carbon.event.formatter.core.config.EventFormatterConfiguration;
import org.wso2.carbon.event.formatter.core.config.EventFormatterConstants;
import org.wso2.carbon.event.formatter.core.internal.config.ToPropertyConfiguration;
import org.wso2.carbon.event.formatter.core.internal.type.wso2event.WSO2EventOutputMapping;
import org.wso2.carbon.event.output.adaptor.core.message.config.OutputEventAdaptorMessageConfiguration;

public class EventFormatterUtil {

    public static EventFormatterConfiguration createDefaultEventFormatter(String streamId, String transportAdaptorName) {
        String streamName = DataBridgeCommonsUtils.getStreamNameFromStreamId(streamId);
        String streamVersion = DataBridgeCommonsUtils.getStreamVersionFromStreamId(streamId);

        EventFormatterConfiguration eventFormatterConfiguration =
                new EventFormatterConfiguration();

        eventFormatterConfiguration.setEventFormatterName(streamId.replaceAll(EventFormatterConstants.STREAM_ID_SEPERATOR, EventFormatterConstants.NORMALIZATION_STRING) + EventFormatterConstants.DEFAULT_EVENT_FORMATTER_POSTFIX);

        WSO2EventOutputMapping wso2EventOutputMapping = new WSO2EventOutputMapping();
        wso2EventOutputMapping.setCustomMappingEnabled(false);
        eventFormatterConfiguration.setOutputMapping(wso2EventOutputMapping);

        ToPropertyConfiguration toPropertyConfiguration = new ToPropertyConfiguration();
        OutputEventAdaptorMessageConfiguration outputEventAdaptorMessageConfiguration = new OutputEventAdaptorMessageConfiguration();
        outputEventAdaptorMessageConfiguration.addOutputMessageProperty(EventFormatterConstants.ADAPTOR_MESSAGE_STREAM_NAME, streamName);
        outputEventAdaptorMessageConfiguration.addOutputMessageProperty(EventFormatterConstants.ADAPTOR_MESSAGE_STREAM_VERSION, streamVersion);
        toPropertyConfiguration.setOutputEventAdaptorMessageConfiguration(outputEventAdaptorMessageConfiguration);
        toPropertyConfiguration.setEventAdaptorName(transportAdaptorName);
        toPropertyConfiguration.setEventAdaptorType(EventFormatterConstants.ADAPTOR_TYPE_WSO2EVENT);
        eventFormatterConfiguration.setToPropertyConfiguration(toPropertyConfiguration);

        eventFormatterConfiguration.setFromStreamName(streamName);
        eventFormatterConfiguration.setFromStreamVersion(streamVersion);

        return eventFormatterConfiguration;
    }
}
