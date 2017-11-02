import React from "react";
//App Components
import DashboardUtils from "../utils/DashboardUtils";
import ChartCard from "../common/ChartCard";
//Material UI
import {Checkbox, Table, TableBody, TableHeader, TableHeaderColumn, TableRow, TableRowColumn} from "material-ui";

const cpuMetadata = {names: ['timestamp', 'system cpu', 'process cpu'], types: ['time', 'linear', 'linear']};
const cpuLineChartConfig = {
    x: 'timestamp',
    charts: [{type: 'line', y: 'system cpu', fill: '#f17b31'}, {type: 'line', y: 'process cpu'}],
    width: 700,
    height: 200,
    tickLabelColor: '#9c9898',
    axisLabelColor: '#9c9898'
};

/**
 * JVM Operating System chart component.
 */
export default class JVMOs extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            loadProcess: this.props.data[0],
            loadSystem: this.props.data[1],
            loadProcessChecked: true,
            loadSystemChecked: true
        };
    }

    componentWillReceiveProps(nextprops) {
        this.setState({
            loadProcess: nextprops.data[0],
            loadSystem: nextprops.data[1]
        });
    }

    render() {
        let data, config, metadata;
        if (this.state.loadProcessChecked && this.state.loadSystemChecked) {
            data = DashboardUtils.getCombinedChartList(this.state.loadProcess, this.state.loadSystem);
            config = cpuLineChartConfig;
            metadata = cpuMetadata;
        } else if (this.state.loadProcessChecked) {
            data = this.state.loadProcess;
            config = {
                x: 'timestamp', charts: [{type: 'line', y: 'process cpu'}], width: 800, height: 250,
                tickLabelColor: '#9c9898', axisLabelColor: '#9c9898'
            };
            metadata = {names: ['timestamp', 'process cpu'], types: ['time', 'linear']};
        } else if (this.state.loadSystemChecked) {
            data = this.state.loadSystem;
            config = {
                x: 'timestamp', charts: [{type: 'line', y: 'system cpu'}], width: 800, height: 250,
                tickLabelColor: '#9c9898', axisLabelColor: '#9c9898'
            };
            metadata = {names: ['timestamp', 'system cpu'], types: ['time', 'linear']};
        } else {
            data = [];
            config = {
                x: 'timestamp', charts: [{type: 'line', y: 'value'}], width: 800, height: 250,
                tickLabelColor: '#9c9898', axisLabelColor: '#9c9898'
            };
            metadata = {names: ['timestamp', 'value'], types: ['time', 'linear']};
        }

        return (
            <div>
                <div style={{display: 'flex', flexDirection: 'row', paddingTop: 50, paddingLeft: 30}}>
                    <div>
                        <Checkbox
                            label="System CPU"
                            onCheck={(e, checked) => this.setState({loadSystemChecked: checked})}
                            checked={this.state.loadSystemChecked}
                            iconStyle={{fill: '#f17b31'}}
                        />
                    </div>
                    <div>
                        <Checkbox
                            label="Process CPU"
                            onCheck={(e, checked) => this.setState({loadProcessChecked: checked})}
                            checked={this.state.loadProcessChecked}
                            iconStyle={{fill: '#f17b31'}}
                        />
                    </div>
                </div>
                <div style={{padding: 30}}>
                    <ChartCard data={data} metadata={metadata} config={config} title="JVM OS CPU"/>
                </div>
            </div>
        );
    }
}