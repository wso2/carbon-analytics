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

import React, { Component } from 'react';
import PropTypes from 'prop-types';
import Qs from 'qs';
import { Route, Switch } from 'react-router-dom';
import { Redirect } from 'react-router';
// Material UI Components
import { createMuiTheme, MuiThemeProvider } from 'material-ui/styles';
// App Components
import BusinessRulesManager from '../pages/landingpage/LandingPage';
import TemplateGroupSelector from '../pages/templategroupselector/TemplateGroupSelector';
import BusinessRuleFromTemplateForm from '../pages/businessruleform/BusinessRuleFromTemplateForm';
import BusinessRuleFromScratchForm from '../pages/businessruleform/BusinessRuleFromScratchForm';
import BusinessRuleCreator from '../pages/modeselector/ModeSelector';
// Auth Utils
import AuthManager from '../../utils/AuthManager';
// Custom Theme
import { Orange } from '../../theme/BusinessRulesManagerColors';

const theme = createMuiTheme({
    palette: {
        primary: Orange,
    },
});

/**
 * App context
 */
const appContext = window.contextPath;

/**
 * Represents the App's router, which is accessible after a successful login
 */
export default class SecuredRouter extends Component {
    componentWillMount() {
        setInterval(() => {
            if (AuthManager.getUser()) {
                const expiresOn = new Date(AuthManager.getUser().expires);
                const skew = 100;
                if ((expiresOn - new Date()) / 1000 < skew) {
                    AuthManager.authenticateWithRefreshToken();
                }
            }
        }, 60000);
    }

    render() {
        // If the user is not logged in, redirect to the login page.
        if (!AuthManager.isLoggedIn()) {
            let referrer = this.props.location.pathname;
            const arr = referrer.split('');
            if (arr[arr.length - 1] !== '/') {
                referrer += '/';
            }

            const params = Qs.stringify({ referrer });
            return (
                <Redirect to={{ pathname: `${appContext}/login`, search: params }} />
            );
        }

        return (
            <Switch>
                <MuiThemeProvider theme={theme}>
                    <div>
                        <Redirect to={`${appContext}/businessRulesManager`} />
                        <Route exact path={`${appContext}/businessRuleCreator`} component={BusinessRuleCreator} />
                        <Route
                            exact
                            path={`${appContext}/businessRuleFromScratchForm/:formMode/` +
                                'templateGroup/:templateGroupUUID?/businessRule/:businessRuleUUID?'}
                            component={BusinessRuleFromScratchForm}
                        />
                        <Route
                            exact
                            path={`${appContext}/businessRuleFromTemplateForm/:formMode/` +
                                'templateGroup/:templateGroupUUID?/businessRule/:businessRuleUUID?'}
                            component={BusinessRuleFromTemplateForm}
                        />
                        <Route exact path={`${appContext}/businessRulesManager`} component={BusinessRulesManager} />
                        <Route
                            exact
                            path={`${appContext}/templateGroupSelector/:mode`}
                            component={TemplateGroupSelector}
                        />
                    </div>
                </MuiThemeProvider>
            </Switch>
        );
    }
}

SecuredRouter.propTypes = {
    location: PropTypes.string.isRequired,
};
