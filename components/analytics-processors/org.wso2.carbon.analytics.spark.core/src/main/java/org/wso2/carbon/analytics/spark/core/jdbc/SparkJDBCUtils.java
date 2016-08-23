/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.analytics.spark.core.jdbc;

import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.AnalyticsDataSourceConstants;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;
import org.wso2.carbon.analytics.spark.core.exception.AnalyticsExecutionException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

import static org.wso2.carbon.analytics.spark.core.util.AnalyticsConstants.SPARK_CONF_DIR;

/**
 * This class contains util methods which facilitate reading the Spark JDBC query configuration file.
 */
public class SparkJDBCUtils {

    private static final String SPARK_JDBC_CONFIG_FILE = "spark-jdbc-config.xml";

    private static SparkJDBCQueryConfiguration carbonJdbcConfig = null;

    public static SparkJDBCQueryConfiguration loadQueryConfiguration() throws AnalyticsException {
        if (SparkJDBCUtils.carbonJdbcConfig != null) {
            return SparkJDBCUtils.carbonJdbcConfig;
        } else {
            try {
                File confFile = new File(GenericUtils.getAnalyticsConfDirectory() + File.separator +
                        AnalyticsDataSourceConstants.ANALYTICS_CONF_DIR + File.separator + SPARK_CONF_DIR +
                        File.separator + SPARK_JDBC_CONFIG_FILE);
                if (!confFile.exists()) {
                    throw new AnalyticsExecutionException("Spark JDBC query configuration file cannot be found at: " +
                            confFile.getPath());
                }
                JAXBContext ctx = JAXBContext.newInstance(SparkJDBCQueryConfiguration.class);
                Unmarshaller unmarshaller = ctx.createUnmarshaller();
                SparkJDBCUtils.carbonJdbcConfig = (SparkJDBCQueryConfiguration) unmarshaller.unmarshal(confFile);
                return SparkJDBCUtils.carbonJdbcConfig;
            } catch (JAXBException e) {
                throw new AnalyticsException("Error in processing Spark JDBC query configuration: " + e.getMessage(), e);
            }
        }
    }
}
