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

import React, { Component } from 'react';
import PropTypes from 'prop-types';
// Material UI Components
import Typography from 'material-ui/Typography';
import Paper from 'material-ui/Paper';

/**
 * Represents the panel which consists forms
 */
export default class FormPanel extends Component {
    render() {
        const wrapperStyles = {
            margin: '0 auto',
            width: this.props.width,
            paddingTop: this.props.paddingTop,
            paddingBottom: 15,
        };

        return (
            <div style={wrapperStyles}>
                <Paper style={{ padding: 50 }}>
                    <form method="post" onSubmit={this.props.onSubmit}>
                        <Typography type="headline">{this.props.title}</Typography>
                        {this.props.children}
                    </form>
                </Paper>
            </div>
        );
    }
}

FormPanel.propTypes = {
    title: PropTypes.string,
    onSubmit: PropTypes.func.isRequired,
    width: PropTypes.number,
    paddingTop: PropTypes.number,
};

FormPanel.defaultProps = {
    title: '',
    width: 450,
    paddingTop: 60,
};
