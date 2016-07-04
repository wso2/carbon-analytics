/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.analytics.datasource.cassandra;

import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;

import java.util.Map;

/**
 * This class contains utility methods related to the Cassandra data connector.
 */
public class CassandraUtils {

    public static String[] splitParentChild(String path) {
        if (path.equals("/")) {
            return new String[] { ".", "/" };
        }
        int index = path.lastIndexOf('/');
        String child = path.substring(index + 1);
        String parent = path.substring(0, index);
        if (parent.length() == 0) {
            parent = "/";
        }
        return new String[] { GenericUtils.normalizePath(parent), child };
    }
    
    public static String generateCreateKeyspaceQuery(String ks, 
            Map<String, String> properties) throws AnalyticsException {
        String className = extractClass(properties);
        int replicationFactor = extractReplicationFactor(properties);
        return "CREATE KEYSPACE IF NOT EXISTS " + ks + " WITH REPLICATION = {'class':'" + className + 
                "', 'replication_factor':" + replicationFactor + "}";
    }
    
    public static String extractDataSourceName(Map<String, String> properties) throws AnalyticsException {
        String dsName = properties.get(CassandraConstants.DATASOURCE_NAME);
        if (dsName == null) {
            throw new AnalyticsException("The Cassandra connector property '" + CassandraConstants.DATASOURCE_NAME +
                    "' is mandatory");
        }
        return dsName;
    }
    
    public static String extractAFSKSName(Map<String, String> properties) {
        String ks = properties.get(CassandraConstants.KEYSPACE);
        if (ks == null) {
            ks = CassandraConstants.DEFAULT_AFS_KS_NAME;
        }
        return ks;
    }
    
    public static String extractARSKSName(Map<String, String> properties) {
        String ks = properties.get(CassandraConstants.KEYSPACE);
        if (ks == null) {
            ks = CassandraConstants.DEFAULT_ARS_KS_NAME;
        }
        return ks;
    }
    
    public static String extractClass(Map<String, String> properties) {
        String className = properties.get(CassandraConstants.CLASS);
        if (className == null) {
            className = CassandraConstants.DEFAULT_CLASS;
        }
        return className;
    }
    
    public static int extractReplicationFactor(Map<String, String> properties) throws AnalyticsException {
        String rfStr = properties.get(CassandraConstants.REPLICATION_FACTOR);
        int rf;
        if (rfStr == null) {
            rf = CassandraConstants.DEFAULT_REPLICATION_FACTOR;
        } else {
            try {
                rf = Integer.parseInt(rfStr);
            } catch (NumberFormatException e) {
                throw new AnalyticsException("Invalid " + CassandraConstants.REPLICATION_FACTOR + ": " + rfStr);
            }
        }
        return rf;
    }
    
}
