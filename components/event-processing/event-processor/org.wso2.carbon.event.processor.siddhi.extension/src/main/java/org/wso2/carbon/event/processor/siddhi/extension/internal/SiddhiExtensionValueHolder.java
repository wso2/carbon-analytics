/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.event.processor.siddhi.extension.internal;

import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;


public class SiddhiExtensionValueHolder {

    private static SiddhiExtensionValueHolder instance;
    private static RegistryService registryService;

    public static SiddhiExtensionValueHolder getInstance() {
        if (instance == null) {
            instance = new SiddhiExtensionValueHolder();
        }
        return instance;
    }

    public void setRegistryService(RegistryService registryService) {
        SiddhiExtensionValueHolder.registryService = registryService;
    }

    public void unSetRegistryService() {
        SiddhiExtensionValueHolder.registryService = null;
    }

    public RegistryService getRegistryService() {
        return SiddhiExtensionValueHolder.registryService;
    }

    public Registry getConfigRegistry(int tenantId) throws RegistryException {
        return registryService.getConfigSystemRegistry(tenantId);
    }

    public Registry getGovernanceRegistry(int tenantId) throws RegistryException {
        return registryService.getGovernanceSystemRegistry(tenantId);
    }

}
