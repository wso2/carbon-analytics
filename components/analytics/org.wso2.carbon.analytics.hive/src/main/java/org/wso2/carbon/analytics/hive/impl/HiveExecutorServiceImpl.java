/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.analytics.hive.impl;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.HiveContext;
import org.wso2.carbon.analytics.hive.HiveConstants;
import org.wso2.carbon.analytics.hive.dto.QueryResult;
import org.wso2.carbon.analytics.hive.dto.QueryResultRow;
import org.wso2.carbon.analytics.hive.dto.ScriptResult;
import org.wso2.carbon.analytics.hive.exception.AnalyzerConfigException;
import org.wso2.carbon.analytics.hive.exception.HiveExecutionException;
import org.wso2.carbon.analytics.hive.exception.RegistryAccessException;
import org.wso2.carbon.analytics.hive.extension.AnalyzerContext;
import org.wso2.carbon.analytics.hive.extension.HiveAnalyzer;
import org.wso2.carbon.analytics.hive.extension.AbstractHiveAnalyzer;
import org.wso2.carbon.analytics.hive.extension.AnalyzerMeta;
import org.wso2.carbon.analytics.hive.extension.util.AnalyzerHolder;
import org.wso2.carbon.analytics.hive.incremental.IncrementalProcessingAnalyzer;
import org.wso2.carbon.analytics.hive.incremental.util.IncrementalProcessingConstants;
import org.wso2.carbon.analytics.hive.service.HiveExecutorService;
import org.wso2.carbon.analytics.hive.util.RegistryAccessUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.*;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HiveExecutorServiceImpl implements HiveExecutorService {

    private static final Log log = LogFactory.getLog(HiveExecutorServiceImpl.class);

    private static boolean IS_PROFILING_ENABLED = false;

    private static DateFormat dateFormat = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
    
    private static HiveConf hiveConf = new HiveConf();

    static {
        try {
            Class.forName("org.apache.hadoop.hive.jdbc.HiveDriver");
        } catch (ClassNotFoundException e) {
            log.fatal("Hive JDBC Driver not found in the class path. Hive query execution will" +
                    " fail..", e);
        }

        try {
            IS_PROFILING_ENABLED = Boolean.parseBoolean(System.getProperty(
                    HiveConstants.ENABLE_PROFILE_PROPERTY));
        } catch (Exception ignored) {
            // Ignore malformed switch values and defaults to false
        }
    }

    /**
     * @param script
     * @return The Resultset of all executed queries in the script
     * @throws HiveExecutionException
     */
    public QueryResult[] execute(String scriptName, String script) throws HiveExecutionException {
        PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (script != null) {

            ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();

            // Parse script to bind registry value
            try {
                script = RegistryAccessUtil.parseScriptForRegistryValues(script);
            } catch (RegistryAccessException e) {
                log.error("Error during parsing registry values ...", e);
                throw new HiveExecutionException(e.getExceptionMessage(), e);
            }

            ScriptCallable callable = new ScriptCallable(tenantId,scriptName,  script);

            Future<ScriptResult> future = singleThreadExecutor.submit(callable);

            ScriptResult result;
            try {
                result = future.get();
            } catch (InterruptedException e) {
                log.error("Query execution interrupted..", e);
                throw new HiveExecutionException("Query execution interrupted..", e);
            } catch (ExecutionException e) {
                log.error("Error during query execution..", e);
                throw new HiveExecutionException("Error during query execution..", e);
            }

            if (result != null) {
                if (result.getErrorMessage() != null) {
                    throw new HiveExecutionException(result.getErrorMessage());
                }

                removeUTFCharsFromValues(result);

                return result.getQueryResults();

            } else {
                throw new HiveExecutionException("Query returned a NULL result..");
            }

        }
        return null;

    }

    private void removeUTFCharsFromValues(ScriptResult result) {
        QueryResult[] queryResults = result.getQueryResults();
        for (QueryResult queryResult : queryResults) {
            QueryResultRow[] resultRows = queryResult.getResultRows();
            for (QueryResultRow queryResultRow : resultRows) {
                String[] columnValues = queryResultRow.getColumnValues();
                String[] columnValuesWithoutUTFChars = new String[columnValues.length];
                for (int i = 0; i < columnValues.length; i++) {
                    columnValuesWithoutUTFChars[i] = columnValues[i].replaceAll("[\\u0000]", "");
                }
                queryResultRow.setColumnValues(columnValuesWithoutUTFChars);
            }

        }
    }

    /**
     * @param script
     * @return The Resultset of all executed queries in the script
     * @throws HiveExecutionException
     */
    public QueryResult[] execute(int tenantId, String scriptName, String script) throws HiveExecutionException {
        if (script != null) {

            ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();

            ScriptCallable callable = new ScriptCallable(tenantId, scriptName, script);

            Future<ScriptResult> future = singleThreadExecutor.submit(callable);

            ScriptResult result;
            try {
                result = future.get();
            } catch (InterruptedException e) {
                log.error("Query execution interrupted..", e);
                throw new HiveExecutionException("Query execution interrupted..", e);
            } catch (ExecutionException e) {
                log.error("Error during query execution..", e);
                throw new HiveExecutionException("Error during query execution..", e);
            }

            if (result != null) {
                if (result.getErrorMessage() != null) {
                    throw new HiveExecutionException(result.getErrorMessage());
                }

                return result.getQueryResults();

            } else {
                throw new HiveExecutionException("Query returned a NULL result..");
            }

        }

        return null;

    }

    private class ScriptCallable implements Callable<ScriptResult> {

        private static final String HIVE_SERVER_PASSWORD = "hive.server.password";

		private static final String HIVE_SERVER_USER = "hive.server.user";

		private static final String HIVE_SERVER_URL = "hive.server.url";

		private static final String LOCAL_HIVE_URL = "jdbc:hive://";

        private String script;
        private String scriptName;
        private int tenantId;

        public ScriptCallable(int tenantId, String scriptName, String script) {
            this.script = script;
            if (null == scriptName){
                this.scriptName = UUID.randomUUID().toString();
            }else {
                this.scriptName = scriptName;
            }
            this.tenantId = tenantId;
        }

        public ScriptResult call() throws HiveExecutionException {

            HiveContext.startTenantFlow(tenantId);

            Connection con;
            try {
                con = getConnection();
            } catch (SQLException e) {
                log.error("Error getting connection to any listed remote Hive meta stores..", e);

                ScriptResult result = new ScriptResult();
                result.setErrorMessage("Error getting connection to any" +
                        " listed remote Hive meta stores..." + e);
                return result;
            }

            Statement stmt;
            try {
                stmt = con.createStatement();
            } catch (SQLException e) {
                log.error("Error getting statement..", e);

                ScriptResult result = new ScriptResult();
                result.setErrorMessage("Error getting statement." + e.getMessage());
                return result;
            }

            try {
                String newScript = script;

                String formattedScript = formatScript(newScript);

                String[] cmdLines = formattedScript.split(";\\r?\\n|;"); // Tokenize with ;[new-line]

                ScriptResult result = new ScriptResult();


                Date startDateTime = null;
                long startTime = 0;
                if (IS_PROFILING_ENABLED) {
                    startTime = System.currentTimeMillis();
                    startDateTime = new Date();
                }

                for (String cmdLine : cmdLines) {

                    String trimmedCmdLine = cmdLine.trim();
                    trimmedCmdLine = trimmedCmdLine.replaceAll(";", "");
                    trimmedCmdLine = trimmedCmdLine.replaceAll("%%", ";");

                    newScript = newScript.replaceAll("\n", " ");
                    newScript = newScript.replaceAll("\t", " ");

                    if (trimmedCmdLine.startsWith("class") ||
                            trimmedCmdLine.startsWith("CLASS")) { // Class analyzer for executing custom logic
                        executeClassAnalyzer(trimmedCmdLine);

                    } else if (trimmedCmdLine.startsWith("analyzer") ||    // custom analyzer for executing custom logic with parameters
                            trimmedCmdLine.startsWith("ANALYZER")) {
                        executeAnalyzer(trimmedCmdLine);

                    } else if (trimmedCmdLine.startsWith("@")) {      //annotation found
                        if (trimmedCmdLine.toLowerCase().startsWith(HiveConstants.INCREMENTAL_ANNOTATION.toLowerCase())) {
                            executeIncrementalAnnotation(result, trimmedCmdLine, stmt);
                        } else {
                            throw new HiveExecutionException("Unsupported Annotation. Only" +
                                    " @Incremental is supported as annotation.");
                        }
                    } else { // Normal hive query
                        executeHiveQuery(result, trimmedCmdLine, stmt);
                    }
                }

                StringBuffer sb = new StringBuffer();
                if (IS_PROFILING_ENABLED) {
                    long endTime = System.currentTimeMillis();
                    long duration = endTime - startTime;

                    long seconds = duration / 1000;

                    long secondsInMinute = seconds % 60;
                    long minutesInHours = ((seconds % 3600) / 60);
                    long hoursInDays = ((seconds % 86400) / 3600);
                    long daysInYears = ((seconds % 2592000) / 86400);
                    long months = seconds / 2592000;

                    sb.append("Start Time : " + dateFormat.format(startDateTime) + "\n");
                    sb.append("End Time : " + dateFormat.format(new Date()) + "\n");
                    sb.append("Duration [MM/DD hh:mm:ss] : " + months + "/" + daysInYears + " " +
                            hoursInDays + ":" + minutesInHours + ":" + secondsInMinute + "\n");
                    sb.append("==========================================\n");

                    synchronized (this) {
                        File file = new File(CarbonUtils.getCarbonHome() + File.separator +
                                "analyzer-perf.txt");

                        try {
                            OutputStream stream = null;
                            try {
                                stream = new BufferedOutputStream(new FileOutputStream(file, true));
                                IOUtils.copy(IOUtils.toInputStream(sb.toString()), stream);
                            } finally {
                                IOUtils.closeQuietly(stream);
                            }
                        } catch (IOException e) {
                            log.error(e.getMessage(), e);
                        }
                    }
                }

                return result;

            } catch (SQLException e) {
                log.error("Error while executing Hive script.\n" + e.getMessage(), e);

                ScriptResult result = new ScriptResult();
                result.setErrorMessage("Error while executing Hive script." + e.getMessage());

                return result;
            } catch (AnalyzerConfigException e) {
                ScriptResult result = new ScriptResult();
                result.setErrorMessage("Error while executing Hive analyzer." + e.getMessage());

                return result;
            } finally {
            	HiveContext.endTenantFlow();
                if (null != con) {
                    try {
                        con.close();
                    } catch (SQLException e) {
                    }
                }
            }

        }

        private void executeIncrementalAnnotation(ScriptResult result, String trimmedCmdLine, Statement stmt)
                throws HiveExecutionException, SQLException {
            HashMap<String, String> incrParameters = new HashMap<String, String>();

            int closeBraceIndex = trimmedCmdLine.indexOf(")");
            String incrConf = trimmedCmdLine.substring(0, closeBraceIndex + 1);
            String hiveQuery = trimmedCmdLine.substring(closeBraceIndex + 1);

            if (hiveQuery == null || incrConf == null) {
                String errorMessage = "Error in incremental Annotation syntax!";
                throw new HiveExecutionException(errorMessage);
            }

            String regEx = "(\\(.*\\))";

            String[] tokensName = incrConf.trim().split(regEx);


            if (tokensName == null) {
                String errorMessage = "Error in processing analyzer command " + trimmedCmdLine;
                throw new HiveExecutionException(errorMessage);
            }

            Pattern pattern = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher matcher = pattern.matcher(incrConf.trim());
            if (matcher.find()) {
                String rBraces = matcher.group(1);
                int temp = rBraces.trim().length();

                String s = rBraces.substring(1, temp - 1);

                String[] variables = s.split(",(?=(?:(?:[^\\\"]*\\\"){2})*[^\\\"]*$)");

                for (String variable : variables) {
                    String[] values = variable.split("=");
                    String key = values[0].trim();
                    String valueTemp = values[1].trim();
                    String value = valueTemp.substring(1, valueTemp.length() - 1);
                    incrParameters.put(key, value);
                }
            }
            if (incrParameters.isEmpty()) {
                String errorMessage = "Incremental Annotation is empty!";
                throw new HiveExecutionException(errorMessage);
            }

            IncrementalProcessingAnalyzer incrAnalyzer =
                    new IncrementalProcessingAnalyzer(tenantId);
            incrParameters.put(IncrementalProcessingConstants.SCRIPT_NAME, scriptName);
            incrAnalyzer.setParameters(incrParameters);
            incrAnalyzer.execute();
            boolean executionSuccess = false;
            try {
                if (incrAnalyzer.isValidToRunQuery()) {
                    executeHiveQuery(result, hiveQuery, stmt);
                }
                executionSuccess = true;
            }finally {
                if (executionSuccess) {
                    incrAnalyzer.finalizeExecution();
                }
                incrAnalyzer.cleanUp();
            }
        }

        private void executeAnalyzer(String trimmedCmdLine) throws AnalyzerConfigException {
            String name;
            String conf;
            HashMap<String, String> parameters = new HashMap<String, String>();
            String[] tokens = trimmedCmdLine.split(HiveConstants.ANALYZER_KEY);
            if (tokens != null && tokens.length >= 2) {
                conf = tokens[1];
            } else {
                String errorMessage = "Error in parameter parsing: " + trimmedCmdLine;
                throw new AnalyzerConfigException(errorMessage);
            }

            String regEx = "(\\(.*\\))";

            String[] tokensName = conf.trim().split(regEx);


            if (tokensName != null) {
                name = tokensName[0];
            } else {
                String errorMessage = "Error in processing analyzer command " + trimmedCmdLine;
                throw new AnalyzerConfigException(errorMessage);
            }

            Pattern pattern = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher matcher = pattern.matcher(conf.trim());
            if (matcher.find()) {
                String rBraces = matcher.group(1);
                int temp = rBraces.trim().length();

                String s = rBraces.substring(1, temp - 1);

                //There may commas witin the values also, therefore not spliiting with comma's and
                // splitting with below gegular expression
                String[] variables = s.split(",(?=(?:(?:[^\\\"]*\\\"){2})*[^\\\"]*$)");

                for (String variable : variables) {
                    String[] values = variable.split("=");
                    String key = values[0].trim();
                    String valueTemp = values[1].trim();
                    String value = valueTemp.substring(1, valueTemp.length() - 1);
                    parameters.put(key, value);
                }
            }

            String className;

            if (AnalyzerHolder.isAnalyzer(name)) {
                AnalyzerMeta analyzerMeta = AnalyzerHolder.getAnalyzer(name);

                Set<String> paramValues = analyzerMeta.getParameters();
                Set<String> params = parameters.keySet();
                className = analyzerMeta.getClassAnalyzer();
                if (paramValues.size() != params.size() || !paramValues.containsAll(params)) {
                    log.warn("No variables defined under this analyzer or variable definition mismatch...");
                }
            } else {
                log.warn("No alias found and loading as a analyzer class ..");
                className = name;
            }

            AnalyzerContext analyzerContext = new AnalyzerContext();

            analyzerContext.setAnalyzerName(name);
            analyzerContext.setParameters(parameters);

            @SuppressWarnings("rawtypes")
			Class clazz;
            try {
                clazz = Class.forName(className, true,
                        this.getClass().getClassLoader());
            } catch (ClassNotFoundException e) {
                log.error("Unable to find custom analyzer class..", e);
                return;
            }

            if (clazz != null) {
                Object analyzer;
                try {
                    analyzer = clazz.newInstance();
                } catch (InstantiationException e) {
                    log.error("Unable to instantiate custom analyzer class..", e);
                    return;
                } catch (IllegalAccessException e) {
                    log.error("Unable to instantiate custom analyzer class..", e);
                    return;
                }

                if (analyzer instanceof HiveAnalyzer) {
                    HiveAnalyzer hiveAnalyzer =
                            (HiveAnalyzer) analyzer;

                    hiveAnalyzer.execute(analyzerContext);
                } else {
                    log.error("Custom analyzers should extend HiveAnalyzer..");
                }

            }
        }

        private String formatScript(String script) {
            Pattern regex1 = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");
            Matcher regexMatcher = regex1.matcher(script);
            String formattedScript = "";
            while (regexMatcher.find()) {
                String temp;
                if (regexMatcher.group(1) != null) {
                    // Add double-quoted string without the quotes
                    temp = regexMatcher.group(1).replaceAll(";", "%%");
                    if (temp.contains("%%")) {
                        temp = temp.replaceAll(" ", "");
                        temp = temp.replaceAll("\n", "");
                    }
                    temp = "\"" + temp + "\"";
                } else if (regexMatcher.group(2) != null) {
                    // Add single-quoted string without the quotes
                    temp = regexMatcher.group(2).replaceAll(";", "%%");
                    if (temp.contains("%%")) {
                        temp = temp.replaceAll(" ", "");
                        temp = temp.replaceAll("\n", "");
                    }
                    temp = "\'" + temp + "\'";
                } else {
                    temp = regexMatcher.group();
                }
                formattedScript += temp + " ";
            }
            return formattedScript;
        }

		private Connection getConnection() throws SQLException {
			try {
				String connectionURL = hiveConf.getStrings(HIVE_SERVER_URL, LOCAL_HIVE_URL)[0];
				String user = hiveConf.getStrings(HIVE_SERVER_USER, "")[0];
				String password = hiveConf.getStrings(HIVE_SERVER_PASSWORD, "")[0];
				return DriverManager.getConnection(connectionURL, user, password);
			} catch (SQLException e) {
				log.error("Error connecting to local Hive: " + e.getMessage(), e);
				throw e;
			}
		}


        private void executeHiveQuery(ScriptResult result, String trimmedCmdLine, Statement stmt) throws SQLException {
        	if (trimmedCmdLine == null || trimmedCmdLine.length() == 0) {
        		return;
        	}
        	
            QueryResult queryResult = new QueryResult();

            queryResult.setQuery(trimmedCmdLine);

            //Append the tenant ID to query
            //trimmedCmdLine += Utils.TENANT_ID_SEPARATOR_CHAR_SEQ + tenantId;

            ResultSet rs = stmt.executeQuery(trimmedCmdLine);
            ResultSetMetaData metaData = rs.getMetaData();

            int columnCount = metaData.getColumnCount();
            List<String> columnsList = new ArrayList<String>();
            for (int i = 1; i <= columnCount; i++) {
                columnsList.add(metaData.getColumnName(i));
            }

            queryResult.setColumnNames(columnsList.toArray(new String[]{}));

            List<QueryResultRow> results = new ArrayList<QueryResultRow>();
            while (rs.next()) {
                QueryResultRow resultRow = new QueryResultRow();

                List<String> columnValues = new ArrayList<String>();

                int noOfColumns = rs.getMetaData().getColumnCount();

                boolean isTombstone = true;
                for (int k = 1; k <= noOfColumns; k++) {
                    Object obj = rs.getObject(k);
                    if (obj != null) {
                        isTombstone = false;
                        break;
                    }
                }

                if (!isTombstone) {
                    Object resObject = rs.getObject(1);
                    if (resObject.toString().contains("\t")) {
                        columnValues = Arrays.asList(resObject.toString().split("\t"));
                    } else {
                        for (int i = 1; i <= columnCount; i++) {
                            Object resObj = rs.getObject(i);
                            if (null != resObj) {
                                columnValues.add(rs.getObject(i).toString());
                            } else {
                                columnValues.add("");
                            }
                        }
                    }
                    resultRow.setColumnValues(columnValues.toArray(new String[]{}));

                    results.add(resultRow);
                }

            }

            queryResult.setResultRows(results.toArray(new QueryResultRow[]{}));
            result.addQueryResult(queryResult);
        }

        private void executeClassAnalyzer(String trimmedCmdLine) throws HiveExecutionException {
            String[] tokens = trimmedCmdLine.split("\\s+");
            if (tokens != null && tokens.length >= 2) {
                String className = tokens[1];

                @SuppressWarnings("rawtypes")
				Class clazz = null;
                try {
                    clazz = Class.forName(className, true,
                            this.getClass().getClassLoader());
                } catch (ClassNotFoundException e) {
                    log.error("Unable to find custom analyzer class..", e);
                }

                if (clazz != null) {
                    Object analyzer = null;
                    try {
                        analyzer = clazz.newInstance();
                    } catch (InstantiationException e) {
                        log.error("Unable to instantiate custom analyzer class..", e);
                    } catch (IllegalAccessException e) {
                        log.error("Unable to instantiate custom analyzer class..", e);
                    }

                    if (analyzer instanceof AbstractHiveAnalyzer) {
                        AbstractHiveAnalyzer hiveAnalyzer =
                                (AbstractHiveAnalyzer) analyzer;
                        hiveAnalyzer.execute();
                    } else {
                        log.error("Custom analyzers should extend AbstractHiveAnalyzer..");
                    }
                }
            }
        }

    }

}
