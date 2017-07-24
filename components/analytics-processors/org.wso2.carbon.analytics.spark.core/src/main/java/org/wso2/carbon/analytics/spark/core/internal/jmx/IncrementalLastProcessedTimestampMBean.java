/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.analytics.spark.core.internal.jmx;

/**
 * Created on 6/6/16.
 *
 * Interface for IncrementalLastProcessedTimestamp MBean.
 * This will expose a last processed timestamp for given spark table id.
 */
public interface IncrementalLastProcessedTimestampMBean {

    /**
     * This will return timestamp of the last processed event of given Spark alias.
     * @param tenantId Tenant ID
     * @param id Alias for Spark table.
     * @param primary Whether from temporary stored value or permanent stored value
     * @return Long.MIN_VALUE for non-exist alias or for event tables still values not there. Permanent stored values for primary true
     * else temporary stored value. -1 for Error situation.
     */
    long getLastProcessedTimestamp(int tenantId, String id, boolean primary);

}
