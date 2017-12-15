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

import React, {Component} from 'react';
import Header from "../common/Header";

import MuiThemeProvider from 'material-ui/styles/MuiThemeProvider';
import darkBaseTheme from 'material-ui/styles/baseThemes/darkBaseTheme';
import getMuiTheme from 'material-ui/styles/getMuiTheme';
import {Link} from "react-router-dom";
import {RaisedButton} from "material-ui";
const muiTheme = getMuiTheme(darkBaseTheme);
const buttonStyle = {marginLeft: 60, width: 100, fontSize: '12px',backgroundColor:'#f17b31'};
const errorTitleStyles = {
    color: "#C3C6CD",
    fontSize: 45
};

const errorMessageStyles = {
    color: "#abaeb4",
    fontSize: 22
};

const errorContainerStyles = {
    textAlign: "center",
    marginTop:40
};

/**
 *  This component provide a basic 401 error page.
 */
class Error404 extends Component {
    render() {
        return <MuiThemeProvider muiTheme={muiTheme}>
            <Header/>
            <div style={errorContainerStyles}>
                <h1 style={errorTitleStyles}>Not Found</h1>
                <h3 style={errorMessageStyles}>The page you looking for was moved, renamed, <br/>removed or might never existed.</h3>
                <Link to={window.contextPath}><RaisedButton backgroundColor='#f17b31' style={buttonStyle} label="Back"/></Link>
            </div>
        </MuiThemeProvider>;
    }
}
export default Error404;
