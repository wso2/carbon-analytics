/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.databridge.core.internal;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;
import org.wso2.carbon.databridge.commons.utils.EventDefinitionConverterUtils;
import org.wso2.carbon.databridge.core.*;
import org.wso2.carbon.databridge.core.Utils.AgentSession;
import org.wso2.carbon.databridge.core.Utils.EventComposite;
import org.wso2.carbon.databridge.core.conf.DataBridgeConfiguration;
import org.wso2.carbon.databridge.core.definitionstore.AbstractStreamDefinitionStore;
import org.wso2.carbon.databridge.core.definitionstore.StreamAddRemoveListener;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;
import org.wso2.carbon.databridge.core.internal.authentication.AuthenticationHandler;
import org.wso2.carbon.databridge.core.internal.queue.EventQueue;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dispactches events  and their definitions subscribers
 */
public class EventDispatcher {

    private List<AgentCallback> subscribers = new ArrayList<AgentCallback>();
    private List<RawDataAgentCallback> rawDataSubscribers = new ArrayList<RawDataAgentCallback>();
    private AbstractStreamDefinitionStore streamDefinitionStore;
    private Map<Integer, StreamTypeHolder> domainNameStreamTypeHolderCache = new ConcurrentHashMap<Integer, StreamTypeHolder>();
    private EventQueue eventQueue;
    private AuthenticationHandler authenticationHandler;

    private static final Log log = LogFactory.getLog(EventDispatcher.class);


    public EventDispatcher(AbstractStreamDefinitionStore streamDefinitionStore,
                           DataBridgeConfiguration dataBridgeConfiguration, AuthenticationHandler authenticationHandler) {
        this.eventQueue = new EventQueue(subscribers, rawDataSubscribers, dataBridgeConfiguration);
        this.streamDefinitionStore = streamDefinitionStore;
        this.authenticationHandler = authenticationHandler;
        streamDefinitionStore.subscribe(new StreamAddRemoveListener() {
            @Override
            public void streamAdded(int tenantId, String streamId) {

            }

            @Override
            public void streamRemoved(int tenantId, String streamId) {
                removeStreamDefinitionFromStreamTypeHolder(tenantId, streamId);

            }
        });
    }

    public void addCallback(AgentCallback agentCallback) {
        subscribers.add(agentCallback);
    }

    /**
     * Add thrift subscribers
     *
     * @param agentCallback
     */
    public void addCallback(RawDataAgentCallback agentCallback) {
        rawDataSubscribers.add(agentCallback);
    }

    public synchronized String defineStream(String streamDefinition, AgentSession agentSession)
            throws MalformedStreamDefinitionException,
            DifferentStreamDefinitionAlreadyDefinedException,
            StreamDefinitionStoreException {

        int tenantId = agentSession.getCredentials().getTenantId();

        StreamDefinition newStreamDefinition = EventDefinitionConverterUtils.convertFromJson(streamDefinition);

        StreamTypeHolder streamTypeHolder = getStreamDefinitionHolder(tenantId);
        StreamAttributeComposite attributeComposite = streamTypeHolder.getAttributeComposite(newStreamDefinition.getStreamId());
        if (attributeComposite != null) {

            StreamDefinition existingStreamDefinition = attributeComposite.getStreamDefinition();
            if (!existingStreamDefinition.equals(newStreamDefinition)) {
                throw new DifferentStreamDefinitionAlreadyDefinedException("Similar event stream for " + newStreamDefinition + " with the same name and version already exist: " + streamDefinitionStore.getStreamDefinition(newStreamDefinition.getName(), newStreamDefinition.getVersion(), tenantId));
            }
            newStreamDefinition = existingStreamDefinition;

        } else {
            for (StreamAttributeComposite aAttributeComposite : streamTypeHolder.getAttributeCompositeMap().values()) {
                validateStreamDefinition(newStreamDefinition, aAttributeComposite.getStreamDefinition());
            }

            updateDomainNameStreamTypeHolderCache(newStreamDefinition, tenantId);
            streamDefinitionStore.saveStreamDefinition(newStreamDefinition, tenantId);

        }

        for (AgentCallback agentCallback : subscribers) {
            agentCallback.definedStream(newStreamDefinition, tenantId);
        }
        for (RawDataAgentCallback agentCallback : rawDataSubscribers) {
            agentCallback.definedStream(newStreamDefinition, tenantId);
        }
        return newStreamDefinition.getStreamId();
    }

