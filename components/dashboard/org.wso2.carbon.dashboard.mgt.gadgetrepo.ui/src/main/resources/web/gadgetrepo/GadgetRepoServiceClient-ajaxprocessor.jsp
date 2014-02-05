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
<%@page import="org.apache.axiom.om.util.Base64"%><%@page
	import="org.wso2.carbon.dashboard.mgt.gadgetrepo.ui.GadgetRepoServiceClient"%>
<%@ page import="org.wso2.carbon.CarbonConstants"%>
<%@ page import="org.apache.axis2.context.ConfigurationContext"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<%@ page import="org.wso2.carbon.utils.ServerConstants"%>
<%@ page import="org.apache.commons.fileupload.servlet.*"%>
<%@ page import="org.apache.commons.fileupload.FileItem"%>
<%@ page import="org.apache.commons.fileupload.*"%>
<%@ page import="org.apache.commons.fileupload.disk.*"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.Iterator"%>
<%
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

	String gName = null;
	String gUrl = null;
	String gDesc = null;
	String gScreen = null;
	String gadgetPath = null;
	String contentType = null;
	long itemSizeInBytes = 0;

	if (ServletFileUpload.isMultipartContent(request)) {
		if ("mod".equals(request.getParameter("mode"))) {
			gadgetPath = request.getParameter("gadgetPath");
			
			FileItemFactory factory = new DiskFileItemFactory();

			ServletFileUpload servletFileUpload = new ServletFileUpload(
					factory);
			List fileItemsList = servletFileUpload
					.parseRequest(request);

			String optionalFileName = "";
			FileItem fileItem = null;

			Iterator it = fileItemsList.iterator();

			while (it.hasNext()) {
				FileItem item = (FileItem) it.next();
				if (item.isFormField()) {
					if (item.getFieldName().equals("gadgetName"))
						gName = item.getString();
					if (item.getFieldName().equals("gadgetUrl"))
						gUrl = item.getString();
					if (item.getFieldName().equals("gadgetDesc"))
						gDesc = item.getString();
				} else {
					contentType = item.getContentType();
					itemSizeInBytes = item.getSize();
					contentType = item.getContentType();
					gScreen = Base64.encode(item.get());
				}

			}
			if (gName == null || gUrl == null) {
				out.print("no_data");
			} else if ((itemSizeInBytes > 0)
					&& !contentType.contains("image")) {
				out.print("not_image");
			} else {
				Boolean created = gadgetRepoServiceClient
						.modifyGadget(gadgetPath, gName, gUrl, gDesc, gScreen, contentType);
				if (created == true) {
					out.print("suc");
				} else if(created == false) {
					out.print("failed");
				} else if(created == null) {
					out.print("exe");
				}
			}
		} else if ("add".equals(request.getParameter("mode"))){

			FileItemFactory factory = new DiskFileItemFactory();

			ServletFileUpload servletFileUpload = new ServletFileUpload(
					factory);
			List fileItemsList = servletFileUpload
					.parseRequest(request);

			String optionalFileName = "";
			FileItem fileItem = null;

			Iterator it = fileItemsList.iterator();

			while (it.hasNext()) {
				FileItem item = (FileItem) it.next();
				if (item.isFormField()) {
					if (item.getFieldName().equals("gadgetName"))
						gName = item.getString();
					if (item.getFieldName().equals("gadgetUrl"))
						gUrl = item.getString();
					if (item.getFieldName().equals("gadgetDesc"))
						gDesc = item.getString();
				} else {
					contentType = item.getContentType();
					itemSizeInBytes = item.getSize();
					contentType = item.getContentType();
					gScreen = Base64.encode(item.get());
				}

			}
			if (gName == null || gUrl == null) {
				out.print("no_data");
			} else if ((itemSizeInBytes > 0)
					&& !contentType.contains("image")) {
				out.print("not_image");
			} else {
				Boolean created = gadgetRepoServiceClient
						.addGadgetToRepo(gName, gUrl, gDesc, gScreen, contentType);
				if (created == true) {
					out.print("suc");
				} else if(created == false) {
					out.print("failed");
				} else if(created == null) {
					out.print("exe");
				}
			}
		}
	} else {
		//This will never happen : the form is multipart
		out.print("Unexpected error : Invalid form type");
	}
%>