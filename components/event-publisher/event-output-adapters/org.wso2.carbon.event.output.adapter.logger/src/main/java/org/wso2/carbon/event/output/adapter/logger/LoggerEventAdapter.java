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
package org.wso2.carbon.event.output.adapter.logger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapter;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;
import org.wso2.carbon.event.output.adapter.core.exception.TestConnectionNotSupportedException;
import org.wso2.carbon.event.output.adapter.logger.internal.util.LoggerEventAdapterConstants;

import java.util.Arrays;
import java.util.Map;

public final class LoggerEventAdapter implements OutputEventAdapter {

    private static final Log log = LogFactory.getLog(LoggerEventAdapter.class);
    private OutputEventAdapterConfiguration eventAdapterConfiguration;
    private Map<String, String> globalProperties;

    public LoggerEventAdapter(OutputEventAdapterConfiguration eventAdapterConfiguration, Map<String, String> globalProperties) {
        this.eventAdapterConfiguration = eventAdapterConfiguration;
        this.globalProperties = globalProperties;
    }


    @Override
    public void init() throws OutputEventAdapterException {
        //not required
    }

    @Override
    public void testConnect() throws TestConnectionNotSupportedException {
        throw new TestConnectionNotSupportedException("not-available");
    }

    @Override
    public void connect() {
        //not required
    }

    @Override
    public void publish(Object message, Map<String, String> dynamicProperties) {
        if (message instanceof Object[]) {
            log.info("Unique ID: " + dynamicProperties.get(LoggerEventAdapterConstants.ADAPTER_MESSAGE_UNIQUE_ID) + ", Event: " + Arrays.deepToString((Object[]) message));
        } else {
            log.info("Unique ID: " + dynamicProperties.get(LoggerEventAdapterConstants.ADAPTER_MESSAGE_UNIQUE_ID) + ", Event: " + message);
        }
    }

    @Override
    public void disconnect() {
        //not required
    }

    @Override
    public void destroy() {
        //not required
    }
}
