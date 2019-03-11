/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * Constants to be used by the siddhi editor tour guide
 */
define(function () {

    "use strict";   // JS strict mode

    /**
     * Constants used by the siddhi editor tour guide
     */
    var constants = {
        CONTENT: '@App:name(\'SweetFactory\')\n' +
            '@App:description(\'Description of the plan\')\n' +
            '\n' +
            '-- Please refer to https://docs.wso2.com/display/SP430/Quick+Start+Guide on getting started with SP editor. \n' +
            '\n' +
            'define stream SweetProductionStream (name string, amount long);\n' +
            '@sink(type = \'log\', \n' +
            '\t@map(type = \'passThrough\'))\n' +
            'define stream TotalProductionStream (TotalProduction long);\n' +
            '\n' +
            '@info(name = \'SweetTotalQuery\')\n' +
            'from SweetProductionStream \n' +
            'select count() as TotalProduction \n' +
            'insert into TotalProductionStream;\n',
        INSERT_STRING: 'define stream SweetProductionStream (name string, amount long);',
        TAB_KEYCODE : 9,
        ERROR_TEXT: "Please close all tabs except welcome-page to start the guide. You can use close all button on the" +
            " right corner of the screen",
        CURRENT_STEP: null
    };

    return constants;
});
