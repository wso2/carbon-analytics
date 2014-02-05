/*
 * Copyright 2004,2005 The Apache Software Foundation.
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

package org.wso2.carbon.event.input.adaptor.wsevent.local.internal.util;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.InOnlyAxisOperation;
import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorListener;
import org.wso2.carbon.event.input.adaptor.core.config.InputEventAdaptorConfiguration;
import org.wso2.carbon.event.input.adaptor.core.message.config.InputEventAdaptorMessageConfiguration;

import javax.xml.namespace.QName;
import java.util.List;

public final class Axis2Util {

    private Axis2Util() {
    }

    public static AxisService registerAxis2Service(
            InputEventAdaptorMessageConfiguration inputEventMessageConfiguration,
            InputEventAdaptorListener eventAdaptorListener,
            InputEventAdaptorConfiguration inputEventAdaptorConfiguration,
            AxisConfiguration axisConfiguration, String subscriptionId)
            throws AxisFault {
        //first create an Axis2 service to receive the messages to this broker
        //operation name can not have
        String axisServiceName = inputEventAdaptorConfiguration.getName() + "Service";
        AxisService axisService = axisConfiguration.getService(axisServiceName);
        if (axisService == null) {
            // create a new axis service
            axisService = new AxisService(axisServiceName);

            List<String> transports = axisService.getExposedTransports();
            transports.clear();
            transports.add("http");
            transports.add("https");
            transports.add("local");
            axisService.setExposedTransports(transports);

            axisConfiguration.addService(axisService);
            axisService.getAxisServiceGroup().addParameter(CarbonConstants.DYNAMIC_SERVICE_PARAM_NAME, "true");
        }

        String topicNameWithoutSlash = inputEventMessageConfiguration.getInputMessageProperties().get(WSEventLocalAdaptorConstants.ADAPTOR_MESSAGE_TOPIC_NAME).replaceAll("/", "");
        AxisOperation axisOperation = axisService.getOperation(new QName("", topicNameWithoutSlash));
        if (axisOperation == null) {
            axisOperation = new InOnlyAxisOperation(new QName("", topicNameWithoutSlash));
            axisOperation.setMessageReceiver(new SubscriptionMessageReceiver());
            axisOperation.setSoapAction("urn:" + topicNameWithoutSlash);


            axisConfiguration.getPhasesInfo().setOperationPhases(axisOperation);
            axisService.addOperation(axisOperation);
        }

        SubscriptionMessageReceiver messageReceiver =
                (SubscriptionMessageReceiver) axisOperation.getMessageReceiver();
        messageReceiver.addEventAdaptorListener(subscriptionId, eventAdaptorListener);
        return axisService;
    }

    /**
     * removes the operation from the Axis service.
     *
     * @param inputEventMessageConfiguration
     *
     * @param inputEventAdaptorConfiguration
     *
     * @param axisConfiguration
     * @param subscriptionId
     * @throws org.apache.axis2.AxisFault
     */
    public static void removeOperation(
            InputEventAdaptorMessageConfiguration inputEventMessageConfiguration,
            InputEventAdaptorConfiguration inputEventAdaptorConfiguration,
            AxisConfiguration axisConfiguration, String subscriptionId) throws
                                                                        AxisFault {


        String axisServiceName = inputEventAdaptorConfiguration.getName() + "Service";
        AxisService axisService = axisConfiguration.getService(axisServiceName);

        if (axisService == null) {
            throw new AxisFault("There is no service with the name ==> " + axisServiceName);
        }
        String topicNameWithoutSlash = inputEventMessageConfiguration.getInputMessageProperties().get(WSEventLocalAdaptorConstants.ADAPTOR_MESSAGE_TOPIC_NAME).replaceAll("/", "");
        AxisOperation axisOperation = axisService.getOperation(new QName("", topicNameWithoutSlash));
        if (axisOperation == null) {
            throw new AxisFault("There is no operation with the name ==> " + topicNameWithoutSlash);
        }
        SubscriptionMessageReceiver messageReceiver =
                (SubscriptionMessageReceiver) axisOperation.getMessageReceiver();
        if (messageReceiver == null) {
            throw new AxisFault("There is no message receiver for operation with name ==> " + topicNameWithoutSlash);
        }
        if (messageReceiver.removeEventAdaptorListener(subscriptionId)) {
            axisService.removeOperation(new QName("", inputEventMessageConfiguration.getInputMessageProperties().get(WSEventLocalAdaptorConstants.ADAPTOR_MESSAGE_TOPIC_NAME).replaceAll("/", "")));
        }

    }


}
