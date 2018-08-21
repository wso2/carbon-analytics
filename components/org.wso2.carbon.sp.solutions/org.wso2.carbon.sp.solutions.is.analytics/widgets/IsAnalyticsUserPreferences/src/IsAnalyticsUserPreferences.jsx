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
 *
 */

import Widget from "@wso2-dashboards/widget";
import Button from '@material-ui/core/Button';
import TextField from "@material-ui/core/TextField";
import Radio from '@material-ui/core/Radio';
import {MuiThemeProvider, createMuiTheme} from '@material-ui/core/styles';
import _ from 'lodash';

let darkTheme = createMuiTheme({
    palette: {
        type: 'dark',
    }
});

let lightTheme = createMuiTheme({
    palette: {
        type: 'light',
    }
});

let inputFields = [
    {
        name: "serviceProvider",
        label: "Service Provider",
        doDisplay: true,
    },
    {
        name: "userStoreDomain",
        label: "User Store Domain",
        doDisplay: true,
    },
    {
        name: "role",
        label: "Role",
        doDisplay: true,
    },
    {
        name: "identityProvider",
        label: "Identity Provider",
        doDisplay: true,
    },
    {
        name: "username",
        label: "Username",
        doDisplay: true,
    },
];

const messageHeader = "additionalFilterConditions";

let filterConditions = {
    serviceProvider: "",
    userStoreDomain: "",
    role: "",
    identityProvider: "",
    username: "",
};

class IsAnalyticsUserPreferences extends Widget {
    constructor(props) {
        super(props);

        this.state = {
            width: this.props.glContainer.width,
            height: this.props.glContainer.height,

            faultyProviderConf: false,
            options: this.props.configs.options,
            filterValue: "",
            inputFields: inputFields,
            filterConditions: filterConditions,
            byFirstLogins: false,
            isPublished: true
        };

        this.handleTextFieldChange = this.handleTextFieldChange.bind(this);
        this.publishFilterConditions = this.publishFilterConditions.bind(this);
        this.clearFilterConditions = this.clearFilterConditions.bind(this);
        this.handleRadioButtonChange = this.handleRadioButtonChange.bind(this);
        this.onReceivingMessage = this.onReceivingMessage.bind(this);

        this.props.glContainer.on('resize', () =>
            this.setState({
                width: this.props.glContainer.width,
                height: this.props.glContainer.height,
            })
        );
    }

    componentDidMount() {
        super.subscribe(this.onReceivingMessage);
        super.getWidgetConfiguration(this.props.widgetID)
            .then((message) => {
                this.setState({
                    dataProviderConf: message.data.configs.providerConfig
                });
            })
            .catch(() => {
                this.setState({
                    faultyProviderConf: true
                });
            });

        let inputFieldsClone = _.cloneDeep(inputFields);

        switch (this.state.options.widgetType) {
            case ("Local"):
                inputFieldsClone[3].doDisplay = false;
                break;
            case ("Federated"):
                inputFieldsClone[1].doDisplay = false;
                inputFieldsClone[2].doDisplay = false;
                break;
            case ("Overall"):
                inputFieldsClone[1].doDisplay = false;
                inputFieldsClone[2].doDisplay = false;
                inputFieldsClone[3].doDisplay = false;
                break;
        }

        this.setState({
            inputFields: inputFieldsClone,
        })
    }

    handleTextFieldChange(event, fieldName) {
        let filterConditionClone = _.cloneDeep(this.state.filterConditions);

        filterConditionClone[fieldName] = event.target.value;
        this.setState({
            filterConditions: filterConditionClone,
            isPublished: false,
        });
    }

    handleRadioButtonChange(event) {
        let filterConditionClone = _.cloneDeep(this.state.filterConditions);

        if (event.target.value === "byFirstLogins" && !this.state.byFirstLogins) {
            filterConditionClone.isFirstLogin = true;
            this.setState({
                filterConditions: filterConditionClone,
                byFirstLogins: true,
                isPublished: false,
            })
        } else if (event.target.value === "byAll" && this.state.byFirstLogins) {
            filterConditionClone.isFirstLogin = undefined;
            this.setState({
                filterConditions: filterConditionClone,
                byFirstLogins: false,
                isPublished: false,
            })
        }
    }

    publishFilterConditions() {
        if (!this.state.isPublished) {
            let message = {
                header: messageHeader,
                body: this.state.filterConditions,
            };
            super.publish(message);

            this.setState({
                isPublished: true
            })
        }
    }

    clearFilterConditions() {
        let message = {
            header: messageHeader,
            body: "",
        };

        this.setState({
            filterConditions: filterConditions,
            byFirstLogins: false,
        }, super.publish(message));
    }

    onReceivingMessage(message) {
        if (message.header === "barChartFilter") {
            let filterConditionsClone = _.cloneDeep(this.state.filterConditions);
            filterConditionsClone[message.title] = message.value;
            this.setState({
                filterConditions: filterConditionsClone,
                isPublished: false,
            }, () => this.publishFilterConditions())
        }
    }

    render() {
        let theme = darkTheme;

        if (this.props.muiTheme.appBar.color === "#eeeeee") {
            theme = lightTheme;
        }

        return (
            <MuiThemeProvider theme={theme}>
                <div style={{height: this.state.height, width: this.state.width}}>
                    <div style={{padding: 24}}>
                        <h3>User Preferences</h3>
                    </div>
                    <div>
                        <tr>
                            {this.state.inputFields.map(function (field, i) {
                                if (field.doDisplay) {
                                    return (
                                        <td style={{padding: 10}}>
                                            <TextField
                                                value={this.state.filterConditions[field.name]}
                                                id={field.name}
                                                label={field.label}
                                                onChange={(event) => this.handleTextFieldChange(event, field.name)}
                                            />
                                        </td>
                                    );
                                }
                            }, this)}
                        </tr>
                        <tr>
                            {
                                this.state.options.widgetType === 'Overall' &&
                                <div>
                                    <td>
                                        <Radio
                                            color="primary"
                                            value="byAll"
                                            checked={!(this.state.byFirstLogins)}
                                            onChange={(event) => this.handleRadioButtonChange(event)}
                                        />
                                    </td>
                                    <td>
                                        By All
                                    </td>
                                    <td>
                                        <Radio
                                            color="secondary"
                                            value="byFirstLogins"
                                            checked={this.state.byFirstLogins}
                                            onChange={(event) => this.handleRadioButtonChange(event)}
                                        />
                                    </td>
                                    <td>
                                        By First Logins
                                    </td>
                                </div>
                            }
                        </tr>
                    </div>
                    <table>
                        <tr>
                            <td style={{padding: 15}}>
                                <Button
                                    color="primary"
                                    variant="contained"
                                    component="span"
                                    onClick={() => this.publishFilterConditions()}
                                >
                                    Filter
                                </Button>
                            </td>
                            <td style={{padding: 15}}>
                                <Button
                                    color="secondary"
                                    variant="contained"
                                    component="span"
                                    onClick={() => this.clearFilterConditions()}
                                >
                                    Clear All
                                </Button>
                            </td>
                        </tr>
                    </table>
                </div>
            </MuiThemeProvider>
        )
    }
}

global.dashboard.registerWidget('IsAnalyticsUserPreferences', IsAnalyticsUserPreferences);
