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

import org.apache.spark.executor.CoarseGrainedExecutorBackend;

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
import java.util.List;


/**
 * Created by niranda on 3/12/15.
 */
public class SparkDataListener /*implements Runnable */ {

    public static void main(String[] args) {
        run();
    }


    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p/>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
//    @Override
    public static void run() {
//    public void run() {
        String destFolderPath = "/home/niranda/wso2/bam-m1/wso2bam-3.0.0-SNAPSHOT/repository/data/spark-data";

        //define a folder root
        Path dir = Paths.get(destFolderPath);


        try {
            WatchService watcher = FileSystems.getDefault().newWatchService();
            dir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE,
                         StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);

            for (; ; ) {
                WatchKey watckKey = watcher.take();
                List<WatchEvent<?>> events = watckKey.pollEvents();
                for (WatchEvent event : events) {
                    if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE
//                        || event.kind() == StandardWatchEventKinds.ENTRY_DELETE
                        || event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {

                        String fileName = event.context().toString();
                        System.out.println("changed: " + fileName);

                        String[] argArray = getArgArray(Paths.get(destFolderPath, fileName));
//                        for (String line : argArray) {
//                            System.out.println(line);
//                        }
                        CoarseGrainedExecutorBackend.main(argArray);
                        break;
                    }
                }
                boolean valid = watckKey.reset();
                if (!valid) {
                    break;
                }
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.toString());
        }


    }

    private static String[] getArgArray(Path filePath) throws IOException {
        List<String> lines = Files.readAllLines(filePath, Charset.defaultCharset());
        return lines.toArray(new String[lines.size()]);

    }
}
