package org.wso2.carbon.event.input.adapter.soap.internal;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.receivers.AbstractInMessageReceiver;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterListener;
import org.wso2.carbon.event.input.adapter.core.exception.InputEventAdapterRuntimeException;

public class SubscriptionMessageReceiver extends AbstractInMessageReceiver {
    private final String serviceName;
    private final String operationName;
    private final int tenantId;
    private final InputEventAdapterListener eventAdaptorListener;

    public SubscriptionMessageReceiver(InputEventAdapterListener eventAdaptorListener, String serviceName, String operationName, int tenantId) {
        this.serviceName = serviceName;
        this.operationName = operationName;
        this.tenantId = tenantId;
        this.eventAdaptorListener = eventAdaptorListener;
    }

    protected void invokeBusinessLogic(MessageContext messageContext) throws AxisFault {

        SOAPEnvelope soapEnvelope = messageContext.getEnvelope();
        OMElement bodyElement = soapEnvelope.getBody().getFirstElement();

        if (log.isDebugEnabled()) {
            log.debug("Event received in Soap Input Event Adaptor - " + bodyElement);
        }
        if (bodyElement != null) {
            try {
                eventAdaptorListener.onEvent(bodyElement);
            } catch (InputEventAdapterRuntimeException e) {
                log.error("Can not process the received event ", e);
            }
        } else {
            log.warn("Dropping the empty/null event received through soap adaptor service " + serviceName
                    + " for the operation " + operationName + " & tenant " + tenantId);
        }
    }
}
