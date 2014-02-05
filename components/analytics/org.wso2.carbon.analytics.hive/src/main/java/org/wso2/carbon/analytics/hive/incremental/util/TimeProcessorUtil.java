package org.wso2.carbon.analytics.hive.incremental.util;

import org.wso2.carbon.analytics.hive.exception.HiveIncrementalProcessException;

/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class TimeProcessorUtil {

    public static long getTimeStamp(String stringTimeValue) throws HiveIncrementalProcessException {
        long timeStamp = -1;
        if (null != stringTimeValue) {
            stringTimeValue = stringTimeValue.toLowerCase().trim();
            if (stringTimeValue.equals(IncrementalProcessingConstants.CURRENT_TIME)) {
                timeStamp = System.currentTimeMillis();
            }
            else if (stringTimeValue.startsWith(IncrementalProcessingConstants.CURRENT_TIME)) {
                stringTimeValue = stringTimeValue.replaceAll("\\s", "");
                String[] timeSplits = stringTimeValue.split("-");
                if (timeSplits.length == 2) {
                    if (timeSplits[0].equals(IncrementalProcessingConstants.CURRENT_TIME)) {
                        timeStamp = System.currentTimeMillis();
                    } else {
                        throw new HiveIncrementalProcessException("Unsupported time stamp received = "
                                + stringTimeValue);
                    }
                    try{
                    timeStamp = timeStamp - Long.parseLong(timeSplits[1]);
                    }catch (NumberFormatException ex){
                        throw new HiveIncrementalProcessException("Unsupported time stamp received = " +
                                stringTimeValue, ex);
                    }
                } else {

                    throw new HiveIncrementalProcessException("Unsupported time stamp received = " + stringTimeValue);
                }
            } else {
                timeStamp = Long.parseLong(stringTimeValue);
            }
        }
        return timeStamp;
    }

}
