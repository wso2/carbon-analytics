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

package org.wso2.carbon.databridge.receiver.binary.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.commons.binary.BinaryMessageConstants;
import org.wso2.carbon.databridge.core.DataBridgeReceiverService;
import org.wso2.carbon.databridge.core.exception.DataBridgeException;
import org.wso2.carbon.databridge.receiver.binary.BinaryEventConverter;
import org.wso2.carbon.databridge.receiver.binary.conf.BinaryDataReceiverConfiguration;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;

import static org.wso2.carbon.databridge.commons.binary.BinaryMessageConverterUtil.loadData;

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
        this.sslReceiverExecutorService = new BinaryDataReceiverThreadPoolExecutor(binaryDataReceiverConfiguration.
                getSizeOfSSLThreadPool(), "Receiver-Binary-SSL");
        this.tcpReceiverExecutorService = new BinaryDataReceiverThreadPoolExecutor(binaryDataReceiverConfiguration.
                getSizeOfTCPThreadPool(), "Receiver-Binary-TCP");
    }

    public void start() throws IOException, DataBridgeException {
        startSecureTransmission();
        startEventTransmission();
    }

    public void stop() {
        log.info("Stopping Binary Server..");
        sslReceiverExecutorService.shutdown();
        tcpReceiverExecutorService.shutdown();
    }

    private void startSecureTransmission() throws IOException, DataBridgeException {

        //TODO Find a way to get this info from carbon and use in default case.
        String keyStore = dataBridgeReceiverService.getInitialConfig().getKeyStoreLocation();
        if (keyStore == null) {
//            String carbonHome = Utils.getCarbonHome().toString();
//            if(carbonHome != null){
//                keyStore = carbonHome + File.separator + "resources"+ File.separator +
//                           "resources/security" +File.separator + "wso2carbon.jks";
//
//            } else {
            keyStore = System.getProperty("Security.KeyStore.Location");
            if (keyStore == null) {
                throw new DataBridgeException("Cannot start binary agent server, " +
                                              "not valid Security.KeyStore.Location is null");
            }
            // }
        }

        String keyStorePassword = dataBridgeReceiverService.getInitialConfig().getKeyStorePassword();
        if (keyStorePassword == null) {
            keyStorePassword = "wso2carbon";
            if (keyStorePassword == null) {
                keyStorePassword = System.getProperty("Security.KeyStore.Password");
                if (keyStorePassword == null) {
                    throw new DataBridgeException("Cannot start binary agent server, not valid Security.KeyStore.Password is null ");
                }
            }
        }

        System.setProperty("javax.net.ssl.keyStore", keyStore);
        System.setProperty("javax.net.ssl.keyStorePassword", keyStorePassword);
        SSLServerSocketFactory sslserversocketfactory =
                (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        SSLServerSocket sslserversocket =
                (SSLServerSocket) sslserversocketfactory.createServerSocket(binaryDataReceiverConfiguration.getSSLPort());

        String sslProtocols = binaryDataReceiverConfiguration.getSslProtocols();
        if (sslProtocols != null && sslProtocols.length() != 0) {
            String[] sslProtocolsArray = sslProtocols.split(",");
            sslserversocket.setEnabledProtocols(sslProtocolsArray);
        }

        String ciphers = binaryDataReceiverConfiguration.getCiphers();
        if (ciphers != null && ciphers.length() != 0) {
            String[] ciphersArray = ciphers.split(",");
            sslserversocket.setEnabledCipherSuites(ciphersArray);
        } else {
            sslserversocket.setEnabledCipherSuites(sslserversocket.getSupportedCipherSuites());
        }

        Thread thread = new Thread(new BinarySecureEventServerAcceptor(sslserversocket));
        thread.start();
        log.info("Started Binary SSL Transport on port : " + binaryDataReceiverConfiguration.getSSLPort());
    }

    private void startEventTransmission() throws IOException {
        ServerSocketFactory serversocketfactory = ServerSocketFactory.getDefault();
        ServerSocket serversocket = serversocketfactory.createServerSocket(binaryDataReceiverConfiguration.getTCPPort());
        Thread thread = new Thread(new BinaryEventServerAcceptor(serversocket));
        thread.start();
        log.info("Started Binary TCP Transport on port : " + binaryDataReceiverConfiguration.getTCPPort());
    }

    private String processMessage(int messageType, byte[] message, OutputStream outputStream) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(message);
        int sessionIdLength;
        String sessionId;

        switch (messageType) {
            case 0: //Login
                int userNameLength = byteBuffer.getInt();
                int passwordLength = byteBuffer.getInt();

                String userName = new String(message, 8, userNameLength);
                String password = new String(message, 8 + userNameLength, passwordLength);

                try {
                    sessionId = dataBridgeReceiverService.login(userName, password);

                    ByteBuffer buffer = ByteBuffer.allocate(5 + sessionId.length());
                    buffer.put((byte) 2);
                    buffer.putInt(sessionId.length());
                    buffer.put(sessionId.getBytes(BinaryMessageConstants.DEFAULT_CHARSET));

                    outputStream.write(buffer.array());
                    outputStream.flush();
                } catch (Exception e) {
                    try {
                        sendError(e, outputStream);
                    } catch (IOException e1) {
                        log.error("Error while sending response for login message: " + e1.getMessage(), e1);
                    }
                }
                break;
            case 1://Logout
                sessionIdLength = byteBuffer.getInt();
                sessionId = new String(message, 4, sessionIdLength);
                try {
                    dataBridgeReceiverService.logout(sessionId);

                    outputStream.write((byte) 0);
                    outputStream.flush();
                } catch (Exception e) {
                    try {
                        sendError(e, outputStream);
                    } catch (IOException e1) {
                        log.error("Error while sending response for login message: " + e1.getMessage(), e1);
                    }
                }
                break;
            case 2: //Publish
                sessionIdLength = byteBuffer.getInt();
                sessionId = new String(message, 4, sessionIdLength);
                try {
                    dataBridgeReceiverService.publish(message, sessionId, BinaryEventConverter.getConverter());

                    outputStream.write((byte) 0);
                    outputStream.flush();
                } catch (Exception e) {
                    try {
                        sendError(e, outputStream);
                    } catch (IOException e1) {
                        log.error("Error while sending response for login message: " + e1.getMessage(), e1);
                    }
                }
                break;
            default:
                log.error("Message Type " + messageType + " is not supported!");
        }
        return null;
    }

    private void sendError(Exception e, OutputStream outputStream) throws IOException {

        int errorClassNameLength = e.getClass().getCanonicalName().length();
        int errorMsgLength = e.getMessage().length();

        ByteBuffer bbuf = ByteBuffer.wrap(new byte[8]);
        bbuf.putInt(errorClassNameLength);
        bbuf.putInt(errorMsgLength);

        outputStream.write((byte) 1);//Error
        outputStream.write(bbuf.array());
        outputStream.write(e.getClass().getCanonicalName().getBytes(BinaryMessageConstants.DEFAULT_CHARSET));
        outputStream.write(e.getMessage().getBytes(BinaryMessageConstants.DEFAULT_CHARSET));
        outputStream.flush();
    }

    public class BinarySecureEventServerAcceptor implements Runnable {
        private ServerSocket serverSocket;

        public BinarySecureEventServerAcceptor(ServerSocket serverSocket) {
            this.serverSocket = serverSocket;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Socket socket = this.serverSocket.accept();
                    sslReceiverExecutorService.submit(new BinaryTransportReceiver(socket));
                } catch (IOException e) {
                    log.error("Error while accepting the connection. ", e);
                }
            }
        }
    }

    public class BinaryEventServerAcceptor implements Runnable {
        private ServerSocket serverSocket;

        public BinaryEventServerAcceptor(ServerSocket serverSocket) {
            this.serverSocket = serverSocket;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Socket socket = this.serverSocket.accept();
                    tcpReceiverExecutorService.submit(new BinaryTransportReceiver(socket));
                } catch (IOException e) {
                    log.error("Error while accepting the connection. ", e);
                }
            }
        }
    }

    public class BinaryTransportReceiver implements Runnable {
        private Socket socket;

        public BinaryTransportReceiver(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                InputStream inputstream = new BufferedInputStream(socket.getInputStream());
                OutputStream outputStream = new BufferedOutputStream((socket.getOutputStream()));
                int messageType = inputstream.read();
                while (messageType != -1) {
                    int messageSize = ByteBuffer.wrap(loadData(inputstream, new byte[4])).getInt();
                    byte[] message = loadData(inputstream, new byte[messageSize]);
                    processMessage(messageType, message, outputStream);
                    messageType = inputstream.read();
                }
            } catch (IOException ex) {
                log.error("Error while reading from the socket. ", ex);
            }
        }
    }
}
