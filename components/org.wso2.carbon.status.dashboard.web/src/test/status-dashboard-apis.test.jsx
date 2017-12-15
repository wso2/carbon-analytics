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

import {describe, it} from "mocha";
import StatusDashboardAPIs from '../utils/apis/StatusDashboardAPIs';
import {assert} from 'chai';
import TestUtils from './utils/test-utils';

TestUtils.setupMockEnvironment();

describe('StatusDashboard',
    function () {

        let sampleWorkerArray = [{
            "host": "localhost",
            "port": "9090"
        }, {
            "host": "10.100.5.41",
            "port": "9095"
        }];

        let sampleWorkerList = {
            "Non Clusters":[{
                "workerId":"localhost_9090",
                "lastUpdate":1512020345075,
                "statusMessage":"Success",
                "serverDetails":{
                    "siddhiApps":{
                        "active":12,
                        "inactive":1
                    },
                    "workerMetrics":{
                        "processCPU":0.0066552049248516446,
                        "systemCPU":0.14297598580222948,
                        "loadAverage":1.14,
                        "memoryUsage":0.09988512251810475
                    },
                    "isStatsEnabled":true,
                    "clusterID":"Non Clusters",
                    "lastSyncTime":"n/a",
                    "lastSnapshotTime":"Thu, 01 Jan 1970 05:30:00 IST",
                    "osName":"other",
                    "runningStatus":"Reachable"
                }
            }],
            "Never Reached":[{
                "workerId":"10.100.5.41_9095",
                "lastUpdate":0,
                "serverDetails":{
                    "runningStatus":"Not-Reachable"
                }
            }]};

        const HTTP_STATUS_CODE_OK = 200;

        describe('#createWorker()',
            function () {
                it('Should return HTTP 200 status code for newly added worker',
                    function () {
                        sampleWorkerArray.forEach(workerJSON => {
                            let promised_create = StatusDashboardAPIs.createWorker(workerJSON);
                            return promised_create.then((response) => {
                                assert.equal(response.status, HTTP_STATUS_CODE_OK, 'Error while adding worker -' +
                                    ' Failed !');
                            });
                        });
                    }
                );
            }
        );
        describe('#getWorkersList()',
            function () {
                it('Should return HTTP 200 status code with workers list',
                    function () {
                        let promised_get_list = StatusDashboardAPIs.getWorkersList();
                        return promised_get_list.then((response) => {
                            assert.equal(response.status, HTTP_STATUS_CODE_OK, 'Error in retrieving workers list -' +
                                ' Failed !');
                            assert.equal(response.data["Non Clusters"].length, 1, 'Error in retrieving workers list -' +
                                ' Failed !');
                            assert.equal(response.data["Non Clusters"][0].workerId, "localhost_9090", 'Error in ' +
                                'retrieving workers list - Failed !');
                            assert.equal(response.data["Never Reached"][0].workerId, "10.100.5.41_9095",
                                'Error in retrieving workers' +
                                ' list -' +
                                ' Failed !');
                        });
                    }
                );
            }
        );
        describe('#postWorkerGeneralByID()',
            function () {
                it('Should return HTTP 200 status code with worker general details with a given ID',
                    function () {
                        let promised_get_worker = StatusDashboardAPIs.postWorkerGeneralByID("localhost_9090");
                        return promised_get_worker.then((response) => {
                            assert.equal(response.status, HTTP_STATUS_CODE_OK, 'Error in retrieving worker - ' +
                                'Failed !');
                            assert.equal(response.data.workerId, sampleWorkerList["Non Clusters"][0].workerId,
                                'Worker content - worker Id is different' +
                                ' - Failed !');
                            assert.equal(response.data.javaRuntimeName,
                                sampleWorkerList["Non Clusters"][0].javaRuntimeName, 'Worker content - Java Runtime is' +
                                ' different- Failed !');
                            assert.equal(response.data.javaVMVersion, sampleWorkerList["Non Clusters"][0].javaVMVersion,
                                'Worker content Java Version is different - Failed !');
                            assert.equal(response.data.osName, sampleWorkerList["Non Clusters"][0].osName,
                                'Worker content OS Name is different - Failed !');
                            assert.equal(response.data.osVersion, sampleWorkerList["Non Clusters"][0].osVersion,
                                'Worker content Os Version is different - Failed !');
                        });
                    }
                );
            }
        );
        describe('#deleteWorkerByID()',
            function () {
                it('Should return HTTP 200 status code by deleting the worker with given ID',
                    function () {
                        let promised_deleted = StatusDashboardAPIs.deleteWorkerByID("localhost_9090");
                        return promised_deleted.then((response) => {
                            assert.equal(response.status, HTTP_STATUS_CODE_OK, 'Error in deleting the worker - ' +
                                'Failed !');
                        })
                    });
                }
        );
    }
);