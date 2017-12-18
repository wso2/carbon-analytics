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

import {Button, Snackbar, TextField} from 'material-ui';
import Qs from 'qs';
import React from 'react';
import {Redirect} from 'react-router-dom';
// Material UI Components
import {FormControlLabel, FormGroup} from 'material-ui/Form';
import Slide from 'material-ui/transitions/Slide';
import Checkbox from 'material-ui/Checkbox';
// App Components
import FormPanel from '../common/FormPanel';
import Header from '../common/Header';
// Auth utils
import AuthManager from '../../utils/AuthManager';
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

/**
 * Login page.
 */
export default class Login extends React.Component {
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
            referrer: appContext,
        };
        this.authenticate = this.authenticate.bind(this);
    }

    componentWillMount() {
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
                const errorMessage = error.response && error.response.status === 401 ?
                    'The username/password is invalid' : 'Unknown error occurred!';;
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

            <MuiThemeProvider muiTheme={theme}>
                <Header hideUserSettings />
                <br />
                <div>
                    <FormPanel title="Login" onSubmit={this.authenticate}>
                        <TextField
                            fullWidth
                            id="username"
                            label="Username"
                            margin="normal"
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
                            type="password"
                            id="password"
                            label="Password"
                            margin="normal"
                            value={this.state.password}
                            onChange={(e) => {
                                this.setState({
                                    password: e.target.value,
                                    error: false,
                                });
                            }}
                        />
                        <br/>
                        <br />
                        <FormGroup>
                            <FormControlLabel
                                control={
                                    <Checkbox
                                        checked={this.state.rememberMe}
                                        onChange={(e, checked) => {
                                            this.setState({
                                                rememberMe: checked,
                                            })
                                        }}
                                        value="rememberMe"
                                    />
                                }
                                label="Remember me"
                            />
                        </FormGroup>
                        <br/>
                        <br/>
                        <Button
                            raised
                            color="primary"
                            type="submit"
                            disabled={this.state.username === '' || this.state.password === ''}
                        >
                            Login
                        </Button>
                    </FormPanel>
                    <Snackbar
                        autoHideDuration={3500}
                        open={this.state.showError}
                        onRequestClose={e => this.setState({showError: false})}
                        transition={<Slide direction={'up'}/>}
                        SnackbarContentProps={{
                            'aria-describedby': 'snackbarMessage',
                        }}
                        message={
                            <span id="snackbarMessage">
                        {this.state.error}
                    </span>
                        }
                    />
                </div>
            </MuiThemeProvider>
        );
    }
}
