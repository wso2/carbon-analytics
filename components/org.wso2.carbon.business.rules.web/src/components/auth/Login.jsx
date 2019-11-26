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
import { Constants } from './Constants';


/**
 * App context
 */
const appContext = window.contextPath;

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
          this.initSSOAuthenticationFlow();
        } else {
          this.initDefaultAuthenticationFlow();
        }
        this.setState({ authType: response.data.authType });
      }).catch((e) => {
        console.error('Unable to get the authentication type. ', e);
      });
  }


  /**
   * Get the referrer URL for the redirection after successful login.
   * @returns {string | string}
   */
  getReferrer() {
    const referrer = localStorage.getItem(Constants.REFERRER_KEY);
    localStorage.removeItem(Constants.REFERRER_KEY);
    return referrer || '/';
  }

  /**
   * Set referrer URL to the local storage.
   * @param referrer
   */
  setReferrer(referrer) {
    if (localStorage.getItem(Constants.REFERRER_KEY) == null) {
      localStorage.setItem(Constants.REFERRER_KEY, referrer);
    }
  }

  getReferrerFromQueryString() {
    const { location } = this.props;
    const queryString = location.search.replace(/^\?/, '');
    return Qs.parse(queryString).referrer || '/';
  }

  /**
   * Initializes the default authentication flow
   */
  initDefaultAuthenticationFlow() {
    if (AuthManager.isRememberMeSet() && !AuthManager.isLoggedIn()) {
      AuthManager.authenticateWithRefreshToken()
        .then(() => this.setState({
          authenticated: true,
        }));
    }
  }

  /**
   * Initializes the SSO authentication flow.
   */
  initSSOAuthenticationFlow() {
    if (AuthManager.isSSOAuthenticated()) {
      const {
        authUser, pID, lID, validityPeriod, iID,
      } = AuthManager.getSSOUserCookie();
      localStorage.setItem('rememberMe', true);
      localStorage.setItem('username', authUser);
      AuthManager.setUser({
        username: authUser,
        SDID: pID,
        validity: validityPeriod,
        expires: AuthManager.calculateExpiryTime(validityPeriod),
      });
      AuthManager.setCookie(Constants.REFRESH_TOKEN_COOKIE, lID, 604800, window.contextPath);
      AuthManager.setCookie(Constants.ID_TOKEN_COOKIE, iID, 604800, window.contextPath);
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
          console.error('Error getting SSO auth URL.');
        });
    }
  }

  /**
   * Authenticates the user
   * @param {Object} e    Event
   */
  authenticate(e) {
    const { intl } = this.context;
    const { username, password, rememberMe } = this.state;
    e.preventDefault();
    AuthManager.authenticate(username, password, rememberMe)
      .then(() => this.setState({ authenticated: true }))
      .catch((error) => {
        const errorMessage = error.response && error.response.status === 401
          ? intl.formatMessage({ id: 'login.error.message', defaultMessage: 'Invalid username/password!' })
          : intl.formatMessage({ id: 'login.unknown.error', defaultMessage: 'Unknown error occurred!' });
        this.setState({
          username: '',
          password: '',
          error: errorMessage,
          showError: true,
        });
      });
  }

  renderDefaultLogin() {
    // const praivacy_policy = (<Link external="https://webmaker.org/en-US/terms">term</Link>);
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
    const { authenticated, authType } = this.state;
    if (authenticated) {
      return (
        <Redirect to={this.getReferrer()} />
      );
    }
    if (authType === Constants.AUTH_TYPE_UNKNOWN) {
      return <div />;
    }
    if (authType === Constants.AUTH_TYPE_SSO) {
      return <div />;
    }
    return this.renderDefaultLogin();
  }
}

Login.propTypes = {
  location: PropTypes.object.isRequired,
};

Login.contextTypes = {
  intl: PropTypes.object.isRequired,
};
