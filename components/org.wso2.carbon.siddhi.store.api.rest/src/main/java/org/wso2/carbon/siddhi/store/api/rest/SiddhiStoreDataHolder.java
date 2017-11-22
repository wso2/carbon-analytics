/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */

package org.wso2.carbon.siddhi.store.api.rest;

import org.wso2.carbon.stream.processor.common.SiddhiAppRuntimeService;

/**
 * This class holds the services referenced by the store api micro services {@link StoresApi}
 */
public class SiddhiStoreDataHolder {
    private SiddhiAppRuntimeService siddhiAppRuntimeService;
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
        this.siddhiAppRuntimeService = siddhiAppRuntimeService;
    }
}
