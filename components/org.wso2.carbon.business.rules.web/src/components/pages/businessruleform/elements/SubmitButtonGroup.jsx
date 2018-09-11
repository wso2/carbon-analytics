/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import React, { Component } from 'react';
import PropTypes from 'prop-types';
// Material UI Components
import Button from 'material-ui/Button';

/**
 * Styles related to this component
 */
const styles = {
    button: {
        marginRight: 10,
    },
};

/**
 * Represents the group of Save, Save and Deploy and Cancel buttons on a business rule form
 */
export default class SubmitButtonGroup extends Component {
    render() {
        return (
            <div>
                <Button
                    raised
                    color="default"
                    style={styles.button}
                    onClick={() => this.props.onSubmit(false)}
                >
                    Save
                </Button>
                <Button
                    raised
                    color="primary"
                    style={styles.button}
                    onClick={() => this.props.onSubmit(true)}
                >
                    Save & Deploy
                </Button>
                <Button
                    color="default"
                    style={styles.button}
                    onClick={() => this.props.onCancel()}
                >
                    Cancel
                </Button>
            </div>
        );
    }
}

SubmitButtonGroup.propTypes = {
    onSubmit: PropTypes.func.isRequired,
    onCancel: PropTypes.func.isRequired,
};
