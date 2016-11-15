/*
 *  Copyright (c) 2016 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.analytics.engine.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.util.Utils;
import org.wso2.analytics.data.commons.AnalyticsEngine;
import org.wso2.analytics.data.commons.AnalyticsEngineQueryResult;
import org.wso2.analytics.data.commons.exception.AnalyticsException;
import org.wso2.analytics.data.commons.utils.AnalyticsCommonUtils;
import org.wso2.analytics.engine.commons.AnalyzerEngineConstants;
import org.wso2.analytics.engine.commons.SparkAnalyticsEngineQueryResult;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.wso2.analytics.data.commons.sources.AnalyticsCommonConstants.ANALYTICS_CONF_DIR;

public class SparkAnalyticsEngine implements AnalyticsEngine {
    private static final Log log = LogFactory.getLog(SparkAnalyticsEngine.class);

    private SparkSession sparkSession;
    private SparkConf sparkConf;
    private String sparkMaster;
    private String appName;

    public SparkAnalyticsEngine() {
        init();
    }

    private void init() {
        try {
            initSparkConf();
        } catch (AnalyticsException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        //create the spark session
        this.sparkSession = SparkSession.builder().master(this.sparkMaster)
                .appName(appName)
                .config(sparkConf).getOrCreate();
    }

    private void initSparkConf() throws AnalyticsException {
        this.sparkConf = new SparkConf(false);
        String sparkConfFile = AnalyticsCommonUtils.getAnalyticsConfDirectory() + File.separator
                + ANALYTICS_CONF_DIR + File.separator + AnalyzerEngineConstants.SPARK_CONF_FOLDER;
        scala.collection.Map<String, String> properties = Utils.getPropertiesFromFile(sparkConfFile);
        this.sparkConf.setAll(properties);
        // setting spark master
        this.sparkMaster = sparkConf.get(AnalyzerEngineConstants.SPARK_MASTER);
        if (this.sparkMaster == null) {
            this.sparkMaster = "local";
        }
        // setting the app name
        this.appName = sparkConf.get(AnalyzerEngineConstants.SPARK_APP_NAME);
    }

    @Override
    public AnalyticsEngineQueryResult executeQuery(String query) {
        String processedQuery;
        if (query.endsWith(";")) {
            processedQuery = query.substring(0, query.length() - 1).trim();
        }
        processedQuery = query.trim();
        // todo: implement processing incremental queries
        // checkAndProcessIncrementalQuery();

        long start = System.currentTimeMillis();
        boolean success = true;

        Dataset<Row> resultsSet = sparkSession.sql(processedQuery);
        return convertToResult(resultsSet);
    }

    private AnalyticsEngineQueryResult convertToResult(Dataset<Row> results) {
        int resultsLimit = this.sparkConf.getInt("carbon.spark.results.limit", -1);
        if (resultsLimit != -1) {
            return new SparkAnalyticsEngineQueryResult(results.schema().fieldNames(),
                    convertRowsToResult(results.limit(resultsLimit).collectAsList()));
        } else {
            return new SparkAnalyticsEngineQueryResult(results.schema().fieldNames(),
                    convertRowsToResult(results.collectAsList()));
        }
    }

    private List<List<Object>> convertRowsToResult(List<Row> rows) {
        List<List<Object>> result = new ArrayList<>();
        List<Object> objects;
        for (Row row : rows) {
            objects = new ArrayList<>();
            for (int i = 0; i < row.length(); i++) {
                objects.add(row.get(i));
            }
            result.add(objects);
        }
        return result;
    }
}
