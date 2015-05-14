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
package org.wso2.carbon.analytics.activitydashboard.admin.bean;

/**
 * A bean class to be used to transfer the object via the admin services.
 */
public class RecordBean {

    private RecordId id;
    private ColumnEntry[] entries;
    private long timeStamp;

    public ColumnEntry[] getColumnEntries() {
        return entries;
    }

    public void setColumnEntries(ColumnEntry[] entries) {
        this.entries = entries;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public RecordId getId() {
        return id;
    }

    public void setId(RecordId id) {
        this.id = id;
    }

}
