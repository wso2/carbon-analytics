import React from "react";
//App Components
import DashboardUtils from "../utils/DashboardUtils";
import ChartCard from "../common/ChartCard";
//Material UI
import {TableHeader, TableHeaderColumn, TableRow, Table, TableRowColumn, TableBody, Checkbox} from "material-ui";

const swapMetadata = {names: ['timestamp', 'free swap size', 'total swap size'], types: ['time', 'linear', 'linear']};
const swapLineChartConfig = {
    x: 'timestamp',
    charts: [{type: 'line', y: 'free swap size', fill: '#f17b31'}, {type: 'line', y: 'total swap size'}],
    width: 700,
    height: 200,
    tickLabelColor: '#9c9898',
    axisLabelColor: '#9c9898'
};

/**
 * JVM Swap Space chart component.
 */
export default class JVMSwap extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            freeSize: this.props.data[0],
            totalSize: this.props.data[1],
            freeSizeChecked: true,
            totalSizeChecked: true
        };
    }

    componentWillReceiveProps(nextprops) {
        this.setState({
            freeSize: nextprops.data[0],
            totalSize: nextprops.data[1]
        });
    }

    render() {
        let data, config, metadata;
        if (this.state.freeSizeChecked && this.state.totalSizeChecked) {
            data = DashboardUtils.getCombinedChartList(this.state.freeSize, this.state.totalSize);
            config = swapLineChartConfig;
            metadata = swapMetadata;
        } else if (this.state.freeSizeChecked) {
            data = this.state.freeSize;
            config = {
                x: 'timestamp', charts: [{type: 'line', y: 'free swap size'}], width: 800, height: 250,
                tickLabelColor: '#9c9898', axisLabelColor: '#9c9898'
            };
            metadata = {names: ['timestamp', 'free swap size'], types: ['time', 'linear']};
        } else if (this.state.totalSizeChecked) {
            data = this.state.totalSize;
            config = {
                x: 'timestamp', charts: [{type: 'line', y: 'total swap size'}], width: 800, height: 250,
                tickLabelColor: '#9c9898', axisLabelColor: '#9c9898'
            };
            metadata = {names: ['timestamp', 'total swap size'], types: ['time', 'linear']};
        } else {
            data =[];
            config = {x: 'timestamp', charts: [{type: 'line', y: 'value'}], width: 800, height: 250,
                tickLabelColor: '#9c9898', axisLabelColor: '#9c9898'};
            metadata = {names: ['timestamp', 'value'], types: ['time', 'linear']};
        }

        return (
            <div>
                <div style={{display: 'flex', flexDirection: 'row', paddingTop: 50, paddingLeft: 30}}>
                    <div>
                        <Checkbox
                            label="Total Swap Size"
                            onCheck={(e, checked) => this.setState({totalSizeChecked: checked})}
                            checked={this.state.totalSizeChecked}
                            iconStyle={{fill: '#f17b31'}}
                        />
                    </div>
                    <div>
                        <Checkbox
                            label="Free Swap Size"
                            onCheck={(e, checked) => this.setState({freeSizeChecked: checked})}
                            checked={this.state.freeSizeChecked}
                            iconStyle={{fill: '#f17b31'}}
                        />
                    </div>
                </div>
                <div style={{paddingLeft: 10}}>
                    <ChartCard data={data} metadata={metadata} config={config} title="JVM Swap Space"/>
                </div>
            </div>
        );
    }
}