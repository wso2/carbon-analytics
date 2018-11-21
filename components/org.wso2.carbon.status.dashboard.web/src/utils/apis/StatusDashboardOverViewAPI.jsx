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

/**
 * This should remove after fixing app by paralleling call workers
 */
export default class StatusDashboardOverViewAPI {

    /**
     * This method will return the AXIOS http client.
     * @returns httpClient
     */
    static getHTTPClient() {
        let httpClient = Axios.create({
            baseURL: window.location.origin + "/" + window.contextPath.substr(1) + '/apis/workers',
            timeout: 15000,
            headers: {"Authorization": "Bearer " + AuthManager.getUser().SDID, "Content-Type": "application/json"}
        });
        httpClient.defaults.headers.post['Content-Type'] = MediaType.APPLICATION_JSON;
        httpClient.defaults.headers.put['Content-Type'] = MediaType.APPLICATION_JSON;
        return httpClient;
    }

    /**
     * This method will return a list of Siddhi Apps of worker given by ID.
     */
    static getSiddhiApps(workerID) {
        return StatusDashboardOverViewAPI.getHTTPClient().get('/' + workerID + '/siddhi-apps');
    }

    /**
     * This method will return the SiddhiApp's components details of a given worker's SiddhiApp.
     * @param workerID
     * @param appName
     */
    static getComponents(workerID, appName) {
        return StatusDashboardOverViewAPI.getHTTPClient().get('/' + workerID + '/siddhi-apps/' + appName +
            '/components');
    }

    /**
     * This method will return a list of workers real-time details.
     */
    static getWorkersList() {
        return StatusDashboardOverViewAPI.getHTTPClient().get();
    }

    /**
     * This method will return the worker general details with given ID.
     * @param workerID
     */
    static postWorkerGeneralByID(workerID) {
        return StatusDashboardOverViewAPI.getHTTPClient().post(workerID + '/system-details');
    }

    /**
     * This method will return the HA details of a worker with given ID.
     * @param workerID
     */
    static getWorkerHaDetailsByID(workerID) {
        return StatusDashboardOverViewAPI.getHTTPClient().get(workerID + '/ha-status');
    }

    /**
     * This method will enable/disable Siddhi App statistics of a given Siddhi App specified by appName.
     * @param workerID
     * @param appName
     * @param statEnable
     */
    static enableSiddhiAppStats(workerID, appName, statEnable) {
        return StatusDashboardOverViewAPI.getHTTPClient().put('/' + workerID + '/siddhi-apps/' + appName +
            '/statistics/', statEnable);
    }

    /**
     * This method will create a worker with given worker details json.
     * @param worker
     * @returns {*}
     */
    static createWorker(worker) {
        return StatusDashboardOverViewAPI.getHTTPClient().post('', worker);
    }

    /**
     * This method will create a manager with given manager details json.
     * @param worker
     * @returns {*}
     */
    static createManager(manager) {
        return StatusDashboardOverViewAPI.getHTTPClient().post('/manager', manager);
    }

    /**
     * This method will return the HA details of manager node.
     * @param managerId
     */
    static getManagerHADetails(managerId) {
        return StatusDashboardOverViewAPI.getHTTPClient().get('/manager' + '/' + managerId);
    }

    /**
     * This method will return a list of managers real-time details.
     */
    static getManagerList() {
        return StatusDashboardOverViewAPI.getHTTPClient().get('/manager');
    }

    /**
     * This method will return a list of managers real-time details.
     */
    static getResourceClusterNodes(managerId) {
        return StatusDashboardOverViewAPI.getHTTPClient()
            .get('/manager' + '/' + managerId + '/clusteredResourceNodeDetails');
    }

    static getSingleNodeDeploymentSiddhiAppSummary(){
        return StatusDashboardOverViewAPI.getHTTPClient().get('/siddhi-apps/single-deployment-apps');
    }

    static getHASiddhiAppSummary(){
        return StatusDashboardOverViewAPI.getHTTPClient().get('/siddhi-apps/ha-apps');
    }

    static getManagerSiddhiAppSummary(){
        return StatusDashboardOverViewAPI.getHTTPClient().get('/manager/siddhi-apps');
    }


}
