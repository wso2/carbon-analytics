/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import Qs from 'qs';
// App Constants
import { MediaType } from '../constants/AuthConstants';
// App Utils
import BusinessRulesUtilityFunctions from '../utils/BusinessRulesUtilityFunctions';
// Auth Utils
import AuthManager from '../utils/AuthManager';

/**
 * Authentication API base path
 */
const basePath = window.location.origin;

/**
 * Password grant type
 */
const passwordGrantType = 'password';

/**
 * App context starting from forward slash
 */
const appContext = window.contextPath.substr(1);

/**
 * Authentication API client
 */
export default class AuthenticationAPI {
    /**
     * Returns an Axios HTTP Client
     * @returns {AxiosInstance}     Axios HTTP Client
     */
    static getHttpClient() {
        const client = Axios.create({
            baseURL: basePath,
            timeout: 2000,
        });
        client.defaults.headers.post['Content-Type'] = MediaType.APPLICATION_JSON;
        return client;
    }

    /**
     * Gets new token using refresh token
     * @returns {AxiosPromise}      Response with the refresh token
     */
    static getAccessTokenWithRefreshToken() {
        return AuthenticationAPI
            .getHttpClient()
            .post(`/login/${appContext}`, Qs.stringify({
                grantType: 'refresh_token',
                rememberMe: true,
            }), {
                headers: {
                    'Content-Type': MediaType.APPLICATION_WWW_FORM_URLENCODED,
                    Authorization: 'Bearer ' + AuthManager.getCookie('RTK'),
                    Accept: MediaType.APPLICATION_JSON,
                },
            });
    }

    /**
     * Logs in a user
     * @param {String} username         Username
     * @param {String} password         Password
     * @param {boolean} rememberMe      Remember me flag
     * @returns {AxiosPromise}          Response after the login
     */
    static login(username, password, rememberMe = false) {
        return AuthenticationAPI
            .getHttpClient()
            .post(`/login/${appContext}`, Qs.stringify({
                username,
                password,
                grantType: passwordGrantType,
                rememberMe,
                appId: 'br_' + BusinessRulesUtilityFunctions.generateGUID(),
            }), {
                headers: {
                    'Content-Type': MediaType.APPLICATION_WWW_FORM_URLENCODED,
                },
            });
    }

    /**
     * Logs out the user
     * @param {String} token        Partial access token
     * @returns {AxiosPromise}      Response after logging out
     */
    static logout(token) {
        return AuthenticationAPI
            .getHttpClient()
            .post(`/logout/${appContext}`, null, {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });
    }
}
