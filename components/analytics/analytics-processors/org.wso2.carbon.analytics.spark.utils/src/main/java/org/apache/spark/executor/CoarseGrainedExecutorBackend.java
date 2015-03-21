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
import java.util.UUID;

/**
 * a proxy class to mimic the CoarseGrainedExecutorBackend class in spark
 */
public class CoarseGrainedExecutorBackend {
    
    private static final String DIR_RELATIVE_PATH = "../../../repository/data/spark-data";

    public static void main(String[] args) {
        BufferedWriter writer = null;
        try {
            UUID uuid = UUID.randomUUID();
            File destDir = new File(DIR_RELATIVE_PATH);
            if (!destDir.exists()){
                destDir.mkdirs();
            }
            File file = new File(destDir.getPath() + "/" + uuid.toString());
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
    }
}
