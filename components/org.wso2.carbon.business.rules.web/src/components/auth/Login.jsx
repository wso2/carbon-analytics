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

import Qs from 'qs';
import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { Redirect } from 'react-router-dom';
// Material UI Components
import { Button, Snackbar, TextField } from 'material-ui';
import { FormControlLabel, FormGroup } from 'material-ui/Form';
import Slide from 'material-ui/transitions/Slide';
import Checkbox from 'material-ui/Checkbox';
// Localization
import { FormattedMessage } from 'react-intl';
// App Components
import FormPanel from '../common/FormPanel';
import Header from '../common/Header';
// Auth Utils
import AuthManager from '../../utils/AuthManager';
import {Constants} from '../../utils/Constants';


/**
 * App context
 */
const REFERRER_KEY = 'referrer';

const styles = {
  cookiePolicy: {
    padding: '10px',
    backgroundColor: '#fcf8e3',
    border: '1px solid #faebcc',
    color: '#8a6d3b',
    fontFamily: '"Roboto", "Helvetica", "Arial", sans-serif',
  },
  cookiePolicyAnchor: {
    fontWeight: 'bold',
    color: '#8a6d3b',
    textDecoration: 'none',
  },
};

/**
 * Login page
 */
export default class Login extends Component {
  constructor(props) {
    super(props);
    this.state = {
      username: '',
      password: '',
      authenticated: false,
      rememberMe: false,
        authType: Constants.AUTH_TYPE_UNKNOWN,
    };
      this.setReferrer(this.getReferrerFromQueryString());
      this.authenticate = this.authenticate.bind(this);
  }

    componentWillMount() {
        AuthManager.getAuthType()
            .then((response) => {
                if (response.data.authType === Constants.AUTH_TYPE_SSO) {
                    // initialize sso authentication flow
                    this.initSSOAuthenticationFlow();
                } else {
                    // initialize default authentication flow
                    this.initDefaultAuthenticationFlow();
                }
                // Once the auth type has been changed asynchronously, login page needs to be re-rendered to show the
                // login form.
                this.setState({authType: response.data.authType});
            }).catch((e) => {
            console.error('Unable to get the authentication type.', e);
        });
    }

    /**
     * Initializes the default authentication flow.
     */
    initDefaultAuthenticationFlow() {
        if (AuthManager.isRememberMeSet() && !AuthManager.isLoggedIn()) {
            AuthManager.authenticateWithRefreshToken()
                .then(() =>
                    this.setState({
                        authenticated: true
                    })
                );
        }
    }

    /**
     * Initializes the SSO authentication flow.
     */
    initSSOAuthenticationFlow() {
        // check if the userDTO is available. if so try to authenticate the user. instead forward the user to the idp.
        // USER_DTO={"authUser":"admin","pID":"0918c1ad-fc0a-35fa","lID":"3ca21853-bd3c-3dfa","validityPeriod":3381};

        if (AuthManager.isSSOAuthenticated()) {
            const {authUser, pID, lID, validityPeriod, iID} = AuthManager.getSSOUserCookie();
            localStorage.setItem('rememberMe', true);
            localStorage.setItem('username', authUser);
            AuthManager.setUser({
                username: authUser,
                SDID: pID,
                validity: validityPeriod,
                expires: AuthManager.calculateExpiryTime(validityPeriod),
            });
            AuthManager.setCookie(Constants.RTK, lID, 604800, window.contextPath);
            AuthManager.setCookie(Constants.ID_TOKEN, iID, 604800, window.contextPath);
            AuthManager.deleteCookie(Constants.USER_DTO_COOKIE);
            this.setState({
                authenticated: true,
            });
        } else {
            // redirect the user to the service providers auth url
            AuthManager.ssoAuthenticate()
                .then((url) => {
                    window.location.href = url;
                })
                .catch((e) => {
                    console.error('Error getting SSO auth URL.', e);
                });
        }
    }

    /**
     * Get the referrer URL for the redirection after successful login.
     *
     * @returns {string} referrer URL
     */
    getReferrer() {
        const referrer = localStorage.getItem(REFERRER_KEY);
        localStorage.removeItem(REFERRER_KEY);
        return referrer || '/';
    }

    /**
     * Set referrer URL to the local storage.
     *
     * @param {String} referrer Referrer URL
     */
    setReferrer(referrer) {
        if (localStorage.getItem(REFERRER_KEY) == null) {
            localStorage.setItem(REFERRER_KEY, referrer);
        }
    }

    /**
     * Extract referrer URL from the query string.
     *
     * @returns {string} referrer URL
     */
    getReferrerFromQueryString() {
        const queryString = this.props.location.search.replace(/^\?/, '');
        return Qs.parse(queryString).referrer;
    }

