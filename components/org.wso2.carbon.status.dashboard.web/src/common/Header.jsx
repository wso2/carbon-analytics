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
 *
 */

import React, { Component } from 'react';
import { Link } from 'react-router-dom';
// Material UI
import { AppBar, FlatButton, IconButton, IconMenu, MenuItem } from 'material-ui';
import AccountCircle from 'material-ui/svg-icons/action/account-circle';
// App Components
import AuthManager from '../auth/utils/AuthManager';
import Logo from '../images/wso2-logo.svg';

const title = {color: '#b9b9b9', fontSize: 18, height: 40, lineHeight: 3};
const appBar = {backgroundColor: '#1a1a1a'};
const accName = {float: 'left', paddingTop: 15};
/**
 * Header component.
 */
export default class Header extends Component {
    /**
     * Render right side header links.
     *
     * @returns {XML} HTML content
     */
    renderRightLinks() {
        if (this.props.hideUserSettings) {
            return <div />;
        }

        // If the user is not set show the login button. Else show account information.
        const user = AuthManager.getUser();
        if (!user) {
            return (
                <FlatButton
                    label="Login"
                    containerElement={<Link to={`${window.contextPath}/login?referrer=${window.location.pathname}`} />}
                />
            );
        }

        return (
            <div>
                <span style={accName}>{user.username}</span>
                <IconMenu
                    iconButtonElement={<IconButton><AccountCircle /></IconButton>}
                    targetOrigin={{ horizontal: 'right', vertical: 'bottom' }}
                    anchorOrigin={{ horizontal: 'right', vertical: 'top' }}
                >
                    <MenuItem
                        primaryText="Logout"
                        containerElement={<Link to={`${window.contextPath}/logout`} />}
                    />
                </IconMenu>
            </div>
        );
    }

    /**
     * Render the component.
     *
     * @returns {XML} HTML content
     */
    render() {
        return (
            <AppBar
                style={appBar}
                title="Stream Processor Status Dashboard"
                iconElementLeft={<img height='24' src={Logo}/>}
                titleStyle={title}
                iconStyleLeft={{margin:'15px 15px 0 0'}}
                iconElementRight={this.renderRightLinks()}
            />
        );
    }
}
