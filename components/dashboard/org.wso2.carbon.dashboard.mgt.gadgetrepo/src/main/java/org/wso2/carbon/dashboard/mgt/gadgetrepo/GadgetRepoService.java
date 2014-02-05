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

package org.wso2.carbon.dashboard.mgt.gadgetrepo;

import org.apache.axiom.om.util.Base64;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.dashboard.DashboardService;
import org.wso2.carbon.dashboard.common.DashboardConstants;
import org.wso2.carbon.dashboard.common.bean.Comment;
import org.wso2.carbon.dashboard.common.bean.Gadget;
import org.wso2.carbon.dashboard.mgt.gadgetrepo.common.CommentSortById;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.social.api.activity.Activity;
import org.wso2.carbon.registry.social.api.activity.ActivityManager;
import org.wso2.carbon.registry.social.impl.activity.ActivityImpl;
import org.wso2.carbon.registry.social.impl.activity.ActivityManagerImpl;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.utils.ServerConstants;

import javax.activation.DataHandler;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class GadgetRepoService extends AbstractAdmin {
    private static final Log log = LogFactory.getLog(GadgetRepoService.class);

    /**
     * The method returns an array of gadgets to the client
     */
    public Gadget[] getGadgetData() {

        List<Resource> rsList = getGadgetResList();
        Registry registry;

        if (rsList == null) {
            return null;
        }

        Gadget[] gadgetList = new Gadget[rsList.size()];

        Resource tempRes;
        Gadget tempGadget;

        try {
            registry = getConfigSystemRegistry();
            for (int i = 0; i < rsList.size(); i++) {
                tempRes = rsList.get(i);

                if ((tempRes.getProperty(DashboardConstants.GADGET_NAME) != null)
                        && (tempRes.getProperty(DashboardConstants.GADGET_URL) != null)) {
                    tempGadget = new Gadget();
                    tempGadget.setGadgetName(tempRes
                            .getProperty(DashboardConstants.GADGET_NAME));

                    tempGadget.setGadgetUrl(tempRes
                            .getProperty(DashboardConstants.GADGET_URL));

                    if (tempRes.getProperty(DashboardConstants.GADGET_DESC) != null) {
                        tempGadget.setGadgetDesc(tempRes
                                .getProperty(DashboardConstants.GADGET_DESC));
                    } else {
                        tempGadget.setGadgetDesc("");
                    }

                    if (tempRes.getProperty(DashboardConstants.USER_CONTER) != null)
                        tempGadget.setUserCount(tempRes
                                .getProperty(DashboardConstants.USER_CONTER));

                    if (tempRes.getProperty(DashboardConstants.DEFAULT_GADGET) != null)
                        tempGadget
                                .setDefaultGadget(tempRes
                                        .getProperty(DashboardConstants.DEFAULT_GADGET));

                    if (tempRes
                            .getProperty(DashboardConstants.UNSIGNED_USER_GADGET) != null)
                        tempGadget
                                .setUnsignedUserGadget(tempRes
                                        .getProperty(DashboardConstants.UNSIGNED_USER_GADGET));

                    if (tempRes.getProperty(DashboardConstants.GADGET_THUMB_URL) != null) {
                        tempGadget.setThumbUrl(tempRes.getProperty(DashboardConstants.GADGET_THUMB_URL));
                    }

                    tempGadget.setGadgetPath(tempRes.getPath());

                    tempGadget.setRating(registry.getAverageRating(tempRes
                            .getPath()));

                    gadgetList[i] = tempGadget;
                }

            }

            return gadgetList;

        } catch (Exception e) {
            log.error("Backend server error : Could not return the gadget list:", e);
            return null;
        }

    }

    /**
     * getting gadgets as a resource list
     *
     * @return gadget resource list
     */
    private List<Resource> getGadgetResList() {
        Registry registry;

        List<Resource> resList = new ArrayList<Resource>();

        String gadgetRootPath = DashboardConstants.SYSTEM_GADGETREPO_REGISTRY_ROOT;
        String gadgetRepoPath = gadgetRootPath + DashboardConstants.GADGETS_COL;
        String nextGadgetIdPath = gadgetRootPath
                + DashboardConstants.GADGETS_COL
                + DashboardConstants.NEXT_GADGET_ID_PATH;

        // getting the gadget data
        try {
            registry = getConfigSystemRegistry();
            if (registry.resourceExists(nextGadgetIdPath)) {

                Collection gadgets = (Collection) registry.get(gadgetRepoPath);

                int gadgetCount = gadgets.getChildCount();
                String[] gadgetPaths = gadgets.getChildren();

                for (int i = 0; i < gadgetCount; i++) {
                    if (registry.resourceExists(gadgetPaths[i])) {
                        Resource tempRes = registry.get(gadgetPaths[i]);
                        resList.add(tempRes);
                    }
                }

                return resList;
            } else {
                return null;
            }

        } catch (Exception e) {
            log.error("Backend server error : Could not retrive gadget meta data or repository is not yet created", e);
            return null;
        }

    }

    /**
     * Adds a gadget to the repository, if the repository does not exist creates
     * the repository location
     */
    public Boolean addGadgetToRepo(String gName, String gUrl, String gDesc,
                                   String gScreen, String screenMediaType) {
        Registry registry = null;

        try {
            if (gadgetUrlExists(gUrl)) {
                return false;

            }
            // get registry instance
            registry = getConfigSystemRegistry();

            // Resource paths
            String gadgetRootPath = DashboardConstants.SYSTEM_GADGETREPO_REGISTRY_ROOT;
            String gadgetRepoPath = gadgetRootPath
                    + DashboardConstants.GADGETS_COL;
            String nextGadgetIdPath = gadgetRootPath
                    + DashboardConstants.GADGETS_COL
                    + DashboardConstants.NEXT_GADGET_ID_PATH;

            // beginning registry transactions
            registry.beginTransaction();

            // if resource is not available create a repository collection
            if (!registry.resourceExists(gadgetRootPath)) {
                Collection gadgetCol = registry.newCollection();
                registry.put(gadgetRepoPath, gadgetCol);
            }

            // resource for storing next unique gadget id; if resource is not
            // available create new with id=0
            Resource nextGadgetIdRes;
            if (!registry.resourceExists(nextGadgetIdPath)) {
                nextGadgetIdRes = registry.newResource();
                nextGadgetIdRes.setProperty(DashboardConstants.NEXT_GADGET_ID,
                        "0");
                registry.put(nextGadgetIdPath, nextGadgetIdRes);
            }

            nextGadgetIdRes = registry.get(nextGadgetIdPath);

            // taking the id to a string
            String gadgetIdStr = nextGadgetIdRes
                    .getProperty(DashboardConstants.NEXT_GADGET_ID);

            int gadgetId = Integer.parseInt(gadgetIdStr);

            /* creating new resource for a new gadget */
            Resource gadget = registry.newResource();

            // gadget properties
            gadget.setProperty(DashboardConstants.GADGET_NAME, gName);
            gadget.setProperty(DashboardConstants.GADGET_URL, gUrl);
            gadget.setProperty(DashboardConstants.GADGET_DESC, gDesc);
            gadget.setProperty(DashboardConstants.DEFAULT_GADGET, "false");

            byte[] imgData = Base64.decode(gScreen);
            gadget.setContent(imgData);
            gadget.setMediaType(screenMediaType);

            // storing the gadget
            registry.put(gadgetRepoPath + DashboardConstants.GADGET_PREFIX
                    + gadgetIdStr, gadget);

            // incrementing the gadget id
            int nextId = gadgetId + 1;
            nextGadgetIdRes.setProperty(DashboardConstants.NEXT_GADGET_ID,
                    Integer.toString(nextId));
            registry.put(nextGadgetIdPath, nextGadgetIdRes);

            // commit registry transaction
            registry.commitTransaction();
            if (getConfigUserRegistry() instanceof UserRegistry) {
                String userId = ((UserRegistry) getConfigUserRegistry()).getUserName();
                // Recording Activities
                ActivityManager manager = new ActivityManagerImpl();
                Activity activity = new ActivityImpl();
                activity.setTitle("added " + gName + "gadget to the gadget repository");
                activity.setUrl(gUrl);
                activity.setBody(gScreen);
                manager.createActivity(userId, "self", "gs", null, activity);
            }

            return true;

        } catch (Exception e) {
            log.error("Failed to save the new gadget : " + e);
            if (registry != null) {
                try {
                    registry.rollbackTransaction();
                } catch (Exception ex) {
                    log.error(ex);
                }
            }

            return null;
        }
    }

    private boolean isGadgetAutharized(String user, String gadgetUrl) throws UserStoreException {
        if (gadgetUrl.startsWith("/registry")) {
            gadgetUrl = gadgetUrl.split("path=")[1];
        } else {
            //GS is not hosting this gadget
            return true;
        }
        UserRegistry registry = (UserRegistry) getConfigUserRegistry();
        return registry.getUserRealm().getAuthorizationManager().isUserAuthorized(user, gadgetUrl, ActionConstants.GET);
    }

    /**
     * Add gadget to user's dashboard
     */
    public Boolean addGadget(String userId, String tabId, String url,
                             String dashboardName, String gadgetGroup, String gadgetPath) {

        Boolean response = false;
        Registry registry = null;
        try {
            DashboardService dashboardService = new DashboardService();
            response = dashboardService.addGadgetToUser(userId, tabId, url, dashboardName, gadgetGroup);
            if (response) {
                // Record Activities
                ActivityManager manager = new ActivityManagerImpl();
                Activity activity = new ActivityImpl();
                activity.setTitle("added new Gadget to the Dashboard ");
                String gadgetViewUrl = "../gadgetrepo/gadget-page.jsp?gadgetPath=" + gadgetPath + "&tab=" + tabId + "&grp=" + gadgetGroup.substring(0, gadgetGroup.indexOf("#"));
                activity.setUrl(gadgetViewUrl);
                manager.createActivity(userId, "self", "gs", null, activity);
            }
        } catch (Exception e) {
            log.error(e);
            if (registry != null) {
                try {
                    registry.rollbackTransaction();
                } catch (Exception ex) {
                    log.error(ex);
                }
            }
        }
        return response;
    }

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

    /**
     * Removes the gadget from the repository
     */
    public Boolean deleteGadget(String gadgetPath) {
        Registry registry;

        try {
            // get registry instance
            registry = getConfigSystemRegistry();

            if (registry.resourceExists(gadgetPath)) {
                registry.delete(gadgetPath);
                return true;

            }
            return false;

        } catch (Exception e) {
            log.error("Backend server error : Could not delete the resource from the registry", e);
            return false;
        }
    }

    /**
     * The method returns the requested gadget
     */
    public Gadget getGadget(String gadgetPath) {
        Registry registry;
        Gadget gadget = null;
        try {
            // get registry instance
            registry = getConfigSystemRegistry();

            if (registry.resourceExists(gadgetPath)) {
                Resource temp = registry.get(gadgetPath);
                gadget = new Gadget();

                if (temp.getProperty(DashboardConstants.GADGET_NAME) != null)
                    gadget.setGadgetName(temp
                            .getProperty(DashboardConstants.GADGET_NAME));

                if (temp.getProperty(DashboardConstants.GADGET_URL) != null)
                    gadget.setGadgetUrl(temp
                            .getProperty(DashboardConstants.GADGET_URL));

                if (temp.getProperty(DashboardConstants.GADGET_DESC) != null) {
                    gadget.setGadgetDesc(temp
                            .getProperty(DashboardConstants.GADGET_DESC));
                } else {
                    gadget.setGadgetDesc("");
                }

                if (temp.getProperty(DashboardConstants.USER_CONTER) != null)
                    gadget.setUserCount(temp
                            .getProperty(DashboardConstants.USER_CONTER));

                if (temp.getProperty(DashboardConstants.DEFAULT_GADGET) != null)
                    gadget.setDefaultGadget(temp
                            .getProperty(DashboardConstants.DEFAULT_GADGET));

                if (temp.getProperty(DashboardConstants.UNSIGNED_USER_GADGET) != null)
                    gadget
                            .setUnsignedUserGadget(temp
                                    .getProperty(DashboardConstants.UNSIGNED_USER_GADGET));

                if (temp.getProperty(DashboardConstants.GADGET_THUMB_URL) != null) {
                    gadget.setThumbUrl(temp.getProperty(DashboardConstants.GADGET_THUMB_URL));
                }

                gadget.setGadgetPath(temp.getPath());

                gadget.setRating(registry.getAverageRating(temp.getPath()));

            }
            return gadget;

        } catch (Exception e) {
            log.error("Backend server error : Could not retrive the gadget", e);
            return null;
        }
    }

    /**
     * The method modifies the gadget information
     */
    public Boolean modifyGadget(String gadegtPath, String gName, String gUrl,
                                String gDesc, String gScreen, String screenMediaType) {
        Registry registry;
        try {
            // get registry instance
            registry = getConfigSystemRegistry();
            if (registry.resourceExists(gadegtPath)) {
                Resource gadget = registry.get(gadegtPath);

                if (!gadget.getProperty(DashboardConstants.GADGET_URL).equals(gUrl) && gadgetUrlExists(gUrl)) {
                    return false;
                }
                gadget.setProperty(DashboardConstants.GADGET_NAME, gName);
                gadget.setProperty(DashboardConstants.GADGET_URL, gUrl);
                gadget.setProperty(DashboardConstants.GADGET_DESC, gDesc);

                if (gScreen != null && !gScreen.equals("")) {
                    byte[] imgData = Base64.decode(gScreen);
                    gadget.setContent(imgData);
                    gadget.setMediaType(screenMediaType);
                } else {
                    gadget.setContent(gadget.getContent());
                }

                registry.put(gadegtPath, gadget);

                return true;
            }

        } catch (Exception e) {
            log.error("Backend server error : Could not modify gadget", e);
            return null;
        }
        return false;
    }

    /**
     * The method adds comments to a gadget
     */
    public Boolean addCommentForGadget(String gadgetPath, Comment comment) {
        try {

            Registry registry = getConfigUserRegistry();

            org.wso2.carbon.registry.core.Comment gadgetComment = new org.wso2.carbon.registry.core.Comment();

            gadgetComment.setAuthorUserName(comment.getAuthorUserName());
            gadgetComment.setText(comment.getCommentText());

            gadgetComment.setCreatedTime(new Date());

            registry.addComment(gadgetPath, gadgetComment);
            // Recording Activities
            ActivityManager manager = new ActivityManagerImpl();
            Activity activity = new ActivityImpl();
            activity.setTitle("commented on Gadget");
            activity.setUrl("../" + comment.getCommentPath());
            activity.setBody(comment.getCommentText());
            manager.createActivity(comment.getAuthorUserName(), "self", "gs", null, activity);
            return true;
        } catch (Exception e) {
            log.error("Backend server error : Could not make the comment", e);
            return false;
        }

    }

    /**
     * The method to rate a gadget
     */
    public Boolean addRatingForGadget(String gadgetPath, int rating, String tabId, String gadgetGroup) {
        try {
            Registry registry = getConfigUserRegistry();
            registry.rateResource(gadgetPath, rating);

            if (registry instanceof UserRegistry) {
                String userId = ((UserRegistry) registry).getUserName();
                // Recording Activities
                ActivityManager manager = new ActivityManagerImpl();
                Activity activity = new ActivityImpl();
                activity.setTitle("rated gadget with rating: " + rating);
                String gadgetViewUrl = "../gadgetrepo/gadget-page.jsp?gadgetPath=" + gadgetPath + "&tab=" + tabId + "&grp=" + gadgetGroup;
                activity.setUrl(gadgetViewUrl);
                activity.setBody(rating + "");
                manager.createActivity(userId, "self", "gs", null, activity);
            }

            return true;
        } catch (Exception e) {
            log.error("Backend server error : Could not rate the resource", e);
            return false;
        }

    }

    /**
     * The method deletes a comment
     */
    public Boolean deleteComment(String path) {
        try {
            Registry registry = getConfigSystemRegistry();
            registry.delete(path);
            return true;
        } catch (Exception e) {
            log.error("Backend server error : Could not delete the comment", e);
            return false;
        }

    }

    public Boolean isAdmin(String userId) {
        try {
            String[] roles = getUserRealm().getUserStoreManager().getRoleListOfUser(userId);
            for (String role : roles) {
                if ("admin".equals(role)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            log.error("Backend server error : Could not retrive the user realm", e);
            return false;
        }
    }

    /**
     * Checking if the user already has the gadget
     *
     * @param gadgetPath = gadget's relative path to the config registry
     * @return
     */
    public Boolean userHasGadget(String gadgetPath) {
        String userName = (String) getHttpSession().getAttribute(ServerConstants.USER_LOGGED_IN);
        try {
            Registry registry = getConfigSystemRegistry();
            Collection gadgetCol;

            Resource addedGadget = registry.get(gadgetPath);
            String gadgetUrl = addedGadget.getProperty("gadgetUrl");

            String pathToUserGadgets = DashboardConstants.USER_DASHBOARD_REGISTRY_ROOT
                    + userName
                    + DashboardConstants.REGISTRY_PRODUCT_ID_PATH
                    + DashboardConstants.GADGET_PATH;

            gadgetCol = (Collection) registry.get(pathToUserGadgets);

            String[] gadgets = gadgetCol.getChildren();

            for (String gPath : gadgets) {
                Resource gadget = registry.get(gPath);
                if (gadgetUrl.equals(gadget.getProperty("gadgetUrl"))) {
                    return true;
                }
            }
            return false;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }

    }

    public Boolean incrementUserCount(String gadgetPath) {
        try {
            Registry registry = getConfigSystemRegistry();
            if (gadgetPath == null || "null".equals(gadgetPath) || userHasGadget(gadgetPath)) {
                return false;
            }
            if (registry.resourceExists(gadgetPath)) {
                Resource gadget = registry.get(gadgetPath);

                String userCountStr = gadget
                        .getProperty(DashboardConstants.USER_CONTER);

                Integer userCount;
                if (userCountStr == null) {
                    userCount = 1;
                } else {
                    userCount = Integer.parseInt(userCountStr);
                    userCount++;
                }
                gadget.setProperty(DashboardConstants.USER_CONTER, userCount
                        .toString());

                registry.put(gadgetPath, gadget);
                return true;
            } else {
                log.error("Backend server error : Gadget Path does not exist");
                return false;
            }

        } catch (Exception e) {
            log.error("Backend server error : Could not increment the user counter", e);
            return false;
        }

    }

    public String getUserRating(String gadgetPath, String userId) {
        try {
            Registry registry = getConfigSystemRegistry();
            if (gadgetPath == null || "null".equals(gadgetPath)) {
                return null;
            }
            if (registry.resourceExists(gadgetPath)) {
                int ratVal = registry.getRating(gadgetPath, userId);
                return Integer.toString(ratVal);
            } else {
                log.error("Backend server error : Gadget Path does not exist");
                return null;
            }

        } catch (Exception e) {
            log.error("Backend server error : Could not get rating for the user", e);
            return null;
        }

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
     * Following Methods related to pagination of gadget list
     * Getting the gadget collection size
     */
    public Integer getCollectionSize() {
        Registry registry;
        String gadgetRootPath = DashboardConstants.SYSTEM_GADGETREPO_REGISTRY_ROOT;
        String gadgetRepoPath = gadgetRootPath + DashboardConstants.GADGETS_COL;
        try {
            registry = getConfigSystemRegistry();
            if (registry.resourceExists(gadgetRepoPath)) {
                Collection gadgetCol = (Collection) registry
                        .get(gadgetRepoPath);
                return gadgetCol.getChildCount();
            } else {
                return null;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    /* getting a chunck of gadgets as an array */

    public Gadget[] getGadgetDataPag(int upper, int lower) {

        List<Resource> rsList = getGadgetResList(upper, lower);
        Registry registry;
        if (rsList == null) {
            return null;
        }

        Gadget[] gadgetList = new Gadget[rsList.size()];

        Resource tempRes;
        Gadget tempGadget;

        try {
            registry = getConfigSystemRegistry();
            for (int i = 0; i < rsList.size(); i++) {
                tempRes = rsList.get(i);

                if ((tempRes.getProperty(DashboardConstants.GADGET_NAME) != null)
                        && (tempRes.getProperty(DashboardConstants.GADGET_URL) != null)) {
                    tempGadget = new Gadget();
                    tempGadget.setGadgetName(tempRes
                            .getProperty(DashboardConstants.GADGET_NAME));

                    tempGadget.setGadgetUrl(tempRes
                            .getProperty(DashboardConstants.GADGET_URL));

                    if (tempRes.getProperty(DashboardConstants.GADGET_DESC) != null) {
                        tempGadget.setGadgetDesc(tempRes
                                .getProperty(DashboardConstants.GADGET_DESC));
                    } else {
                        tempGadget.setGadgetDesc("");
                    }

                    if (tempRes.getProperty(DashboardConstants.USER_CONTER) != null)
                        tempGadget.setUserCount(tempRes
                                .getProperty(DashboardConstants.USER_CONTER));

                    if (tempRes.getProperty(DashboardConstants.DEFAULT_GADGET) != null)
                        tempGadget
                                .setDefaultGadget(tempRes
                                        .getProperty(DashboardConstants.DEFAULT_GADGET));

                    if (tempRes
                            .getProperty(DashboardConstants.UNSIGNED_USER_GADGET) != null)
                        tempGadget
                                .setUnsignedUserGadget(tempRes
                                        .getProperty(DashboardConstants.UNSIGNED_USER_GADGET));

                    if (tempRes.getProperty(DashboardConstants.GADGET_THUMB_URL) != null) {
                        tempGadget.setThumbUrl(tempRes.getProperty(DashboardConstants.GADGET_THUMB_URL));
                    }

                    tempGadget.setGadgetPath(tempRes.getPath());

                    tempGadget.setRating(registry.getAverageRating(tempRes
                            .getPath()));

                    gadgetList[i] = tempGadget;
                }

            }

            return gadgetList;

        } catch (Exception e) {
            log.error("Backend server error : Could not return the gadget list : ", e);
            return null;
        }

    }

    /* Overloaded method : Getting a chunck of gadgets as a resource list */

    private List<Resource> getGadgetResList(int upper, int lower) {
        Registry registry;

        List<Resource> resList = new ArrayList<Resource>();

        String gadgetRootPath = DashboardConstants.SYSTEM_GADGETREPO_REGISTRY_ROOT;
        String gadgetRepoPath = gadgetRootPath + DashboardConstants.GADGETS_COL;
        String nextGadgetIdPath = gadgetRootPath
                + DashboardConstants.GADGETS_COL
                + DashboardConstants.NEXT_GADGET_ID_PATH;

        // getting the gadget data
        try {
            registry = getConfigSystemRegistry();
            if (registry.resourceExists(nextGadgetIdPath)) {

                // Get all the gadgets
                Collection gadgets = (Collection) registry.get(gadgetRepoPath);
                int gadgetCount = gadgets.getChildCount();
                String[] gadgetPaths = gadgets.getChildren();

                // Put all gadgets to a List and avoid nextGadgetId resource
                for (int i = 0; i < gadgetCount; i++) {
                    if (registry.resourceExists(gadgetPaths[i])) {
                        Resource tempRes = registry.get(gadgetPaths[i]);
                        if (!tempRes.getId().equals(nextGadgetIdPath)) {
                            resList.add(tempRes);
                        }
                    }
                }

                // Comparator which sorts list according to the resource created time
                Comparator<Resource> creationTimeComparator = new Comparator<Resource>() {
                    public int compare(Resource r1, Resource r2) {
                        Date createdTime1 = r1.getCreatedTime();
                        Date createdTime2 = r2.getCreatedTime();
                        if (createdTime1.compareTo(createdTime2) > 0) {
                            return -1;
                        } else if (createdTime1.compareTo(createdTime2) < 0) {
                            return 1;
                        } else {
                            return 0;
                        }
                    }
                };

                // Sort the collection (Last added gadget first)
                Collections.sort(resList, creationTimeComparator);
                int lowerBound = lower + upper;
                if (lowerBound > gadgetCount) {
                    lowerBound = gadgetCount - 1;
                }

                // create the subList to return
                List<Resource> subResList = new ArrayList<Resource>();
                for (int i = upper; i < lowerBound; i++) {
                    // Ensures correct sub list when number of gadget are less than i
                    if (resList.size() > i) {
                        subResList.add(resList.get(i));
                    }
                }

                return subResList;
            } else {
                return null;
            }

        } catch (Exception e) {
            log.error("Backend server error - pagination method : Could not retrive gadget meta data or repository is not yet created", e);
            return null;
        }

    }

    /**
     * The method returns the total number of comments for a gadget
     */
    public Integer getCommentsCount(String resPath) {
        Registry registry;
        try {
            registry = getConfigSystemRegistry();

            return registry.getComments(resPath).length;

        } catch (Exception e) {
            log.error("Backend server error - could not get the comment count", e);
            return null;
        }
    }

/*    *//**
     * The method returns a chunk of comments for a resource
     *//*
    public Comment[] getCommentSet(String resPath, int start, int size) {
        Registry registry;
        try {
            registry = getConfigSystemRegistry();

            //Resource comQuery = registry.newResource();

            String pathSplit[] = resPath
                    .split(DashboardConstants.SYSTEM_GADGETREPO_REGISTRY_ROOT
                            + DashboardConstants.GADGETS_COL + "/");
            String reg_path_value = RegistryConstants.CONFIG_REGISTRY_BASE_PATH + DashboardConstants.SYSTEM_GADGETREPO_REGISTRY_ROOT
                    + DashboardConstants.GADGETS_COL;
            String reg_name = pathSplit[1];

            String sql;

            Map<String, Object> params = new HashMap<String, Object>();

            *//*
            database type filter had to be done inorder to execute database level pagination
             *//*
            Resource res = (Resource)registry.get(resPath);

            res.getProperties();

            if ("postgresql".equals(DatabaseCreator.getDatabaseType(registry.getRegistryContext().getDataSource().getConnection()))) {
                sql = "SELECT REG_COMMENT_ID FROM REG_RESOURCE_COMMENT RC, REG_RESOURCE R, REG_COMMENT C, REG_PATH P WHERE "
                        + "RC.REG_VERSION=R.REG_VERSION AND "
                        + "R.REG_NAME=? AND "
                        + "P.REG_PATH_VALUE=? AND "
                        + "C.REG_ID = RC.REG_COMMENT_ID AND "
                        + "P.REG_PATH_ID=R.REG_PATH_ID ORDER BY C.REG_COMMENTED_TIME DESC LIMIT ? OFFSET ?";

                params.put("query", sql);
                params.put(RegistryConstants.RESULT_TYPE_PROPERTY_NAME, RegistryConstants.COMMENTS_RESULT_TYPE);
                params.put("1", reg_name);
                params.put("2", reg_path_value);
                params.put("3", size);
                params.put("4", start);

            } else if ("mysql".equals(DatabaseCreator.getDatabaseType(registry.getRegistryContext().getDataSource().getConnection()))) {
                sql = "SELECT REG_COMMENT_ID FROM REG_RESOURCE_COMMENT RC, REG_RESOURCE R, REG_COMMENT C, REG_PATH P WHERE "
                        + "RC.REG_VERSION=R.REG_VERSION AND "
                        + "R.REG_NAME=? AND "
                        + "P.REG_PATH_VALUE=? AND "
                        + "C.REG_ID = RC.REG_COMMENT_ID AND "
                        + "P.REG_PATH_ID=R.REG_PATH_ID ORDER BY C.REG_COMMENTED_TIME DESC LIMIT ?, ?";

                params.put("query", sql);
                params.put(RegistryConstants.RESULT_TYPE_PROPERTY_NAME, RegistryConstants.COMMENTS_RESULT_TYPE);
                params.put("1", reg_name);
                params.put("2", reg_path_value);
                params.put("3", start);
                params.put("4", size);
            } else if ("h2".equals(DatabaseCreator.getDatabaseType(registry.getRegistryContext().getDataSource().getConnection()))) {
                sql = "SELECT REG_COMMENT_ID FROM REG_RESOURCE_COMMENT RC, REG_RESOURCE R, REG_COMMENT C, REG_PATH P WHERE "
                        + "RC.REG_VERSION=R.REG_VERSION AND "
                        + "R.REG_NAME=? AND "
                        + "P.REG_PATH_VALUE=? AND "
                        + "C.REG_ID = RC.REG_COMMENT_ID AND "
                        + "P.REG_PATH_ID=R.REG_PATH_ID ORDER BY C.REG_COMMENTED_TIME DESC LIMIT ?, ?";

                params.put("query", sql);
                params.put(RegistryConstants.RESULT_TYPE_PROPERTY_NAME, RegistryConstants.COMMENTS_RESULT_TYPE);
                params.put("1", reg_name);
                params.put("2", reg_path_value);
                params.put("3", start);
                params.put("4", size);
            } else {
                sql = "SELECT REG_COMMENT_ID FROM REG_RESOURCE_COMMENT RC, REG_RESOURCE R, REG_COMMENT C, REG_PATH P WHERE "
                        + "RC.REG_VERSION=R.REG_VERSION AND "
                        + "R.REG_NAME=? AND "
                        + "P.REG_PATH_VALUE=? AND "
                        + "C.REG_ID = RC.REG_COMMENT_ID AND "
                        + "P.REG_PATH_ID=R.REG_PATH_ID ORDER BY C.REG_COMMENTED_TIME DESC";

                params.put("query", sql);
                params.put(RegistryConstants.RESULT_TYPE_PROPERTY_NAME, RegistryConstants.COMMENTS_RESULT_TYPE);
                params.put("1", reg_name);
                params.put("2", reg_path_value);
            }


            Collection qResults = registry.executeQuery("/", params);

            String[] qPaths = (String[]) qResults.getContent();
            int commentsCount = qPaths.length;
            if (qPaths.length > size) {
                qPaths = qResults.getChildren(start, size);
                commentsCount = qPaths.length;
            }

            org.wso2.carbon.registry.core.Comment[] comments = new org.wso2.carbon.registry.core.Comment[commentsCount];

            for (int i = 0; i < commentsCount; i++) {
                if (registry.resourceExists(qPaths[i])) {
                    comments[i] = (org.wso2.carbon.registry.core.Comment) registry
                            .get(qPaths[i]);
                }
            }

            Comment[] gadgetComments = new Comment[comments.length];

            for (int p = 0; p < comments.length; p++) {
                Comment tempCom = new Comment();

                tempCom.setCommentPath(comments[p].getCommentPath());

                tempCom.setAuthorUserName(comments[p].getAuthorUserName());
                tempCom.setCommentText(comments[p].getText());

                tempCom.setCreateTime(comments[p].getCreatedTime());

                gadgetComments[p] = tempCom;
            }

            Arrays.sort(gadgetComments, new CommentSortById());

            return gadgetComments;

        } catch (Exception e) {
            log.error("Backend server error - could not get the comment set", e);
            return null;
        }

    }*/

    /**
     * The method returns a chunk of comments for a resource
     */
    public Comment[] getCommentSet(String resPath, int start, int size) {
        Registry registry;
        try {
            registry = getConfigSystemRegistry();

            org.wso2.carbon.registry.core.Comment[] commentsInFull = registry.getComments(resPath);

            if (start < 0) {
                start = 0;
            }

            int end = start + size;
            if (end > commentsInFull.length) {
                end = start + (commentsInFull.length - start);
            }

            Arrays.sort(commentsInFull, new CommentSortById());
            List<org.wso2.carbon.registry.core.Comment> commentList = Arrays.asList(commentsInFull).subList(start, end);

            if (commentList == null && commentList.size() == 0) {
                log.error("Backend server error - Comment set is empty");
                return null;
            }

            //org.wso2.carbon.registry.core.Comment[] comments = new org.wso2.carbon.registry.core.Comment[commentList.size()];
            //comments = (org.wso2.carbon.registry.core.Comment[])commentList.toArray();

            Comment[] gadgetComments = new Comment[commentList.size()];

            for (int p = 0; p < commentList.size(); p++) {
                Comment tempCom = new Comment();

                tempCom.setCommentPath(commentList.get(p).getCommentPath());

                tempCom.setAuthorUserName(commentList.get(p).getAuthorUserName());
                tempCom.setCommentText(commentList.get(p).getText());

                tempCom.setCreateTime(commentList.get(p).getCreatedTime());

                gadgetComments[p] = tempCom;
            }

            //Arrays.sort(gadgetComments, new CommentSortById());

            return gadgetComments;

        } catch (Exception e) {
            log.error("Backend server error - could not get the comment set", e);
            return null;
        }

    }

    public Boolean makeGadgetDefault(String gadgetPath, Boolean isMakeDefault) {
        try {
            Registry registry = getConfigSystemRegistry();
            if (registry.resourceExists(gadgetPath)) {
                Resource res = registry.get(gadgetPath);
                if (isMakeDefault) {
                    res.setProperty(DashboardConstants.DEFAULT_GADGET, "true");
                } else {
                    res.setProperty(DashboardConstants.DEFAULT_GADGET, "false");
                }
                registry.put(gadgetPath, res);
            }
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public String[] getDefaultGadgetUrlSet() {
        Registry registry;
        try {
            registry = getConfigSystemRegistry();

            Resource comQuery = registry.newResource();

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

            String[] urls = new String[qPaths.length];

            for (int i = 0; i < qPaths.length; i++) {
                if (registry.resourceExists(qPaths[i])) {
                    urls[i] = registry.get(qPaths[i]).getProperty(
                            DashboardConstants.GADGET_URL);
                }
            }
            return urls;

        } catch (Exception e) {
            log.error("Backend server error - could not get the comment set", e);
            return null;
        }

    }

    public Boolean makeUnsignedUserGadget(String gadgetPath,
                                          Boolean isMakeDefault) {
        try {
            Registry registry = getConfigSystemRegistry();
            if (registry.resourceExists(gadgetPath)) {
                Resource res = registry.get(gadgetPath);
                if (isMakeDefault) {
                    res.setProperty(DashboardConstants.UNSIGNED_USER_GADGET,
                            "true");
                } else {
                    res.setProperty(DashboardConstants.UNSIGNED_USER_GADGET,
                            "false");
                }
                registry.put(gadgetPath, res);
            }
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    private Boolean gadgetUrlExists(String newUrl) {
        Registry registry;
        try {
            registry = getConfigSystemRegistry();

            Resource comQuery = registry.newResource();

            String gadgetMetaDataPath = RegistryConstants.CONFIG_REGISTRY_BASE_PATH
                    + DashboardConstants.SYSTEM_GADGETREPO_REGISTRY_ROOT
                    + DashboardConstants.GADGETS_COL;

            String sql = "SELECT R.REG_NAME, R.REG_PATH_ID FROM REG_RESOURCE R, REG_PROPERTY P, " +
                    "REG_RESOURCE_PROPERTY RP, REG_PATH PA WHERE R.REG_VERSION=RP.REG_VERSION AND " +
                    "P.REG_NAME='gadgetUrl' AND " +
                    "P.REG_VALUE LIKE ? AND " +
                    "P.REG_ID=RP.REG_PROPERTY_ID AND " +
                    "PA.REG_PATH_ID=R.REG_PATH_ID AND " +
                    "PA.REG_PATH_VALUE='" + gadgetMetaDataPath + "'";

            Map<String, String> params = new HashMap<String, String>();

            params.put("1", newUrl.trim());
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

    public Boolean deleteGadgetImage(String path) {
        try {
            Registry registry = getConfigSystemRegistry();
            if (registry.resourceExists(path)) {
                Resource tempRes = registry.get(path);

                if (tempRes.getProperty(DashboardConstants.GADGET_THUMB_URL) != null) {
                    // remove the thumbnail related property from resource
                    tempRes.removeProperty(DashboardConstants.GADGET_THUMB_URL);
                }
                registry.put(path, tempRes);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * Add Gadget to registry from given local file
     *
     * @param gadgetContent DataHandler with gadget file data
     * @param gName         Gadget Name
     * @return gadget path
     * @throws GadgetRepoException
     */
    private String addGadgetToRegistryFromFile(DataHandler gadgetContent, String gName) throws GadgetRepoException {
        Registry registry = null;
        try {
            registry = getConfigSystemRegistry();
            String regGadgetPath = DashboardConstants.GS_REGISTRY_ROOT +
                    DashboardConstants.GADGET_PATH + gName + RegistryConstants.PATH_SEPARATOR +
                    gName;
            Resource gadget = registry.newResource();

            gadget.setContentStream(gadgetContent.getInputStream());
            gadget.setMediaType("application/vnd.wso2-gadget+xml");

            if (!registry.resourceExists(regGadgetPath)) {
                registry.put(regGadgetPath, gadget);
            }

            String gadgetPath = "/registry/resource" + RegistryConstants.CONFIG_REGISTRY_BASE_PATH + regGadgetPath;

            return gadgetPath;

        } catch (Exception e) {
            throw new GadgetRepoException(e);
        }
    }

    private String[] addGadgetToRegistryFromZip(DataHandler gadgetContent, String gName)
            throws GadgetRepoException {

        Registry registry;
        String returnArray[] = new String[2];

        String configRegistryPath = "/registry/resource" + RegistryConstants.CONFIG_REGISTRY_BASE_PATH;
        String regGadgetPath = DashboardConstants.GS_REGISTRY_ROOT + DashboardConstants.GADGET_PATH +
                gName + RegistryConstants.PATH_SEPARATOR;

        String gadgetPath = configRegistryPath + regGadgetPath;
        String thumbPath = configRegistryPath + regGadgetPath;

        ZipInputStream zipInputStream = null;

        try {
            registry = getConfigSystemRegistry();
            Resource gadget = registry.newResource();

            gadget.setContentStream(gadgetContent.getInputStream());
            gadget.setMediaType(DashboardConstants.GADGET_MEDIA_TYPE);
            gadget.setProperty(DashboardConstants.GADGET_NAME, gName);

            if (!registry.resourceExists(regGadgetPath)) {
                // Using handler to put gadget
                registry.put(regGadgetPath, gadget);
            }

            zipInputStream = new ZipInputStream(gadget.getContentStream());
            ZipEntry zipentry = null;

            try {
                zipentry = zipInputStream.getNextEntry();
                if (zipentry == null) {
                    throw new GadgetRepoException("Gadget bundle should be a zip file");
                }
            } catch (IOException e) {
                log.error(e);
            }

            // Iterate through the zip and identify gadgetPath and thumbPath
            while (zipentry != null) {
                String entryName = zipentry.getName();

                if (!zipentry.isDirectory()) {
                    // if the entry is not a directory then check for gadget.xml and thumbnail
                    if (entryName.contains(".xml")) {
                        gadgetPath = gadgetPath + entryName;
                    }
                    if (entryName.contains("thumb")) {
                        thumbPath = thumbPath + entryName;
                    }
                }
                try {
                    zipentry = zipInputStream.getNextEntry();
                } catch (IOException e) {
                    log.error(e);
                }
            }
        } catch (Exception e) {
            log.error("Failed to add gadget to registry from Zip file.");
            throw new GadgetRepoException(e);
        } finally {
            try {
                zipInputStream.close();
            } catch (IOException e) {
                log.error(e);
            }

        }

        if (!(configRegistryPath + regGadgetPath).equals(gadgetPath.toString())) {
            returnArray[0] = gadgetPath.toString();
        } else {
            returnArray[0] = null;
        }

        if (!(configRegistryPath + regGadgetPath).equals(thumbPath.toString())) {
            returnArray[1] = thumbPath.toString();
        } else {
            returnArray[1] = null;
        }

        return returnArray;

    }

    /**
     * Add gadget to registry from given registry file
     * Set the media type of file
     *
     * @param regPath registry path to file
     * @return gadget path
     * @throws GadgetRepoException
     */
    private String addGadgetToRegistryFromRegistry(String regPath) throws GadgetRepoException {
        Registry registry = null;
        try {
            registry = getConfigSystemRegistry();
            Resource gadget = registry.get(regPath);

            gadget.setMediaType("application/vnd.wso2-gadget+xml");

            String gadgetPath = "/registry/resource" + RegistryConstants.CONFIG_REGISTRY_BASE_PATH + regPath;

            return gadgetPath;

        } catch (Exception e) {
            throw new GadgetRepoException(e);
        }
    }

    /**
     * Adding a gadget to the repository
     *
     * @param gName           name of the gadget
     * @param gUrl            gadget url to be saved i.e /registry/repository/_system/..
     * @param gDesc           Gadget description
     * @param gScreen         Gadget screenshot
     * @param screenMediaType Gadget
     * @param gadgetContent   mediatype
     * @return
     */
    public Boolean addGadgetEntryToRepo(String gName, String gUrl, String gDesc,
                                        DataHandler gScreen, String screenMediaType, DataHandler gadgetContent) {
        Registry registry = null;
        String thumbRegistryPath = null;

        try {
            if (gUrl != null && !"".equals(gUrl)) {
                if ("conf:".equals(gUrl.substring(0, 5))) {
                    gUrl = addGadgetToRegistryFromRegistry(gUrl.substring(5, gUrl.length()));
                }
            }
            if (gadgetContent != null) {
                // In the case of zip file screenMediaType contains gadget media type
                // So add gadget using addGadgetToRegistryFromZip function
                if (DashboardConstants.GADGET_MEDIA_TYPE.equals(screenMediaType)) {
                    String[] gadgetData = addGadgetToRegistryFromZip(gadgetContent, gName.replace(" ", ""));
                    gUrl = gadgetData[0];
                    thumbRegistryPath = gadgetData[1];
                    if (gUrl == null) {
                        log.error("Backend server error : Gadget xml can not be found in zip file");
                        return false;
                    }
                } else {
                    // If a file then add gadget using file
                    gUrl = addGadgetToRegistryFromFile(gadgetContent, gName.replace(" ", ""));
                }
            }

            if (gadgetUrlExists(gUrl)) {
                log.error("Backend server error : Gadget Url already exist");
                return false;
            }
            // get registry instance
            registry = getConfigSystemRegistry();

            // Resource paths
            String gadgetRootPath = DashboardConstants.SYSTEM_GADGETREPO_REGISTRY_ROOT;
            String gadgetRepoPath = gadgetRootPath
                    + DashboardConstants.GADGETS_COL;
            String nextGadgetIdPath = gadgetRootPath
                    + DashboardConstants.GADGETS_COL
                    + DashboardConstants.NEXT_GADGET_ID_PATH;

            // beginning registry transactions
            registry.beginTransaction();

            // if resource is not available create a repository collection
            if (!registry.resourceExists(gadgetRootPath)) {
                Collection gadgetCol = registry.newCollection();
                registry.put(gadgetRepoPath, gadgetCol);
            }

            // resource for storing next unique gadget id; if resource is not
            // available create new with id=0
            Resource nextGadgetIdRes;
            if (!registry.resourceExists(nextGadgetIdPath)) {
                nextGadgetIdRes = registry.newResource();
                nextGadgetIdRes.setProperty(DashboardConstants.NEXT_GADGET_ID,
                        "0");
                registry.put(nextGadgetIdPath, nextGadgetIdRes);
            }

            nextGadgetIdRes = registry.get(nextGadgetIdPath);

            // taking the id to a string
            String gadgetIdStr = nextGadgetIdRes
                    .getProperty(DashboardConstants.NEXT_GADGET_ID);

            int gadgetId = Integer.parseInt(gadgetIdStr);

            /* creating new resource for a new gadget */
            Resource gadget = registry.newResource();

            // gadget properties
            gadget.setProperty(DashboardConstants.GADGET_NAME, gName);
            gadget.setProperty(DashboardConstants.GADGET_URL, gUrl);
            gadget.setProperty(DashboardConstants.GADGET_DESC, gDesc);
            gadget.setProperty(DashboardConstants.DEFAULT_GADGET, "false");

            if (gScreen != null) {
                thumbRegistryPath = addGadgetThumbToRegistry(gScreen, screenMediaType, gName.replace(" ", ""));
            }
            if (thumbRegistryPath != null) {
                gadget.setProperty(DashboardConstants.GADGET_THUMB_URL, thumbRegistryPath);
            }

            // storing the gadget
            registry.put(gadgetRepoPath + DashboardConstants.GADGET_PREFIX
                    + gadgetIdStr, gadget);

            // incrementing the gadget id
            int nextId = gadgetId + 1;
            nextGadgetIdRes.setProperty(DashboardConstants.NEXT_GADGET_ID,
                    Integer.toString(nextId));
            registry.put(nextGadgetIdPath, nextGadgetIdRes);

            // commit registry transaction
            registry.commitTransaction();
            if (getConfigUserRegistry() instanceof UserRegistry) {
                String userId = ((UserRegistry) getConfigUserRegistry()).getUserName();
                // Recording Activities
                ActivityManager manager = new ActivityManagerImpl();
                Activity activity = new ActivityImpl();
                activity.setTitle("added " + gName + "gadget to the gadget repository");
                activity.setUrl(gUrl);
                //activity.setBody(gScreen);
                manager.createActivity(userId, "self", "gs", null, activity);
            }

            return true;

        } catch (Exception e) {
            log.error("Failed to save the new gadget : " + e);
            if (registry != null) {
                try {
                    registry.rollbackTransaction();
                } catch (Exception ex) {
                    log.error(ex);
                }
            }

            return null;
        }
    }

    /**
     * Modify the added gadgets inside the repository
     *
     * @param gadegtPath      path to gadget
     * @param gName           name of the gadget
     * @param gUrl            gadget url to be saved i.e /registry/repository/_system/..
     * @param gDesc           description
     * @param gScreen         DataHandler to manage gadget screen
     * @param screenMediaType screenMediaType
     * @param gadgetContent   DataHandler  to manage gadget file content
     * @return the status of modify operation
     */
    public Boolean modifyGadgetEntry(String gadegtPath, String gName, String gUrl,
                                     String gDesc, DataHandler gScreen, String screenMediaType, DataHandler gadgetContent) {
        Registry registry;
        try {

            if (gUrl != null && !"".equals(gUrl)) {
                //If gUrl is contains path to a registry file
                if (gUrl.length() >= 5 && "conf:".equals(gUrl.substring(0, 5))) {
                    // Set gadget url (set media type to registry file)
                    gUrl = addGadgetToRegistryFromRegistry(gUrl.substring(5, gUrl.length()));
                }
            }
            if (gadgetContent != null) {
                gUrl = addGadgetToRegistryFromFile(gadgetContent, gName.replace(" ", ""));
            }

            // get registry instance
            registry = getConfigSystemRegistry();
            if (registry.resourceExists(gadegtPath)) {
                Resource gadget = registry.get(gadegtPath);

                gadget.setProperty(DashboardConstants.GADGET_NAME, gName);
                gadget.setProperty(DashboardConstants.GADGET_URL, gUrl);
                gadget.setProperty(DashboardConstants.GADGET_DESC, gDesc);

                if (gScreen != null) {
                    String thumbRegistryPath = addGadgetThumbToRegistry(gScreen, screenMediaType, gName.replace(" ", ""));
                    gadget.setProperty(DashboardConstants.GADGET_THUMB_URL, thumbRegistryPath);
                }

                registry.put(gadegtPath, gadget);

                return true;
            }

        } catch (Exception e) {
            log.error("Backend server error : Could not modify gadget", e);
            return null;
        }
        return false;
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

/* getting the gadget by matching with the gadget name */

    public Gadget[] getGadgetByName(String name) {

        List<Resource> rsList = getGadgetResList();
        Registry registry;
        if (rsList == null) {
            return null;
        }

        Gadget[] gadgetList = new Gadget[rsList.size()];
        int j = 0;
        Resource tempRes;
        Gadget tempGadget;

        try {
            registry = getConfigSystemRegistry();
            for (int i = 0; i < rsList.size(); i++) {
                tempRes = rsList.get(i);
                String gadgetName = tempRes.getProperty(DashboardConstants.GADGET_NAME);
                if (gadgetName != null) {

                    if (gadgetName.matches("(?i).*" + name + ".*")) {

                        tempGadget = new Gadget();
                        if (tempRes.getProperty(DashboardConstants.GADGET_URL) != null) {
                            tempGadget.setGadgetName(tempRes
                                    .getProperty(DashboardConstants.GADGET_NAME));

                            tempGadget.setGadgetUrl(tempRes
                                    .getProperty(DashboardConstants.GADGET_URL));

                            if (tempRes.getProperty(DashboardConstants.GADGET_DESC) != null) {
                                tempGadget.setGadgetDesc(tempRes
                                        .getProperty(DashboardConstants.GADGET_DESC));
                            } else {
                                tempGadget.setGadgetDesc("");
                            }

                            if (tempRes.getProperty(DashboardConstants.USER_CONTER) != null)
                                tempGadget.setUserCount(tempRes
                                        .getProperty(DashboardConstants.USER_CONTER));

                            if (tempRes.getProperty(DashboardConstants.DEFAULT_GADGET) != null)
                                tempGadget
                                        .setDefaultGadget(tempRes
                                                .getProperty(DashboardConstants.DEFAULT_GADGET));

                            if (tempRes
                                    .getProperty(DashboardConstants.UNSIGNED_USER_GADGET) != null)
                                tempGadget
                                        .setUnsignedUserGadget(tempRes
                                                .getProperty(DashboardConstants.UNSIGNED_USER_GADGET));

                            if (tempRes.getProperty(DashboardConstants.GADGET_THUMB_URL) != null) {
                                tempGadget.setThumbUrl(tempRes.getProperty(DashboardConstants.GADGET_THUMB_URL));
                            }

                            tempGadget.setGadgetPath(tempRes.getPath());

                            tempGadget.setRating(registry.getAverageRating(tempRes
                                    .getPath()));

                            gadgetList[j] = tempGadget;
                            j++;
                        }
                    }
                }

            }

            return gadgetList;

        } catch (Exception e) {
            log.error("Backend server error : Could not return the gadget list : ", e);
            return null;
        }

    }

    // Store Gadget Thumbnail as a registry resource
    private String addGadgetThumbToRegistry(DataHandler gadgetThumb, String thumbMediaType,
                                            String gadgetName) throws GadgetRepoException {
        try {
            Registry registry = getConfigSystemRegistry();
            String regGadgetThumbPath = DashboardConstants.GS_REGISTRY_ROOT +
                    DashboardConstants.GADGET_PATH + gadgetName +
                    DashboardConstants.GADGET_THUMB_PATH +
                    gadgetName + DashboardConstants.GADGET_THUMB;

            // Create a new registry resource for the gadget thumbnail
            Resource thumb = registry.newResource();
            thumb.setContentStream(gadgetThumb.getInputStream());
            thumb.setMediaType(thumbMediaType);

            // Store gadget thumbnail as a registry resource
            registry.put(regGadgetThumbPath, thumb);

            String thumbPath = "/registry/resource" + RegistryConstants.CONFIG_REGISTRY_BASE_PATH +
                    regGadgetThumbPath;

            return thumbPath;

        } catch (Exception e) {
            log.error("Backend server error : Could not add gadget thumbnail to registry : ", e);
            return null;
        }
    }

    protected Registry getConfigSystemRegistry() {
        try {
            return  GadgetRepoContext.getRegistryService()
                    .getConfigSystemRegistry(CarbonContext.getCurrentContext().getTenantId());
        } catch (RegistryException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

}
