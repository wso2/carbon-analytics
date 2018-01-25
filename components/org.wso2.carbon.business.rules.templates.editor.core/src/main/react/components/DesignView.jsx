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

import React from 'react';
import PropTypes from 'prop-types';
// Material UI Components
import Paper from 'material-ui/Paper';
// App Components
import TemplateGroup from './TemplateGroup';

/**
 * Styles related to this component
 */
const styles = {
    formPaper: {
        margin: 50,
    }
};

/**
 * Represents the design view
 */
class DesignView extends React.Component {
    render() {
        return (
            <div>
                <div style={styles.formPaper}>
                    <div>
                        <Paper>
                            <div style={styles.formPaper}>
                                <TemplateGroup
                                    configuration={this.props.templateGroup}
                                    scriptAdditionsBins={this.props.scriptAdditionsBins}
                                    editorSettings={this.props.editorSettings}
                                    handleTemplateGroupValueChange={this.props.handleTemplateGroupValueChange}
                                    handleRuleTemplateChange={this.props.handleRuleTemplateChange}
                                    addScript={this.props.addScript}
                                    addRuleTemplate={this.props.addRuleTemplate}
                                    removeRuleTemplate={this.props.removeRuleTemplate}
                                />
                            </div>
                        </Paper>
                    </div>
                </div>
            </div>
        );
    }
}

DesignView.propTypes = {
    templateGroup: PropTypes.object.isRequired,
    scriptAdditionsBins: PropTypes.array.isRequired,
    editorSettings: PropTypes.object.isRequired,
    handleTemplateGroupValueChange: PropTypes.func.isRequired,
    handleRuleTemplateChange: PropTypes.func.isRequired,
    addScript: PropTypes.func.isRequired,
    addRuleTemplate: PropTypes.func.isRequired,
    removeRuleTemplate: PropTypes.func.isRequired
};

export default DesignView;
