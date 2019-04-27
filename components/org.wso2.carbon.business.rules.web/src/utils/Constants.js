/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import React, { Component } from 'react';

export const Constants = {
    // Dashboard authentication types
    AUTH_TYPE_UNKNOWN: 'unknown',
    AUTH_TYPE_DEFAULT: 'default',
    AUTH_TYPE_SSO: 'sso',

    // Cookies
    RTK: 'RTK',
    ID_TOKEN: 'RPK',
    USER_DTO_COOKIE: 'USER_DTO',
    SESSION_USER_COOKIE: 'BR_USER',
    REFRESH_TOKEN_VALIDITY_PERIOD: 604800,
};
