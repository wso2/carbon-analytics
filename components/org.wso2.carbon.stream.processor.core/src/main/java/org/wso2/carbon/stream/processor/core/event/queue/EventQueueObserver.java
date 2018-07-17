package org.wso2.carbon.stream.processor.core.event.queue;

import org.apache.log4j.Logger;
import org.wso2.carbon.stream.processor.core.internal.SiddhiAppData;
import org.wso2.carbon.stream.processor.core.internal.StreamProcessorDataHolder;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.util.snapshot.PersistenceReference;

import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class EventQueueObserver implements Runnable {

    private static final Logger log = Logger.getLogger(EventQueueObserver.class);

    @Override
    public void run() {
        if (EventQueueManager.getEventQueue().getRemainingCapacity() < 10000) {
            log.error("!!!!!!!!!!!!!!!!!!! Event Queue Full !!!!!!!!!!!!!!!!!!!");
            persistSiddhiApps();
        }
        persistSiddhiApps();
    }

    public void persistSiddhiApps() {
        try {
            Map<String, SiddhiAppData> siddhiAppMap = StreamProcessorDataHolder.getStreamProcessorService().
                    getSiddhiAppMap();
            for (Map.Entry<String, SiddhiAppData> siddhiAppMapEntry : siddhiAppMap.entrySet()) {
                SiddhiAppRuntime siddhiAppRuntime = siddhiAppMapEntry.getValue().getSiddhiAppRuntime();
                if (siddhiAppRuntime != null) {
                    PersistenceReference persistenceReference = siddhiAppRuntime.persist();
                    Future fullStateFuture = persistenceReference.getFullStateFuture();
                    if (fullStateFuture != null) {
                        fullStateFuture.get(StreamProcessorDataHolder.getDeploymentConfig().getLiveSync().getStateSyncTimeout(),
                                TimeUnit.MILLISECONDS);
                    } else {
                        for (Future future : persistenceReference.getIncrementalStateFuture()) {
                            future.get(StreamProcessorDataHolder.getDeploymentConfig().getLiveSync().getStateSyncTimeout(),
                                    TimeUnit.MILLISECONDS);
                        }
                    }
                } else {
                    log.error("Active Node: Persisting of Siddhi app " + siddhiAppMapEntry.getValue() +
                            " not successful. Check if app deployed properly");
                }
            }
        } catch (Exception e) {
            log.error("Error while persisting siddhi applications. " + e.getMessage(), e);
        }
        log.info("Active Node: Persisting of all Siddhi Applications on request of passive node successful");
    }
}
