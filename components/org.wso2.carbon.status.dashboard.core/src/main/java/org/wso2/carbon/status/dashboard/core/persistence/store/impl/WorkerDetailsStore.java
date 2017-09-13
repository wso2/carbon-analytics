package org.wso2.carbon.status.dashboard.core.persistence.store.impl;

import com.zaxxer.hikari.HikariDataSource;
import org.wso2.carbon.status.dashboard.core.persistence.datasourceServicers.StatusDashboardWorkerDataHolder;
import org.wso2.carbon.status.dashboard.core.persistence.store.WorkerStore;

import java.util.Map;

/**
 * .
 */
public class WorkerDetailsStore implements WorkerStore {

    @Override
    public boolean connect(String datasourceName) {
        HikariDataSource dataHolder = StatusDashboardWorkerDataHolder.getInstance().getDataSource();
        
        return false;
    }

    @Override
    public boolean insert(String tableName, Map values) {
        return false;
    }

    @Override
    public boolean delete(String tableName, String condition) {
        return false;
    }

    @Override
    public Object select(String tableName, String condition) {
        return null;
    }

    @Override
    public Object update(String tableName, String condition, Map values) {
        return null;
    }

    @Override
    public boolean isTupleAvailable(String tableName, String condition) {
        return false;
    }

    @Override
    public boolean createTableIfNotExist() {
        return false;
    }

    @Override
    public void cleanupConnections() {

    }
}
