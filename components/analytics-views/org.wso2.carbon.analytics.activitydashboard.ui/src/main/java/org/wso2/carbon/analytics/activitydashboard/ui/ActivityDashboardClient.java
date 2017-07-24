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
package org.wso2.carbon.analytics.activitydashboard.ui;

import org.apache.axiom.attachments.ByteArrayDataSource;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.activitydashboard.stub.ActivityDashboardAdminServiceActivityDashboardException;
import org.wso2.carbon.analytics.activitydashboard.stub.ActivityDashboardAdminServiceActivityDashboardExceptionException;
import org.wso2.carbon.analytics.activitydashboard.stub.ActivityDashboardAdminServiceStub;
import org.wso2.carbon.analytics.activitydashboard.stub.ActivityDashboardException;
import org.wso2.carbon.analytics.activitydashboard.stub.bean.ActivitySearchRequest;
import org.wso2.carbon.analytics.activitydashboard.stub.bean.RecordBean;
import org.wso2.carbon.analytics.activitydashboard.stub.bean.RecordId;
import org.wso2.carbon.analytics.activitydashboard.commons.SearchExpressionTree;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;

import javax.activation.DataHandler;

import java.rmi.RemoteException;

/**
 * This class represents the client to the ActivityDashboardAdminService,
 * therefore the frontend jsp pages can invoke this to communicate with backend.
 */
public class ActivityDashboardClient {

    private static final Log logger = LogFactory.getLog(ActivityDashboardClient.class);

    private ActivityDashboardAdminServiceStub stub;

    public ActivityDashboardClient(String cookie,
                                   String backEndServerURL,
                                   ConfigurationContext configCtx) throws AxisFault {
        String serviceURL = backEndServerURL + "ActivityDashboardAdminService";
        stub = new ActivityDashboardAdminServiceStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        int timeout = 5 * 60 * 1000; // 1 hour
        option.setTimeOutInMilliSeconds(timeout);
        option.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
        option.setProperty(HTTPConstants.SO_TIMEOUT, timeout);
        option.setProperty(HTTPConstants.CONNECTION_TIMEOUT, timeout);
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    public String[] searchActivities(long fromTime, long toTime, SearchExpressionTree searchExpressionTree)
            throws ActivityDashboardAdminServiceActivityDashboardExceptionException {
        try {
            ActivitySearchRequest searchRequest = new ActivitySearchRequest();
            searchRequest.setFromTime(fromTime);
            searchRequest.setToTime(toTime);
            searchRequest.setSearchTreeExpression(new DataHandler(
                    new ByteArrayDataSource(serializeObject(searchExpressionTree))));
            return stub.getActivities(searchRequest);
        } catch (RemoteException e) {
            String message = "Error while getting the activities from time: " + fromTime + ", to time : " + toTime;
            logger.error(message, e);
            throw getActivityException(message, e);
        }
    }

    public RecordId[] getRecordIds(String activityId, String[] tableNames)
            throws ActivityDashboardAdminServiceActivityDashboardExceptionException {
        try {
            return stub.getRecordIds(activityId, tableNames);
        } catch (RemoteException ex) {
            String msg = "Error while getting the list of records for activity :" + activityId + ". ";
            logger.error(msg, ex);
            throw getActivityException(msg, ex);
        }
    }

    private byte[] serializeObject(Object obj) throws ActivityDashboardAdminServiceActivityDashboardExceptionException {
        return GenericUtils.serializeObject(obj);
    }

    public String[] getAllTables()
            throws ActivityDashboardAdminServiceActivityDashboardExceptionException {
        try {
            return stub.getAllTables();
        } catch (RemoteException e) {
            String msg = "Error while getting the list of tables. ";
            logger.error(msg, e);
            throw getActivityException(msg, e);
        }
    }

    public RecordBean getRecord(String fullQualifiedRecordId)
            throws ActivityDashboardAdminServiceActivityDashboardExceptionException {
        RecordId recordId = getRecordId(fullQualifiedRecordId);
        try {
            return stub.getRecord(recordId);
        } catch (RemoteException | ActivityDashboardAdminServiceActivityDashboardExceptionException e) {
            String msg = "Cannot get the record for record id => " + fullQualifiedRecordId;
            logger.error(msg, e);
            throw getActivityException(msg, e);
        }
    }

    private RecordId getRecordId(String fullQualifiedRecordId) {
        String[] recordIdElements = fullQualifiedRecordId.split(":");
        RecordId recordId = new RecordId();
        recordId.setTableName(recordIdElements[0].trim());
        recordId.setRecordId(recordIdElements[1].trim());
        return recordId;
    }

    private ActivityDashboardAdminServiceActivityDashboardExceptionException getActivityException(String message,
                                                                                                  Exception ex) {
        if (ex.getMessage() != null) {
            message += ex.getMessage();
        }
        ActivityDashboardAdminServiceActivityDashboardExceptionException exception =
                new ActivityDashboardAdminServiceActivityDashboardExceptionException(message, ex);
        exception.setFaultMessage(new ActivityDashboardAdminServiceActivityDashboardException());
        exception.getFaultMessage().setActivityDashboardException(new ActivityDashboardException());
        exception.getFaultMessage().getActivityDashboardException().setErrorMessage(message);
        return exception;
    }

}
