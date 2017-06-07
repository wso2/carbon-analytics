package org.wso2.carbon.stream.processor.common;

/**
 * DeployerListener interface is implemented by deployers that depends on artifacts deployed by other deployers
 */
public interface DeployerListener {

    void onDeploy();

    void onUpdate();

    void onDelete();

}
