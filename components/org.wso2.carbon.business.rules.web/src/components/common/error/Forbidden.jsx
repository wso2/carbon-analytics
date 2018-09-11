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
// Styles
import Styles from '../../../style/Styles';
import '../../../index.css';

/**
 * App context
 */
const appContext = window.contextPath;

/**
 * Represents a Forbidden Error message display
 */
class Forbidden extends React.Component {
    render() {
        return (
            <Paper style={Styles.messageContainer}>
                <Typography type="title">
                    Forbidden
                </Typography>
                <Typography type="subheading">
                    Please login with enough permissions
                </Typography>
                <br />
                <Link to={`${appContext}/logout`} style={{ textDecoration: 'none' }}>
                    <Button color="primary">
                        Login
                    </Button>
                </Link>
            </Paper>
        );
    }
}

export default Forbidden;
