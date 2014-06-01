package org.wso2.carbon.databridge.restapi.jaxrs;

import org.osgi.service.http.HttpContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;

/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class RestAPISecureContext implements HttpContext {

    @Override
    public boolean handleSecurity(HttpServletRequest request, HttpServletResponse response) throws IOException {

//
//        if (RESTUtils.isAuthenticated(request)) {
//            return true;
//        }
//        if (!request.getScheme().equals("https")) {
//            response.sendError(HttpServletResponse.SC_FORBIDDEN);
//            return false;
//        }
//
//        if (request.getHeader("Authorization") == null) {
//            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
//            return false;
//        }
//        if (RESTUtils.authenticate(request)) {
//            return true;
//        } else {
//            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
//            return false;
//        }
        return false;
    }

    @Override
    public URL getResource(String s) {
        return null;
    }

    @Override
    public String getMimeType(String s) {
        return null;
    }


}
