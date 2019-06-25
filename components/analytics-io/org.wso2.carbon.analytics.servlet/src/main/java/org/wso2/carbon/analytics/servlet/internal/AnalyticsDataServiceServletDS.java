/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.analytics.servlet.internal;

import com.hazelcast.core.HazelcastInstance;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.core.SecureAnalyticsDataService;
import org.wso2.carbon.analytics.io.commons.AnalyticsAPIConstants;
import org.wso2.carbon.analytics.servlet.*;
import org.wso2.carbon.identity.authentication.AuthenticationService;
import org.wso2.carbon.user.core.service.RealmService;
import javax.servlet.ServletException;
import java.util.Hashtable;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
         name = "analytics.thrift.component", 
         immediate = true)
public class AnalyticsDataServiceServletDS {

    private static final Log log = LogFactory.getLog(AnalyticsDataServiceServletDS.class);

    @Activate
    protected void activate(ComponentContext context) {
        ServiceHolder.setAuthenticator(new AnalyticsAPIAuthenticator());
        try {
            ServiceHolder.getHttpService().registerServlet(AnalyticsAPIConstants.MANAGEMENT_SERVICE_URI, new AnalyticsManagementProcessor(), new Hashtable<>(), ServiceHolder.getHttpService().createDefaultHttpContext());
            ServiceHolder.getHttpService().registerServlet(AnalyticsAPIConstants.TABLE_PROCESSOR_SERVICE_URI, new AnalyticsTableProcessor(), new Hashtable<>(), ServiceHolder.getHttpService().createDefaultHttpContext());
            ServiceHolder.getHttpService().registerServlet(AnalyticsAPIConstants.SCHEMA_PROCESSOR_SERVICE_URI, new AnalyticsTableSchemaProcessor(), new Hashtable<>(), ServiceHolder.getHttpService().createDefaultHttpContext());
            ServiceHolder.getHttpService().registerServlet(AnalyticsAPIConstants.ANALYTICS_SERVICE_PROCESSOR_URI, new AnalyticsServiceProcessor(), new Hashtable<>(), ServiceHolder.getHttpService().createDefaultHttpContext());
            ServiceHolder.getHttpService().registerServlet(AnalyticsAPIConstants.SEARCH_PROCESSOR_SERVICE_URI, new AnalyticsSearchProcessor(), new Hashtable<>(), ServiceHolder.getHttpService().createDefaultHttpContext());
            ServiceHolder.getHttpService().registerServlet(AnalyticsAPIConstants.ANALYTIC_RECORD_READ_PROCESSOR_SERVICE_URI, new AnalyticsRecordReadProcessor(), new Hashtable<>(), ServiceHolder.getHttpService().createDefaultHttpContext());
            ServiceHolder.getHttpService().registerServlet(AnalyticsAPIConstants.RECORD_PROCESSOR_SERVICE_URI, new AnalyticsRecordProcessor(), new Hashtable<>(), ServiceHolder.getHttpService().createDefaultHttpContext());
            ServiceHolder.getHttpService().registerServlet(AnalyticsAPIConstants.INDEX_PROCESSOR_SERVICE_URI, new AnalyticsIndexProcessor(), new Hashtable<>(), ServiceHolder.getHttpService().createDefaultHttpContext());
            ServiceHolder.getHttpService().registerServlet(AnalyticsAPIConstants.ANALYTIC_RECORD_STORE_PROCESSOR_SERVICE_URI, new AnalyticsRecordStoreProcessor(), new Hashtable<>(), ServiceHolder.getHttpService().createDefaultHttpContext());
        } catch (ServletException | NamespaceException e) {
            log.error("Error while registering the servlet. " + e.getMessage(), e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
    }

    @Reference(
             name = "org.wso2.carbon.identity.authentication.internal.AuthenticationServiceComponent", 
             service = org.wso2.carbon.identity.authentication.AuthenticationService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetAuthenticationService")
    protected void setAuthenticationService(AuthenticationService authenticationService) {
        ServiceHolder.setAuthenticationService(authenticationService);
    }

    protected void unsetAuthenticationService(AuthenticationService authenticationService) {
        ServiceHolder.setAuthenticationService(null);
    }

    @Reference(
             name = "analytics.component", 
             service = AnalyticsDataService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetAnalyticsDataService")
    protected void setAnalyticsDataService(AnalyticsDataService analyticsDataService) {
        ServiceHolder.setAnalyticsDataService(analyticsDataService);
    }

    protected void unsetAnalyticsDataService(AnalyticsDataService analyticsDataService) {
        ServiceHolder.setAnalyticsDataService(null);
    }

    @Reference(
             name = "http.service", 
             service = org.osgi.service.http.HttpService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetHttpService")
    protected void setHttpService(HttpService httpService) {
        ServiceHolder.setHttpService(httpService);
    }

    protected void unsetHttpService(HttpService httpService) {
        ServiceHolder.setHttpService(null);
    }

    @Reference(
             name = "hazelcast.instance.service", 
             service = com.hazelcast.core.HazelcastInstance.class, 
             cardinality = ReferenceCardinality.OPTIONAL, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetHazelcastInstance")
    protected void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        ServiceHolder.setHazelcastInstance(hazelcastInstance);
    }

    protected void unsetHazelcastInstance(HazelcastInstance hazelcastInstance) {
        ServiceHolder.setHazelcastInstance(null);
    }

    @Reference(
             name = "user.realmservice.default", 
             service = org.wso2.carbon.user.core.service.RealmService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {
        ServiceHolder.setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        ServiceHolder.setRealmService(null);
    }

    @Reference(
             name = "analytics.secure.component", 
             service = SecureAnalyticsDataService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetSecureAnalyticsDataService")
    protected void setSecureAnalyticsDataService(SecureAnalyticsDataService secureAnalyticsDataService) {
        ServiceHolder.setSecureAnalyticsDataService(secureAnalyticsDataService);
    }

    protected void unsetSecureAnalyticsDataService(SecureAnalyticsDataService secureAnalyticsDataService) {
        ServiceHolder.setSecureAnalyticsDataService(null);
    }
}

