package org.wso2.carbon.databridge.persistence.cassandra.caches;

import me.prettyprint.hector.api.Cluster;

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
public class ClusterKSCFBean {
    private final Cluster cluster;
    private final String keyspaceName;
    private final String columnFamilyName;

    public ClusterKSCFBean(Cluster cluster, String keyspaceName, String columnFamilyName) {
        this.cluster = cluster;
        this.keyspaceName = keyspaceName;
        this.columnFamilyName = columnFamilyName;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public String getKeyspaceName() {
        return keyspaceName;
    }

    public String getColumnFamilyName() {
        return columnFamilyName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClusterKSCFBean that = (ClusterKSCFBean) o;

        if (!cluster.equals(that.cluster)) return false;
        if (!columnFamilyName.equals(that.columnFamilyName)) return false;
        if (!keyspaceName.equals(that.keyspaceName)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = cluster.hashCode();
        result = 31 * result + keyspaceName.hashCode();
        result = 31 * result + columnFamilyName.hashCode();
        return result;
    }
}
