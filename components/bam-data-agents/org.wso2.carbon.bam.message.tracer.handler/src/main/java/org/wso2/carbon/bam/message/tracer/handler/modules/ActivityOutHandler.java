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
package org.wso2.carbon.bam.message.tracer.handler.modules;

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
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.httpclient.Header;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.data.publisher.util.BAMDataPublisherConstants;
import org.wso2.carbon.bam.data.publisher.util.PublisherUtil;
import org.wso2.carbon.bam.message.tracer.handler.conf.EventingConfigData;
import org.wso2.carbon.bam.message.tracer.handler.data.TracingInfo;
import org.wso2.carbon.bam.message.tracer.handler.publish.Publisher;
import org.wso2.carbon.bam.message.tracer.handler.util.AgentUtil;
import org.wso2.carbon.bam.message.tracer.handler.util.HandlerUtils;
import org.wso2.carbon.bam.message.tracer.handler.util.MessageTracerConstants;
import org.wso2.carbon.bam.message.tracer.handler.util.TenantEventConfigData;
import org.wso2.carbon.core.util.SystemFilter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ActivityOutHandler extends AbstractHandler {

    private static Log log = LogFactory.getLog(ActivityOutHandler.class);

    public static final String HTTP_SC = "HTTP_SC";

    private Publisher publisher;

    @Override
    public void init(HandlerDescription handlerdesc) {
        if (log.isDebugEnabled()) {
            log.debug("Initiate message tracer handler");
        }
        super.init(handlerdesc);
        publisher = new Publisher();
    }

    @Override
    public InvocationResponse invoke(MessageContext messageContext) throws AxisFault {

        MessageContext inMessageContext = null;
        int tenantID = PublisherUtil.getTenantId(messageContext);
        if (tenantID == -1) {
            inMessageContext = messageContext.getOperationContext().getMessageContext(
                    WSDL2Constants.MESSAGE_LABEL_IN);
            Object tId = inMessageContext.getProperty(MessageTracerConstants.TENANT_ID);
            if (tId != null) {
                tenantID = Integer.parseInt(tId.toString());
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
                    inMessageContext = messageContext.getOperationContext().getMessageContext(
                            WSDL2Constants.MESSAGE_LABEL_IN);
                }

                String activityID = HandlerUtils.getUniqueId();
                //engage transport headers
                Object transportHeaders = messageContext.getProperty(MessageContext.TRANSPORT_HEADERS);

                Object inTransportHeaders = inMessageContext.getProperty(MessageContext.TRANSPORT_HEADERS);

                if (transportHeaders != null) {
                    String aid = (String) ((Map) transportHeaders).get(MessageTracerConstants.ACTIVITY_ID);
                    if (aid == null || aid.equals(MessageTracerConstants.EMPTY_STRING)) {
                        if (inTransportHeaders != null) {
                            String inID = (String) ((Map) inTransportHeaders).get(MessageTracerConstants.ACTIVITY_ID);
                            if (!((inID == null) || (inID.equals(MessageTracerConstants.EMPTY_STRING)))) {
                                activityID = inID;
                                if (log.isDebugEnabled()) {
                                    log.debug("OUT using IN's AID, transport header present");
                                }
                            }
                        }
                        ((Map) messageContext.getProperty(MessageContext.TRANSPORT_HEADERS)).
                                put(MessageTracerConstants.ACTIVITY_ID, activityID);
                    } else {
                        activityID = aid;
                        if (log.isDebugEnabled()) {
                            log.debug("OUT using new AID :" + aid);
                        }
                    }
                } else {
                    if (inTransportHeaders != null) {
                        String inID = (String) ((Map) inTransportHeaders).get(MessageTracerConstants.ACTIVITY_ID);
                        if (!((inID == null) || (inID.equals(MessageTracerConstants.EMPTY_STRING)))) {
                            activityID = inID;
                            if (log.isDebugEnabled()) {
                                log.debug("OUT using IN's AID, transport header absent");
                            }
                        }
                    }
//                    Map<String, String> headers = new TreeMap<String, String>();
//                    headers.put(MessageTracerConstants.ACTIVITY_ID, activityID);
//                    messageContext.setProperty(MessageContext.TRANSPORT_HEADERS, headers);

                    MessageContext responseMessageContext;
                    List headersList = new ArrayList();
                    
                    //The messageContext passed to this is already outMessageContext,
                    // hence we can directly use that.
                    responseMessageContext =  messageContext;
                    
                    headersList.add(new Header(MessageTracerConstants.ACTIVITY_ID, activityID));
                    responseMessageContext.setProperty(HTTPConstants.HTTP_HEADERS, headersList);
                }

                TracingInfo tracingInfo = new TracingInfo();
                tracingInfo.setActivityId(activityID);
                tracingInfo.setServer(AgentUtil.getServerName());
                tracingInfo.setMessageDirection(BAMDataPublisherConstants.OUT_DIRECTION);
                tracingInfo.setHost(PublisherUtil.getHostAddress());
                tracingInfo.setServiceName(messageContext.getAxisService().getName());
                tracingInfo.setOperationName(messageContext.getAxisOperation().getName().getLocalPart());

                try {
                    if (eventingConfigData.isDumpBodyEnable()) {
                        try {
                            Class cls = Class.forName(MessageTracerConstants.ORG_APACHE_SYNAPSE_TRANSPORT_PASSTHRU_UTIL_RELAY_UTILS_CLASS_NAME);
                            Class[] paramClasses = new Class[]{MessageContext.class, Boolean.TYPE};
                            Method method = cls.getMethod(MessageTracerConstants.BUILD_MESSAGE_METHOD_NAME, paramClasses);
                            method.invoke(null, messageContext, false);
                        } catch (ClassNotFoundException ignore) {
                            // ignore
                        } catch (Exception e) {
                            throw new AxisFault("Error in building input message: " + e.getMessage(), e);
                        }
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

                    tracingInfo.setTimestamp(System.currentTimeMillis());

                    if (MessageContext.OUT_FLOW == messageContext.getFLOW()) {
                        Object httpSC = messageContext.getProperty(HTTP_SC);
                        if (httpSC != null) {
                            int statusCode = Integer.parseInt(httpSC.toString());
                            if (statusCode == 200 || statusCode == 202) {
                                tracingInfo.setStatus(MessageTracerConstants.STATUS_SUCCESS);
                            } else {
                                tracingInfo.setStatus(MessageTracerConstants.STATUS_FAULT);
                            }
                        } else {
                            tracingInfo.setStatus(MessageTracerConstants.STATUS_SUCCESS);
                        }
                    } else if (MessageContext.OUT_FAULT_FLOW == messageContext.getFLOW()) {
                        tracingInfo.setStatus(MessageTracerConstants.STATUS_FAULT);
                    }

                    if (eventingConfigData.isPublishToBAMEnable()) {
                        publisher.publish(tenantID, tracingInfo);
                    }

                    if (eventingConfigData.isLoggingEnable()) {
                        HandlerUtils.logTracingInfo(tracingInfo);
                    }

                } catch (OMException e) {
                    log.error("Unable to get SOAP details " + e.getMessage(), e);
                }
            }
        }

        return InvocationResponse.CONTINUE;
    }
}
