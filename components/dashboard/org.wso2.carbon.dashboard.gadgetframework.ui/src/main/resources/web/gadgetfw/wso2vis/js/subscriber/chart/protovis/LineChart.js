//Class c.protovis.LineChart : AreaChart
//This is the custom wrapper class for protovis area/line charts

//Constructor
wso2vis.s.chart.protovis.LineChart = function(div, chartTitle, chartDesc) {
    wso2vis.s.chart.protovis.AreaChart.call(this, div, chartTitle, chartDesc);
}

// this makes c.protovis.LineChart.prototype inherit from ProtovisStakedAreaChart
wso2vis.extend(wso2vis.s.chart.protovis.LineChart, wso2vis.s.chart.protovis.AreaChart);

//Public function load
//Loads the chart inside the given HTML element
wso2vis.s.chart.protovis.LineChart.prototype.load = function (w, h, band) {
    if ( w !== undefined ) {
        this.width(w);
    }
    if ( h !== undefined ) {
        this.height(h);
    }
    if ( band !== undefined ) {
        this.band(band);
    }

    var thisObject = this;

    this.x = pv.Scale.linear(0, this.band).range(0, this.width());
    this.y = pv.Scale.linear(0, 50).range(0, this.height()*0.9);
 
    this.vis = new pv.Panel()
        .canvas(function() { return thisObject.divEl(); })
        .width(function() { return thisObject.width(); })
        .height(function() { return thisObject.height(); })
        .def("i", -1)
        .bottom(20)
        .top(0)
        .left(30)
        .right(10);

    var panel = this.vis.add(pv.Panel)
        .top(function() { return (thisObject.height() * (1 - thisObject.titleSpacing())); })
        .height(function() { return (thisObject.height() * thisObject.titleSpacing()); })
        .data(function() { return thisObject.getData(thisObject); });

    var line = panel.add(pv.Line)
        .data(function(a) { return a; })
        //.left(function(d) { return thisObject.x(this.index); })
		.left(function(d) 
			  {
				  if (thisObject.dirFromLeft())
				  	 return thisObject.x(this.index);
			      return thisObject.x((thisObject.dataHistory[this.parent.index].length <= thisObject.band()) ? (thisObject.band() - thisObject.dataHistory[this.parent.index].length) + this.index + 1: this.index); 
			  })
        .bottom(function(d) { return thisObject.y(d); })
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
    
    var lineDot = line.add(pv.Dot).title(function(d) { return d; })
        .visible(function() { return thisObject.marks(); })
        .fillStyle(function() { return this.strokeStyle(); });
        //.add(pv.Label);

    line.add(pv.Dot).title(function(d) { return d; })
        .visible(function() { return thisObject.marks(); })
        .strokeStyle("#fff")
        .lineWidth(1);

    /* Legend */
    panel.add(pv.Dot)
        .visible(function() { return thisObject.legend(); })
        .right(100)
        .fillStyle(function() { return line.strokeStyle(); })
        .bottom(function() { return (this.parent.index * 15) + 10; })
        .size(20) 
        .lineWidth(1)
        .strokeStyle("#000")
      .anchor("right").add(pv.Label)
        .text(function() { return thisObject.getDataLabel(this.parent.index); });
     
    panel.add(pv.Rule)
        .data(function() { return thisObject.x.ticks(); })
        //.visible(function(d) { return (d > 0); })
        .left(function(d) { return (Math.round(thisObject.x(d)) - 0.5); })
        .strokeStyle("rgba(128,128,128,.1)")
      .add(pv.Rule)
        .bottom(-2)
        .height(5)
        .strokeStyle("rgba(128,128,128,1)")
      .anchor("bottom").add(pv.Label)
        .textMargin(10)
        .text(function(d) { var n = new Number(((thisObject.dirFromLeft())? d : thisObject.band() - d) * thisObject.xInterval() / 1000); return n.toFixed() + thisObject.xSuffix(); })
        .font(function() { return thisObject.labelFont(); })
        .textStyle("rgba(128,128,128,0.5)");
     
    panel.add(pv.Rule)
        .data(function() { return thisObject.y.ticks(); })
        //.visible(function() { return !(this.index % 2); })
        .bottom(function(d) { return (Math.round(thisObject.y(d)) - 0.5); })
        .strokeStyle("rgba(128,128,128,.2)")
      .add(pv.Rule)
        .left(-5) //TODO right(5)
        .width(5)
        .strokeStyle("rgba(128,128,128,1)")
      .anchor("left").add(pv.Label) //TODO right
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

