package org.wso2.carbon.stream.processor.common;

/**
 * DeployerNotifier interface is implemented by deployers to notify other deployers that a new artifact was
 * deployed
 */
public interface DeployerNotifier {

    void register(DeployerListener deployerListener);

    void unregister(DeployerListener deployerListener);

    void broadcastDeploy();

    void broadcastUpdate();

    void broadcastDelete();
}
