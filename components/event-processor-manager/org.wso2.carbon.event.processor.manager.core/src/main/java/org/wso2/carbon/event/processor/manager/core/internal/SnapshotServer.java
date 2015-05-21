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
package org.wso2.carbon.event.processor.manager.core.internal;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.wso2.carbon.event.processor.manager.commons.utils.HostAndPort;
import org.wso2.carbon.event.processor.manager.core.config.HAConfiguration;
import org.wso2.carbon.event.processor.manager.core.internal.thrift.ManagementServiceImpl;
import org.wso2.carbon.event.processor.manager.core.internal.thrift.service.ManagementService;

import java.net.InetSocketAddress;


public class SnapshotServer {
    private static final Log log = LogFactory.getLog(SnapshotServer.class);
    private static TServer dataReceiverServer;

    public static void start(HAConfiguration config) {
        try {
            start(config.getManagement());

        } catch (RuntimeException e) {
            log.error("Error in starting Agent Server ", e);
        } catch (Throwable e) {
            log.error("Error in starting Agent Server ", e);
        }
    }

    private static void start(HostAndPort hostAndPort) throws Exception {
        try {
            TServerSocket serverTransport = new TServerSocket(
                    new InetSocketAddress(hostAndPort.getHostName(), hostAndPort.getPort()));
            ManagementService.Processor<ManagementServiceImpl> processor =
                    new ManagementService.Processor<ManagementServiceImpl>(
                            new ManagementServiceImpl());
            dataReceiverServer = new TThreadPoolServer(
                    new TThreadPoolServer.Args(serverTransport).processor(processor));
            Thread thread = new Thread(new ServerThread(dataReceiverServer));
            log.info("CEP Management Thrift Server started on " + hostAndPort.getHostName() + ":" + hostAndPort.getPort());
            thread.start();
        } catch (TTransportException e) {
            throw new Exception("Cannot start CEP Management Thrift server on port " + hostAndPort.getPort() +
                    " on host " + hostAndPort.getHostName(), e);
        }
    }

    public static void shutDown(){
         dataReceiverServer.stop();
    }

    static class ServerThread implements Runnable {
        private TServer server;

        ServerThread(TServer server) {
            this.server = server;
        }

        public void run() {
            this.server.serve();
        }
    }

}
