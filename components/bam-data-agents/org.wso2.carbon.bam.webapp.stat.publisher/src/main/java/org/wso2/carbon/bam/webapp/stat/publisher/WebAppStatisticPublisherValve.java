/*
* Copyright 2004,2013 The Apache Software Foundation.
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

package org.wso2.carbon.bam.webapp.stat.publisher;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.webapp.stat.publisher.conf.InternalEventingConfigData;
import org.wso2.carbon.bam.webapp.stat.publisher.data.CarbonDataHolder;
import org.wso2.carbon.bam.webapp.stat.publisher.data.WebappStatEvent;
import org.wso2.carbon.bam.webapp.stat.publisher.data.WebappStatEventData;
import org.wso2.carbon.bam.webapp.stat.publisher.publish.EventPublisher;
import org.wso2.carbon.bam.webapp.stat.publisher.publish.GlobalWebappEventPublisher;
import org.wso2.carbon.bam.webapp.stat.publisher.publish.WebappAgentUtil;
import org.wso2.carbon.bam.webapp.stat.publisher.util.BrowserInfoUtils;
import org.wso2.carbon.bam.webapp.stat.publisher.util.TenantEventConfigData;
import org.wso2.carbon.bam.webapp.stat.publisher.util.WebappStatisticsPublisherConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.servlet.ServletException;
import java.io.IOException;
import java.security.Principal;
import java.util.Map;

/**
 * This is the starting class for the web app statistics. This class is initiated through tomcat ValveBase. Classpath has to be added
 * to <WSO2 Application Server home>/repository/conf/tomcat/catelina-server.xml  as following
 * <Valve className="org.wso2.carbon.bam.webapp.stat.publisher.WebAppStatisticPublisherValve"/>
 * Purpose of this class is to initiate the publishing web application statistics. Web applications statistic will be measured in per server, per tenant
 * and per web application. We are sending only meta data and payload data, We are not using correlation data,
 * <p/>
 * Following details will be extracted  from the bam server, basis of per server, per tenant and per web application. This will be done on the hive query of the BAM Tool Box
 * 1. Request count/ Response Count / Fault Count
 * 2. Average Response Time
 * 3. Maximum Response Time
 * 4. Minimum Response Time
 * 5. Request, Response Count as a Bar Chart
 * 6. Number of User Visits
 * 7. Average Time length of User Visits
 * <p/>
 * There are few things to be done prior to enabling the statics.
 * Configuring Web App Statics Data Agent
 * 1. Go to <WSO2 Application Server home>/repository/conf/etc and open bam.xml file and enable WebappDataPublishing as follows:
 * <BamConfig>
 * <ServiceDataPublishing>disable</ServiceDataPublishing>
 * <WebappDataPublishing>enable</WebappDataPublishing>
 * </BamConfig>
 * This is disabled by default, and BAM publishing won't occur even if you proceed with the rest of the steps if not explicitly enabled.
 * <p/>
 * 2. Log in to the Application Server management console and select "Webapp Data Publishing" from the "Configure" menu.
 * The "Webapp Statistics Publisher Configuration" window opens. Fill it appropriately.
 * Stream Name     - bam_webapp_statistics
 * Version         -   1.0.0
 * Nick Name       - WebappDataAgent
 * Description     - Publish webapp statistics events
 * <p/>
 * If you need to change above values you should change them in the webapp_stats_stream_def in the tool box too. if not tool box will not work.
 * <p/>
 * Receiver URL    - tcp://{host}:{port}  ->  host- thrift host, port - thrift port
 * Username        - {Cassandra Username}
 * Password        - {Cassandra Password}
 * <p/>
 * 3. After successful deployment of your web app you have to click to enable "Enable BAM Statistics" in "Home > Manage > Applications > List > Application Dashboard
 */
public class WebAppStatisticPublisherValve extends ValveBase {

    private static Log log = LogFactory.getLog(WebAppStatisticPublisherValve.class);

    Long responseTime = 0L;
    int tenantID;

