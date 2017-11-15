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
import {CircularProgress} from 'material-ui/Progress';
import {Typography} from "material-ui";
import Paper from 'material-ui/Paper';
// App Components
import Header from "./Header";
// CSS
import '../index.css';
// Custom Theme
import {createMuiTheme, MuiThemeProvider} from 'material-ui/styles';
import {Orange} from './styles/BusinessRulesManagerColors';

const theme = createMuiTheme({
    palette: {
        primary: Orange,
    },
});

const styles = {
    progress: {
        color: '#EF6C00',
    },
    paper: {
        maxWidth: 400,
        paddingTop: 30,
        paddingBottom: 30
    },
}

/**
 * Shows circular progress
 */
class ProgressDisplay extends React.Component {
    render() {
        return (
            <MuiThemeProvider theme={theme}>
                <div>
                    <center>
                        <Header/>
                        <br/>
                        <br/>
                        <div>
                            <Paper style={styles.paper}>
                                <br/>
                                {(this.props.error) ?
                                    (<div>
                                        <Typography type="headline">
                                            {this.props.error[0]}
                                        </Typography>
                                        <Typography type="body2">
                                            {this.props.error[1]}
                                        </Typography>
                                    </div>) :
                                    (<div>
                                        <CircularProgress size={50}/>
                                        <Typography type="subheading">
                                            Please wait
                                        </Typography>
                                    </div>)
                                }
                                <br/>
                            </Paper>
                        </div>
                        <br/>
                    </center>
                </div>
            </MuiThemeProvider>
        )

    }
}

export default ProgressDisplay;