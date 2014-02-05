/*
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.dashboard.ui.internal;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shindig.auth.AuthenticationServletFilter;
import org.apache.shindig.gadgets.servlet.*;
import org.apache.shindig.protocol.DataServiceServlet;
import org.apache.shindig.protocol.JsonRpcServlet;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpService;
import org.wso2.carbon.dashboard.common.OAuthUtils;
import org.wso2.carbon.dashboard.common.oauth.GSOAuthModule;
import org.wso2.carbon.dashboard.ui.DashboardUiContext;
import org.wso2.carbon.dashboard.ui.servlets.MakeSoapRequestServlet;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.base.ServerConfiguration;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

/**
 * @scr.component name="org.wso2.carbon.dashboard.ui" immediate="true"
 * @scr.reference name="servlet.context.service"
 * interface="javax.servlet.ServletContext"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setServletContextService"
 * unbind="unsetServletContextService"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setConfigurationContextService"
 * unbind="unsetConfigurationContextService"
 */
public class DashboardUiServiceComponent {

    // Shindig properties
    protected static final String INJECTOR_ATTRIBUTE = "guice-injector";
    protected static final String MODULES_ATTRIBUTE = "guice-modules";

    private static HttpService httpServiceInstance;

    private static final Log log = LogFactory.getLog(DashboardUiServiceComponent.class);

    private List<ServiceRegistration> serviceRegistrations = null;

    private static ServletContext servletCtx = null;

    private static BundleContext bctx = null;


    protected void activate(ComponentContext context) {
        try {
            initShindig(servletCtx);
            bctx = context.getBundleContext();
            registerServlets(context.getBundleContext());

            log.debug("******* Dashboard UI Component bundle is activated ******* ");

        } catch (Exception e) {
            log.debug("******* Failed to activate Dashboard UI Component bundle ******* ");
        }
    }

    protected void deactivate(ComponentContext context) {
        unregisterServlets();
        log.debug("******* Dashboard UI Component bundle is deactivated ******* ");
    }

    protected void setServletContextService(ServletContext servletContext) {
        this.servletCtx = servletContext;
        //initShindig(servletContext);
    }

    protected void unsetServletContextService(ServletContext servletContext) {
        servletContext.removeAttribute(INJECTOR_ATTRIBUTE);
    }

