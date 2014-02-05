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
package org.wso2.carbon.bam.service.server;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.wso2.carbon.bam.service.ReceiverService;

public class ReceiverServer {

    public static void start(ReceiverService.Iface service) {
        ThriftServerThread serverThread = new ThriftServerThread(service);
        serverThread.start();
    }


    private static class ThriftServerThread extends Thread {

        ReceiverService.Iface service;

        public ThriftServerThread(ReceiverService.Iface service) {
            this.service = service;
        }

        public void run() {
            startThriftServer();
        }

        private void startThriftServer() {
            try {
                TServerSocket serverTransport = new TServerSocket(7911);
                ReceiverService.Processor processor = new ReceiverService.Processor(service);
                TBinaryProtocol.Factory protFactory = new TBinaryProtocol.Factory(true, true);
                TServer server = new TThreadPoolServer(processor, serverTransport, protFactory);
                System.out.println("Starting server on port 7911 ...");
                server.serve();
            } catch (TTransportException e) {
                e.printStackTrace();
            }
        }
    }

}
