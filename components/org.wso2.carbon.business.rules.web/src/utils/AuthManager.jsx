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

import AuthenticationAPI from "../api/AuthenticationAPI";

/**
 * Name of the session cookie.
 */
const sessionUser = 'wso2dashboard_user';

/**
 * App context.
 */
const appContext = window.contextPath;

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
        AuthManager.setSessionCookie(sessionUser, JSON.stringify(user), 7 * 24 * 3600 * 1000);
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
                    AuthManager.setUser({username, rememberMe, roles, token: response.data.partialAccessToken});
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
                    resolve();
                })
                .catch(error => {reject(error)});
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
        document.cookie = `${name}=${value};${expires}path=${appContext};Secure`;
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
        document.cookie = name + '=;expires=Thu, 01 Jan 1970 00:00:00 UTC;path=' + appContext;
    }
}
