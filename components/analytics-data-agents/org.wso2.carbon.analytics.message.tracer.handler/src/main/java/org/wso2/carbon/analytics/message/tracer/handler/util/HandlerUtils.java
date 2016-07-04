/**
 * Copyright (c) 2005 - 2013, WSO2 Inc. (http://www.wso2.com) All Rights Reserved.
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
package org.wso2.carbon.analytics.message.tracer.handler.util;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.util.SecurityManager;
import org.w3c.dom.Document;
import org.wso2.carbon.analytics.message.tracer.handler.data.TracingInfo;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.security.SecureRandom;
import java.util.Map;

public class HandlerUtils {

    private static final Log LOG = LogFactory.getLog(HandlerUtils.class);

    private static final int ENTITY_EXPANSION_LIMIT = 0;

    private HandlerUtils() {
    }

    public static String getUniqueId() {
        SecureRandom secRandom = new SecureRandom();
        return String.valueOf(System.nanoTime()) + Math.round(secRandom.nextFloat() * 123456789);
    }

    public static void logTracingInfo(TracingInfo tracingInfo) {
        LOG.info("Massage Info: Transaction id=" + tracingInfo.getActivityId() +
                 "  Message direction=" + tracingInfo.getMessageDirection() +
                 "  Server name=" + tracingInfo.getHost() +
                 "  Timestamp=" + tracingInfo.getTimestamp() +
                 "  Service name=" + tracingInfo.getServiceName() +
                 "  Operation Name=" + tracingInfo.getOperationName());
    }

    public static Document convertToDocument(File file) throws Exception {
        DocumentBuilderFactory fac = getSecuredDocumentBuilder();
        //fac.setNamespaceAware(true);
        try {
            return fac.newDocumentBuilder().parse(file);
        } catch (Exception e) {
            throw new Exception("Error in creating an XML document from file: " +
                    e.getMessage(), e);
        }
    }

    private static DocumentBuilderFactory getSecuredDocumentBuilder() {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setXIncludeAware(false);
        dbf.setExpandEntityReferences(false);
        try {
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            dbf.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE, false);
            dbf.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE, false);
            dbf.setFeature(Constants.XERCES_FEATURE_PREFIX + Constants.LOAD_EXTERNAL_DTD_FEATURE, false);
            dbf.setFeature(Constants.XERCES_FEATURE_PREFIX + Constants.DISALLOW_DOCTYPE_DECL_FEATURE, true);
        } catch (ParserConfigurationException e) {
            LOG.error(
                    "Failed to load XML Processor Feature " + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE + " or " +
                            Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE + " or " + Constants.LOAD_EXTERNAL_DTD_FEATURE);
        }

        SecurityManager securityManager = new SecurityManager();
        securityManager.setEntityExpansionLimit(ENTITY_EXPANSION_LIMIT);
        dbf.setAttribute(Constants.XERCES_PROPERTY_PREFIX + Constants.SECURITY_MANAGER_PROPERTY, securityManager);

        return dbf;
    }

    public static String getUserNameIN(MessageContext messageContext) {
        //getting username from messageContext
        String userName = "";
        if(messageContext == null) {
            return userName;
        }
        HttpServletRequest request = (HttpServletRequest) messageContext.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
        HttpSession session;
        if (request != null && (session = request.getSession(false)) != null) {
            String tenantDomain = (String) session.getAttribute(MultitenantConstants.TENANT_DOMAIN);
            userName = (String) session.getAttribute(ServerConstants.USER_LOGGED_IN);
            if(userName != null && tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(tenantDomain)){
                userName = userName + tenantDomain;
            }
        }
        if(userName != null && !userName.isEmpty()) {
            return userName;
        } else {
            Object transportHeaders = messageContext.getProperty(MessageContext.TRANSPORT_HEADERS);
            if(transportHeaders != null) {
                String basicAuthHeader = (String)((Map)transportHeaders).get("Authorization");
                if(basicAuthHeader != null && !basicAuthHeader.isEmpty()) {

                    String basicAuthHeaderValue = new String(Base64.decodeBase64(basicAuthHeader.replace("Basic", "").trim().getBytes()));
                    String[] basicAuth = basicAuthHeaderValue.split(":");
                    if(basicAuth.length >= 2) {
                        userName = basicAuth[0];
                        return userName;
                    }
                }
            }
        }
        if(MessageTracerConstants.AUTHENTICATION_ADMIN.equalsIgnoreCase(messageContext.getAxisService().getName()) &&
           MessageTracerConstants.MESSAGE_DIRECTION.equalsIgnoreCase(messageContext.getAxisMessage().getDirection())
           && MessageTracerConstants.AUTHENTICATION_OPERATION.equalsIgnoreCase(messageContext.getAxisOperation().getName().getLocalPart())) {
            try {
                OMElement responsePayload = messageContext.getEnvelope().getBody().getFirstElement();
                if(responsePayload != null) {
                    userName = responsePayload.getFirstChildWithName(new QName(responsePayload.getNamespace().getNamespaceURI()
                            , "username", responsePayload.getNamespace().getPrefix())).getText();
                }
            }catch (Exception e) {
                LOG.error("Error while trying to get the user name form authentication request", e);
            }
            return userName;
        }
        //returning empty value if unable to fetch the username
        return "";
    }

    public static String getUserNameOUT(MessageContext messageContext) {
        //getting username from messageContext
        String userName = "";
        if(messageContext == null) {
            return userName;
        }
        userName = CarbonContext.getThreadLocalCarbonContext().getUsername();
        if(userName != null && !userName.isEmpty()) {
            return userName;
        } else {
            Object transportHeaders = messageContext.getProperty(MessageContext.TRANSPORT_HEADERS);
            if(transportHeaders != null) {
                String basicAuthHeader = (String)((Map)transportHeaders).get("Authorization");
                if(basicAuthHeader != null && !basicAuthHeader.isEmpty()) {
                    String basicAuthHeaderValue = new String(Base64.decodeBase64(basicAuthHeader.replace("Basic", "").trim().getBytes()));
                    String[] basicAuth = basicAuthHeaderValue.split(":");
                    if(basicAuth.length >= 2) {
                        userName = basicAuth[0];
                        return userName;
                    }
                }
            }
        }
        //returning empty value if unable to fetch the username
        return "";
    }
}
