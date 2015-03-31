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

import org.apache.spark.executor.CoarseGrainedExecutorBackend2;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

/**
 * Creates different threads for each executor
 */
public class SparkBackendExecutor implements Runnable {

    private final String[] argArray;

    private final String fileUUID;

    private static final String OUT_DIR_RELATIVE_PATH = "repository/data/spark-data/out";

    public SparkBackendExecutor(String[] argArray, String fileUUID) {
        this.argArray = argArray;
        this.fileUUID = fileUUID;
    }

    @Override
    public void run() {

        try {
            CoarseGrainedExecutorBackend2.main(argArray);
        } finally {
            System.out.println("######################## Spark backend executor ended!!!!");
            BufferedWriter writer = null;
            try {
                File destDir = new File(OUT_DIR_RELATIVE_PATH);
                if (!destDir.exists()) {
                    destDir.mkdirs();
                }
                File file = new File(destDir.getPath() + File.separator + fileUUID);
                System.out.println("##################### Writing to the file " + file.getName());
                writer = new BufferedWriter(new FileWriter(file));
                writer.write("EXITED" + "\n");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    assert writer != null;
                    writer.close();
                } catch (Exception ignored) {
                }
            }
        }

    }
}
