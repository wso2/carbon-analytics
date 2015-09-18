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
package org.wso2.carbon.event.processor.manager.commons.utils;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.List;

public class Utils {
    public static String findAddress(String hostname) throws SocketException {
        if (hostname.trim().equals("localhost") || hostname.trim().equals("127.0.0.1") || hostname.trim().equals("::1")) {
            Enumeration<NetworkInterface> ifaces =
                    NetworkInterface.getNetworkInterfaces();
            while (ifaces.hasMoreElements()) {
                NetworkInterface iface = ifaces.nextElement();
                Enumeration<InetAddress> addresses = iface.getInetAddresses();

                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
            return "127.0.0.1";
        } else {
            return hostname;
        }
    }

    public static boolean isPortUsed(final int portNumber, final String host) {
        boolean isPortUsed;
        ServerSocket serverSocket = null;
        try {
            InetAddress inetAddress = InetAddress.getByName(host);
            serverSocket = new ServerSocket(portNumber, 50, inetAddress);
            isPortUsed = false;
        } catch (IOException ignored) {
            isPortUsed = true;
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    isPortUsed = true;
                }
            }
        }
        if (!isPortUsed) {
            Socket socket = null;
            try {
                socket = new Socket("localhost", portNumber);
                isPortUsed = true;
            } catch (IOException ignored) {
                isPortUsed = false;
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                        isPortUsed = true;
                    } catch (IOException e) {
                        isPortUsed = true;
                    }
                }
            }
        }
        return isPortUsed;
    }

    public static String constructQueryExpression(List<String> importDefinitions, List<String> exportDefinitions,
                                                  String queryExpressions) {
        StringBuilder builder = new StringBuilder();

        for (String definition : importDefinitions) {
            builder.append(definition);
        }

        for (String definition : exportDefinitions) {
            builder.append(definition);
        }
        builder.append(queryExpressions);
        return builder.toString();
    }
}
