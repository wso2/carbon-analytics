/**
 * Copyright (c) 2005 - 2013, WSO2 Inc. (http://www.wso2.com) All Rights Reserved.
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
package org.wso2.carbon.analytics.message.tracer.handler.modules;

import org.apache.axiom.om.OMException;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.message.tracer.handler.conf.EventingConfigData;
import org.wso2.carbon.analytics.message.tracer.handler.data.TracingInfo;
import org.wso2.carbon.analytics.message.tracer.handler.publish.Publisher;
import org.wso2.carbon.analytics.message.tracer.handler.util.AgentUtil;
import org.wso2.carbon.analytics.message.tracer.handler.util.HandlerUtils;
import org.wso2.carbon.analytics.message.tracer.handler.util.MessageTracerConstants;
import org.wso2.carbon.analytics.message.tracer.handler.util.PublisherUtil;
import org.wso2.carbon.analytics.message.tracer.handler.util.TenantEventConfigData;
import org.wso2.carbon.core.util.SystemFilter;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ActivityOutHandler extends AbstractHandler {

    private static final Log LOG = LogFactory.getLog(ActivityOutHandler.class);
    private static final String HTTP_SC = "HTTP_SC";

    private Publisher publisher;

    @Override
    public void init(HandlerDescription handlerdesc) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Initiate message tracer handler");
        }
        super.init(handlerdesc);
        publisher = new Publisher();
    }

    @Override
    @SuppressWarnings("unchecked")
    public InvocationResponse invoke(MessageContext messageContext) throws AxisFault {
        MessageContext inMessageContext = null;
        int tenantID = PublisherUtil.getTenantId(messageContext);
        if (tenantID == MultitenantConstants.INVALID_TENANT_ID) {
            inMessageContext = messageContext.getOperationContext().getMessageContext(WSDL2Constants.MESSAGE_LABEL_IN);
            if (inMessageContext != null) {
                Object tId = inMessageContext.getProperty(MessageTracerConstants.TENANT_ID);
                if (tId != null) {
                    tenantID = Integer.parseInt(tId.toString());
                } else {
                    return InvocationResponse.CONTINUE;
                }
            } else {
                return InvocationResponse.CONTINUE;
            }
        }
        Map<Integer, EventingConfigData> tenantSpecificEventConfig = TenantEventConfigData.getTenantSpecificEventingConfigData();
        EventingConfigData eventingConfigData = tenantSpecificEventConfig.get(tenantID);
        if (eventingConfigData != null && eventingConfigData.isMessageTracingEnable()) {
            AxisService service = messageContext.getAxisService();
            if (service == null ||
                (SystemFilter.isFilteredOutService(service.getAxisServiceGroup()) && !AgentUtil.allowedServices(service.getName()))
                || service.isClientSide()) {
                return InvocationResponse.CONTINUE;
            } else {
                if (messageContext.getMessageID() == null) {
                    messageContext.setMessageID(HandlerUtils.getUniqueId());
                }
                //get IN Message Context from OutMessageContext to track request and response
                if (inMessageContext == null) {
                    inMessageContext = messageContext.getOperationContext().getMessageContext(WSDL2Constants.MESSAGE_LABEL_IN);
                }
                String activityID = getActivityId(messageContext, inMessageContext);
                TracingInfo tracingInfo = getTracingInfo(messageContext, activityID);
                Map<String, Object> transportHeadersMap = (Map<String, Object>) messageContext.getProperty(MessageContext
                                                                                                              .TRANSPORT_HEADERS);
                AgentUtil.setTransportHeaders(tracingInfo, transportHeadersMap);
                try {
                    if (eventingConfigData.isDumpBodyEnable()) {
                        boolean isEmptyBody = isEmptyBody(transportHeadersMap);
                        if (!isEmptyBody) {
                            setPayloadAndHeader(messageContext, tracingInfo);
                        }
                    }
                    tracingInfo.setStatus(getStatus(messageContext));
                    if (eventingConfigData.isPublishToBAMEnable()) {
                        publisher.publish(tracingInfo);
                    }
                    if (eventingConfigData.isLoggingEnable()) {
                        HandlerUtils.logTracingInfo(tracingInfo);
                    }
                } catch (OMException e) {
                    LOG.error("Unable to get SOAP details " + e.getMessage(), e);
                }
            }
        }
        return InvocationResponse.CONTINUE;
    }

    private void setPayloadAndHeader(MessageContext messageContext, TracingInfo tracingInfo) throws AxisFault {
        buildSoapMessage(messageContext);
        SOAPEnvelope soapEnvelope = messageContext.getEnvelope();
        SOAPBody body = soapEnvelope.getBody();
        if (body != null) {
            tracingInfo.setPayload(body.toString());
        }
        SOAPHeader header = soapEnvelope.getHeader();
        if (header != null) {
            tracingInfo.setHeader(header.toString());
        }
    }

    private String getStatus(MessageContext messageContext) {
        String status = MessageTracerConstants.STATUS_SUCCESS;
        if (MessageContext.OUT_FLOW == messageContext.getFLOW()) {
            Object httpSC = messageContext.getProperty(HTTP_SC);
            if (httpSC != null) {
                int statusCode = Integer.parseInt(httpSC.toString());
                if (statusCode == 200 || statusCode == 202) {
                    status = MessageTracerConstants.STATUS_SUCCESS;
                } else {
                    status = MessageTracerConstants.STATUS_FAULT;
                }
            } else {
                status = MessageTracerConstants.STATUS_SUCCESS;
            }
        } else if (MessageContext.OUT_FAULT_FLOW == messageContext.getFLOW()) {
            status = MessageTracerConstants.STATUS_FAULT;
        }
        return status;
    }

    private boolean isEmptyBody(Map<String, Object> transportHeadersMap) {
        boolean isEmptyBody = false;
        if (transportHeadersMap != null) {
            Object contentLength = transportHeadersMap.get(HTTPConstants.HEADER_CONTENT_LENGTH);
            if (contentLength != null && Integer.parseInt(contentLength.toString()) == 0) {
                isEmptyBody = true;
            }
        }
        return isEmptyBody;
    }

    @SuppressWarnings("unchecked")
    private String getActivityId(MessageContext messageContext, MessageContext inMessageContext) {
        String activityID = HandlerUtils.getUniqueId();
        //engage transport headers
        Object transportHeaders = messageContext.getProperty(MessageContext.TRANSPORT_HEADERS);
        Object inTransportHeaders = null;
        if (inMessageContext != null) {
            inTransportHeaders = inMessageContext.getProperty(MessageContext.TRANSPORT_HEADERS);
        }
        if (transportHeaders != null) {
            String aid = (String) ((Map) transportHeaders).get(MessageTracerConstants.ACTIVITY_ID);
            if (aid == null || aid.equals(MessageTracerConstants.EMPTY_STRING)) {
                if (inTransportHeaders != null) {
                    String inID = (String) ((Map) inTransportHeaders).get(MessageTracerConstants.ACTIVITY_ID);
                    if (!((inID == null) || (inID.equals(MessageTracerConstants.EMPTY_STRING)))) {
                        activityID = inID;
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("OUT using IN's AID, transport header present");
                        }
                    }
                }
                ((Map) messageContext.getProperty(MessageContext.TRANSPORT_HEADERS)).
                        put(MessageTracerConstants.ACTIVITY_ID, activityID);
            } else {
                activityID = aid;
                if (LOG.isDebugEnabled()) {
                    LOG.debug("OUT using new AID :" + aid);
                }
            }
        } else {
            if (inTransportHeaders != null) {
                String inID = (String) ((Map) inTransportHeaders).get(MessageTracerConstants.ACTIVITY_ID);
                if (!((inID == null) || (inID.equals(MessageTracerConstants.EMPTY_STRING)))) {
                    activityID = inID;
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("OUT using IN's AID, transport header absent");
                    }
                }
            }
            Map<String, String> headers = new HashMap<>(1);
            headers.put(MessageTracerConstants.ACTIVITY_ID, activityID);
            messageContext.setProperty(MessageContext.TRANSPORT_HEADERS, headers);
        }
        return activityID;
    }

    @SuppressWarnings("unchecked")
    private void buildSoapMessage(MessageContext messageContext) throws AxisFault {
        try {
            Class cls = Class.forName(MessageTracerConstants.ORG_APACHE_SYNAPSE_TRANSPORT_PASSTHRU_UTIL_RELAY_UTILS_CLASS_NAME);
            Class[] paramClasses = new Class[]{MessageContext.class, Boolean.TYPE};
            Method method = cls.getMethod(MessageTracerConstants.BUILD_MESSAGE_METHOD_NAME, paramClasses);
            method.invoke(null, messageContext, false);
        } catch (ClassNotFoundException ignore) {
            LOG.warn(ignore);
        } catch (Exception e) {
            throw new AxisFault("Error in building input message: " + e.getMessage(), e);
        }
    }

    private TracingInfo getTracingInfo(MessageContext messageContext, String activityID) {
        TracingInfo tracingInfo = new TracingInfo();
        tracingInfo.setActivityId(activityID);
        tracingInfo.setServer(AgentUtil.getServerName());
        tracingInfo.setMessageDirection(MessageTracerConstants.OUT_DIRECTION);
        tracingInfo.setUserName(HandlerUtils.getUserNameOUT(messageContext));
        tracingInfo.setHost(PublisherUtil.getHostAddress());
        tracingInfo.setServiceName(messageContext.getAxisService().getName());
        tracingInfo.setOperationName(messageContext.getAxisOperation().getName().getLocalPart());
        tracingInfo.setTimestamp(System.currentTimeMillis());
        return tracingInfo;
    }
}
