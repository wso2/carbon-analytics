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

package org.wso2.carbon.dashboard.mgt.gadgetrepo.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.dashboard.mgt.gadgetrepo.stub.types.carbon.Comment;
import org.wso2.carbon.dashboard.mgt.gadgetrepo.stub.types.carbon.Gadget;
import org.wso2.carbon.dashboard.mgt.gadgetrepo.stub.GadgetRepoServiceStub;

import javax.activation.DataHandler;
import java.rmi.RemoteException;
import java.util.Locale;

public class GadgetRepoServiceClient {
    private static final Log log = LogFactory
            .getLog(GadgetRepoServiceClient.class);

    GadgetRepoServiceStub stub;

    public GadgetRepoServiceClient(String cookie, String backendServerURL,
                                   ConfigurationContext configCtx, Locale locale) throws AxisFault {

        String serviceURL = backendServerURL + "GadgetRepoService";
        stub = new GadgetRepoServiceStub(configCtx, serviceURL);

        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(
                org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                cookie);
    }

    public Boolean isSessionValid() {
        try {
            return stub.isSessionValid();
        } catch (RemoteException e) {
            log.error(e);
            return false;
        }
    }

    /**
     * Client stub to add a gadget to the repository
     */
    public Boolean addGadgetToRepo(String gName, String gUrl, String gDesc,
                                   String gScreen, String screenMediaType) {
        try {
            return stub.addGadgetToRepo(gName, gUrl, gDesc, gScreen, screenMediaType);
        } catch (RemoteException e) {
            String errorMsg = "Error from UI (client side) : Could not add the gadget to the repository";
            log.error(new GadgetRepoUiException(errorMsg, e));
            return null;
        }

    }

    /**
     * Client method to get gadgets as an array
     * this method is not in use for current functionality
     * the method with pagination enabler is set to be used
     */
    public Gadget[] getGadgetData() {

        try {
/*            org.wso2.carbon.dashboard.mgt.gadgetrepo.common.Gadget[] result = new org.wso2.carbon.dashboard.mgt.gadgetrepo.common.Gadget[stub
                    .getGadgetData().length];

            Gadget[] original = stub.getGadgetData();

            for (int i = 0; i < original.length; i++) {
                if (original[i] != null) {
                    result[i] = new org.wso2.carbon.dashboard.mgt.gadgetrepo.common.Gadget();
                    result[i].setGadgetName(original[i].getGadgetName());
                    result[i].setGadgetUrl(original[i].getGadgetUrl());
                    result[i].setGadgetDesc(original[i].getGadgetDesc());
                    result[i].setUserCount(original[i].getUserCount());
                    result[i].setGadgetScreenBase64(original[i]
                            .getGadgetScreenBase64());
                    result[i].setGadgetPath(original[i].getGadgetPath());
                    result[i].setDefaultGadget(original[i].getDefaultGadget());
                    result[i].setUnsignedUserGadget(original[i].getUnsignedUserGadget());
                    result[i].setRating(original[i].getRating());
                }*/
            //  }
            //  return result;
            return stub.getGadgetData();
        } catch (RemoteException e) {
            log.error(e.getMessage(), e);
            return null;
        }

    }

    /**
     * Client method to add a gadget to a user dashboard
     */
    public Boolean addGadget(String userId, String tabId, String url,
                             String dashboardName, String gadgetGroup, String gadgetPath) {
        try {
            return stub.addGadget(userId, tabId, url, dashboardName, gadgetGroup, gadgetPath);
        } catch (Exception e) {
            log.error(e);
        }

        return false;
    }

    public Boolean isReadOnlyMode(String userId) {
        try {
            return stub.isReadOnlyMode(userId);
        } catch (Exception e) {
            log.error(e);
        }

        return true;
    }

    /**
     * Client stub to delete a gadget
     */
    public Boolean deleteGadget(String gadgetPath) {
        try {
            return stub.deleteGadget(gadgetPath);
        } catch (Exception e) {
            log.error(e);
            return false;
        }

    }

