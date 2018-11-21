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
import Header from '../common/Header';

import MuiThemeProvider from 'material-ui/styles/MuiThemeProvider';
import darkBaseTheme from 'material-ui/styles/baseThemes/darkBaseTheme';
import getMuiTheme from 'material-ui/styles/getMuiTheme';
import {Link} from 'react-router-dom';
import {Card, CardHeader, CardMedia, CardText, CardTitle, RaisedButton} from 'material-ui';
import FormPanel from '../common/FormPanel';
import { FormattedMessage } from 'react-intl';

const muiTheme = getMuiTheme(darkBaseTheme);
const buttonStyle = {position: 'center', marginTop: 60, width: '15%', fontSize: '12px', backgroundColor: '#f17b31'};
const errorTitleStyles = {
    color: "#c7cad1",
    fontSize: 45
};

const errorMessageStyles = {
    color: "#abaeb4",
    fontSize: 22
};

const errorContainerStyles = {
    textAlign: "center",
    marginTop: 30
};

/**
 *  This component provide a basic 401 error page.
 */
class Error404 extends Component {
    render() {
        return <MuiThemeProvider muiTheme={muiTheme}>
            <Header/>
            <Card style={{
                width: 700, high: '100%', marginTop: '10%', marginLeft: '33%', backgroundColor: '#1a1a1a'
                , position: 'center'
            }}>
                <CardText style={{borderBottom: '1px solid #AE5923', borderTop: '1px solid #AE5923'}}>
                    <FormPanel title={""} width={650}>
                        <div style={errorContainerStyles}>
                            <h1 style={errorTitleStyles}>
                                <FormattedMessage id='error.404.notFound' defaultMessage='Page Not Found!' />
                            </h1>
                            <text style={errorMessageStyles}>
                                <FormattedMessage id='error.404.description' defaultMessage='The page you looking for was moved, renamed,{br} removed or might never existed. ' values={{ br: <br /> }} />
                            </text>
                            <Link to={window.contextPath}>
                                <br/>
                                <br/>
                                <RaisedButton backgroundColor='#f17b31' style={buttonStyle} label={<FormattedMessage id='error.404.home' defaultMessage='Home' />} /></Link>
                        </div>
                    </FormPanel>
                </CardText>
            </Card>
        </MuiThemeProvider>;
    }
}

export default Error404;
