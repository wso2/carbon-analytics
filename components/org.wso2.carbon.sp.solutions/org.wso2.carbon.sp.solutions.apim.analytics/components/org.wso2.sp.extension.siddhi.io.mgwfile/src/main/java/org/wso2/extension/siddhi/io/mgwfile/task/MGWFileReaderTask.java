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
import org.wso2.extension.siddhi.io.mgwfile.MGWFileDataRetriever;
import org.wso2.extension.siddhi.io.mgwfile.MGWFileDataRetrieverThreadFactory;
import org.wso2.extension.siddhi.io.mgwfile.MGWFileSourceDS;
import org.wso2.extension.siddhi.io.mgwfile.dao.MGWFileSourceDAO;
import org.wso2.extension.siddhi.io.mgwfile.dto.MGWFileInfoDTO;
import org.wso2.extension.siddhi.io.mgwfile.exception.MGWFileSourceException;

import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Task for scheduling the usage publishing threads
 */
public class MGWFileReaderTask extends TimerTask {

    private static final Log log = LogFactory.getLog(
            MGWFileReaderTask.class);
    private static int workerThreadCount = Integer.parseInt(MGWFileSourceDS.getWorkerThreadCount());
    private static Executor usagePublisherPool = Executors
            .newFixedThreadPool(workerThreadCount, new MGWFileDataRetrieverThreadFactory("mgwfile-thread"));
    private boolean isPaused = false;

    public MGWFileReaderTask() {
        log.debug("Initializing Uploaded Usage Publisher Executor Task");
    }

    public void setPaused(boolean paused) {
        isPaused = paused;
    }

    @Override
    public void run() {
        try {
            if (!isPaused) {
                List<MGWFileInfoDTO> uploadedFileList = MGWFileSourceDAO.getNextFilesToProcess(workerThreadCount);
                for (MGWFileInfoDTO dto : uploadedFileList) {
                    if (log.isDebugEnabled()) {
                        log.info("Scheduled publishing micro-gateway API Usage data for : " + dto.getFileName());
                    }
                    Runnable worker = new MGWFileDataRetriever(dto);
                    usagePublisherPool.execute(worker);
                }
            } else {
                log.info("Paused publishing micro-gateway API Usage data ");
            }
        } catch (MGWFileSourceException e) {
            log.error("Error occurred while publishing micro-gateway API Usage data.", e);
        }
    }

}