    /**
     * Client stub to get a gadget from the service
     */
    public Gadget getGadget(
            String gadgetPath) {
        try {
/*            org.wso2.carbon.dashboard.mgt.gadgetrepo.common.Gadget result = new org.wso2.carbon.dashboard.mgt.gadgetrepo.common.Gadget();

            Gadget original = stub.getGadget(gadgetPath);

            if (original != null) {
                result = new org.wso2.carbon.dashboard.mgt.gadgetrepo.common.Gadget();
                result.setGadgetName(original.getGadgetName());
                result.setGadgetUrl(original.getGadgetUrl());
                result.setGadgetDesc(original.getGadgetDesc());
                result.setGadgetScreenBase64(original.getGadgetScreenBase64());
                result.setGadgetPath(original.getGadgetPath());
                result.setDefaultGadget(original.getDefaultGadget());
                result.setUnsignedUserGadget(original.getUnsignedUserGadget());
                result.setRating(original.getRating());
                result.setUserCount(original.getUserCount());
            }

            return result;*/
            return stub.getGadget(gadgetPath);
        } catch (RemoteException e) {
            String errorMsg = "Error from UI (client side) : Could not load the requested gadget";
            log.error(new GadgetRepoUiException(errorMsg, e));
            return null;
        }
    }

    /**
     * Client stub to modify a gadget
     */
    public Boolean modifyGadget(String gadegtPath, String gName, String gUrl,
                                String gDesc, String gScreen, String screenMediaType) {
        try {
            return stub.modifyGadget(gadegtPath, gName, gUrl, gDesc, gScreen, screenMediaType);
        } catch (Exception e) {
            String errorMsg = "Error from UI (client side) : Could not modify the gadget";
            log.error(new GadgetRepoUiException(errorMsg, e));
            return false;
        }

    }

    /**
     * client stub to add a comment to a gadget
     */
    public Boolean addCommentForGadget(String gadgetPath,
                                       Comment comment) {
        try {
/*
            Comment com = new Comment();
            com.setAuthorUserName(comment.getAuthorUserName());
            com.setCommentText(comment.getCommentText());
            com.setCommentPath(comment.getCommentPath());
            com.setCreateTime(comment.getCreateTime());*/

            stub.addCommentForGadget(gadgetPath, comment);

            return true;
        } catch (Exception e) {
            String errorMsg = "Error from UI (client side) : Could not make a comment";
            log.error(new GadgetRepoUiException(errorMsg, e));
            return false;
        }
    }

    /**
     * Client stub method to rate a gadget
     */
    public Boolean addRatingForGadget(String gadgetPath, int rating, String tabId, String gadgetGroup) {
        try {
            stub.addRatingForGadget(gadgetPath, rating, tabId, gadgetGroup);
            return true;
        } catch (Exception e) {
            String errorMsg = "Error from UI (client side) : Could not make a comment";
            log.error(new GadgetRepoUiException(errorMsg, e));
            return false;
        }
    }

    /**
     * Client stub method to delete a comment
     */
    public Boolean deleteComment(String id) {
        try {
            stub.deleteComment(id);
            return true;
        } catch (Exception e) {
            String errorMsg = "Error from UI (client side) : Could not delete comment";
            log.error(new GadgetRepoUiException(errorMsg, e));
            return false;
        }
    }

    /**
     * Client stub to check the logged user is an admin
     */
    public Boolean isAdmin(String userId) {
        try {
            return stub.isAdmin(userId);
        } catch (Exception e) {
            String errorMsg = "Error from UI (client side) : isAdmin cannot checked";
            log.error(new GadgetRepoUiException(errorMsg, e));
            return false;
        }
    }

    public Boolean incrementUserCount(String gadgetPath) {
        try {
            return stub.incrementUserCount(gadgetPath);
        } catch (Exception e) {
            String errorMsg = "Error from UI (client side) : Counter could not be incremented";
            log.error(new GadgetRepoUiException(errorMsg, e));
            return false;
        }
    }

    public String getUserRating(String gadgetPath, String userId) {
        try {
            return stub.getUserRating(gadgetPath, userId);
        } catch (Exception e) {
            String errorMsg = "Error from UI (client side) : user rating could not be retrived";
            log.error(new GadgetRepoUiException(errorMsg, e));
            return null;
        }
    }

    public Boolean isExternalGadgetAdditionEnabled() {
        try {
            return stub.isExternalGadgetAdditionEnabled();
        } catch (Exception e) {
            log.error(e);
        }

        return false;
    }

