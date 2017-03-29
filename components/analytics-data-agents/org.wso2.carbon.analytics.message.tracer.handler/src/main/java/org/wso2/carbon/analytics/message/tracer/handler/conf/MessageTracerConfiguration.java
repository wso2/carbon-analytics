/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.analytics.message.tracer.handler.conf;

import org.wso2.carbon.analytics.message.tracer.handler.exception.MessageTracerHandlerException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;
import java.util.Set;

@XmlRootElement (name = "MessageTracer")
public class MessageTracerConfiguration {

    private Marshaller msgTracingMarshaller;

    private Set<String> messageTracingEnabledAdminServices;

    private String messageTracingEnabled;

    public MessageTracerConfiguration() throws MessageTracerHandlerException {
        try {
            JAXBContext ctx = JAXBContext.newInstance(MessageTracerConfiguration.class);
            msgTracingMarshaller = ctx.createMarshaller();
        } catch (JAXBException e) {
            throw new MessageTracerHandlerException("Error creating message tracing configuration info marshaller: " +
                    e.getMessage(), e);
        }
    }

    @XmlElementWrapper(name = "MessageTracingEnabledAdminServices", nillable = false)
    @XmlElement(name = "Service", nillable = false)
    public Set<String> getMessageTracingEnabledAdminServices() {
        return messageTracingEnabledAdminServices;
    }

    public void setMessageTracingEnabledAdminServices(Set<String> messageTracingEnabledAdminServices) {
        this.messageTracingEnabledAdminServices = messageTracingEnabledAdminServices;
    }

    @XmlRootElement(name = "Service")
    public static class MessageTracingService {

        private String value;

        @XmlValue
        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    @XmlElement(name = "MessageTracingEnabled")
    public void setmessageTracingEnabled(String messageTracingEnabled) {
        this.messageTracingEnabled = messageTracingEnabled;
    }

    public String getMessageTracingEnabled() {
        return messageTracingEnabled;
    }

    @XmlTransient
    public Marshaller getMsgTracingMarshaller() {
        return msgTracingMarshaller;
    }

}