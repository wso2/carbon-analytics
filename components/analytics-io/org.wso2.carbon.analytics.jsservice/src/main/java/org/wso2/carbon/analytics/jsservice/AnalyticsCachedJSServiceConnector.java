/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.analytics.jsservice;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.Weigher;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MaxSizeConfig;
import com.hazelcast.config.MaxSizeConfig.MaxSizePolicy;
import com.hazelcast.core.HazelcastInstance;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.wso2.carbon.analytics.jsservice.beans.ResponseBean;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * This is a thin wrapper on top of {@link AnalyticsJSServiceConnector} to provide caching functionality.
 */
public class AnalyticsCachedJSServiceConnector extends AnalyticsJSServiceConnector {
    
    private AnalyticsCache cache;
    
    public AnalyticsCachedJSServiceConnector(AnalyticsCache cache) {
        super();
        this.cache = cache;
    }
    
    private String calculateCacheItemId(String... keys) {
        StringBuilder builder = new StringBuilder();
        for (String key : keys) {
            builder.append(key + "_");
        }
        return builder.toString();
    }
    
    private ResponseBean lookupCachedValue(String cacheItemId) {
        return this.cache.lookupCachedValue(cacheItemId);
    }
    
    private void setCachedValue(String cacheItemId, ResponseBean result) {
        this.cache.setCachedValue(cacheItemId, result);
    }
    
    @Override
    public ResponseBean getRecordCount(String username, String tableName) {
        String cacheItemId = this.calculateCacheItemId("getRecordCount", tableName);
        ResponseBean result = this.lookupCachedValue(cacheItemId);
        if (result == null) {
            result = super.getRecordCount(username, tableName);
            this.setCachedValue(cacheItemId, result);
        }
        return result;
    }

    @Override
    public ResponseBean getRecordCount(int tenantId, String tableName) {
        String cacheItemId = this.calculateCacheItemId("getRecordCount", tableName);
        ResponseBean result = this.lookupCachedValue(cacheItemId);
        if (result == null) {
            result = super.getRecordCount(tenantId, tableName);
            this.setCachedValue(cacheItemId, result);
        }
        return result;
    }
    
    @Override
    public ResponseBean getRecordsByRange(String username, String tableName, String timeFrom, String timeTo, String recordsFrom,
            String count, String columns) {
        String cacheItemId = this.calculateCacheItemId("getRecordsByRange", tableName, timeFrom, timeTo, recordsFrom, count, columns);
        ResponseBean result = this.lookupCachedValue(cacheItemId);
        if (result == null) {
            result = super.getRecordsByRange(username, tableName, timeFrom, timeTo, recordsFrom, count, columns);
            this.setCachedValue(cacheItemId, result);
        }
        return result;
    }

    @Override
    public ResponseBean getRecordsByRange(int tenantId, String tableName, String timeFrom, String timeTo, String recordsFrom,
                                          String count, String columns) {
        String cacheItemId = this.calculateCacheItemId("getRecordsByRange", tableName, timeFrom, timeTo, recordsFrom, count, columns);
        ResponseBean result = this.lookupCachedValue(cacheItemId);
        if (result == null) {
            result = super.getRecordsByRange(tenantId, tableName, timeFrom, timeTo, recordsFrom, count, columns);
            this.setCachedValue(cacheItemId, result);
        }
        return result;
    }
    
    @Override
    public ResponseBean getWithKeyValues(String username, String tableName, String valuesBatch) {
        String cacheItemId = this.calculateCacheItemId("getWithKeyValues", tableName, valuesBatch);
        ResponseBean result = this.lookupCachedValue(cacheItemId);
        if (result == null) {
            result = super.getWithKeyValues(username, tableName, valuesBatch);
            this.setCachedValue(cacheItemId, result);
        }
        return result;
    }

    @Override
    public ResponseBean getWithKeyValues(int tenantId, String tableName, String valuesBatch) {
        String cacheItemId = this.calculateCacheItemId("getWithKeyValues", tableName, valuesBatch);
        ResponseBean result = this.lookupCachedValue(cacheItemId);
        if (result == null) {
            result = super.getWithKeyValues(tenantId, tableName, valuesBatch);
            this.setCachedValue(cacheItemId, result);
        }
        return result;
    }

