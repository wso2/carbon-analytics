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
package org.wso2.carbon.dashboard.mgt.theme.ui.processors;

import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.dashboard.mgt.theme.ui.GSThemeMgtClient;
import org.wso2.carbon.server.admin.common.IServerAdmin;
import org.wso2.carbon.server.admin.common.ServerData;
import org.wso2.carbon.server.admin.ui.ServerAdminClient;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.ui.transports.fileupload.AbstractFileUploadExecutor;
import org.wso2.carbon.ui.CarbonUIMessage;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.FileItemData;
import org.wso2.carbon.utils.ServerConstants;

import javax.activation.DataHandler;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Map;

/**
 * This Servlet handles the theme Upload process
 */
public class ThemeUploadProcessors extends AbstractFileUploadExecutor {
    @Override
    public boolean execute(HttpServletRequest request, HttpServletResponse response) throws CarbonException, IOException {
        String webContext = (String) request.getAttribute(CarbonConstants.WEB_CONTEXT);
        String serverURL = (String) request.getAttribute(CarbonConstants.SERVER_URL);
        String cookie = (String) request.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        HttpSession session = request.getSession();

        Map<String, ArrayList<FileItemData>> fileItemsMap = getFileItemsMap();

        if (fileItemsMap == null || fileItemsMap.isEmpty()) {
            String msg = "File uploading failed. Content is not set properly.";
            log.error(msg);

            CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.ERROR, request);
            response.sendRedirect(
                    getContextRoot(request) + "/" + webContext + "/admin/error.jsp");

            return false;
        }
        String errorRedirect = null;
        try {
            GSThemeMgtClient client =
                    new GSThemeMgtClient(cookie, serverURL, configurationContext, request.getLocale());

            String parentPath = null;
            Map<String, ArrayList<String>> formFieldsMap = getFormFieldsMap();

            if (formFieldsMap.get("path") != null) {
                parentPath = formFieldsMap.get("path").get(0);
            }
            String resourceName = null;
            if (formFieldsMap.get("filename") != null) {
                resourceName = formFieldsMap.get("filename").get(0);
            }
            String mediaType = null;
            if (formFieldsMap.get("mediaType") != null) {
                mediaType = formFieldsMap.get("mediaType").get(0);
            }
            String description = null;
            if (formFieldsMap.get("description") != null) {
                description = formFieldsMap.get("description").get(0);
            }
            String redirect = null;
            if (formFieldsMap.get("redirect") != null) {
                redirect = formFieldsMap.get("redirect").get(0);
            }
            if (formFieldsMap.get("errorRedirect") != null) {
                errorRedirect = formFieldsMap.get("errorRedirect").get(0);
            }
            String symlinkLocation = null;
            if (formFieldsMap.get("symlinkLocation") != null) {
                symlinkLocation = formFieldsMap.get("symlinkLocation").get(0);
            }
            IServerAdmin adminClient =
                    (IServerAdmin) CarbonUIUtil.
                            getServerProxy(new ServerAdminClient(configurationContext,
                                    serverURL, cookie, session), IServerAdmin.class, session);
            ServerData data = adminClient.getServerData();
            String chroot = "";
            if (data.getRegistryType() != null && data.getRegistryType().equals("remote") &&
                    data.getRemoteRegistryChroot() != null &&
                    !data.getRemoteRegistryChroot().equals(RegistryConstants.PATH_SEPARATOR)) {
                chroot = data.getRemoteRegistryChroot();
                if (!chroot.startsWith(RegistryConstants.PATH_SEPARATOR)) {
                    chroot = RegistryConstants.PATH_SEPARATOR + chroot;
                }
                if (chroot.endsWith(RegistryConstants.PATH_SEPARATOR)) {
                    chroot = chroot.substring(0, chroot.length() - RegistryConstants.PATH_SEPARATOR.length());
                }
            }
            if (symlinkLocation != null) {
                symlinkLocation = chroot + symlinkLocation;
            }

            FileItemData fileItemData = fileItemsMap.get("upload").get(0);

            if ("".equals(resourceName) || resourceName == null) {
                resourceName = getFileName(fileItemData.getFileItem().getName());
            }

            if ((fileItemData == null) || (fileItemData.getFileItem().getSize() == 0)) {
                String msg = "Failed add resource. Resource content is empty.";
                log.error(msg);

                CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.ERROR, request);
                if (errorRedirect == null) {
                    response.sendRedirect(
                            getContextRoot(request) + "/" + webContext + "/admin/error.jsp");
                } else {
                    response.sendRedirect(
                            getContextRoot(request) + "/" + webContext + "/" + errorRedirect + (errorRedirect.indexOf("?")
                                    == -1 ? "?" : "&") + "msg=" + URLEncoder.encode(msg, "UTF-8"));
                }
                return false;
            }
            DataHandler dataHandler = fileItemData.getDataHandler();

            client.addResource(
                    calcualtePath(parentPath, resourceName), mediaType, description, dataHandler,
                    symlinkLocation);

            response.setContentType("text/html; charset=utf-8");
            String msg = "Successfully uploaded content.";
            if (redirect == null) {
                CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.INFO, request);
                try {
                    parentPath = parentPath.replace("&", "%26");
                } catch (Exception ignore) {
                }
                if ("/".equals(parentPath)) {
                    parentPath += "&viewType=std";
                }
                response.sendRedirect(getContextRoot(request) + "/" + webContext + "/resources/resource.jsp?path=" +
                        parentPath);
            } else {
                response.sendRedirect(getContextRoot(request) + "/" + webContext + "/" + redirect);
            }
            return true;

        } catch (Exception e) {
            String msg = "File upload failed. " + e.getMessage();
            log.error(msg);

            CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.ERROR, request);
            if (errorRedirect == null) {
                response.sendRedirect(
                        getContextRoot(request) + "/" + webContext + "/admin/error.jsp");
            } else {
                response.sendRedirect(
                        getContextRoot(request) + "/" + webContext + "/" + errorRedirect + (errorRedirect.indexOf("?")
                                == -1 ? "?" : "&") + "msg=" + URLEncoder.encode(msg, "UTF-8"));
            }
            return false;
        }
    }

    private static String calcualtePath(String parentPath, String resourceName) {
        String resourcePath;
        if (!parentPath.startsWith(RegistryConstants.PATH_SEPARATOR)) {
            parentPath = RegistryConstants.PATH_SEPARATOR + parentPath;
        }
        if (parentPath.endsWith(RegistryConstants.PATH_SEPARATOR)) {
            resourcePath = parentPath + resourceName;
        } else {
            resourcePath = parentPath + RegistryConstants.PATH_SEPARATOR + resourceName;
        }
        return resourcePath;
    }
}
