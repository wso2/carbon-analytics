/**
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.analytics.hive.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.hive.RegistryAccessUtilConstants;
import org.wso2.carbon.analytics.hive.ServiceHolder;
import org.wso2.carbon.analytics.hive.exception.RegistryAccessException;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.exceptions.ResourceNotFoundException;
import org.wso2.carbon.registry.core.session.UserRegistry;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegistryAccessUtil {

    private static Log log = LogFactory.getLog(RegistryAccessUtil.class);
    private static UserRegistry configRegistry;
    private static UserRegistry governanceRegistry;
    private static UserRegistry localRegistry;

    public RegistryAccessUtil() {
    }

    private static UserRegistry getConfigSystemRegistry(int tenantId)
            throws RegistryException {
        if (configRegistry == null) {
            configRegistry = ServiceHolder.getRegistryService().getConfigSystemRegistry(tenantId);
        }
        return configRegistry;
    }

    private static UserRegistry getGovernanceSystemRegistry(int tenantId)
            throws RegistryException {
        if (governanceRegistry == null) {
            governanceRegistry = ServiceHolder.getRegistryService().getGovernanceSystemRegistry(tenantId);
        }
        return governanceRegistry;
    }

    private static UserRegistry getLocalRegistry(int tenantId)
            throws RegistryException {
        if (localRegistry == null) {
            localRegistry = ServiceHolder.getRegistryService().getLocalRepository(tenantId);
        }
        return localRegistry;
    }

    public static String parseScriptForRegistryValues(String script)
            throws RegistryAccessException {
        if (log.isDebugEnabled()) {
            log.debug("Starting to parse script for bind registry values.");
        }
        if (!script.isEmpty()) {
            Pattern pattern = Pattern.compile(RegistryAccessUtilConstants.REGISTRY_KEY_PATTERN, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(script);
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            UserRegistry userRegistry = null;
            try {
                while (matcher.find()) {
                    String matchingRegistryKey = script.substring(matcher.start(), matcher.end());
                    if (log.isDebugEnabled()) {
                        log.debug((new StringBuilder()).append("Place-holder key :").append(matchingRegistryKey).toString());
                    }
                    String registryCode = matchingRegistryKey.substring(2, matchingRegistryKey.indexOf(RegistryAccessUtilConstants.COLON));
                    if (log.isDebugEnabled()) {
                        log.debug((new StringBuilder()).append("Registry code: ").append(registryCode).toString());
                    }
                    userRegistry = getUserRegistry(tenantId, registryCode);
                    String resourcePath = matchingRegistryKey.substring(
                            matchingRegistryKey.indexOf(RegistryAccessUtilConstants.COLON) + 1,
                            matchingRegistryKey.indexOf(RegistryAccessUtilConstants.CLOSE_CURLY_BRACKET));
                    if (log.isDebugEnabled()) {
                        log.debug((new StringBuilder()).append("Resource path :").append(resourcePath).toString());
                    }
                    Resource resource = userRegistry.get(resourcePath);
                    if (resource != null && RegistryAccessUtilConstants.MEDIA_TYPE_TEXT_PLAIN.equalsIgnoreCase(resource.getMediaType())) {
                        if (resource.getContent() != null && (resource.getContent() instanceof byte[])) {
                            byte res[] = (byte[]) resource.getContent();
                            script = script.replace(matchingRegistryKey, new String(res));
                            matcher = pattern.matcher(script);
                        } else {
                            log.error("Resource content null or is not a byte array ...!");
                            throw new RegistryAccessException("Resource content null or is not a byte array ...!");
                        }
                    } else {
                        log.error("Resource not available or media type is not text/plain ...!");
                        throw new RegistryAccessException("Resource not available or media type is not text/plain ...!");
                    }
                }
            } catch (ResourceNotFoundException e) {
                log.error("Resource not found", e);
                throw new RegistryAccessException((new StringBuilder()).append("Resource not found. ").append(e.getMessage()).toString(), e);
            } catch (RegistryException e) {
                log.error("Error occurred while fetching registry value ...", e);
                throw new RegistryAccessException("Error occurred while fetching registry value ...", e);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Successfully ended parse script for bind registry values.");
        }
        return script;
    }

    private static UserRegistry getUserRegistry(int tenantId, String registryCode)
            throws RegistryAccessException {
        UserRegistry userRegistry;
        try {
            if (RegistryAccessUtilConstants.REGISTRY_CONFIG.equalsIgnoreCase(registryCode)) {
                userRegistry = getConfigSystemRegistry(tenantId);
            } else if (RegistryAccessUtilConstants.REGISTRY_GOVERNANCE.equalsIgnoreCase(registryCode)) {
                userRegistry = getGovernanceSystemRegistry(tenantId);
            } else if (RegistryAccessUtilConstants.REGISTRY_LOCAL.equalsIgnoreCase(registryCode)) {
                userRegistry = getLocalRegistry(tenantId);
            } else {
                StringBuilder errorMsg = getUnrecognizedRegistryErrorMessage(registryCode);
                throw new RegistryAccessException(errorMsg.toString());
            }
        } catch (RegistryException ex) {
            log.error("Unable to get Registry...", ex);
            throw new RegistryAccessException("Unable to get Registry...", ex);
        }
        return userRegistry;
    }

    private static StringBuilder getUnrecognizedRegistryErrorMessage(String registryCode) {
        StringBuilder errorMsg = new StringBuilder();
        errorMsg.append("Unrecognizable registry type:");
        errorMsg.append(registryCode);
        errorMsg.append(". Please use one of the following syntax: ");
        errorMsg.append(RegistryAccessUtilConstants.REGISTRY_CONFIG);
        errorMsg.append(" or ");
        errorMsg.append(RegistryAccessUtilConstants.REGISTRY_GOVERNANCE);
        errorMsg.append(" or ");
        errorMsg.append(RegistryAccessUtilConstants.REGISTRY_LOCAL);
        return errorMsg;
    }
}