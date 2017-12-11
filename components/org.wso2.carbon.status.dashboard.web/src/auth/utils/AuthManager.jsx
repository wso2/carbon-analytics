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
const sessionUser = 'wso2dashboard_user';
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
        const buffer = AuthManager.getSessionCookie(sessionUser);
        return buffer ? JSON.parse(buffer) : null;
    }

    /**
     * Set user into a session cookie.
     *
     * @param {{}} user  User object
     */
    static setUser(user) {
        AuthManager.setSessionCookie(sessionUser, JSON.stringify(user), (user.validity - TIMESTAMP_SKEW) * 1000);
    }

    /**
     * Delete user from the session cookie.
     */
    static clearUser() {
        AuthManager.deleteSessionCookie(sessionUser);
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
                        username:  window.localStorage.getItem("username"),
                        token: response.data.partialAccessToken,
                        validity: response.data.validityPeriod
                    });
                    AuthManager.setCookie("REFRESH_TOKEN", response.data.partialRefreshToken,
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
                    // TODO: Get user roles from the SCIM API.
                    const roles = [];
                    AuthManager.setUser({ username, rememberMe, roles, token: response.data.partialAccessToken,
                        validity: response.data.validityPeriod });
                    if (rememberMe) {
                        window.localStorage.setItem("rememberMe", rememberMe);
                        window.localStorage.setItem("username", username);
                        AuthManager.setCookie("REFRESH_TOKEN", response.data.partialRefreshToken,
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
                .logout(AuthManager.getUser().token)
                .then(() => {
                    AuthManager.clearUser();
                    window.localStorage.clear();
                    AuthManager.delete_cookie("REFRESH_TOKEN");
                    resolve();
                })
                .catch(error => reject(error));
        });
    }

    /**
     * Set session cookie.
     *
     * @param {string} name Name of the cookie
     * @param {string} value Value of the cookie
     * @param {number} expiresIn Number of milliseconds to expire the cookie
     */
    static setSessionCookie(name, value, expiresIn) {
        let expires = '';
        if (expiresIn) {
            const d = new Date();
            d.setTime(d.getTime() + expiresIn);
            expires = `expires=${d.toUTCString()};`;
        }
        document.cookie = `${name}=${value};${expires}path=${window.contextPath};Secure`;
    }

    /**
     * Get session cookie by name.
     *
     * @param {string} name Name of the cookie
     * @returns {string} Content
     */
    static getSessionCookie(name) {
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
     * Delete session cookie by name.
     *
     * @param {string} name Name of the cookie
     */
    static deleteSessionCookie(name) {
        document.cookie = name + '=;expires=Thu, 01 Jan 1970 00:00:00 UTC;path=/monitoring';
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
     * Get JavaScript accessible cookies saved in browser, by giving the cooke name.
     * @param {String} name : Name of the cookie which need to be retrived
     * @returns {String|null} : If found a cookie with given name , return its value,Else null value is returned
     */
    static getCookie(name) {
        let pairs = document.cookie.split(";");
        let cookie = null;
        for (let pair of pairs) {
            pair = pair.split("=");
            let cookie_name = pair[0].trim();
            let value = encodeURIComponent(pair[1]);
            if (cookie_name === name) {
                cookie = value;
                break;
            }
        }
        return cookie;
    }

    /**
     * Delete a browser cookie given its name
     * @param {String} name : Name of the cookie which need to be deleted
     */
    static delete_cookie(name) {
        document.cookie = name + '=; Path=' + "/" + '; Expires=Thu, 01 Jan 1970 00:00:01 GMT;';
    }
}
