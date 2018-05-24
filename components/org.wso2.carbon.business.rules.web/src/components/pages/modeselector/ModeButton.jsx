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

import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { Link } from 'react-router-dom';
// Material UI Components
import Typography from 'material-ui/Typography';
import Button from 'material-ui/Button';
import List from 'material-ui-icons/List';
import Create from 'material-ui-icons/Create';
import Paper from 'material-ui/Paper';
// App Constants
import BusinessRulesConstants from '../../../constants/BusinessRulesConstants';
// CSS
import '../../../index.css';

/**
 * Styles related to this component
 */
const styles = {
    button: {
        height: 100,
        width: 100,
        color: '#EF6C00',
        backgroundColor: '#212121',
    },
    paper: {
        width: 300,
        padding: 50,
    },
};

/**
 * App context
 */
const appContext = window.contextPath;

/**
 * Represents a button which allows to select a mode for creating a business rule,
 * either 'from template' or 'from scratch'
 */
export default class ModeButton extends Component {
    render() {
        let icon;
        if (this.props.mode === BusinessRulesConstants.BUSINESS_RULE_TYPE_TEMPLATE) {
            icon = <List />;
        } else {
            icon = <Create />;
        }

        return (
            <Paper style={styles.paper}>
                <Link
                    to={appContext + '/templateGroupSelector/' + this.props.mode}
                    style={{ textDecoration: 'none' }}
                >
                    <Button fab style={styles.button}>
                        {icon}
                    </Button>
                </Link>
                <br />
                <br />
                <br />
                <Typography type="headline" component="h2">
                    {this.props.title}
                </Typography>
                <Typography component="subheading" color="secondary">
                    {this.props.description}
                </Typography>
            </Paper>
        );
    }
}

ModeButton.propTypes = {
    mode: PropTypes.oneOf([
        BusinessRulesConstants.BUSINESS_RULE_TYPE_TEMPLATE,
        BusinessRulesConstants.BUSINESS_RULE_TYPE_SCRATCH,
    ]).isRequired,
    title: PropTypes.string.isRequired,
    description: PropTypes.string.isRequired,
};
