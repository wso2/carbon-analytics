/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.event.publisher.core.internal.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.wso2.carbon.event.publisher.core.exception.EventPublisherConfigurationException;
import org.wso2.carbon.event.publisher.core.internal.ds.EventPublisherServiceValueHolder;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class RuntimeResourceLoader {

    private static final long DEFAULT_TIMEOUT_DURATION = 15;
    private final LoadingCache<String, String> cache;
    private Map<String, Integer> propertyPositionMap = null;

    public RuntimeResourceLoader(long cacheTimeoutDuration, Map<String, Integer> propertyPositionMap) {


        this.propertyPositionMap = propertyPositionMap;

        if (cacheTimeoutDuration < 0) {
            cacheTimeoutDuration = DEFAULT_TIMEOUT_DURATION;
        }

        CacheLoader<String, String> loader = new CacheLoader<String, String>() {
            @Override
            public String load(String path) throws EventPublisherConfigurationException {
                return EventPublisherServiceValueHolder.getCarbonEventPublisherService().getRegistryResourceContent(path);
            }
        };
        // Default time unit is minute
        this.cache = CacheBuilder.newBuilder().expireAfterWrite(cacheTimeoutDuration, TimeUnit.MINUTES).build(loader);
    }


    public String getResourceContent(String path) throws EventPublisherConfigurationException {
        try {
            return cache.get(path);
        } catch (ExecutionException e) {
            throw new EventPublisherConfigurationException("Error in getting cached resource", e);
        }
    }
}
