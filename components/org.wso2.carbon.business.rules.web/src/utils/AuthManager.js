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

import AuthenticationAPI from '../api/AuthenticationAPI';

/**
 * Name of the session cookie
 */
const SESSION_USER_COOKIE = 'BR_USER';
const REFRESH_TOKEN_VALIDITY_PERIOD = 604800;
const RTK = 'RTK';

/**
 * App context
 */
const appContext = window.contextPath;

/**
 * Authentication manager
 */
export default class AuthManager {
    /**
     * Deletes user from the session cookie
     */
    static clearUser() {
        AuthManager.deleteSessionCookie(SESSION_USER_COOKIE);
    }

    /**
     * Sets session cookie
     * @param {String} name         Name of the cookie
     * @param {String} value        Value of the cookie
     * @param {number} expiresIn    Number of milliseconds to expire the cookie
     */
    static setSessionCookie(name, value, expiresIn) {
        let expires = '';
        if (expiresIn) {
            const d = new Date();
            d.setTime(d.getTime() + expiresIn);
            expires = `expires=${d.toUTCString()};`;
        }
        document.cookie = `${name}=${value};${expires}path=${appContext};Secure`;
    }

    /**
     * Gets session cookie by name
     * @param {String} name             Name of the cookie
     * @returns {String} Content        Content of the cookie
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
     * Deletes session cookie by name
     * @param {String} name     Name of the cookie
     */
    static deleteSessionCookie(name) {
        document.cookie = name + '=;expires=Thu, 01 Jan 1970 00:00:00 UTC;path=' + appContext;
    }

    /**
     * Gets user from the session cookie
     * @returns {Object}        User object
     */
    static getUser() {
        const buffer = AuthManager.getCookie(SESSION_USER_COOKIE);
        return buffer ? JSON.parse(buffer) : null;
    }

    /**
     * Sets user into a session cookie
     * @param {Object} user     User object
     */
    static setUser(user) {
        AuthManager.setCookie(SESSION_USER_COOKIE, JSON.stringify(user), null, window.contextPath);
    }

    /**
     * Discards active user session
     */
    static discardSession() {
        AuthManager.deleteCookie(SESSION_USER_COOKIE);
        AuthManager.deleteCookie(RTK);
        window.localStorage.clear();
    }

    /**
     * Checks whether the user is logged in or not
     * @returns {boolean}       Login status
     */
    static isLoggedIn() {
        return !!AuthManager.getUser();
    }

    /**
     * Checks whether remember me has been set or not
     * @returns {boolean}       Remember me status
     */
    static isRememberMeSet() {
        return (window.localStorage.getItem('rememberMe') === 'true');
    }

    /**
     * Calculates expiry time
     * @param {number} validityPeriod      Validity period
     * @returns {Date}                     Expiry time
     */
    static calculateExpiryTime(validityPeriod) {
        const expires = new Date();
        expires.setSeconds(expires.getSeconds() + validityPeriod);
        return expires;
    }

    /**
     * Authenticates the user and sets the user into the session
     * @param {String} username         Username
     * @param {String } password        Password
     * @param {boolean} rememberMe      Remember me flag
     * @returns {Promise}               Promise for authentication
     */
    static authenticate(username, password, rememberMe) {
        return new Promise((resolve, reject) => {
            AuthenticationAPI
                .login(username, password, rememberMe)
                .then((response) => {
                    const { authUser, pID, lID, validityPeriod } = response.data;

                    window.localStorage.setItem('rememberMe', rememberMe);
                    if (rememberMe) {
                        window.localStorage.setItem('username', authUser);
                    }
                    // Set user in a cooke
                    AuthManager.setUser({
                        username: authUser,
                        SDID: pID,
                        validity: validityPeriod,
                        expires: AuthManager.calculateExpiryTime(validityPeriod),
                    });
                    // If rememberMe, set refresh token into a persistent cookie, otherwise a session cookie
                    const refreshTokenValidityPeriod =
                        AuthManager.isRememberMeSet() ? REFRESH_TOKEN_VALIDITY_PERIOD : null;
                    AuthManager.setCookie(RTK, lID, refreshTokenValidityPeriod, window.contextPath);
                    resolve();
                })
                .catch(error => reject(error));
        });
    }

    /**
     * Authenticates with refresh token, when remember me has been set
     * @returns {Promise}       Promise for authentication with refresh token
     */
    static authenticateWithRefreshToken() {
        return new Promise((resolve, reject) => {
            AuthenticationAPI
                .getAccessTokenWithRefreshToken()
                .then((response) => {
                    const { pID, lID, validityPeriod } = response.data;
                    const username =
                        AuthManager.isRememberMeSet() ? window.localStorage.getItem('username') :
                            AuthManager.getUser().username;
                    AuthManager.setUser({
                        username,
                        SDID: pID,
                        validity: validityPeriod,
                        expires: AuthManager.calculateExpiryTime(validityPeriod),
                    });
                    // If rememberMe, set refresh token into a persistent cookie else session cookie.
                    const refreshTokenValidityPeriod =
                        AuthManager.isRememberMeSet() ? REFRESH_TOKEN_VALIDITY_PERIOD : null;
                    AuthManager.setCookie(RTK, lID, refreshTokenValidityPeriod, window.contextPath);
                    resolve();
                })
                .catch(error => reject(error));
        });
    }

    /**
     * Logs out the user by revoking tokens and clearing the session
     * @returns {Promise}       Promise after logging out the user
     */
    static logout() {
        return new Promise((resolve, reject) => {
            AuthenticationAPI
                .logout(AuthManager.getUser().SDID)
                .then(() => {
                    AuthManager.discardSession();
                    resolve();
                })
                .catch(error => reject(error));
        });
    }

    /**
     * Gets JavaScript accessible cookies saved in the browser, by giving the cookie name
     * @param {String} name     Name of the cookie to be retrieved
     * @returns {String}        Cookie with the given name if found, empty string otherwise
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
     * Sets a cookie with given parameters assigned to it
     * @param {String} name                 Name of the cookie to be set
     * @param {String} value                Value of the cookie
     * @param {number} validityPeriod       (Optional) Validity period of the cookie in seconds
     * @param {String} path                 Path to set the given cookie
     * @param {boolean} secured             Whether the Secured parameter is set or not
     */
    static setCookie(name, value, validityPeriod, path = '/', secured = true) {
        let expires = '';
        const securedDirective = secured ? '; Secure' : '';
        if (validityPeriod) {
            const date = new Date();
            date.setTime(date.getTime() + validityPeriod * 1000);
            expires = '; expires=' + date.toUTCString();
        }
        document.cookie = name + '=' + value + expires + '; path=' + path + securedDirective;
    }

    /**
     * Deletes a browser cookie that has the given name
     * @param {String} name         Name of the cookie to be deleted
     */
    static deleteCookie(name) {
        document.cookie = name + '=; path=' + window.contextPath + '; expires=Thu, 01 Jan 1970 00:00:01 GMT;';
    }
}
