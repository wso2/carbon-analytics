/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.dashboard.template.deployer.internal.util;

import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;

public class DashboardTemplateDeployerUtility {
    private DashboardTemplateDeployerUtility() {
    }

    /**
     * Return the Carbon Registry.
     *
     * @return
     */
    public static Registry getRegistry() {
        CarbonContext cCtx = CarbonContext.getThreadLocalCarbonContext();
        Registry registry = cCtx.getRegistry(RegistryType.SYSTEM_CONFIGURATION);
        return registry;
    }

    public static void createRegistryResource(String url, Object content) throws RegistryException {
        Registry registry = getRegistry();
        Resource resource = registry.newResource();
        resource.setContent(content);
        resource.setMediaType("application/json");
        registry.put(url, resource);
    }

    public static void removeRegistryResource(String resourcePath) throws RegistryException {
        Registry registry = getRegistry();
        if (registry.resourceExists(resourcePath)) {
            registry.delete(resourcePath);
        }
    }
}
