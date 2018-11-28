package org.zhenchao.dora.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author zhenchao.wang 2018-08-08 14:31
 * @version 1.0.0
 */
public class NetworkUtils {

    private static final Logger log = LoggerFactory.getLogger(NetworkUtils.class);

    public static String hostname() {
        String hostname = null;
        try {
            hostname = InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException e) {
            log.error("get local hostname exception", e);
        }
        return hostname;
    }

    public static String ip() {
        String ip = null;
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.error("get local ip exception", e);
        }
        return ip;
    }

    public static String host2Ip(String host) {
        InetAddress address;
        try {
            address = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            log.warn("Can't convert hostname({}) to ip, return hostname.", host, e);
            return host;
        }
        return address.getHostAddress();
    }

    public static String ip2Host(String ip) {
        InetAddress address;
        try {
            address = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            log.warn("Can't convert ip({}) to hostname, return ip.", ip, e);
            return ip;
        }
        return address.getHostName();
    }

}
