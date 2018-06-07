/**
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

define(['require', 'alerts'],
    function (require, alerts) {

        /**
         * @class DesignViewUtils Contains utility methods related to design view
         * @constructor
         */
        var DesignViewUtils = function () {};

        /**
         * Display's a warning using the AlertsManager.
         *
         * @param message The content to be displayed in the alert
         */
        DesignViewUtils.prototype.warnAlert = function (message) {
            alerts.warn(message);
        };

        /**
         * Display's a error using the AlertsManager.
         *
         * @param message The content to be displayed in the alert
         */
        DesignViewUtils.prototype.errorAlert = function (message) {
            alerts.error(message);
        };

        return DesignViewUtils;
    });
