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
<%@ page import="org.apache.axiom.om.util.Base64" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.apache.commons.fileupload.FileItem" %>
<%@ page import="org.apache.commons.fileupload.FileItemFactory" %>
<%@ page import="org.apache.commons.fileupload.disk.DiskFileItemFactory" %>
<%@ page import="org.apache.commons.fileupload.servlet.ServletFileUpload" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.dashboard.social.ui.GadgetServerSocialDataMgtServiceClient" %>
<%@ page import="org.wso2.carbon.dashboard.social.ui.ImageScalerUtil" %>
<%@ page import="org.wso2.carbon.dashboard.ui.DashboardUiUtils" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.io.ByteArrayInputStream" %>
<%@ page import="java.io.InputStream" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ResourceBundle" %>
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
    String loggedUser = (String) request.getSession().getAttribute("logged-user");
    String BUNDLE = "org.wso2.carbon.dashboard.social.ui.resource.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
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
    String imageFileData = null;
    String contentType = null;
    long itemSizeInBytes = 0;
    String forwardTo;
    final int THUMBNAIL_HEIGHT = 250;
    final int THUMBNAIL_WIDTH = 250;
    final int QUALITY = 100;
    try {
        if (ServletFileUpload.isMultipartContent(request)) {
            FileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload servletFileUpload = new ServletFileUpload(factory);
            List fileItemsList = servletFileUpload.parseRequest(request);
            String optionalFileName = "profileImage";
            FileItem fileItem = null;
            Iterator it = fileItemsList.iterator();
            byte[] imageDataArray = null;
            while (it.hasNext()) {
                FileItem item = (FileItem) it.next();
                if (item.isFormField()) {
                    // do nothing
                } else {
                    contentType = item.getContentType();
                    itemSizeInBytes = item.getSize();
                    if (itemSizeInBytes <= 0) {
                        break;
                    }
                    imageDataArray = item.get();
                }
            }
            if ((itemSizeInBytes > 0)
                    && !contentType.contains("image")) {
                String message = resourceBundle.getString("unable.to.upload.profile.image.wrong.content.type");
                CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);

                forwardTo = "updateprofileimage.jsp";
            } else if (itemSizeInBytes > 0) {    // uploaded is an image type
                ByteArrayInputStream bais = new ByteArrayInputStream(imageDataArray);
                byte[] img = null;
                ImageScalerUtil imageScaler = new ImageScalerUtil();
                InputStream is = imageScaler.scaleImage(bais, THUMBNAIL_HEIGHT, THUMBNAIL_WIDTH, QUALITY);
                img = new byte[imageScaler.getThubmnailArraySize()];
                if (is != null) {
                    is.read(img);
                }
                is.close();
                String imageData = Base64.encode(img);
                if (itemSizeInBytes > 0) {
                    socialDataMgtClient.saveUserProfileImage(loggedUser, imageData, contentType);
                }
                forwardTo = CarbonUIUtil.https2httpURL(DashboardUiUtils.getAdminConsoleURL(request)) + "social/userprofile.jsp";
                String message = resourceBundle.getString("successfully.changed.profile.picture");
                CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.INFO, request);
            } else {
                forwardTo = CarbonUIUtil.https2httpURL(DashboardUiUtils.getAdminConsoleURL(request)) + "social/userprofile.jsp";
            }

        } else {
            //This will never happen : the form is multipart
            //out.print("Unexpected error : Invalid form type");

            forwardTo = "../admin/error.jsp";
        }
    }
    catch (Exception e) {
        String message = resourceBundle.getString("unable.to.upload.profile.image");
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request); //TODO: Display alert
        forwardTo = "../admin/error.jsp";
    }

%>
<script type="text/javascript" src="../admin/js/jquery.js"></script>
<script type="text/javascript" src="../admin/js/jquery.form.js"></script>
<script type="text/javascript" src="../dialog/js/jqueryui/jquery-ui.min.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<script type="text/javascript" src="../admin/js/WSRequest.js"></script>
<script type="text/javascript" src="../admin/js/cookies.js"></script>

<script type="text/javascript" src="global-params.js"></script>
<script type="text/javascript" src="../admin/js/dhtmlHistory.js"></script>
<script type="text/javascript" src="../admin/js/WSRequest.js"></script>
<script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../admin/js/cookies.js"></script>

<script type="text/javascript" src="../yui/build/yahoo/yahoo-min.js"></script>
<script type="text/javascript" src="javascript/gadgetrepo-server-utils.js"></script>
<script type="text/javascript" src="javascript/gadgetrepo-service-stub.js"></script>
<script type="text/javascript" src="javascript/util.js"></script>
<script type="text/javascript" src="../dialog/js/dialog.js"></script>

<script type="text/javascript">
    location.href = "<%=forwardTo%>";
</script>
