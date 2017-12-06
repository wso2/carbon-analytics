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
/**
 * Util class for status dashboard.
 */
export default class DashboardUtils {

    /**
     * Method to combine two data arrays.
     * @param chartA
     * @param chartB
     * @returns {Array}
     */
    static getCombinedChartList(chartA, chartB) {
        let combined = [];
        for (let n = 0; n < chartA.length; n++) {
            let subA = chartA[n].slice(0);
            for (let i = 0; i < chartB.length; i++) {
                let subB = chartB[i];
                if (subA.indexOf(subB[0]) === 0) {
                    subA.push(subB[1]);
                }
            }
            combined.push(subA);
        }
        return combined;
    }

    /**
     * Method to get y domain of a given data set.
     * @param arr
     * @returns {[*,*]}
     */
    static getYDomain(arr) {
        let values = arr.map(function (element) {
            return Number(element[1]);
        });
        let max = Math.ceil(Math.max.apply(null, values));
        let min = Math.floor(Math.min.apply(null, values));
        if(max === 0){
            return [min, 10];
        }
        return [min, max];
    }
}