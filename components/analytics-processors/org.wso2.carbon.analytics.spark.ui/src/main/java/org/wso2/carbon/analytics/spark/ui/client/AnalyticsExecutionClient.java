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
package org.wso2.carbon.analytics.spark.ui.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.lang.StringEscapeUtils;
import org.wso2.carbon.analytics.spark.admin.stub.AnalyticsProcessorAdminServiceAnalyticsProcessorAdminExceptionException;
import org.wso2.carbon.analytics.spark.admin.stub.AnalyticsProcessorAdminServiceStub;

import java.rmi.RemoteException;

public class AnalyticsExecutionClient {
    private AnalyticsProcessorAdminServiceStub stub;

    public AnalyticsExecutionClient(String cookie,
                                    String backEndServerURL,
                                    ConfigurationContext configCtx) throws AxisFault {
        String serviceURL = backEndServerURL + "AnalyticsProcessorAdminService";
        stub = new AnalyticsProcessorAdminServiceStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        int timeout = 60 * 60 * 1000; // 1 hour
        stub._getServiceClient().getOptions().setTimeOutInMilliSeconds(timeout);
        stub._getServiceClient().getOptions().setProperty(
                HTTPConstants.SO_TIMEOUT, timeout);
        stub._getServiceClient().getOptions().setProperty(
                HTTPConstants.CONNECTION_TIMEOUT, timeout);
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setTimeOutInMilliSeconds(timeout);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    public AnalyticsExecutionClient() {

    }

    public String execute(String query){
        query = StringEscapeUtils.unescapeHtml(query);
        AnalyticsProcessorAdminServiceStub.AnalyticsQueryResultDto queryResult;
        try {
            queryResult = stub.executeQuery(query);
        } catch (AnalyticsProcessorAdminServiceAnalyticsProcessorAdminExceptionException e) {
            e.printStackTrace();
            return errorToJson(query, e.getFaultMessage().getAnalyticsProcessorAdminException().getMessage());
        } catch (Throwable e){
            e.printStackTrace();
            return errorToJson(query, e.getMessage());
        }
        return resultToJson(query, queryResult);
    }

    private String errorToJson(String query, String message) {
        JsonObject resObj = new JsonObject();

        JsonObject meta = new JsonObject();
        meta.addProperty("code", 400);
        meta.addProperty("responseMessage", "ERROR EXECUTING QUERY : " + query);
        JsonArray colArray = new JsonArray();

        colArray.add(new JsonPrimitive("ERROR"));

        meta.add("columns", colArray);
        resObj.add("meta", meta);

        JsonObject response = new JsonObject();
        JsonArray rows = new JsonArray();
        JsonArray singleRow = new JsonArray();
        singleRow.add(new JsonPrimitive(message));
        rows.add(singleRow);

        response.add("items", rows);
        resObj.add("response", response);

        return resObj.toString();
    }

    public AnalyticsProcessorAdminServiceStub.AnalyticsQueryResultDto[] executeScriptContent(String scriptContent) throws RemoteException,
            AnalyticsProcessorAdminServiceAnalyticsProcessorAdminExceptionException {
        return stub.execute(scriptContent);
    }

    public void executeScriptContentInBackground(String scriptContent) throws RemoteException,
            AnalyticsProcessorAdminServiceAnalyticsProcessorAdminExceptionException {
        stub.executeInBackground(scriptContent);
    }

    public AnalyticsProcessorAdminServiceStub.AnalyticsQueryResultDto[] executeScript(String scriptName)
            throws RemoteException, AnalyticsProcessorAdminServiceAnalyticsProcessorAdminExceptionException {
        return stub.executeScript(scriptName);
    }

    public void executeScriptInBackground(String scriptName)
            throws RemoteException, AnalyticsProcessorAdminServiceAnalyticsProcessorAdminExceptionException {
        stub.executeScriptInBackground(scriptName);
    }

    public void deleteScript(String scriptName) throws RemoteException,
            AnalyticsProcessorAdminServiceAnalyticsProcessorAdminExceptionException {
        stub.deleteScript(scriptName);
    }

    public void saveScript(String scriptName, String scriptContent, String cron) throws RemoteException,
            AnalyticsProcessorAdminServiceAnalyticsProcessorAdminExceptionException {
        stub.saveScript(scriptName, scriptContent, cron);
    }

    public void pauseScripts() throws RemoteException,
            AnalyticsProcessorAdminServiceAnalyticsProcessorAdminExceptionException {
        stub.pauseAllScripts();
    }

    public void resumeScripts() throws RemoteException,
            AnalyticsProcessorAdminServiceAnalyticsProcessorAdminExceptionException {
        stub.resumeAllScripts();
    }

    public AnalyticsProcessorAdminServiceStub.AnalyticsScriptDto[] getAllScripts() throws RemoteException,
            AnalyticsProcessorAdminServiceAnalyticsProcessorAdminExceptionException {
        return stub.getAllScripts();
    }

    public AnalyticsProcessorAdminServiceStub.AnalyticsScheduledScriptDto[]
        getScheduledTaskStatuses() throws RemoteException,
            AnalyticsProcessorAdminServiceAnalyticsProcessorAdminExceptionException {
        return stub.getScheduledTaskStatuses();
    }

    public AnalyticsProcessorAdminServiceStub.AnalyticsScriptDto getScriptContent(String scriptName)
            throws RemoteException, AnalyticsProcessorAdminServiceAnalyticsProcessorAdminExceptionException {
        return stub.getScript(scriptName);
    }

    public void updateScript(String scriptName, String scriptContent, String cron) throws
            RemoteException, AnalyticsProcessorAdminServiceAnalyticsProcessorAdminExceptionException {
        String currentSavedContent = null;
        if (scriptContent != null) {
            currentSavedContent = stub.getScript(scriptName).getScriptContent();
            stub.updateScriptContent(scriptName, scriptContent);
        }
        if (cron != null) {
            try {
                stub.updateScriptTask(scriptName, cron);
            } catch (Exception ex) {
                if (currentSavedContent != null && !currentSavedContent.trim().isEmpty()) {
                    stub.updateScriptContent(scriptName, currentSavedContent);
                }
                throw new AnalyticsProcessorAdminServiceAnalyticsProcessorAdminExceptionException(ex.getMessage(), ex);
            }
        }
    }

    public String resultToJson(String query, AnalyticsProcessorAdminServiceStub.AnalyticsQueryResultDto res) {
        JsonObject resObj = new JsonObject();

        JsonObject meta = new JsonObject();
        meta.addProperty("code", 200);
        meta.addProperty("responseMessage", "EXECUTED QUERY : " + query);
        JsonArray colArray = new JsonArray();

        if (res != null && res.getColumnNames() != null && !res.getQuery().trim().toLowerCase().startsWith("insert")) {
            for (String col : res.getColumnNames()) {
                if (col != null) {
                    colArray.add(new JsonPrimitive(col));
                }
            }
        }
        meta.add("columns", colArray);
        resObj.add("meta", meta);

        JsonObject response = new JsonObject();
        JsonArray rows = new JsonArray();

        if (res != null && res.getRowsResults() != null) {
            for (AnalyticsProcessorAdminServiceStub.AnalyticsRowResultDto row : res.getRowsResults()) {
                if (row != null) {
                    JsonArray singleRow = new JsonArray();
                    for (String elm : row.getColumnValues()) {
                        if (elm != null) {
                            singleRow.add(new JsonPrimitive(elm));
                        } else {
                            singleRow.add(new JsonPrimitive("NULL"));
                        }
                    }
                    rows.add(singleRow);
                }
            }
        }

        response.add("items", rows);
        resObj.add("response", response);

        return resObj.toString();
    }


    public boolean isAnalyticsExecutionEnabled() throws RemoteException {
        return stub.isAnalyticsExecutionEnabled();
    }

    public boolean isAnalyticsScriptExecuting(String scriptName) throws RemoteException {
        return stub.isAnalyticsScriptExecuting(scriptName);
    }

    public boolean isAnalyticsTaskExecuting(String scriptName) throws RemoteException, AnalyticsProcessorAdminServiceAnalyticsProcessorAdminExceptionException {
        return stub.isAnalyticsTaskExecuting(scriptName);
    }
}
