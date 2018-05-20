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
// Material UI Components
import Typography from 'material-ui/Typography';
import Grid from 'material-ui/Grid';
// App Constants
import BusinessRulesConstants from '../../../constants/BusinessRulesConstants';
// App Components
import Header from '../../common/Header';
import CreateButton from './ModeButton';
// CSS
import '../../../index.css';

/**
 * Styles related to this component
 */
const styles = {
    root: {
        flexGrow: 1,
    },
    spacing: 40,
};

/**
 * Represents the page that allows to select a mode for creating a business rule
 */
export default class ModeSelector extends Component {
    render() {
        return (
            <div>
                <Header />
                <br />
                <br />
                <center>
                    <Typography type="headline">
                        Choose an option
                    </Typography>
                    <br />
                    <br />
                    <Grid container style={styles.root}>
                        <Grid item xs={12}>
                            <Grid container justify="center" spacing={styles.spacing}>
                                <Grid item>
                                    <CreateButton
                                        mode={BusinessRulesConstants.BUSINESS_RULE_TYPE_TEMPLATE}
                                        title="From Template"
                                        description="Create a business rule based on an existing template"
                                    />
                                </Grid>
                                <Grid item>
                                    <CreateButton
                                        mode={BusinessRulesConstants.BUSINESS_RULE_TYPE_SCRATCH}
                                        title="From Scratch"
                                        description={
                                            'Create a business rules with templates for input & output,' +
                                            'and customized filters'}
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
