package org.wso2.carbon.analytics.datasource.core.rs;

/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.RecordGroup;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;

import java.util.Iterator;

/**
 * This interface uses to get Record iterator from RecordGroup object.
 */
public interface AnalyticsRecordReader {

    /**
     * Reads in the records from a given record group, the records will be streamed in.
     *
     * @param recordGroup The record group which represents the local data set
     * @return An iterator of type {@link org.wso2.carbon.analytics.datasource.commons.Record} in the local record group
     * @throws org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException
     */
    Iterator<Record> readRecords(RecordGroup recordGroup) throws AnalyticsException;
}
