package org.wso2.carbon.siddhi.editor.core.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Host address finder.
 */
public class HostAddressFinder {
    public static String findAddress(String hostname) throws SocketException {
        if (hostname.trim().equals("localhost") || hostname.trim().equals("127.0.0.1") || hostname.trim().equals
                ("0.0.0.0") || hostname.trim().equals("::1")) {
            Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
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
}
