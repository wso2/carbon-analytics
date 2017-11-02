package org.wso2.carbon.siddhi.store.api.rest;

import org.wso2.carbon.stream.processor.common.SiddhiAppRuntimeService;

/**
 * This class holds the services referenced by the store api micro services {@link StoresApi}
 */
public class SiddhiStoreDataHolder {
    private static SiddhiAppRuntimeService siddhiAppRuntimeService;
    private static SiddhiStoreDataHolder  instance = new SiddhiStoreDataHolder();

    private SiddhiStoreDataHolder() {

    }

    public static SiddhiStoreDataHolder getInstance() {
        return instance;
    }

    public SiddhiAppRuntimeService getSiddhiAppRuntimeService() {
        return siddhiAppRuntimeService;
    }

    public void setSiddhiAppRuntimeService(SiddhiAppRuntimeService siddhiAppRuntimeService) {
        SiddhiStoreDataHolder.siddhiAppRuntimeService = siddhiAppRuntimeService;
    }
}
