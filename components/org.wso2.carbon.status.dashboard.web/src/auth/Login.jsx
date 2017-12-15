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

import {Checkbox, RaisedButton, Snackbar, TextField} from "material-ui";
import {darkBaseTheme, getMuiTheme, MuiThemeProvider} from "material-ui/styles";
import Qs from "qs";
import React, {Component} from "react";
import {Redirect} from "react-router-dom";
import PropTypes from "prop-types";


import AuthManager from "./utils/AuthManager";
import FormPanel from "../common/FormPanel";
import Header from "../common/Header";

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

    componentWillMount(){
        if (AuthManager.isRememberMeSet()) {
            AuthManager.authenticateWithRefreshToken()
                .then((response) => {
                    this.setState({authenticated: true})
                });
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
                const errorMessage = error.response && error.response.status === 401 ? "Invalid username/password!":
                    "Unknown error occurred!";
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

        return (
            <MuiThemeProvider muiTheme={muiTheme}>
                <div>
                    <Header title={"monitoring"} hideUserSettings/>
                    <FormPanel title={"Login"}
                               onSubmit={this.authenticate}>
                        <TextField
                            floatingLabelFocusStyle={{color: '#f17b31'}}
                            underlineFocusStyle={{borderColor: '#f17b31'}}
                            //style={textField}
                            fullWidth
                            floatingLabelText={"Username"}
                            value={this.state.username}
                            onChange={(e) => {
                                this.setState({
                                    username: e.target.value,
                                    error: false,
                                });
                            }}
                        />
                        <br />
                        <TextField
                            fullWidth
                            type="password"
                            floatingLabelText={"Password"}
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
                        <br />
                        <Checkbox
                            label={"Remember Me"}
                            checked={this.state.rememberMe}
                            iconStyle={{fill: '#f17b31'}}
                            onCheck={(e, checked) => {
                                this.setState({
                                    rememberMe: checked,
                                });
                            }}
                            style={{'margin': '30px 0'}}
                        />
                        <br />
                        <RaisedButton
                            type="submit"
                            disabled={this.state.username === '' || this.state.password === ''}
                            label={"Login"}
                            style={buttonStyle}
                            backgroundColor='#f17b31'
                            disabledBackgroundColor="rgb(51, 51, 51)"
                        />
                    </FormPanel>
                    <Snackbar
                        message={this.state.error}
                        open={this.state.showError}
                        autoHideDuration="4000"
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