    @Override
    public ResponseBean getRecordsByIds(String username, String tableName, String idsAsString) {
        String cacheItemId = this.calculateCacheItemId("getRecordsByIds", tableName, idsAsString);
        ResponseBean result = this.lookupCachedValue(cacheItemId);
        if (result == null) {
            result = super.getRecordsByIds(username, tableName, idsAsString);
            this.setCachedValue(cacheItemId, result);
        }
        return result;
    }

    @Override
    public ResponseBean getRecordsByIds(int tenantId, String tableName, String idsAsString) {
        String cacheItemId = this.calculateCacheItemId("getRecordsByIds", tableName, idsAsString);
        ResponseBean result = this.lookupCachedValue(cacheItemId);
        if (result == null) {
            result = super.getRecordsByIds(tenantId, tableName, idsAsString);
            this.setCachedValue(cacheItemId, result);
        }
        return result;
    }
    
    @Override
    public ResponseBean search(String username, String tableName, String queryAsString) {
        String cacheItemId = this.calculateCacheItemId("search", tableName, queryAsString);
        ResponseBean result = this.lookupCachedValue(cacheItemId);
        if (result == null) {
            result = super.search(username, tableName, queryAsString);
            this.setCachedValue(cacheItemId, result);
        }
        return result;
    }

    @Override
    public ResponseBean search(int tenantId, String tableName, String queryAsString) {
        String cacheItemId = this.calculateCacheItemId("search", tableName, queryAsString);
        ResponseBean result = this.lookupCachedValue(cacheItemId);
        if (result == null) {
            result = super.search(tenantId, tableName, queryAsString);
            this.setCachedValue(cacheItemId, result);
        }
        return result;
    }

    @Override
    public ResponseBean searchWithAggregates(String username, String tableName, String requestAsString) {
        String cacheItemId = this.calculateCacheItemId("searchWithAggregates", tableName, requestAsString);
        ResponseBean result = this.lookupCachedValue(cacheItemId);
        if (result == null) {
            result = super.searchWithAggregates(username, tableName, requestAsString);
            this.setCachedValue(cacheItemId, result);
        }
        return result;
    }

    @Override
    public ResponseBean searchWithAggregates(int tenantId, String tableName, String requestAsString) {
        String cacheItemId = this.calculateCacheItemId("searchWithAggregates", tableName, requestAsString);
        ResponseBean result = this.lookupCachedValue(cacheItemId);
        if (result == null) {
            result = super.searchWithAggregates(tenantId, tableName, requestAsString);
            this.setCachedValue(cacheItemId, result);
        }
        return result;
    }

    @Override
    public ResponseBean searchMultiTablesWithAggregates(String username, String requestAsString) {
        String cacheItemId = this.calculateCacheItemId("searchMultiTablesWithAggregates", requestAsString);
        ResponseBean result = this.lookupCachedValue(cacheItemId);
        if (result == null) {
            result = super.searchMultiTablesWithAggregates(username, requestAsString);
            this.setCachedValue(cacheItemId, result);
        }
        return result;
    }

    @Override
    public ResponseBean searchMultiTablesWithAggregates(int tenantId, String requestAsString) {
        String cacheItemId = this.calculateCacheItemId("searchMultiTablesWithAggregates", requestAsString);
        ResponseBean result = this.lookupCachedValue(cacheItemId);
        if (result == null) {
            result = super.searchMultiTablesWithAggregates(tenantId, requestAsString);
            this.setCachedValue(cacheItemId, result);
        }
        return result;
    }

    @Override
    public ResponseBean searchCount(String username, String tableName, String queryAsString) {
        String cacheItemId = this.calculateCacheItemId("searchCount", tableName, queryAsString);
        ResponseBean result = this.lookupCachedValue(cacheItemId);
        if (result == null) {
            result = super.searchCount(username, tableName, queryAsString);
            this.setCachedValue(cacheItemId, result);
        }
        return result;
    }

    @Override
    public ResponseBean searchCount(int tenantId, String tableName, String queryAsString) {
        String cacheItemId = this.calculateCacheItemId("searchCount", tableName, queryAsString);
        ResponseBean result = this.lookupCachedValue(cacheItemId);
        if (result == null) {
            result = super.searchCount(tenantId, tableName, queryAsString);
            this.setCachedValue(cacheItemId, result);
        }
        return result;
    }

