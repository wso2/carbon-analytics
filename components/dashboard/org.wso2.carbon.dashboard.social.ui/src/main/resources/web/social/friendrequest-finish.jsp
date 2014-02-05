<%--<!--
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
 -->--%>
<%@ page import="org.wso2.carbon.registry.social.api.people.relationship.RelationshipManager" %>
<%@ page import="org.wso2.carbon.registry.social.impl.people.relationship.RelationshipManagerImpl" %>
<%@page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.util.CharacterEncoder" %>
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
    String owner = CharacterEncoder.getSafeText((String) request.getParameter("owner"));
    String status = CharacterEncoder.getSafeText((String) request.getParameter("requestStatus"));
    RelationshipManager relationshipManager = null;
    String forwardTo = null;
    String BUNDLE = "org.wso2.carbon.dashboard.social.ui.resource.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
    String message = null;
    boolean result = false;
    try {

        relationshipManager = new RelationshipManagerImpl();
        if (status.equals("add")) {
            result = relationshipManager.requestRelationship(loggedUser, owner);
            if (result) {
                message = resourceBundle.getString("friend.request.sent");
            } else {
                message = resourceBundle.getString("error.while.sending.friend.request");
            }
        } else if (status.equals("remove")) {
            result = relationshipManager.removeRelationship(loggedUser, owner);
            if (result) {
                message = resourceBundle.getString("removed.from.friends");
            } else {
                message = resourceBundle.getString("error.while.removing.friend");
            }
        } else if (status.equals("accept")) {
            //accept friend request
            result = relationshipManager.acceptRelationshipRequest(loggedUser, owner);
            if (result) {
                message = resourceBundle.getString("friend.request.accepted");
            } else {
                message = resourceBundle.getString("error.while.accepting.friend.request");
            }
        } else if (status.equals("ignore")) {
            //ignore friend request
            result = relationshipManager.ignoreRelationship(loggedUser, owner);
            if (result) {
                message = resourceBundle.getString("friend.request.ignored");
            } else {
                message = resourceBundle.getString("error.while.ignoring.friend.request");
            }

        }
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.INFO, request);   //TODO: Display alert
        forwardTo = "showprofile.jsp?owner=" + owner;
        response.sendRedirect(forwardTo);
    } catch (Exception e) {

        CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request);    //TODO: Display alert
        forwardTo = "../admin/error.jsp";
    }
%>
<script type="text/javascript">
    location.href = "<%=forwardTo%>";
</script>