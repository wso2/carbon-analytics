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
import IconButton from 'material-ui/IconButton';
import Tooltip from 'material-ui/Tooltip';
import NewIcon from 'material-ui-icons/InsertDriveFile';
import SaveIcon from 'material-ui-icons/Save';
import CodeIcon from 'material-ui-icons/Code';
import SettingsIcon from 'material-ui-icons/Settings';
import FolderIcon from 'material-ui-icons/Folder';
import Logo from '../images/wso2-logo.svg';
// CSS
import '../index.css';

// Styles related to this component
const styles = {
    headerStyle: {
        color: 'white',
        backgroundColor: '#212121',
    }
};

/**
 * Represents the header
 */
class Header extends React.Component {
    constructor() {
        super();
        this.state = {
            anchorEl: null,
            showOpenDialog: false,
            showSaveConfirmation: false,
            saveConfirmationAction: '',
            showSnackbar: false,
            snackbarMessage: '',
            snackbarAutoHideDuration: 3500,
        };
    }

    render() {
        return (
            <AppBar position="static" style={styles.headerStyle}>
                <Toolbar>
                    <img height='35' src={Logo} />
                    &nbsp;
                    &nbsp;
                    &nbsp;
                    <Typography type="subheading" color="inherit" style={{flex: 1}}>
                        Business Rules Template Editor
                    </Typography>
                    <Tooltip title="New">
                        <IconButton color="contrast" onClick={() => this.props.onNewClick()}>
                            <NewIcon/>
                        </IconButton>
                    </Tooltip>
                    <Tooltip title="Open">
                        <IconButton color="contrast" onClick={() => this.props.onOpenClick()}>
                            <FolderIcon/>
                        </IconButton>
                    </Tooltip>
                    <Tooltip title="Save">
                        <IconButton
                            color={this.props.isUnsaved ? "primary" : "contrast"}
                            onClick={() => this.props.onSaveClick()}
                        >
                            <SaveIcon/>
                        </IconButton>
                    </Tooltip>
                    <Tooltip
                        id="tooltip-bottom"
                        title="Editor Appearance Settings"
                        placement="bottom"
                    >
                        <IconButton
                            color="contrast"
                            aria-label='toggleLightDark'
                            onClick={() => this.props.onSettingsClick()}
                        >
                            <SettingsIcon />
                        </IconButton>
                    </Tooltip>
                    <Tooltip id="tooltip-bottom" title="Toggle code view" placement="bottom">
                        <IconButton
                            color={this.props.isCodeViewEnabled ? ('primary') : ('contrast')}
                            aria-label='toggleCodeView'
                            onClick={() => this.props.onCodeViewToggle()}
                        >
                            <CodeIcon />
                        </IconButton>
                    </Tooltip>
                </Toolbar>
            </AppBar>
        );
    }
}

export default Header;
