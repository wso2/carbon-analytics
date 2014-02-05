/*
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.dashboard.common;

public class DashboardConstants {

    public static String SYSTEM_GADGETREPO_REGISTRY_ROOT = "/repository/gadget-server/gadget-repo";
    public static String GS_REGISTRY_ROOT = "/repository/gadget-server";

    public static String GADGETS_COL = "/gadget-metadata";
    public static String TAB_PATH = "/tabs/";

    public static String GADGET_PREFIX = "/gadget-";

    public static String NEXT_GADGET_ID_PATH = "/nextGadgetId";
    public static String NEXT_GADGET_ID = "nextGadgetId";

    public static String GADGET_NAME = "gadgetName";
    public static String GADGET_DESC = "gadgetDesc";
    public static String GADGET_URL = "gadgetUrl";
    public static String GADGET_SCREEN = "gadgetScreen";
    public static String USER_CONTER = "userCounter";
    public static String DEFAULT_GADGET = "defaultGadget";
    public static String UNSIGNED_USER_GADGET = "unsignedUserGadget";


    public static String REGISTRY_PRODUCT_ID_PATH = "/gadget-server";

    public static String USER_DASHBOARD_REGISTRY_ROOT = "/users/";
    public static String SYSTEM_DASHBOARDS_REGISTRY_ROOT = "/repository/dashboards/";

    public static String GADGET_PREFERENCE_PATH = "/gadgetprefs";
    public static String DASHBOARD_LAYOUT_STORE_PATH = "/layoutstore";

    public static String GADGET_PATH = "/gadgets/";
   
    public static String CURRENT_GADGET_LAYOUT_PATH = "/currentGadgetLayout";
    public static String CURRENT_GADGET_LAYOUT = "currentGadgetLayout";

    public static String CURRENT_TAB_LAYOUT = "currentTabLayout";
    public static String NEXT_TAB_ID = "nextTabId";
    public static String TAB_TITLE = "tabTitle";

    /*public static String DASHBOARD_MANAGER_ROLE = "dashboard-manager";*/

    public static String REGISTRY_ADMIN_PROPERTIES_PATH = "/repository" + REGISTRY_PRODUCT_ID_PATH + "/admin-data";

    public static String USER_SELF_REG_PROPERTY_ID = "isSelfRegistrationEnabled";
    public static String USER_EXTERNAL_GADGET_ADD_PROPERTY_ID = "isExternalGadgetAdditionEnabled";
    public static String ANON_MODE_ACT = "isAnonModeEnabled";

    public static int GADGETS_PER_PAGE = 6;
    public static int COMMENTS_PER_PAGE = 10;

    public static String SQL_STATEMENTS_PATH = "/repository/gadget-server/sql";

    public static String PORTAL_USER_ROLE = "identity";

    /*Theme related constants*/
    public static final String GS_THEME_LOCATION = "/repository/gadget-server/themes";
    public static final String THEME_PATH = "/themes";
    public static final String THEME_USER_PATH = "/gadget-server/themes";
    public static String GS_DEFAULT_THEME_PATH = "/gadget-server/defaultTheme";

    /*OAuth Related constants*/
    public static final String OAUTH_KEY_STORE = "/repository/gadget-server/oauth";
    public static final String CONSUMER_SECRET_KEY = "consumer_secret";
    public static final String CONSUMER_KEY_KEY = "consumer_key";
    public static final String KEY_TYPE_KEY = "key_type";
    public static final String CALLBACK_URL = "callback_url";
    public static final String CONSUMER_SERVICE = "consumer_service";

    /*GadgetRepository related constants*/
    public static final String GADGET_THUMB = "-thumb";
    public static final String GADGET_THUMB_PATH = "/thumb/";
    public static final String GADGET_THUMB_URL = "thumbUrl";
    public static final String GADGET_MEDIA_TYPE = "application/vnd.wso2.gs.gadget";

    public static final String PRODUCT_SERVER_NAME = "WSO2 Gadget Server";
    public static final String SERVICE_SERVER_NAME = "WSO2 Stratos Gadget Server";
}
