package org.wso2.carbon.bam.dashboard.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.dashboard.BAMDashboardConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.*;

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
public class BAMDashboardAxisConfigurationObserverImp extends AbstractAxis2ConfigurationContextObserver {
    private static final Log log = LogFactory.getLog(BAMDashboardAxisConfigurationObserverImp.class);

    public void creatingConfigurationContext(int tenantId) {

    }

    public void createdConfigurationContext(ConfigurationContext configContext) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String tenantDashboard = CarbonUtils.getCarbonTenantsDirPath() + File.separator+ tenantId+
                File.separator+ BAMDashboardConstants.TENANT_JAGGERY_APP_PATH+File.separator+BAMDashboardConstants.BAM_DASHBOARD_APP;
        if (!isBAMDashboardInitialized(tenantDashboard)) {
            String sourceResourceLocation =
                    CarbonUtils.getCarbonHome() + File.separator +
                            BAMDashboardConstants.REPOSITORY_RESOURCE_PATH;
            try {
                copyFolder(new File(sourceResourceLocation), new File(tenantDashboard));
            } catch (IOException e) {
               log.error("Error while initializing the tenant BAM dashboard for tenant :"+
                       PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true)
                       +e.getMessage());
            }
        }

    }

    private boolean isBAMDashboardInitialized(String jaggeryashboardPath) {
        File file = new File(jaggeryashboardPath);
        boolean exists = true;
        if (!file.exists()) {
            exists = false;
            file.mkdirs();
        }
        return exists;
    }

    private void copyFolder(File src, File dest)
            throws IOException {

        if (src.isDirectory()) {
            //if directory not exists, create it
            if (!dest.exists()) {
                dest.mkdir();
            }

            //list all the directory contents
            String files[] = src.list();

            for (String file : files) {
                //construct the src and dest file structure
                File srcFile = new File(src, file);
                File destFile = new File(dest, file);
                //recursive copy
                copyFolder(srcFile, destFile);
            }

        } else {
            //if file, then copy it
            //Use bytes stream to support all file types
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dest);

            byte[] buffer = new byte[1024];

            int length;
            //copy the file content in bytes
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }

            in.close();
            out.close();
        }
    }
}
