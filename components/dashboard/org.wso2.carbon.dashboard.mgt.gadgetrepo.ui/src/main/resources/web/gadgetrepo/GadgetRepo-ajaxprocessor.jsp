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
<%@page
        import="org.wso2.carbon.dashboard.mgt.gadgetrepo.ui.GadgetRepoServiceClient" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page
        import="org.wso2.carbon.dashboard.mgt.gadgetrepo.stub.types.carbon.Comment" %>
<%
    String loggeduser = (String) request.getSession().getAttribute(
            "logged-user");

    if (loggeduser == null) {
        out.print("timeOut");
        return;
    }

    String backendServerURL = CarbonUIUtil.getServerURL(config
            .getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext) config
            .getServletContext().getAttribute(
                    CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session
            .getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    GadgetRepoServiceClient gadgetRepoServiceClient = new GadgetRepoServiceClient(cookie,
            backendServerURL,
            configContext,
            request.getLocale());

    // Getting the function name
    String funcName = request.getParameter("func");


    if ("sessionValid".equals(funcName)) {
        out.print(gadgetRepoServiceClient.isSessionValid());

    } else if ("deleteGadget".equals(funcName)) {
        String gadgetPath = request.getParameter("gadgetPath");
        try {
            gadgetRepoServiceClient.deleteGadget(gadgetPath);
            out.print("true");
        } catch (Exception e) {
            out.print("Could not delete the gadget");
        }
    } else if ("addComment".equals(funcName)) {
        String gadgetPath = request.getParameter("gadgetPath");
        String commentText = request.getParameter("commentText");
        String user = request.getParameter("user");
        String tabId = request.getParameter("tab");
        String gadgetGroup = request.getParameter("grp");
        String commentPath = "gadgetrepo/gadget-page.jsp?gadgetPath=" + gadgetPath + "&tab=" + tabId + "&grp=" + gadgetGroup;
        Comment newComment = new Comment();

        try {
            newComment.setCommentText(commentText);
            newComment.setAuthorUserName(user);
            newComment.setCommentPath(commentPath);
            gadgetRepoServiceClient.addCommentForGadget(gadgetPath,
                    newComment);

            out.print("true");
        } catch (Exception e) {
            out.print("Could not add the comment");
        }

    } else if ("rateGadget".equals(funcName)) {
        String gadgetPath = request.getParameter("gadgetPath");
        String rating = request.getParameter("rating");
        String tabId = request.getParameter("tab");
        String gadgetGroup = request.getParameter("grp");
        try {
            gadgetRepoServiceClient.addRatingForGadget(gadgetPath,
                    Integer.parseInt(rating), tabId, gadgetGroup);
            out.print("true");
        } catch (Exception e) {
            out.print("Could not make a rating");
        }

    } else if ("deleteComment".equals(funcName)) {
        String commentId = request.getParameter("commentId");

        try {
            gadgetRepoServiceClient.deleteComment(commentId);
            out.print("true");
        } catch (Exception e) {
            out.print("Could not delete comment");
        }

    } else if ("makeDef".equals(funcName)) {
        String path = request.getParameter("gadgetPath");
        Boolean isMakeDefault = Boolean.parseBoolean(request.getParameter("isMakeDefault"));
        try {
            boolean created = gadgetRepoServiceClient.makeGadgetDefault(path, isMakeDefault);
            if (created) {
                out.print("true");
            } else {
                out.print("Could not make the gadget a default : operation failed");
            }
        } catch (Exception e) {
            out.print("Could not make teh gadget a default : exception occoured");
        }

    } else if ("unsigned".equals(funcName)) {
        String path = request.getParameter("gadgetPath");
        Boolean isUnsigned = Boolean.parseBoolean(request.getParameter("isUnsigned"));
        try {
            boolean created = gadgetRepoServiceClient.makeUnsignedUserGadget(path, isUnsigned);
            if (created) {
                out.print("true");
            } else {
                out.print("Could not make the gadget a default : operation failed");
            }
        } catch (Exception e) {
            out.print("Could not make teh gadget a default : exception occoured");
        }

    } else if ("delimage".equals(funcName)) {
        String path = request.getParameter("gadgetPath");
        try {
            boolean imageDeleted = gadgetRepoServiceClient.deleteGadgetImage(path);
            if (imageDeleted) {
                out.print("true");
            } else {
                out.print("Could not delete the gadget image");
            }
        } catch (Exception e) {
            out.print("Could not delete the gadget image");
        }

    }
%>