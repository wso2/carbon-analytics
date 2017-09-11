package org.wso2.carbon.stream.processor.statistic.api;

import org.wso2.msf4j.MicroservicesRunner;

/**
 * Application entry point.
 *
 * @since 1.0.0-SNAPSHOT
 */
public class Application {
    public static void main(String[] args) {
        new MicroservicesRunner()
                .deploy(new SystemDetailsApi())
                .start();
        new MicroservicesRunner()
                .deploy(new StatisticsApi())
                .start();
    }
}
