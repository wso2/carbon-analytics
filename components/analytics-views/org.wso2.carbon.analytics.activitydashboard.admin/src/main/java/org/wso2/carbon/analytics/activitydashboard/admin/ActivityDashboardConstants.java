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
package org.wso2.carbon.analytics.activitydashboard.admin;

/**
 * This class holds the constants used in activity monitoring.
 */
public class ActivityDashboardConstants {
    private ActivityDashboardConstants(){
        //to avoid the instantiation.
    }

    public static final String ACTIVITY_ID_FIELD_NAME = "correlation_activity_id";
    public static final String TIMESTAMP_FIELD = "_timestamp";
    public static final long MAX_ACTIVITY_ID_LIMIT = 10000;
    public static final int MAX_RECORD_COUNT_LIMIT = 100;
}