    @Override
    public ResponseBean getTableSchema(String username, String tableName) {
        String cacheItemId = this.calculateCacheItemId("getTableSchema", tableName);
        ResponseBean result = this.lookupCachedValue(cacheItemId);
        if (result == null) {
            result = super.getTableSchema(username, tableName);
            this.setCachedValue(cacheItemId, result);
        }
        return result;
    }

    @Override
    public ResponseBean getTableSchema(int tenantId, String tableName) {
        String cacheItemId = this.calculateCacheItemId("getTableSchema", tableName);
        ResponseBean result = this.lookupCachedValue(cacheItemId);
        if (result == null) {
            result = super.getTableSchema(tenantId, tableName);
            this.setCachedValue(cacheItemId, result);
        }
        return result;
    }

    @Override
    public ResponseBean isPaginationSupported(String recordStoreName) {
        String cacheItemId = this.calculateCacheItemId("isPaginationSupported", recordStoreName);
        ResponseBean result = this.lookupCachedValue(cacheItemId);
        if (result == null) {
            result = super.isPaginationSupported(recordStoreName);
            this.setCachedValue(cacheItemId, result);
        }
        return result;
    }

    @Override
    public ResponseBean drillDownCategories(String username, String tableName, String queryAsString) {
        String cacheItemId = this.calculateCacheItemId("drillDownCategories", tableName, queryAsString);
        ResponseBean result = this.lookupCachedValue(cacheItemId);
        if (result == null) {
            result = super.drillDownCategories(username, tableName, queryAsString);
            this.setCachedValue(cacheItemId, result);
        }
        return result;
    }

    @Override
    public ResponseBean drillDownCategories(int tenantId, String tableName, String queryAsString) {
        String cacheItemId = this.calculateCacheItemId("drillDownCategories", tableName, queryAsString);
        ResponseBean result = this.lookupCachedValue(cacheItemId);
        if (result == null) {
            result = super.drillDownCategories(tenantId, tableName, queryAsString);
            this.setCachedValue(cacheItemId, result);
        }
        return result;
    }
    
    @Override
    public ResponseBean drillDownSearch(String username, String tableName, String queryAsString) {
        String cacheItemId = this.calculateCacheItemId("drillDownSearch", tableName, queryAsString);
        ResponseBean result = this.lookupCachedValue(cacheItemId);
        if (result == null) {
            result = super.drillDownSearch(username, tableName, queryAsString);
            this.setCachedValue(cacheItemId, result);
        }
        return result;
    }

    @Override
    public ResponseBean drillDownSearch(int tenantId, String tableName, String queryAsString) {
        String cacheItemId = this.calculateCacheItemId("drillDownSearch", tableName, queryAsString);
        ResponseBean result = this.lookupCachedValue(cacheItemId);
        if (result == null) {
            result = super.drillDownSearch(tenantId, tableName, queryAsString);
            this.setCachedValue(cacheItemId, result);
        }
        return result;
    }

    @Override
    public ResponseBean drillDownSearchCount(String username, String tableName, String queryAsString) {
        String cacheItemId = this.calculateCacheItemId("drillDownSearchCount", tableName, queryAsString);
        ResponseBean result = this.lookupCachedValue(cacheItemId);
        if (result == null) {
            result = super.drillDownSearchCount(username, tableName, queryAsString);
            this.setCachedValue(cacheItemId, result);
        }
        return result;
    }

    @Override
    public ResponseBean drillDownSearchCount(int tenantId, String tableName, String queryAsString) {
        String cacheItemId = this.calculateCacheItemId("drillDownSearchCount", tableName, queryAsString);
        ResponseBean result = this.lookupCachedValue(cacheItemId);
        if (result == null) {
            result = super.drillDownSearchCount(tenantId, tableName, queryAsString);
            this.setCachedValue(cacheItemId, result);
        }
        return result;
    }

    @Override
    public ResponseBean drillDownRangeCount(String username, String tableName, String queryAsString) {
        String cacheItemId = this.calculateCacheItemId("drillDownRangeCount", tableName, queryAsString);
        ResponseBean result = this.lookupCachedValue(cacheItemId);
        if (result == null) {
            result = super.drillDownRangeCount(username, tableName, queryAsString);
            this.setCachedValue(cacheItemId, result);
        }
        return result;
    }

