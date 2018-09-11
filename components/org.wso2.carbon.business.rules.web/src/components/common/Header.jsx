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
import AppBar from 'material-ui/AppBar';
import Toolbar from 'material-ui/Toolbar';
import IconButton from 'material-ui/IconButton';
import Button from 'material-ui/Button';
import AccountCircle from 'material-ui-icons/AccountCircle';
import HomeIcon from 'material-ui-icons/Home';
import Menu, { MenuItem } from 'material-ui/Menu';
import Logo from '../../images/wso2-logo.svg';
// Auth Utils
import AuthManager from '../../utils/AuthManager';
// CSS
import '../../index.css';

/**
 * Styles related to this component
 */
const styles = {
    title: {
        color: '#EEE',
        fontSize: 16,
        height: 40,
        lineHeight: '40px',
        marginLeft: 15,
        flex: 1
    },
    appBar: {
        backgroundColor: '#263238',
        height:40
    },
    toolBar: {
        height: 40,
        minHeight: '40px'
    },
};

/**
 * App context
 */
const appContext = window.contextPath;

/**
 * Represents the Header
 */
export default class Header extends Component {
    constructor() {
        super();
        this.state = {
            anchorEl: null,
        };
    }

    /**
     * Renders the right side elements of the header
     * @returns {HTMLElement}       Div with right side elements of the header
     */
    renderRightLinks() {
        const homeButton = (
            <Link
                style={{ textDecoration: 'none' }}
                to={`${appContext}/businessRulesManager`}
            >
                <IconButton color="contrast">
                    <HomeIcon />
                </IconButton>
            </Link>
        );

        if (this.props.hideUserSettings) {
            return (<div />);
        }
        // Show account icon / login button depending on logged in status
        const user = AuthManager.getUser();
        if (!user) {
            if (window.location.pathname === appContext + '/login') {
                return (<div />);
            }
            return (
                <Link
                    style={{ textDecoration: 'none' }}
                    to={`${appContext}/login?referrer=${window.location.pathname}`}
                >
                    <Button color="contrast">Login</Button>
                </Link>
            );
        }

        return (
            <div>
                <Toolbar>
                    {!this.props.hideHomeButton ? (homeButton) : (null)}
                    <Typography type="body1" style={{ color: 'inherit' }}>
                        {user.username}
                    </Typography>
                    <IconButton
                        aria-owns={open ? 'menu-appbar' : null}
                        aria-haspopup="true"
                        onClick={(event) => {
                            this.setState({ anchorEl: event.currentTarget });
                        }}
                        color="contrast"
                    >
                        <AccountCircle />
                    </IconButton>
                </Toolbar>
                <Menu
                    id="menu-appbar"
                    anchorEl={this.state.anchorEl}
                    anchorOrigin={{
                        vertical: 'top',
                        horizontal: 'right',
                    }}
                    transformOrigin={{
                        vertical: 'top',
                        horizontal: 'right',
                    }}
                    open={this.state.anchorEl !== null}
                    onRequestClose={() => {
                        this.setState({ anchorEl: null });
                    }}
                >
                    <Link to={`${appContext}/logout`} style={{ textDecoration: 'none', color: 'black' }}>
                        <MenuItem>
                            Log out
                        </MenuItem>
                    </Link>
                </Menu>
            </div>
        );
    }

    render() {
        return (
            <AppBar position="static" style={styles.appBar}>
                <Toolbar style={styles.toolBar}>
                    <Link to={`${appContext}/businessRulesManager`} style={{ textDecoration: 'none', height: 17 }}>
                        <img height="17" src={Logo} style={{ cursor: 'pointer' }} />
                    </Link>
                    <Typography type="subheading" style={styles.title}>
                        Business Rules Manager
                    </Typography>
                    {this.renderRightLinks()}
                </Toolbar>
            </AppBar>
        );
    }
}

Header.propTypes = {
    hideUserSettings: PropTypes.bool,
    hideHomeButton: PropTypes.bool,
};

Header.defaultProps = {
    hideUserSettings: false,
    hideHomeButton: false,
};
