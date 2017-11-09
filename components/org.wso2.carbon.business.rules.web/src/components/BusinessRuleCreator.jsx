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
import {BrowserRouter, Route, Link, Switch} from 'react-router-dom';
// Material UI Components
import Typography from 'material-ui/Typography';
import CreateButton from "./CreateButton";
import BusinessRulesUtilityFunctions from "../utils/BusinessRulesUtilityFunctions";
import Grid from 'material-ui/Grid';
// App Components
import Header from "./Header";
// App Utilities
import BusinessRulesConstants from "../utils/BusinessRulesConstants";
// CSS
import '../index.css';


/**
 * Allows to create a Business Rule either from scratch or from a Template
 */

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
                                    <Link to={"/business-rules/templateGroupSelector/" + BusinessRulesConstants.BUSINESS_RULE_TYPE_TEMPLATE} style={{ textDecoration: 'none' }}>
                                        <CreateButton
                                            mode={BusinessRulesConstants.BUSINESS_RULE_TYPE_TEMPLATE}
                                            title='From Template'
                                            description='Create a business rule based on an existing template'
                                        />
                                    </Link>
                                </Grid>
                                <Grid item>
                                    <Link to={"/business-rules/templateGroupSelector/" + BusinessRulesConstants.BUSINESS_RULE_TYPE_SCRATCH} style={{ textDecoration: 'none' }}>
                                        <CreateButton
                                            mode={BusinessRulesConstants.BUSINESS_RULE_TYPE_SCRATCH}
                                            title='From Scratch'
                                            description='Create a business rules with templates for input & output,
                                        and customized filters'
                                        />
                                    </Link>
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