    /**
   * Authenticates the user
   * @param {Object} e    Event
   */
  authenticate(e) {
    const {intl} = this.context;
    const {username, password, rememberMe} = this.state;
    e.preventDefault();
    AuthManager.authenticate(username, password, rememberMe)
      .then(() => this.setState({authenticated: true}))
      .catch((error) => {
        const errorMessage = error.response && error.response.status === 401
                    ? intl.formatMessage({id: 'login.error.message', defaultMessage: 'Invalid username/password!'})
                    : intl.formatMessage({id: 'login.unknown.error', defaultMessage: 'Unknown error occurred!'});
        this.setState({
          username: '',
          password: '',
          error: errorMessage,
          showError: true,
        });
      });
  }

    renderDefaultLogin() {
    const cookiePolicy = (
      <a
        style={styles.cookiePolicyAnchor}
        href="/policies/cookie-policy"
        target="_blank"
      >
        <FormattedMessage id="login.CookiePolicy.anchor" defaultMessage="Cookie Policy" />
      </a>
    );

    const privacyPolicy = (
      <a
        style={styles.cookiePolicyAnchor}
        href="/policies/privacy-policy"
        target="_blank"
      >
        <FormattedMessage
          id="login.privacy.policy.title"
          defaultMessage="Privacy Policy"
        />
      </a>
    );

    return (
      <div>
        <Header hideUserSettings />
        <br />
        <div>
          <FormPanel
            title={<FormattedMessage id="login.title" defaultMessage="Login" />}
            onSubmit={this.authenticate}
          >
            <TextField
              fullWidth
              id="username"
              label={(
                <FormattedMessage
                  id="login.username"
                  defaultMessage="Username"
                />
)}
              margin="normal"
              autoComplete="off"
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
              id="password"
              label={(
                <FormattedMessage
                  id="login.password"
                  defaultMessage="Password"
                />
)}
              margin="normal"
              autoComplete="off"
              value={this.state.password}
              onChange={(e) => {
                this.setState({
                  password: e.target.value,
                  error: false,
                });
              }}
            />
            <br />
            <br />
            <FormGroup>
              <FormControlLabel
                control={(
                  <Checkbox
                    checked={this.state.rememberMe}
                    onChange={(e, checked) => {
                      this.setState({
                        rememberMe: checked,
                      });
                    }}
                    value="rememberMe"
                  />
)}
                label={(
                  <FormattedMessage
                    id="login.rememberMe"
                    defaultMessage="Remember Me"
                  />
)}
              />
            </FormGroup>
            <br />
            <br />
            <Button
              raised
              color="primary"
              type="submit"
              disabled={
                this.state.username === '' || this.state.password === ''
              }
            >
              <FormattedMessage id="login.title" defaultMessage="Login" />
            </Button>
            <br />
            <br />
            <div style={styles.cookiePolicy}>
              <div>
                <FormattedMessage
                  id="login.cookie.policy"
                  defaultMessage=" After a successful sign in, we use a cookie in your browser to
                track your session. You can refer our {cookiePolicy} for more details."
                  values={{ cookiePolicy }}
                />
              </div>
            </div>
            <br />
            <div style={styles.cookiePolicy}>
              <div>
                <FormattedMessage
                  id="login.privacy.policy.description"
                  defaultMessage="By signing in, you agree to our {privacyPolicy}"
                  values={{ privacyPolicy }}
                />
              </div>
            </div>
          </FormPanel>
          <Snackbar
            autoHideDuration={3500}
            open={this.state.showError}
            onRequestClose={() => this.setState({ showError: false })}
            transition={<Slide direction="up" />}
            SnackbarContentProps={{
              'aria-describedby': 'snackbarMessage',
            }}
            message={<span id="snackbarMessage">{this.state.error}</span>}
          />
        </div>
      </div>
    );
  }

    render() {
        const {authenticated, authType} = this.state;
        // If the user is already authenticated redirect to referrer link.
        if (authenticated) {
            return (
                <Redirect to={this.getReferrer()}/>
            );
        }

        // If the authType is not defined, show a blank page (or loading gif).
        if (authType === Constants.AUTH_TYPE_UNKNOWN) {
            return <div/>;
        }

        // If the authType is sso, show a blank page since the redirection is pending.
        if (authType === Constants.AUTH_TYPE_SSO) {
            return <div/>;
        }

        // Render the default login form.
        return this.renderDefaultLogin();
    }

}

Login.propTypes = {
  location: PropTypes.string.isRequired,
};
