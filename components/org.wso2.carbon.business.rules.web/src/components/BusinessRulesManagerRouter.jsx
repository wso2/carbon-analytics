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
 */

import React from 'react';
import { BrowserRouter, Route, Switch } from 'react-router-dom';
// Material UI Components
import { createMuiTheme, MuiThemeProvider } from 'material-ui/styles';
// Localization
import { IntlProvider, addLocaleData, defineMessages } from 'react-intl';
import Axios from 'axios';
// Auth Components
import SecuredRouter from './auth/SecuredRouter';
import Login from './auth/Login';
import Logout from './auth/Logout';
// Custom Theme
import { PrimaryColor } from '../theme/PrimaryColor';


/**
 * Language.
 * @type {string}
 */
const language = (navigator.languages && navigator.languages[0])
  || navigator.language
  || navigator.userLanguage;

/**
 * Language without region code.
 */
const languageWithoutRegionCode = language.toLowerCase().split(/[_-]+/)[0];
const theme = createMuiTheme({
  palette: {
    primary: PrimaryColor,
  },
});

/**
 * App context
 */
const appContext = window.contextPath;

/**
 * Router for the Business Rules Manager App
 */
export default class BusinessRulesManagerRouter extends React.Component {
  constructor() {
    super();
    this.state = {
      messages: {},
    };
  }

  /**
   * Initialize i18n.
   */
  componentWillMount() {
    const locale = languageWithoutRegionCode || language || 'en';
    this.loadLocale(locale).catch(() => {
      this.loadLocale().catch(() => {
        // TODO: Show error message.
      });
    });
  }

  /**
   * Load locale file.
   *
   * @param {string} locale Locale name
   * @returns {Promise} Promise
   */
  loadLocale(locale = 'en') {
    return new Promise((resolve, reject) => {
      Axios.get(`${window.contextPath}/public/app/locales/${locale}.json`)
        .then((response) => {
          // eslint-disable-next-line global-require, import/no-dynamic-require
          addLocaleData(require(`react-intl/locale-data/${locale}`));
          this.setState({ messages: defineMessages(response.data) });
          resolve();
        })
        .catch(error => reject(error));
    });
  }

  render() {
    return (
      <IntlProvider locale={language} messages={this.state.messages}>
        <BrowserRouter history>
          <Switch>
            <MuiThemeProvider theme={theme}>
              <div>
                {/* Authentication */}
                <Route path={`${appContext}/login`} component={Login} />
                <Route path={`${appContext}/logout`} component={Logout} />
                {/* Secured routes */}
                <Route component={SecuredRouter} />
              </div>
            </MuiThemeProvider>
          </Switch>
        </BrowserRouter>
      </IntlProvider>
    );
  }
}
