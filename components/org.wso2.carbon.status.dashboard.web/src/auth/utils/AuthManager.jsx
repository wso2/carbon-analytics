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

import AuthenticationAPI from '../../utils/apis/AuthenticationAPI';

/**
 * Name of the session cookie.
 */
const sessionUser = 'DASHBOARD_USER';
/**
 * Name of the refresh token cookie.
 */
const REFRESH_TOKEN_COOKIE_NAME = 'REFRESH_TOKEN';
const TIMESTAMP_SKEW =  100;
const REFRESH_TOKEN_VALIDITY_PERIOD = 604800;

/**
 * Authentication manager.
 */
export default class AuthManager {
    /**
     * Get user from the session cookie.
     *
     * @returns {{}|null} User object
     */
    static getUser() {
        const buffer = AuthManager.getCookie(sessionUser);
        return buffer ? JSON.parse(buffer) : null;
    }

    /**
     * Set user into a session cookie.
     *
     * @param {{}} user  User object
     */
    static setUser(user) {
        AuthManager.setCookie(sessionUser, JSON.stringify(user), (user.validity - TIMESTAMP_SKEW), window.contextPath);
    }

    /**
     * Delete user from the session cookie.
     */
    static clearUser() {
        AuthManager.deleteCookie(sessionUser);
    }

    /**
     * Check whether the user is logged in.
     *
     * @returns {boolean} Status
     */
    static isLoggedIn() {
        return !!AuthManager.getUser();
    }

    static authenticateWithRefreshToken(){
        return new Promise((resolve, reject) => {
            AuthenticationAPI
                .getAccessTokenWithRefreshToken()
                .then((response) => {
                    AuthManager.setUser({
                        username: window.localStorage.getItem("username"),
                        SDID: response.data.pID,
                        validity: response.data.validityPeriod
                    });
                    AuthManager.setCookie(REFRESH_TOKEN_COOKIE_NAME, response.data.lID,
                        REFRESH_TOKEN_VALIDITY_PERIOD, window.contextPath);
                    resolve();
                })
                .catch(error => reject(error));
        });
    }

    /**
     * Check whether the rememberMe is set
     *
     * @returns {boolean} Status
     */
    static isRememberMeSet() {
        return !!(window.localStorage.getItem("rememberMe"));
    }


    /**
     * Authenticate the user and set the user into the session.
     *
     * @param {string} username Username
     * @param {string } password Password
     * @param {boolean} rememberMe Remember me flag
     * @returns {Promise} Promise
     */
    static authenticate(username, password, rememberMe) {
        return new Promise((resolve, reject) => {
            AuthenticationAPI
                .login(username, password, rememberMe)
                .then((response) => {
                    AuthManager.setUser({
                        username: response.data.authUser,
                        rememberMe,
                        roles: [],
                        SDID: response.data.pID,
                        validity: response.data.validityPeriod,
                    });
                    if (rememberMe) {
                        window.localStorage.setItem("rememberMe", rememberMe);
                        window.localStorage.setItem("username", username);
                        AuthManager.setCookie(REFRESH_TOKEN_COOKIE_NAME, response.data.lID,
                            REFRESH_TOKEN_VALIDITY_PERIOD, window.contextPath);
                    }
                    resolve();
                })
                .catch(error => reject(error));
        });
    }

    /**
     * Logout user by revoking tokens and clearing the session.
     *
     * @returns {Promise} Promise
     */
    static logout() {
        return new Promise((resolve, reject) => {
            AuthenticationAPI
                .logout(AuthManager.getUser().SDID)
                .then(() => {
                    AuthManager.clearUser();
                    window.localStorage.clear();
                    AuthManager.deleteCookie(REFRESH_TOKEN_COOKIE_NAME);
                    resolve();
                })
                .catch(error => reject(error));
        });
    }

    /**
     * Get JavaScript accessible cookies saved in browser, by giving the cooke name.
     * @param {String} name : Name of the cookie which need to be retrived
     * @returns {String|null} : If found a cookie with given name , return its value,Else null value is returned
     */
    static getCookie(name) {
        name = `${name}=`;
        const arr = document.cookie.split(';');
        for (let i = 0; i < arr.length; i++) {
            let c = arr[i];
            while (c.charAt(0) === ' ') {
                c = c.substring(1);
            }
            if (c.indexOf(name) === 0) {
                return c.substring(name.length, c.length);
            }
        }
        return '';
    }

    /**
     * Set a cookie with given name and value assigned to it.
     * @param {String} name : Name of the cookie which need to be set
     * @param {String} value : Value of the cookie, expect it to be URLEncoded
     * @param {number} validityPeriod :  (Optional) Validity period of the cookie in seconds
     * @param {String} path : Path which needs to set the given cookie
     * @param {boolean} secured : secured parameter is set
     */
    static setCookie(name, value, validityPeriod, path = "/", secured = true) {
        let expires = '';
        const securedDirective = secured ? "; Secure" : "";
        if (validityPeriod) {
            const date = new Date();
            date.setTime(date.getTime() + validityPeriod * 1000);
            expires = "; expires=" + date.toUTCString();
        }
        document.cookie = name + "=" + value + expires + "; path=" + path + securedDirective;
    }

    /**
     * Delete a browser cookie given its name
     * @param {String} name : Name of the cookie which need to be deleted
     */
    static deleteCookie(name) {
        document.cookie = name + '=; path=' + window.contextPath + '; expires=Thu, 01 Jan 1970 00:00:01 GMT;';
    }
}