    /**
     * Pagination related operations
     */
    public Integer getCollectionSize() {
        try {
            if (stub.getCollectionSize() < 0) {
                return null;
            } else {
                return stub.getCollectionSize();
            }

        } catch (RemoteException e) {
            return null;
        }
    }

    public int getGadgetsPerPage() {
        return 10;
    }

    public Gadget[] getGadgetDataPag(
            int upper, int lower) {
        try {
            return stub.getGadgetDataPag(upper, lower);
        } catch (RemoteException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public Gadget[] getGadgetByName(
            String gadgetName) {
        try {
            Gadget[] searchGadget = stub.getGadgetByName(gadgetName);
            if (searchGadget[0] == null) {
                return null;
            } else {
                return searchGadget;
            }

        } catch (RemoteException e) {
            log.error("My error" + e.getMessage(), e);
            return null;
        }
    }


    public Comment[] getCommentSet(
            String resPath, int start, int size) {
        try {
            return stub.getCommentSet(resPath, start, size);
        } catch (Exception e) {
            String errorMsg = "Error from UI (client side) : Could not load the comment set";
            log.error(new GadgetRepoUiException(errorMsg, e));
            return null;
        }
    }

    public Integer getCommentsCount(String resPath) {
        try {
            if (stub.getCommentsCount(resPath) < 0) {
                return null;
            } else {
                return stub.getCommentsCount(resPath);
            }
        } catch (Exception e) {
            String errorMsg = "Error from UI (client side) : Could not get comment length";
            log.error(new GadgetRepoUiException(errorMsg, e));
            return null;
        }
    }

    public Boolean makeGadgetDefault(String gadgetPath, Boolean isMakeDefault) {
        try {
            return stub.makeGadgetDefault(gadgetPath, isMakeDefault);
        } catch (Exception e) {
            String errorMsg = "Error from UI (client side) : Could make the gadget a default gadget";
            log.error(new GadgetRepoUiException(errorMsg, e));
            return null;
        }
    }

    public String[] getDefaultGadgetUrlSet() {
        try {
            return stub.getDefaultGadgetUrlSet();
        } catch (Exception e) {
            return null;
        }
    }

    public Boolean makeUnsignedUserGadget(String gadgetPath,
                                          Boolean isMakeUnsigned) {
        try {
            return stub.makeUnsignedUserGadget(gadgetPath, isMakeUnsigned);
        } catch (Exception e) {
            String errorMsg = "Error from UI (client side) : Could not make the gadget a unsigned_user gadget";
            log.error(new GadgetRepoUiException(errorMsg, e));
            return null;
        }
    }

    public Boolean deleteGadgetImage(String path) {
        try {
            return stub.deleteGadgetImage(path);
        } catch (Exception e) {
            String errorMsg = "Error from UI (client side) : Could not delete the gadget image";
            log.error(new GadgetRepoUiException(errorMsg, e));
            return null;
        }
    }

    public String addResource(String path, String mediaType, String description, DataHandler content,
                              String symlinkLocation) {
        return "/path";
    }

    public boolean addGadgetEntryToRepo(String gName, String gUrl, String gDesc,
                                        DataHandler gScreen, String screenMediaType, DataHandler gadgetContent) {
        try {
            return stub.addGadgetEntryToRepo(gName, gUrl, gDesc, gScreen, screenMediaType, gadgetContent);
        } catch (Exception e) {
            String errorMsg = "Error from UI (client side) : Could not add gadget entry";
            log.error(new GadgetRepoUiException(errorMsg, e));
            return false;
        }
    }

    public Boolean modifyGadgetEntry(String gadegtPath, String gName, String gUrl,
                                     String gDesc, DataHandler gScreen, String screenMediaType, DataHandler gadgetContent) {
        try {
            return stub.modifyGadgetEntry(gadegtPath, gName, gUrl, gDesc, gScreen, screenMediaType, gadgetContent);
        } catch (Exception e) {
            String errorMsg = "Error from UI (client side) : Could not modify the gadget";
            log.error(new GadgetRepoUiException(errorMsg, e));
            return false;
        }

    }

    public Boolean userHasGadget(String gadgetPath) {
        try {
            return stub.userHasGadget(gadgetPath);
        } catch (Exception e) {
            String errorMsg = "Error from UI (client side) : Could not get the user";
            log.error(new GadgetRepoUiException(errorMsg, e));
            return false;
        }
    }
}
