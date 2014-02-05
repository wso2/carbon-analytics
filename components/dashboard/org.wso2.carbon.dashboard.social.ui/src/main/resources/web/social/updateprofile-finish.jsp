<!--
 ~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~    http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 -->
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.dashboard.social.common.PrivacyFieldDTO" %>
<%@ page import="org.wso2.carbon.dashboard.social.common.utils.SocialUtils" %>
<%@ page import="org.wso2.carbon.dashboard.social.ui.GadgetServerSocialDataMgtServiceClient" %>
<%@ page import="org.wso2.carbon.dashboard.ui.DashboardUiUtils" %>
<%@ page import="org.wso2.carbon.registry.social.impl.people.userprofile.PersonManagerImpl" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.ui.util.CharacterEncoder" %>
<%@ page import="org.wso2.carbon.user.core.claim.Claim" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
           prefix="carbon" %>
<%--
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
--%>

<%
    String loggedUser = CharacterEncoder.getSafeText((String) request.getSession().getAttribute("logged-user"));
    PersonManagerImpl personManager = null;
    Claim[] userClaimInfo = null;
    boolean result = false;
    String forwardTo = null;
    String BUNDLE = "org.wso2.carbon.dashboard.social.ui.resource.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
    try {
        personManager = new PersonManagerImpl();
        userClaimInfo = personManager.getOrderedUserClaimInfo();
        Map<String, String> claimValues = new HashMap<String, String>();
        String cookie = (String) session
                .getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config
                .getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config
                .getServletContext().getAttribute(
                        CarbonConstants.CONFIGURATION_CONTEXT);
        GadgetServerSocialDataMgtServiceClient socialDataMgtClient =
                new GadgetServerSocialDataMgtServiceClient
                        (cookie, backendServerURL, configContext, request.getLocale());
        PrivacyFieldDTO[] privacyInfo = new PrivacyFieldDTO[userClaimInfo.length - SocialUtils.DEFAULT_PROFILE_FIELDS_COUNT];
        int index = 0;
        String contentType = null;
        long itemSizeInBytes = 0;
        for (Claim claim : userClaimInfo) {
            String value = request.getParameter(claim.getClaimUri());
            if (claim != null && value != null) {
                claimValues.put(claim.getClaimUri(), value);
            }
            if (index >= SocialUtils.DEFAULT_PROFILE_FIELDS_COUNT) {
                String privacyValue = request.getParameter("show-" + claim.getClaimUri());
                privacyInfo[index - SocialUtils.DEFAULT_PROFILE_FIELDS_COUNT] = new PrivacyFieldDTO();
                privacyInfo[index - SocialUtils.DEFAULT_PROFILE_FIELDS_COUNT].setFieldName(claim.getClaimUri());
                privacyInfo[index - SocialUtils.DEFAULT_PROFILE_FIELDS_COUNT].setVisibilityValue(privacyValue);
            }
            index++;
        }
        personManager.saveUserClaims(loggedUser, claimValues);
        result = socialDataMgtClient.updateUserProfile(loggedUser, null, privacyInfo);
        if (result) {
            String message = resourceBundle.getString("successfully.updated.user.profile");
            CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.INFO, request);   //TODO: Display alert

            forwardTo = CarbonUIUtil.https2httpURL(DashboardUiUtils.getAdminConsoleURL(request)) + "social/userprofile.jsp";
        } else {
            forwardTo = "../admin/error.jsp";
        }
    } catch (Exception e) {
        String message = resourceBundle.getString("error.while.updating.user.profile.data");
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);//TODO: Display alert
        forwardTo = "../admin/error.jsp";
    }
%>

<script type="text/javascript">
    location.href = "<%=forwardTo%>";
</script>

