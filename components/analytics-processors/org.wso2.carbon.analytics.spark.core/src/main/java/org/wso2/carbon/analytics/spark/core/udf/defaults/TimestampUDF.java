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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.analytics.spark.core.udf.defaults;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/*
 * This class is an UDF class to support Spark SQL UDFs
 * It returns the epoch time value (of type long) of a date string
 */
public class TimestampUDF {

	public long timestamp(String timestampString) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss zzz");
		Date date = null;
		try {
			date = sdf.parse(timestampString);
		} catch (ParseException e) {
			sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
			try {
				date = sdf.parse(timestampString);
			} catch (ParseException e1) {
				sdf = new SimpleDateFormat("yyyy-MM-dd");
				date = sdf.parse(timestampString);
			}
		}
		long timeInMillisSinceEpoch = date.getTime();
		return timeInMillisSinceEpoch;
	}
}
