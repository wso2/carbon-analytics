import React from 'react';
import BasicChart from './BasicChart.jsx';
import { VictoryLine, VictoryArea, VictoryGroup,  VictoryBar,  VictoryTooltip, VictoryStack } from 'victory';

export default class InlineChart extends BasicChart {

    constructor(props){
        super(props);

        this.handleAndSortData=this.handleAndSortData.bind(this);
    }


    componentDidMount(){
        this.handleAndSortData(this.props);
        console.info(this.state);
    }


    componentWillReceiveProps(nextProps){
        this.handleAndSortData(nextProps);
    }

    render(){

        let { config } = this.props;
        let { height, width, chartArray, dataSets} = this.state;
        let chartComponents = [];
        let legendItems = [];
        let horizontal = false;
        let lineCharts = [];
        let areaCharts = [];
        let barCharts = [];

        chartArray.map((chart, chartIndex) => {
            switch (chart.type) {
                case 'spark-line':
                    Object.keys(chart.dataSetNames).map((dataSetName) => {
                        legendItems.push({ name: dataSetName, symbol: { fill: chart.dataSetNames[dataSetName] } });
                        lineCharts.push(
                            <VictoryGroup
                                key={`chart-${chartIndex}-${chart.type}-${dataSetName}`}
                                data={dataSets[dataSetName]}
                                color={chart.dataSetNames[dataSetName]}
                                height={height}
                                width={width}
                                padding={0}
                                style={{ data: { strokeWidth: 0.5 } }}

                            >
                                <VictoryLine />
                                {/* <VictoryPortal>
                                    <VictoryScatter
                                        labels={(d) => `${config.x}:${d.x}\n${config.charts[chartIndex].y}:${d.y}`}
                                        labelComponent={
                                            <VictoryTooltip
                                                orientation='bottom'
                                            />
                                        }
                                        size={(d, a) => {
                                            return a ? 20 : 6;
                                        }}
                                    />
                                </VictoryPortal> */}
                            </VictoryGroup>
                        );
                    });
                    break;
                case 'spark-area': {
                    let areaLocal = [];
                    Object.keys(chart.dataSetNames).map((dataSetName) => {
                        legendItems.push({ name: dataSetName, symbol: { fill: chart.dataSetNames[dataSetName] } });

                        areaLocal.push(
                            <VictoryGroup
                                key={`chart-${chartIndex}-${chart.type}-${dataSetName}`}
                                data={dataSets[dataSetName]}
                                color={chart.dataSetNames[dataSetName]}
                                style={{ data: { fillOpacity: 0.5, strokeWidth: 0.5 } }}
                                height={height}
                                width={width}
                                padding={0}

                            >
                                <VictoryArea />
                                {/* <VictoryPortal>
                                    <VictoryScatter
                                        labels={(d) => `${config.x}:${d.x}\n${config.charts[chartIndex].y}:${d.y}`}
                                        labelComponent={
                                            <VictoryTooltip
                                                orientation='bottom'
                                            />
                                        }
                                        size={(d, a) => {
                                            return a ? 20 : 6;
                                        }}
                                    />
                                </VictoryPortal> */}
                            </VictoryGroup>
                        );
                    });

                    if (chart.mode === 'stacked') {
                        areaCharts.push(
                            <VictoryStack
                                height={height}
                                width={width}
                                padding={0}
                            >
                                {areaLocal}
                            </VictoryStack>
                        );
                    } else {
                        areaCharts = areaCharts.concat(areaLocal);

                    }

                    break;
                }
                case 'spark-bar': {
                    let localBar = [];

                    horizontal = horizontal ? horizontal : chart.orientation === 'left';

                    Object.keys(chart.dataSetNames).map((dataSetName) => {
                        legendItems.push({ name: dataSetName, symbol: { fill: chart.dataSetNames[dataSetName] } });
                        localBar.push(
                            <VictoryBar
                                labels={(d) => `${config.x}:${d.x}\n${config.charts[chartIndex].y}:${d.y}`}
                                labelComponent={
                                    <VictoryTooltip
                                        orientation='bottom'
                                    />
                                }
                                data={dataSets[dataSetName]}
                                color={chart.dataSetNames[dataSetName]}
                                height={height}
                                width={width}
                                padding={0}
                            />
                        );

                    });

                    if (chart.mode === 'stacked') {
                        barCharts.push(
                            <VictoryStack
                                height={height}
                                width={width}
                                padding={0}
                            >
                                {localBar}
                            </VictoryStack>
                        );
                    } else {
                        barCharts = barCharts.concat(localBar);
                    }


                    break;
                }
            }
        });


        if (areaCharts.length > 0) chartComponents = chartComponents.concat(areaCharts);
        if (lineCharts.length > 0) chartComponents = chartComponents.concat(lineCharts);
        if (barCharts.length > 0) {

            let barWidth = (horizontal ? height : width) / (config.maxLength * (barCharts.length > 1 ? barCharts.length : 2)) - 3;

            chartComponents.push(
                <VictoryGroup
                    horizontal={horizontal}
                    offset={barWidth}
                    style={{ data: { width: barWidth } }}
                    height={height}
                    width={width}
                    padding={5}
                >
                    {barCharts}
                </VictoryGroup>
            );
        }

        console.info(chartComponents);


        return (
            <div>{chartComponents}</div>
        );

    }

}