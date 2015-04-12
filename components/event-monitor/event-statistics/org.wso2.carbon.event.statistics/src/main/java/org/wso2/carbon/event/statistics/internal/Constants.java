/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.event.statistics.internal;

public class Constants {

    public static final long SEC_IN_MS = 1000;
    public static final long MIN_IN_MS = 1000 * 60;
    public static final long MIN15_IN_MS = 1000 * 60 * 15;
    public static final long HOUR_IN_MS = 1000 * 60 * 60;
    public static final long HOUR6_IN_MS = 1000 * 60 * 60 * 6;
    public static final long DAY_IN_MS = 1000 * 60 * 60 * 24;

    public static final String TENANT = "tenant";
    public static final String CATEGORY = "category";
    public static final String DEPLOYMENT = "deployment";
    public static final String ELEMENT = "element";

    // Carbon XML properties
    public static final String STAT_CONFIG_ELEMENT = "EventStat";
    public static final String STAT_REPORTING_INTERVAL = STAT_CONFIG_ELEMENT + ".ReportingInterval";
    public static final String STAT_OBSERVERS = STAT_CONFIG_ELEMENT + ".Observers";


}
