package org.wso2.carbon.status.dashboard.core.persistence.store.impl;

import org.wso2.carbon.status.dashboard.core.persistence.store.WorkerStore;

import java.util.Map;


public class WorkerConfigStore implements WorkerStore {
    @Override
    public boolean insert(Map data) {
        return false;
    }

    @Override
    public WorkerConfiguration query(String tableName) {
        return null;
    }

    @Override
    public boolean createTableIfNotExist() {
        return false;
    }

    @Override
    public void cleanupConnections() {

    }
}
