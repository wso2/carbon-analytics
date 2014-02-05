//Class c.protovis.LineChart2 : AreaChart2
//This is the custom wrapper class for protovis area/line charts

//Constructor
wso2vis.s.chart.protovis.LineChart2 = function(div, chartTitle, chartDesc) {
    wso2vis.s.chart.protovis.AreaChart2.call(this, div, chartTitle, chartDesc);
}

// this makes c.protovis.LineChart2.prototype inherit from ProtovisStakedAreaChart
wso2vis.extend(wso2vis.s.chart.protovis.LineChart2, wso2vis.s.chart.protovis.AreaChart2);

//Public function load
//Loads the chart inside the given HTML element
wso2vis.s.chart.protovis.LineChart2.prototype.load = function (w, h) {
    if ( w !== undefined ) {
        this.width(w);
    }
    if ( h !== undefined ) {
        this.height(h);
    }

    var thisObject = this;

    this.x = pv.Scale.linear(0, 4).range(0, this.width());
    this.y = pv.Scale.linear(0, 50).range(0, this.height()*0.9);
    this.customXticks = [];
 
    this.vis = new pv.Panel()
        .canvas(function() { return thisObject.divEl(); })
        .width(function() { return thisObject.width(); })
        .height(function() { return thisObject.height(); })
        .def("i", -1)
        .bottom(160)
        .top(0)
        .left(30)
        .right(60);

    var panel = this.vis.add(pv.Panel)
        .top(function() { return (thisObject.height() * (1 - thisObject.titleSpacing())); })
        .height(function() { return (thisObject.height() * thisObject.titleSpacing()); })
        .data(function() { return thisObject.getData(thisObject); });

    var line = panel.add(pv.Line)
        .data(function(a) { return a; })
        .left(function(d) { return thisObject.x(d.x); })
        .bottom(function(d) { return thisObject.y(d.y); })
        .lineWidth(3)
        .title(function() {
            var dataObj = thisObject.traverseToDataField(thisObject.data, thisObject.dataField());
            if( dataObj instanceof Array ) {
                return thisObject.onTooltip(dataObj[this.parent.index]);
            }
            else {
                return thisObject.onTooltip(dataObj);
            }
        })
        .event("click", function() {
            var dataObj = thisObject.traverseToDataField(thisObject.data, thisObject.dataField());
            if( dataObj instanceof Array ) {
                return thisObject.onClick(dataObj[this.parent.index]);
            }
            else {
                return thisObject.onClick(dataObj);
            }
        });
    
    var lineDot = line.add(pv.Dot).title(function(d) { return d.y; })
        .visible(function() { return thisObject.marks(); })
        .fillStyle(function() { return this.strokeStyle(); });
        //.add(pv.Label);

    line.add(pv.Dot).title(function(d) { return d.y; })
        .visible(function() { return thisObject.marks(); })
        .strokeStyle("#fff")
        .lineWidth(1);

    /* Legend */
    panel.add(pv.Dot)
        .visible(function() { return thisObject.legend(); })
        .right(150)
        .fillStyle(function() { return line.strokeStyle(); })
        .bottom(function() { return (this.parent.index * 15) + 10; })
        .size(20) 
        .lineWidth(1)
        .strokeStyle("#000")
      .anchor("right").add(pv.Label)
        .text(function() { return thisObject.getDataLabel(this.parent.index); });
     
    panel.add(pv.Rule)
        .data(function() { return thisObject.customXticks; /*thisObject.x.ticks()*/ })
        .visible(function(d) { return (d >= 0); })
        .left(function(d) { return (Math.round(thisObject.x(d)) - 0.5); })
        .strokeStyle("rgba(128,128,128,.1)")
      .add(pv.Rule)
        .bottom(-2)
        .height(5)
        .strokeStyle("rgba(128,128,128,1)")
      .anchor("bottom").add(pv.Label)
        .textMargin(5)
        .textBaseline("top")
        .textAlign("left")
        .textAngle(Math.PI / 3)
        /*.text(function(d) { return thisObject.formatDataLabel( thisObject.traverseToDataField(thisObject.traverseToDataField(thisObject.traverseToDataField(thisObject.data, thisObject.dataField())[this.parent.index], thisObject.subDataField())[this.index], thisObject.xLabel()) ) + thisObject.xSuffix(); })*/
        .text(function(d) { return thisObject.getXDataLabel(this.parent.index, this.index); })
        .font(function() { return thisObject.labelFont(); })
        .textStyle("rgba(128,128,128,0.5)");
     
    panel.add(pv.Rule)
        .data(function() { return thisObject.y.ticks(); })
        //.visible(function() { return !(this.index % 2); })
        .bottom(function(d) { return (Math.round(thisObject.y(d)) - 0.5); })
        .strokeStyle("rgba(128,128,128,.2)")
      .add(pv.Rule)
        .left(-5)
        .width(5)
        .strokeStyle("rgba(128,128,128,1)")
      .anchor("left").add(pv.Label)
        .textMargin(10)
        .text(function(d) {return d.toFixed() + thisObject.ySuffix(); })
        .font(function() { return thisObject.labelFont(); })
        .textStyle("rgba(128,128,128,0.5)");

    this.vis.add(pv.Label)
        .left(this.width() / 2)
        .visible(function() { return !(thisObject.title() === ""); })
        .top(16)
        .textAlign("center")
        .text(function() { return thisObject.title(); })
        .font(function() { return thisObject.titleFont(); });
};

wso2vis.s.chart.protovis.LineChart2.prototype.formatDataLabel = function (label) {

    return label;
};

wso2vis.s.chart.protovis.LineChart2.prototype.getXDataLabel = function (parentIndex, thisIndex) {

    var thisObject = this;

    var a = thisObject.getDataObject( thisObject.traverseToDataField( thisObject.data, thisObject.dataField() ), [parentIndex] );

    var b = thisObject.getDataObject( thisObject.traverseToDataField( a, thisObject.subDataField() ), [thisIndex] );

    var c = thisObject.traverseToDataField( b, thisObject.xLabel() );

    return thisObject.formatDataLabel( c ) + thisObject.xSuffix();
};

