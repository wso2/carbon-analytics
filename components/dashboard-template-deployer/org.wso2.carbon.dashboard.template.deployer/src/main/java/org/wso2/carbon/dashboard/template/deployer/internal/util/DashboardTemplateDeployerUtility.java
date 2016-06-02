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
