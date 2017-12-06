import React from "react";
//App Components
import DashboardUtils from "../utils/DashboardUtils";
import ChartCard from "../common/ChartCard";
//Material UI
import {TableHeader, TableHeaderColumn, TableRow, Table, TableRowColumn, TableBody, Checkbox} from "material-ui";

const swapMetadata = {names: ['timestamp', 'free swap size', 'total swap size'], types: ['time', 'linear', 'linear']};

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
            totalSizeChecked: true,
            tickCount: 20
        };
    }

    componentWillReceiveProps(nextprops) {
        this.setState({
            freeSize: nextprops.data[0],
            totalSize: nextprops.data[1],
            tickCount: nextprops.data[0].length>20 ? 20 : nextprops.data[0].length
        });
    }

    render() {
        const swapLineChartConfig = {
            x: 'timestamp',
            charts: [{type: 'line', y: 'free swap size', fill: '#f17b31',style: {markRadius: 2}}, {type: 'line', y: 'total' +
            ' swap size',style: {markRadius: 2}}],
            width: 700,
            height: 200,
            style: {
                tickLabelColor:'#f2f2f2',
                legendTextColor: '#9c9898',
                legendTitleColor: '#9c9898',
                axisLabelColor: '#9c9898'
            },
            legend:true,
            interactiveLegend: true,
            gridColor: '#f2f2f2',
            xAxisTickCount:this.state.tickCount
        };
        if(this.state.freeSize.length === 0 && this.state.totalSize.length === 0){
            return(
                <div style={{paddingLeft: 10}}>
                    <Card>
                        <CardHeader
                            title="JVM Swap Space"
                        />
                        <Divider/>
                        <CardMedia>
                            <div style={{backgroundColor: '#131313'}}>
                                <h4 style={{marginTop: 0}}>No Data Available</h4>
                            </div>
                        </CardMedia>
                    </Card>
                </div>
            );
        }
        return (
            <div style={{paddingLeft: 10}}>
                <ChartCard data={DashboardUtils.getCombinedChartList(this.state.freeSize, this.state.totalSize)}
                           metadata={swapMetadata} config={swapLineChartConfig} title="JVM Swap Space"/>
            </div>
        );
    }
}