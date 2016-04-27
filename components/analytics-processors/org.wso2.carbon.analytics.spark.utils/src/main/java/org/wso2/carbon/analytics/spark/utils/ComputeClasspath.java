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

package org.wso2.carbon.analytics.spark.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * this class creates the spark classpath by looking at the plugins folder
 */
public class ComputeClasspath {
    private static String SEP = System.getProperty("os.name").toLowerCase().contains("win") ? ";" : ":";

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            throw new Exception("Arguments to the main method should not be empty");
        }

        String carbonHome = args[0];
        String sparkClasspath = "";

        System.out.println(getSparkClasspath(sparkClasspath, carbonHome));
    }

    public static String getSparkClasspath(String sparkClasspath, String carbonHome)
            throws IOException {
        if (!isDirectory(carbonHome)) {
            throw new IOException(
                    "CarbonHome specified, does not exists :" + carbonHome);
        }
        return getSparkClasspath(sparkClasspath, carbonHome, new String[0]);
    }

    public static String getSparkClasspath(String sparkClasspath, String carbonHome,
                                           String[] excludeJars)
            throws IOException {
        if (!isDirectory(carbonHome)) {
            throw new IOException(
                    "CarbonHome specified, does not exists :" + carbonHome);
        }

        List<String> requiredJars = getCarbonJars(carbonHome);
        String cp = createInitialSparkClasspath(sparkClasspath, carbonHome, requiredJars, SEP, excludeJars);
        return cp + addJarsFromDropins("", carbonHome, SEP) + addJarsFromLib("", carbonHome, SEP) +
               addJarsFromEndorsedLib("", carbonHome, SEP) + addJarsFromConfig("", carbonHome, SEP);
    }

    private static List<String> getCarbonJars(String carbonHome) throws IOException {
        File jarsFile = new File(carbonHome + File.separator + "repository" + File.separator + "conf"
                                 + File.separator + "analytics" + File.separator + "spark"
                                 + File.separator + "carbon-spark-classpath.conf");

        List<String> result = new ArrayList<>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(jarsFile));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    // skip if a comment or an empty line or does not start with "carbon."
                    continue;
                }
                if (line.endsWith(";")) {
                    line = line.substring(0, line.length());
                }
                result.add(line);
            }
        } finally {
            try {
                assert reader != null;
                reader.close();
            } catch (IOException e) {
                // throw e;
            }
        }
        return result;
    }


    private static String addJarsFromLib(String scp, String carbonHome, String separator) {
        File libDir = new File(carbonHome + File.separator + "repository" + File.separator
                               + "components" + File.separator + "lib");
        File[] libJars = listJars(libDir);
        for (File jar : libJars) {
            scp = scp + separator + jar.getAbsolutePath();
        }
        return scp;
    }

    private static String addJarsFromDropins(String scp, String carbonHome, String separator) {
        File libDir = new File(carbonHome + File.separator + "repository" + File.separator
                               + "components" + File.separator + "dropins");
        File[] libJars = listJars(libDir);
        for (File jar : libJars) {
            scp = scp + separator + jar.getAbsolutePath();
        }
        return scp;
    }

    private static String addJarsFromEndorsedLib(String scp, String carbonHome, String separator) {
        File libDir = new File(carbonHome + File.separator + "lib" + File.separator + "endorsed");
        File[] libJars = listJars(libDir);
        for (File jar : libJars) {
            scp = scp + separator + jar.getAbsolutePath();
        }
        return scp;
    }

    private static String addJarsFromConfig(String scp, String carbonHome, String separator)
            throws IOException {
        File cpFile = new File(carbonHome + File.separator + "repository" + File.separator + "conf"
                               + File.separator + "analytics" + File.separator + "spark"
                               + File.separator + "external-spark-classpath.conf");

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(cpFile));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    // skip if a comment or an empty line or does not start with "carbon."
                    continue;
                }

                if (line.endsWith(";")) {
                    line = line.substring(0, line.length());
                }

                if (fileExists(line)) {
                    scp = scp + separator + line;
                } else if (fileExists(carbonHome + File.separator + line)) {
                    scp = scp + separator + carbonHome + File.separator + line;
                } else {
                    throw new IOException("File not found : " + line);
                }
            }
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
//                        throw e;
                }
            }
        }

        return scp;
    }

    private static boolean fileExists(String path) {
        File tempFile = new File(path);
        return tempFile.exists() && !tempFile.isDirectory() && tempFile.isAbsolute();
    }

    private static boolean isDirectory(String path) {
        File tempFile = new File(path);
        return tempFile.exists() && tempFile.isDirectory();
    }

    private static File[] listJars(File dir) {
        return dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        });
    }

    private static String createInitialSparkClasspath(String sparkClasspath, String carbonHome,
                                                      List<String> requiredJars, String separator,
                                                      String[] excludeJars) {
        File pluginsDir = new File(carbonHome + File.separator + "repository" + File.separator
                                   + "components" + File.separator + "plugins");
        File[] pluginJars = listJars(pluginsDir);

        for (String requiredJar : requiredJars) {
            for (File pluginJar : pluginJars) {
                String plugin = pluginJar.getName();
                String jarName = plugin.split("_")[0];

                if (containsInArray(excludeJars, jarName)) {
                    continue;
                }

                if (jarName.equals(requiredJar)) {
                    if (sparkClasspath.isEmpty()) {
                        sparkClasspath = pluginJar.getAbsolutePath();
                    } else {
                        sparkClasspath = sparkClasspath + separator + pluginJar.getAbsolutePath();
                    }
                }
            }
        }
        return sparkClasspath;
    }

    private static boolean containsInArray(String[] arr, String str) {
        for (String s : arr) {
            if (s.equals(str)) {
                return true;
            }
        }
        return false;
    }
}
