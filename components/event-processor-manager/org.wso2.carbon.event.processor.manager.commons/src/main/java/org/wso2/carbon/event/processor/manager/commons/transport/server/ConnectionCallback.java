package org.wso2.carbon.event.processor.manager.commons.transport.server;

/**
 * Invoke this callback when a connection between CEP and Storm is created or lost.
 */
public interface ConnectionCallback {

    public void onCepReceiverConnect();

    public void onCepReceiverDisconnect();

    public void onPublisherBoltConnect();

    public void onPublisherBoltDisconnect();
}
