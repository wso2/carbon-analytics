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

import React from "react";
import {Link} from "react-router-dom";
//Material UI
import {CardActions, Dialog, FlatButton, GridList, GridTile, IconButton, Snackbar} from "material-ui";
import ToolTip from "react-tooltip";
import CircleBorder from "material-ui/svg-icons/av/fiber-manual-record";
import AuthenticationAPI from "../utils/apis/AuthenticationAPI";
import AuthManager from "../auth/utils/AuthManager";

const styles = {gridList: {width: '100%', height: 250}, smallIcon: {width: 20, height: 20, zIndex: 1}};
const messageBoxStyle = {textAlign: "center", color: "white"};
const errorMessageStyle = {backgroundColor: "#FF5722", color: "white"};
const successMessageStyle = {backgroundColor: "#4CAF50", color: "white"};

export default class DistributedViewAppThumbnail extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            warning: '',
            showMsg: '',
            message: '',
            messageStyle: '',
            open: false,
            distributedApp: this.props.distributedApp,
            hasPermission: false
        };

        this.showError = this.showError.bind(this);
        this.showMessage = this.showMessage.bind(this);
    }

    componentWillMount() {
        let that = this;
        AuthenticationAPI.isUserAuthorized('manager', AuthManager.getUser().SDID)
            .then((response) => {
                that.setState({
                    hasPermission: response.data
                });
            });
    }

    showError(message) {
        this.setState({
            messageStyle: errorMessageStyle,
            showMsg: true,
            message: message
        });
    }

    showMessage(message) {
        this.setState({
            messageStyle: successMessageStyle,
            showMsg: true,
            message: message
        });
    }

    componentWillReceiveProps(nextProps) {
        console.log("property" + nextProps);
        this.setState({distributedApp: nextProps.distributedApp});
        console.log("props" + nextProps);
    }


    renderGridTile() {
        let gridTiles, color, appStatus;
        // if(this.props.worker.ParentAppName != null){
        console.log("AM passing here");
        console.log(this.props.distributedApp);
        console.log("data" + this.props.distributedApp.parentAppName);


        // if(this.props.distributedApp.parentAppName != null){
        gridTiles =
            <div>
                <Link style={{textDecoration: 'none'}}
                      to={window.contextPath + "/" + this.props.distributedApp.managerId + '/siddhi-apps/' + this.props.distributedApp.parentAppName}>
                    <GridList cols={3} cellHeight={180}  style={styles.gridList}>

                        <GridTile title="Groups" titlePosition="bottom" titleStyle={{fontSize: 10}} style={{marginLeft: '30%'}} data-tip
                                  data-for='groups'>
                            <div className="grid-tile-h1" style={{marginTop: '50%',marginLeft:-50}}>
                                <h1 className="deployed-apps-details">{this.props.distributedApp.numberOfGroups}</h1>
                            </div>
                            <ToolTip id='groups' aria-haspopup='true' role='example'>
                                <p>Indicates number of groups in the parent siddhi application</p>
                            </ToolTip>

                        </GridTile>
                        <GridTile title="Child Apps" titlePosition="bottom" titleStyle={{fontSize: 10}} style={{marginLeft: '30%'}}>
                            <div className="grid-tile-h1" style={{marginTop: '50%'}}><h1
                                className="active-apps" data-tip data-for='deployedChildApps'>{this.props.distributedApp.deployedChildApps}</h1>
                                <h1 style={{display: 'inline'}}> | </h1>
                                <h1 className="inactive-apps" data-tip data-for='undeployedchildapps'>
                                    {this.props.distributedApp.undeployedChildApps}
                                </h1>
                            </div>
                            <ToolTip id='deployedChildApps' aria-haspopup='true' role='example'>
                            <p>Indicates number of deployed child apps</p>
                            </ToolTip>
                            <ToolTip id='undeployedchildapps' aria-haspopup='true' role='example'>
                                <p>Indicates number of un-deployed child apps</p>
                            </ToolTip>
                        </GridTile>
                        <GridTile title="Worker Nodes" titlePosition="bottom" titleStyle={{fontSize: 10}} style={{marginLeft: '30%'}}>
                            <div className="grid-tile-h1" style={{marginTop: '50%'}}><h1
                                className="active-apps" data-tip data-for='usedWorkerNodes'>{this.props.distributedApp.usedWorkerNodes}</h1>
                                <h1 style={{display: 'inline'}}> | </h1>
                                <h1 className="inactive-apps" style={{color:'orange'}} data-tip data-for='totalWorkerNodes'>
                                    {this.props.distributedApp.totalWorkerNodes}
                                </h1>
                            </div>
                            <ToolTip id='usedWorkerNodes' aria-haspopup='true' role='example'>
                                <p>Indicates number of worker nodes used in the <br/> particular parent siddhi
                                    applications deployment</p>
                            </ToolTip>
                            <ToolTip id='totalWorkerNodes' aria-haspopup='true' role='example'>
                                <p>Indicates total number of worker nodes in the resource cluster</p>
                            </ToolTip>
                        </GridTile>

                        {/*<GridTile title="Used workerNodes" titlePosition="bottom" titleStyle={{fontSize: 10}} data-tip*/}
                                  {/*data-for='usedWorkerNodes'>*/}
                            {/*<div style={{display: 'inline', float: 'right', marginTop: '65%', marginRight: '60%'}}>*/}

                                {/*<h1 className="deployed-apps-details">{this.props.distributedApp.usedWorkerNodes}</h1>*/}
                            {/*</div>*/}
                            {/*<ToolTip id='usedWorkerNodes' aria-haspopup='true' role='example'>*/}
                                {/*<p>Indicates how many worker nodes used for the <br/> particular parent siddhi*/}
                                    {/*applications deployment</p>*/}
                            {/*</ToolTip>*/}

                        {/*</GridTile>*/}
                        {/*<GridTile title="workerNodes" titlePosition="bottom" titleStyle={{fontSize: 10}} data-tip*/}
                                  {/*data-for='totalWorkerNodes'>*/}
                            {/*<div style={{display: 'inline', float: 'right', marginTop: '65%', marginRight: '60%'}}>*/}

                                {/*<h1 className="deployed-apps-details">{this.props.distributedApp.totalWorkerNodes}</h1>*/}
                            {/*</div>*/}
                            {/*<ToolTip id='totalWorkerNodes' aria-haspopup='true' role='example'>*/}
                                {/*<p>Indicates total number of worker nodes inside resource cluster</p>*/}
                            {/*</ToolTip>*/}
                        {/*</GridTile>*/}
                    </GridList>
                </Link>
            </div>
        //if (this.props.worker.serverDetails.runningStatus === "Reachable") {
        if (this.props.distributedApp.usedWorkerNodes !== "0") {
            color = 'green';
            appStatus = 'Deployed'
        } else {
            color = 'red';
            appStatus = 'Not-Deployed'
        }

        return [gridTiles, color, appStatus];
    }

    render() {
        let items = this.renderGridTile();
        return (

            <div>
                <GridTile
                    title={this.props.distributedApp.parentAppName}
                    actionIcon={<IconButton ><CircleBorder
                        color={items[1]}/></IconButton>}
                    // actionIcon={<IconButton><CircleBorder
                    //     color={items[2]}/></IconButton>}
                    actionPosition="left"
                    style={{background: 'black'}}
                    titleBackground={'#424242'}
                >
                    <CardActions style={{boxSizing: 'border-box', float: 'right', display: 'inline', height: 20}}>

                    </CardActions>
                    {items[0]}
                </GridTile>
                <Snackbar contentStyle={messageBoxStyle} bodyStyle={this.state.messageStyle} open={this.state.showMsg}
                          message={this.state.message} autoHideDuration={4000}
                          onRequestClose={() => {
                              this.setState({showMsg: false, message: ""})
                          }}/>
            </div>
        );
    }

}