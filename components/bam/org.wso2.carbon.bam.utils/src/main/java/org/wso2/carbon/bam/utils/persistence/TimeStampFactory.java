/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.bam.utils.persistence;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulated time stamp creation according to granularity
 */
public class TimeStampFactory {

    private TimeStampFactory() {
        populateGranularityMap();
    }

    private static TimeStampFactory instance = null;

    public static synchronized TimeStampFactory getFactory() {
        if (instance == null) {
            instance = new TimeStampFactory();
        }
        return instance;
    }

    private Map<String, Integer[]> granularityMap = new HashMap<String, Integer[]>();


    private void populateGranularityMap() {
        granularityMap.put("year", new Integer[]{Calendar.MINUTE, Calendar.SECOND, Calendar.HOUR_OF_DAY, Calendar.DAY_OF_MONTH, Calendar.MONTH});
        granularityMap.put("month",new Integer[]{Calendar.MINUTE, Calendar.SECOND, Calendar.HOUR_OF_DAY, Calendar.DAY_OF_MONTH});
        granularityMap.put("day", new Integer[]{Calendar.MINUTE, Calendar.SECOND, Calendar.HOUR_OF_DAY});
        granularityMap.put("hour", new Integer[]{Calendar.MINUTE, Calendar.SECOND});
        granularityMap.put("minute", new Integer[]{Calendar.SECOND});
        granularityMap.put("none", new Integer[]{0});
    }

    public String getTimeStamp(String eventTimeStamp, String granularity)
            throws IllegalArgumentException, ParseException {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (!granularityMap.containsKey(granularity)) {
            throw new IllegalArgumentException("Invalid granularity value - " + granularity);
        }
        Date date = formatter.parse(eventTimeStamp);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        // Reduce values according to granularity
        Integer[] deltaFactors = granularityMap.get(granularity);
        for (Integer deltaFactor : deltaFactors) {
            calendar.add(deltaFactor, calendar.get(deltaFactor) * -1);
        }
        return formatter.format(calendar.getTime());

    }

}