    public synchronized String defineStream(String streamDefinition, AgentSession agentSession,
                                            String indexDefinition)
            throws MalformedStreamDefinitionException,
            DifferentStreamDefinitionAlreadyDefinedException,
            StreamDefinitionStoreException {

        int tenantId = agentSession.getCredentials().getTenantId();

        StreamDefinition newStreamDefinition = EventDefinitionConverterUtils.convertFromJson(streamDefinition);
        StreamTypeHolder streamTypeHolder = getStreamDefinitionHolder(tenantId);
        StreamAttributeComposite attributeComposite = streamTypeHolder.getAttributeComposite(newStreamDefinition.getStreamId());
        if (attributeComposite != null) {

            StreamDefinition existingStreamDefinition = attributeComposite.getStreamDefinition();
            if (!existingStreamDefinition.equals(newStreamDefinition)) {
                throw new DifferentStreamDefinitionAlreadyDefinedException("Similar event stream for " + newStreamDefinition + " with the same name and version already exist: " + streamDefinitionStore.getStreamDefinition(newStreamDefinition.getName(), newStreamDefinition.getVersion(), tenantId));
            }
            newStreamDefinition = existingStreamDefinition;

        } else {
            for (StreamAttributeComposite aAttributeComposite : streamTypeHolder.getAttributeCompositeMap().values()) {
                validateStreamDefinition(newStreamDefinition, aAttributeComposite.getStreamDefinition());
            }

            updateDomainNameStreamTypeHolderCache(newStreamDefinition, tenantId);
            streamDefinitionStore.saveStreamDefinition(newStreamDefinition, tenantId);
        }
        newStreamDefinition.createIndexDefinition(indexDefinition);

        for (AgentCallback agentCallback : subscribers) {
            agentCallback.definedStream(newStreamDefinition, tenantId);
        }
        for (RawDataAgentCallback agentCallback : rawDataSubscribers) {
            agentCallback.definedStream(newStreamDefinition, tenantId);
        }
        return newStreamDefinition.getStreamId();
    }

    private void validateStreamDefinition(StreamDefinition newStreamDefinition,
                                          StreamDefinition existingStreamDefinition)
            throws DifferentStreamDefinitionAlreadyDefinedException {
        if (newStreamDefinition.getName().equals(existingStreamDefinition.getName())) {
            validateAttributes(newStreamDefinition.getMetaData(), existingStreamDefinition.getMetaData(), "meta", newStreamDefinition, existingStreamDefinition);
            validateAttributes(newStreamDefinition.getCorrelationData(), existingStreamDefinition.getCorrelationData(), "correlation", newStreamDefinition, existingStreamDefinition);
            validateAttributes(newStreamDefinition.getPayloadData(), existingStreamDefinition.getPayloadData(), "payload", newStreamDefinition, existingStreamDefinition);
        }
    }

    private void validateAttributes(List<Attribute> newAttributes,
                                    List<Attribute> existingAttributes, String type,
                                    StreamDefinition newStreamDefinition,
                                    StreamDefinition existingStreamDefinition)
            throws DifferentStreamDefinitionAlreadyDefinedException {
        if (newAttributes != null && existingAttributes != null) {
            for (Attribute attribute : newAttributes) {
                for (Attribute existingAttribute : existingAttributes) {
                    if (attribute.getName().equals(existingAttribute.getName())) {
                        if (attribute.getType() != existingAttribute.getType()) {
                            throw new DifferentStreamDefinitionAlreadyDefinedException("Attribute type mismatch " + type + " " +
                                    attribute.getName() + " type:" + attribute.getType() +
                                    " was already defined with type:" + existingAttribute.getType() +
                                    " in " + existingStreamDefinition + ", hence " + newStreamDefinition +
                                    " cannot be defined");
                        }
                    }
                }
            }
        }
    }


    public void publish(Object eventBundle, AgentSession agentSession,
                        EventConverter eventConverter) {
        eventQueue.publish(new EventComposite(eventBundle, getStreamDefinitionHolder(agentSession.getCredentials().getTenantId()), agentSession, eventConverter));
    }

