package org.wso2.carbon.event.stream.core;

import com.hazelcast.core.MembershipEvent;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.stream.core.exception.StreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.event.stream.core.exception.StreamDefinitionNotFoundException;
import org.wso2.carbon.event.stream.core.exception.StreamDefinitionStoreException;
import org.wso2.carbon.event.stream.core.internal.stream.StreamAddRemoveListener;

import java.util.Collection;

public interface StreamDefinitionStore {/*
    void startMessageListner();
    void startTimer();*/

    StreamDefinition getStreamDefinition(String name, String version, int tenantId) throws StreamDefinitionNotFoundException, StreamDefinitionStoreException;

    StreamDefinition getStreamDefinition(String streamId, int tenantId) throws StreamDefinitionNotFoundException, StreamDefinitionStoreException;

    Collection<StreamDefinition> getAllStreamDefinitions(int tenantId);

    void saveStreamDefinition(StreamDefinition streamDefinition, int tenantId);

    boolean deleteStreamDefinition(String name, String version, int tenantId) throws StreamDefinitionStoreException;

    void subscribe(StreamAddRemoveListener streamAddRemoveListener);

    void unsubscribe(StreamAddRemoveListener streamAddRemoveListener);

    void editStreamDefinition(StreamDefinition streamDefinition, int tenantId) throws StreamDefinitionStoreException;

    void memberAdded(MembershipEvent membershipEvent);

    void memberRemoved(MembershipEvent membershipEvent);
}
