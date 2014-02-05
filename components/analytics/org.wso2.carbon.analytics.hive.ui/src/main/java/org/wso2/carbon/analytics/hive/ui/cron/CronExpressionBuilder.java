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

package org.wso2.carbon.analytics.hive.ui.cron;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CronExpressionBuilder {

    private static CronExpressionBuilder instance = new CronExpressionBuilder();

    private CronExpressionBuilder(){

    }

    public static CronExpressionBuilder getInstance(){
        return instance;
    }

    public String getCronExpression(HashMap<String, String> cronValues) {
        String dateCron = getDate(cronValues);
        String timeCron = getTime(cronValues, dateCron);
        return timeCron + " " + dateCron;
    }

    private String getTime(HashMap<String, String> cronValues, String dateCron) {
        String cronMinute = getCronText(cronValues.get(CronBuilderConstants.MINUTES));
        String cronHour = getCronText(cronValues.get(CronBuilderConstants.HOURS));
        String tempCron = cronMinute + " " + cronHour + " " + dateCron;
        String cronSec = "";
        boolean isNumberExists = false;
        Pattern pattern = Pattern.compile("\\d");
		Matcher matcher = pattern.matcher(tempCron);
		if (matcher.find()){
			isNumberExists = true;
		}
        if (!isNumberExists) {
            cronSec = "1";
        } else {
            cronSec = "0";
        }
        return cronSec + " " + cronMinute + " " + cronHour;

    }

    private String getCronText(String text) {
        if (text.equalsIgnoreCase("All")) {
            return "*";
        }
        return text;
    }

    private String getDate(HashMap<String, String> cronValues) {
        String dayMonth = cronValues.get(CronBuilderConstants.DAY_OF_MONTH);
        String cronDayMonth = "";
        String cronDayWeek = "";
        String cronMonth = "";
        String cronYear = "";
        if (null != dayMonth && !dayMonth.equals("")) {
            cronDayMonth = getCronText(dayMonth);
            cronDayWeek = "?";
        } else {
            //dayWeek should be set here
            String dayWeek = cronValues.get(CronBuilderConstants.DAY_OF_WEEK);
            cronDayWeek = getCronText(dayWeek);
            cronDayMonth = "?";
        }
        cronMonth = getCronText(cronValues.get(CronBuilderConstants.MONTH));
        cronYear = getCronText(cronValues.get(CronBuilderConstants.YEAR));
        String dateCron = cronDayMonth + " " + cronMonth + " " + cronDayWeek + " " + cronYear;
        return dateCron;
    }


}
