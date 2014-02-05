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
package org.wso2.carbon.dashboard.mgt.theme.handlers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.dashboard.common.DashboardConstants;
import org.wso2.carbon.dashboard.mgt.theme.Utils.ThemeMgtContext;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.session.CurrentSession;
import org.wso2.carbon.user.core.UserStoreException;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * The ThemeUploadHandler is manipulating the zip uploads of themes
 * Once a theme is uploaded as a zip file, this handler is invoked
 */
public class ThemeUploadHandler extends Handler {
    private static final Log log = LogFactory.getLog(ThemeUploadHandler.class);

    public void put(RequestContext requestContext) throws RegistryException {
        Registry sysRegistry = ThemeMgtContext.getRegistryService().getConfigSystemRegistry();

        try {
            //Authorizing the user for his/her theme space
            /*if (!CurrentSession.getUserRealm().getAuthorizationManager().isRoleAuthorized(CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME,
                    RegistryConstants.CONFIG_REGISTRY_BASE_PATH + DashboardConstants.USER_DASHBOARD_REGISTRY_ROOT +
                            CurrentSession.getUser() + DashboardConstants.THEME_USER_PATH,
                    ActionConstants.GET)) {

                CurrentSession.getUserRealm().getAuthorizationManager().authorizeRole(CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME,
                        RegistryConstants.CONFIG_REGISTRY_BASE_PATH + DashboardConstants.USER_DASHBOARD_REGISTRY_ROOT +
                                CurrentSession.getUser() + DashboardConstants.THEME_USER_PATH,
                        ActionConstants.GET);*/

            if (!CurrentSession.getUserRealm().getAuthorizationManager().isUserAuthorized(CurrentSession.getUser(),
                    RegistryConstants.CONFIG_REGISTRY_BASE_PATH + DashboardConstants.USER_DASHBOARD_REGISTRY_ROOT +
                            CurrentSession.getUser() + DashboardConstants.THEME_USER_PATH,
                    ActionConstants.GET)) {

                CurrentSession.getUserRealm().getAuthorizationManager().authorizeUser(CurrentSession.getUser(),
                        RegistryConstants.CONFIG_REGISTRY_BASE_PATH + DashboardConstants.USER_DASHBOARD_REGISTRY_ROOT +
                                CurrentSession.getUser() + DashboardConstants.THEME_USER_PATH,
                        ActionConstants.GET);

/*                CurrentSession.getUserRealm().getAuthorizationManager().authorizeRole(CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME,
                        RegistryConstants.CONFIG_REGISTRY_BASE_PATH + DashboardConstants.USER_DASHBOARD_REGISTRY_ROOT +
                                CurrentSession.getUser() + DashboardConstants.THEME_USER_PATH,
                        ActionConstants.PUT);

                CurrentSession.getUserRealm().getAuthorizationManager().authorizeRole(CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME,
                        RegistryConstants.CONFIG_REGISTRY_BASE_PATH + DashboardConstants.USER_DASHBOARD_REGISTRY_ROOT +
                                CurrentSession.getUser() + DashboardConstants.THEME_USER_PATH,
                        ActionConstants.DELETE);*/
            }
        } catch (UserStoreException use) {
            log.error("Exception when providing permission : ", use);
        }

        //adding the stream from the resource to the zip input
        ZipInputStream zis = new ZipInputStream(requestContext.getResource().getContentStream());
        ZipEntry zipentry = null;
        int i;
        byte[] buff = new byte[1024];
        try {
            zipentry = zis.getNextEntry();
            if (zipentry == null) {
                throw new RegistryException("Theme bundle should be a zip file");
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
                sysRegistry.put(DashboardConstants.USER_DASHBOARD_REGISTRY_ROOT +
                        CurrentSession.getUser() +
                        DashboardConstants.THEME_USER_PATH +
                        "/" + entryName, col);
            } else {
                if (!(entryName.indexOf("/") > 0)) {
                    sysRegistry.rollbackTransaction();
                    throw new RegistryException("Theme bundle is not in the correct format : {<Theme-Name>/style.css}");
                }
                //if a file creating a resource
                Resource res = sysRegistry.newResource();
                try {
                    while ((i = zis.read(buff, 0, buff.length)) > 0) {
                        byteOut.write(buff, 0, i);
                    }
                } catch (IOException ioe) {
                    sysRegistry.rollbackTransaction();
                    log.error(ioe);
                }
                if (zipentry.getName().contains("theme-conf.xml")) {
                    ByteArrayInputStream bis = new ByteArrayInputStream(byteOut.toByteArray());
                    InputStreamReader isr = new InputStreamReader(bis);

                    sysRegistry.restore(DashboardConstants.USER_DASHBOARD_REGISTRY_ROOT +
                            CurrentSession.getUser() +
                            DashboardConstants.THEME_USER_PATH +
                            "/" + entryName, isr);

                } else {
                    res.setContent(byteOut.toByteArray());
                    sysRegistry.put(DashboardConstants.USER_DASHBOARD_REGISTRY_ROOT +
                            CurrentSession.getUser() +
                            DashboardConstants.THEME_USER_PATH +
                            "/" + entryName, res);
                }
            }
            try {
                zipentry = zis.getNextEntry();
            } catch (IOException e) {
                sysRegistry.rollbackTransaction();
                log.error(e);
            }
        }
        sysRegistry.commitTransaction();
        requestContext.setProcessingComplete(true);
    }
}
