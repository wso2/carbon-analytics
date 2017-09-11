package org.wso2.carbon.status.dashboard.core.persistence.store;


import java.util.Map;

/**
 * .
 */
public interface WorkerStore {
    public boolean save(Map data);
    public Object load(String id);
    public boolean createTableIfNotExist();
    public void cleanupConnections();
}
