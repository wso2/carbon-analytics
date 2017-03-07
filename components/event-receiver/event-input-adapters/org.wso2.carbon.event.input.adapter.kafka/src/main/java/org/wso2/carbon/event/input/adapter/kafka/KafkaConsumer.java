/*
*  Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.event.input.adapter.kafka;

import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterListener;

public class KafkaConsumer implements Runnable {

    private KafkaStream stream;
    private InputEventAdapterListener brokerListener;
    private String event;
    private int tenantId;
    private Log log = LogFactory.getLog(KafkaConsumer.class);

    public KafkaConsumer(KafkaStream inStream, InputEventAdapterListener inBrokerListener, int inTenantId) {
        stream = inStream;
        brokerListener = inBrokerListener;
        this.tenantId = inTenantId;
    }

    public void run() {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);

            ConsumerIterator<byte[], byte[]> it = stream.iterator();
            while (it.hasNext()) {
                try {
                    event = new String(it.next().message());
                    if (log.isDebugEnabled()) {
                        log.debug("Event received in Kafka Event Adaptor - " + event);
                    }
                    brokerListener.onEvent(event);
                } catch (Throwable t) {
                    log.error("Error while transforming the event : "+ event, t);
                }
            }
        } catch (Throwable t) {
            log.error("Error while consuming event ", t);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }
}
