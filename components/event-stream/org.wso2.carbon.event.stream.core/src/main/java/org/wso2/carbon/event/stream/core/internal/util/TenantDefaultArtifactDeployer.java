/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.event.stream.core.internal.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.File;
import java.io.IOException;

//todo: remove
public class TenantDefaultArtifactDeployer {
    public static final String DEFAULT_CONF_LOCATION = CarbonUtils.getCarbonHome() + File.separatorChar
            + "repository" + File.separatorChar +
            "conf" + File.separatorChar + "cep" + File.separatorChar + "default-artifacts";

    private static final Log log = LogFactory.getLog(TenantDefaultArtifactDeployer.class);

    public static void deployDefaultArtifactsForTenant(int tenantId){
        try {
            File defaultArtifactDir = new File(DEFAULT_CONF_LOCATION);

            if (!defaultArtifactDir.exists() || !defaultArtifactDir.isDirectory()){
                log.warn("Default artifacts are not available at  " + DEFAULT_CONF_LOCATION);
                return;
            }

            FileUtils.copyDirectory(new File(DEFAULT_CONF_LOCATION),
                    new File(MultitenantUtils.getAxis2RepositoryPath(tenantId)));
            log.info("Successfully deployed default artifacts for tenant id " + tenantId);

        } catch (IOException e) {
            log.warn("Could not deploy default artifacts for the tenant : " + e.getMessage(), e);
        }
    }
}
