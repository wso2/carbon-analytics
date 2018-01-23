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
import { Link } from 'react-router-dom';
// Material UI Components
import Typography from 'material-ui/Typography';
import Grid from 'material-ui/Grid';
// App Components
import TemplateGroup from './TemplateGroup';
import Header from './common/Header';
// App Utilities
// App Constants
import BusinessRulesConstants from '../constants/BusinessRulesConstants';
// CSS
import '../index.css';
import BusinessRulesAPICaller from '../api/BusinessRulesAPICaller';

/**
 * Styles related to this component
 */
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
};

/**
 * App context.
 */
const appContext = window.contextPath;

/**
 * Allows to select a Template Group, among Template Groups displayed as thumbnails
 */
class TemplateGroupSelector extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            mode: this.props.match.params.mode,
            templateGroups: []
        };
    }

    componentDidMount() {
        new BusinessRulesAPICaller(BusinessRulesConstants.BASE_URL).getTemplateGroups()
            .then((templateGroupsResponse) => {
                let filteredTemplateGroups = this.removeInvalidTemplateGroups(templateGroupsResponse.data[2]);
                this.setState({
                    templateGroups: filteredTemplateGroups
                });
            });
    }

    /**
     * Removes template groups that don't have at least one input rule template & one output rule template from state,
     * When opened in 'from scratch' mode
     *
     * @param templateGroups
     * @returns {Array}
     */
    removeInvalidTemplateGroups(templateGroups) {
        const filteredTemplateGroups = [];
        for (let i = 0; i < templateGroups.length; i++) {
            let templateRuleTemplatesCount = 0;
            let inputRuleTemplatesCount = 0;
            let outputRuleTemplatesCount = 0;
            for (let ruleTemplate of templateGroups[i].ruleTemplates) {
                if (ruleTemplate.type === BusinessRulesConstants.RULE_TEMPLATE_TYPE_TEMPLATE) {
                    templateRuleTemplatesCount++;
                } else if (ruleTemplate.type === BusinessRulesConstants.RULE_TEMPLATE_TYPE_INPUT) {
                    inputRuleTemplatesCount++;
                } else {
                    outputRuleTemplatesCount++;
                }
            }
            if (this.state.mode === BusinessRulesConstants.BUSINESS_RULE_TYPE_TEMPLATE) {
                if (templateRuleTemplatesCount > 0) {
                    filteredTemplateGroups.push(templateGroups[i]);
                }
            } else {
                if (inputRuleTemplatesCount > 0 && outputRuleTemplatesCount > 0) {
                    filteredTemplateGroups.push(templateGroups[i]);
                }
            }
        }
        return filteredTemplateGroups;
    }

    render() {
        let templateGroups;
        // Business rule to be created from template
        if (this.state.mode === BusinessRulesConstants.BUSINESS_RULE_TYPE_TEMPLATE) {
            templateGroups = this.state.templateGroups.map((templateGroup) =>
                <Grid item key={templateGroup.uuid}>
                    <Link
                        to={appContext + "/businessRuleFromTemplateForm/" +
                        BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_CREATE + "/templateGroup/" + templateGroup.uuid
                        + "/businessRule/" + templateGroup.uuid}
                        style={{textDecoration: 'none'}}>
                        <TemplateGroup
                            key={templateGroup.uuid}
                            name={templateGroup.name}
                            uuid={templateGroup.uuid}
                            description={templateGroup.description ? templateGroup.description : ''}
                        />
                    </Link>
                </Grid>
            );
        } else {
            // Business rule to be created from scratch
            templateGroups = this.state.templateGroups.map((templateGroup) =>
                <Grid item key={templateGroup.uuid}>
                    <Link
                        to={appContext + "/businessRuleFromScratchForm/" +
                        BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_CREATE + "/templateGroup/" +
                        templateGroup.uuid + "/businessRule/" + templateGroup.uuid}
                        style={{textDecoration: 'none'}}>
                        <TemplateGroup
                            key={templateGroup.uuid}
                            name={templateGroup.name}
                            uuid={templateGroup.uuid}
                            description={templateGroup.description ? templateGroup.description : ''}
                        />
                    </Link>
                </Grid>
            );
        }

        return (
            <div>
                <Header />
                <br />
                <br/>
                <center>
                    <Typography type="headline">
                        Select a Template Group
                    </Typography>
                    <br/>
                    <Grid container style={styles.root}>
                        <Grid item xs={12}>
                            <Grid container justify="center" spacing={Number(styles.spacing)}>
                                {(this.state.templateGroups.length > 0) ?
                                    ({templateGroups}) :
                                    (<Grid item>
                                        <Typography>
                                            No Template Groups found
                                        </Typography>
                                    </Grid>)}
                            </Grid>
                        </Grid>
                    </Grid>
                </center>
            </div>
        );
    }
}

export default TemplateGroupSelector;
