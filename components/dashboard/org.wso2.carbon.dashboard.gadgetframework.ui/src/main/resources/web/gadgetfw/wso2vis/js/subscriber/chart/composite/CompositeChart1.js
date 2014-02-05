//Class s.chart.composite.CompositeChart1 : Chart
//This is the custom wrapper class for protovis bar charts

//Constructor
wso2vis.s.chart.composite.CompositeChart1 = function(canvas, chartTitle, chartDesc) {
    wso2vis.s.chart.Chart.call(this, canvas, chartTitle, chartDesc);

    /* @private */
    this.vis = null;
    this.y = null;
    this.x = null;
	this.chart = null;
	this.chartType(0);
	

    //this.legendText("Data 1");
}

// this makes s.chart.composite.CompositeChart1.prototype inherits
// from Chart.prototype
wso2vis.extend(wso2vis.s.chart.composite.CompositeChart1, wso2vis.s.chart.Chart);

wso2vis.s.chart.composite.CompositeChart1.prototype
    .property("dataField")
    .property("dataValue")
    .property("dataLabel")
    .property("ySuffix")
    .property("xSuffix")
    .property("titleTop")
    .property("titleLeft")
    .property("titleRight")
    .property("titleBottom")
    .property("xTitle")
    .property("yTitle")
    .property("legendText")
    .property("segmentBorderColor")
	.property("labelLength")
    .property("thickness")
	.property("chartType");
	
wso2vis.s.chart.composite.CompositeChart1.prototype.load = function (w, h) {
    if (this.chartType() == 0) {
		this.chart = new wso2vis.s.chart.protovis.BarChart(this.divEl(), this.title(), this.description());
	}
	else if (this.chartType() == 1) {
		this.chart = new wso2vis.s.chart.protovis.ColumnChart(this.divEl(), this.title(), this.description());
	}
	else if (this.chartType() == 2) {
		this.chart = new wso2vis.s.chart.protovis.WedgeChart(this.divEl(), this.title(), this.description());
	}
	else if (this.chartType() == 3) {
		this.chart = new wso2vis.s.chart.protovis.PieChart(this.divEl(), this.title(), this.description());
	}
	else if (this.chartType() == 4) {
		this.chart = new wso2vis.s.chart.protovis.LineChart2(this.divEl(), this.title(), this.description());
	}	
	else if (this.chartType() == 5) {
		this.chart = new wso2vis.s.chart.protovis.AreaChart2(this.divEl(), this.title(), this.description());
	}	
	
	
	this.chart.dataField(this.dataField());
	this.chart.dataValue(this.dataValue());
	this.chart.dataLabel(this.dataLabel());
	this.chart.ySuffix(this.ySuffix());
	this.chart.xSuffix(this.xSuffix());
	this.chart.tooltip(this.tooltip());
	this.chart.legend(this.legend());
	this.chart.marks(this.marks());
	this.chart.width(this.width());
	this.chart.height(this.height());
	this.chart.titleFont(this.titleFont());
	this.chart.labelFont(this.labelFont());
	this.chart.legendX(this.legendX());
	this.chart.legendY(this.legendY());
	this.chart.paddingTop(this.paddingTop());
	this.chart.paddingLeft(this.paddingLeft());
	this.chart.paddingRight(this.paddingRight());
	this.chart.paddingBottom(this.paddingBottom());

	this.chart.load(w, h);
};

/**
* @private
*/
wso2vis.s.chart.composite.CompositeChart1.prototype.populateData = function (thisObject) {    
	this.chart.populateData(thisObject.chart);
};

wso2vis.s.chart.composite.CompositeChart1.prototype.update = function () {
    this.chart.update();
};

wso2vis.s.chart.composite.CompositeChart1.prototype.getDataLabel = function (i) {
    if (this.data !== null){
        var rootObj = this.traverseToDataField(this.data, this.dataField());
        if( rootObj instanceof Array ) {
            return  this.traverseToDataField(rootObj[i], this.dataLabel());
        }
        else {
            return  this.traverseToDataField(rootObj, this.dataLabel());
        }
    }
    
    return i;
};

wso2vis.s.chart.composite.CompositeChart1.prototype.pushData = function (d) {
    if( this.validateData(d) ){
        this.chart.data = d;
        this.chart.update();
    } else {
        this.updateMessageDiv(this.messageInterceptFunction());
    }
};
