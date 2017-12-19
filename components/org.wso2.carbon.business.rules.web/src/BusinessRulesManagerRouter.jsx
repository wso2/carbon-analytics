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
 */

import React from 'react';
import {BrowserRouter, Route, Switch} from 'react-router-dom';
// Auth Components
import SecuredRouter from "./components/auth/SecuredRouter";
import Login from "./components/auth/Login";
import Logout from "./components/auth/Logout";
// App Components
import Header from "./components/common/Header";
// Custom Theme
import {createMuiTheme, MuiThemeProvider} from 'material-ui/styles';
import {Orange} from './theme/BusinessRulesManagerColors';

const theme = createMuiTheme({
    palette: {
        primary: Orange,
    },
});

/**
 * App context.
 */
const appContext = window.contextPath;

export default class BusinessRulesManagerRouter extends React.Component {
    render() {
        return (
            <BrowserRouter history>
                <Switch>
                    <MuiThemeProvider theme={theme}>
                        <div>
                            {/* Authentication */}
                            <Route path={`${appContext}/login`} component={Login}/>
                            <Route path={`${appContext}/logout`} component={Logout}/>
                            {/* Secured routes */}
                            <Route component={SecuredRouter}/>
                        </div>
                    </MuiThemeProvider>
                </Switch>
            </BrowserRouter>
        );
    }
}