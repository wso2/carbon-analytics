/**
 *
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


package org.wso2.carbon.databridge.agent.thrift.conf;

/**
 * configuration details of Agent Server Endpoint
 */
public class ReceiverConfiguration {

    public enum Protocol {
        TCP, HTTP
    }

    private String userName;
    private String password;
    private String dataReceiverIp;
    private int dataReceiverPort;
    private String secureDataReceiverIp;
    private int secureDataReceiverPort;
    private boolean dataTransferSecured = false;
    private Protocol dataReceiverProtocol = Protocol.TCP;
    private Protocol secureDataReceiverProtocol = Protocol.TCP;

    public ReceiverConfiguration(String userName, String password, Protocol dataReceiverProtocol,
                                 String dataReceiverIp,
                                 int dataReceiverPort, Protocol secureDataReceiverProtocol,
                                 String secureDataReceiverIp,
                                 int secureDataReceiverPort, boolean secured) {
        this.dataReceiverProtocol = dataReceiverProtocol;
        this.secureDataReceiverProtocol = secureDataReceiverProtocol;
        this.userName = userName;
        this.password = password;
        this.dataReceiverIp = dataReceiverIp;
        this.dataReceiverPort = dataReceiverPort;
        this.secureDataReceiverIp = secureDataReceiverIp;
        this.secureDataReceiverPort = secureDataReceiverPort;
        this.dataTransferSecured = secured;
    }

    public String getDataReceiverIp() {
        return dataReceiverIp;
    }

    public void setDataReceiverIp(String dataReceiverIp) {
        this.dataReceiverIp = dataReceiverIp;
    }

    public int getDataReceiverPort() {
        return dataReceiverPort;
    }

    public void setDataReceiverPort(int dataReceiverPort) {
        this.dataReceiverPort = dataReceiverPort;
    }

    public String getSecureDataReceiverIp() {
        return secureDataReceiverIp;
    }

    public void setSecureDataReceiverIp(String secureDataReceiverIp) {
        this.secureDataReceiverIp = secureDataReceiverIp;
    }

    public int getSecureDataReceiverPort() {
        return secureDataReceiverPort;
    }

    public void setSecureDataReceiverPort(int secureDataReceiverPort) {
        this.secureDataReceiverPort = secureDataReceiverPort;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isDataTransferSecured() {
        return dataTransferSecured;
    }

    public void setDataTransferSecured(boolean dataTransferSecured) {
        this.dataTransferSecured = dataTransferSecured;
    }

    public Protocol getDataReceiverProtocol() {
        return dataReceiverProtocol;
    }

    public void setDataReceiverProtocol(Protocol dataReceiverProtocol) {
        this.dataReceiverProtocol = dataReceiverProtocol;
    }

    public Protocol getSecureDataReceiverProtocol() {
        return secureDataReceiverProtocol;
    }

    public void setSecureDataReceiverProtocol(
            Protocol secureDataReceiverProtocol) {
        this.secureDataReceiverProtocol = secureDataReceiverProtocol;
    }
}
