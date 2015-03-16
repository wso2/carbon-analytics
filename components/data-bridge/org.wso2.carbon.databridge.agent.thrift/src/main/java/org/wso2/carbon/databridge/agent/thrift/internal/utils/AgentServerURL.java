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
package org.wso2.carbon.databridge.agent.thrift.internal.utils;

import org.wso2.carbon.databridge.agent.thrift.conf.ReceiverConfiguration;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * The Agent server endpoint url
 */
public class AgentServerURL {
    private ReceiverConfiguration.Protocol protocol;
    private String host;
    private int port;
    private boolean isSecured = false;

    public AgentServerURL(String url) throws MalformedURLException {
        URL theUrl;
        if (url.startsWith("http:")) {
            this.protocol= ReceiverConfiguration.Protocol.HTTP;
            theUrl = new URL(url);
            isSecured=false;
        } else  if (url.startsWith("https:")) {
            this.protocol= ReceiverConfiguration.Protocol.HTTP;
            theUrl = new URL(url);
            isSecured=true;
        } else  if (url.startsWith("tcp:")) {
            theUrl = new URL(url.replaceFirst("tcp", "http"));
            this.protocol= ReceiverConfiguration.Protocol.TCP;
            isSecured=false;
        } else if (url.startsWith("ssl:")) {
            this.protocol= ReceiverConfiguration.Protocol.TCP;
            theUrl = new URL(url.replaceFirst("ssl", "http"));
            isSecured=true;
        } else {
            throw new MalformedURLException("The url protocol is not tcp or ssl " + url);
        }
        this.host = theUrl.getHost();
        this.port = theUrl.getPort();
    }

    public ReceiverConfiguration.Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(ReceiverConfiguration.Protocol protocol) {
        this.protocol = protocol;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isSecured() {
        return isSecured;
    }

    public void setSecured(boolean secured) {
        isSecured = secured;
    }
}
