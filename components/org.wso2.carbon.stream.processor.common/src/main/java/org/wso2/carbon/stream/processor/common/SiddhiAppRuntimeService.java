package org.wso2.carbon.stream.processor.common;

import org.wso2.siddhi.core.SiddhiAppRuntime;

import java.util.Map;

/**
 * This represents the interface which defines the apis for SiddhiApp Runtimes
 */
public interface SiddhiAppRuntimeService {
    Map<String, SiddhiAppRuntime> getActiveSiddhiAppRuntimes();
}
