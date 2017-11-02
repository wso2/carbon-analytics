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
import AppBar from 'material-ui/AppBar';
import Toolbar from 'material-ui/Toolbar';

import Logo from '../images/wso2-logo.svg';

// App Utilities
import BusinessRulesUtilityFunctions from '../utils/BusinessRulesUtilityFunctions';
// CSS
import '../index.css';

/**
 * Represents a Create Button used in the Business Rule Manager
 */

// Styles related to this component
const styles = {
    headerStyle: {
        color: 'white',
        backgroundColor: '#212121',
    }
}

class Header extends React.Component {
    render() {
        return (
            <AppBar position="static" style={styles.headerStyle}>
                <Toolbar>
                    <img height='35'
                        src={Logo}
                        style={{cursor:'pointer'}}
                        onClick={(e) => BusinessRulesUtilityFunctions.loadBusinessRulesManager()}/>
                    &nbsp;
                    &nbsp;
                    &nbsp;
                    <Typography type="subheading" color="inherit">
                        Business Rules Manager
                    </Typography>
                </Toolbar>
            </AppBar>
        );
    }
}

export default Header;
