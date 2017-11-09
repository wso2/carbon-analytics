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
import IconButton from 'material-ui/IconButton';
import RefreshIcon from 'material-ui-icons/Refresh';
import EditIcon from 'material-ui-icons/Edit';
import DeleteIcon from 'material-ui-icons/Delete';
import {TableCell, TableRow,} from 'material-ui/Table';
import Tooltip from 'material-ui/Tooltip';
import VisibilityIcon from 'material-ui-icons/Visibility';
// App Utilities
import BusinessRulesConstants from "../utils/BusinessRulesConstants";
import BusinessRulesUtilityFunctions from "../utils/BusinessRulesUtilityFunctions";
import BusinessRulesAPICaller from "../utils/BusinessRulesAPICaller";
// CSS
import '../index.css';
import BusinessRulesManager from "./BusinessRulesManager";
import BusinessRuleCreator from "./BusinessRuleCreator";
import BusinessRuleFromScratchForm from "./BusinessRuleFromScratchForm";
import BusinessRuleFromTemplateForm from "./BusinessRuleFromTemplateForm";
import ProgressDisplay from "./ProgressDisplay";
import TemplateGroupSelector from "./TemplateGroupSelector";

/**
 * Represents each Business Rule, that is shown as a row, to view, edit, delete / re-deploy Business Rules
 */
class Test2 extends React.Component {

    render(){
        return(
            <h1>Test</h1>
        );
    }
}

export default Test2;
