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

import React from 'react';
import {Link} from 'react-router-dom';
//Material UI
import {GridList, GridTile} from 'material-ui/GridList';
import Info from 'material-ui/svg-icons/action/info';
import HomeButton from 'material-ui/svg-icons/action/home';
import {
    IconButton, Divider, FlatButton, RaisedButton, Toggle, FloatingActionButton, Paper,
    FontIcon
} from "material-ui";
import ContentAdd from 'material-ui/svg-icons/content/add';
//App Components
import WorkerThumbnail from "./WorkerThumbnail";
import StatusDashboardAPIS from "../utils/apis/StatusDashboardAPIs";

const styles = {
    root: {display: 'flex', flexWrap: 'wrap', justifyContent: 'space-around', backgroundColor: '#222222'},
    gridList: {width: '90%', height: '100%', overflowY: 'auto', padding: 40},
    h3: {color: 'white', marginLeft: '4%', backgroundColor: '#222222'},
    titleStyle: {fontSize: 18, lineHeight: 1.5, color: '#FF3D00'},
    headerStyle: {height: 30, backgroundColor: '#242424'},
    paper: {height: 50, width: 500, textAlign: 'center'}
};

/**
 * class which manages overview page.
 */
export default class WorkerOverview extends React.Component {

    constructor() {
        super();
        this.state = {
            clustersList: {},
            pInterval: 0,
            currentTime: '',
            interval: '',
            enableAutoSync: false,
            isApiCalled: false,
            counter: 0
        };
        this.autoSync = this.autoSync.bind(this);
        this.renderWorkers = this.renderWorkers.bind(this);
    }

    componentDidMount() {
        StatusDashboardAPIS.getDashboardConfig()
            .then((response) => {
                this.setState({
                    pInterval: response.data,
                    counter: this.state.counter
                });
            }).catch((error) => {
            //TODO Need to use proper notification library to show the error
        });

        this.setState({currentTime: new Date().getTime()});
        StatusDashboardAPIS.getWorkersList()
            .then((response) => {
                this.setState({
                    clustersList: response.data,
                    isApiCalled: true
                });
            }).catch((error) => {
            //TODO Need to use proper notification library to show the error
        });
    }

    componentWillUnmount() {
        clearInterval(this.state.interval);
    }

    /**
     * Method which handles auto sync button submit
     */
    autoSync() {
        let interval;
        let that = this;
        if (!this.state.enableAutoSync) {
            interval = setInterval(() => {
                that.setState({currentTime: new Date().getTime()});
                StatusDashboardAPIS.getWorkersList()
                    .then((response) => {
                        that.setState({clustersList: response.data});
                    }).catch((error) => {
                    //TODO Need to use proper notification library to show the error
                });
            }, parseInt(this.state.pInterval.pollingInterval * 1000));
            this.setState({interval: interval, enableAutoSync: true});
        } else {
            clearInterval(this.state.interval);
            this.setState({enableAutoSync: false});
        }
    }

    /**
     * Method which render workers
     * @param workersList
     * @returns {XML}
     */
    renderWorkers(workersList) {
        if (this.state.isApiCalled && !WorkerOverview.hasWorkers(this.state.clustersList)) {
            return (
                <div style={{backgroundColor: '#222222'}}>
                    <div className="info-card" style={{backgroundColor: '#f17b31'}}>
                        <FlatButton
                            label="Currently there are no workers to display"
                            icon={<Info />}
                            style={{marginTop: 10, backgroundColor: '#f17b31'}}
                        />
                    </div>
                    <div className="add-button">
                        <Link to="/sp-status-dashboard/add-worker"><FlatButton
                            label="Add New Worker"
                            icon={<ContentAdd />}
                            style={{marginTop: 10}}
                        /></Link>
                    </div>
                </div>
            );
        } else if (this.state.isApiCalled && WorkerOverview.hasWorkers(this.state.clustersList)) {
            return (
                <div style={{backgroundColor: '#222222'}}>
                    <div style={{height: 80, padding: 20, backgroundColor: '#222222'}}>
                        <div className="floating-button">
                            <Link to="add-worker">
                                <FloatingActionButton backgroundColor='#f17b31'>
                                    <ContentAdd />
                                </FloatingActionButton>
                            </Link>
                        </div>
                        <div className="toggle">
                            <Toggle labelPosition="left"
                                    label={<b>Auto Sync</b>}
                                    labelStyle={{color: 'white', fontSize: 18}}
                                    onToggle={this.autoSync}
                                    thumbStyle={{backgroundColor: 'grey'}}
                                    thumbSwitchedStyle={{backgroundColor: '#f17b31'}}
                                    trackSwitchedStyle={{backgroundColor: '#f17b31'}}>
                            </Toggle>
                        </div>
                    </div>

                    { Object.keys(workersList).map((id, workerList) => {
                        return (
                            <div>
                                <h3 style={styles.h3}>{id}</h3>
                                <Divider inset={true} style={{width: '90%'}}/>
                                <div style={styles.root}>
                                    <GridList cols={3} padding={50} cellHeight={300} style={styles.gridList}>
                                        {workersList[id].map((worker) => {
                                            return (
                                                <WorkerThumbnail worker={worker}
                                                                 currentTime={this.state.currentTime}/>
                                            )
                                        })}
                                    </GridList>
                                </div>
                            </div>
                        )
                    })}
                </div>
            );
        } else {
            return (
                <div style={{backgroundColor: '#222222', width: '100%', height: '1000px'}} data-toggle="loading"
                     data-loading-inverse="true">
                    <div id="wrapper" style={{
                        backgroundColor: '#222222',
                        textAlign: 'center',
                        paddingTop: '200px',
                        paddingBottom: '200px'
                    }}>
                        <i className="fw fw-sync fw-spin fw-inverse fw-5x"></i>
                    </div>

                </div>
            );
        }
    }

    render() {
        return (
            <div style={{backgroundColor: '#222222'}}>
                <div className="navigation-bar">
                    <FlatButton label="Overview" icon={<HomeButton color="black"/>}/>
                </div>
                {this.renderWorkers(this.state.clustersList)}
            </div>
        );
    }

    static hasWorkers(clusters) {
        for (let prop in clusters) {
            if (clusters.hasOwnProperty(prop)) {
                return true;
            }
        }
        return false;
    }

}