    private StreamTypeHolder getStreamDefinitionHolder(int tenantId) {
        // this will occur only outside of carbon (ex: Siddhi)

        StreamTypeHolder streamTypeHolder = domainNameStreamTypeHolderCache.get(tenantId);

        if (streamTypeHolder != null) {
            if (log.isDebugEnabled()) {
                String logMsg = "Event stream holder for tenant : " + tenantId + " : \n ";
                logMsg += "Meta, Correlation & Payload Data Type Map : ";
                for (Map.Entry entry : streamTypeHolder.getAttributeCompositeMap().entrySet()) {
                    logMsg += "StreamID=" + entry.getKey() + " :  ";
                    logMsg += "Meta= " + Arrays.deepToString(((StreamAttributeComposite) entry.getValue()).getAttributeTypes()[0]) + " :  ";
                    logMsg += "Correlation= " + Arrays.deepToString(((StreamAttributeComposite) entry.getValue()).getAttributeTypes()[1]) + " :  ";
                    logMsg += "Payload= " + Arrays.deepToString(((StreamAttributeComposite) entry.getValue()).getAttributeTypes()[2]) + "\n";
                }
                log.debug(logMsg);
            }
            return streamTypeHolder;
        } else {
            return initDomainNameStreamTypeHolderCache(tenantId);
        }
    }

    public synchronized void updateStreamDefinitionHolder(AgentSession agentSession) {
        int tenantId = agentSession.getCredentials().getTenantId();
        StreamTypeHolder streamTypeHolder = domainNameStreamTypeHolderCache.get(tenantId);

        if (streamTypeHolder != null) {
            if (log.isDebugEnabled()) {
                String logMsg = "Event stream holder for tenant : " + tenantId + " : \n ";
                logMsg += "Meta, Correlation & Payload Data Type Map : ";
                for (Map.Entry entry : streamTypeHolder.getAttributeCompositeMap().entrySet()) {
                    logMsg += "StreamID=" + entry.getKey() + " :  ";
                    logMsg += "Meta= " + Arrays.deepToString(((StreamAttributeComposite) entry.getValue()).getAttributeTypes()[0]) + " :  ";
                    logMsg += "Correlation= " + Arrays.deepToString(((StreamAttributeComposite) entry.getValue()).getAttributeTypes()[1]) + " :  ";
                    logMsg += "Payload= " + Arrays.deepToString(((StreamAttributeComposite) entry.getValue()).getAttributeTypes()[2]) + "\n";
                }
                log.debug(logMsg);
            }
            updateDomainNameStreamTypeHolderCache(tenantId);
        }
    }

    private synchronized void updateDomainNameStreamTypeHolderCache(
            StreamDefinition streamDefinition, int tenantId) {
        StreamTypeHolder streamTypeHolder = getStreamDefinitionHolder(tenantId);
        streamTypeHolder.putStreamDefinition(streamDefinition);
    }

    public synchronized void reloadDomainNameStreamTypeHolderCache(int tenantId){
        StreamTypeHolder streamTypeHolder = getStreamDefinitionHolder(tenantId);
        Collection<StreamDefinition> allStreamDefinitions =
                streamDefinitionStore.getAllStreamDefinitions(tenantId);
        for (StreamDefinition streamDefinition: allStreamDefinitions){
            if (!streamTypeHolder.getAttributeCompositeMap().containsKey(streamDefinition.getStreamId())){
                streamTypeHolder.putStreamDefinition(streamDefinition);
                for (AgentCallback agentCallback : subscribers) {
                    agentCallback.definedStream(streamDefinition, tenantId);
                }
                for (RawDataAgentCallback agentCallback : rawDataSubscribers) {
                    agentCallback.definedStream(streamDefinition, tenantId);
                }
            }
        }
    }

    private synchronized StreamTypeHolder initDomainNameStreamTypeHolderCache(int tenantId) {
        StreamTypeHolder streamTypeHolder = domainNameStreamTypeHolderCache.get(tenantId);
        if (null == streamTypeHolder) {
            streamTypeHolder = new StreamTypeHolder(tenantId);
            streamTypeHolder.setEventDispatcherCallback(this);
            Collection<StreamDefinition> allStreamDefinitions =
                    streamDefinitionStore.getAllStreamDefinitions(tenantId);
            if (null != allStreamDefinitions) {
                for (StreamDefinition aStreamDefinition : allStreamDefinitions) {
                    streamTypeHolder.putStreamDefinition(aStreamDefinition);
                    for (AgentCallback agentCallback : subscribers) {
                        agentCallback.definedStream(aStreamDefinition, tenantId);
                    }
                    for (RawDataAgentCallback agentCallback : rawDataSubscribers) {
                        agentCallback.definedStream(aStreamDefinition, tenantId);
                    }
                }
            }
            domainNameStreamTypeHolderCache.put(tenantId, streamTypeHolder);
        }
        return streamTypeHolder;
    }

