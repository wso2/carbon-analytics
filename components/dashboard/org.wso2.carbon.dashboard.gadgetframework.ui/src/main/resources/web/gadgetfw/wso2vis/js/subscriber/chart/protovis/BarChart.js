//Class c.protovis.BarChart : Chart
//This is the custom wrapper class for protovis bar charts

//Constructor
wso2vis.s.chart.protovis.BarChart = function(canvas, chartTitle, chartDesc) {
    wso2vis.s.chart.Chart.call(this, canvas, chartTitle, chartDesc);

    /* @private */
    this.vis = null;
    this.y = null;
    this.x = null;

    this.legendText("Data 1");
}

// this makes c.protovis.BarChart.prototype inherits
// from Chart.prototype
wso2vis.extend(wso2vis.s.chart.protovis.BarChart, wso2vis.s.chart.Chart);

wso2vis.s.chart.protovis.BarChart.prototype
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
    .property("segmentBorderColor");

//Public function load
//Loads the chart inside the given HTML element
wso2vis.s.chart.protovis.BarChart.prototype.load = function (w, h) {
    if ( w !== undefined ) {
        this.width(w);
    }
    if ( h !== undefined ) {
        this.height(h);
    }

    var thisObject = this;

    this.x = pv.Scale.linear(0, 1).range(0, this.width());
    this.y = pv.Scale.ordinal(pv.range(3)).splitBanded(0, this.height(), 4/5);
 
    this.vis = new pv.Panel()
        .canvas(function() { return thisObject.divEl(); })
        .width(function() { return thisObject.width(); })
        .height(function() { return thisObject.height(); });

    var chart = this.vis.add(pv.Panel)
        .width(function() { return (thisObject.width() - thisObject.paddingLeft() - thisObject.paddingRight()); })
        .height(function() { return (thisObject.height() - thisObject.paddingTop() - thisObject.paddingBottom()); })
        .top(thisObject.paddingTop())
        .bottom(thisObject.paddingBottom())
        .left(thisObject.paddingLeft())
        .right(thisObject.paddingRight());

    /* Draw Bars */
    var bar = chart.add(pv.Bar)
        .data(function() { return thisObject.getData(thisObject); })
        .top(function() { return thisObject.y(this.index); })
        .height(function() { return thisObject.y.range().band; })
        .width(thisObject.x)
        .left(0)
        //.strokeStyle("rgba(15, 55, 90, .9)")
        .fillStyle("rgba(31, 119, 180, 1)")
        .title(function() {
            var dataObj = thisObject.traverseToDataField(thisObject.data, thisObject.dataField());
            if( dataObj instanceof Array ) {
                return thisObject.onTooltip(dataObj[this.index]);
            }
            else {
                return thisObject.onTooltip(dataObj);
            }
        })
        .event("click", function() {
            var dataObj = thisObject.traverseToDataField(thisObject.data, thisObject.dataField());
            if( dataObj instanceof Array ) {
                return thisObject.onClick(dataObj[this.index]);
            }
            else {
                return thisObject.onClick(dataObj);
            }
        });
    
    /* marks */
    bar.anchor("right").add(pv.Label)
        .visible(function() { return thisObject.marks(); })
        .textStyle("white")
        .textMargin(5)
        .text(function(d) { return d; });

    /* legend */
    chart.add(pv.Dot)
        .data(function() { return [thisObject.legendText()]; })
        .visible(function() { return thisObject.legend(); })
        .left(function() { return thisObject.legendX(); })
        .top(function() { return thisObject.legendY(); })
        .fillStyle(function() { return bar.fillStyle(); })
        .size(20)
        .lineWidth(1)
        .strokeStyle("#000")
      .anchor("right").add(pv.Label);

    bar.anchor("left").add(pv.Label)
        .textMargin(5)
        .textAlign("right")
        .text(function() { return thisObject.getDataLabel(this.index); })
        .font(function() { return thisObject.labelFont(); })
        .textStyle("rgb(0,0,0)");
     
    chart.add(pv.Rule)
        .data(function() { return thisObject.x.ticks(); })
        .left(function(d) { return (Math.round(thisObject.x(d)) - 0.5); })
        .strokeStyle(function(d) { return (d ? "rgba(128,128,128,.3)" : "rgba(128,128,128,.8)"); })
      .add(pv.Rule)
        .bottom(0)
        .height(5)
        .strokeStyle("rgba(128,128,128,1)")
      .anchor("bottom").add(pv.Label)
        .text(function(d) { return d.toFixed(); })
        .font(function() { return thisObject.labelFont(); })
        .textStyle("rgb(0,0,0)");

    this.vis.add(pv.Label)
        .left(this.width() / 2)
        .visible(function() { return !(thisObject.title() === ""); })
        .top(16)
        .textAlign("center")
        .text(function() { return thisObject.title(); })
        .font(function() { return thisObject.titleFont(); });
};

/**
* @private
*/
wso2vis.s.chart.protovis.BarChart.prototype.titleSpacing = function () {
    if(this.title() === "") {
        return 1;
    }
    else {
        return 0.9;
    }
};

/**
* @private
*/
wso2vis.s.chart.protovis.BarChart.prototype.populateData = function (thisObject) {
    var _dataField = thisObject.traverseToDataField(thisObject.data, thisObject.dataField());

    var dataGrpCount = 1;
    if( _dataField instanceof Array ) {
        dataGrpCount = _dataField.length;
    }

    thisObject.formattedData = pv.range(dataGrpCount).map( genDataMap );

    
    var maxVal = thisObject.formattedData.max();
    if (maxVal < 5) maxVal = 5; // fixing value repeating issue.

    thisObject.x.domain(0, maxVal).range(0, (thisObject.width() - thisObject.paddingLeft() - thisObject.paddingRight()) );
    thisObject.y.domain(pv.range(dataGrpCount)).splitBanded(0, (thisObject.height() - thisObject.paddingTop() - thisObject.paddingBottom()), 4/5);

    function genDataMap(x) {
        var rootObj;
        if( _dataField instanceof Array ) {
            rootObj = _dataField[x];
        }
        else {
            rootObj = _dataField;
        }
        return parseInt(thisObject.traverseToDataField(rootObj, thisObject.dataValue()));
    }
};

wso2vis.s.chart.protovis.BarChart.prototype.getData = function (thisObject) {
    return thisObject.formattedData;
};

wso2vis.s.chart.protovis.BarChart.prototype.update = function () {
    this.populateData(this);
    this.vis.render();
    if(this.tooltip() === true) {
        tooltip.init();
    }
};

wso2vis.s.chart.protovis.BarChart.prototype.getDataLabel = function (i) {
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

