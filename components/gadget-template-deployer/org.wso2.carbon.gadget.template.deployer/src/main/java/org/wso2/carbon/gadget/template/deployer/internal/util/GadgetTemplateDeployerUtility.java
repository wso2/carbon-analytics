package org.wso2.carbon.gadget.template.deployer.internal.util;

import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.gadget.template.deployer.internal.GadgetTemplateDeployerConstants;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;

public class GadgetTemplateDeployerUtility {

    private GadgetTemplateDeployerUtility() {
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

    /**
     * Returns the absolute path for the artifact store location.
     *
     * @return path of the artifact
     */
    public static String getGadgetArtifactPath() {
        String carbonRepository = CarbonUtils.getCarbonRepository();
        StringBuilder sb = new StringBuilder(carbonRepository);
        sb.append("jaggeryapps").append(File.separator).append(GadgetTemplateDeployerConstants.APP_NAME).append(File.separator)
                .append("store").append(File.separator)
                .append(CarbonContext.getThreadLocalCarbonContext().getTenantDomain()).append(File.separator)
                .append(GadgetTemplateDeployerConstants.DEFAULT_STORE_TYPE).append(File.separator).append(GadgetTemplateDeployerConstants.ARTIFACT_TYPE)
                .append(File.separator);
        return sb.toString();
    }
}
