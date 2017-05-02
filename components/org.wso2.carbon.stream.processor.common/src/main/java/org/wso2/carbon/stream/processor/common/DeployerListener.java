package org.wso2.carbon.stream.processor.common;

import org.wso2.carbon.deployment.engine.Artifact;

/**
 * DeployerListener class is used by deployers that depends on artifacts deployed by other deployers
 * */
public interface DeployerListener {
    void onDeploy(Artifact artifact);
}