package org.wso2.carbon.databridge.core;

import org.wso2.carbon.databridge.commons.StreamDefinition;


public interface RawDataAgentCallback {
    /**
     * will get called  when types are defined
     *
     * @param streamDefinition TypeDefinition of event streams
     * @param tenantId         of the credential defining the event stream definition
     */
    void definedStream(StreamDefinition streamDefinition, int tenantId);

    /**
     * will get called  when types are removed
     *
     * @param streamDefinition TypeDefinition of event streams
     * @param tenantId         of the credential defining the event stream definition
     */
    void removeStream(StreamDefinition streamDefinition, int tenantId);

    /**
     * will get called when Events arrive
     *
     * @param eventComposite Event Composite
     */
    void receive(Object eventComposite);

}
