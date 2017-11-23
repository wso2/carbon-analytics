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
import Qs from 'qs';
import {Route, Switch} from 'react-router-dom';
import {Redirect} from 'react-router';
// App Components
import BusinessRulesManager from "../BusinessRulesManager";
import TemplateGroupSelector from "../TemplateGroupSelector";
import ProgressDisplay from "../ProgressDisplay";
import BusinessRuleFromTemplateForm from "../BusinessRuleFromTemplateForm";
import BusinessRuleFromScratchForm from "../BusinessRuleFromScratchForm";
import BusinessRuleCreator from "../BusinessRuleCreator";
// Auth Utilities
import AuthManager from "../../utils/AuthManager";
// Custom Theme
import {createMuiTheme, MuiThemeProvider} from 'material-ui/styles';
import {Orange} from "../../theme/BusinessRulesManagerColors";

const theme = createMuiTheme({
    palette: {
        primary: Orange,
    },
});

/**
 * App context.
 */
const appContext = window.contextPath;

export default class SecuredRouter extends React.Component {
    render() {
        // If the user is not logged in, redirect to the login page.
        if (!AuthManager.isLoggedIn()) {
            const params = Qs.stringify({referrer: this.props.location.pathname});
            return (
                <Redirect to={{pathname: `${appContext}/login`, search: params}}/>
            );
        }

        return (
            <Switch>
                <MuiThemeProvider theme={theme}>
                    <div>
                        <Redirect to={`${appContext}/businessRulesManager`}/>
                        <Route exact path={`${appContext}/businessRuleCreator`} component={BusinessRuleCreator}/>
                        <Route exact
                               path={`${appContext}/businessRuleFromScratchForm/:formMode/templateGroup/:templateGroupUUID?/businessRule/:businessRuleUUID?`}
                               component={BusinessRuleFromScratchForm}/>
                        <Route exact
                               path={`${appContext}/businessRuleFromTemplateForm/:formMode/templateGroup/:templateGroupUUID?/businessRule/:businessRuleUUID?`}
                               component={BusinessRuleFromTemplateForm}/>
                        <Route exact path={`${appContext}/businessRulesManager`} component={BusinessRulesManager}/>
                        <Route exact path={`${appContext}/progressDisplay`} component={ProgressDisplay}/>
                        <Route exact path={`${appContext}/templateGroupSelector/:mode`}
                               component={TemplateGroupSelector}/>
                    </div>
                </MuiThemeProvider>
            </Switch>
        );
    }
}
