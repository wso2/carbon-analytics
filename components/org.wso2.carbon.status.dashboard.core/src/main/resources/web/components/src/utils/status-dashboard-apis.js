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

import axios from 'axios';

class StatusDashboardAPIS {

    constructor(url){
        this.url=url;
    }

    /**
     * This method will return the AXIOS http client.
     * @returns httpClient
     */
    getHTTPClient() {
        let httpClient = axios.create({
            baseURL:  this.url + '/status-dashboard/workers',
            timeout: 2000
        });
        httpClient.defaults.headers.post['Content-Type'] = 'application/json';
        return httpClient;
    }

    /**
     * This method will return a list of workers real-time details.
     */
    getWorkersList() {
        return this.getHTTPClient().get();
    }

    /**
     * This method will create a worker with given worker details json.
     * @param worker
     * @returns {*}
     */
    createWorker(worker) {
        return this.getHTTPClient().post('', worker)
    }

    /**
     * This method will test the connection of a worker with given username password json.
     * @param id
     * @param userPass
     * @returns {*}
     */
    testConnection(id,userPass) {
        return this.getHTTPClient().put(id , userPass)
    }

    /**
     * This method will delete the worker with given ID
     * @param workerID
     * @returns {boolean}
     */
    deleteWorkerByID(workerID) {
        return this.getHTTPClient().delete(workerID);
    }

    /**
     * This method will return the worker configuration details with given ID.
     * @param workerID
     */
    getWorkerConfigByID(workerID) {
        return this.getHTTPClient().get(workerID);
    }

    /**
     * This method will update the worker with given ID.
     * @param workerID
     * @param worker
     */
    updateWorkerByID(workerID, worker) {
        return this.getHTTPClient().put(workerID, worker);
    }

    /**
     * This method will return the worker general details with given ID.
     * @param workerID
     */
    getWorkerGeneralByID(workerID) {
        return this.getHTTPClient().get('/'+ workerID + '/details');
    }

    /**
     * This method will return the worker history details types given by queryParams and with given worker ID.
     * @param workerID
     * @param queryParams - json object with required statistic types
     */
    getWorkerHistoryByID(workerID, queryParams) {
        return this.getHTTPClient().get('/'+ workerID + '/history', queryParams);
    }

    /**
     * This method will return a list of Siddhi Apps of worker given by ID.
     */
    getSiddhiAppsList(workerID) {
        return this.getHTTPClient().get('/'+ workerID + '/siddhi-apps');
    }

    /**
     * This method will return details of Siddhi App with given name of a worker with  given worker ID.
     * @param workerID
     * @param appName
     */
    getSiddhiApp(workerID, appName) {
        return this.getHTTPClient().get('/'+ workerID + '/siddhi-apps' + appName);
    }

    /**
     * This method will return the Siddhi App history details types given by queryParams and with given Siddhi App name.
     * @param workerID
     * @param appName
     * @param queryParams - json object with required statistics types
     */
    getSiddhiAppHistoryByID(workerID, appName, queryParams) {
        return this.getHTTPClient().get('/'+ workerID + '/siddhi-apps' + appName + '/history', queryParams);
    }

    /**
     * This method will return the Siddhi App's component history details types given by queryParams and with given componentID.
     * @param workerID
     * @param appName
     * @param componentID
     * @param queryParams - json object with required statistics types
     */
    getComponentHistoryByID(workerID, appName, componentID, queryParams) {
        return this.getHTTPClient().get('/'+ workerID + '/siddhi-apps' + appName + '/components' + componentID + '/history', queryParams);
    }

    /**
     * This method will enable/disable Siddhi App statistics of a given Siddhi App specified by appName.
     * @param workerID
     * @param appName
     * @param statEnable
     */
    enableSiddhiAppStats(workerID, appName, statEnable) {
        return this.getHTTPClient().put('/'+ workerID + '/siddhi-apps' + appName + '/statistics', statEnable);
    }
}

export default StatusDashboardAPIS;