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

import React from "react";
import {Link} from "react-router-dom";
//Material UI
import {GridList} from "material-ui/GridList";
import Info from "material-ui/svg-icons/action/info";
import HomeButton from "material-ui/svg-icons/action/home";
import {
    Card, CardText, Divider, FlatButton, RaisedButton
} from "material-ui";
//App Components
import DistributedViewAppThumbnail from "./DistributedViewAppThumbnail";
import StatusDashboardAPIS from "../utils/apis/StatusDashboardAPIs";
import Header from "../common/Header";
import AuthenticationAPI from "../utils/apis/AuthenticationAPI";
import AuthManager from "../auth/utils/AuthManager";
import {Redirect} from 'react-router-dom';
import FormPanel from "../common/FormPanel";
import Error500 from "../error-pages/Error500";
import {HttpStatus} from "../utils/Constants";

const styles = {
    root: {display: 'flex', flexWrap: 'wrap', justifyContent: 'space-around', backgroundColor: '#222222'},
    gridList: {width: '90%', height: '100%', overflowY: 'auto', padding: 40},
    h3: {color: 'white', marginLeft: '4%', backgroundColor: '#222222'},
    titleStyle: {fontSize: 18, lineHeight: 1.5, color: '#FF3D00'},
    headerStyle: {height: 30, backgroundColor: '#242424'},
    paper: {height: 50, width: 500, textAlign: 'center'},
    background: {backgroundColor: '#222222'}
};
const errorTitleStyles = {
    color: "#c7cad1",
    fontSize: 45
};

const errorMessageStyles = {
    color: "#abaeb4",
    fontSize: 22
};
const errorContainerStyles = {
    textAlign: "center",
    marginTop: 30
};
const buttonStyle = {marginLeft: 50, width: '35%', fontSize: '12px', backgroundColor: '#f17b31'};
/**
 * class which manages overview page.
 */