    @Override
    public ResponseBean drillDownRangeCount(int tenantId, String tableName, String queryAsString) {
        String cacheItemId = this.calculateCacheItemId("drillDownRangeCount", tableName, queryAsString);
        ResponseBean result = this.lookupCachedValue(cacheItemId);
        if (result == null) {
            result = super.drillDownRangeCount(tenantId, tableName, queryAsString);
            this.setCachedValue(cacheItemId, result);
        }
        return result;
    }

    /**
     * This represents the cache used to hold the data.
     */
    public static class AnalyticsCache {
        
        private static final String ENABLE_DISTRIBUTED_WEB_SERVICE_CONNECTOR_CACHE = "enableDistributedWebServiceConnectorCache";

        private static final String CACHE_MAP_NAME = "CarbonAnalyticsWebServiceConnectorCache";

        private int cacheTimeoutSeconds;
        
        private int cacheSizeBytes;
        
        private ConcurrentMap<String, byte[]> cache;
        
        public AnalyticsCache(int cacheTimeoutSeconds, int cacheSizeBytes) {
            this.cacheTimeoutSeconds = cacheTimeoutSeconds;
            this.cacheSizeBytes = cacheSizeBytes;
            this.initCache();
        }

        public int getCacheTimeoutSeconds() {
            return cacheTimeoutSeconds;
        }
        
        public int getCacheSizeBytes() {
            return cacheSizeBytes;
        }

        private void initCache() {
            HazelcastInstance hz = this.loadHazelcast();
            String enableDCache = System.getProperty(ENABLE_DISTRIBUTED_WEB_SERVICE_CONNECTOR_CACHE);
            if (hz != null && enableDCache != null) {
                this.initHzCacheMapConfig(hz);
                this.cache = hz.getMap(CACHE_MAP_NAME);
            } else {
                this.cache = CacheBuilder.newBuilder().maximumWeight(
                        this.getCacheSizeBytes()).weigher(
                            new Weigher<String, byte[]>() {
                                public int weigh(String key, byte[] value) {
                                    return value.length;
                                }
                            }).expireAfterWrite(this.getCacheTimeoutSeconds(), 
                        TimeUnit.SECONDS).build().asMap();
            }
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        private HazelcastInstance loadHazelcast() {
            BundleContext ctx = FrameworkUtil.getBundle(AnalyticsCachedJSServiceConnector.class).getBundleContext();
            ServiceReference ref = ctx.getServiceReference(HazelcastInstance.class);
            if (ref != null) {
                return (HazelcastInstance) ctx.getService(ref);
            } else {
                return null;
            }
        }
        
        private void initHzCacheMapConfig(HazelcastInstance hz) {
            Map<String, MapConfig> mapConfigs = hz.getConfig().getMapConfigs();
            if (mapConfigs == null) {
                mapConfigs = new HashMap<String, MapConfig>();
                hz.getConfig().setMapConfigs(mapConfigs);
            }
            MapConfig config = new MapConfig();
            config.setTimeToLiveSeconds(this.getCacheTimeoutSeconds());
            config.setMaxSizeConfig(new MaxSizeConfig(this.getCacheSizeBytes(), MaxSizePolicy.PER_NODE));
            mapConfigs.put(CACHE_MAP_NAME, config);        
        }
        
        private byte[] serialize(ResponseBean obj) {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            try {
                ObjectOutputStream out = new ObjectOutputStream(byteOut);
                out.writeObject(obj);
                out.close();
                return byteOut.toByteArray();
            } catch (IOException e) {
                throw new IllegalStateException("Error in serializing: " + e.getMessage(), e);
            }
        }
        
        private ResponseBean deserialize(byte[] buff) {
            ByteArrayInputStream byteIn = new ByteArrayInputStream(buff);
            ObjectInputStream objIn = null;
            try {
                objIn = new ObjectInputStream(byteIn);
                return (ResponseBean) objIn.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new IllegalStateException("Error in deserializing: " + e.getMessage(), e);
            } finally {
                if (objIn != null) {
                    try {
                        objIn.close();
                    } catch (IOException ignore) {
                        /* ignore */
                    }
                }
            }
        }
        
        private ResponseBean lookupCachedValue(String cacheItemId) {
            byte[] buff = this.cache.get(cacheItemId);
            if (buff == null) {
                return null;
            }
            return this.deserialize(buff);
        }
        
        private void setCachedValue(String cacheItemId, ResponseBean result) {
            this.cache.put(cacheItemId, this.serialize(result));
        }
        
    }

}
