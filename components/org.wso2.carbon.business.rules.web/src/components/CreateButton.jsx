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
 */

import React from 'react';
import {Link} from 'react-router-dom';
// Material UI Components
import Typography from 'material-ui/Typography';
import Button from 'material-ui/Button';
import List from 'material-ui-icons/List';
import Create from 'material-ui-icons/Create';
import Paper from 'material-ui/Paper';
// App Utilities
import BusinessRulesConstants from "../utils/BusinessRulesConstants";
// CSS
import '../index.css';

// Styles related to this component
const styles = {
    button: {
        height: 100,
        width: 100,
        color: '#EF6C00',
        backgroundColor: '#212121',
    },
    paper: {
        width: 300,
        padding: 50
    }
}

/**
 * Represents a Create Button used in the Business Rule Creator, which will direct to
 * the specific create business rule page
 */
class CreateButton extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            mode: props.mode,
            text: props.text,
            onClick: props.onClick
        }
    }

    render() {
        let icon

        if (this.state.mode === BusinessRulesConstants.BUSINESS_RULE_TYPE_TEMPLATE) {
            icon = <List/>
        } else {
            icon = <Create/>
        }

        return (
            <Paper style={styles.paper}>
                <Link
                    to={"/business-rules/templateGroupSelector/" + this.state.mode}
                    style={{textDecoration: 'none'}}
                >
                    <Button fab style={styles.button} onClick={this.state.onClick}>
                        {icon}
                    </Button>
                </Link>
                <br/>
                <br/>
                <br/>
                <Typography type="headline" component="h2">
                    {this.props.title}
                </Typography>
                <Typography component="subheading" color="secondary">
                    {this.props.description}
                </Typography>
            </Paper>
        )
    }
}

export default CreateButton;
