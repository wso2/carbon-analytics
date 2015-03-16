/**
 *
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.databridge.receiver.thrift;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import org.wso2.carbon.databridge.commons.thrift.service.general.ThriftEventTransmissionService;
import org.wso2.carbon.databridge.commons.thrift.service.secure.ThriftSecureEventTransmissionService;
import org.wso2.carbon.databridge.commons.thrift.utils.CommonThriftConstants;
import org.wso2.carbon.databridge.core.DataBridgeReceiverService;
import org.wso2.carbon.databridge.core.exception.DataBridgeException;
import org.wso2.carbon.databridge.core.internal.utils.DataBridgeConstants;
import org.wso2.carbon.databridge.receiver.thrift.conf.ThriftDataReceiverConfiguration;
import org.wso2.carbon.databridge.receiver.thrift.service.ThriftEventTransmissionServiceImpl;
import org.wso2.carbon.databridge.receiver.thrift.service.ThriftSecureEventTransmissionServiceImpl;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * Carbon based implementation of the agent server
 */
public class ThriftDataReceiver {
    private static final Log log = LogFactory.getLog(ThriftDataReceiver.class);
    private DataBridgeReceiverService dataBridgeReceiverService;
    private ThriftDataReceiverConfiguration thriftDataReceiverConfiguration;
    private TServer authenticationServer;
    private TServer dataReceiverServer;

    /**
     * Initialize Carbon Agent Server
     *
     * @param secureReceiverPort
     * @param receiverPort
     * @param dataBridgeReceiverService
     */
    public ThriftDataReceiver(int secureReceiverPort, int receiverPort,
                              DataBridgeReceiverService dataBridgeReceiverService) {
        this.dataBridgeReceiverService = dataBridgeReceiverService;
        this.thriftDataReceiverConfiguration = new ThriftDataReceiverConfiguration(secureReceiverPort, receiverPort);
    }

    /**
     * Initialize Carbon Agent Server
     *
     * @param receiverPort
     * @param dataBridgeReceiverService
     */
    public ThriftDataReceiver(int receiverPort,
                              DataBridgeReceiverService dataBridgeReceiverService) {
        this.dataBridgeReceiverService = dataBridgeReceiverService;
        this.thriftDataReceiverConfiguration = new ThriftDataReceiverConfiguration(receiverPort + CommonThriftConstants.SECURE_EVENT_RECEIVER_PORT_OFFSET, receiverPort);
    }

    /**
     * Initialize Carbon Agent Server
     *
     * @param thriftDataReceiverConfiguration
     *
     * @param dataBridgeReceiverService
     */
    public ThriftDataReceiver(ThriftDataReceiverConfiguration thriftDataReceiverConfiguration,
                              DataBridgeReceiverService dataBridgeReceiverService) {
        this.dataBridgeReceiverService = dataBridgeReceiverService;
        this.thriftDataReceiverConfiguration = thriftDataReceiverConfiguration;
    }

    /**
     * To start the Agent server
     *
     * @throws org.wso2.carbon.databridge.core.exception.DataBridgeException
     *          if the agent server cannot be started
     */
    public void start(String hostName)
            throws DataBridgeException {
        startSecureEventTransmission(hostName, thriftDataReceiverConfiguration.getSecureDataReceiverPort(), dataBridgeReceiverService);
        startEventTransmission(hostName, thriftDataReceiverConfiguration.getDataReceiverPort(), dataBridgeReceiverService);
    }


    private void startSecureEventTransmission(String hostName, int port,
                                              DataBridgeReceiverService dataBridgeReceiverService)
            throws DataBridgeException {
        try {

            ServerConfiguration serverConfig = ServerConfiguration.getInstance();
            String keyStore = serverConfig.getFirstProperty("Security.KeyStore.Location");
            if (keyStore == null) {
                keyStore = System.getProperty("Security.KeyStore.Location");
                if (keyStore == null) {
                    throw new DataBridgeException("Cannot start agent server, not valid Security.KeyStore.Location is null");
                }
            }
            String keyStorePassword = serverConfig.getFirstProperty("Security.KeyStore.Password");
            if (keyStorePassword == null) {
                keyStorePassword = System.getProperty("Security.KeyStore.Password");
                if (keyStorePassword == null) {
                    throw new DataBridgeException("Cannot start agent server, not valid Security.KeyStore.Password is null ");
                }
            }

            startSecureEventTransmission(hostName, port, keyStore, keyStorePassword, dataBridgeReceiverService);
        } catch (TransportException e) {
            throw new DataBridgeException("Cannot start agent server on port " + port, e);
        } catch (UnknownHostException e) {
            //ignore since localhost
        }
    }

    protected void startSecureEventTransmission(String hostName, int port, String keyStore,
                                                String keyStorePassword,
                                                DataBridgeReceiverService dataBridgeReceiverService)
            throws TransportException, UnknownHostException {
        TSSLTransportFactory.TSSLTransportParameters params =
                new TSSLTransportFactory.TSSLTransportParameters();
        params.setKeyStore(keyStore, keyStorePassword);

        TServerSocket serverTransport;
        try {
            InetAddress inetAddress = InetAddress.getByName(hostName);
            serverTransport = TSSLTransportFactory.getServerSocket(
                    port, DataBridgeConstants.CLIENT_TIMEOUT_MS, inetAddress, params);
            log.info("Thrift Server started at " + hostName);
        } catch (TTransportException e) {
            throw new TransportException("Thrift transport exception occurred ", e);
        }

        ThriftSecureEventTransmissionService.Processor<ThriftSecureEventTransmissionServiceImpl> processor =
                new ThriftSecureEventTransmissionService.Processor<ThriftSecureEventTransmissionServiceImpl>(
                        new ThriftSecureEventTransmissionServiceImpl(dataBridgeReceiverService));
        authenticationServer = new TThreadPoolServer(
                new TThreadPoolServer.Args(serverTransport).processor(processor));
        Thread thread = new Thread(new ServerThread(authenticationServer));
        log.info("Thrift SSL port : " + port);
        thread.start();
    }

    protected void startEventTransmission(String hostName, int port,
                                          DataBridgeReceiverService dataBridgeReceiverService)
            throws DataBridgeException {
        try {
            TServerSocket serverTransport = new TServerSocket(
                    new InetSocketAddress(hostName, port));
            ThriftEventTransmissionService.Processor<ThriftEventTransmissionServiceImpl> processor =
                    new ThriftEventTransmissionService.Processor<ThriftEventTransmissionServiceImpl>(
                            new ThriftEventTransmissionServiceImpl(dataBridgeReceiverService));
            dataReceiverServer = new TThreadPoolServer(
                    new TThreadPoolServer.Args(serverTransport).processor(processor));
            Thread thread = new Thread(new ServerThread(dataReceiverServer));
            log.info("Thrift port : " + port);
            thread.start();
        } catch (TTransportException e) {
            throw new DataBridgeException("Cannot start Thrift server on port " + port +
                                          " on host " + hostName, e);
        }
    }

    /**
     * To stop the server
     */
    public void stop() {
        authenticationServer.stop();
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