    public WebAppStatisticPublisherValve() {
        super(true);
    }

    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {
        // Get the requested url from the request to check what it consist of  and to check weather this web app has enable statistic monitoring
        Long startTime = System.currentTimeMillis();
        /*
        * Invoke the next valve. For our valve being the last configured in catalina-server.xml this will trigger the web application context requested.
        * After the completion of serving the request, response will return to here.
        */
        getNext().invoke(request, response);

        // This start time is to capture the request initiated time to measure the response time.
        responseTime = System.currentTimeMillis() - startTime;

        /*
        * Checks weather web applications statistics are enable in repository/conf/etc/bam.xml,
        * Check  weather web application has enable the statistics.
        * Checks weather requested url contains favicon.ico
        * if any of those becomes false next valve is invoked and exit from executing further.
        */

        // check weather web app has enabled for web app statistics
        boolean webappStatsEnable = Boolean.parseBoolean(request.getContext().findParameter(WebappStatisticsPublisherConstants.ENABLE_STATISTICS));
        boolean isTenantPublishingEnabled = WebappAgentUtil.getPublishingEnabled() && webappStatsEnable;
        String requestURI = request.getRequestURI();
        if((!WebappAgentUtil.isGlobalPublishingEnabled() && !isTenantPublishingEnabled) || (requestURI.contains("favicon.ico"))) {
            return;
        }

        /**
         * Checks whether request content type is css or java script.
         */

        if(!checkRequestType(request,response)){
            return;
        }

        String serverRoot = ServerConfiguration.getInstance().getFirstProperty("WebContextRoot");
        boolean isMgtConsoleRequest = ((!serverRoot.equals("/") && requestURI.startsWith(serverRoot)) || requestURI.startsWith("/carbon"));
        boolean isThemeRepoUrl = requestURI.contains("/_system/governance/repository/theme/");

        if (isMgtConsoleRequest || isThemeRepoUrl) {
            return;
        }



        try {

            //Extracting the tenant domain using the requested url.
            String tenantDomain = MultitenantUtils.getTenantDomainFromRequestURL(requestURI);
            //Extracting the configuration context. if tenant domain is null then main carbon server configuration is loaded
            ConfigurationContext currentCtx;
            if (tenantDomain != null) {
                currentCtx = getTenantConfigurationContext(tenantDomain);
            } else {
                currentCtx = CarbonDataHolder.getServerConfigContext();
            }

            //Requesting the tenant id, if this main carbon context id will be -1234
            tenantID = MultitenantUtils.getTenantId(currentCtx);
            //Extracting the tenant specific BAM configuration data.
            Map<Integer, InternalEventingConfigData> tenantSpecificEventConfig = TenantEventConfigData.getTenantSpecificEventingConfigData();
            InternalEventingConfigData eventingConfigData = tenantSpecificEventConfig.get(tenantID);

            // Validate BAM configuration data and check weather enabled from the back end. if enabled set the data to stream definition and publish.
            //if (eventingConfigData != null && eventingConfigData.isWebappStatsEnabled()) {
            if (eventingConfigData != null) {

                //Extracting the data from request and response and setting them to bean class
                WebappStatEventData webappStatEventData = prepareWebappStatEventData(request, response);

                //Time stamp of request initiated in the class
                webappStatEventData.setTimestamp(startTime);

                WebappStatEvent event = WebappAgentUtil.makeEventList(webappStatEventData, eventingConfigData);

                // publish stats to ST space
                if (WebappAgentUtil.isGlobalPublishingEnabled()) {
                    GlobalWebappEventPublisher.publish(event);
                }

                // publish stats to tenant space
                if (isTenantPublishingEnabled && eventingConfigData.isWebappStatsEnabled()) {
                    EventPublisher publisher = new EventPublisher();
                    publisher.publish(event, eventingConfigData);
                    if (log.isDebugEnabled()) {
                        log.debug("Web app stats are successfully published to bam for tenant " + tenantID);
                    }
                }

            }
        } catch (Exception e) {
            log.error("Failed to publish web app stat events to bam.", e);
        }


    }


