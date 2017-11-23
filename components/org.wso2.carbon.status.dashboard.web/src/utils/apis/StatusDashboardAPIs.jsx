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

import Axios from "axios";

import { MediaType } from '../Constants';
import AuthManager from "../../auth/utils/AuthManager";

export default class StatusDashboardAPIS {

    /**
     * This method will return the AXIOS http client.
     * @returns httpClient
     */
    static getHTTPClient() {
        let httpClient = Axios.create({
            baseURL: window.location.origin + '/monitoring/apis/workers',
            timeout: 10000,
            headers: {
                'Content-Type': MediaType.APPLICATION_JSON,
                Authorization: `Bearer ${AuthManager.getUser().token}`
            }
        });
        httpClient.defaults.headers.post['Content-Type'] = MediaType.APPLICATION_JSON;
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
     * This method will create a worker with given worker details json.
     * @param worker
     * @returns {*}
     */
    static createWorker(worker) {
        return StatusDashboardAPIS.getHTTPClient().post('', worker);
    }

    /**
     * This method will test the connection of a worker with given username password json.
     * @param id
     * @param userPass
     * @returns {*}
     */
    static testConnection(id, userPass) {
        return StatusDashboardAPIS.getHTTPClient().put(id, userPass);
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
    static getWorkerGeneralByID(workerID) {
        return StatusDashboardAPIS.getHTTPClient().get(workerID + '/system-details');
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
        return StatusDashboardAPIS.getHTTPClient().get('/' + workerID + '/siddhi-apps/' + appName + '/history', queryParams);
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
     * This method will return the Siddhi App's component history details types given by queryParams and with given componentID.
     * @param workerID
     * @param appName
     * @param componentType
     * @param componentID
     * @param queryParams - json object with required statistics types
     */
    static getComponentHistoryByID(workerID, appName, componentType, componentID, queryParams) {
        return StatusDashboardAPIS.getHTTPClient().get('/' + workerID + '/siddhi-apps/' + appName + '/components/' + componentType + '/' + componentID + '/history', queryParams);
    }

    /**
     * This method will enable/disable Siddhi App statistics of a given Siddhi App specified by appName.
     * @param workerID
     * @param appName
     * @param statEnable
     */
    static enableSiddhiAppStats(workerID, appName, statEnable) {
        return StatusDashboardAPIS.getHTTPClient().put('/' + workerID + '/siddhi-apps/' + appName + '/statistics/', statEnable);
    }
}
