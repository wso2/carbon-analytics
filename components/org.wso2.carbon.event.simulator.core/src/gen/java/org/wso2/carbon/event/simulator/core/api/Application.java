package org.wso2.carbon.event.simulator.core.api;

import org.wso2.msf4j.MicroservicesRunner;

/**
 * Application entry point.
 *
 * @since 1.0.0-SNAPSHOT
 */
public class Application {
    public static void main(String[] args) {

        System.out.println("starting Micro Services");
        new MicroservicesRunner()
                .deploy(new SingleApi())
                .start();
        new MicroservicesRunner()
                .deploy(new FeedApi())
                .start();
        new MicroservicesRunner()
                .deploy(new FilesApi())
                .start();
        new MicroservicesRunner()
                .deploy(new ConnectToDatabaseApi())
                .start();
    }
}
