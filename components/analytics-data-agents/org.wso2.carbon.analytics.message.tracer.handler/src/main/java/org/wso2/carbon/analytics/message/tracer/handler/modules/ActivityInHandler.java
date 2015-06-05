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
package org.wso2.carbon.analytics.message.tracer.handler.modules;

import org.apache.axiom.om.OMException;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.handlers.AbstractHandler;
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

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;


public class ActivityInHandler extends AbstractHandler {

    private static final Log LOG = LogFactory.getLog(ActivityInHandler.class);

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
        int tenantID = PublisherUtil.getTenantId(messageContext);
        Map<Integer, EventingConfigData> tenantSpecificEventConfig = TenantEventConfigData.getTenantSpecificEventingConfigData();
        EventingConfigData eventingConfigData = tenantSpecificEventConfig.get(tenantID);
        if (eventingConfigData != null && eventingConfigData.isMessageTracingEnable()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Message tracing enabled.");
            }
            AxisService service = messageContext.getAxisService();
            // Temporary fix for track API manager calls
            if (service == null ||
                (SystemFilter.isFilteredOutService(service.getAxisServiceGroup()) && !AgentUtil.allowedServices(service.getName()))
                || service.isClientSide()) {
                return InvocationResponse.CONTINUE;
            } else {
                String activityUUID = getActivityUUID(messageContext);
                messageContext.setProperty(MessageTracerConstants.TENANT_ID, tenantID);
                TracingInfo tracingInfo = getTracingInfo(messageContext, activityUUID);
                AgentUtil.setTransportHeaders(tracingInfo, (Map<String, Object>) messageContext.getProperty(MessageContext
                                                                                                               .TRANSPORT_HEADERS));
                try {
                    if (eventingConfigData.isDumpBodyEnable()) {
                        buildSoapMessage(messageContext);
                        SOAPEnvelope soapEnvelope = messageContext.getEnvelope();
                        setMustUnderstandHeader(soapEnvelope);
                        SOAPBody body = soapEnvelope.getBody();
                        if (body != null) {
                            tracingInfo.setPayload(body.toString());
                        }
                        SOAPHeader header = soapEnvelope.getHeader();
                        if (header != null) {
                            tracingInfo.setHeader(header.toString());
                        }
                    }
                    if (MessageContext.IN_FLOW == messageContext.getFLOW()) {
                        tracingInfo.setStatus(MessageTracerConstants.STATUS_SUCCESS);
                    } else if (MessageContext.IN_FAULT_FLOW == messageContext.getFLOW()) {
                        tracingInfo.setStatus(MessageTracerConstants.STATUS_FAULT);
                    }
                    if (eventingConfigData.isPublishToBAMEnable()) {
                        publisher.publish(tracingInfo);
                    }
                    if (eventingConfigData.isLoggingEnable()){
                        HandlerUtils.logTracingInfo (tracingInfo);
                    }
                } catch (OMException e) {
                    LOG.error("Unable to get SOAP details " + e.getMessage(), e);
                }
            }
        }
        return InvocationResponse.CONTINUE;
    }

    private void setMustUnderstandHeader(SOAPEnvelope soapEnvelope) {
        if (soapEnvelope.getHeader() != null) {
            Iterator headerBlocks = soapEnvelope.getHeader().getHeadersToProcess(null);
            while (headerBlocks.hasNext()) {
                SOAPHeaderBlock headerBlock = (SOAPHeaderBlock) headerBlocks.next();
// if this header block mustUnderstand but has
// not been processed
// then mark it as processed to get the message
                // in to Synapse
                if (!headerBlock.isProcessed() && headerBlock.getMustUnderstand()) {
                    headerBlock.setProcessed();
                }
            }
        }
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

    @SuppressWarnings("unchecked")
    private String getActivityUUID(MessageContext messageContext) {
        String activityUUID = HandlerUtils.getUniqueId();
        Object transportHeaders = messageContext.getProperty(MessageContext.TRANSPORT_HEADERS);
        if (transportHeaders != null) {
            String aid = (String) ((Map) transportHeaders).get(MessageTracerConstants.ACTIVITY_ID);
            if (aid != null) {
                if (aid.equals(MessageTracerConstants.EMPTY_STRING)) {
                    ((Map) messageContext.getProperty(MessageContext.TRANSPORT_HEADERS)).
                            put(MessageTracerConstants.ACTIVITY_ID, activityUUID);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Propagated AID was empty, IN generating new AID");
                    }
                } else {
                    activityUUID = aid;
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("IN using propagated AID");
                    }
                }
            } else {
                ((Map) transportHeaders).put(MessageTracerConstants.ACTIVITY_ID, activityUUID);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Propagated AID was null, IN generating new AID");
                }
            }
        } else {
            Map<String, String> headers = new TreeMap<>();
            headers.put(MessageTracerConstants.ACTIVITY_ID, activityUUID);
            messageContext.setProperty(MessageContext.TRANSPORT_HEADERS, headers);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Transport headers absent, IN generating new AID");
            }
        }
        return activityUUID;
    }

    private TracingInfo getTracingInfo(MessageContext messageContext, String activityUUID) {
        TracingInfo tracingInfo = new TracingInfo();
        tracingInfo.setActivityId(activityUUID);
        tracingInfo.setServer(AgentUtil.getServerName());
        tracingInfo.setMessageDirection(MessageTracerConstants.IN_DIRECTION);
        tracingInfo.setHost(PublisherUtil.getHostAddress());
        tracingInfo.setServiceName(messageContext.getAxisService().getName());
        tracingInfo.setOperationName(messageContext.getAxisOperation().getName().getLocalPart());
        tracingInfo.setRequestUrl(String.valueOf(messageContext.getProperty(MessageTracerConstants.TRANSPORT_IN_URL)));
        tracingInfo.setUserName(HandlerUtils.getUserNameIN(messageContext));
        tracingInfo.setTimestamp(System.currentTimeMillis());
        return tracingInfo;
    }
}