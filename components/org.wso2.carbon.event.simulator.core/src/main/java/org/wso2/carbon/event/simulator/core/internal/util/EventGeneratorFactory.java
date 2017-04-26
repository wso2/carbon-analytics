package org.wso2.carbon.event.simulator.core.internal.util;

import org.json.JSONObject;
import org.wso2.carbon.event.simulator.core.exception.InsufficientAttributesException;
import org.wso2.carbon.event.simulator.core.exception.InvalidConfigException;
import org.wso2.carbon.event.simulator.core.internal.generator.EventGenerator;

/**
 * Factory class used to create event generators
 */
public interface EventGeneratorFactory {

    EventGenerator createEventGenerator(JSONObject sourceConfig, long startTimestamp, long endTimestamp)
            throws InvalidConfigException;

    void validateGeneratorConfiguration(JSONObject sourceConfig) throws InvalidConfigException,
            InsufficientAttributesException;
}
