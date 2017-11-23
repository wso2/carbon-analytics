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
// Material UI Components
import Typography from 'material-ui/Typography';
import CreateButton from "./CreateButton";
import Grid from 'material-ui/Grid';
// App Constants
import BusinessRulesConstants from "../constants/BusinessRulesConstants";
// CSS
import '../index.css';

/**
 * App context.
 */
const appContext = window.contextPath;

// Styles related to this component
const styles = {
    root: {
        flexGrow: 1,
    },
    control: {
        padding: 5,
    },
    paper: {
        maxWidth: 400,
        paddingTop: 30,
        paddingBottom: 30
    },
    spacing: '40'
}

/**
 * Allows to create a Business Rule either from scratch or from a Template
 */
class BusinessRuleCreator extends React.Component {
    render() {
        return (
            <div>
                <br/>
                <center>
                    <Typography type="headline">
                        Choose an option
                    </Typography>
                    <br/>
                    <br/>

                    <Grid container style={styles.root}>
                        <Grid item xs={12}>
                            <Grid container justify="center" spacing={Number(styles.spacing)}>
                                <Grid item>
                                    <CreateButton
                                        mode={BusinessRulesConstants.BUSINESS_RULE_TYPE_TEMPLATE}
                                        title='From Template'
                                        description='Create a business rule based on an existing template'
                                    />
                                </Grid>
                                <Grid item>
                                    <CreateButton
                                        mode={BusinessRulesConstants.BUSINESS_RULE_TYPE_SCRATCH}
                                        title='From Scratch'
                                        description='Create a business rules with templates for input & output,
                                        and customized filters'
                                    />
                                </Grid>
                            </Grid>
                        </Grid>
                    </Grid>

                </center>
            </div>
        );
    }
}

export default BusinessRuleCreator;
