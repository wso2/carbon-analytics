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

import React from 'react';
import {BrowserRouter, Route, Switch} from 'react-router-dom';
//App Components
import Login from './auth/Login';
import Logout from './auth/Logout';
import SecuredRouter from './auth/SecuredRouter';
//Material UI
import darkBaseTheme from 'material-ui/styles/baseThemes/darkBaseTheme';
import MuiThemeProvider from 'material-ui/styles/MuiThemeProvider';
import getMuiTheme from 'material-ui/styles/getMuiTheme';

const appContext = window.contextPath;
const muiTheme = getMuiTheme(darkBaseTheme, {
    palette: {
        accent2Color: '#824722',
    }
});

/**
 * class to manage routing of status dashboard component.
 */
export default class DashboardRouter extends React.Component {
    render() {
        return (
            <MuiThemeProvider muiTheme={muiTheme}>
                <BrowserRouter history>
                    <Switch>
                        {/* Authentication */}
                        <Route path={`${appContext}/login`} component={Login}/>
                        <Route path={`${appContext}/logout`} component={Logout}/>
                        {/* Secured routes */}
                        <Route component={SecuredRouter}/>
                    </Switch>
                </BrowserRouter>
            </MuiThemeProvider>
        );
    }
}

