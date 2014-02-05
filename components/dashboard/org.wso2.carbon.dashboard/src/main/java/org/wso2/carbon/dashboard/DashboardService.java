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

package org.wso2.carbon.dashboard;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.dashboard.bean.DashboardContentBean;
import org.wso2.carbon.dashboard.common.DashboardConstants;
import org.wso2.carbon.dashboard.common.LayoutConstants;
import org.wso2.carbon.dashboard.common.bean.Gadget;
import org.wso2.carbon.dashboard.common.bean.Tab;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.utils.Transaction;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.MediaTypesUtils;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class DashboardService extends AbstractAdmin {

    private static final Log log = LogFactory.getLog(DashboardService.class);

    /**
     * Stores a given preference value for a Gadget in the Registry
     *
     * @param userId        The name of the user in concern
     * @param gadgetId      A unique gadget id
     * @param prefId        A unique Preference id
     * @param value         Preference Value
     * @param dashboardName The name of the Dashboard (this can be null)
     * @return A boolean indicating success or failure
     */
    public Boolean setGadgetPrefs(String userId, String gadgetId,
                                  String prefId, String value, String dashboardName) {
        Boolean response = false;

        try {

            Registry registry = getConfigSystemRegistry();

            String gadgetPrefPath;
            if ((dashboardName == null) || ("null".equals(dashboardName))) {
                gadgetPrefPath = DashboardConstants.USER_DASHBOARD_REGISTRY_ROOT
                        + userId
                        + DashboardConstants.REGISTRY_PRODUCT_ID_PATH
                        + DashboardConstants.GADGET_PATH
                        + gadgetId
                        + DashboardConstants.GADGET_PREFERENCE_PATH + prefId;
            } else {
                // Check whether this user is in a role to change settings
                if (!checkUserAuthorization(userId, "setGadgetPrefs")) {
                    return false;
                }

                gadgetPrefPath = DashboardConstants.SYSTEM_DASHBOARDS_REGISTRY_ROOT
                        + dashboardName
                        + DashboardConstants.GADGET_PATH
                        + gadgetId + DashboardConstants.GADGET_PREFERENCE_PATH + prefId;
            }

            Resource gadgetPreferences;

            if (registry.resourceExists(gadgetPrefPath)) {
                gadgetPreferences = registry.get(gadgetPrefPath);
            } else {
                gadgetPreferences = registry.newResource();
            }

            // Storing the gadget preference
            //gadgetPreferences.setProperty(prefId, value);
            //registry.put(gadgetPrefPath, gadgetPreferences);

            gadgetPreferences.setContent(value);
            gadgetPreferences.setMediaType("text/html");
            registry.put(gadgetPrefPath, gadgetPreferences);

            response = true;
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Failed to set preferences for user " + userId + " for prefId " + prefId, e);
            }
        }

        return response;
    }

    /**
     * Retrieves a given preference value for a Gadget from the Registry
     *
     * @param userId        Currently logged in user
     * @param gadgetId      A unique gadget id
     * @param prefId        Preference Id
     * @param dashboardName The name of the Dashboard (this can be null)
     * @return A string with preference meta-data
     */
    public String getGadgetPrefs(String userId, String gadgetId, String prefId,
                                 String dashboardName) {
        String response = "NA";

        try {
            Registry registry = getConfigSystemRegistry();

            String gadgetPrefPath;
            if ((dashboardName == null) || ("null".equals(dashboardName))) {
                gadgetPrefPath = DashboardConstants.USER_DASHBOARD_REGISTRY_ROOT
                        + userId
                        + DashboardConstants.REGISTRY_PRODUCT_ID_PATH
                        + DashboardConstants.GADGET_PATH
                        + gadgetId
                        + DashboardConstants.GADGET_PREFERENCE_PATH + prefId;
            } else {
                gadgetPrefPath = DashboardConstants.SYSTEM_DASHBOARDS_REGISTRY_ROOT
                        + dashboardName
                        + DashboardConstants.GADGET_PATH
                        + gadgetId + DashboardConstants.GADGET_PREFERENCE_PATH + prefId;
            }

            Resource gadgetPreferences = registry.get(gadgetPrefPath);

            // Retrieve preferences
            //response = gadgetPreferences.getProperty(prefId);
            response = new String((byte[]) gadgetPreferences.getContent());

            // Decoding the value
            response = new String(Hex.decodeHex(response.toCharArray()));

            /* if (response == null) {
                response = "NA";
            }*/

        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Preferences were not found for user " + userId + " for prefId " + prefId, e);
            }
        }

        return response;
    }

    /**
     * Persists the gadget layout in the dashboard page to registry
     *
     * @param userId        Currently logged in user
     * @param newLayout     The new layout meta-data to store
     * @param tabId         The id of the relevant Tab
     * @param dashboardName The name of the Dashboard (this can be null)
     * @return Boolean, indicating success or failure
     */
    public Boolean setGadgetLayout(String userId, String tabId,
                                   String newLayout, String dashboardName) {
        Boolean response = false;

        try {
            Registry registry = getConfigSystemRegistry();

            String gadgetLayoutPath;
            if ((dashboardName == null) || ("null".equals(dashboardName))) {
                gadgetLayoutPath = DashboardConstants.USER_DASHBOARD_REGISTRY_ROOT
                        + userId
                        + DashboardConstants.REGISTRY_PRODUCT_ID_PATH
                        + DashboardConstants.TAB_PATH
                        + tabId
                        + DashboardConstants.CURRENT_GADGET_LAYOUT_PATH;
            } else {
                gadgetLayoutPath = DashboardConstants.SYSTEM_DASHBOARDS_REGISTRY_ROOT
                        + dashboardName
                        + DashboardConstants.TAB_PATH
                        + tabId
                        + DashboardConstants.CURRENT_GADGET_LAYOUT_PATH;
            }

            // Check whether this user is in a role to change settings
            if (!checkUserAuthorization(userId, "setGadgetLayout")) {
                return false;
            }

            Resource gadgetLayout;

            if (registry.resourceExists(gadgetLayoutPath)) {
                gadgetLayout = registry.get(gadgetLayoutPath);
            } else {
                gadgetLayout = registry.newCollection();
            }
            // Storing the gadget layout
            gadgetLayout.setProperty(DashboardConstants.CURRENT_GADGET_LAYOUT,
                    newLayout);
            registry.put(gadgetLayoutPath, gadgetLayout);

            response = true;
        } catch (Exception e) {
            log.error("Failed to set new layout for user " + userId
                    + " for layout info [" + newLayout + "]", e);
        }

        return response;
    }

    /**
     * Retrieves the stored layout meta-data
     *
     * @param userId        Currently logged in user
     * @param tabId         The id of the relevant Tab
     * @param dashboardName The name of the Dashboard (this can be null)
     * @return Layout meta-data
     */
    public String getGadgetLayout(String userId, String tabId,
                                  String dashboardName) {
        String response;

        try {
            Registry registry = getConfigSystemRegistry();

            String gadgetLayoutPath;
            if ((dashboardName == null) || ("null".equals(dashboardName))) {
                gadgetLayoutPath = DashboardConstants.USER_DASHBOARD_REGISTRY_ROOT
                        + userId
                        + DashboardConstants.REGISTRY_PRODUCT_ID_PATH
                        + DashboardConstants.TAB_PATH
                        + tabId
                        + DashboardConstants.CURRENT_GADGET_LAYOUT_PATH;
            } else {
                gadgetLayoutPath = DashboardConstants.SYSTEM_DASHBOARDS_REGISTRY_ROOT
                        + dashboardName
                        + DashboardConstants.TAB_PATH
                        + tabId
                        + DashboardConstants.CURRENT_GADGET_LAYOUT_PATH;
            }

            Resource gadgetLayout;
            if (registry.resourceExists(gadgetLayoutPath)) {
                gadgetLayout = registry.get(gadgetLayoutPath);
            } else {
                // If the layout is not there, this might be a new tab. Create
                // new resource
                gadgetLayout = registry.newCollection();
                registry.put(gadgetLayoutPath, gadgetLayout);
            }

            // Retrieving the gadget layout
            response = gadgetLayout
                    .getProperty(DashboardConstants.CURRENT_GADGET_LAYOUT);

            if (response == null || "".equals(response)) {
                response = "NA";
            }

        } catch (Exception e) {
            log.error("Failed to get layout information for user " + userId, e);
            response = "NA";
        }

        return response;
    }

    public String getTabLayout(String userId, String dashboardName) {
        String response = "0";

        try {
            Registry registry = getConfigSystemRegistry();

            String tabLayoutPath;
            if ((dashboardName == null) || ("null".equals(dashboardName))) {
                tabLayoutPath = DashboardConstants.USER_DASHBOARD_REGISTRY_ROOT
                        + userId + DashboardConstants.REGISTRY_PRODUCT_ID_PATH
                        + DashboardConstants.TAB_PATH;
            } else {
                tabLayoutPath = DashboardConstants.SYSTEM_DASHBOARDS_REGISTRY_ROOT
                        + dashboardName + DashboardConstants.TAB_PATH;
            }

            Resource gadgetLayout;
            if (registry.resourceExists(tabLayoutPath)) {
                gadgetLayout = registry.get(tabLayoutPath);
            } else {
                // If not found we need to create the first gadget tab
                gadgetLayout = registry.newCollection();
            }

            // Retrieving the current tab layout
            response = gadgetLayout
                    .getProperty(DashboardConstants.CURRENT_TAB_LAYOUT);

            if (response == null) {
                gadgetLayout.setProperty(DashboardConstants.CURRENT_TAB_LAYOUT,
                        "0");
                registry.put(tabLayoutPath, gadgetLayout);
                response = "0";
            }

        } catch (Exception e) {
            log.error("Failed to get layout information for user " + userId, e);
        }

        return response;
    }

    /**
     * Traverses the JSON layout and retrieves the set of corresponding gadget IDs.
     *
     * @param layouts A JSON Layout array
     * @throws JSONException
     */
    private void getGadgetIdsFromJsonLayout(JSONArray layouts, ArrayList IdStore) throws JSONException {

        for (int x = 0; x < layouts.length(); x++) {
            JSONObject currentLayoutObject = layouts.getJSONObject(x);
            if (currentLayoutObject.getString("type").equalsIgnoreCase("gadget")) {
                // This is a gadget. Store the ID
                IdStore.add(currentLayoutObject.getString("id"));
            } else {
                // This is a container element. Recurse
                getGadgetIdsFromJsonLayout(currentLayoutObject.getJSONArray("layout"), IdStore);
            }
        }
    }

    private boolean isGadgetAutharized(String user, String gadgetUrl) throws UserStoreException {
        if (gadgetUrl.startsWith("/registry")) {
            gadgetUrl = gadgetUrl.split("resource")[1];
        } else {
            //GS is not hosting this gadget
            return true;
        }
        UserRegistry registry = (UserRegistry) getConfigUserRegistry();
        if (registry == null) {
            registry = (UserRegistry) getConfigSystemRegistry();
        }
        return registry.getUserRealm().getAuthorizationManager().isUserAuthorized(user, gadgetUrl, ActionConstants.GET);
    }

    /**
     * Looks at the in memory gadget layout and returns an array of
     * corresponding gadget spec URLs
     *
     * @param userId           Currently logged in user
     * @param tabId            The id of the relevant Tab
     * @param dashboardName    The name of the Dashboard (this can be null)
     * @param backendServerURL
     * @return A String Array of Gadget Spec URLs
     */
    public String[] getGadgetUrlsToLayout(String userId, String tabId,
                                          String dashboardName, String backendServerURL) throws AxisFault {
        try {

            ArrayList gadgetIdStore = new ArrayList();

            String storedLayout = getGadgetLayout(userId, tabId, dashboardName);
            if ("NA".equalsIgnoreCase(storedLayout)) {
                return null;
            } else if (storedLayout.contains("G1#")) {
                // This is a legacy layout. Grab IDs the old way.
                String[] columnIdCombos = storedLayout.split(",");
                for (int x = 0; x < columnIdCombos.length; x++) {
                    String currentId = columnIdCombos[x].split("#")[1];
                    if ((currentId != null) && (!"".equals(currentId))) {
                        gadgetIdStore.add(currentId);
                    }
                }
            } else {
                getGadgetIdsFromJsonLayout(new JSONObject(storedLayout).getJSONArray("layout"), gadgetIdStore);
            }

            Registry registry = getConfigSystemRegistry();

            ArrayList gadgetUrlsList = new ArrayList();
            String[] indGadgetIds = new String[gadgetIdStore.size()];
            gadgetIdStore.toArray(indGadgetIds);

            for (String indGadgetId : indGadgetIds) {
                // Get the corresponding Gadget Spec URL
                String gadgetPath;
                if ((dashboardName == null)
                        || ("null".equals(dashboardName))) {
                    gadgetPath = DashboardConstants.USER_DASHBOARD_REGISTRY_ROOT
                            + userId
                            + DashboardConstants.REGISTRY_PRODUCT_ID_PATH
                            + DashboardConstants.GADGET_PATH
                            + indGadgetId;
                } else {
                    gadgetPath = DashboardConstants.SYSTEM_DASHBOARDS_REGISTRY_ROOT
                            + dashboardName
                            + DashboardConstants.GADGET_PATH
                            + indGadgetId;
                }

                Resource currentGadget;
                try {
                    currentGadget = registry.get(gadgetPath);

                    String gadgetUrlTmp = currentGadget
                            .getProperty(DashboardConstants.GADGET_URL);

                    if (isGadgetAutharized(userId, gadgetUrlTmp)) {
                        gadgetUrlsList.add(gadgetUrlTmp);
                    }

                } catch (RegistryException e) {
                    // Remove this gadget from the layout
                    removeGadgetFromLayout(userId, tabId, indGadgetId, dashboardName);
                }
            }

            String[] gadgetUrls = new String[gadgetUrlsList.size()];
            gadgetUrlsList.toArray(gadgetUrls);

            if ((gadgetUrls.length > 0)) {
                // Sanitize URLs. This takes care of realtive paths for Gadgets
                // stored in the registry.
                gadgetUrls = sanitizeUrls(gadgetUrls, backendServerURL);
            }
            return gadgetUrls;
        } catch (Exception e) {
            log.error("Error occurred while parsing the gadget urls to the layout : ", e);
            throw new AxisFault(e.getMessage(), e);
        }
    }

    /**
     * Removes a given gadget Id from the existing layout
     *
     * @param userId        Currently logged in user
     * @param tabId         Current tab
     * @param dashboardName The name of the Dashboard (this can be null)
     * @param gadgetId      The id of the gadget to be removed
     */
    private void removeGadgetFromLayout(String userId, String tabId,
                                        String gadgetId, String dashboardName) throws JSONException {
        JSONObject storedLayout = new JSONObject(getGadgetLayout(userId, tabId, dashboardName));

        JSONArray layoutsArray = storedLayout.getJSONArray("layout");
        if ((layoutsArray != null) && (layoutsArray.length() > 0)) {
            JSONArray modifiedLayoutsArray = searchAndRemoveGadgetFromLayout(gadgetId, layoutsArray);
            storedLayout.put("layout", modifiedLayoutsArray);
            String newLayout = storedLayout.toString();

            // Persist this new layout
            setGadgetLayout(userId, tabId, newLayout, dashboardName);
        }
    }

    private JSONArray searchAndRemoveGadgetFromLayout(String gadgetId, JSONArray layoutsArray) throws JSONException {

        JSONArray modifiedLayoutsArray = new JSONArray();
        for (int x = 0; x < layoutsArray.length(); x++) {
            JSONObject currentLayoutElement = (JSONObject) layoutsArray.get(x);
            if ("gadget".equals(currentLayoutElement.get("type"))) {
                if (!gadgetId.equals(currentLayoutElement.get("id"))) {
                    // This is NOT the gadget we want to remove. Add it to the modified layout
                    modifiedLayoutsArray.put(currentLayoutElement);
                }
            } else {
                // This is another container element.
                currentLayoutElement.put("layout", searchAndRemoveGadgetFromLayout(gadgetId, currentLayoutElement.getJSONArray("layout")));
                modifiedLayoutsArray.put(currentLayoutElement);
            }

        }

        return modifiedLayoutsArray;
    }

    /**
     * Adds a new Gadget to the Dashboard
     *
     * @param userId        Currently logged in user
     * @param url           The gadget spec URL
     * @param tabId         The id of the relevant Tab
     * @param dashboardName The name of the Dashboard (this can be null)
     * @return A Boolean indicating success / failure
     */
    public Boolean addGadgetToUser(String userId, String tabId, String url,
                                   String dashboardName, String gadgetGroup) {

        if (!checkUserAuthorization(userId, "addGadget")) {
            return false;
        }

        Boolean response = false;
        Registry registry = null;
        try {
            if (!isGadgetAutharized(userId, url)) {
                return false;
            }
            registry = getConfigSystemRegistry();
            // Need not do any null checks for registry, as if so, we'll never
            // get here.
            registry.beginTransaction();

            // Adding the gadget to the registry and getting its ID
            String gadgetId = addGadgetToRegistry(userId, url, dashboardName, registry);

            // Adding the latest Gadget ID to the layout
            String gadgetLayoutPath;
            if ((dashboardName == null) || ("null".equals(dashboardName))) {
                gadgetLayoutPath = DashboardConstants.USER_DASHBOARD_REGISTRY_ROOT
                        + userId
                        + DashboardConstants.REGISTRY_PRODUCT_ID_PATH
                        + DashboardConstants.TAB_PATH
                        + tabId
                        + DashboardConstants.CURRENT_GADGET_LAYOUT_PATH;
            } else {
                gadgetLayoutPath = DashboardConstants.SYSTEM_DASHBOARDS_REGISTRY_ROOT
                        + dashboardName
                        + DashboardConstants.TAB_PATH
                        + tabId
                        + DashboardConstants.CURRENT_GADGET_LAYOUT_PATH;
            }

            Resource gadgetLayout;
            if (registry.resourceExists(gadgetLayoutPath)) {
                gadgetLayout = registry.get(gadgetLayoutPath);
            } else {
                gadgetLayout = registry.newCollection();
            }

            //get the column which the new gadget should be added to have a equal distribution <column, numberOfGadgets>
            HashMap<Integer, Integer> gadgets = new HashMap<Integer, Integer>();
            String layout = getGadgetLayout(userId, tabId, dashboardName);
            if (layout.equals("NA")) {
                layout = "{layout:[]}";
            }
            // to hold the sorted result
            HashMap<Integer, Integer> map = new LinkedHashMap<Integer, Integer>();
            JSONObject json = null;
            try {
                json = new JSONObject(layout);


            } catch (Exception e) {
                log.error("JSONParser unsuccessful : ", e);
            }

            for (int i = 0; i < json.getJSONArray("layout").length(); i++) {
                JSONArray numberOfGadgetsInColumn = json.getJSONArray("layout").getJSONObject(i).getJSONArray("layout");
                gadgets.put(i, numberOfGadgetsInColumn.length());
            }

            List<Integer> mapKeys = new ArrayList<Integer>(gadgets.keySet());
            List<Integer> mapValues = new ArrayList<Integer>(gadgets.values());
            TreeSet<Integer> sortedSet = new TreeSet<Integer>(mapValues);
            Object[] sortedArray = sortedSet.toArray();
            int size = sortedArray.length;

            for (int i = 0; i < size; i++) {
                map.put
                        (mapKeys.get(mapValues.indexOf(sortedArray[i])),
                                Integer.parseInt(sortedArray[i].toString()));
            }

            List<Integer> ref = new ArrayList<Integer>(map.keySet());

            // Retrieving the gadget layout
            String currentLayout = gadgetLayout
                    .getProperty(DashboardConstants.CURRENT_GADGET_LAYOUT);

            int colNum = 0;
            if (ref.size() != 0) {
                colNum = ref.get(0);
            }

            gadgetLayout.setProperty(DashboardConstants.CURRENT_GADGET_LAYOUT,
                    insertGadgetToJsonLayout(currentLayout, gadgetId, colNum).toString());

            registry.put(gadgetLayoutPath, gadgetLayout);

            // Done
            response = true;
            registry.commitTransaction();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            if (registry != null) {
                try {
                    registry.rollbackTransaction();
                } catch (Exception ex) {
                    log.error(ex.getMessage(), e);
                }
            }
        }


        return response;
    }

    private String addGadgetToRegistry(String userId, String url, String dashboardName, Registry registry) throws RegistryException {
        String nextGadgetIdPath;
        if ((dashboardName == null) || ("null".equals(dashboardName))) {
            nextGadgetIdPath = DashboardConstants.USER_DASHBOARD_REGISTRY_ROOT
                    + userId
                    + DashboardConstants.REGISTRY_PRODUCT_ID_PATH
                    + DashboardConstants.NEXT_GADGET_ID_PATH;
        } else {
            nextGadgetIdPath = DashboardConstants.SYSTEM_DASHBOARDS_REGISTRY_ROOT
                    + dashboardName
                    + DashboardConstants.NEXT_GADGET_ID_PATH;
        }

        Resource nextGadgetIdResource;
        if (registry.resourceExists(nextGadgetIdPath)) {
            nextGadgetIdResource = registry.get(nextGadgetIdPath);
        } else {
            nextGadgetIdResource = registry.newCollection();
        }


        // Generating a unique ID for this gadget
        int nextGadgetId;
        String gadgetId = "0";
        if (nextGadgetIdResource
                .getProperty(DashboardConstants.NEXT_GADGET_ID) != null) {
            // We need to use that
            gadgetId = nextGadgetIdResource
                    .getProperty(DashboardConstants.NEXT_GADGET_ID);
        }
        // Increment and update counter
        nextGadgetId = Integer.parseInt(gadgetId) + 1;
        nextGadgetIdResource.setProperty(DashboardConstants.NEXT_GADGET_ID,
                String.valueOf(nextGadgetId));
        registry.put(nextGadgetIdPath, nextGadgetIdResource);

        // Adding the new gadget to registry
        String newGadgetPath;
        if ((dashboardName == null) || ("null".equals(dashboardName))) {
            newGadgetPath = DashboardConstants.USER_DASHBOARD_REGISTRY_ROOT
                    + userId + DashboardConstants.REGISTRY_PRODUCT_ID_PATH
                    + DashboardConstants.GADGET_PATH + gadgetId;
        } else {
            newGadgetPath = DashboardConstants.SYSTEM_DASHBOARDS_REGISTRY_ROOT
                    + dashboardName
                    + DashboardConstants.GADGET_PATH
                    + gadgetId;
        }

        Resource newGadgetResource;
        if (registry.resourceExists(newGadgetPath)) {
            newGadgetResource = registry.get(newGadgetPath);
        } else {
            newGadgetResource = registry.newCollection();
        }
        // Storing the gadget url as a property and adding the new gadget
        newGadgetResource.setProperty(DashboardConstants.GADGET_URL, url);
        registry.put(newGadgetPath, newGadgetResource);
        return gadgetId;
    }

    private JSONObject insertGadgetToJsonLayout(String layout, String gadgetId, int columnNumber) throws JSONException {

        // We will add this gadget to the first row or column container in the layout. The user
        // will decide where the gadget will go eventually via the UI.

        if ((layout == null) || ("".equals(layout))) {
            // This is the first gadget for this tab and a layout is not present for some reason.
            // Creating a 3 column default layout, with this gadget included.
            String twoColumnTemplate = "{\n" +
                    "    \"layout\":\n" +
                    "            [\n" +
                    "                {\n" +
                    "                    \"type\": \"columnContainer\",\n" +
                    "                    \"width\": \"33%\",\n" +
                    "                    \"layout\":\n" +
                    "                            [\n" +
                    "                                {\n" +
                    "                                    \"type\": \"gadget\",\n" +
                    "                                    \"id\": \"" + gadgetId + "\"\n" +
                    "                                }\n" +
                    "                            ]\n" +
                    "                },\n" +
                    "                {\n" +
                    "                    \"type\": \"columnContainer\",\n" +
                    "                    \"width\": \"67%\",\n" +
                    "                    \"layout\":\n" +
                    "                            [\n" +
                    "\n" +
                    "                            ]\n" +
                    "                }\n" +
                    "            ]\n" +
                    "}";

            return new JSONObject(twoColumnTemplate);
        } else {
            // We already have a layout
            JSONObject currentLayout = new JSONObject(layout);

            // Create gadget object
            JSONObject newGadget = new JSONObject();
            newGadget.put("type", "gadget");
            newGadget.put("id", gadgetId);

            // Get the first container and add the gadget to it.
            JSONArray firstContainersLayoutArray = currentLayout.getJSONArray("layout").getJSONObject(columnNumber).getJSONArray("layout");
            firstContainersLayoutArray.put(firstContainersLayoutArray.length(), newGadget);

            return currentLayout;
        }

    }

    private String getGadgetUrl(String userId, String gadgetId,
                                String dashboardName) {
        String gadgetUrl = null;
        String gadgetPath;
        if ((dashboardName == null) || ("null".equals(dashboardName))) {
            gadgetPath = DashboardConstants.USER_DASHBOARD_REGISTRY_ROOT
                    + userId + DashboardConstants.REGISTRY_PRODUCT_ID_PATH
                    + DashboardConstants.GADGET_PATH + gadgetId;
        } else {
            gadgetPath = DashboardConstants.SYSTEM_DASHBOARDS_REGISTRY_ROOT
                    + dashboardName + DashboardConstants.GADGET_PATH + gadgetId;
        }
        try {
            Registry registry = getConfigSystemRegistry();
            Resource gadgetRes = registry.get(gadgetPath);
            gadgetUrl = gadgetRes.getProperty(DashboardConstants.GADGET_URL);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return gadgetUrl;
    }

    /**
     * Removes a gadget from the Dashboard
     *
     * @param userId        Currently logged in user
     * @param gadgetId      The id of the gadget to be removed
     * @param tabId         The id of the relevant Tab
     * @param dashboardName The name of the Dashboard (this can be null)
     * @return Boolean indicating success/failure
     */
    public Boolean removeGadget(String userId, String tabId, String gadgetId,
                                String dashboardName) {

        if (!checkUserAuthorization(userId, "removeGadget")) {
            return false;
        }

        Boolean response = false;
        Registry registry = null;
        try {
            registry = getConfigSystemRegistry();
            registry.beginTransaction();

            // First remove this gadget from the Layout and persist the new layout.
            removeGadgetFromLayout(userId, tabId, gadgetId, dashboardName);

            // Remove the Gadget from registry
            String gadgetPath;
            if ((dashboardName == null) || ("null".equals(dashboardName))) {
                gadgetPath = DashboardConstants.USER_DASHBOARD_REGISTRY_ROOT
                        + userId + DashboardConstants.REGISTRY_PRODUCT_ID_PATH
                        + DashboardConstants.GADGET_PATH + gadgetId;
            } else {
                gadgetPath = DashboardConstants.SYSTEM_DASHBOARDS_REGISTRY_ROOT
                        + dashboardName + DashboardConstants.GADGET_PATH
                        + gadgetId;
            }
            registry.delete(gadgetPath);

            // Done
            response = true;
            registry.commitTransaction();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            if (registry != null) {
                try {
                    registry.rollbackTransaction();
                } catch (Exception ex) {
                    log.error(ex.getMessage(), e);
                }
            }
        }

        return response;
    }

    /**
     * Creates a new tab and persists its meta-data
     *
     * @param userId        Currently logged in user
     * @param tabTitle      A title forthe new tab
     * @param dashboardName The name of the Dashboard (this can be null)
     * @return integer
     */
    public Integer addNewTab(String userId, String tabTitle,
                             String dashboardName) {
        if (!checkUserAuthorization(userId, "addNewTab") || tabNameExsists(tabTitle, userId)) {
            return 0;
        }

        Integer response = 0;

        String dashboardTabPath;
        if ((dashboardName == null) || ("null".equals(dashboardName))) {
            dashboardTabPath = DashboardConstants.USER_DASHBOARD_REGISTRY_ROOT
                    + userId + DashboardConstants.REGISTRY_PRODUCT_ID_PATH
                    + DashboardConstants.TAB_PATH;
        } else {
            dashboardTabPath = DashboardConstants.SYSTEM_DASHBOARDS_REGISTRY_ROOT
                    + dashboardName + DashboardConstants.TAB_PATH;
        }

        Registry registry = null;
        try {
            registry = getConfigSystemRegistry();
            // Need not do any null checks for registry, as if so, we'll never
            // get here.
            registry.beginTransaction();
            Resource userTabResource = registry.get(dashboardTabPath);

            // First generate the new Id
            String nextTabId = userTabResource
                    .getProperty(DashboardConstants.NEXT_TAB_ID);
            if (nextTabId == null) {
                // This is the first tab after home [0]
                nextTabId = "1";
            }
            // Persisting the updated counter
            userTabResource.setProperty(DashboardConstants.NEXT_TAB_ID, String
                    .valueOf(Integer.parseInt(nextTabId) + 1));
            registry.put(dashboardTabPath, userTabResource);

            // Creating a new tab resource
            Resource newTab = registry.newCollection();

            // Storing the name of the tab
            if ("".equals(tabTitle)) {
                tabTitle = "Tab " + nextTabId;
            }
            newTab.setProperty(DashboardConstants.TAB_TITLE, tabTitle);

            String newTabPath = dashboardTabPath + nextTabId;
            registry.put(newTabPath, newTab);

            // Retrieving the current tab layout
            String currentTabLayout = userTabResource
                    .getProperty(DashboardConstants.CURRENT_TAB_LAYOUT);
            // Adding the new Tab
            currentTabLayout = currentTabLayout + ","
                    + String.valueOf(nextTabId);
            userTabResource.setProperty(DashboardConstants.CURRENT_TAB_LAYOUT,
                    currentTabLayout);
            registry.put(dashboardTabPath, userTabResource);

            // Done
            response = Integer.parseInt(nextTabId);
            registry.commitTransaction();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            if (registry != null) {
                try {
                    registry.rollbackTransaction();
                } catch (Exception ex) {
                    log.error(ex.getMessage(), e);
                }
            }
        }

        return response;
    }

    /**
     * Returns the title of a given tab
     *
     * @param userId        Currently logged in user
     * @param tabId         The tab id
     * @param dashboardName The name of the Dashboard (this can be null)
     * @return The title of the tab or a default value if not found
     */
    public String getTabTitle(String userId, String tabId, String dashboardName) {
        String response = "Tab " + tabId;

//        if (((dashboardName == null) && ("0".equals(tabId))) || ("null".equals(dashboardName)) && ("0".equals(tabId))) {
//            return "Home";
//        }

        String tabPath;
        if ((dashboardName == null) || ("null".equals(dashboardName))) {
            tabPath = DashboardConstants.USER_DASHBOARD_REGISTRY_ROOT + userId
                    + DashboardConstants.REGISTRY_PRODUCT_ID_PATH
                    + DashboardConstants.TAB_PATH + tabId;
        } else {
            tabPath = DashboardConstants.SYSTEM_DASHBOARDS_REGISTRY_ROOT
                    + dashboardName + DashboardConstants.TAB_PATH + tabId;
        }

        try {
            Registry registry = getConfigSystemRegistry();
            if ("0".equals(tabId) && (!registry.resourceExists(tabPath) || registry.get(tabPath).getProperty(DashboardConstants.TAB_TITLE) == null)) {
                return "Home";
            }
            Resource tabResource = registry.get(tabPath);
            if (tabResource.getProperty(DashboardConstants.TAB_TITLE) != null) {
                return tabResource.getProperty(DashboardConstants.TAB_TITLE);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return response;
    }

    /**
     * Removes a given tab from the system
     *
     * @param userId        Currently logged in user
     * @param tabId         The id of the tab to be removed
     * @param dashboardName The name of the Dashboard (this can be null)
     * @return Boolean indicating success/failure
     */
    public Boolean removeTab(String userId, String tabId, String dashboardName) {

        if (!checkUserAuthorization(userId, "removeTab")) {
            return false;
        }

        if ("0".equals(tabId)) {
            // We don't allow the Home tab to be removed
            return false;
        }

        Boolean response = false;

        String dashboardTabPath;
        if ((dashboardName == null) || ("null".equals(dashboardName))) {
            dashboardTabPath = DashboardConstants.USER_DASHBOARD_REGISTRY_ROOT
                    + userId + DashboardConstants.REGISTRY_PRODUCT_ID_PATH
                    + DashboardConstants.TAB_PATH;
        } else {
            dashboardTabPath = DashboardConstants.SYSTEM_DASHBOARDS_REGISTRY_ROOT
                    + dashboardName + DashboardConstants.TAB_PATH;
        }

        Registry registry = null;
        boolean transactionStarted = false;
        try {
            registry = getConfigSystemRegistry();
            // Need not do any null checks for registry, as if so, we'll never
            // get here.

            if (!Transaction.isStarted()) {
                registry.beginTransaction();
                transactionStarted = true;
            }
            Resource userTabResource = registry.get(dashboardTabPath);

            // First remove this tab from the tab layout
            String currentTabLayout = userTabResource
                    .getProperty(DashboardConstants.CURRENT_TAB_LAYOUT);
            String[] tabIds = currentTabLayout.split(",");
            String newTabLayout = "";
            for (String tabId1 : tabIds) {
                if (!tabId1.equals(tabId)) {
                    if (newTabLayout.equals("")) {
                        newTabLayout = tabId1;
                    } else {
                        newTabLayout = newTabLayout + "," + tabId1;
                    }
                }
            }
            userTabResource.setProperty(DashboardConstants.CURRENT_TAB_LAYOUT,
                    newTabLayout);
            registry.put(dashboardTabPath, userTabResource);

            // Then remove all the Gadgets belonging to this tab
            JSONObject currentLayout = new JSONObject(getGadgetLayout(userId, tabId, dashboardName));
            JSONArray layoutElements = currentLayout.getJSONArray("layout");

            ArrayList gadgetIdStore = new ArrayList();
            getGadgetIdsFromJsonLayout(layoutElements, gadgetIdStore);

            if (gadgetIdStore.size() > 0) {
                // We have gadgets need to remove them
                String[] gadgetList = new String[gadgetIdStore.size()];
                gadgetIdStore.toArray(gadgetList);

                for (int x = 0; x < gadgetList.length; x++) {
                    removeGadget(userId, tabId, gadgetList[x], dashboardName);
                }
            }

            // Finally remove the tab
            String tabPath = dashboardTabPath + tabId;
            registry.delete(tabPath);

            // Done
            response = true;
            if (transactionStarted) {
                registry.commitTransaction();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            if (registry != null) {
                try {
                    if (transactionStarted) {
                        registry.rollbackTransaction();
                    }
                } catch (Exception ex) {
                    log.error(ex.getMessage(), ex);
                }
            }
        }

        return response;
    }

    /**
     * Checks whether the Dashboard should be rendered in read-only mode or not
     * for a given user
     *
     * @param userId Currently logged in user
     * @return Boolean
     */
    public Boolean isReadOnlyMode(String userId) {
        if (userId == null || "null".equals(userId)) {
            return true;
        }
        Boolean resp = true;
        try {
            String[] userRoles = getUserRealm()
                    .getUserStoreManager().getRoleListOfUser(userId);
            for (int x = 0; x < userRoles.length; x++) {
                if ("admin".equals(userRoles[x])) {
                    return false;
                }
            }
        } catch (Exception e) {
            log.error(e);
        }
        return resp;
    }

    public String getBackendHttpPort() {
        String httpPort = null;
        try {
//            httpPort = ServerConfiguration.getInstance().getFirstProperty("RegistryHttpPort");
/*            String apacheHttpPort = ServerConfiguration.getInstance().getFirstProperty(
                    "ApacheHttpPort");
            if (apacheHttpPort != null && !"".equals(apacheHttpPort)) {
                httpPort = apacheHttpPort;
            }*/
            int port = CarbonUtils.getTransportProxyPort(getConfigContext(), "http");
            if (port == -1) {
                port = CarbonUtils.getTransportPort(getConfigContext(), "http");
            }
            httpPort = Integer.toString(port);

/*            if (httpPort == null) {
                httpPort = (String) DashboardContext.getConfigContext()
                        .getAxisConfiguration().getTransportIn("http")
                        .getParameter("port").getValue();
            }*/

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return httpPort;
    }

    public Boolean isSelfRegistrationEnabled() {
        Boolean response = false;
        try {
            Registry registry = getConfigSystemRegistry();

            Resource regAdminDataResource;
            if (registry
                    .resourceExists(DashboardConstants.REGISTRY_ADMIN_PROPERTIES_PATH)) {
                regAdminDataResource = registry
                        .get(DashboardConstants.REGISTRY_ADMIN_PROPERTIES_PATH);

                String storedValue = regAdminDataResource
                        .getProperty(DashboardConstants.USER_SELF_REG_PROPERTY_ID);

                if ((storedValue != null) && ("true".equals(storedValue))) {
                    return true;
                }

            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return response;
    }

    public Boolean isExternalGadgetAdditionEnabled() {
        Boolean response = false;

        try {
            Registry registry = getConfigSystemRegistry();

            Resource regAdminDataResource;
            if (registry
                    .resourceExists(DashboardConstants.REGISTRY_ADMIN_PROPERTIES_PATH)) {
                regAdminDataResource = registry
                        .get(DashboardConstants.REGISTRY_ADMIN_PROPERTIES_PATH);

                String storedValue = regAdminDataResource
                        .getProperty(DashboardConstants.USER_EXTERNAL_GADGET_ADD_PROPERTY_ID);

                if ((storedValue != null) && ("true".equals(storedValue))) {
                    return true;
                }

            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return response;
    }

    /**
     * A custom query to retrieve the data for default view
     *
     * @return array of strings containing the gadget URLs
     */
    public String[] getDefaultGadgetUrlSet(String userId) throws AxisFault {
        Registry registry;
        try {
            if (isOldUser(userId)) {
                return new String[0];
            }
            registry = getConfigSystemRegistry();

            String sql = "SELECT R.REG_NAME, R.REG_PATH_ID FROM REG_RESOURCE R, REG_PROPERTY P, REG_RESOURCE_PROPERTY RP, REG_PATH PA WHERE "
                    + "R.REG_VERSION=RP.REG_VERSION AND "
                    + "P.REG_NAME='"
                    + DashboardConstants.DEFAULT_GADGET
                    + "' AND "
                    + "P.REG_VALUE='true' AND "
                    + "P.REG_ID=RP.REG_PROPERTY_ID AND "
                    + "PA.REG_PATH_ID=R.REG_PATH_ID";

            HashMap<String, String> map = new HashMap<String, String>();
            map.put("query", sql);
            Collection qResults = registry.executeQuery(
                    DashboardConstants.SQL_STATEMENTS_PATH + "/query3", map);

            String[] qPaths = (String[]) qResults.getContent();
            ArrayList gadgetUrlsList = new ArrayList();

            for (String qPath : qPaths) {
                if (registry.resourceExists(qPath)) {
                    Resource tempRes = registry.get(qPath);
                    String gadgetUrlTmp = tempRes.getProperty(
                            DashboardConstants.GADGET_URL);
                    if (isGadgetAutharized(userId, gadgetUrlTmp)) {
                        gadgetUrlsList.add(gadgetUrlTmp);
                        try {
                            Integer userCount = Integer.parseInt(tempRes.getProperty(
                                    DashboardConstants.USER_CONTER));
                            userCount = userCount + 1;
                            tempRes.setProperty(DashboardConstants.USER_CONTER, userCount.toString());
                            registry.put(qPath, tempRes);
                        } catch (Exception e) {
                            log.error("could not increment the user count :" + e.getMessage());
                        }
                    }
                }
            }

            String[] gadgetUrls = new String[gadgetUrlsList.size()];
            gadgetUrlsList.toArray(gadgetUrls);
            return gadgetUrls;

        } catch (Exception e) {
            log.error("Backend server error - could not get the default gadget url set", e);
            throw new AxisFault(e.getMessage(), e);
        }

    }

    /**
     * The method checks for whether the user is authorized to execute the operation
     *
     * @param username
     * @param operation
     * @return true | false
     */
    private boolean checkUserAuthorization(String username, String operation) {
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

    private String getHttpServerRoot(String backendServerURL) {
        String response = "";
        try {
            String hostName = CarbonUtils.getServerConfiguration().getFirstProperty("HostName");
            // Removing the carbon part
            response = backendServerURL.split("/carbon/")[0];

            URL newUrl = new URL(response);

            if ("".equals(newUrl.getPath())) {
                response = "http://" + newUrl.getHost() + ":" + getBackendHttpPort();
            } else {
                response = "http://" + newUrl.getHost() + ":" + getBackendHttpPort() + newUrl.getPath();
            }

            if (hostName != null && !hostName.equals("")) {
                if (!"".equals(newUrl.getPath())) {
                    response = "http://" + hostName + ":" + getBackendHttpPort() + newUrl.getPath();
                } else {
                    response = "http://" + hostName + ":" + getBackendHttpPort();
                }
            }

        } catch (MalformedURLException e) {
            log.error(e.getMessage(), e);
        }
        return response;
    }

    private String[] sanitizeUrls(String[] gadgetUrls, String backendServerURL) {
        String[] response = new String[0];
        String tDomain = getTenantDomain();
        String tPathWithDOmain = "";
        if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tDomain)) {
            tPathWithDOmain = "/t/" + tDomain;
        }

        if (gadgetUrls != null) {
            ArrayList tempUrlHolder = new ArrayList();

            for (String currentUrl : gadgetUrls) {
                // Removing trailing and leading special characters
                currentUrl = currentUrl.replaceAll("[\r\n]", "").trim();

                // Check whether this is a relative URL. If so, attach the local
                // server http root to it
                if ("/".equals(String.valueOf(currentUrl.charAt(0)))) {
                    currentUrl = getHttpServerRoot(backendServerURL) + tPathWithDOmain + currentUrl;
                }

                tempUrlHolder.add(currentUrl);
            }

            response = new String[tempUrlHolder.size()];
            tempUrlHolder.toArray(response);
        }

        return response;
    }

    /**
     * This operation duplicates the content of a given Tab to a new Tab appends
     * it into the users Tab Layout.
     *
     * @param userId        The logged in user
     * @param dashboardName The name of the System Dashboard, if applicable.
     * @param sourceTabId   The Tab to be dupliated
     * @param newTabName    The name to be used for the new Tab
     * @return Integer      ID of the newly created Tab
     */
    public Integer duplicateTab(String userId, String dashboardName,
                                String sourceTabId, String newTabName) {
        if (!checkUserAuthorization(userId, "duplicateTab")) {
            return 0;
        }

        int resp = 0;

        // Create the new tab
        String newTabId = String.valueOf(addNewTab(userId, newTabName, dashboardName));

        // Get the Gadget layout of the source Tab
        try {

            Registry registry = getConfigSystemRegistry();

            JSONObject sourceGadgetLayout = new JSONObject(getGadgetLayout(userId, sourceTabId,
                    dashboardName));
            JSONArray layoutElements = sourceGadgetLayout.getJSONArray("layout");

            // Traverse this source format, duplicating gadgets and replacing source gadget IDs
            // with the new ones.
            JSONArray gadgetDuplicatedLayoutElements = duplicateGadgetsInLayout(userId, dashboardName, registry, layoutElements);
            sourceGadgetLayout.put("layout", gadgetDuplicatedLayoutElements);

            // Store this layout
            setGadgetLayout(userId, newTabId, sourceGadgetLayout.toString(), dashboardName);

            return Integer.parseInt(newTabId);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            resp = 0;
        }


        return resp;
    }

    private JSONArray duplicateGadgetsInLayout(String userId, String dashboardName, Registry registry, JSONArray layoutElements) throws JSONException, RegistryException {
        for (int x = 0; x < layoutElements.length(); x++) {
            JSONObject currentLayoutElement = (JSONObject) layoutElements.get(x);
            if ("gadget".equals(currentLayoutElement.get("type"))) {
                // Duplicate this gadget and replace with the new gadget's ID
                String sourceGadgetsId = currentLayoutElement.get("id").toString();
                if ((sourceGadgetsId != null) && (!"".equals(sourceGadgetsId))) {
                    String duplicatesId = addGadgetToRegistry(userId, getGadgetUrl(userId, sourceGadgetsId, dashboardName), dashboardName, registry);
                    currentLayoutElement.put("id", duplicatesId);
                }
            } else {
                // Container element. Recurse.
                duplicateGadgetsInLayout(userId, dashboardName, registry, currentLayoutElement.getJSONArray("layout"));
            }
        }

        return layoutElements;
    }

    /**
     * The method copies a given gadegt
     *
     * @param userId         The loged in user
     * @param dashboardName  dashboard type
     * @param sourceGadgetId source gadget
     * @param tab            copying tab
     * @param grp
     * @return
     */
    public Boolean copyGadget(String userId, String dashboardName,
                              String sourceGadgetId, String tab, String grp) {
        Boolean resp;
        if (!checkUserAuthorization(userId, "copyGadget")) {
            return false;
        }

        String gadgetUrl = getGadgetUrl(userId, sourceGadgetId, dashboardName);

        resp = addGadgetToUser(userId, tab, gadgetUrl, dashboardName, grp);

        return resp;
    }

    /**
     * Moves a gadget to a new tab.
     *
     * @param userId
     * @param dashboardName
     * @param sourceGadgetId
     * @param tab
     * @return
     */
    public Boolean moveGadgetToTab(String userId, String dashboardName,
                                   String sourceGadgetId, String tab) {
        Boolean resp;
        if (!checkUserAuthorization(userId, "moveGadgetToTab")) {
            return false;
        }

        String gadgetUrl = getGadgetUrl(userId, sourceGadgetId, dashboardName);

        resp = addGadgetToUser(userId, tab, gadgetUrl, dashboardName, "G1#");

        return resp;
    }

    /**
     * checking for duplicate tab names
     *
     * @param tabName
     * @param userId
     * @return
     */
    private Boolean tabNameExsists(String tabName, String userId) {
        Registry registry;
        try {
            registry = getConfigSystemRegistry();

            Resource comQuery = registry.newResource();

            String sql = "SELECT R.REG_NAME, R.REG_PATH_ID FROM REG_RESOURCE R, REG_PROPERTY P, REG_RESOURCE_PROPERTY RP, " +
                    "REG_PATH PA WHERE R.REG_VERSION=RP.REG_VERSION AND P.REG_NAME='tabTitle' " +
                    "AND P.REG_VALUE LIKE ? AND P.REG_ID=RP.REG_PROPERTY_ID AND PA.REG_PATH_ID=R.REG_PATH_ID " +
                    "AND PA.REG_PATH_VALUE LIKE ?";

            Map<String, String> params = new HashMap<String, String>();

            params.put("1", tabName);
            String tabPath = DashboardConstants.USER_DASHBOARD_REGISTRY_ROOT + userId + "%";
            params.put("2", tabPath);
            params.put("query", sql);

            Collection qResults = registry.executeQuery(
                    DashboardConstants.SQL_STATEMENTS_PATH + "/query4", params);

            String[] qPaths = (String[]) qResults.getContent();

            return (qPaths.length != 0);
        } catch (Exception e) {
            log.error("Backend server error - could validate the url for duplicates", e);
            return false;
        }

    }

    /**
     * Checks if the user has logged in before
     *
     * @param userId
     * @return
     */
    private Boolean isOldUser(String userId) {
        Boolean newUser;
        String nextGadgetIdPath;
        Registry registry;
        nextGadgetIdPath = DashboardConstants.USER_DASHBOARD_REGISTRY_ROOT
                + userId
                + DashboardConstants.REGISTRY_PRODUCT_ID_PATH
                + DashboardConstants.NEXT_GADGET_ID_PATH;
        try {
            registry = getConfigSystemRegistry();
            newUser = registry.resourceExists(nextGadgetIdPath);
            return newUser;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }

    }

    /**
     * Checking for session expiration
     *
     * @return true | false
     */
    public Boolean isSessionValid() {
        MessageContext msgContext = MessageContext.getCurrentMessageContext();
        HttpServletRequest request = (HttpServletRequest) msgContext
                .getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
        HttpSession httpSession = request.getSession(false);
        return (!httpSession.isNew());
    }

    public String getPortalStylesUrl(String user) {
        Registry userRegistry = getConfigSystemRegistry();
        String retPath = null;
        try {
            if (userRegistry.resourceExists(DashboardConstants.USER_DASHBOARD_REGISTRY_ROOT + user + DashboardConstants.GS_DEFAULT_THEME_PATH)) {
                Resource res = userRegistry.get(DashboardConstants.USER_DASHBOARD_REGISTRY_ROOT + user + DashboardConstants.GS_DEFAULT_THEME_PATH);
                retPath = res.getProperty("theme.location");
                return retPath;
            }
            return retPath;
        } catch (Exception e) {
            log.error("Exception when retriving Portal style URL", e);
            return null;
        }
    }

    /**
     * Set the tab layout according  to user's selection.
     *
     * @param userId
     * @param tabId
     * @param layout
     * @return true/false
     */
    public Boolean populateCustomLayouts(String userId, String tabId, String layout, String dashboard) {

        Boolean result = false;
        String response = "NA";
        if ((userId == null) | ("NULL".equalsIgnoreCase(userId))) {
            // Unsigned user. Do nothing
            response = "NA";
        } else {
            try {
                String templateType = null;
                if (layout.equalsIgnoreCase("1")) {
                    templateType = LayoutConstants.ONE_COLUMN_LAYOUT;
                } else if (layout.equalsIgnoreCase("2")) {
                    templateType = LayoutConstants.TWO_COLUMN_SYMETRIC_LAYOUT;
                } else if (layout.equalsIgnoreCase("3")) {
                    templateType = LayoutConstants.DEFAULT_THREE_COLUMN_LAYOUT;
                } else if (layout.equalsIgnoreCase("4")) {
                    templateType = LayoutConstants.CUSTOM_THREE_COLUMN_LAYOUT;
                } else if (layout.equalsIgnoreCase("5")) {
                    templateType = LayoutConstants.TWO_COLUMN_ASYMETRIC1_LAYOUT;
                } else if (layout.equalsIgnoreCase("6")) {
                    templateType = LayoutConstants.TWO_COLUMN_ASYMETRIC2_LAYOUT;
                }


                response = new JSONObject(templateType).toString();
                try {
                    JSONObject currentLayout = new JSONObject(getGadgetLayout(userId, tabId, dashboard));
                    JSONArray layoutElements = currentLayout.getJSONArray("layout");

                    ArrayList<String> gadgetIdStore = new ArrayList<String>();
                    getGadgetIdsFromJsonLayout(layoutElements, gadgetIdStore);

                    for (String gadgetId : gadgetIdStore) {
                        response = insertGadgetToJsonLayout(response, gadgetId, 0).toString();
                    }
                } catch (JSONException e) {
                    //ignoring the exception
                    log.debug("ignoring this exception : ", e);
                }

                //getGadgetIdsFromJsonLayout();
                // Store this layout in the Registry
                setGadgetLayout(userId, tabId, response, dashboard);


            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

        }

        return result;
    }


    public String populateDefaultThreeColumnLayout(String userId, String tabId) throws AxisFault {

        String response = "NA";

        if ((userId == null) | ("NULL".equalsIgnoreCase(userId))) {
            // Unsigned user. Do nothing
            response = "NA";
        } else {
            String[] gadgetURLs = getDefaultGadgetUrlSet(userId);


            Registry registry = null;
            try {

                JSONObject masterLayout = new JSONObject();

                JSONObject column1Layout = new JSONObject();
                column1Layout.put("type", "columnContainer");
                column1Layout.put("width", "33%");

                JSONObject column2Layout = new JSONObject();
                column2Layout.put("type", "columnContainer");
                column2Layout.put("width", "33%");

                JSONObject column3Layout = new JSONObject();
                column3Layout.put("type", "columnContainer");
                column3Layout.put("width", "33%");

                registry = getConfigSystemRegistry();

                JSONArray column1Gadgets = new JSONArray();
                JSONArray column2Gadgets = new JSONArray();
                JSONArray column3Gadgets = new JSONArray();

                for (int x = 0; x < gadgetURLs.length; x = x + 3) {
                    String gadgetId = addGadgetToRegistry(userId, gadgetURLs[x], null, registry);

                    JSONObject gadget = new JSONObject();
                    gadget.put("type", "gadget");
                    gadget.put("id", gadgetId);

                    column1Gadgets.put(column1Gadgets.length(), gadget);
                }

                for (int x = 1; x < gadgetURLs.length; x = x + 3) {
                    String gadgetId = addGadgetToRegistry(userId, gadgetURLs[x], null, registry);

                    JSONObject gadget = new JSONObject();
                    gadget.put("type", "gadget");
                    gadget.put("id", gadgetId);

                    column2Gadgets.put(column2Gadgets.length(), gadget);
                }

                for (int x = 2; x < gadgetURLs.length; x = x + 3) {
                    String gadgetId = addGadgetToRegistry(userId, gadgetURLs[x], null, registry);

                    JSONObject gadget = new JSONObject();
                    gadget.put("type", "gadget");
                    gadget.put("id", gadgetId);

                    column3Gadgets.put(column3Gadgets.length(), gadget);
                }

                column1Layout.put("layout", column1Gadgets);
                column2Layout.put("layout", column2Gadgets);
                column3Layout.put("layout", column3Gadgets);

                JSONArray columnCollection = new JSONArray();
                columnCollection.put(columnCollection.length(), column1Layout);
                columnCollection.put(columnCollection.length(), column2Layout);
                columnCollection.put(columnCollection.length(), column3Layout);

                masterLayout.put("layout", columnCollection);

                response = masterLayout.toString();

                // Store this layout in the Registry
                setGadgetLayout(userId, tabId, response, null);

            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw new AxisFault(e.getMessage(), e);
            }

        }

        return response;
    }

    /**
     * The method is used to obtain the complete dashboard as a bean
     *
     * @param userId
     * @param dashboardName
     * @param tDomain
     * @param backendServerURL
     * @return
     * @throws AxisFault
     */
    public DashboardContentBean getDashboardContent(String userId, String dashboardName, String tDomain, String backendServerURL) throws AxisFault {
        DashboardContentBean dashboardContentBean = new DashboardContentBean();

        dashboardContentBean.setBackendHttpPort(getBackendHttpPort());
        dashboardContentBean.setDefaultGadgetUrlSet(getDefaultGadgetUrlSet(userId));
        dashboardContentBean.setPortalCss(getPortalStylesUrl(userId));
        dashboardContentBean.setReadOnlyMode(isReadOnlyMode(userId));
        dashboardContentBean.setTabLayout(getTabLayout(userId, dashboardName));

        String[] userTabLayout = getTabLayout(userId, dashboardName).split(",");

        ArrayList<Tab> userTabs = new ArrayList<Tab>();
        try {
            for (String userTab : userTabLayout) {
                Tab tab = new Tab();
                tab.setTabName(getTabTitle(userId, userTab, dashboardName));
                tab.setTabId(userTab);
                tab.setGadgetLayout(getGadgetLayout(userId, userTab, dashboardName));
                tab.setGadgets(getGadgetsForTab(userId, dashboardName, tab.getGadgetLayout()));
                //tab.setGadgetUrls(getGadgetUrlsToLayout(userId, userTab, dashboardName, backendServerURL));
                userTabs.add(tab);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage(), e);
        }

        Tab[] tabs = new Tab[userTabs.size()];
        dashboardContentBean.setTabs(userTabs.toArray(tabs));
        return dashboardContentBean;
    }

    /**
     * Method is used to obtain the complete dashboard as a JSOn data structure
     *
     * @param userId
     * @param dashboardName
     * @param tDomain
     * @param backendServerURL
     * @return
     * @throws AxisFault
     */
    public String getDashboardContentAsJson(String userId, String dashboardName, String tDomain, String backendServerURL) throws AxisFault {
        DashboardContentBean dashboardContentBean = getDashboardContent(userId, dashboardName, tDomain, backendServerURL);
        try {
            return dashboardContentBean.toJSONObject().toString();
        } catch (JSONException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage(), e);
        }
    }

    /**
     * Tab content is taken via this method as a JSOn string at each tab refresh.
     *
     * @param userId
     * @param dashboardName
     * @param tDomain
     * @param backendServerURL
     * @param tabId
     * @return
     * @throws AxisFault
     */
    public String getTabContentAsJson(String userId, String dashboardName, String tDomain, String backendServerURL, String tabId) throws AxisFault {
        DashboardContentBean dashboardContentBean = getDashboardContent(userId, dashboardName, tDomain, backendServerURL);
        Tab tab = getTabContentFromId(dashboardContentBean, tabId);
        try {
            return tab.toJSONObject().toString();
        } catch (JSONException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage(), e);
        }
    }

    private Gadget[] getGadgetsForTab(String userId, String dashboardName, String storedLayout) throws Exception {
        ArrayList<String> gadgetIdStore = new ArrayList<String>();

        if ("NA".equalsIgnoreCase(storedLayout)) {
            return new Gadget[0];
        } else if (storedLayout.contains("G1#")) {
            // This is a legacy layout. Grab IDs the old way.
            String[] columnIdCombos = storedLayout.split(",");
            for (int x = 0; x < columnIdCombos.length; x++) {
                String currentId = columnIdCombos[x].split("#")[1];
                if ((currentId != null) && (!"".equals(currentId))) {
                    gadgetIdStore.add(currentId);
                }
            }
        } else {
            getGadgetIdsFromJsonLayout(new JSONObject(storedLayout).getJSONArray("layout"), gadgetIdStore);
        }

        ArrayList<Gadget> gadgets = new ArrayList<Gadget>();

        for (String gadgetId : gadgetIdStore) {
            Gadget gadget = new Gadget();
            gadget.setGadgetId(gadgetId);
            gadget.setGadgetUrl(getGadgetUrl(userId, gadgetId, dashboardName));
            gadget.setGadgetPrefs(getGadgetPrefs(userId, gadgetId, "gadgetUserPrefs-" + gadgetId, dashboardName));
            gadgets.add(gadget);
        }

        Gadget[] gadgetsArray = new Gadget[gadgets.size()];

        return gadgets.toArray(gadgetsArray);

    }

    private Tab getTabContentFromId(DashboardContentBean dcb, String tabId) {
        Tab[] tabs = dcb.getTabs();
        for (Tab tab : tabs) {
            if (tab.getTabId().equals(tabId)) {
                return tab;
            }
        }
        return null;
    }

    /**
     * A webservice method which is used to obtain both tab id and associated name
     */
    public String getTabLayoutWithNames(String userId, String dashboard) throws AxisFault {
        String[] userTabLayout = getTabLayout(userId, dashboard).split(",");

        StringBuilder result = new StringBuilder();
        for (String tabId : userTabLayout) {
            result.append(tabId + "-" + getTabTitle(userId, tabId, dashboard));
            result.append(",");
        }
        if (result.length() != 0) {
            return result.substring(0, result.length() - 1);
        }
        return "";
    }

    /**
     * Populating Gadget resources to registry for the given tenant
     * (On demand  Gadget population)
     *
     * @param tabId
     * @return boolean
     * @throws AxisFault
     */
    public boolean populateDashboardTab(String tabId) throws AxisFault {

        boolean returnValue = false;

        // Read gadgets from {CARBON_HOME}/repository/resources/dashboard/gadgets/
        String gadgetDiskRoot = System.getProperty(ServerConstants.CARBON_HOME) + File.separator +
                "repository" + File.separator + "resources" + File.separator + "dashboard" +
                File.separator + "gadgets" + File.separator;

        String tabResourcePath = "tab" + tabId;

        // Read tab resources from {CARBON_HOME}/repository/resources/dashboard/gadgets/tabX
        String tabDiskLocation = gadgetDiskRoot + tabResourcePath;

        String registryGadgetPath = DashboardConstants.SYSTEM_DASHBOARDS_REGISTRY_ROOT +
                DashboardConstants.GADGET_PATH;

        String serverName = CarbonUtils.getServerConfiguration().getFirstProperty("Name");

        Registry registry = getConfigSystemRegistry();

        try {

            // Following resource already added in dashboard.xml population time
            if (registry.resourceExists(DashboardConstants.SYSTEM_DASHBOARDS_REGISTRY_ROOT + serverName)) {

                Resource serverNameResource = registry.
                        get(DashboardConstants.SYSTEM_DASHBOARDS_REGISTRY_ROOT + serverName);

                // If this tab is not populated yet, populate it
                if (serverNameResource.getProperty(tabResourcePath) == null) {

                    // Following property used to ensure first time population (running only ones)
                    serverNameResource.setProperty(tabResourcePath, tabResourcePath);

                    String tenantDomain = PrivilegedCarbonContext.getCurrentContext().getTenantDomain();

                    // So gadget resources not populated for this tab
                    // Need to populate resources related to this tab
                    File tabResourceDir = new File(tabDiskLocation);
                    if (tabResourceDir.exists()) {
                        beginFileTransfer(tabResourceDir, registry, registryGadgetPath);
                        log.info("Successfully populated the default Gadgets for tab : " + tabId + " in tenant : " + tenantDomain);
                        returnValue = true;

                    } else {
                        log.info("Couldn't find contents at '" + tabDiskLocation + "'. Giving up.");
                    }

                    registry.put(DashboardConstants.SYSTEM_DASHBOARDS_REGISTRY_ROOT + serverName, serverNameResource);

                }

            } else {
                log.debug("Couldn't find a dashboard resources for " + serverName + ", and skipping on demand gadget population");
            }
        } catch (RegistryException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage(), e);
        }

        return returnValue;

    }

    private void beginFileTransfer(File rootDirectory, Registry registry,
                                   String registryGadgetPath) throws RegistryException {

        // Storing the root path for future reference
        String rootPath = rootDirectory.getAbsolutePath();

        // Creating the default gadget collection resource
        Collection defaultGadgetCollection = registry.newCollection();
        try {
            registry.beginTransaction();
            if (!registry.resourceExists(registryGadgetPath)) {

                registry.put(registryGadgetPath, defaultGadgetCollection);
            }

            transferDirectoryContentToRegistry(rootDirectory, rootPath, registry, registryGadgetPath);
            registry.commitTransaction();
        } catch (Exception e) {
            registry.rollbackTransaction();
            log.error(e.getMessage(), e);
        }

    }

    private void transferDirectoryContentToRegistry(File rootDirectory, String rootPath,
                                                    Registry registry, String registryGadgetPath)
            throws FileNotFoundException {

        try {
            File[] filesAndDirs = rootDirectory.listFiles();
            List<File> filesDirs = Arrays.asList(filesAndDirs);

            for (File file : filesDirs) {

                if (!file.isFile()) {
                    // This is a Directory add a new collection
                    // This path is used to store the file resource under registry
                    String directoryRegistryPath =
                            registryGadgetPath + file.getAbsolutePath()
                                    .substring(rootPath.length()).replaceAll("[/\\\\]+", "/");
                    Collection newCollection = registry.newCollection();
                    if (!registry.resourceExists(directoryRegistryPath)) {

                        setAnonymousReadPermission(directoryRegistryPath);


                        registry.put(directoryRegistryPath, newCollection);
                    }

                    // recursive
                    transferDirectoryContentToRegistry(file, rootPath, registry, registryGadgetPath);
                } else {
                    // Add this to registry
                    addToRegistry(rootPath, file, registry, registryGadgetPath);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }


    private void addToRegistry(String rootPath, File file, Registry registry, String registryGadgetPath) {

        try {

            // This path is used to store the file resource under registry
            String fileRegistryPath =
                    registryGadgetPath + file.getAbsolutePath().substring(rootPath.length())
                            .replaceAll("[/\\\\]+", "/");

            // Adding the file to the Registry

            Resource fileResource = registry.newResource();
            String mediaType = MediaTypesUtils.getMediaType(file.getAbsolutePath());
            if (mediaType.equals("application/xml")) {
                fileResource.setMediaType("application/vnd.wso2-gadget+xml");
            } else {
                fileResource.setMediaType(mediaType);
            }
            fileResource.setContentStream(new FileInputStream(file));
            registry.put(fileRegistryPath, fileResource);

            setAnonymousReadPermission(fileRegistryPath);

        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            log.error(e.getMessage(), e);
        } catch (RegistryException e) {
            log.error(e.getMessage(), e);
        } catch (FileNotFoundException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Setting read permission for anonymous user
     *
     * @param fileRegistryPath
     * @throws org.wso2.carbon.user.api.UserStoreException
     *
     */
    private void setAnonymousReadPermission(String fileRegistryPath)
            throws org.wso2.carbon.user.api.UserStoreException {
        AuthorizationManager accessControlAdmin =
                CarbonContext.getCurrentContext().getUserRealm().getAuthorizationManager();

        if (!accessControlAdmin.isRoleAuthorized(CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME,
                RegistryConstants.CONFIG_REGISTRY_BASE_PATH +
                        fileRegistryPath, ActionConstants.GET)) {
            accessControlAdmin.authorizeRole(CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME,
                    RegistryConstants.CONFIG_REGISTRY_BASE_PATH +
                            fileRegistryPath, ActionConstants.GET);
        }
    }

}
