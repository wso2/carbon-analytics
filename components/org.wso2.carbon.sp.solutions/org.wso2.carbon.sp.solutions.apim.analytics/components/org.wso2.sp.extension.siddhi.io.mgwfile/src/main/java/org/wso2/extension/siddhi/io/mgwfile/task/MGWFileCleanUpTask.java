/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.extension.siddhi.io.mgwfile.task;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.extension.siddhi.io.mgwfile.MGWFileSourceDS;
import org.wso2.extension.siddhi.io.mgwfile.dao.MGWFileSourceDAO;
import org.wso2.extension.siddhi.io.mgwfile.exception.MGWFileSourceException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimerTask;

/**
 * Task for cleaning uploaded old usage files in db
 */
public class MGWFileCleanUpTask extends TimerTask {

    private static final Log log = LogFactory.getLog(MGWFileCleanUpTask.class);
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");


    /**
     * Return the {@link Date} up to which files should be retained
     *
     * @param fileRetentionDays No of days to retain the files
     * @return {@link Date} up to which files should be retained
     */
    private Date getLastKeptDate(int fileRetentionDays) {
        Date myDate = new Date(System.currentTimeMillis());
        Calendar cal = Calendar.getInstance();
        cal.setTime(myDate);
        cal.add(Calendar.DATE, -fileRetentionDays);
        return cal.getTime();
    }

    @Override
    public void run() {
        log.debug("UploadUsage Cleaning task started");
        String fileRetentionDays = MGWFileSourceDS.getFileRetentionDays();
        Date lastKeptDate = getLastKeptDate(Integer.parseInt(fileRetentionDays));
        log.info("Uploaded API Usage data in the db will be cleaned up to : " + dateFormat.format(lastKeptDate));
        try {
            MGWFileSourceDAO.deleteProcessedOldFiles(lastKeptDate);
        } catch (MGWFileSourceException e) {
            log.error("Error occurred while cleaning the uploaded usage data.", e);
        }
    }

}