export default class DistributedOverview extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            sessionInvalid: false,
            id: this.props.match.params.id,
            clustersList: {},
            pInterval: window.localStorage.getItem("pInterval") != null ? parseInt(window.localStorage.getItem("pInterval")) : 5,
            currentTime: '',
            interval: '',
            isApiCalled: false,
            counter: 0,
            hasManagerPermission: false,
            hasViewPermission: true,
            statusMessage: "Currently there are no siddhi application to display",
            isError: false

        };
        this.renderWorkers = this.renderWorkers.bind(this);
    }

    componentDidMount() {
        StatusDashboardAPIS.getDashboardConfig()
            .then((response) => {
                this.setState({
                    pInterval: response.data.pollingInterval,
                    counter: this.state.counter
                });
            }).catch((error) => {
            if (error.response != null) {
                if (error.response.status === 401) {
                    this.setState({
                        sessionInvalid: true,
                        statusMessage: "Authentication fail. Please login again.",
                        isApiCalled: true
                    })
                } else if (error.response.status === 403) {
                    this.setState({
                        hasViewPermission: false,
                        statusMessage: "User Have No Permission to view this page.",
                        isApiCalled: true
                    })
                } else {
                    this.setState({
                        isError: true,
                        statusMessage: "Unknown error occurred! : " + JSON.stringify(error.response.data),
                        isApiCalled: true
                    })
                }
            }
        });

        this.setState({currentTime: new Date().getTime()});
        //console.log("Id is"+this.props.match.params.id);
        StatusDashboardAPIS.getManagerSiddhiApps(this.props.match.params.id)
            .then((response) => {
                //console.log("id"+this.props.match.params.id);
                if (response.status = HttpStatus.OK) {
                    this.setState({
                        clustersList: response.data,
                        isApiCalled: true,
                        statusMessage: response.data === null ? "Currently there are no" +
                            " apps to display" : ''
                    });
                }
            }).catch((error) => {
            if (error.response != null) {
                if (error.response.status === 401) {
                    this.setState({
                        isApiCalled: true,
                        sessionInvalid: true,
                        statusMessage: "Authentication fail. Please login again."
                    })
                } else if (error.response.status === 403) {
                    this.setState({
                        isApiCalled: true,
                        statusMessage: "User Have No Permission to view this page."
                    });
                } else {
                    this.setState({
                        isError: true,
                        isApiCalled: true,
                        statusMessage: "Unknown error occurred! : " + JSON.stringify(error.response.data)
                    });
                }
            }
            // console.log("ClusterList"+this.state.clustersList.toString());
        });


    }


    componentWillUnmount() {
        clearInterval(this.state.interval);

    }

    componentWillMount() {
        let that = this;
        // this.initAutoSync();
        AuthenticationAPI.isUserAuthorized('manager', AuthManager.getUser().SDID)
            .then((response) => {
                that.setState({
                    hasManagerPermission: response.data
                });
            })
            .catch((error) => {
                if (error.response.status === 401) {
                    this.setState({
                        isApiCalled: true,
                        sessionInvalid: true,
                        statusMessage: "Authentication fail. Please login again."
                    })
                } else {
                    this.setState({
                        isError: true,
                        isApiCalled: true,
                        statusMessage: "Unknown error occurred! : " + JSON.stringify(error.response.data)
                    });
                }
            });
    }


    /**
     * Method which render workers
     * @param workersList
     * @returns {XML}
     */
    renderWorkers(appList) {
        console.log("cluster" + this.state.clustersList);
        if (this.state.isApiCalled && !DistributedOverview.hasApps(this.state.clustersList)) {
            if (this.state.hasViewPermission) {
                return (
                    <div style={styles.background}>
                        <div className="info-card" style={{backgroundColor: '#f17b31'}}>
                            <FlatButton
                                label={"Currently there are no siddhi apps deployed in the node"}
                                icon={<Info/>}
                                style={{marginTop: 10, backgroundColor: '#f17b31'}}
                            />
                        </div>
                    </div>
                );
            } else {
                return (
                    <div style={styles.background}>
                        <Card style={{
                            width: 700, high: '100%', marginTop: '10%', marginLeft: '33%', backgroundColor: '#1a1a1a',
                            borderColor: '#f17b31', borderRadius: 2, borderBottomColor: '#f17b31'
                        }}>
                            <CardText style={{borderBottom: '1px solid #AE5923', borderTop: '1px solid #AE5923'}}>
                                <FormPanel title={""} width={650}>
                                    <div style={errorContainerStyles}>
                                        <i class="fw fw-security fw-inverse fw-5x"></i>
                                        <h1 style={errorTitleStyles}>Page Forbidden!</h1>
                                        <text style={errorMessageStyles}>You have no permission to access this page.
                                        </text>
                                        <br/>
                                        <br/>
                                        <Link to={`${window.contextPath}/logout`}>
                                            <RaisedButton backgroundColor='#f17b31' style={buttonStyle} label="Login"/>
                                        </Link>
                                    </div>
                                </FormPanel>
                            </CardText>
                        </Card>
                    </div>
                );
            }
            //deleted one condition
        } else if (this.state.isApiCalled && DistributedOverview.hasApps(this.state.clustersList)) {
            console.log("Am going through");
            console.log("app list" + appList);
            //console.log(this.state.clustersList.toString());
            return <div style={styles.background}>
                <div style={{height: 20, padding: 20, backgroundColor: '#222222'}}>

                </div>

                <div>
                    <h3 style={styles.h3}>Parent Siddhi Application</h3>
                    <Divider inset={true} style={{width: '90%'}}/>
                    <div style={styles.root}>
                        <GridList cols={3} padding={50} cellHeight={300} style={styles.gridList}>

                            {Object.keys(appList).map(key => {
                                return (

                                    <DistributedViewAppThumbnail distributedApp={appList[key]}
                                                                 currentTime={new Date().getTime()}/>
                                )
                            })}
                        </GridList>
                    </div>
                </div>


            </div>;
        } else {
            return (
                <div style={{backgroundColor: '#222222', width: '100%', height: '1000px'}} data-toggle="loading"
                     data-loading-inverse="true">
                    <div style={{
                        textAlign: 'center',
                        paddingTop: '200px'
                    }}>
                        <i className="fw fw-loader5 fw-spin fw-inverse fw-5x"></i>
                    </div>

                </div>
            );
        }
    }

    render() {
        if (this.state.isError) {
            return <Error500 message={this.state.statusMessage}/>;
        }
        if (!this.state.sessionInvalid) {
            return (
                <div style={styles.background}>
                    <Header/>
                    <div className="navigation-bar">
                        <FlatButton label="Overview" icon={<HomeButton color="black"/>}/>
                    </div>
                    {this.renderWorkers(this.state.clustersList)}
                </div>
            );
        } else {
            return (
                <Redirect to={{pathname: `${window.contextPath}/logout`}}/>
            );
        }
    }

    static hasApps(clusters) {
        for (let prop in clusters) {

            if (clusters.hasOwnProperty(prop)) {
                return true;
            }
        }
        return false;
    }
}


