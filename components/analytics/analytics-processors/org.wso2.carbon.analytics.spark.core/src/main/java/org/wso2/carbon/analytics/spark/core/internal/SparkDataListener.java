/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.analytics.spark.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class listens to the repository/data/spark-data directory for spark data changes
 */
public class SparkDataListener implements Runnable {

    private static final String DIR_RELATIVE_PATH = "repository/data/spark-data";
    
    private static final Log log = LogFactory.getLog(SparkDataListener.class);
    
    private ExecutorService executor = Executors.newCachedThreadPool();
    
    private Set<String> appIds = new HashSet<String>();

    @Override
    public void run() {
        String destFolderPath = CarbonUtils.getCarbonHome() + "/" + DIR_RELATIVE_PATH;
        if (! new File(destFolderPath).exists()) new File(destFolderPath).mkdirs();
        Path dir = Paths.get(destFolderPath);
        try {
            WatchService watcher = FileSystems.getDefault().newWatchService();
            dir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);
            while (true) {
                WatchKey watckKey = watcher.take();
                List<WatchEvent<?>> events = watckKey.pollEvents();
                for (WatchEvent<?> event : events) {
                    if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                        String fileName = event.context().toString();
                        final String[] argArray = getArgArray(Paths.get(destFolderPath, fileName));
                        if (argArray.length == 0) {
                            if (log.isDebugEnabled()) {
                                log.debug("Empty param Spark executor execution");
                            }
                            continue;
                        }
                        String appId = argArray[4];
                        if (this.appIds.contains(appId)) {
                            if (log.isDebugEnabled()) {
                                log.debug("Spark executor already available for app id: " + appId);
                            }
                            continue;
                        } else {
                            this.appIds.add(appId);
                        }
                        new File(destFolderPath + File.separator + fileName).delete();
                        log.info("Starting a Spark executor: " + Arrays.toString(argArray));
                        this.executor.execute(new SparkBackendExecutor(argArray));
                    }
                }
                boolean valid = watckKey.reset();
                if (!valid) {
                    break;
                }
            }
        } catch (Exception e) {
            log.error("Error in SparkDataListener: " + e.toString(), e);
        }
    }

    private static String[] getArgArray(Path filePath) throws IOException {
        List<String> lines = Files.readAllLines(filePath, Charset.defaultCharset());
        return lines.toArray(new String[lines.size()]);
    }
    
}
