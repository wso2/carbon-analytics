/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import Axios from 'axios';
import {MediaType} from '../Constants';
import AuthManager from '../../auth/utils/AuthManager';

export default class StatusDashboardAPIS {

    /**
     * This method will return the AXIOS http client.
     * @returns httpClient
     */
    static getHTTPClient() {
        let httpClient = Axios.create({
            baseURL: window.location.origin + "/" + window.contextPath.substr(1) + '/apis/workers',
            timeout: 12000,
            headers: {"Authorization": "Bearer " + AuthManager.getUser().SDID}
        });
        httpClient.defaults.headers.post['Content-Type'] = MediaType.APPLICATION_JSON;
        httpClient.defaults.headers.put['Content-Type'] = MediaType.APPLICATION_JSON;
        return httpClient;
    }

    /**
     * This method will return the polling interval.
     */
    static getDashboardConfig() {
        return StatusDashboardAPIS.getHTTPClient().get("config");
    }

    /**
     * This method will return a list of workers real-time details.
     */
    static getWorkersList() {
        return StatusDashboardAPIS.getHTTPClient().get();
    }

    /**
     * This method will test the connection of a worker with given username password json.
     * @param workerID
     * @returns {*}
     */
    static testConnection(workerID) {
        return StatusDashboardAPIS.getHTTPClient().post(workerID + '/status');
    }

    /**
     * This method will delete the worker with given ID
     * @param workerID
     * @returns {boolean}
     */
    static deleteWorkerByID(workerID) {
        return StatusDashboardAPIS.getHTTPClient().delete(workerID);
    }

    /**
     * This method will return the worker general details with given ID.
     * @param workerID
     */
    static postWorkerGeneralByID(workerID) {
        return StatusDashboardAPIS.getHTTPClient().post(workerID + '/system-details');
    }

    /**
     * This method will return the HA details of a worker with given ID.
     * @param workerID
     */
    static getWorkerHaDetailsByID(workerID) {
        return StatusDashboardAPIS.getHTTPClient().get(workerID + '/ha-status');
    }

    /**
     * This method will return the worker history details types given by queryParams and with given worker ID.
     * @param workerID
     * @param queryParams - json object with required statistic types
     */
    static getWorkerHistoryByID(workerID, queryParams) {
        return StatusDashboardAPIS.getHTTPClient().get('/' + workerID + '/history', queryParams);
    }

    /**
     * This method will return a list of Siddhi Apps of worker given by ID.
     */
    static getSiddhiApps(workerID) {
        return StatusDashboardAPIS.getHTTPClient().get('/' + workerID + '/siddhi-apps');
    }

    /**
     * This method will return details of Siddhi App with given name of a worker with  given worker ID.
     * @param workerID
     * @param appName
     */
    static getSiddhiAppByName(workerID, appName) {
        return StatusDashboardAPIS.getHTTPClient().get('/' + workerID + '/siddhi-apps/' + appName);
    }

    /**
     * This method will return the Siddhi App history details types given by queryParams and with given Siddhi App name.
     * @param workerID
     * @param appName
     * @param queryParams - json object with required statistics types
     */
    static getSiddhiAppHistoryByID(workerID, appName, queryParams) {
        return StatusDashboardAPIS.getHTTPClient().get('/' + workerID + '/siddhi-apps/' + appName + '/history'
            , queryParams);
    }

    /**
     * This method will return the SiddhiApp's components details of a given worker's SiddhiApp.
     * @param workerID
     * @param appName
     */
    static getComponents(workerID, appName) {
        return StatusDashboardAPIS.getHTTPClient().get('/' + workerID + '/siddhi-apps/' + appName + '/components');
    }

    /**
     * This method will return the Siddhi App's component history details types given by queryParams and with given
     * componentID.
     * @param workerID
     * @param appName
     * @param componentType
     * @param componentID
     * @param queryParams - json object with required statistics types
     */
    static getComponentHistoryByID(workerID, appName, componentType, componentID, queryParams) {
        return StatusDashboardAPIS.getHTTPClient().get('/' + workerID + '/siddhi-apps/' + appName +
            '/components/' + componentType + '/' + componentID + '/history', queryParams);
    }

    /**
     * This method will return the run time of the node.
     * @param workerId
     */
    static getRuntimeEnv(workerId) {
        return StatusDashboardAPIS.getHTTPClient().get('/' + workerId + '/runTime');
    }

    /**
     * This method will return the siddhi app details that are deployed in the manager node.
     * @param managerId
     */
    static getManagerSiddhiApps(managerId) {
        return StatusDashboardAPIS.getHTTPClient().get('/manager' + '/' + managerId + '/siddhi-apps');
    }

    /**
     * This method will return the HA details of the clustered manager nodes.
     * @param managerId
     */
    static getManagerHADetails(managerId) {
        return StatusDashboardAPIS.getHTTPClient().get('/manager' + '/' + managerId);
    }

    /**
     * This method will return the text view of the parent siddhi application.
     * @param managerId
     * @param appName
     */
    static getSiddhiAppTextView(managerId, appName) {
        return StatusDashboardAPIS.getHTTPClient().get('/manager/' + managerId + '/siddhi-apps/' + appName);
    }

    /**
     * This method will return the child app details.
     * @param managerId
     * @param appName
     */
    static getChildAppDetails(managerId, appName) {
        return StatusDashboardAPIS.getHTTPClient().get('/manager/' + managerId + '/siddhi-apps/' + appName +
            '/child-apps')
    }

    /**
     * This method will delete existing manager node.
     * @param managerId
     */
    static deleteManagerById(managerId) {
        return StatusDashboardAPIS.getHTTPClient().delete('/manager/' + managerId)
    }

    /**
     * This method will return the kafka details of each child apps
     * @param managerId
     * @param appName
     */
    static getKafkaDetails(managerId, appName) {
        return StatusDashboardAPIS.getHTTPClient().get('/manager/' + managerId + '/siddhi-apps/' + appName +
            '/child-apps/' + 'transport')
    }

    /**
     * This method will return details of Siddhi App elements with given name of a worker with  given worker ID.
     * @param workerID
     * @param appName
     */
    static getSiddhiAppElementsByName(workerID, appName) {
        return StatusDashboardAPIS.getHTTPClient().get('/' + workerID + '/siddhi-apps/' + appName +'/elements');
    }
}
