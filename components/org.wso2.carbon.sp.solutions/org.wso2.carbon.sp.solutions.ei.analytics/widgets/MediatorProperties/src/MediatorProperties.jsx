/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import React from 'react';
import Widget from '@wso2-dashboards/widget';
import CodeMirror from 'codemirror/lib/codemirror';
/*
MergeView requires google's diff_match_patch library as a dependency. Use the one provided in utils by
replacing node_modules/codemirror/addon/merge/merge.js with ../utils/codemirror/addon/merge/merge.js
 */
import MergeView from 'codemirror/addon/merge/merge';
import './ambiance.css';
import './codemirror.css';
import './merge.css';
import './nanoscroller.css';

const META_TENANT_ID = '-1234';
const centerDiv = {
    textAlign: 'center',
    verticalAlign: 'middle',
};
const table = {
    tableLayout: 'fixed',
    fontSize: '14px',
    width: '100%',
    maxWidth: '100%',
    marginBottom: '20px',
    backgroundColor: 'transparent',
    borderSpacing: '0',
    borderCollapse: 'collapse',
    minHeight: '.01%',
    overflowX: 'auto',
};

class MediatorProperties extends Widget {
    constructor(props) {
        super(props);
        this.state = {
            isNoData: true,
            messageComparisonData: null,
            widgetHeight: this.props.glContainer.height,
            widgetWidth: this.props.glContainer.width,
        };
        this.isChildDataPresent = false;
        this.domElementPayloadView = null;
        this.domElementTransportPropView = null;
        this.domElementContextPropView = null;
    }

    componentWillMount() {
        /* Handle attributes published from the clicked mediator. */
        super.subscribe((mediatorAttributes) => {
            if ('componentId' in mediatorAttributes) {
                /* If a mediator clicked, data is allowed stored again until a component data with
                   children data is stored. */
                this.isChildDataPresent = false;
                this.setState({
                    isNoData: true,
                }, () => {
                    const componentId = mediatorAttributes.componentId;
                    const queryParameters = getQueryString();
                    const messageFlowId = queryParameters.id;

                    // Get message flow details of the mediator
                    super.getWidgetConfiguration(this.props.widgetID)
                        .then((message) => {
                            // Get data provider sub json string from the widget configuration
                            const dataProviderConf = message.data.configs.providerConfig;
                            let query = dataProviderConf.configs.config.queryData
                                .GET_MESSAGE_FLOW_DATA_QUERY;
                            // Insert required parameters to the query string
                            const formattedQuery = query
                                .replace('{{messageFlowId}}', messageFlowId)
                                .replace('{{componentId}}', componentId)
                                .replace('{{meta_tenantId}}', META_TENANT_ID);
                            dataProviderConf.configs.config.queryData = {query: formattedQuery};
                            // Request data store with the modified query
                            super.getWidgetChannelManager()
                                .subscribeWidget(
                                    this.props.id,
                                    this.handleComponentMessageFlowData(messageFlowId).bind(this),
                                    dataProviderConf,
                                );
                        })
                        .catch(() => {
                            console.error("Unable to load widget configurations");
                        });
                });
            }
        });
    }

