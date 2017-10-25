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
import TemplateGroup from './TemplateGroup';
import Grid from 'material-ui/Grid';
// App Components
import Header from "./Header";
// App Utilities
import BusinessRulesUtilityFunctions from "../utils/BusinessRulesUtilityFunctions";
import BusinessRulesConstants from "../utils/BusinessRulesConstants";
// CSS
import '../index.css';

/**
 * Allows to select a Template Group, among Template Groups displayed as thumbnails
 */

// Styles related to this component
const styles = {
    containerDiv: {
        maxWidth: 750
    },
    root: {
        flexGrow: 1,
    },
    control: {
        padding: 5,
    },
    spacing: '0'
}

class TemplateGroupSelector extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            mode: props.mode, // 'template' or 'scratch'
            templateGroups: props.templateGroups // Available Template Groups
        }
    }

    render() {
        let templateGroups

        // Business rule to be created from template
        if (this.state.mode === BusinessRulesConstants.BUSINESS_RULE_TYPE_TEMPLATE) {
            templateGroups = this.state.templateGroups.map((templateGroup) =>
                <Grid item key={templateGroup.uuid}>
                    <TemplateGroup
                        key={templateGroup.uuid}
                        name={templateGroup.name}
                        uuid={templateGroup.uuid}
                        description={templateGroup.description}
                        onClick={(e) =>
                            BusinessRulesUtilityFunctions.loadBusinessRulesFromTemplateCreator(templateGroup.uuid)
                        }
                    />
                </Grid>
            )
        } else {
            // Business rule to be created from scratch
            templateGroups = this.state.templateGroups.map((templateGroup) =>
                <Grid item key={templateGroup.uuid}>
                    <TemplateGroup
                        key={templateGroup.uuid}
                        name={templateGroup.name}
                        uuid={templateGroup.uuid}
                        description={templateGroup.description}
                        onClick={(e) =>
                            BusinessRulesUtilityFunctions.loadBusinessRuleFromScratchCreator(templateGroup.uuid)
                        }
                    />
                </Grid>
            )
        }


        return (
            <div>
                <Header/>
                <br/>
                <br/>
                <center>
                    <Typography type="headline">
                        Select a Template Group
                    </Typography>
                    <br/>

                    <Grid container style={styles.root}>
                        <Grid item xs={12}>
                            <Grid container justify="center" spacing={Number(styles.spacing)}>
                                {templateGroups}
                            </Grid>
                        </Grid>
                    </Grid>

                </center>
            </div>
        )
    }
}

export default TemplateGroupSelector;
