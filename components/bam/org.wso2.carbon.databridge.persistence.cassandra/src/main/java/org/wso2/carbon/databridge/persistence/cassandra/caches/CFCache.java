package org.wso2.carbon.databridge.persistence.cassandra.caches;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import me.prettyprint.hector.api.Cluster;
import org.apache.log4j.Logger;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class CFCache {

    private static final Logger log = Logger.getLogger(CFCache.class);

    private static volatile LoadingCache<ClusterKSCFBean, Boolean> CFCache = null;

    private static void init() {
        if (CFCache != null) {
            return;
        }
        synchronized (CFCache.class) {
            if (CFCache != null) {
                return;
            }
            CFCache = CacheBuilder.newBuilder()
                    .maximumSize(1000)
                    .expireAfterAccess(30, TimeUnit.MINUTES)
                    .build(new CacheLoader<ClusterKSCFBean, Boolean>() {

                        @Override
                        public Boolean load(ClusterKSCFBean clusterKSCFBean) throws Exception {
                            return false;
                        }
                    }
                    );
        }

    }

    public static Boolean getCF(Cluster cluster, String keyspaceName, String columnFamilyName)
            throws ExecutionException {
        init();
        return CFCache.get(new ClusterKSCFBean(cluster, keyspaceName, columnFamilyName));
    }

    public static void putCF(Cluster cluster, String keyspaceName, String columnFamilyName, boolean present) {
        init();
        CFCache.put(new ClusterKSCFBean(cluster, keyspaceName, columnFamilyName), present);
    }


    public static void deleteCF(Cluster cluster, String keyspaceName, String columnFamilyName) {
        init();
        CFCache.invalidate(new ClusterKSCFBean(cluster, keyspaceName, columnFamilyName));
    }
}
