/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.event.builder.core.internal.config;

import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderUtil;
import org.wso2.carbon.event.processor.api.receive.BasicEventListener;
import org.wso2.carbon.event.processor.api.receive.Wso2EventListener;
import org.wso2.carbon.event.processor.api.receive.exception.EventReceiverException;

import java.util.ArrayList;
import java.util.List;

public class StreamEventJunction {
    private StreamDefinition exportedStreamDefinition = null;
    private Attribute[] exportedStreamAttributes = null;
    private List<BasicEventListener> basicEventListeners = new ArrayList<BasicEventListener>();
    private List<Wso2EventListener> wso2EventListeners = new ArrayList<Wso2EventListener>();
    private StreamJunctionManager streamJunctionManager = null;

    public StreamEventJunction(StreamDefinition exportedStreamDefinition, StreamJunctionManager streamJunctionManager) {
        this.exportedStreamDefinition = exportedStreamDefinition;
        this.streamJunctionManager = streamJunctionManager;
        if (exportedStreamDefinition != null) {
            this.exportedStreamAttributes = EventBuilderUtil.streamDefinitionToAttributeArray(exportedStreamDefinition);
        }
    }

    /**
     * Sets or unsets the exported stream definition for a stream event junction. Note that this method would
     * register the stream definition with its associated {@link StreamJunctionManager} if the exported stream
     * definition passed in is not null. If {@literal null} is passed in for exportedStreamDefinition, the currently set
     * exportedStreamDefinition will be unregistered from {@link StreamJunctionManager}
     *
     * @param exportedStreamDefinition the stream definition to be set for this {@link StreamEventJunction}
     * @param tenantId                 the tenant id of the calling context
     */
    public void setExportedStreamDefinition(StreamDefinition exportedStreamDefinition, int tenantId) {
        if (this.exportedStreamDefinition == null && exportedStreamDefinition != null) {
            this.streamJunctionManager.registerStreamDefinition(exportedStreamDefinition, tenantId);
            this.exportedStreamAttributes = EventBuilderUtil.streamDefinitionToAttributeArray(exportedStreamDefinition);
        } else if (this.exportedStreamDefinition != null && exportedStreamDefinition == null) {
            this.streamJunctionManager.unregisterStreamDefinition(this.exportedStreamDefinition.getStreamId(), tenantId);
            this.exportedStreamAttributes = null;
        }
        this.exportedStreamDefinition = exportedStreamDefinition;
    }

    public StreamDefinition getExportedStreamDefinition() {
        return exportedStreamDefinition;
    }

    public void addEventListener(BasicEventListener basicEventListener) {
        basicEventListeners.add(basicEventListener);
        basicEventListener.onAddDefinition(exportedStreamAttributes);
    }

    public void removeEventListener(BasicEventListener basicEventListener) throws EventReceiverException {
        if (!basicEventListeners.contains(basicEventListener)) {
            throw new EventReceiverException("The given basic event listener is not registered to the stream:" + this.exportedStreamDefinition.getStreamId());
        }
        basicEventListeners.remove(basicEventListener);
        basicEventListener.onRemoveDefinition(exportedStreamAttributes);
    }

    public void addEventListener(Wso2EventListener wso2EventListener) {
        wso2EventListeners.add(wso2EventListener);
        wso2EventListener.onAddDefinition(exportedStreamDefinition);
    }

    public void removeEventListener(Wso2EventListener wso2EventListener) throws EventReceiverException {
        if (!wso2EventListeners.contains(wso2EventListener)) {
            throw new EventReceiverException("The given wso2 event listener is not registered to the stream:" + this.exportedStreamDefinition.getStreamId());
        }
        wso2EventListeners.remove(wso2EventListener);
        wso2EventListener.onRemoveDefinition(exportedStreamDefinition);
    }

    public void dispatchEvent(Object[] outObjArray) {
        if (!basicEventListeners.isEmpty()) {
            for (BasicEventListener basicEventListener : basicEventListeners) {
                basicEventListener.onEvent(outObjArray);
            }
        }
        if (!wso2EventListeners.isEmpty()) {
            Event event = convertToWso2Event(outObjArray);
            for (Wso2EventListener wso2EventListener : wso2EventListeners) {
                wso2EventListener.onEvent(event);
            }
        }
    }

    private Event convertToWso2Event(Object[] objArray) {
        int metaSize;
        int correlationSize;
        int payloadSize;

        Object[] metaAttributes = null;
        Object[] correlationAttributes = null;
        Object[] payloadAttributes = null;

        int attributeIndex = 0;

        if (exportedStreamDefinition.getMetaData() != null) { // If there is at least 1 meta data field
            metaSize = exportedStreamDefinition.getMetaData().size();
            metaAttributes = new Object[metaSize];
            for (int i = 0; i < metaSize; i++) {
                metaAttributes[i] = objArray[attributeIndex++];
            }
        }
        if (exportedStreamDefinition.getCorrelationData() != null) { // If there is at least 1 correlation data field
            correlationSize = exportedStreamDefinition.getCorrelationData().size();
            correlationAttributes = new Object[correlationSize];
            for (int i = 0; i < correlationSize; i++) {
                correlationAttributes[i] = objArray[attributeIndex++];
            }
        }
        if (exportedStreamDefinition.getPayloadData() != null) { // If there is at least 1 payload data field
            payloadSize = exportedStreamDefinition.getPayloadData().size();
            payloadAttributes = new Object[payloadSize];
            for (int i = 0; i < payloadSize; i++) {
                payloadAttributes[i] = objArray[attributeIndex++];
            }
        }

        return new Event(exportedStreamDefinition.getStreamId(), System.currentTimeMillis(), metaAttributes, correlationAttributes, payloadAttributes);
    }

    public void cleanup() {
        if (!basicEventListeners.isEmpty()) {
            for (BasicEventListener basicEventListener : basicEventListeners) {
                basicEventListener.onRemoveDefinition(exportedStreamAttributes);
            }
        }
        if (!wso2EventListeners.isEmpty()) {
            for (Wso2EventListener wso2EventListener : wso2EventListeners) {
                wso2EventListener.onRemoveDefinition(exportedStreamDefinition);
            }
        }
        basicEventListeners.clear();
        wso2EventListeners.clear();
    }
}