    handleComponentMessageFlowData(messageFlowId) {
        return (messageFlowData) => {
            const messageInfoBefore = parseDatastoreMessage(messageFlowData)[0];
            if (messageInfoBefore.children != null && messageInfoBefore.children !== 'null') {
                const childIndex = JSON.parse(messageInfoBefore.children)[0];
                // Get message flow details of the child component
                super.getWidgetConfiguration(this.props.widgetID)
                    .then((message) => {
                        // Get data provider sub json string from the widget configuration
                        const dataProviderConf = message.data.configs.providerConfig;
                        let query = dataProviderConf.configs.config.queryData
                            .GET_CHILD_MESSAGE_FLOW_DATA_QUERY;
                        // Insert required parameters to the query string
                        const formattedQuery = query
                            .replace('{{messageFlowId}}', messageFlowId)
                            .replace('{{componentIndex}}', childIndex)
                            .replace('{{meta_tenantId}}', META_TENANT_ID);
                        dataProviderConf.configs.config.queryData = {query: formattedQuery};
                        // Request data store with the modified query
                        super.getWidgetChannelManager()
                            .subscribeWidget(
                                this.props.id,
                                this.handleChildMessageFlowData(messageInfoBefore).bind(this),
                                dataProviderConf,
                            );
                    })
                    .catch((error) => {
                        console.error("Unable to load widget configurations");
                    });
                /*
                If DB returned nothing, still continue the process
                 */
                this.handleChildMessageFlowData(messageInfoBefore)('');
            } else {
                /* If child details are not available, continue  with the normal flow */
                this.handleChildMessageFlowData(messageInfoBefore)('');
            }
        };
    }

    handleChildMessageFlowData(messageInfoBefore) {
        return (childMessageDetails) => {
            let messageInfoAfter = {};
            if (childMessageDetails !== '') {
                messageInfoAfter = parseDatastoreMessage(childMessageDetails)[0];
            }
            const result = {};
            result.payload = {
                before: messageInfoBefore.beforePayload,
                after: messageInfoBefore.afterPayload
            };

            const transportProperties = [];
            const contextProperties = [];
            let transportPropertyMapBefore;
            let contextPropertyMapBefore;

            if (messageInfoBefore.transportPropertyMap != null) {
                transportPropertyMapBefore = processProperties(messageInfoBefore.transportPropertyMap);
            } else {
                transportPropertyMapBefore = {};
            }
            if (messageInfoBefore.contextPropertyMap != null) {
                contextPropertyMapBefore = processProperties(messageInfoBefore.contextPropertyMap);
            } else {
                contextPropertyMapBefore = {};
            }

            const allTransportProperties = Object.keys(transportPropertyMapBefore);
            const allContextProperties = Object.keys(contextPropertyMapBefore);
            let transportPorpertyMapAfter;
            let contextPorpertyMapAfter;

            if (messageInfoAfter != null) {
                if (messageInfoAfter.transportPropertyMap != null) {
                    transportPorpertyMapAfter = processProperties(messageInfoAfter.transportPropertyMap);
                } else {
                    transportPorpertyMapAfter = {};
                }
                if (messageInfoAfter.contextPropertyMap != null) {
                    contextPorpertyMapAfter = processProperties(messageInfoAfter.contextPropertyMap);
                } else {
                    contextPorpertyMapAfter = {};
                }

                for (const property in transportPorpertyMapAfter) {
                    if (allTransportProperties.indexOf(property) < 0) {
                        allTransportProperties.push(property);
                    }
                }
                for (const property in contextPorpertyMapAfter) {
                    if (allContextProperties.indexOf(property) < 0) {
                        allContextProperties.push(property);
                    }
                }
            }
            // Add Transport Properties
            for (const property in allTransportProperties) {
                const propertyName = allTransportProperties[property];
                let beforeValue;
                let afterValue;
                if (transportPropertyMapBefore.hasOwnProperty(propertyName)) {
                    beforeValue = transportPropertyMapBefore[propertyName];
                } else {
                    beforeValue = 'N/A';
                }
                if (messageInfoAfter != null) {
                    if (transportPorpertyMapAfter.hasOwnProperty(propertyName)) {
                        afterValue = transportPorpertyMapAfter[propertyName];
                    } else {
                        afterValue = 'N/A';
                    }
                } else {
                    afterValue = beforeValue;
                }
                transportProperties.push({"name": propertyName, before: beforeValue, 'after': afterValue});
            }
            result.transportProperties = transportProperties;

            // Add Context Properties
            for (const property in allContextProperties) {
                const propertyName = allContextProperties[property];
                let beforeValue;
                let afterValue;
                if (contextPropertyMapBefore.hasOwnProperty(propertyName)) {
                    beforeValue = contextPropertyMapBefore[propertyName];
                } else {
                    beforeValue = 'N/A';
                }
                if (messageInfoAfter != null) {
                    if (contextPorpertyMapAfter.hasOwnProperty(propertyName)) {
                        afterValue = contextPorpertyMapAfter[propertyName];
                    } else {
                        afterValue = 'N/A';
                    }
                } else {
                    afterValue = beforeValue;
                }
                contextProperties.push({"name": propertyName, 'before': beforeValue, after: afterValue});
            }
            result.contextProperties = contextProperties;

            // If child data is not present in recent state update, update state
            if (!this.isChildDataPresent) {
                this.isChildDataPresent = childMessageDetails !== '';
                this.setState({
                    isNoData: false,
                    messageComparisonData: result,
                }, this.generateMergedView.bind(this));
            }
        };
    }

