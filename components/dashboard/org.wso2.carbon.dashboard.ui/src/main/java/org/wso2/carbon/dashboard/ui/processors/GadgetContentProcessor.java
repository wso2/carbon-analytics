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

package org.wso2.carbon.dashboard.ui.processors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.dashboard.stub.resource.beans.xsd.ContentDownloadBean;
import org.wso2.carbon.dashboard.ui.GadgetResourceServiceClient;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

/**
 * The content processor which is being used by the GadgetServlet.
 */
public class GadgetContentProcessor {
    private static final Log log = LogFactory.getLog(GadgetContentProcessor.class);

    public static void getContent(HttpServletRequest request, HttpServletResponse response, ServletConfig config)
            throws Exception {

        try {
            GadgetResourceServiceClient client = new GadgetResourceServiceClient(config, request.getSession());
            String uri = request.getRequestURI();
            String path = uri.split("registry/resource")[1];
            if (path == null) {
                String msg = "Could not get the resource content. Path is not specified.";
                log.error(msg);
                response.setStatus(400);
                return;
            }

            String tenantDomain = (String)request.getAttribute("tenantDomain");
            ContentDownloadBean bean = client.getContentDownloadBean(path, tenantDomain);

            InputStream contentStream;
            if (bean.getContent() != null) {
                contentStream = bean.getContent().getInputStream();
            } else {
                String msg = "The resource content was empty.";
                log.error(msg);
                response.setStatus(204);
                return;
            }

            response.setDateHeader("Last-Modified", bean.getLastUpdatedTime().getTime().getTime());

            if (bean.getMediatype() != null && bean.getMediatype().length() > 0) {
                response.setContentType(bean.getMediatype());
            } else {
                response.setContentType("application/download");
            }

            if (contentStream != null) {

                ServletOutputStream servletOutputStream = null;
                try {
                    servletOutputStream = response.getOutputStream();

                    byte[] contentChunk = new byte[1024];
                    int byteCount;
                    while ((byteCount = contentStream.read(contentChunk)) != -1) {
                        servletOutputStream.write(contentChunk, 0, byteCount);
                    }
                    response.setContentLength(byteCount);
                    response.flushBuffer();
                    servletOutputStream.flush();

                } finally {
                    contentStream.close();

                    if (servletOutputStream != null) {
                        servletOutputStream.close();
                    }
                }
            }

        } catch (RegistryException e) {
            String msg = "Failed to get resource content. " + e.getMessage();
            log.error(msg, e);
            response.setStatus(500);
        } catch (IOException ex) {
            log.debug(ex);
            //when flash data is requested using SWFOBject this exception occours, however it does not break functionality.
            //hence ignoring the exception, since this cannot be controlled by the serverside
            //all other exceptions thrown out
            if (!ex.getClass().getName().equals("org.apache.catalina.connector.ClientAbortException")) {
                throw ex;
            }
        }
    }
}
