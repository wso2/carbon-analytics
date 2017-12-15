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

import Qs from 'qs';
import React, { Component } from 'react';
import { Redirect, Route, Switch } from 'react-router-dom';
//App Components
import AuthManager from './utils/AuthManager';
import WorkerOverview from "../overview/WorkerOverview";
import AppSpecific from "../siddhi-app/AppSpecific";
import AppHistory from "../siddhi-app/AppHistory";
import ComponentHistory from "../siddhi-app/ComponentHistory";
import AddWorker from "../add-worker/AddWorker";
import WorkerHistory from "../worker-history/WorkerHistory";
import WorkerHistoryMore from "../worker-history-more/WorkerHistoryMore";
import WorkerSpecific from "../worker/WorkerSpecific";
import Error404 from "../error-pages/Error404";
/**
 * App context.
 */
const appContext = window.contextPath;

/**
 * Secured router (protects secured pages).
 */
export default class SecuredRouter extends Component {
    /**
     * Render routing.
     *
     * @return {XML} HTML content
     */
    render() {
        // If the user is not logged in, redirect to the login page.
        if (!AuthManager.isLoggedIn()) {
            const params = Qs.stringify({ referrer: this.props.location.pathname });
            return (
                <Redirect to={{ pathname: `${appContext}/login`, search: params }} />
            );
        }

        return (
            <Switch>
                <Route exact path={appContext} component={WorkerOverview}/>
                <Route exact path={appContext + '/worker/:id/siddhi-apps/:appName/:isStatsEnabled'}
                       component={AppSpecific}/>
                <Route exact
                       path={appContext + '/worker/:id/siddhi-apps/:appName/app/history/:isStatsEnabled'}
                       component={AppHistory}/>
                <Route exact path={appContext + '/worker/:id/siddhi-apps/:appName/components/:componentType/' +
                ':componentId/history/:isStatsEnabled'}
                       component={ComponentHistory}/>
                <Route exact path={appContext +'/add-worker'} component={AddWorker}/>
                <Route exact path={appContext + '/worker/:id'} component={WorkerSpecific}/>
                <Route exact path={appContext + '/worker/history/:id'} component={WorkerHistory}/>
                <Route exact path={appContext + '/worker/history/:id/more'} component={WorkerHistoryMore}/>
                <Route component={Error404}/>
            </Switch>
        );
    }
}
