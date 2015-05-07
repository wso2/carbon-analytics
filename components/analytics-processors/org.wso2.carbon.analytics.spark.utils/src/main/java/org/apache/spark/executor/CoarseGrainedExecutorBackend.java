package org.apache.spark.executor;
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.UUID;

/**
 * a proxy class to mimic the CoarseGrainedExecutorBackend class in spark
 */
public class CoarseGrainedExecutorBackend {

    private static final String IN_DIR_RELATIVE_PATH = "../../../repository/data/spark-data/in";

    private static final String OUT_DIR_RELATIVE_PATH = "../../../repository/data/spark-data/out";
    private static final String FILE_CONTENT = "EXITED";

//    private static String fileUUID = null;


    public static void main(String[] args) {
        BufferedWriter writer = null;
        String fileUUID = UUID.randomUUID().toString();
        try {
            File destDir = new File(IN_DIR_RELATIVE_PATH);
            if (!destDir.exists()) {
                destDir.mkdirs();
            }
            File file = new File(destDir.getPath() + File.separator + fileUUID);
            writer = new BufferedWriter(new FileWriter(file));
            for (String line : args) {
                writer.write(line + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                assert writer != null;
                writer.close();
            } catch (Exception ignored) {
            }
        }


        String outDirPath = OUT_DIR_RELATIVE_PATH;
        if (!new File(outDirPath).exists()) {
            new File(outDirPath).mkdirs();
        }
        Path dir = Paths.get(outDirPath);
        WatchService watcher = null;
        try {
            watcher = FileSystems.getDefault().newWatchService();
            dir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);
            while (true) {
                WatchKey watckKey = watcher.take();
                List<WatchEvent<?>> events = watckKey.pollEvents();
                boolean finished = false;
                for (WatchEvent<?> event : events) {
                    if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                        String fileName = event.context().toString();
                        if (fileName.equals(fileUUID)) {
                            finished = true;
//                            System.out.println("#################### Coarse Grained Executor dummy ended!");
                            break;
                        }
                    }
                }
                if (finished) {
                    break;
                }
                boolean valid = watckKey.reset();
                if (!valid) {
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("Error in SparkDataListener: " + e.toString());
        }finally {
            try {
                assert watcher != null;
                watcher.close();
            } catch (Exception ignored) {

            }
        }
    }
}
