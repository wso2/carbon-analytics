//Class c.protovis.ColumnChart : Chart
//This is the custom wrapper class for protovis column charts

//Constructor
wso2vis.s.chart.protovis.ColumnChart = function(canvas, chartTitle, chartDesc) {
    wso2vis.s.chart.Chart.call(this, canvas, chartTitle, chartDesc);

    this.ySuffix("")
        .xSuffix("");

    /* @private */
    this.vis = null;
    this.y = null;
    this.x = null;
}

// this makes c.protovis.ColumnChart.prototype inherits
// from Chart.prototype
wso2vis.extend(wso2vis.s.chart.protovis.ColumnChart, wso2vis.s.chart.Chart);

wso2vis.s.chart.protovis.ColumnChart.prototype
    .property("dataField")
    .property("dataValue")
    .property("dataLabel")
    .property("ySuffix")
    .property("xSuffix");

//Public function load
//Loads the chart inside the given HTML element
wso2vis.s.chart.protovis.ColumnChart.prototype.load = function (w, h) {
    if ( w !== undefined ) {
        this.width(w);
    }
    if ( h !== undefined ) {
        this.height(h);
    }

    var n = 3;
    var thisObject = this;

    this.y = pv.Scale.linear(0, 1).range(0, this.height());
    this.x = pv.Scale.ordinal(pv.range(n)).splitBanded(0, this.width(), 4/5);
 
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
     
    var bar = chart.add(pv.Bar)
        .data(function() { return thisObject.getData(thisObject); })
        .left(function() { return thisObject.x(this.index); })
        .width(function() { return thisObject.x.range().band; })
        .bottom(0)
        .height(thisObject.y)
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
     
    bar.anchor("top").add(pv.Label)
        .visible(function() { return thisObject.marks(); })
        .textStyle("white")
        .textMargin(5)
        .text(function(d) { return d; });

    bar.anchor("bottom").add(pv.Label)
        .textMargin(10)
        .textBaseline("top")
        .textAngle(Math.PI / 2)
        .textAlign("left")
        .text(function() { return thisObject.getDataLabel(this.index); })
        .font(function() { return thisObject.labelFont(); });
        /*.add(pv.Bar).fillStyle("rgba(128,128,128,0.1)").height(6);*/
     
    chart.add(pv.Rule)
        .data(function() { return thisObject.y.ticks(); })
        .bottom(function(d) { return (Math.round(thisObject.y(d)) - 0.5); })
        .strokeStyle(function(d) { return (d ? "rgba(128,128,128,.5)" : "#000"); })
      .add(pv.Rule)
        .left(0)
        .width(5)
        .strokeStyle("rgba(128,128,128,1)")
      .anchor("left").add(pv.Label)
        .text(function(d) { return d.toFixed(); })
        .font(function() { return thisObject.labelFont(); });

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
wso2vis.s.chart.protovis.ColumnChart.prototype.titleSpacing = function () {
    if(this.title() === "") {
        return 1;
    }
    else {
        return 0.9;
    }
};

wso2vis.s.chart.protovis.ColumnChart.prototype.populateData = function (thisObject) {
    var _dataField = thisObject.traverseToDataField(thisObject.data, thisObject.dataField());

    var dataGrpCount = 1;
    if( _dataField instanceof Array ) {
        dataGrpCount = _dataField.length;
    }

    thisObject.formattedData = pv.range(dataGrpCount).map( genDataMap );

    
    var maxVal = thisObject.formattedData.max() + 5; //to make sure the bars are inside the rule
    if (maxVal < 5) maxVal = 5; // fixing value repeating issue.

    this.y.domain(0, maxVal).range(0, (thisObject.height() - thisObject.paddingTop() - thisObject.paddingBottom()));
    this.x.domain(pv.range(dataGrpCount)).splitBanded(0, (thisObject.width() - thisObject.paddingLeft() - thisObject.paddingRight()), 4/5);
    
    var maxLabelLength = (maxVal == 0) ? 1 : Math.floor(Math.log(maxVal)/Math.log(10)) + 1; //TODO: maxheight will never become 0. But we check it just to be in the safe side. useless?
    this.vis.left((maxLabelLength*9.5)+5);

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

wso2vis.s.chart.protovis.ColumnChart.prototype.getData = function (thisObject) {
    return thisObject.formattedData;
};

wso2vis.s.chart.protovis.ColumnChart.prototype.update = function () {
    this.populateData(this);
    this.vis.render();
    if(this.tooltip() === true) {
        tooltip.init();
    }
};

wso2vis.s.chart.protovis.ColumnChart.prototype.getDataLabel = function (i) {
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

