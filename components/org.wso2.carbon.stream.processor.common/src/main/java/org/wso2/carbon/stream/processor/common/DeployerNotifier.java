package org.wso2.carbon.stream.processor.common;

import org.wso2.carbon.deployment.engine.Artifact;

/**
 * DeployerNotifier class is the used for deployers to notify other deployers that a new artifact was deployed
 * */
public interface DeployerNotifier {
    void register(DeployerListener deployerListener);

    void unregister(DeployerListener deployerListener);

    void broadcast(Artifact artifact);
}