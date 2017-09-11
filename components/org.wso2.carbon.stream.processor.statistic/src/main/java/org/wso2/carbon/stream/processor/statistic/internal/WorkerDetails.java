package org.wso2.carbon.stream.processor.statistic.internal;

import org.wso2.carbon.kernel.config.model.PortsConfig;
import org.wso2.carbon.kernel.config.model.StartupResolverConfig;

/**
 * .
 */
public class WorkerDetails {
    private String id = "0.0.0.0:9443";
    private String name = "PRODUCT SP";
    private String tenant = "0.0.0.0";
    private PortsConfig ports = new PortsConfig();
    private StartupResolverConfig startupResolver = new StartupResolverConfig();

    public WorkerDetails(String id) {
        this.id = id;
    }
    public WorkerDetails() {
    }
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public PortsConfig getPorts() {
        return ports;
    }

    public void setPorts(PortsConfig ports) {
        this.ports = ports;
    }

    public StartupResolverConfig getStartupResolver() {
        return startupResolver;
    }

    public void setStartupResolver(StartupResolverConfig startupResolver) {
        this.startupResolver = startupResolver;
    }

}
