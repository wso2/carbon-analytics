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
// Ace Editor Components
import AceEditor from 'react-ace';
import 'brace/mode/text';
// Material UI Components
import Typography from 'material-ui/Typography';
import Card, {CardActions, CardContent} from 'material-ui/Card';
import {IconButton} from "material-ui";
import ClearIcon from 'material-ui-icons/Clear';
import ExpandMoreIcon from 'material-ui-icons/ExpandMore';
import Collapse from 'material-ui/transitions/Collapse';

const styles = {
    formPaper: {
        padding: 30,
    },
    formPaperContainer: {
        padding: 10,
    },
    header: {
        padding: 5,
        paddingLeft: 30,
    },
    cardContent: {
        padding: 30,
        paddingTop: 10,
    },
    flexGrow: {
        flex: '1 1 auto',
    },
    errorText: {
        color: '#C62828',
    }
}

/**
 * Represents a SiddhiApp template
 */
class Template extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            type: 'siddhiApp',
            content: props.content,
            isExpanded: false,
        };
        this.toggleExpansion = this.toggleExpansion.bind(this);
    }

    /**
     * Toggle expansion of this component
     */
    toggleExpansion() {
        const state = this.state;
        state.isExpanded = !state.isExpanded;
        this.setState(state);
    }

    render() {
        return (
            <div style={styles.formPaperContainer}>
                <Card>
                    <CardActions disableActionSpacing style={styles.header}>
                        <Typography type='subheading'>SiddhiApp</Typography>
                        <div style={styles.flexGrow} />
                        {(!this.props.invalid) ? (null) :
                            (<Typography type="body2" style={styles.errorText}>
                                An {this.props.invalid} rule template can not have many templates
                            </Typography>)}
                        <IconButton
                            onClick={this.toggleExpansion}
                        >
                            <ExpandMoreIcon/>
                        </IconButton>
                        <IconButton
                            color='primary'
                            disabled={this.props.notRemovable}
                            aria-label='Remove'
                            onClick={this.props.removeTemplate}
                        >
                            <ClearIcon/>
                        </IconButton>
                    </CardActions>
                    <Collapse in={this.state.isExpanded} transitionDuration='auto' unmountOnExit>
                        <CardContent style={{padding: 0, paddingBottom: 0}}>
                            <AceEditor
                                mode='text'
                                theme={this.props.editorSettings.theme}
                                fontSize={this.props.editorSettings.fontSize}
                                wrapEnabled={this.props.editorSettings.wrapEnabled}
                                value={this.props.content}
                                onChange={value => this.props.handleTemplateValueChange(value)}
                                name='siddhiAppTemplate'
                                ref="siddhiAppTemplate"
                                editorProps={{$blockScrolling: true}}
                                showPrintMargin={false}
                                style={{width: '100%'}}
                            />
                        </CardContent>
                    </Collapse>
                </Card>
            </div>
        );
    }
}

export default Template;
