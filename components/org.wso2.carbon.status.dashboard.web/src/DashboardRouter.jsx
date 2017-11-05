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

import React from "react";
import {BrowserRouter as Router, Route} from "react-router-dom";
//App Components
import AddWorker from "./add-worker/AddWorker";
import WorkerOverview from "./overview/WorkerOverview";
import AppSpecific from "./siddhi-app/AppSpecific";
import WorkerSpecific from "./worker/WorkerSpecific";
import WorkerHistory from "./worker-history/WorkerHistory";
import AppHistory from "./siddhi-app/AppHistory";
import WorkerHistoryMore from "./worker-history-more/WorkerHistoryMore";
//Material UI
import darkBaseTheme from "material-ui/styles/baseThemes/darkBaseTheme";
import MuiThemeProvider from "material-ui/styles/MuiThemeProvider";
import getMuiTheme from "material-ui/styles/getMuiTheme";
import {AppBar} from "material-ui";
import LandingPage from "./common/LandingPage";

import Logo from './images/wso2-logo.svg';

const muiTheme = getMuiTheme(darkBaseTheme);
const title = {color: '#b9b9b9', fontSize: 18, height:40, lineHeight:2.2};
const appBar = {backgroundColor: '#1a1a1a'};
/**
 * class to manage routing of status dashboard component.
 */
export default class DashboardRouter extends React.Component {
    // TODO: 11/1/17 Remove Inline styles

    render() {
        let styles = {
            lineHeight: '64px',
            color: 'white',
            marginTop: '-10px',
            fontSize: '34px',
            marginRight: '5px'
        };
        return (
            <MuiThemeProvider muiTheme={muiTheme}>
                <div>
                    <AppBar
                        style={appBar}
                        title="Stream Processor Status Dashboard"
                        iconElementLeft={<img height='24' src={Logo}/>}
                        titleStyle={title}
                        iconStyleLeft={{margin:'8px 15px 0 0'}}
                    />
                    <Router>
                        <div>
                            <Route exact path='/sp-status-dashboard' component={LandingPage}/>
                            <Route exact path='*/overview' component={WorkerOverview}/>
                            <Route exact path='*/sp-status-dashboard/worker/:id/siddhi-apps/:appName/:isStatsEnabled'
                                   component={AppSpecific}/>
                            <Route exact path='*/sp-status-dashboard/worker/:id/siddhi-apps/:appName/app/history/:isStatsEnabled'
                                   component={AppHistory}/>
                            <Route exact path='*/add-worker' component={AddWorker}/>
                            <Route exact path='/sp-status-dashboard/worker/:id' component={WorkerSpecific}/>
                            <Route exact path='/sp-status-dashboard/worker/history/:id' component={WorkerHistory}/>
                            <Route exact path='/sp-status-dashboard/worker/history/:id/more'
                                   component={WorkerHistoryMore}/>
                        </div>
                    </Router>
                </div>
            </MuiThemeProvider>
        );
    }
}

