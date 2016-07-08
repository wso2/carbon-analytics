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

package org.wso2.carbon.gadget.template.deployer.internal.util;

import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.gadget.template.deployer.internal.GadgetTemplateDeployerConstants;
import org.wso2.carbon.gadget.template.deployer.internal.GadgetTemplateDeployerException;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    /**
     * Validate the given file path is in the parent directory itself.
     *
     * @param parentDirectory
     * @param filePath
     */
    public static void validatePath(String parentDirectory, String filePath) throws GadgetTemplateDeployerException {
        Path parentPath = Paths.get(parentDirectory);
        Path subPath = Paths.get(filePath).normalize();
        if (!subPath.normalize().startsWith(parentPath)) {
            throw new GadgetTemplateDeployerException("File path is invalid: " + filePath);
        }
    }
}
