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
package org.wso2.carbon.bam.webapp.stat.publisher.data;

public class WebappStatEventData {
    private String webappName;
    private String webappOwnerTenant;
    private String webappVersion;
    private String userId;
    private long timestamp;
    private String resourcePath;
    private String browser;
    private String browserVersion;
    private String operatingSystem;
    private String operatingSystemVersion;
    private String searchEngine;
    private String country;
    private String webappType;
    private String webappDisplayName;
    private String webappContext;
    private String sessionId;
    private String httpMethod;
    private String contentType;
    private String responseContentType;
    private int responseHttpStatusCode;
    private String remoteAddress;
    private String referer;
    private String remoteUser;
    private String authType;
    private String userAgent;
    private long responseTime;
    private String serverAddess;
    private String serverName;
    private int tenantId;
    private String  userTenant;
    private int requestCount;
    private int responceCount;
    private int faultCount;
    private long requestSizeBytes;
    private long responseSizeBytes;

    public int getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(int requestCount) {
        this.requestCount = requestCount;
    }

    public int getResponceCount() {
        return responceCount;
    }

    public void setResponceCount(int responceCount) {
        this.responceCount = responceCount;
    }

    public int getFaultCount() {
        return faultCount;
    }

    public void setFaultCount(int faultCount) {
        this.faultCount = faultCount;
    }

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getServerAddess() {
        return serverAddess;
    }

    public void setServerAddess(String serverAddess) {
        this.serverAddess = serverAddess;
    }

    public String getSearchEngine() {
        return searchEngine;
    }

    public void setSearchEngine(String searchEngine) {
        this.searchEngine = searchEngine;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getWebappType() {
        return webappType;
    }

    public void setWebappType(String webappType) {
        this.webappType = webappType;
    }

    public String getWebappDisplayName() {
        return webappDisplayName;
    }

    public void setWebappDisplayName(String webappDisplayName) {
        this.webappDisplayName = webappDisplayName;
    }

    public String getWebappContext() {
        return webappContext;
    }

    public void setWebappContext(String webappContext) {
        this.webappContext = webappContext;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }


    public String getWebappName() {
        return webappName;
    }

    public void setWebappName(String webappName) {
        this.webappName = webappName;
    }


    public String getWebappVersion() {
        return webappVersion;
    }

    public void setWebappVersion(String webappVersion) {
        this.webappVersion = webappVersion;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getWebappOwnerTenant() {
        return webappOwnerTenant;
    }

    public void setWebappOwnerTenant(String webappOwnerTenant) {
        this.webappOwnerTenant = webappOwnerTenant;
    }

    public String getUserTenant() {
        return userTenant;
    }

    public void setUserTenant(String userTenant) {
        this.userTenant = userTenant;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getResponseContentType() {
        return responseContentType;
    }

    public void setResponseContentType(String responseContentType) {
        this.responseContentType = responseContentType;
    }

    public int getResponseHttpStatusCode() {
        return responseHttpStatusCode;
    }

    public void setResponseHttpStatusCode(int responseHttpStatusCode) {
        this.responseHttpStatusCode = responseHttpStatusCode;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

    public String getRemoteUser() {
        return remoteUser;
    }

    public void setRemoteUser(String remoteUser) {
        this.remoteUser = remoteUser;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getBrowserVersion() {
        return browserVersion;
    }

    public void setBrowserVersion(String browserVersion) {
        this.browserVersion = browserVersion;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public String getOperatingSystemVersion() {
        return operatingSystemVersion;
    }

    public void setOperatingSystemVersion(String operatingSystemVersion) {
        this.operatingSystemVersion = operatingSystemVersion;
    }

    public long getRequestSizeBytes() {
        return requestSizeBytes;
    }

    public void setRequestSizeBytes(long requestSizeBytes) {
        this.requestSizeBytes = requestSizeBytes;
    }

    public long getResponseSizeBytes() {
        return responseSizeBytes;
    }

    public void setResponseSizeBytes(long responseSizeBytes) {
        this.responseSizeBytes = responseSizeBytes;
    }

}
