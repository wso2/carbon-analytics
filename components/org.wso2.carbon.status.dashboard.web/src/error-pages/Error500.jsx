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
import {Card, CardHeader, CardMedia, CardText, CardTitle, RaisedButton} from "material-ui";
import FormPanel from "../common/FormPanel";
const muiTheme = getMuiTheme(darkBaseTheme);
const buttonStyle = {position: 'center',marginTop: 60, width: '15%', fontSize: '12px',backgroundColor:'#f17b31'};
const errorTitleStyles = {
    color: "#c7cad1",
    fontSize: 45
};

const errorMessageStyles = {
    color: "#abaeb4",
    fontSize: 18
};

const errorContainerStyles = {
    textAlign: "center",
    marginTop:30
};

/**
 *  This component provide a basic 401 error page.
 */
class Error500 extends Component {
    constructor(props) {
        super(props);
        this.state = {
            errorMessage: this.props.message
        }
    }
    render() {
        return <MuiThemeProvider muiTheme={muiTheme}>
            <Header/>
            <Card style={{width:700,high:'100%',marginTop:'10%',marginLeft: '33%',backgroundColor:'#1a1a1a'
                ,  position: 'center'}}>
                <CardText  style={{borderBottom:'1px solid #AE5923',borderTop:'1px solid #AE5923'}}>
                    <FormPanel title={""} width={650}>
                        <div style={errorContainerStyles}>
                            <i class="fw fw-error  fw-inverse fw-5x"></i>
                            <h1 style={errorTitleStyles}>
                                Internal Server Error!
                            </h1>
                            <text style={errorMessageStyles}>There is a problem with the resource you are looking for,
                                <br/> and it cannot be displayed.</text>
                            <br/>
                            <br/>
                            <Link to={`${window.contextPath}/logout`} >
                                <RaisedButton backgroundColor='#f17b31' style={buttonStyle} label="Login"/>
                            </Link>
                        </div>
                    </FormPanel>
                </CardText>
            </Card>
        </MuiThemeProvider>;
    }
}
export default Error500;