    /*
     * This method set the statics data to webappStatEventData bean.
     */
    private WebappStatEventData prepareWebappStatEventData(Request request, Response response) {
        //todo get these extracted values from request using a utility method. please check  the comments at extractTenantDomainFromInternalUsername

        WebappStatEventData webappStatEventData = new WebappStatEventData();
        String consumerName = WebappStatisticsPublisherConstants.ANNONYMOUS_USER;
        String consumerTenantDomain = WebappStatisticsPublisherConstants.ANNONYMOUS_TENANT;
        Principal principal = request.getUserPrincipal();
        if (principal != null) {
            consumerName = principal.getName();
            try {
                consumerTenantDomain = extractTenantDomainFromInternalUsername(consumerName);
            } catch (Exception e) {
                log.error("Failed to extract tenant domain of user:" + consumerName +
                        ". tenant domain is set as anonymous.tenant only for publishing data to bam.", e);
                consumerTenantDomain = WebappStatisticsPublisherConstants.ANNONYMOUS_TENANT;
            }
        }

        webappStatEventData.setUserId(consumerName);
        webappStatEventData.setUserTenant(consumerTenantDomain);

        // set request / response size
        long requestSize = request.getCoyoteRequest().getContentLengthLong();
        webappStatEventData.setRequestSizeBytes(requestSize > 0 ? requestSize : 0);
        webappStatEventData.setResponseSizeBytes(response.getBytesWritten(true));

        String requestedURI = request.getRequestURI();

        /*
        * Checks requested url null
        */
        if (requestedURI != null) {

            requestedURI = requestedURI.trim();
            String[] requestedUriParts = requestedURI.split("/");

           /*
            * If url start with /t/, the request comes to a tenant web app
            */
            if (requestedURI.startsWith("/t/")) {
                if (requestedUriParts.length >= 4) {
                    webappStatEventData.setWebappName(requestedUriParts[4]);
                    webappStatEventData.setWebappOwnerTenant(requestedUriParts[2]);
                }
            } else {
                webappStatEventData.setWebappOwnerTenant(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
                webappStatEventData.setWebappName(requestedUriParts[1]);
            }

            /*
            * Adding the data extracted from request and response, then set to WebappStatEventData bean.
            */

            String webappServletVersion = request.getContext().getEffectiveMajorVersion() + "." +
                    request.getContext().getEffectiveMinorVersion();
            webappStatEventData.setWebappVersion(webappServletVersion);
            webappStatEventData.setResourcePath(request.getPathInfo());

            String[] browserInfo = BrowserInfoUtils.getBrowserInfo(request.getHeader(WebappStatisticsPublisherConstants.USER_AGENT));
            webappStatEventData.setBrowser(browserInfo[0]);
            webappStatEventData.setBrowserVersion(browserInfo[1]);
            webappStatEventData.setOperatingSystem("-");
            webappStatEventData.setOperatingSystemVersion("-");
            webappStatEventData.setHttpMethod(request.getMethod());
            webappStatEventData.setContentType(request.getContentType());
            webappStatEventData.setResponseContentType(response.getContentType());
            webappStatEventData.setResponseHttpStatusCode(response.getStatus());
            webappStatEventData.setRemoteAddress(getClientIpAddr(request));
            webappStatEventData.setReferer("-");
            webappStatEventData.setRemoteUser(request.getRemoteUser());
            webappStatEventData.setAuthType(request.getAuthType());
            webappStatEventData.setUserAgent(request.getHeader(WebappStatisticsPublisherConstants.USER_AGENT));
            webappStatEventData.setCountry("-");
            webappStatEventData.setResponseTime(responseTime);
            webappStatEventData.setSearchEngine("-");
            /*
            * CXF web services does not have a sesion id, because they are stateless
            */

            if (request.getSession(false) != null) {
                webappStatEventData.setSessionId(request.getSession().getId());
            }
          /*  try{
                // each and every
                request.set
                webappStatEventData.setSessionId(request.getSession().getId());
            }catch (Exception ex){

            }*/
            webappStatEventData.setWebappDisplayName(request.getContext().getDisplayName());
            webappStatEventData.setWebappContext(requestedURI);
            webappStatEventData.setWebappType("-");
            webappStatEventData.setServerAddess(request.getServerName());
            webappStatEventData.setServerName(request.getLocalName());
            webappStatEventData.setTenantId(tenantID);

            /*
            * Check the first digit of the response status code to differentiate success and fault response. 2 and 3 will be success, but 4 and 5 will be failures
            * Setting the request count (always 1), response count (1 or 0) and fault count (1 or 0)
            */
            int firstDigit = Integer.parseInt(Integer.toString(response.getStatus()).substring(0, 1));
            webappStatEventData.setRequestCount(1);
            if (firstDigit == 2 || firstDigit == 3) {
                webappStatEventData.setResponceCount(1);
                webappStatEventData.setFaultCount(0);
            } else if (firstDigit == 4 || firstDigit == 5) {
                webappStatEventData.setResponceCount(0);
                webappStatEventData.setFaultCount(1);
            }
        }
        return webappStatEventData;
    }

    private ConfigurationContext getTenantConfigurationContext(String tenantDomain) {
        return TenantAxisUtils.
                getTenantConfigurationContext(tenantDomain, CarbonDataHolder.getServerConfigContext());
    }

    // todo: Due to the additional jars that we have to copy to non osgi environment, we have duplicated code here.
    // todo: with the upcoming release, we can use utility method directly without copying all osgi bundles.
    public static String extractTenantDomainFromInternalUsername(String username) throws Exception {
        if (username == null || "".equals(username.trim()) ||
                !username.contains(WebappStatisticsPublisherConstants.UID_REPLACE_CHAR) || "".equals(username.split(WebappStatisticsPublisherConstants.UID_REPLACE_CHAR_REGEX)[1].trim())) {
            throw new Exception("Invalid username.");
        }
        return username.split(WebappStatisticsPublisherConstants.UID_REPLACE_CHAR_REGEX)[1].trim();
    }

    /*
    * Checks the remote address of the request. Server could be hiding behind a proxy or load balancer. if we get only request.getRemoteAddr() will give
    * only the proxy pr load balancer address. For that we are checking the request forwarded address in the header of the request.
    */
    public String getClientIpAddr(Request request) {
        String ip = request.getHeader(WebappStatisticsPublisherConstants.X_FORWARDED_FOR);
        if (ip == null || ip.length() == 0 || WebappStatisticsPublisherConstants.UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader(WebappStatisticsPublisherConstants.PROXY_CLIENT_IP);
        }
        if (ip == null || ip.length() == 0 || WebappStatisticsPublisherConstants.UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader(WebappStatisticsPublisherConstants.WL_PROXY_CLIENT_IP);
        }
        if (ip == null || ip.length() == 0 || WebappStatisticsPublisherConstants.UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader(WebappStatisticsPublisherConstants.HTTP_CLIENT_IP);
        }
        if (ip == null || ip.length() == 0 || WebappStatisticsPublisherConstants.UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader(WebappStatisticsPublisherConstants.HTTP_X_FORWARDED_FOR);
        }
        if (ip == null || ip.length() == 0 || WebappStatisticsPublisherConstants.UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    private boolean checkRequestType(Request request, Response response) {

        String type = response.getContentType();
            if (type != null) {
                if (type.startsWith("text/html")) {
                    return true;
                } else if (type.startsWith("text/css") || type.startsWith("application/css") ||
                        type.startsWith("image") || type.startsWith("application/javascript") ||
                        type.startsWith("text/javascript")) {
                    return false;
                }
            } else {
                type = request.getRequest().getHeader("Accept");
                if (type != null) {
                    if (type.contains("text/css") || type.contains("application/css") || type.contains("image")) {
                        return false;
                    }
                } else {
                    String lastCh = request.getRequestURI().substring(request.getRequestURI().length() - 3);
                    if (lastCh == ".js") {
                        return false;
                    }
                }
            }

        return true;
    }

}
