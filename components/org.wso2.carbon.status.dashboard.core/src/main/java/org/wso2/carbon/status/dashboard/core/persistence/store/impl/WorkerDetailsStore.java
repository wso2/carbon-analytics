package org.wso2.carbon.status.dashboard.core.persistence.store.impl;

import org.wso2.carbon.status.dashboard.core.persistence.store.WorkerStore;

import java.util.Map;


public class WorkerDetailsStore implements WorkerStore {
    @Override
    public boolean save(Map data) {
        return false;
    }

    @Override
    public WorkerDetails load(String id) {
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
