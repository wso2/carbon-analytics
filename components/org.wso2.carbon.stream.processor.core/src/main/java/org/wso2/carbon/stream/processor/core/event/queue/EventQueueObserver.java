package org.wso2.carbon.stream.processor.core.event.queue;

import org.apache.log4j.Logger;
import org.wso2.carbon.stream.processor.core.ha.transport.TCPNettyClient;
import org.wso2.carbon.stream.processor.core.internal.SiddhiAppData;
import org.wso2.carbon.stream.processor.core.internal.StreamProcessorDataHolder;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.util.snapshot.PersistenceReference;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class EventQueueObserver implements Runnable {

    private static final Logger log = Logger.getLogger(EventQueueObserver.class);
    TCPNettyClient tcpNettyClient = new TCPNettyClient();

    @Override
    public void run() {
        log.error("!!!!!!!!!!!!!!!!!!! Event Queue ******** !!!!!!!!!!!!!!!!!!!");
        if (EventQueueManager.getEventQueue().getRemainingCapacity() < 18000) {
            log.error("!!!!!!!!!!!!!!!!!!! Event Queue Full !!!!!!!!!!!!!!!!!!!");
            persistSiddhiApps();
        }
    }

    public void persistSiddhiApps() {
        try {
            Map<String, SiddhiAppData> siddhiAppMap = StreamProcessorDataHolder.getStreamProcessorService().
                    getSiddhiAppMap();
            String[] siddhiRevisionArray = new String[siddhiAppMap.size()];
            int siddhiAppCount = 0;
            for (Map.Entry<String, SiddhiAppData> siddhiAppMapEntry : siddhiAppMap.entrySet()) {
                SiddhiAppRuntime siddhiAppRuntime = siddhiAppMapEntry.getValue().getSiddhiAppRuntime();
                if (siddhiAppRuntime != null) {
                    PersistenceReference persistenceReference = siddhiAppRuntime.persist();
                    siddhiRevisionArray[siddhiAppCount] = persistenceReference.getRevision();
                    siddhiAppCount++;
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
            if (!tcpNettyClient.isActive()) {
                tcpNettyClient.connect("localhost", 9892);
            }
            String siddhiAppRevisions = Arrays.toString(siddhiRevisionArray);
            tcpNettyClient.send("controlMessage", siddhiAppRevisions.getBytes());
        } catch (ConnectionUnavailableException e) {
            log.error("Error in connecting to 'localhost' and '9892' of the Passive node");
        } catch (Exception e) {
            log.error("Error while persisting siddhi applications. " + e.getMessage(), e);
        }
        log.info("Active Node: Persisting of all Siddhi Applications on request of passive node successful");
    }
}
