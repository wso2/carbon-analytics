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
import ReactDOM from 'react-dom';
import {BrowserRouter, Route, Switch} from 'react-router-dom';
import {Redirect} from 'react-router';
// App Components
import BusinessRulesManager from "./components/BusinessRulesManager";
// App Utilities
import TemplateGroupSelector from "./components/TemplateGroupSelector";
import ProgressDisplay from "./components/ProgressDisplay";
import BusinessRuleFromTemplateForm from "./components/BusinessRuleFromTemplateForm";
import BusinessRuleFromScratchForm from "./components/BusinessRuleFromScratchForm";
import BusinessRuleCreator from "./components/BusinessRuleCreator";
import Header from "./components/Header";
// Custom Theme
import {createMuiTheme, MuiThemeProvider} from 'material-ui/styles';
import {Orange} from './components/styles/BusinessRulesManagerColors';

const theme = createMuiTheme({
    palette: {
        primary: Orange,
    },
});

ReactDOM.render(
    <BrowserRouter history>
        <Switch>
            <MuiThemeProvider theme={theme}>
                <div>
                    <Header/>
                    <br/>
                    <Redirect to='/business-rules/businessRulesManager'/>
                    <Route exact path="/business-rules/businessRuleCreator" component={BusinessRuleCreator}/>
                    <Route exact
                           path="/business-rules/businessRuleFromScratchForm/:formMode/templateGroup/:templateGroupUUID?/businessRule/:businessRuleUUID?"
                           component={BusinessRuleFromScratchForm}/>
                    <Route exact
                           path="/business-rules/businessRuleFromTemplateForm/:formMode/templateGroup/:templateGroupUUID?/businessRule/:businessRuleUUID?"
                           component={BusinessRuleFromTemplateForm}/>
                    <Route exact path="/business-rules/businessRulesManager" component={BusinessRulesManager}/>
                    <Route exact path="/business-rules/progressDisplay" component={ProgressDisplay}/>
                    <Route exact path="/business-rules/templateGroupSelector/:mode" component={TemplateGroupSelector}/>
                </div>
            </MuiThemeProvider>
        </Switch>
    </BrowserRouter>,
    document.getElementById('root')
);