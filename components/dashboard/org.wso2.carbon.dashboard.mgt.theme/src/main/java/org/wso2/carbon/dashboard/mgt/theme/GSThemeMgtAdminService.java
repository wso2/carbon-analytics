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
package org.wso2.carbon.dashboard.mgt.theme;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.dashboard.common.DashboardConstants;
import org.wso2.carbon.dashboard.mgt.theme.common.Theme;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.dashboard.mgt.theme.constants.ThemeConstants;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.utils.ServerConstants;

import javax.activation.DataHandler;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


public class GSThemeMgtAdminService extends AbstractAdmin {
    private static final Log log = LogFactory.getLog(GSThemeMgtAdminService.class);

    public Theme[] getThemes(String user) throws Exception {
        if (!isUserAuthorized(user, "getThemes")) {
            log.error(user + " : is not properly authenticated");
            return new Theme[0];
        }
        Registry themeRegistry = getConfigSystemRegistry();
        try {
            Collection themeCol = getDefaultThemes();
            Collection userThemeCol = getUserThemes(user);
            if ((themeCol == null && userThemeCol == null)) {
                return new Theme[0];
            }

            Theme[] gsThemes = new Theme[0];
            Theme[] userThemes = new Theme[0];

            if (themeCol != null) {
                gsThemes = new Theme[themeCol.getChildCount()];
                for (int i = 0; i < themeCol.getChildCount(); i++) {
                    Theme theme = new Theme();
                    if (themeRegistry.resourceExists(themeCol.getChildren()[i]) && themeRegistry.resourceExists(themeCol.getChildren()[i] + ThemeConstants.THEME_CONF_PATH)) {
                        Resource themeConf = themeRegistry.get(themeCol.getChildren()[i] + ThemeConstants.THEME_CONF_PATH);
                        if (themeRegistry.resourceExists(themeCol.getChildren()[i] + "/" + themeConf.getProperty(ThemeConstants.PROP_CSS))) {
                            String cssPath = themeRegistry.get(themeCol.getChildren()[i] + "/" + themeConf.getProperty(ThemeConstants.PROP_CSS)).getPath();
                            theme.setCssUrl(cssPath);
                        }
                        if (themeRegistry.resourceExists(themeCol.getChildren()[i] + "/" + themeConf.getProperty(ThemeConstants.PROP_THUMB))) {
                            String thumbPath = themeRegistry.get(themeCol.getChildren()[i] + "/" + themeConf.getProperty(ThemeConstants.PROP_THUMB)).getPath();
                            theme.setThumbUrl(thumbPath);
                        }

                        theme.setThemeName(themeConf.getProperty(ThemeConstants.PROP_NAME));
                        theme.setThemeAuthor(themeConf.getProperty(ThemeConstants.PROP_AUTHOR));
                        theme.setThemeDesc(themeConf.getProperty(ThemeConstants.PROP_DESC));

                    }
                    gsThemes[i] = theme;
                }
            }
            if (userThemeCol != null) {
                userThemes = new Theme[userThemeCol.getChildCount()];
                for (int i = 0; i < userThemeCol.getChildCount(); i++) {
                    Theme theme = new Theme();
                    if (themeRegistry.resourceExists(userThemeCol.getChildren()[i]) && themeRegistry.resourceExists(userThemeCol.getChildren()[i] + ThemeConstants.THEME_CONF_PATH)) {
                        Resource themeConf = themeRegistry.get(userThemeCol.getChildren()[i] + ThemeConstants.THEME_CONF_PATH);
                        if (themeRegistry.resourceExists(userThemeCol.getChildren()[i] + "/" + themeConf.getProperty(ThemeConstants.PROP_CSS))) {
                            String cssPath = themeRegistry.get(userThemeCol.getChildren()[i] + "/" + themeConf.getProperty(ThemeConstants.PROP_CSS)).getPath();
                            theme.setCssUrl(cssPath);
                        }
                        if (themeRegistry.resourceExists(userThemeCol.getChildren()[i] + "/" + themeConf.getProperty(ThemeConstants.PROP_THUMB))) {
                            String thumbPath = themeRegistry.get(userThemeCol.getChildren()[i] + "/" + themeConf.getProperty(ThemeConstants.PROP_THUMB)).getPath();
                            theme.setThumbUrl(thumbPath);
                        }

                        theme.setThemeName(themeConf.getProperty(ThemeConstants.PROP_NAME));
                        theme.setThemeAuthor(themeConf.getProperty(ThemeConstants.PROP_AUTHOR));
                        theme.setThemeDesc(themeConf.getProperty(ThemeConstants.PROP_DESC));

                    }
                    userThemes[i] = theme;
                }
            }

            //concatenating both theme arrays
            Theme[] allThemes = new Theme[gsThemes.length + userThemes.length];
            System.arraycopy(gsThemes, 0, allThemes, 0, gsThemes.length);
            System.arraycopy(userThemes, 0, allThemes, gsThemes.length, userThemes.length);

            return allThemes;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    private Collection getDefaultThemes() {
        Registry themeRegistry = getConfigSystemRegistry();
        Collection defThemeCol = null;
        try {
            if (!themeRegistry.resourceExists(DashboardConstants.GS_THEME_LOCATION)) {
                log.debug("The theme directory is not created");
                return null;
            }
            defThemeCol = (Collection) themeRegistry.get(DashboardConstants.GS_THEME_LOCATION);
            if (defThemeCol.getChildCount() < 1) {
                log.error("There are no themes installed in the theme directory");
                return null;
            }

        } catch (RegistryException re) {
            log.error(re);
        }
        return defThemeCol;
    }

    private Collection getUserThemes(String user) {
        Registry themeRegistry = getConfigSystemRegistry();
        Collection userThemeCol = null;
        try {
            if (!themeRegistry.resourceExists(DashboardConstants.USER_DASHBOARD_REGISTRY_ROOT + user + DashboardConstants.THEME_USER_PATH)) {
                log.debug("The theme directory is not created");
                return null;
            }
            userThemeCol = (Collection) themeRegistry.get(DashboardConstants.USER_DASHBOARD_REGISTRY_ROOT + user + DashboardConstants.THEME_USER_PATH);
            if (userThemeCol.getChildCount() < 1) {
                log.error("There are no themes installed in the theme directory");
                return null;
            }

        } catch (RegistryException re) {
            log.error(re);
        }
        return userThemeCol;
    }

    public boolean setThemeForUser(String themePath, String user) throws Exception {
        Registry userThemeRegistry = getConfigSystemRegistry();
        try {
            if (!userThemeRegistry.resourceExists(DashboardConstants.USER_DASHBOARD_REGISTRY_ROOT + user + DashboardConstants.GS_DEFAULT_THEME_PATH)) {
                Resource defaultTheme = userThemeRegistry.newResource();
                defaultTheme.setProperty("theme.location", themePath);
                userThemeRegistry.put(DashboardConstants.USER_DASHBOARD_REGISTRY_ROOT + user + DashboardConstants.GS_DEFAULT_THEME_PATH, defaultTheme);
            } else {
                Resource defaultTheme = userThemeRegistry.get(DashboardConstants.USER_DASHBOARD_REGISTRY_ROOT + user + DashboardConstants.GS_DEFAULT_THEME_PATH);
                defaultTheme.setProperty("theme.location", themePath);
                userThemeRegistry.put(DashboardConstants.USER_DASHBOARD_REGISTRY_ROOT + user + DashboardConstants.GS_DEFAULT_THEME_PATH, defaultTheme);
            }
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public String getDefaultThemeForUser(String user) throws Exception {
        Registry userThemeRegistry = getConfigSystemRegistry();
        Resource defTheme;
        String themePath = null;
        try {
            if (userThemeRegistry.resourceExists(DashboardConstants.USER_DASHBOARD_REGISTRY_ROOT + user + DashboardConstants.GS_DEFAULT_THEME_PATH)) {
                defTheme = userThemeRegistry.get(DashboardConstants.USER_DASHBOARD_REGISTRY_ROOT + user + DashboardConstants.GS_DEFAULT_THEME_PATH);
                themePath = defTheme.getProperty("theme.location");
            }
            return themePath;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    private boolean isUserAuthorized(String username, String operation) {
        MessageContext msgContext = MessageContext.getCurrentMessageContext();
        HttpServletRequest request = (HttpServletRequest) msgContext
                .getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
        HttpSession httpSession = request.getSession(false);
        if (httpSession != null) {
            String userName = (String) httpSession
                    .getAttribute(ServerConstants.USER_LOGGED_IN);
            if (!username.equals(userName)) {
                log.warn("Unauthorised action by user '" + username
                        + "' to access " + operation + " denied.");
                return false;
            } else {
                return true;
            }

        }
        log.warn("Unauthorised action by user '" + username + "' to access "
                + operation + " denied.");
        return false;
    }

    public void addResource(String path, String mediaType, String description, DataHandler content,
                            String symlinkLocation)
            throws Exception {
        UserRegistry registry = (UserRegistry) getConfigUserRegistry();
        if (RegistryUtils.isRegistryReadOnly(registry.getRegistryContext())) {
            return;
        }
        addResourceToRegistry(path, mediaType, description, content, symlinkLocation, registry);
    }

    private void addResourceToRegistry(
            String path, String mediaType, String description, DataHandler content,
            String symlinkLocation, Registry registry)
            throws Exception {

        try {

            ResourceImpl resourceImpl = (ResourceImpl) registry.newResource();
            resourceImpl.setMediaType(mediaType);
            resourceImpl.setDescription(description);
            resourceImpl.setContentStream(content.getInputStream());

            if (symlinkLocation != null) {
                if (!symlinkLocation.endsWith(RegistryConstants.PATH_SEPARATOR)) {
                    symlinkLocation += RegistryConstants.PATH_SEPARATOR;
                }
                resourceImpl.setProperty(RegistryConstants.SYMLINK_PROPERTY_NAME, symlinkLocation);
            }

            registry.put(path, resourceImpl);
            resourceImpl.discard();

        } catch (Exception e) {

            String msg = "Failed to add resource " + path + ". " + e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
    }

    public void rollbackToDefaultGSTheme(String user) throws Exception {
        Registry userThemeRegistry = getConfigSystemRegistry();
        try {
            if (userThemeRegistry.resourceExists(DashboardConstants.USER_DASHBOARD_REGISTRY_ROOT + user + DashboardConstants.GS_DEFAULT_THEME_PATH)) {
                userThemeRegistry.delete(DashboardConstants.USER_DASHBOARD_REGISTRY_ROOT + user + DashboardConstants.GS_DEFAULT_THEME_PATH);
            }
        } catch (Exception e) {
            throw e;
        }
    }
}
