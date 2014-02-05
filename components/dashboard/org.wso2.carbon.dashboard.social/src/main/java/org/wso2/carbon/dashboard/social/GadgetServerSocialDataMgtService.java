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
package org.wso2.carbon.dashboard.social;

import org.apache.axiom.om.util.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.dashboard.social.common.PrivacyFieldDTO;
import org.wso2.carbon.dashboard.social.common.utils.SocialUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;


public class GadgetServerSocialDataMgtService extends AbstractAdmin {
    private static final Log log =
            LogFactory.getLog(GadgetServerSocialDataMgtService.class);

    public Boolean updateUserProfile(String userId, String profileName,
                                     PrivacyFieldDTO[] profileData) {
        Boolean response = false;
        if (profileName == null) {
            profileName = "default";
        }
        try {
            Registry registry = GadgetServerSocialDataMgtServiceContext.getRegistry();
            String profileDataPath;
            profileDataPath = SocialUtils.USER_DASHBOARD_REGISTRY_ROOT
                    + userId + SocialUtils.USER_PROFILE_DASHBOARD_REGISTRY_ROOT + profileName + "/";
            // create a resource for each key value and set the value as it's property
            // store the resource to registry
            Resource profileDataResource;
            String fullProfileDataPath;
            for (PrivacyFieldDTO pair : profileData) {
                fullProfileDataPath = profileDataPath + pair.getFieldName();
                try {
                    profileDataResource = registry.get(fullProfileDataPath);
                } catch (RegistryException e) {
                    profileDataResource = registry.newCollection();
                }

                profileDataResource.setProperty(SocialUtils.USER_PROFILE_FIELD_VISIBILITY, pair.getVisibilityValue());
                registry.put(fullProfileDataPath, profileDataResource);
            }

            response = true;

        } catch (Exception e) {
            log.debug("Failed to update profile for user " + userId);
        }

        return response;
    }


    public PrivacyFieldDTO[] getUserProfile(String userId, String profileName,
                                            String[] claimValues) {
        PrivacyFieldDTO[] profileData = new PrivacyFieldDTO[claimValues.length];
        try {
            if (profileName == null) {
                profileName = "default";
            }
            Registry registry = GadgetServerSocialDataMgtServiceContext.getRegistry();
            String profileDataPath;
            profileDataPath = SocialUtils.USER_DASHBOARD_REGISTRY_ROOT +
                    userId +
                    SocialUtils.USER_PROFILE_DASHBOARD_REGISTRY_ROOT +
                    profileName +
                    "/";
            String fullProfileDataPath = "";
            // read all claim url values and get the visibility values
            for (int i = 0; i < claimValues.length; i++) {
                fullProfileDataPath = profileDataPath + claimValues[i];
                Resource profileDataResource;
                // try {
                if (registry.resourceExists(fullProfileDataPath)) {
                    profileDataResource = registry.get(fullProfileDataPath);
                    profileData[i] = new PrivacyFieldDTO();
                    profileData[i].setFieldName(claimValues[i]);
                    if (profileDataResource.getProperty(SocialUtils.USER_PROFILE_FIELD_VISIBILITY) != null) {
                        profileData[i].setVisibilityValue((profileDataResource.getProperty
                                (SocialUtils.USER_PROFILE_FIELD_VISIBILITY)));
                    } else {
                        profileData[i].setVisibilityValue((SocialUtils.VISIBILITY_NONE));
                    }

                } else {
                    profileData[i] = new PrivacyFieldDTO();
                    profileData[i].setFieldName(claimValues[i]);
                    profileData[i].setVisibilityValue(SocialUtils.VISIBILITY_NONE);
                }
                //}
                //catch (RegistryException e) {
                // such resource doesn't exists. i.e: all are default values
                //  return null;
                // }

            }


        } catch (Exception e) {
            log.debug("Failed to retrieve profile for user " + userId);
            //return null;
        }
        return profileData;
    }

    public String getUserProfileImage(String userId) {
        String profileImage = null;
        try {
            Registry registry = GadgetServerSocialDataMgtServiceContext.getRegistry();
            String profileImagePath = SocialUtils.USER_ROOT + userId + SocialUtils.PROFILE_IMAGE_PATH;
            String defaultImagePath = SocialUtils.USER_ROOT + SocialUtils.PROFILE_IMAGE_NAME;
            Resource imageResource;
            if (registry.resourceExists(profileImagePath)) {
                imageResource = registry.get(profileImagePath);
                return Base64.encode((byte[]) imageResource.getContent());
            }
            /*else if (registry.resourceExists(defaultImagePath)) {
                imageResource = registry.get(defaultImagePath);
                return Base64.encode((byte[]) imageResource.getContent());
            }*/ 
            else {
                return null;
            }
        } catch (Exception e) {
            log.debug("Failed to retrieve profile image for user " + userId);
            return null;
        }
    }

    public boolean saveUserProfileImage(String userId, String profileImage, String mediaType) {
        boolean result = false;
        String profileImagePath = SocialUtils.USER_ROOT + userId + SocialUtils.PROFILE_IMAGE_PATH;
        Resource profileImageRes;
        try {
            Registry registry = GadgetServerSocialDataMgtServiceContext.getRegistry();
            if (registry.resourceExists(profileImagePath)) {
                profileImageRes = registry.get(profileImagePath);
            } else {
                profileImageRes = registry.newResource();
            }
            byte[] imgData = Base64.decode(profileImage);
            profileImageRes.setContent(imgData);
            profileImageRes.setMediaType(mediaType);
            registry.put(profileImagePath, profileImageRes);
            result = true;
        } catch (Exception e) {
            log.debug("Failed to save profile image for user " + userId);
        }

        return result;
    }

    public boolean isProfileImageExists(String userId) {
        boolean result = false;
        String profileImagePath = SocialUtils.USER_ROOT + userId + SocialUtils.PROFILE_IMAGE_PATH;
        try {
            Registry registry = GadgetServerSocialDataMgtServiceContext.getRegistry();
            if (registry.resourceExists(profileImagePath)) {
                result = true;
            }
            else{
                result=false;
            }
        } catch (Exception e) {
            log.debug("Failed to save profile image for user " + userId);
        }
        return result;
    }

}
