/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
*
*/
package org.wso2.carbon.analytics.api;

import org.apache.commons.collections.IteratorUtils;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDataResponse;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AnalyticsDataAPIUtil {
    public static List<Record> listRecords(AnalyticsDataAPI ads,
                                           AnalyticsDataResponse response) throws AnalyticsException {
        List<Record> result = new ArrayList<Record>();
        for (AnalyticsDataResponse.Entry entry : response.getEntries()) {
            result.addAll(IteratorUtils.toList(ads.readRecords(entry.getRecordStoreName(), entry.getRecordGroup())));
        }
        return result;
    }
    public static Iterator<Record> responseToIterator(AnalyticsDataAPI service, AnalyticsDataResponse response)
            throws AnalyticsException {
        return new AnalyticsDataAPIUtil.ResponseIterator(service, response);
    }

    /**
     * This class exposes an {@link AnalyticsDataResponse} as a record iterator.
     */
    public static class ResponseIterator implements Iterator<Record> {

        private AnalyticsDataResponse.Entry[] entries;

        private Iterator<Record> itr;

        private AnalyticsDataAPI service;

        private int index = -1;

        public ResponseIterator(AnalyticsDataAPI service, AnalyticsDataResponse response)
                throws AnalyticsException {
            this.service = service;
            this.entries = response.getEntries().toArray(new AnalyticsDataResponse.Entry[0]);
        }

        @Override
        public boolean hasNext() {
            boolean result;
            if (this.itr == null) {
                result = false;
            } else {
                result = this.itr.hasNext();
            }
            if (result) {
                return true;
            } else {
                if (this.entries.length > this.index + 1) {
                    try {
                        this.index++;
                        this.itr = this.service.readRecords(this.entries[index].getRecordStoreName(),
                                this.entries[index].getRecordGroup());
                    } catch (AnalyticsException e) {
                        throw new IllegalStateException("Error in traversing record group: " + e.getMessage(), e);
                    }
                    return this.hasNext();
                } else {
                    return false;
                }
            }
        }

        @Override
        public Record next() {
            if (this.hasNext()) {
                return this.itr.next();
            } else {
                return null;
            }
        }

        @Override
        public void remove() {
            /* ignored */
        }
    }

}