    generateMergedView() {
        const data = this.state.messageComparisonData;
        drawMergeView(this.domElementPayloadView, data.payload.before.trim(), data.payload.after.trim());

        if (data.transportProperties) {
            let transportPropertiesBefore = '';
            let transportPropertiesAfter = '';
            data.transportProperties.forEach((property) => {
                if (typeof(property.before) === "string") {
                    property.before = "'" + property.before + "'";
                }
                if (typeof(property.after) === "string") {
                    property.after = "'" + property.after + "'";
                }

                transportPropertiesBefore += property.name + " : " + property.before + "\n";
                transportPropertiesAfter += property.name + " : " + property.after + "\n";
            });
            drawMergeView(this.domElementTransportPropView, transportPropertiesBefore.trim(), transportPropertiesAfter.trim());
        }

        if (data.contextProperties) {
            let contextPropertiesBefore = '';
            let contextPropertiesAfter = '';
            data.contextProperties.forEach((property) => {
                if (typeof(property.before) === "string") {
                    property.before = "'" + property.before + "'";
                }
                if (typeof(property.after) === "string") {
                    property.after = "'" + property.after + "'";
                }
                contextPropertiesBefore += property.name + " : " + property.before + "\n";
                contextPropertiesAfter += property.name + " : " + property.after + "\n";
            });
            drawMergeView(this.domElementContextPropView, contextPropertiesBefore.trim(), contextPropertiesAfter.trim());
        }
    }

    render() {
        return (
            <body className="nano">
            <div id="gadget-message"/>
            <div className="nano-content">
                <table className="table table-condensed table-responsive" style={table}>
                    <thead>
                    <tr>
                        <th>
                            <h4 style={centerDiv}>
                                <center>Before</center>
                            </h4>
                        </th>
                        <th>
                            <h4 style={centerDiv}>
                                <center>After</center>
                            </h4>
                        </th>
                    </tr>
                    </thead>
                </table>
                <h4>
                    <center>Payload</center>
                </h4>
                <div ref={input => (this.domElementPayloadView = input)}/>
                <h4>
                    <center>Transport Properties</center>
                </h4>
                <div ref={input => (this.domElementTransportPropView = input)}/>
                <h4>
                    <center>Context Properties</center>
                </h4>
                <div ref={input => (this.domElementContextPropView = input)}/>

            </div>
            </body>
        );
    }
}

/**
 * Combine meta data and data separated data store message in to an object with names as attributes
 * @param recievedData
 * @returns {Array}
 */
function parseDatastoreMessage(recievedData) {
    const parsedArray = [];
    const dataMapper = {};

    const dataArray = recievedData.data;
    const metaData = recievedData.metadata.names;

    metaData.forEach((value, index) => {
        dataMapper[index] = value;
    });
    dataArray.forEach((dataPoint) => {
        const parsedObject = {};
        dataPoint.forEach((value, index) => {
            parsedObject[dataMapper[index]] = value;
        });
        parsedArray.push(parsedObject);
    });

    return parsedArray;
}

