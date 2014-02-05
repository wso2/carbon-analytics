package org.wso2.carbon.bam.restapi;

import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.servlet.CXFNonSpringJaxrsServlet;
import org.apache.cxf.jaxrs.utils.ResourceUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

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
public class RestAPIServlet extends CXFNonSpringJaxrsServlet{
    @Override
    protected void createServerFromApplication(String cName, ServletConfig servletConfig) throws ServletException {
        RestAPIApp app = new RestAPIApp();
        JAXRSServerFactoryBean bean = ResourceUtils.createApplication(app, true);
        bean.create();
    }
}
