/*
package org.wso2.carbon.event.stream.core.internal;

import com.hazelcast.core.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.utils.EventDefinitionConverterUtils;
import org.wso2.carbon.event.stream.core.StreamDefinitionStore;
import org.wso2.carbon.event.stream.core.exception.StreamDefinitionNotFoundException;
import org.wso2.carbon.event.stream.core.exception.StreamDefinitionStoreException;
import org.wso2.carbon.event.stream.core.internal.ds.EventStreamServiceValueHolder;
import org.wso2.carbon.event.stream.core.internal.stream.StreamAddRemoveListener;
import org.wso2.carbon.event.stream.core.internal.util.RegistryStreamDefinitionStoreUtil;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.core.tenant.Tenant;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CarbonStreamDefinitionStore implements StreamDefinitionStore {

    private Log log = LogFactory.getLog(CarbonStreamDefinitionStore.class);


    private List<StreamAddRemoveListener> streamAddRemoveListenerList = new ArrayList<StreamAddRemoveListener>();
    */
/*private Map<String, StreamDefinition> streamDefCache = new ConcurrentHashMap<String, StreamDefinition>();
    Timer timer= new Timer();
    ITopic<String> topic = null;*//*


    */
/*@Override
    public void startMessageListner() {
        if(EventStreamServiceValueHolder.getHazelcastInstance() != null ) {
            topic = EventStreamServiceValueHolder.getHazelcastInstance().getTopic("Foo");
            topic.addMessageListener(new MessageListener<String>() {
                @Override
                public void onMessage(Message<String> message) {
                    String elements[] = message.getMessageObject().split(StreamdefinitionStoreConstants.TOPIC_MESSAGE_SPLITTER);
                    if(elements[2].equals(StreamdefinitionStoreConstants.TOPIC_MESSAGE_ADDED)) {
                        for (StreamAddRemoveListener streamAddRemoveListener : streamAddRemoveListenerList) {
                            streamAddRemoveListener.streamAdded(Integer.parseInt(elements[0]), elements[1]);
                        }
                    } else {
                        for (StreamAddRemoveListener streamAddRemoveListener : streamAddRemoveListenerList) {
                            streamAddRemoveListener.streamRemoved(Integer.parseInt(elements[0]), elements[1]);
                        }
                    }
                }
            });
        }
    }

    @Override
    public void startTimer() {
        timer.schedule(new TimerTask() {
           public void run() {
               Map<String, StreamDefinition> streamDefCacheTemp = new ConcurrentHashMap<String, StreamDefinition>();
               streamDefCacheTemp.putAll(streamDefCache);
               List<Tenant> tenantList;
               try {
                   tenantList = TenantAxisUtils.getActiveTenants(EventStreamServiceValueHolder.
                           getConfigurationContextService().getServerConfigContext());
               } catch (Exception e) {
                   throw new RuntimeException("Unexpected Error while getting tenant list",e);
               }
               for(Tenant tenant: tenantList) {
                   int tenantID = tenant.getId();
                   Collection<StreamDefinition> streamDefinitions = getAllStreamDefinitions(tenantID);
                   for (StreamDefinition newStreamDefinition : streamDefinitions) {
                       StreamDefinition oldStreamdefinition = streamDefCacheTemp.get(tenantID
                               + StreamdefinitionStoreConstants.STREAM_ID_SPLITTER
                               + newStreamDefinition.getStreamId());
                       if(oldStreamdefinition == null) {
                           streamDefCache.put(tenantID
                                   + StreamdefinitionStoreConstants.STREAM_ID_SPLITTER
                                   + newStreamDefinition.getStreamId(),newStreamDefinition);
                           for (StreamAddRemoveListener streamAddRemoveListener : streamAddRemoveListenerList) {
                               streamAddRemoveListener.streamAdded(tenantID,newStreamDefinition.getStreamId());
                           }
                       } else if(oldStreamdefinition.equals(newStreamDefinition)){
                           streamDefCacheTemp.remove(tenantID
                                   + StreamdefinitionStoreConstants.STREAM_ID_SPLITTER
                                   + newStreamDefinition.getStreamId());
                       } else {
                           streamDefCache.remove(tenantID
                                   + StreamdefinitionStoreConstants.STREAM_ID_SPLITTER
                                   + oldStreamdefinition.getStreamId());
                           for (StreamAddRemoveListener streamAddRemoveListener : streamAddRemoveListenerList) {
                               streamAddRemoveListener.streamRemoved(tenantID, newStreamDefinition.getStreamId());
                           }
                           streamDefCache.put(tenantID
                                   + StreamdefinitionStoreConstants.STREAM_ID_SPLITTER
                                   + newStreamDefinition.getStreamId(),newStreamDefinition);
                           for (StreamAddRemoveListener streamAddRemoveListener : streamAddRemoveListenerList) {
                               streamAddRemoveListener.streamAdded(tenantID,newStreamDefinition.getStreamId());
                           }
                           streamDefCacheTemp.remove(tenantID
                                   + StreamdefinitionStoreConstants.STREAM_ID_SPLITTER
                                   + newStreamDefinition.getStreamId());
                       }
                   }
               }
               Set<String> deletedSDs = streamDefCacheTemp.keySet();
               for (String deletedSD : deletedSDs) {
                   String elements[] = deletedSD.split(StreamdefinitionStoreConstants.STREAM_ID_SPLITTER);
                   for (StreamAddRemoveListener streamAddRemoveListener : streamAddRemoveListenerList) {
                       streamAddRemoveListener.streamAdded(Integer.parseInt(elements[0]),elements[1]+":"+elements[2]);
                   }
                   streamDefCache.remove(Integer.parseInt(elements[0])+":"+elements[1]+":"+elements[2]);
               }
           }
       }, StreamdefinitionStoreConstants.IMMEDIATE,
                StreamdefinitionStoreConstants.CACHE_TIMEOUT);
    }*//*


    @Override
    public StreamDefinition getStreamDefinition(String name, String version, int tenantId)
            throws StreamDefinitionNotFoundException, StreamDefinitionStoreException {
        try {
            UserRegistry registry = EventStreamServiceValueHolder.getRegistryService()
                    .getGovernanceSystemRegistry(tenantId);
            Resource resource = registry.get(RegistryStreamDefinitionStoreUtil.getStreamDefinitionPath(name, version));
            if(registry.resourceExists(RegistryStreamDefinitionStoreUtil.getStreamDefinitionPath(name, version)) ) {
                return EventDefinitionConverterUtils
                        .convertFromJson(RegistryUtils.decodeBytes((byte[]) resource.getContent()));
            }
        } catch (RegistryException e) {
            log.error("Error accessing registry " + RegistryStreamDefinitionStoreUtil.getStreamDefinitionPath(name, version), e);
        } catch (MalformedStreamDefinitionException e) {
            log.error(e.getErrorMessage(), e);
        }
        return null;
    }

    @Override
    public StreamDefinition getStreamDefinition(String streamId, int tenantId) throws StreamDefinitionNotFoundException, StreamDefinitionStoreException {
        String name, version;
        if(streamId != null && streamId.contains(StreamdefinitionStoreConstants.STREAM_ID_SPLITTER)) {
            name = streamId.split(StreamdefinitionStoreConstants.STREAM_ID_SPLITTER)[0];
            version = streamId.split(StreamdefinitionStoreConstants.STREAM_ID_SPLITTER)[1];
        } else {
            return null;
        }
        try {
            UserRegistry registry = EventStreamServiceValueHolder.getRegistryService().getGovernanceSystemRegistry(tenantId);
            if(registry.resourceExists(RegistryStreamDefinitionStoreUtil.getStreamDefinitionPath(name, version)) ) {
                return EventDefinitionConverterUtils
                        .convertFromJson(RegistryUtils.decodeBytes((byte[]) registry.
                                get(RegistryStreamDefinitionStoreUtil.getStreamDefinitionPath(name,
                                        version)).getContent()));
            }
        } catch (RegistryException e) {
            log.error("Error accessing registry " + RegistryStreamDefinitionStoreUtil.getStreamDefinitionPath(name, version), e);
        } catch (MalformedStreamDefinitionException e) {
            log.error(e.getErrorMessage(), e);
        }
        return null;
    }

    @Override
    public Collection<StreamDefinition> getAllStreamDefinitions(int tenantId) {
        ConcurrentHashMap<String, StreamDefinition> map = new ConcurrentHashMap<String, StreamDefinition>();

        try {
            UserRegistry registry = EventStreamServiceValueHolder.getRegistryService().getGovernanceSystemRegistry(tenantId);

            if (!registry.resourceExists(RegistryStreamDefinitionStoreUtil.getStreamDefinitionStorePath())) {
                registry.put(RegistryStreamDefinitionStoreUtil.getStreamDefinitionStorePath(), registry.newCollection());
            } else {
                org.wso2.carbon.registry.core.Collection collection =
                        (org.wso2.carbon.registry.core.Collection) registry.get(RegistryStreamDefinitionStoreUtil.
                                getStreamDefinitionStorePath());
                for (String streamNameCollection : collection.getChildren()) {

                    org.wso2.carbon.registry.core.Collection innerCollection =
                            (org.wso2.carbon.registry.core.Collection) registry.get(streamNameCollection);
                    for (String streamVersionCollection : innerCollection.getChildren()) {

                        Resource resource = (Resource) registry.get(streamVersionCollection);
                        try {
                            StreamDefinition streamDefinition = EventDefinitionConverterUtils
                                    .convertFromJson(RegistryUtils.decodeBytes((byte[]) resource.getContent()));
                            map.put(streamDefinition.getStreamId(), streamDefinition);
                        } catch (Throwable e) {
                            log.error("Error in retrieving streamDefinition from the resource at "
                                    + resource.getPath(), e);
                        }
                    }
                }
            }

        } catch (RegistryException e) {
            log.error("Error in retrieving streamDefinitions from the registry", e);
        }

        return map.values();
    }

    @Override
    public void saveStreamDefinition(StreamDefinition streamDefinition, int tenantId) {

        try {
            UserRegistry registry = EventStreamServiceValueHolder.getRegistryService().getGovernanceSystemRegistry(tenantId);
            Resource resource = registry.newResource();
            resource.setContent(EventDefinitionConverterUtils.convertToJson(streamDefinition));
            resource.setMediaType(StreamdefinitionStoreConstants.JSON_TYPE);
            if(registry.resourceExists(RegistryStreamDefinitionStoreUtil.getStreamDefinitionPath(streamDefinition.
                    getName(), streamDefinition.getVersion())) ) {
                deleteStreamDefinition(streamDefinition.getName(), streamDefinition.getVersion(), tenantId);
            }
            registry.put(RegistryStreamDefinitionStoreUtil.getStreamDefinitionPath(streamDefinition.
                    getName(), streamDefinition.getVersion()), resource);
            for (StreamAddRemoveListener streamAddRemoveListener : streamAddRemoveListenerList) {
                streamAddRemoveListener.streamAdded(tenantId, streamDefinition.getStreamId());
            }
            log.info("Stream definition added to registry successfully : " + streamDefinition.getStreamId());
        } catch (RegistryException e) {
            log.error("Failed adding stream definition : " + streamDefinition.getStreamId(), e);
        } catch (StreamDefinitionStoreException e) {
            //ignore, since already checked
        }
    }

    @Override
    public boolean deleteStreamDefinition(String name, String version, int tenantId) throws StreamDefinitionStoreException {
        try {
            UserRegistry registry = EventStreamServiceValueHolder.getRegistryService().
                    getGovernanceSystemRegistry(tenantId);
            if(!registry.resourceExists(RegistryStreamDefinitionStoreUtil.getStreamDefinitionPath(name, version)) ) {
                throw new StreamDefinitionStoreException("Cannot find the stream definition " + name + ":"
                        + version + " to delete");
            } else {
                registry.delete(RegistryStreamDefinitionStoreUtil.getStreamDefinitionPath(name, version));
                for (StreamAddRemoveListener streamAddRemoveListener : streamAddRemoveListenerList) {
                    streamAddRemoveListener.streamRemoved(tenantId, name + ":" + version);
                }
                return true;
            }
        } catch (RegistryException e) {
            log.error("failed deleting stream definition " + name + ":" + version, e);
        }
        return false;
    }

    @Override
    public void subscribe(StreamAddRemoveListener streamAddRemoveListener) {
        if (streamAddRemoveListener != null) {
            streamAddRemoveListenerList.add(streamAddRemoveListener);
        }
    }

    @Override
    public void unsubscribe(StreamAddRemoveListener streamAddRemoveListener) {
        if (streamAddRemoveListener != null) {
            streamAddRemoveListenerList.remove(streamAddRemoveListener);
        }
    }

    @Override
    public void editStreamDefinition(StreamDefinition streamDefinition, int tenantId)
            throws StreamDefinitionStoreException {
        deleteStreamDefinition(streamDefinition.getName(),streamDefinition.getVersion(),tenantId);
        saveStreamDefinition(streamDefinition, tenantId);
    }

    @Override
    public void memberAdded(MembershipEvent membershipEvent) {

    }

    @Override
    public void memberRemoved(MembershipEvent membershipEvent) {

    }
}*/
