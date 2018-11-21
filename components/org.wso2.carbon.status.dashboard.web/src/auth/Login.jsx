/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the 'License'); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  'AS IS' BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

import {Checkbox, RaisedButton, Snackbar, TextField} from 'material-ui';
import {darkBaseTheme, getMuiTheme, MuiThemeProvider} from 'material-ui/styles';
import Qs from 'qs';
import React, {Component} from 'react';
import {Redirect} from 'react-router-dom';
import PropTypes from 'prop-types';


import AuthManager from './utils/AuthManager';
import FormPanel from '../common/FormPanel';
import Header from '../common/Header';
import '../../public/css/dashboard.css';
//Localization
import { FormattedMessage } from 'react-intl';

const muiTheme = getMuiTheme(darkBaseTheme);
const titleStyle = {textAlign: 'center', marginTop: 50, color: '#9c9898'};
const textField = {width: 450};
const buttonStyle = {marginLeft: '35%', width: '40%'};
/**
 * Style constants.
 */
const styles = {
    messageBox: {textAlign: 'center', color: 'white'},
    errorMessage: {backgroundColor: '#333333', color: 'white', border: '2px solid #e74c3c'},
    cookiePolicy: {
        padding: '10px', fontFamily: muiTheme.fontFamily,
        border: '1px solid #8a6d3b', color: '#8a6d3b'
    },
    cookiePolicyAnchor: {fontWeight: '400', color: '#0D47A1', fontStyle: 'italic' }
};

/**
 * Login page.
 */
export default class Login extends Component {
    /**
     * Constructor.
     *
     * @param {{}} props Props
     */
    constructor(props) {
        super(props);
        this.state = {
            username: '',
            password: '',
            authenticated: false,
            rememberMe: false,
            referrer: window.contextPath,
        };
        this.authenticate = this.authenticate.bind(this);
    }

    componentWillMount() {
        if (AuthManager.isRememberMeSet() && !AuthManager.isLoggedIn()) {
            AuthManager.authenticateWithRefreshToken()
                .then(() => this.setState({authenticated: true}));
        }
    }

    /**
     * Extract the referrer and check whether the user logged-in.
     */
    componentDidMount() {
        // Extract referrer from the query string.
        const queryString = this.props.location.search.replace(/^\?/, '');
        const params = Qs.parse(queryString);
        if (params.referrer) {
            this.state.referrer = params.referrer;
        }
        // If the user already logged in set the state to redirect user to the referrer page.
        if (AuthManager.isLoggedIn()) {
            this.state.authenticated = true;
        }
    }

    /**
     * Call authenticate API and authenticate the user.
     *
     * @param {{}} e event
     */
    authenticate(e) {
        e.preventDefault();
        AuthManager
            .authenticate(this.state.username, this.state.password, this.state.rememberMe)
            .then(() => this.setState({authenticated: true}))
            .catch((error) => {
                const errorMessage = error.response && error.response.status === 401 ? this.context.intl.formatMessage({ id: 'login.invalidUsername', defaultMessage: 'Invalid username/password!' }) :
                    this.context.intl.formatMessage({ id: 'login.unknownError', defaultMessage: 'Unknown error occurred!' });
                this.setState({
                    username: '',
                    password: '',
                    error: errorMessage,
                    showError: true,
                });
            });
    }

    /**
     * Renders the login page.
     *
     * @return {XML} HTML content
     */
    render() {
        // If the user is already authenticated redirect to referrer link.
        if (this.state.authenticated) {
            return (
                <Redirect to={this.state.referrer}/>
            );
        }

        const cookiePolicy = (
            <a
            style={styles.cookiePolicyAnchor}
            href='/policies/cookie-policy'
            target='_blank'>
                <FormattedMessage id='login.cookiePolicy' defaultMessage='Cookie Policy' />
            </a>);

        const privacyPolicy = (
            <a
                style={styles.cookiePolicyAnchor}
                href='/policies/privacy-policy'
                target='_blank'>
                    <FormattedMessage id='login.privacyPolicy' defaultMessage='Privacy Policy' />
            </a>
        )    
        return (
            <MuiThemeProvider muiTheme={muiTheme}>
                <div className={'login-container'}>
                    <Header title={window.contextPath.substr(1)} hideUserSettings/>
                    <FormPanel 
                        title={<FormattedMessage id='login.title' defaultMessage='Login' />} 
                        onSubmit={this.authenticate}>                  
                        <TextField
                            floatingLabelFocusStyle={{color: '#f17b31'}}
                            underlineFocusStyle={{borderColor: '#f17b31'}}
                            //style={textField}
                            fullWidth
                            floatingLabelText={<FormattedMessage id='login.username' defaultMessage='Username' />}
                            autoComplete="off"
                            value={this.state.username}
                            onChange={(e) => {
                                this.setState({
                                    username: e.target.value,
                                    error: false,
                                });
                            }}
                        />
                        <br/>
                        <TextField
                            fullWidth
                            type='password'
                            autoComplete='off'
                            floatingLabelText={<FormattedMessage id='login.password' defaultMessage='Password' />}
                            floatingLabelFocusStyle={{color: '#f17b31'}}
                            underlineFocusStyle={{borderColor: '#f17b31'}}
                            //style={textField}
                            value={this.state.password}
                            onChange={(e) => {
                                this.setState({
                                    password: e.target.value,
                                    error: false,
                                });
                            }}
                        />
                        <br/>
                        <Checkbox
                            label={<FormattedMessage id='login.rememberMe' defaultMessage='Remember Me' />}
                            checked={this.state.rememberMe}
                            iconStyle={{fill: '#f17b31'}}
                            onCheck={(e, checked) => {
                                this.setState({
                                    rememberMe: checked,
                                });
                            }}
                            style={{'margin': '30px 0'}}
                            labelStyle={{color: '#BDBDBD'}}
                        />
                        <br/>
                        <RaisedButton
                            type='submit'
                            disabled={this.state.username === '' || this.state.password === ''}
                            label={<FormattedMessage id='login.loginButton' defaultMessage='Login' />}
                            style={buttonStyle}
                            backgroundColor='#f17b31'
                            disabledBackgroundColor='rgb(51, 51, 51)'
                        />
                        <br/>
                        <br/>
                        <div style={styles.cookiePolicy}>
                            <div>
                                <FormattedMessage id='login.cookieMsg' defaultMessage='After a successful sign in, we use a cookie in your browser to track your session. You can refer our {cookiePolicy} for more details.' values={{cookiePolicy:cookiePolicy}} />                                
                            </div>
                        </div>
                        <br/>
                        <div style={styles.cookiePolicy}>
                            <div>
                                <FormattedMessage id='login.bysigningUp' defaultMessage='By signing in, you agree to our {privacyPolicy}' values={{privacyPolicy:privacyPolicy}} />                                
                            </div>
                        </div>
                    </FormPanel>
                    <Snackbar
                        message={this.state.error}
                        open={this.state.showError}
                        autoHideDuration='4000'
                        contentStyle={styles.messageBox}
                        bodyStyle={styles.errorMessage}
                        onRequestClose={() => this.setState({error: '', showError: false})}
                    />
                </div>
            </MuiThemeProvider>
        );
    }
}

Login.contextTypes = {
    intl: PropTypes.object.isRequired
};
