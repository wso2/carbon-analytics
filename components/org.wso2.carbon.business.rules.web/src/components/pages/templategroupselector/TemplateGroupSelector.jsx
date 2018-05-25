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
import Grid from 'material-ui/Grid';
// App Components
import TemplateGroupThumbnail from './TemplateGroupThumbnail';
import ProgressDisplay from '../../common/ProgressDisplay';
import ErrorDisplay from '../../common/error/ErrorDisplay';
import Header from '../../common/Header';
// App Utils
import BusinessRulesUtilityFunctions from '../../../utils/BusinessRulesUtilityFunctions';
// App APIs
import BusinessRulesAPI from '../../../api/BusinessRulesAPI';
// App Constants
import BusinessRulesConstants from '../../../constants/BusinessRulesConstants';
// CSS
import '../../../index.css';

/**
 * Styles related to this component
 */
const styles = {
    root: {
        flexGrow: 1,
    },
    spacing: '0',
};

/**
 * App context
 */
const appContext = window.contextPath;

/**
 * Represents the page that allows to select a Template Group
 */
export default class TemplateGroupSelector extends Component {
    constructor(props) {
        super(props);
        this.state = {
            mode: this.props.match.params.mode,
            templateGroups: [],

            // To show Progress or Unauthorized message
            hasLoaded: false,
            errorCode: BusinessRulesConstants.ERROR_CODES.UNKNOWN,
        };
    }

    componentDidMount() {
        new BusinessRulesAPI(BusinessRulesConstants.BASE_URL).getTemplateGroups()
            .then((templateGroupsResponse) => {
                const filteredTemplateGroups = this.filterTemplateGroups(templateGroupsResponse.data[2]);
                this.setState({
                    templateGroups: filteredTemplateGroups,
                    hasLoaded: true,
                    errorCode: BusinessRulesConstants.ERROR_CODES.NONE,
                });
            })
            .catch((error) => {
                this.setState({
                    hasLoaded: true,
                    errorCode: BusinessRulesUtilityFunctions.getErrorDisplayCode(error),
                });
            });
    }

    /**
     * Filters Template Groups by removing unnecessary template groups.
     * Template Groups are removed for filtering, based on the following criteria.
     *      1.  Template Groups that don't have at least one Input Rule Template & one Output Rule Template,
     *          when the mode is 'from scratch'
     *      2.  Template Groups that don't have at least one 'template' type of Rule Template,
     *          when the mode is 'from template'
     * @param {Array} templateGroups        Unfiltered array of available Template Groups
     * @returns {Array}                     Filtered array of Template Groups
     */
    filterTemplateGroups(templateGroups) {
        const filteredTemplateGroups = [];
        for (let i = 0; i < templateGroups.length; i++) {
            let templateRuleTemplatesCount = 0;
            let inputRuleTemplatesCount = 0;
            let outputRuleTemplatesCount = 0;
            for (const ruleTemplate of templateGroups[i].ruleTemplates) {
                if (ruleTemplate.type === BusinessRulesConstants.RULE_TEMPLATE_TYPE_TEMPLATE) {
                    templateRuleTemplatesCount++;
                } else if (ruleTemplate.type === BusinessRulesConstants.RULE_TEMPLATE_TYPE_INPUT) {
                    inputRuleTemplatesCount++;
                } else if (ruleTemplate.type === BusinessRulesConstants.RULE_TEMPLATE_TYPE_OUTPUT) {
                    outputRuleTemplatesCount++;
                }
            }
            if ((this.state.mode === BusinessRulesConstants.BUSINESS_RULE_TYPE_TEMPLATE &&
                templateRuleTemplatesCount > 0) ||
                (this.state.mode === BusinessRulesConstants.BUSINESS_RULE_TYPE_SCRATCH &&
                    inputRuleTemplatesCount > 0 && outputRuleTemplatesCount > 0)) {
                filteredTemplateGroups.push(templateGroups[i]);
            }
        }
        return filteredTemplateGroups;
    }

    /**
     * Returns message, denoting no suitable Template Groups were found
     * @returns {Component}     Component with message
     */
    displayNoTemplateGroupsFound() {
        return (
            <Grid item>
                <Typography>
                    No Suitable Template Groups found
                </Typography>
            </Grid>);
    }

    /**
     * Displays content of the page
     * @returns {Component}     Content of the page
     */
    displayContent() {
        if (this.state.hasLoaded) {
            if (this.state.errorCode === BusinessRulesConstants.ERROR_CODES.NONE) {
                let templateGroups;
                // Business rule to be created from template
                if (this.state.mode === BusinessRulesConstants.BUSINESS_RULE_TYPE_TEMPLATE) {
                    templateGroups = this.state.templateGroups.map(templateGroup =>
                        (<Grid item key={templateGroup.uuid}>
                            <Link
                                to={appContext + '/businessRuleFromTemplateForm/' +
                                BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_CREATE + '/templateGroup/' +
                                templateGroup.uuid + '/businessRule/' + templateGroup.uuid}
                                style={{ textDecoration: 'none' }}
                            >
                                <TemplateGroupThumbnail
                                    key={templateGroup.uuid}
                                    name={templateGroup.name}
                                    uuid={templateGroup.uuid}
                                    description={templateGroup.description ? templateGroup.description : ''}
                                />
                            </Link>
                        </Grid>));
                } else {
                    // Business rule to be created from scratch
                    templateGroups = this.state.templateGroups.map(templateGroup =>
                        (<Grid item key={templateGroup.uuid}>
                            <Link
                                to={appContext + '/businessRuleFromScratchForm/' +
                                BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_CREATE + '/templateGroup/' +
                                templateGroup.uuid + '/businessRule/' + templateGroup.uuid}
                                style={{ textDecoration: 'none' }}
                            >
                                <TemplateGroupThumbnail
                                    key={templateGroup.uuid}
                                    name={templateGroup.name}
                                    uuid={templateGroup.uuid}
                                    description={templateGroup.description ? templateGroup.description : ''}
                                />
                            </Link>
                        </Grid>));
                }

                return (
                    <center>
                        <Typography type="headline">
                            Select a Template Group
                        </Typography>
                        <br />
                        <Grid container style={styles.root}>
                            <Grid item xs={12}>
                                <Grid container justify="center" spacing={Number(styles.spacing)}>
                                    {(this.state.templateGroups.length > 0) ?
                                        (templateGroups) : (this.displayNoTemplateGroupsFound())}
                                </Grid>
                            </Grid>
                        </Grid>
                    </center>
                );
            } else {
                return <ErrorDisplay errorCode={this.state.errorCode} />;
            }
        } else {
            return <ProgressDisplay />;
        }
    }

    render() {
        return (
            <div>
                <Header />
                <br />
                <br />
                <div>
                    {this.displayContent()}
                </div>
            </div>
        );
    }
}

TemplateGroupSelector.propTypes = {
    match: PropTypes.shape({
        params: PropTypes.shape({
            mode: PropTypes.oneOf([
                BusinessRulesConstants.BUSINESS_RULE_TYPE_TEMPLATE,
                BusinessRulesConstants.BUSINESS_RULE_TYPE_SCRATCH,
            ]),
        }),
    }).isRequired,
};
