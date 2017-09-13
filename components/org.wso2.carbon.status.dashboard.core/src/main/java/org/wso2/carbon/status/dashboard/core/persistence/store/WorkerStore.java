package org.wso2.carbon.status.dashboard.core.persistence.store;


import java.util.Map;

/**
 * .
 */
public interface WorkerStore {
    public boolean connect(String datasourceName);
    public boolean insert(String tableName, Map values);
    public boolean delete(String tableName , String condition);
    public Object select(String tableName , String condition);
    public Object update(String tableName , String condition , Map values);
    public boolean isTupleAvailable(String tableName , String condition);
    public boolean createTableIfNotExist();
    public void cleanupConnections();
}
