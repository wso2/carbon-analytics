/*
* Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.dashboard.social.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.dashboard.social.common.PrivacyFieldDTO;
import org.wso2.carbon.dashboard.social.stub.GadgetServerSocialDataMgtServiceStub;

import java.rmi.RemoteException;
import java.util.Locale;

public class GadgetServerSocialDataMgtServiceClient {
    private static final Log log = LogFactory
            .getLog(GadgetServerSocialDataMgtServiceClient.class);

    GadgetServerSocialDataMgtServiceStub stub;

    public GadgetServerSocialDataMgtServiceClient(String cookie, String backendServerURL,
                                                  ConfigurationContext configCtx, Locale locale)
            throws AxisFault {

        String serviceURL = backendServerURL + "GadgetServerSocialDataMgtService";
        stub = new GadgetServerSocialDataMgtServiceStub(configCtx, serviceURL);

        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(
                org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                cookie);

    }

    public Boolean updateUserProfile(String userId, String profileName,
                                     PrivacyFieldDTO[] profileData) {
        try {
            org.wso2.carbon.dashboard.social.stub.types.carbon.PrivacyFieldDTO[] profileDataArray =
                    new org.wso2.carbon.dashboard.social.stub.types.carbon.PrivacyFieldDTO[profileData.length];
            int index = 0;
            for (PrivacyFieldDTO data : profileData) {
                org.wso2.carbon.dashboard.social.stub.types.carbon.PrivacyFieldDTO obj =
                        new org.wso2.carbon.dashboard.social.stub.types.carbon.PrivacyFieldDTO();
                obj.setFieldName(data.getFieldName());
                obj.setVisibilityValue(data.getVisibilityValue());
                profileDataArray[index++] = obj;
            }
            return stub.updateUserProfile(userId, profileName, profileDataArray);
        }
        catch (RemoteException e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    public PrivacyFieldDTO[] getUserProfile(String userId, String profileName,
                                            String[] claimValues) {
        try {
            org.wso2.carbon.dashboard.social.stub.types.carbon.PrivacyFieldDTO[] result =
                    stub.getUserProfile(userId, profileName, claimValues);
            PrivacyFieldDTO[] profileData = new PrivacyFieldDTO[result.length];
            int index = 0;
            for (org.wso2.carbon.dashboard.social.stub.types.carbon.PrivacyFieldDTO data : result) {
                PrivacyFieldDTO obj = new PrivacyFieldDTO();
                obj.setFieldName(data.getFieldName());
                obj.setVisibilityValue(data.getVisibilityValue());
                profileData[index++] = obj;
            }
            return profileData;
        }
        catch (RemoteException e) {
            log.error(e.getMessage(), e);
            return new PrivacyFieldDTO[0];
        }

    }

    public String getUserProfileImage(String userId) {
        try {
            return stub.getUserProfileImage(userId);
        } catch (RemoteException e) {
            log.error(e.getMessage(), e);
            return "";
        }
    }

    public boolean saveUserProfileImage(String userId, String profileImage, String mediaType) {
        try {
            return stub.saveUserProfileImage(userId, profileImage, mediaType);
        } catch (RemoteException e) {
            log.error(e.getMessage(), e);
            return false;
        }

    }

    public boolean isProfileImageExists(String userId) {
        try {
            return stub.isProfileImageExists(userId);
        } catch (RemoteException e) {
            log.error(e.getMessage(), e);
            return false;
        }

    }

}



