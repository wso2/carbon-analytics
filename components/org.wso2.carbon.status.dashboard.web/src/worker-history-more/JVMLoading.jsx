import React from "react";
//App Components
import DashboardUtils from "../utils/DashboardUtils";
import ChartCard from "../common/ChartCard";
//Material UI
import {Checkbox, Table, TableBody, TableHeader, TableHeaderColumn, TableRow, TableRowColumn} from "material-ui";

const metadataAll = {
    names: ['timestamp', 'loaded total', 'loaded current', 'unloaded total'],
    types: ['time', 'linear', 'linear', 'linear']
};
const chartConfigAll = {
    x: 'timestamp',
    charts: [{type: 'line', y: 'loaded total', fill: '#058DC7'}, {
        type: 'line',
        y: 'loaded current',
        fill: '#50B432'
    }, {type: 'line', y: 'unloaded total', fill: '#f17b31'}],
    width: 700,
    height: 200,
    tickLabelColor: '#9c9898',
    axisLabelColor: '#9c9898'
};

/**
 * JVM Loading chart component.
 */
export default class JVMLoading extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            jvmClassLoadingLoadedTotal: this.props.data[0],
            jvmClassLoadingLoadedCurrent: this.props.data[1],
            jvmClassLoadingUnloadedTotal: this.props.data[2],
            loadedTotalChecked: true,
            loadedCurrentChecked: true,
            unloadedTotalChecked: true
        }
    }

    componentWillReceiveProps(nextprops) {
        this.setState({
            jvmClassLoadingLoadedTotal: nextprops.data[0],
            jvmClassLoadingLoadedCurrent: nextprops.data[1],
            jvmClassLoadingUnloadedTotal: nextprops.data[2]
        });
    }

    render() {
        let data, config, metadata;
        if (this.state.loadedTotalChecked && this.state.loadedCurrentChecked && this.state.unloadedTotalChecked) {
            data = DashboardUtils.getCombinedChartList(DashboardUtils.getCombinedChartList(this.state.jvmClassLoadingLoadedTotal,
                this.state.jvmClassLoadingLoadedCurrent), this.state.jvmClassLoadingUnloadedTotal);
            config = chartConfigAll;
            metadata = metadataAll;
        } else if (this.state.loadedTotalChecked && this.state.loadedCurrentChecked) {
            data = DashboardUtils.getCombinedChartList(this.state.jvmClassLoadingLoadedTotal,
                this.state.jvmClassLoadingLoadedCurrent);
            config = {
                x: 'timestamp',
                charts: [{type: 'line', y: 'loaded total', fill: '#f17b31'}, {type: 'line', y: 'loaded current'}],
                width: 700,
                height: 200,
                tickLabelColor: '#9c9898',
                axisLabelColor: '#9c9898'
            };
            metadata = {names: ['timestamp', 'loaded total', 'loaded current'], types: ['time', 'linear', 'linear']};
        } else if (this.state.loadedCurrentChecked && this.state.unloadedTotalChecked) {
            data = DashboardUtils.getCombinedChartList(this.state.jvmClassLoadingLoadedCurrent,
                this.state.jvmClassLoadingUnloadedTotal);
            config = {
                x: 'timestamp',
                charts: [{type: 'line', y: 'loaded current', fill: '#f17b31'}, {type: 'line', y: 'unloaded total'}],
                width: 700,
                height: 200,
                tickLabelColor: '#9c9898',
                axisLabelColor: '#9c9898'
            };
            metadata = {names: ['timestamp', 'loaded current', 'unloaded total'], types: ['time', 'linear', 'linear']};
        } else if (this.state.loadedTotalChecked && this.state.unloadedTotalChecked) {
            data = DashboardUtils.getCombinedChartList(this.state.jvmClassLoadingLoadedTotal,
                this.state.jvmClassLoadingUnloadedTotal);
            config = {
                x: 'timestamp',
                charts: [{type: 'line', y: 'loaded total', fill: '#f17b31'}, {type: 'line', y: 'unloaded total'}],
                width: 700,
                height: 200,
                tickLabelColor: '#9c9898',
                axisLabelColor: '#9c9898'
            };
            metadata = {names: ['timestamp', 'loaded total', 'unloaded total'], types: ['time', 'linear', 'linear']};
        } else if (this.state.loadedTotalChecked) {
            data = this.state.jvmClassLoadingLoadedTotal;
            config = {
                x: 'timestamp', charts: [{type: 'line', y: 'loaded total'}], width: 800, height: 250,
                tickLabelColor: '#9c9898', axisLabelColor: '#9c9898'
            };
            metadata = {names: ['timestamp', 'loaded total'], types: ['time', 'linear']};
        } else if (this.state.loadedCurrentChecked) {
            data = this.state.jvmClassLoadingLoadedCurrent;
            config = {
                x: 'timestamp', charts: [{type: 'line', y: 'loaded current'}], width: 800, height: 250,
                tickLabelColor: '#9c9898', axisLabelColor: '#9c9898'
            };
            metadata = {names: ['timestamp', 'loaded current'], types: ['time', 'linear']};
        } else if (this.state.unloadedTotalChecked) {
            data = this.state.jvmClassLoadingUnloadedTotal;
            config = {
                x: 'timestamp', charts: [{type: 'line', y: 'unloaded total'}], width: 800, height: 250,
                tickLabelColor: '#9c9898', axisLabelColor: '#9c9898'
            };
            metadata = {names: ['timestamp', 'unloaded total'], types: ['time', 'linear']};
        }
        else {
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
                            label="Loaded Total"
                            onCheck={(e, checked) => this.setState({loadedTotalChecked: checked})}
                            checked={this.state.loadedTotalChecked}
                            iconStyle={{fill: '#f17b31'}}
                        />
                    </div>
                    <div>
                        <Checkbox
                            label="Loaded Current"
                            onCheck={(e, checked) => this.setState({loadedCurrentChecked: checked})}
                            checked={this.state.loadedCurrentChecked}
                            iconStyle={{fill: '#f17b31'}}
                        />
                    </div>
                    <div>
                        <Checkbox
                            label="Unloaded Total"
                            onCheck={(e, checked) => this.setState({unloadedTotalChecked: checked})}
                            checked={this.state.unloadedTotalChecked}
                            iconStyle={{fill: '#f17b31'}}
                        />
                    </div>
                </div>
                <div style={{padding: 30}}>
                    <ChartCard data={data} metadata={metadata} config={config}
                               title="CPU Usage"/>
                </div>
            </div>
        );


    }
}