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
     * Method to combine two data arrays.
     * @param chartA
     * @param chartB
     * @returns {Array}
     */
    static initCombinedYDomain(chartA, chartB) {
        let max, min;
        if (!(chartA.length === 0) && !(chartB.length === 0)) {
            let maxA = chartA.reduce(function (max, arr) {
                return Math.max(max, arr[1]);
            }, -Infinity);
            let maxB = chartB.reduce(function (max, arr) {
                return Math.max(max, arr[1]);
            }, -Infinity);
            let minA = chartA.reduce(function (max, arr) {
                return Math.min(max, arr[1]);
            }, Infinity);
            let minB = chartB.reduce(function (max, arr) {
                return Math.min(max, arr[1]);
            }, Infinity);
            max = maxA > maxB ? maxA : maxB;
            min = minA > minB ? minB : minA;
        } else if (!(chartA.length === 0)) {
            max = chartA.reduce(function (max, arr) {
                return Math.max(max, arr[1]);
            }, -Infinity);
            min = chartA.reduce(function (max, arr) {
                return Math.min(max, arr[1]);
            }, Infinity);
        } else if (!(chartB.length === 0)) {
            max = chartB.reduce(function (max, arr) {
                return Math.max(max, arr[1]);
            }, -Infinity);
            min = chartB.reduce(function (max, arr) {
                return Math.min(max, arr[1]);
            }, Infinity);
        } else {
            max = 10;
            min = 0;
        }
        return [min, max];
    }
    static getCombinedYDomain(chartA, prevYDomain) {
        let max, min;
        if (!(chartA.length === 0)) {
            max = chartA.reduce(function (max, arr) {
                return Math.max(max, arr[1]);
            }, -Infinity);
            min = chartA.reduce(function (max, arr) {
                return Math.min(max, arr[1]);
            }, Infinity);
        }
        max=max>prevYDomain[1]?max:prevYDomain[1];
        min=min<prevYDomain[0]?min:prevYDomain[0];
        return [min,max];
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
        if (max === 0) {
            return [min, 10];
        }
        if(((min===max)&&(min===0))||(min!==max)) {
            return [min, max];
        }else {
            return [0,max]
        }
    }

    static generateguid () {
        function s4() {
            return Math.floor((1 + Math.random()) * 0x10000)
                .toString(16)
                .substring(1);
        }

        return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
            s4() + '-' + s4() + s4() + s4();
    }
}