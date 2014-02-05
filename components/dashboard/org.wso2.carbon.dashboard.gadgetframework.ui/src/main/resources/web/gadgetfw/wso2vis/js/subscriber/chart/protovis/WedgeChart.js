//@class wso2vis.s.chart.protovis.WedgeChart : wso2vis.s.chart.Chart
//This is the custom wrapper class for axiis bar charts

//Constructor
wso2vis.s.chart.protovis.WedgeChart = function(canvas, chartTitle, chartDesc) {
    wso2vis.s.chart.Chart.call(this, canvas, chartTitle, chartDesc);

    this.labelLength(12)
        .thickness(30);

    /* @private */
    this.vis = null;
}

// this makes c.protovis.WedgeChart.prototype inherits from wso2vis.s.chart.Chart
wso2vis.extend(wso2vis.s.chart.protovis.WedgeChart, wso2vis.s.chart.Chart);

wso2vis.s.chart.protovis.WedgeChart.prototype
    .property("dataField")
    .property("dataValue")
    .property("dataLabel")
    .property("labelLength")
    .property("thickness");

//Public function load
//Loads the chart inside the given HTML element
wso2vis.s.chart.protovis.WedgeChart.prototype.load = function (w) {
    if ( w !== undefined ) {
        this.width(w);
    }
    /*if ( h !== undefined ) { //not using height for the Wedge
        this.height(h);
    }*/
    //var r = this.width() / 2.5;

    var thisObject = this;
    
    this.vis = new pv.Panel()
        .canvas(function() { return thisObject.divEl(); })
        .width(function() { return thisObject.width(); })
        .height(function() { return thisObject.height(); })
        .overflow('hidden');

    var chart = this.vis.add(pv.Panel)
        .width(function() { return (thisObject.width() - thisObject.paddingLeft() - thisObject.paddingRight()); })
        .height(function() { return (thisObject.height() - thisObject.paddingTop() - thisObject.paddingBottom()); })
        .top(thisObject.paddingTop())
        .bottom(thisObject.paddingBottom())
        .left(thisObject.paddingLeft())
        .right(thisObject.paddingRight());
    
    var wedge = chart.add(pv.Wedge)
        .data(function() { return pv.normalize(thisObject.getData(thisObject)); })
        .left((thisObject.width() - thisObject.paddingLeft() - thisObject.paddingRight()) / 2)
        .bottom((thisObject.height() - thisObject.paddingTop() - thisObject.paddingBottom()) / 2)
        .innerRadius(function() { return (((thisObject.width() - thisObject.paddingLeft() - thisObject.paddingRight()) / 2.5) - thisObject.thickness()); })
        .outerRadius(function() { return (thisObject.width() - thisObject.paddingLeft() - thisObject.paddingRight()) / 2.5; })
        .angle(function(d) { return (d * 2 * Math.PI); })
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

    wedge.anchor("outer").add(pv.Label)
        .visible(function(d) { return (d > 0.05); })
        .textMargin(function(){ return thisObject.thickness() + 5; })
        .text(function(d) { var lbl=thisObject.getDataLabel(this.index); return (lbl.length > thisObject.labelLength() ? lbl.substring(0,thisObject.labelLength())+"..." : lbl); })
        .font(function() { return thisObject.labelFont(); })
        .textStyle(function() { return wedge.fillStyle(); });

    wedge.anchor("center").add(pv.Label)
         .visible(function(d) { return (thisObject.marks() && (d > 0.10)); })
         .textAngle(0)
         .text(function(d) { return (d*100).toFixed() + "%"; })
         .textStyle("#fff");

    /* Legend */
/*    var legend = chart.add(pv.Panel)
        .top(function() { return thisObject.legendTop(); })
        .left(function() { return thisObject.legendLeft(); })
        .right(function() { return thisObject.legendRight(); })
        .bottom(function() { return thisObject.legendBottom(); })*/
        

    chart.add(pv.Dot)
        .data(function() { return pv.normalize(thisObject.getData(thisObject)); })
        .visible(function() { return thisObject.legend(); })
        .fillStyle(function() { return wedge.fillStyle(); })
        .right(function() { return (thisObject.width() - thisObject.legendX()); })
        .bottom(function() { return (this.index * 15) + (thisObject.height() - thisObject.legendY()); })
        .size(20) 
        .lineWidth(1)
        .strokeStyle("#000")
      .anchor("right").add(pv.Label)
        .text(function() { var lbl=thisObject.getDataLabel(this.index); return (lbl.length > thisObject.labelLength() ? lbl.substring(0,thisObject.labelLength())+"..." : lbl); });

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
wso2vis.s.chart.protovis.WedgeChart.prototype.titleSpacing = function () {
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
wso2vis.s.chart.protovis.WedgeChart.prototype.populateData = function (thisObject) {

    var _dataField = thisObject.traverseToDataField(thisObject.data, thisObject.dataField());
    var dataGrpCount = 1;

    if( _dataField instanceof Array ) {
        dataGrpCount = _dataField.length;
    }

    this.formattedData = pv.range(dataGrpCount).map( genDataMap );

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

wso2vis.s.chart.protovis.WedgeChart.prototype.getData = function (thisObject) {

    return thisObject.formattedData;
};

wso2vis.s.chart.protovis.WedgeChart.prototype.update = function () {

    this.populateData(this);
    this.vis.render();
    if(this.tooltip() === true) {
        tooltip.init();
    }
};

wso2vis.s.chart.protovis.WedgeChart.prototype.getDataLabel = function (i) {

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