    /**
     * Initilizes Shindig
     *
     * @param context
     */
    protected void initShindig(ServletContext context) {

        System.setProperty("shindig-host-context", getHostWithContext());

        String moduleNames =
                "org.apache.shindig.common.PropertiesModule:org.apache.shindig.gadgets.DefaultGuiceModule:org.wso2.carbon.dashboard.social.GuiceModuleImpl:org.wso2.carbon.dashboard.common.oauth.GSOAuthModule";

        List<Module> modules = Lists.newLinkedList();
        for (String moduleName : moduleNames.split(":")) {
            try {
                moduleName = moduleName.trim();
                if (moduleName.length() > 0) {
                    modules.add((Module) Class.forName(moduleName).newInstance());
                }
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                if (moduleName.contains("org.wso2.carbon.dashboard.social.GuiceModuleImpl")) {
                    try {
                        modules.add((Module) Class.forName("org.apache.shindig.social.sample.SampleModule").newInstance());
                    } catch (Exception e1) {
                        log.error(e1);
                    }
                } else {
                    throw new RuntimeException(e);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        Injector injector = null;
        try {
            injector = Guice.createInjector(Stage.PRODUCTION, modules);
            GSOAuthModule.OAuthStoreProvider provider = injector.getInstance(GSOAuthModule.OAuthStoreProvider.class);
            OAuthUtils.setOauthStoreProvider(provider);
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
        }
        context.setAttribute(INJECTOR_ATTRIBUTE, injector);
    }

    private String getHostWithContext() {
        String hostWithContext;
        try {
            String webAppContext = DashboardUiContext.getConfigContext().getContextRoot();
            if (webAppContext.equals("/")) {
                hostWithContext = System.getProperty("carbon.local.ip") + ":" + getBackendHttpPort();
            } else {
                hostWithContext = System.getProperty("carbon.local.ip") + ":" + getBackendHttpPort() + webAppContext;
            }

            return hostWithContext;

        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

    private String getBackendHttpPort() {
        String httpPort = null;
        try {
            httpPort = ServerConfiguration.getInstance().getFirstProperty("RegistryHttpPort");
            if (httpPort == null) {
                httpPort = (String) DashboardUiContext.getConfigContext().getAxisConfiguration().getTransportIn("http")
                        .getParameter("port").getValue();
            }

        } catch (Exception e) {
            log.error(e);
        }

        return httpPort;
    }

    /**
     * Registers the requires Shindig Servlets
     *
     * @param bundleContext
     */
    private void registerServlets(BundleContext bundleContext) throws Exception {
        try {

            ServiceRegistration temp = null;
            if (serviceRegistrations != null) {
                // Avoid duplicate registration
                return;
            }

            serviceRegistrations = new LinkedList<ServiceRegistration>();

            //Registering a filter for a stub servlet


            HttpServlet authServlet = new HttpServlet() {
                // the redirector filter will forward the request before this servlet is hit
                protected void doGet(HttpServletRequest request, HttpServletResponse response)
                        throws ServletException, IOException {
                }
            };

            registerServlets();

        } catch (Exception e) {
            String msg = "Failed to Register the Shindig Servlets";
            log.error(msg, e);
            throw new Exception(msg, e);
        }
    }

    private void unregisterServlets() {
        for (ServiceRegistration reg : serviceRegistrations) {
            reg.unregister();
        }
        serviceRegistrations = null;
    }

    protected void setConfigurationContextService(ConfigurationContextService configCtx) {
        DashboardUiContext.setConfigContextService(configCtx);
    }

    protected void unsetConfigurationContextService(ConfigurationContextService configCtx) {
        DashboardUiContext.setConfigContextService(null);
    }

    private void registerServlets() throws Exception {
        registerXMLtoHTMLServlet();
        registerAccelServlet();
        registerMakeRequestServlet();
        registerProxyServlet();
        // registerProxyServlet2();
        registerConcatServlet();
        registerOAuthCallbackServlet();
        registerMetadataServlet();
        registerJsServlet();
        registerRestApiServlet();
        registerJsonRpcServlet();
        registerSOAPRequestServlet();
    }

    private void registerServlet(BundleContext bundleContext, HttpServlet servlet, Dictionary servletParams) {
        ServiceRegistration temp = bundleContext
                .registerService(Servlet.class.getName(), servlet, servletParams);
        if (temp != null) {
            serviceRegistrations.add(temp);
            temp = null;
        }
    }

    private void registerXMLtoHTMLServlet() {
        GadgetRenderingServlet gadgetRenderingServlet = new GadgetRenderingServlet();
        AuthenticationServletFilter authFilter = new AuthenticationServletFilter();
        Dictionary servletMappings = new Hashtable();
        servletMappings.put("servlet-name", "xml-to-html");
        servletMappings.put("url-pattern", "/ifr");
//        servletMappings.put("associated-filter", authFilter);
        registerServlet(bctx, gadgetRenderingServlet, servletMappings);
    }

    private void registerAccelServlet() {
        HtmlAccelServlet htmlAccelServlet = new HtmlAccelServlet();
        Dictionary servletMappings = new Hashtable();
        servletMappings.put("servlet-name", "accel");
        servletMappings.put("url-pattern", "/gadgets/accel");
        registerServlet(bctx, htmlAccelServlet, servletMappings);
    }

    private void registerMakeRequestServlet() {
        MakeRequestServlet makeRequestServlet = new MakeRequestServlet();
        AuthenticationServletFilter authFilter = new AuthenticationServletFilter();
        Dictionary servletMappings = new Hashtable();
        servletMappings.put("servlet-name", "makeRequest");
        servletMappings.put("url-pattern", "/gadgets/makeRequest");
        servletMappings.put("associated-filter", authFilter);
        registerServlet(bctx, makeRequestServlet, servletMappings);
    }

    private void registerProxyServlet() {
        ProxyServlet proxyServlet = new ProxyServlet();
        Dictionary servletMappings = new Hashtable();
        servletMappings.put("servlet-name", "proxy");
        servletMappings.put("url-pattern", "/gadgets/proxy");
        registerServlet(bctx, proxyServlet, servletMappings);
    }

/* commenting out since cannot register for the same path
    private void registerProxyServlet2() {
        ProxyServlet proxyServlet = new ProxyServlet();
        Dictionary servletMappings = new Hashtable();
        servletMappings.put("servlet-name", "proxy");
        servletMappings.put("url-pattern", "/gadgets/gadgets/proxy");
        registerServlet(bctx, proxyServlet, servletMappings);
    }*/

    private void registerConcatServlet() {
        ConcatProxyServlet concatProxyServlet = new ConcatProxyServlet();
        Dictionary servletMappings = new Hashtable();
        servletMappings.put("servlet-name", "concat");
        servletMappings.put("url-pattern", "/gadgets/concat");
        registerServlet(bctx, concatProxyServlet, servletMappings);
    }

    private void registerOAuthCallbackServlet() {
        OAuthCallbackServlet oauthCallbackServlet = new OAuthCallbackServlet();
        Dictionary servletMappings = new Hashtable();
        servletMappings.put("servlet-name", "oauthCallback");
        servletMappings.put("url-pattern", "/gadgets/oauthcallback");
        registerServlet(bctx, oauthCallbackServlet, servletMappings);
    }

    private void registerMetadataServlet() {
        RpcServlet rpcServlet = new RpcServlet();
        Dictionary servletMappings = new Hashtable();
        servletMappings.put("servlet-name", "metadata");
        servletMappings.put("url-pattern", "/gadgets/metadata");
        registerServlet(bctx, rpcServlet, servletMappings);
    }

    private void registerJsServlet() {
        JsServlet jsServlet = new JsServlet();
        Dictionary servletMappings = new Hashtable();
        servletMappings.put("servlet-name", "js");
        servletMappings.put("url-pattern", "/gadgets/js");
        registerServlet(bctx, jsServlet, servletMappings);
    }

    private void registerRestApiServlet() {
        registerRestApiServlet("/rest");
        registerRestApiServlet("/gadgets/api/rest");
        registerRestApiServlet("/social/rest");
    }

    private void registerRestApiServlet(String urlPattern) {
        DataServiceServlet dataServiceServlet = new DataServiceServlet();
        AuthenticationServletFilter authFilter = new AuthenticationServletFilter();
        Dictionary servletMappings = new Hashtable();
        Dictionary<String, String> initParams = new Hashtable<String, String>();
        initParams.put("handlers", "org.apache.shindig.social.handlers");

        servletMappings.put("url-pattern", urlPattern);
        servletMappings.put("servlet-params", initParams);
        servletMappings.put("associated-filter", authFilter);
        registerServlet(bctx, dataServiceServlet, servletMappings);
    }

    private void registerJsonRpcServlet() {
        registerJsonRpcServlet("/rpc");
        registerJsonRpcServlet("/gadgets/api/rpc");
        registerJsonRpcServlet("/social/rpc");
    }

    private void registerJsonRpcServlet(String urlPattern) {
        JsonRpcServlet jsonRpcServlet = new JsonRpcServlet();
        AuthenticationServletFilter authFilter = new AuthenticationServletFilter();
        Dictionary servletMappings = new Hashtable();
        Dictionary<String, String> initParams = new Hashtable<String, String>();
        initParams.put("handlers", "org.apache.shindig.social.handlers");
        servletMappings.put("url-pattern", urlPattern);
        servletMappings.put("servlet-params", initParams);
        servletMappings.put("associated-filter", authFilter);
        registerServlet(bctx, jsonRpcServlet, servletMappings);
    }

    private void registerSOAPRequestServlet() {
        MakeSoapRequestServlet makeSoapRequestServlet = new MakeSoapRequestServlet();
        Dictionary servletMappings = new Hashtable();
        servletMappings.put("servlet-name", "soapRequest");
        servletMappings.put("url-pattern", "/gadgets/makeSOAPRequest");
        registerServlet(bctx, makeSoapRequestServlet, servletMappings);
    }

/*    private void registerSampleOAuth() {
        SampleOAuthServlet sampleOAuthServlet = new SampleOAuthServlet();
        Dictionary servletMappings = new Hashtable();
        servletMappings.put("servlet-name", "js");
        servletMappings.put("url-pattern", "/gadgets/js*//*");
        registerServlet(bctx, jsServlet, servletMappings);
    }*/
}
