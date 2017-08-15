/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.databridge.receiver.thrift.service;

import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.server.TServlet;

/**
 * Thrift Event Transmission Servlet
 */
public class ThriftEventTransmissionServlet extends TServlet {

    public ThriftEventTransmissionServlet(TProcessor processor, TProtocolFactory inProtocolFactory,
                                          TProtocolFactory outProtocolFactory) {
        super(processor, inProtocolFactory, outProtocolFactory);
    }

}
