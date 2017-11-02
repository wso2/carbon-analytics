package org.wso2.carbon.stream.processor.core.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.stream.processor.common.SiddhiAppRuntimeService;
import org.wso2.siddhi.core.SiddhiAppRuntime;

import java.util.HashMap;
import java.util.Map;

/**
 * This class contains the implementations of the apis required to get SiddhiAppRuntimes
 */
public class CarbonSiddhiAppRuntimeService implements SiddhiAppRuntimeService {

    @Override
    public Map<String, SiddhiAppRuntime> getActiveSiddhiAppRuntimes() {
        Map<String, SiddhiAppData> siddhiApps =
                StreamProcessorDataHolder.getStreamProcessorService().getSiddhiAppMap();
        Map<String, SiddhiAppRuntime> siddhiAppRuntimes = new HashMap<>();
        for (Map.Entry<String, SiddhiAppData> entry : siddhiApps.entrySet()) {
            if (entry.getValue() != null && entry.getValue().isActive()) {
                siddhiAppRuntimes.put(entry.getKey(), entry.getValue().getSiddhiAppRuntime());
            }
        }
        return siddhiAppRuntimes;
    }
}
