package org.wso2.carbon.databridge.core;

import org.wso2.carbon.databridge.commons.StreamDefinition;


public interface RawDataAgentCallback {
    // TODO: 1/30/17 no tenant concept
    /**
     * will get called  when types are defined
     *
     * @param streamDefinition TypeDefinition of event streams
     */
    void definedStream(StreamDefinition streamDefinition/*, int tenantId*/);

    /**
     * will get called  when types are removed
     *
     * @param streamDefinition TypeDefinition of event streams
     */
    void removeStream(StreamDefinition streamDefinition/*, int tenantId*/);

    /**
     * will get called when Events arrive
     *
     * @param eventComposite Event Composite
     */
    void receive(Object eventComposite);

}