/**
 * Get query parameters parsed in to an object
 */
function getQueryString() {
    let queryStringKeyValue = window.parent.location.search.replace('?', '').split('&');
    let qsJsonObject = {};
    if (queryStringKeyValue !== '') {
        for (let i = 0; i < queryStringKeyValue.length; i++) {
            qsJsonObject[queryStringKeyValue[i].split('=')[0]] = queryStringKeyValue[i].split('=')[1];
        }
    }
    return qsJsonObject;
}

/**
 * Split and process elements in any map of properties, if given in "{name1=value1, name2=value2 }" format.
 * in the publisher-end.
 */
function processProperties(propertyMap) {
    let output = {};
    if (propertyMap) {
        let properties = propertyMap.slice(1, -1).split(',');
        for (let i = 0; i < properties.length; i++) {
            let property = properties[i];
            if ((i + 1 < properties.length) && (properties[i + 1].indexOf('=') === -1)) {
                property = property + ',' + properties[i + 1];
            }
            if (property.indexOf('=') === -1) {
                continue;
            } else {
                let nameValuePair = property.split('=');
                output[nameValuePair[0]] = nameValuePair[1];
                for (let j = 2; j < nameValuePair.length; j++) {
                    if (nameValuePair[j]) {
                        output[nameValuePair[0]] += '=' + nameValuePair[j];
                    }
                }
            }
        }
        return output;
    } else {
        return {};
    }
}

function drawMergeView(placeholder, before, after) {
    const view = placeholder;
    if (isJSON(before)) {
        before = JSON.stringify(JSON.parse(before), null, '\t');
    } else {
        before = formatXML(before);
    }
    if (isJSON(after)) {
        after = JSON.stringify(JSON.parse(after), null, '\t');
    } else {
        after = formatXML(after);
    }
    view.innerHTML = '';
    const dv = CodeMirror.MergeView(view, {
        value: after,
        origLeft: before,
        lineNumbers: true,
        theme: 'ambiance',
        highlightDifferences: true,
        connect: 'connect'
    });
}

function isJSON(str) {
    try {
        JSON.parse(str);
    } catch (e) {
        return false;
    }
    return true;
}

function formatXML(xml) {
    let reg = /(>)(<)(\/*)/g;
    let wsexp = / *(.*) +\n/g;
    let contexp = /(<.+>)(.+\n)/g;
    xml = xml.replace(reg, '$1\n$2$3').replace(wsexp, '$1\n').replace(contexp, '$1\n$2');
    let pad = 0;
    let formatted = '';
    let lines = xml.split('\n');
    let indent = 0;
    let lastType = 'other';
    let transitions = {
        'single->single': 0,
        'single->closing': -1,
        'single->opening': 0,
        'single->other': 0,
        'closing->single': 0,
        'closing->closing': -1,
        'closing->opening': 0,
        'closing->other': 0,
        'opening->single': 1,
        'opening->closing': 0,
        'opening->opening': 1,
        'opening->other': 1,
        'other->single': 0,
        'other->closing': -1,
        'other->opening': 0,
        'other->other': 0,
    };

    for (let i = 0; i < lines.length; i++) {
        let ln = lines[i];
        let single = Boolean(ln.match(/<.+\/>/));
        let closing = Boolean(ln.match(/<\/.+>/));
        let opening = Boolean(ln.match(/<[^!].*>/));
        let type = single ? 'single' : closing ? 'closing' : opening ? 'opening' : 'other';
        let fromTo = lastType + '->' + type;
        lastType = type;
        let padding = '';

        indent += transitions[fromTo];
        for (let j = 0; j < indent; j++) {
            padding += '    ';
        }

        formatted += padding + ln + '\n';
    }

    return formatted;
}

global.dashboard.registerWidget('MediatorProperties', MediatorProperties);
