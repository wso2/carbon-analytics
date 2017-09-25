package org.wso2.carbon.business.rules.api;

import org.wso2.msf4j.MicroservicesRunner;

/**
 * Application entry point.
 *
 * @since 1.0.0-SNAPSHOT
 */
public class Application {
    public static void main(String[] args) {
        new MicroservicesRunner()
                .deploy(new BusinessRuleApi())
                .start();
        new MicroservicesRunner()
                .deploy(new TemplateGroupsApi())
                .start();
        new MicroservicesRunner()
                .deploy(new InstancesApi())
                .start();
    }
}
