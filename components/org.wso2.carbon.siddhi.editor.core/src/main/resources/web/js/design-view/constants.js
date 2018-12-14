/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

/**
 * Constants to be used by the siddhi editor  design view
 */
define(function () {

    "use strict";   // JS strict mode

    /**
     * Constants used by the siddhi editor design-view
     */
    var constants = {
        ALPHABETIC_VALIDATOR_REGEX: /^([a-zA-Z])$/,
        START: "start",
        CRON_EXPRESSION: "cron-expression",
        EVERY: "every",
        AT: "at",
        SORT: "sort",
        FREQUENT: "frequent",
        LOSSY_FREQUENT: "lossyfrequent",
        DEFAULT_STORE_TYPE: "in-memory",
        RDBMS_STORE_TYPE: "rdbms",
        MAP: "map",
        LIST: "list"
    };

    return constants;
});