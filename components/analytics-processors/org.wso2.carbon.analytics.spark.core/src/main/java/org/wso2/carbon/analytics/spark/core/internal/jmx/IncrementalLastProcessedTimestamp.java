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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.spark.core.internal.ServiceHolder;

/**
 * Created on 6/6/16.
 *
 * Implementation of the IncrementalLastProcessedTimestampMBean.
 */
public class IncrementalLastProcessedTimestamp implements IncrementalLastProcessedTimestampMBean {

    private static final Log LOG = LogFactory.getLog(IncrementalLastProcessedTimestamp.class);

    @Override
    public long getLastProcessedTimestamp(int tenantId, String id, boolean primary) {
        try {
            return ServiceHolder.getIncrementalMetaStore().getLastProcessedTimestamp(tenantId, id, primary);
        } catch (Exception e) {
            LOG.error("Unable to get last processed timestamp for " + tenantId + "-" + id + ": " + e.getMessage(), e);
            return -1;
        }
    }
}