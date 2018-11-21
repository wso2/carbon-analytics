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
import { Link } from 'react-router-dom';
// Material UI Components
import Button from 'material-ui/Button';
import Typography from 'material-ui/Typography';
import Paper from 'material-ui/Paper';
// Localization
import { FormattedMessage } from 'react-intl';
// Styles
import Styles from '../../../style/Styles';
import '../../../index.css';

/**
 * App context
 */
const appContext = window.contextPath;

/**
 * Represents an Access Denied Error message display
 */
class AccessDenied extends React.Component {
  render() {
    return (
      <Paper style={Styles.messageContainer}>
        <Typography type="title">
          <FormattedMessage
            id="error.accessdenied.title"
            defaultMessage="Access Denied"
          />
        </Typography>
        <Typography type="subheading">
          <FormattedMessage
            id="error.accessdenied.subheading"
            defaultMessage="Please login with valid permissions"
          />
        </Typography>
        <br />
        <Link to={`${appContext}/logout`} style={{ textDecoration: 'none' }}>
          <Button color="primary">
            <FormattedMessage id="login.button" defaultMessage="Login" />
          </Button>
        </Link>
      </Paper>
    );
  }
}

export default AccessDenied;
