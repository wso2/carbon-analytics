package org.wso2.carbon.status.dashboard.core.persistence.store.impl;

import org.wso2.carbon.status.dashboard.core.persistence.store.WorkerStore;

import java.util.Map;


public class WorkerConfigStore implements WorkerStore {


    public boolean connect(String datasourceName) {
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
    public boolean isTableExist() {
        return false;
    }

    @Override
    public void cleanupConnections() {

    }
}
