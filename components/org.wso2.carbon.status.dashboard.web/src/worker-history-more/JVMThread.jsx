import React from "react";
//App Components
import DashboardUtils from "../utils/DashboardUtils";
import ChartCard from "../common/ChartCard";
//Material UI
import {Checkbox, Table, TableBody, TableHeader, TableHeaderColumn, TableRow, TableRowColumn} from "material-ui";

const threadMetadata = {names: ['timestamp', 'thread count', 'daemon count'], types: ['time', 'linear', 'linear']};
const threadLineChartConfig = {
    x: 'timestamp',
    charts: [{type: 'line', y: 'thread count', fill: '#f17b31'}, {type: 'line', y: 'daemon count'}],
    width: 700,
    height: 200,
    tickLabelColor: '#9c9898',
    axisLabelColor: '#9c9898'
};

/**
 * JVM Threads chart component.
 */
export default class JVMThread extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            count: this.props.data[0],
            daemonCount: this.props.data[1],
            countChecked: true,
            daemonCountChecked: true
        };
    }

    componentWillReceiveProps(nextprops) {
        this.setState({
            count: nextprops.data[0],
            daemonCount: nextprops.data[1]
        });
    }

    render() {
        let data, config, metadata;
        if (this.state.countChecked && this.state.daemonCountChecked) {
            data = DashboardUtils.getCombinedChartList(this.state.count, this.state.daemonCount);
            config = threadLineChartConfig;
            metadata = threadMetadata;
        } else if (this.state.countChecked) {
            data = this.state.count;
            config = {
                x: 'timestamp', charts: [{type: 'line', y: 'thread count'}], width: 800, height: 250,
                tickLabelColor: '#9c9898', axisLabelColor: '#9c9898'
            };
            metadata = {names: ['timestamp', 'thread count'], types: ['time', 'linear']};
        } else if (this.state.daemonCountChecked) {
            data = this.state.daemonCount;
            config = {
                x: 'timestamp', charts: [{type: 'line', y: 'daemon count'}], width: 800, height: 250,
                tickLabelColor: '#9c9898', axisLabelColor: '#9c9898'
            };
            metadata = {names: ['timestamp', 'daemon count'], types: ['time', 'linear']};
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
                            label="Daemon Thread Count"
                            onCheck={(e, checked) => this.setState({daemonCountChecked: checked})}
                            checked={this.state.daemonCountChecked}
                            iconStyle={{fill: '#f17b31'}}
                        />
                    </div>
                    <div>
                        <Checkbox
                            label="Threads Count"
                            onCheck={(e, checked) => this.setState({countChecked: checked})}
                            checked={this.state.countChecked}
                            iconStyle={{fill: '#f17b31'}}
                        />
                    </div>
                </div>
                <div style={{paddingLeft: 10}}>
                    <ChartCard data={data} metadata={metadata} config={config} title="JVM Threads"/>
                </div>
            </div>
        );
    }
}