/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.analytics.spark.core.util;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MultiMap;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsServiceHolder;

import java.io.Serializable;
import java.util.Collection;

/**
 * A holder for spark table names
 */
public class SparkTableNamesHolder implements Serializable {

    private static final long serialVersionUID = 7222154767575842994L;

    private MultiMap<Integer, String> sparkTableNames;

    private ListMultimap<Integer, String> inMemSparkTableNames;

    private boolean isClustered = false;

    public SparkTableNamesHolder() {
    }

    public SparkTableNamesHolder(boolean isClustered) {
        this.isClustered = isClustered;
        if (isClustered) {
            HazelcastInstance hz = AnalyticsServiceHolder.getHazelcastInstance();
            this.sparkTableNames = hz.getMultiMap(AnalyticsConstants.TENANT_ID_AND_TABLES_MAP);
        } else {
            this.inMemSparkTableNames = ArrayListMultimap.create();
        }
    }

    public void addTableName(int tenantId, String tableName) {
        if (isClustered) {
            this.sparkTableNames.put(tenantId, tableName);
        } else {
            this.inMemSparkTableNames.put(tenantId, tableName);
        }
    }

    public Collection<String> getTableNames(int tenantId) {
        if (isClustered) {
            return this.sparkTableNames.get(tenantId);
        } else {
            return this.inMemSparkTableNames.get(tenantId);
        }
    }

}
