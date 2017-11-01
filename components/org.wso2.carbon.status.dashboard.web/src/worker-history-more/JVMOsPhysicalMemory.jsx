import React from "react";
//App Components
import DashboardUtils from "../utils/DashboardUtils";
import ChartCard from "../common/ChartCard";
//Material UI
import {TableHeader, TableHeaderColumn, TableRow, Table, TableRowColumn, TableBody, Checkbox} from "material-ui";

const memoryMetadata = {names: ['timestamp', 'free physical memory', 'total physical memory'], types: ['time', 'linear', 'linear']};
const memoryLineChartConfig = {
    x: 'timestamp',
    charts: [{type: 'line', y: 'free physical memory', fill: '#f17b31'}, {type: 'line', y: 'total physical memory'}],
    width: 700,
    height: 200,
    tickLabelColor: '#9c9898',
    axisLabelColor: '#9c9898'
};

/**
 * JVM Physical memory chart component.
 */
export default class JVMOsPhysicalMemory extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            free: this.props.data[0],
            total: this.props.data[1],
            freeChecked: true,
            totalChecked: true
        };
    }

    componentWillReceiveProps(nextprops) {
        this.setState({
            free: nextprops.data[0],
            total: nextprops.data[1]
        });
    }

    render() {
        let data, config, metadata;
        if (this.state.freeChecked && this.state.totalChecked) {
            data = DashboardUtils.getCombinedChartList(this.state.free, this.state.total);
            config = memoryLineChartConfig;
            metadata = memoryMetadata;
        } else if (this.state.freeChecked) {
            data = this.state.free;
            config = {
                x: 'timestamp', charts: [{type: 'line', y: 'free physical memory'}], width: 800, height: 250,
                tickLabelColor: '#9c9898', axisLabelColor: '#9c9898'
            };
            metadata = {names: ['timestamp', 'free physical memory'], types: ['time', 'linear']};
        } else if (this.state.totalChecked) {
            data = this.state.total;
            config = {
                x: 'timestamp', charts: [{type: 'line', y: 'total physical memory'}], width: 800, height: 250,
                tickLabelColor: '#9c9898', axisLabelColor: '#9c9898'
            };
            metadata = {names: ['timestamp', 'total physical memory'], types: ['time', 'linear']};
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
                            label="Total Physical memory"
                            onCheck={(e, checked) => this.setState({totalChecked: checked})}
                            checked={this.state.totalChecked}
                            iconStyle={{fill: '#f17b31'}}
                        />
                    </div>
                    <div>
                        <Checkbox
                            label="Free Physical memory"
                            onCheck={(e, checked) => this.setState({freeChecked: checked})}
                            checked={this.state.freeChecked}
                            iconStyle={{fill: '#f17b31'}}
                        />
                    </div>
                </div>
                <div style={{padding: 30}}>
                    <ChartCard data={data} metadata={metadata} config={config} title="JVM Physical Memory"/>
                </div>
            </div>
        );
    }
}