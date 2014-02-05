package org.wso2.carbon.dashboard;

import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.registry.app.Utils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.resource.beans.ContentDownloadBean;
import org.wso2.carbon.registry.resource.services.utils.GetDownloadContentUtil;

/**
 * The service pick gadget specs from the registry and returns them to the FE
 */
public class GadgetContentDownloadService extends AbstractAdmin {
    public Registry getRootRegistry(String tDomain) throws Exception {
        if (getHttpSession() != null) {
            Registry registry =
                    (Registry) getHttpSession().getAttribute(
                            RegistryConstants.ROOT_REGISTRY_INSTANCE);
            if (registry != null) {
                return registry;
            } else {
                if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tDomain)) {
                    // getting supper tenant registry
                    registry = Utils.getEmbeddedRegistryService().getRegistry(
                            CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME, RegistryContext.getBaseInstance().getRealmService().
                            getTenantManager().getTenantId(getTenantDomain()));
                } else {
                    // getting tennant specific registry
                    registry = Utils.getEmbeddedRegistryService().getRegistry(
                            CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME, RegistryContext.getBaseInstance().getRealmService().
                            getTenantManager().getTenantId(tDomain));
                }
                return registry;
            }
        }
        return null;
    }

    /**
     *
     * @param path of the resource which needs to downloaded
     * @param tDomain tenantDomain (need to pass this because this value is not avilable here)
     * @return ContentDownloadBean
     * @throws Exception
     */
    public ContentDownloadBean getContentDownloadBean(String path, String tDomain) throws Exception {
        UserRegistry userRegistry = (UserRegistry) getRootRegistry(tDomain);
        return GetDownloadContentUtil.getContentDownloadBean(path, userRegistry);
    }
}
