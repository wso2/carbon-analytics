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
// App Components
import Header from "./Header";
import BusinessRuleFromScratchForm from "./BusinessRuleFromScratchForm";
// App Utilities
import BusinessRulesConstants from "../utils/BusinessRulesConstants";
// CSS
import '../index.css';

/**
 * Allows to select a Rule Template, of a given type ('template', 'input' or 'output')
 * which will then display the form with Rule Template's properties
 */
class BusinessRuleFromScratchCreator extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            templateGroup: props.templateGroup,
            inputRuleTemplates: props.inputRuleTemplates,
            outputRuleTemplates: props.outputRuleTemplates
        }
    }

    /**
     * Renders a drop down which displays available Rule Templates of filtered types,
     * And displays a form on selecting a Rule Template
     */
    render() {
        // To display the business Rule form
        let businessRuleForm =
            <BusinessRuleFromScratchForm
                formMode={BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_CREATE}
                selectedTemplateGroup={this.state.templateGroup}
                inputRuleTemplates={this.state.inputRuleTemplates}
                outputRuleTemplates={this.state.outputRuleTemplates}
            />


        return (
            <div>
                <Header/>
                <br/>
                <Typography type="headline">Create a business rule from scratch</Typography>
                <Typography type="subheading">
                    Select a Template Group for choosing rule templates for input and output from
                </Typography>
                <br/>
                {businessRuleForm}
            </div>
        )
    }
}

export default BusinessRuleFromScratchCreator;
