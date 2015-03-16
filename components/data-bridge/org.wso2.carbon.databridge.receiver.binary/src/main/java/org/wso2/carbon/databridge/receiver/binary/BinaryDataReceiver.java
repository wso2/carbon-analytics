/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.databridge.receiver.binary;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.databridge.commons.binary.BinaryMessageConverterUtil;
import org.wso2.carbon.databridge.core.DataBridgeReceiverService;
import org.wso2.carbon.databridge.core.exception.DataBridgeException;
import org.wso2.carbon.databridge.receiver.binary.conf.BinaryDataReceiverConfiguration;
import org.wso2.carbon.databridge.receiver.binary.internal.RequestProcessor;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Binary Transport Receiver implementation.
 */
public class BinaryDataReceiver {
    private static final Log log = LogFactory.getLog(BinaryDataReceiver.class);
    private DataBridgeReceiverService dataBridgeReceiverService;
    private BinaryDataReceiverConfiguration binaryDataReceiverConfiguration;
    private ExecutorService sslReceiverExecutorService;
    private ExecutorService tcpReceiverExecutorService;

    public BinaryDataReceiver(BinaryDataReceiverConfiguration binaryDataReceiverConfiguration,
                              DataBridgeReceiverService dataBridgeReceiverService) {
        this.dataBridgeReceiverService = dataBridgeReceiverService;
        this.binaryDataReceiverConfiguration = binaryDataReceiverConfiguration;
        this.sslReceiverExecutorService = Executors.newFixedThreadPool(binaryDataReceiverConfiguration.
                getSizeOfSSLThreadPool());
        this.tcpReceiverExecutorService = Executors.newFixedThreadPool(binaryDataReceiverConfiguration.
                getSizeOfTCPThreadPool());
    }

    public void start() throws IOException, DataBridgeException {
        startSecureTransmission();
        startEventTransmission();
    }

    public void stop(){
        sslReceiverExecutorService.shutdownNow();
        tcpReceiverExecutorService.shutdownNow();
    }

    private void startSecureTransmission() throws IOException, DataBridgeException {
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
        System.setProperty("javax.net.ssl.keyStore", keyStore);
        System.setProperty("javax.net.ssl.keyStorePassword", keyStorePassword);
        SSLServerSocketFactory sslserversocketfactory =
                (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        SSLServerSocket sslserversocket =
                (SSLServerSocket) sslserversocketfactory.createServerSocket(binaryDataReceiverConfiguration.getSSLPort());
        sslserversocket.setEnabledCipherSuites(sslserversocket.getSupportedCipherSuites());
        for (int i = 0; i < binaryDataReceiverConfiguration.getSizeOfSSLThreadPool(); i++) {
            sslReceiverExecutorService.execute(new BinaryTransportReceiver(sslserversocket));
        }
        log.info("Started Binary SSL Transport on port : " + binaryDataReceiverConfiguration.getSSLPort());
    }

    private void startEventTransmission() throws IOException {
        ServerSocketFactory serversocketfactory = ServerSocketFactory.getDefault();
        ServerSocket serversocket = serversocketfactory.createServerSocket(binaryDataReceiverConfiguration.getTCPPort());
        for (int i = 0; i < binaryDataReceiverConfiguration.getSizeOfTCPThreadPool(); i++) {
            tcpReceiverExecutorService.submit(new BinaryTransportReceiver(serversocket));
        }
        log.info("Started Binary TCP Transport on port : " + binaryDataReceiverConfiguration.getTCPPort());
    }

    public class BinaryTransportReceiver implements Runnable {
        private ServerSocket serverSocket;

        public BinaryTransportReceiver(ServerSocket serverSocket) {
            this.serverSocket = serverSocket;
        }

        @Override
        public void run() {
            Socket socket;
            try {
                /*
                Always server needs to listen and accept the socket connection
                 */
                while (true) {
                    socket = this.serverSocket.accept();
                    InputStream inputstream = socket.getInputStream();
                    InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
                    BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
                    BufferedWriter outputStream = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    String messageLine;
                    RequestProcessor requestProcessor = new RequestProcessor(dataBridgeReceiverService);
                    while ((messageLine = bufferedreader.readLine()) != null) {
                        if (requestProcessor.isMessageEnded()) {
                            requestProcessor = new RequestProcessor(dataBridgeReceiverService);
                        }
                        try {
                            String response = requestProcessor.consume(messageLine);
                            if (response != null) {
                                outputStream.write(response);
                                outputStream.flush();
                            }
                        } catch (Exception e) {
                            String errorMsg = "Error occurred while reading the message. " + e.getMessage();
                            log.error(errorMsg, e);
                            outputStream.write(BinaryMessageConverterUtil.getCompleteError(errorMsg, e));
                            outputStream.flush();
                            break;
                        }
                    }
                }
            } catch (IOException ex) {
                log.error("Error while creating SSL socket on port : " + binaryDataReceiverConfiguration.getSizeOfTCPThreadPool(), ex);
            }
        }
    }
}
