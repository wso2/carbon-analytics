/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.analytics.eventsink;

import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.eventsink.exception.AnalyticsEventStoreException;

/**
 * This is the interface for the OSGI service exposed to do event store/sink related operations.
 */
public interface AnalyticsEventSinkService {

    void putEventSink(int tenantId, String streamName, String version, AnalyticsSchema analyticsSchema,
                      String recordStoreName) throws AnalyticsEventStoreException;

    void putEventSinkWithSchemaMergeInfo(int tenantId, String streamName, String version, AnalyticsSchema analyticsSchema,
                      String recordStoreName, boolean isMergeSchema) throws AnalyticsEventStoreException;

    void putEventStore(int tenantId, AnalyticsEventStore eventStore) throws AnalyticsEventStoreException;

    void putEventStoreWithSchemaMerge(int tenantId, AnalyticsEventStore analyticsEventStore)
            throws AnalyticsEventStoreException;

    void removeEventSink(int tenantId, String streamName, String version) throws AnalyticsEventStoreException;

    AnalyticsEventStore getEventStore(int tenantId, String streamName);

    String generateAnalyticsTableName(String streamName);

}
