/*
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.analytics.dataservice;

import java.util.List;

/**
 * The implementation of {@link AnalyticsDataService}.
 */
public class AnalyticsDataServiceImpl implements AnalyticsDataService {

    @Override
    public void insertEvents(List<AnalyticsEvent> eventList) throws AnalyticsDataServiceException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public List<AnalyticsEvent> getEvents(String streamId, List<String> meta, List<String> correlation,
            List<String> payload, long timeFrom, long timeTo, int recordsFrom, int recordsCount)
            throws AnalyticsDataServiceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<AnalyticsEvent> getEvents(String streamId, List<String> meta, List<String> correlation,
            List<String> payload, List<String> ids) throws AnalyticsDataServiceException {
        // TODO Auto-generated method stub
        return null;
    }

}
