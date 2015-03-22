/**
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.analytics.dataservice;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.IteratorUtils;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.RecordGroup;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;

/**
 * This class contains utility methods for analytics data service.
 */
public class AnalyticsDSUtils {

    @SuppressWarnings("unchecked")
    public static List<Record> listRecords(AnalyticsDataService ads,
            RecordGroup[] rgs) throws AnalyticsException {
        List<Record> result = new ArrayList<Record>();
        for (RecordGroup rg : rgs) {
            result.addAll(IteratorUtils.toList(ads.readRecords(rg)));
        }
        return result;
    }
    
}
