/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.dashboard.mgt.gadgetrepo.handlers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.dashboard.common.DashboardConstants;
import org.wso2.carbon.dashboard.mgt.gadgetrepo.GadgetRepoContext;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class GadgetZipUploadHandler extends Handler {


    /**
     * The GadgetZipUploadHandler is manipulating the zip uploads of gadgets
     * Once a gadget is uploaded as a zip file, this handler is invoked
     */
    private static final Log log = LogFactory.getLog(GadgetZipUploadHandler.class);

    public void put(RequestContext requestContext) throws RegistryException {

        int tenantId = PrivilegedCarbonContext.getCurrentContext().getTenantId();
        Registry sysRegistry = GadgetRepoContext.getRegistryService().getConfigSystemRegistry(tenantId);

        // adding the stream from the resource to the zip input

        String gadgetName = requestContext.getResource().getProperty(DashboardConstants.GADGET_NAME);

        ZipInputStream zis = new ZipInputStream(requestContext.getResource().getContentStream());
        ZipEntry zipentry = null;
        int i;
        byte[] buff = new byte[1024];
        try {
            zipentry = zis.getNextEntry();
            if (zipentry == null) {
                throw new RegistryException("Gadget bundle should be a zip file");
            }
        } catch (IOException e) {
            log.error(e);
        }

        sysRegistry.beginTransaction();
        while (zipentry != null) {
            String entryName = zipentry.getName();

            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();

            if (zipentry.isDirectory()) {
                //if the entry is a directory creating a collection
                Collection col = sysRegistry.newCollection();
                sysRegistry.put(DashboardConstants.GS_REGISTRY_ROOT +
                                DashboardConstants.GADGET_PATH + gadgetName
                                + RegistryConstants.PATH_SEPARATOR + entryName, col);
            } else {
                //if a file, creating a resource
                Resource res = sysRegistry.newResource();
                try {
                    while ((i = zis.read(buff, 0, buff.length)) > 0) {
                        byteOut.write(buff, 0, i);
                    }
                } catch (IOException ioe) {
                    sysRegistry.rollbackTransaction();
                    log.error(ioe);
                }

                res.setContent(byteOut.toByteArray());

                // If entry is gadget xml then set its media type.
                if (entryName.contains(".xml")) {
                    res.setMediaType("application/vnd.wso2-gadget+xml");
                }

                sysRegistry.put(DashboardConstants.GS_REGISTRY_ROOT +
                                DashboardConstants.GADGET_PATH + gadgetName +
                                RegistryConstants.PATH_SEPARATOR + entryName, res);

            }
            try {
                zipentry = zis.getNextEntry();
            } catch (IOException e) {
                sysRegistry.rollbackTransaction();
                log.error(e);
            }
        }

        try {
            zis.close();
        } catch (IOException e) {
            log.error(e);
        }

        sysRegistry.commitTransaction();
        requestContext.setProcessingComplete(true);
    }

}
