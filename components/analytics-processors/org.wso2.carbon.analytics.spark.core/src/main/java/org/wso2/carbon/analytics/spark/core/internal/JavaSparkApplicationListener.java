package org.wso2.carbon.analytics.spark.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.JavaSparkListener;
import org.apache.spark.scheduler.SparkListenerApplicationEnd;
import org.apache.spark.scheduler.SparkListenerApplicationStart;

public class JavaSparkApplicationListener extends JavaSparkListener {
    private static final Log log = LogFactory.getLog(JavaSparkApplicationListener.class);
    private String applicationName;

    @Override
    public void onApplicationStart(SparkListenerApplicationStart arg0) {
        applicationName = arg0.appName();
        log.info("Spark application '" + arg0.appName() + "' with id '"
                + arg0.appId().get() + "' started successfully");
        ServiceHolder.setSparkContextRestartRequired(false);
    }

    @Override
    public void onApplicationEnd(SparkListenerApplicationEnd arg0) {
        log.error("Spark application '" + applicationName + "' ended.");
        ServiceHolder.setSparkContextRestartRequired(true);
    }
}
