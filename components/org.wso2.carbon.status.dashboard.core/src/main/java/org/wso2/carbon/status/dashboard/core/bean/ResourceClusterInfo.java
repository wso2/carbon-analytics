package org.wso2.carbon.status.dashboard.core.bean;

public class ResourceClusterInfo {
    private String nodeId;
    private String http_host;
    private String http_port;

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getHost() {
        return http_host;
    }

    public void setHost(String http_host) {
        this.http_host = http_host;
    }

    public String getPort() {
        return http_port;
    }

    public void setPort(String http_port) {
        this.http_port = http_port;
    }
}
