package org.wso2.carbon.das.jobmanager.core.util;

import org.wso2.carbon.das.jobmanager.core.bean.InterfaceConfig;
import org.wso2.carbon.das.jobmanager.core.bean.ManagerNode;
import org.wso2.carbon.das.jobmanager.core.model.ManagerNodeConfig;

public class TypeConverter {

    public static InterfaceConfig convert(org.wso2.carbon.das.jobmanager.core.model.InterfaceConfig config) {
        InterfaceConfig iConfig = new InterfaceConfig();
        iConfig.setHost(config.getHost());
        iConfig.setPort(config.getPort());
        return iConfig;
    }

    public static org.wso2.carbon.das.jobmanager.core.model.InterfaceConfig convert(InterfaceConfig config) {
        org.wso2.carbon.das.jobmanager.core.model.InterfaceConfig iConfig =
                new org.wso2.carbon.das.jobmanager.core.model.InterfaceConfig();
        iConfig.setHost(config.getHost());
        iConfig.setPort(config.getPort());
        return iConfig;
    }

    public static ManagerNodeConfig convert(ManagerNode node) {
        ManagerNodeConfig config = new ManagerNodeConfig();
        config.setId(node.getId());
        config.setHttpInterface(convert(node.getHttpInterface()));
        config.setHeartbeatInterval(node.getHeartbeatInterval());
        config.setHeartbeatMaxRetry(node.getHeartbeatMaxRetry());
        return config;
    }

    public static ManagerNode convert(ManagerNodeConfig config) {
        ManagerNode node = new ManagerNode();
        node.setId(config.getId());
        node.setHttpInterface(convert(config.getHttpInterface()));
        node.setHeartbeatInterval(config.getHeartbeatInterval());
        node.setHeartbeatMaxRetry(config.getHeartbeatMaxRetry());
        return node;
    }

}