    private synchronized StreamTypeHolder updateDomainNameStreamTypeHolderCache(int tenantId) {
        StreamTypeHolder streamTypeHolder = domainNameStreamTypeHolderCache.get(tenantId);
        if (null != streamTypeHolder) {
            Collection<StreamDefinition> allStreamDefinitions =
                    streamDefinitionStore.getAllStreamDefinitions(tenantId);
            if (null != allStreamDefinitions) {
                for (StreamDefinition aStreamDefinition : allStreamDefinitions) {
                    if (streamTypeHolder.getAttributeComposite(aStreamDefinition.getStreamId()) == null) {
                        streamTypeHolder.putStreamDefinition(aStreamDefinition);
                        for (AgentCallback agentCallback : subscribers) {
                            agentCallback.definedStream(aStreamDefinition, tenantId);
                        }
                        for (RawDataAgentCallback agentCallback : rawDataSubscribers) {
                            agentCallback.definedStream(aStreamDefinition, tenantId);
                        }
                    }
                }

                List<String> streamIdList = new ArrayList<String>();
                for (StreamDefinition streamDefinition : allStreamDefinitions) {
                    streamIdList.add(streamDefinition.getStreamId());
                }

                Iterator<String> streamIdIterator = streamTypeHolder.getAttributeCompositeMap().keySet().iterator();
                while (streamIdIterator.hasNext()) {
                    if (!streamIdList.contains(streamIdIterator.next())) {
                        streamIdIterator.remove();
                    }
                }

            }
            domainNameStreamTypeHolderCache.put(tenantId, streamTypeHolder);
        }
        return streamTypeHolder;
    }


    public List<AgentCallback> getSubscribers() {
        return subscribers;
    }

    public List<RawDataAgentCallback> getRawDataSubscribers() {
        return rawDataSubscribers;
    }

    public String findStreamId(String streamName, String streamVersion, AgentSession agentSession)
            throws StreamDefinitionStoreException {

        int tenantId = agentSession.getCredentials().getTenantId();

        //Updating the cache when calling the findStreamId to keep the sync between the stream manager and register with data publisher
        //for CEP - need to review and fix
        updateDomainNameStreamTypeHolderCache(tenantId);
        StreamTypeHolder streamTypeHolder = getStreamDefinitionHolder(tenantId);
        StreamAttributeComposite attributeComposite = streamTypeHolder.getAttributeComposite(DataBridgeCommonsUtils.generateStreamId(streamName, streamVersion));
        if (attributeComposite != null) {
            return attributeComposite.getStreamDefinition().getStreamId();
        }
        return null;
    }

    public boolean deleteStream(String streamName, String streamVersion,
                                AgentSession agentSession) {

        int tenantId = agentSession.getCredentials().getTenantId();

        String streamId = DataBridgeCommonsUtils.generateStreamId(streamName, streamVersion);
        StreamDefinition streamDefinition = removeStreamDefinitionFromStreamTypeHolder(tenantId, streamId);
        if (streamDefinition != null) {
            for (AgentCallback agentCallback : subscribers) {
                agentCallback.removeStream(streamDefinition, tenantId);
            }
            for (RawDataAgentCallback agentCallback : rawDataSubscribers) {
                agentCallback.removeStream(streamDefinition, tenantId);
            }
        }
        return streamDefinitionStore.deleteStreamDefinition(streamName, streamVersion, tenantId);
    }

    private synchronized StreamDefinition removeStreamDefinitionFromStreamTypeHolder(int tenantId,
                                                                                     String streamId) {
        StreamTypeHolder streamTypeHolder = domainNameStreamTypeHolderCache.get(tenantId);
        if (streamTypeHolder != null) {
            StreamAttributeComposite attributeComposite = streamTypeHolder.getAttributeCompositeMap().remove(streamId);
            if (attributeComposite != null) {
                return attributeComposite.getStreamDefinition();
            }
        }
        return null;
    }
}
