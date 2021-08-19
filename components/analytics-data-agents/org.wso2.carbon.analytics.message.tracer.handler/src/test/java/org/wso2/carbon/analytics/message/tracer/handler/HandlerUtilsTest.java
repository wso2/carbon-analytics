/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.analytics.message.tracer.handler;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.context.MessageContext;

import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.transport.http.HTTPConstants;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.analytics.message.tracer.handler.util.HandlerUtils;
import org.wso2.carbon.analytics.message.tracer.handler.util.MessageTracerConstants;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.namespace.QName;
import java.util.HashMap;

public class HandlerUtilsTest {

    @Test
    public void testGetUserNameIN() {
        MessageContext messageContext = null;
        Assert.assertEquals(HandlerUtils.getUserNameIN(messageContext), "");

        messageContext = Mockito.mock(MessageContext.class);
        HttpSession session = Mockito.mock(HttpSession.class);
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

        Mockito.when(messageContext.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST)).thenReturn(request);
        Mockito.when(request.getSession(false)).thenReturn(session);
        Mockito.when(session.getAttribute(MultitenantConstants.TENANT_DOMAIN)).thenReturn("carbon.super");
        Mockito.when(session.getAttribute(ServerConstants.USER_LOGGED_IN)).thenReturn("admin");
        Assert.assertEquals(HandlerUtils.getUserNameIN(messageContext), "admin");

        Mockito.when(session.getAttribute(MultitenantConstants.TENANT_DOMAIN)).thenReturn("@wso2.com");
        Mockito.when(session.getAttribute(ServerConstants.USER_LOGGED_IN)).thenReturn("admin");
        Assert.assertEquals(HandlerUtils.getUserNameIN(messageContext), "admin@wso2.com");

        Mockito.when(session.getAttribute(MultitenantConstants.TENANT_DOMAIN)).thenReturn("carbon.super");
        Mockito.when(session.getAttribute(ServerConstants.USER_LOGGED_IN)).thenReturn("");
        HashMap transportHeaders = new HashMap();
        transportHeaders.put("Authorization", "Basic YWRtaW46YWRtaW4=");
        Mockito.when(messageContext.getProperty(MessageContext.TRANSPORT_HEADERS)).thenReturn(transportHeaders);
        Assert.assertEquals(HandlerUtils.getUserNameIN(messageContext), "admin");

        transportHeaders.put("Authorization", " ");
        Mockito.when(messageContext.getProperty(MessageContext.TRANSPORT_HEADERS)).thenReturn(transportHeaders);
        Assert.assertEquals(HandlerUtils.getUserNameIN(messageContext), "");

        transportHeaders.put("Authorization", null);
        AxisService axisService = Mockito.mock(AxisService.class);
        Mockito.when(messageContext.getAxisService()).thenReturn(axisService);
        Mockito.when(axisService.getName()).thenReturn(MessageTracerConstants.AUTHENTICATION_ADMIN);

        AxisMessage axisMessage = Mockito.mock(AxisMessage.class);
        Mockito.when(messageContext.getAxisMessage()).thenReturn(axisMessage);
        Mockito.when(axisMessage.getDirection()).thenReturn(MessageTracerConstants.MESSAGE_DIRECTION);

        AxisOperation axisOperation = Mockito.mock(AxisOperation.class);
        Mockito.when(messageContext.getAxisOperation()).thenReturn(axisOperation);
        QName qName = Mockito.mock(QName.class);
        Mockito.when(axisOperation.getName()).thenReturn(qName);
        Mockito.when(qName.getLocalPart()).thenReturn(MessageTracerConstants.AUTHENTICATION_OPERATION);

        OMElement responsePayload = Mockito.mock(OMElement.class);
        SOAPEnvelope soapEnvelope = Mockito.mock(SOAPEnvelope.class);
        Mockito.when(messageContext.getEnvelope()).thenReturn(soapEnvelope);

        SOAPBody soapBody = Mockito.mock(SOAPBody.class);
        Mockito.when(soapEnvelope.getBody()).thenReturn(soapBody);
        Mockito.when(soapBody.getFirstElement()).thenReturn(responsePayload);

        OMNamespace omNamespace = Mockito.mock(OMNamespace.class);
        Mockito.when(responsePayload.getNamespace()).thenReturn(omNamespace);

        Mockito.when(omNamespace.getNamespaceURI()).thenReturn("http://abc.com/namespace");
        Mockito.when(omNamespace.getPrefix()).thenReturn("ns1");

        QName qName1 = new QName(responsePayload.getNamespace().getNamespaceURI(), "username",
                responsePayload.getNamespace().getPrefix());
        Mockito.when(responsePayload.getFirstChildWithName(qName1)).thenReturn(responsePayload);
        Mockito.when(responsePayload.getText()).thenReturn("admin");
        Assert.assertEquals(HandlerUtils.getUserNameIN(messageContext), "admin");
    }
}
