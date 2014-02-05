wso2vis.s.chart.raphael.PieChart = function(canvas, chartTitle, chartDesc) {
    wso2vis.s.chart.Chart.call(this, canvas, chartTitle, chartDesc);
    
    this.colscheme(20)
        .width(300)
        .height(300)
        .showPercent(true)
        .showValue(true)
        .padding(5)
        .fontFamily('Fontin-Sans, Arial')
        .fontSize('10px')
        .raphaelPaper(null)
		.left(0)
		.top(0);
		
	this.chart = null;
}

// inherits from Chart
wso2vis.extend(wso2vis.s.chart.raphael.PieChart, wso2vis.s.chart.Chart);

wso2vis.s.chart.raphael.PieChart.prototype
    .property("colscheme")
    .property("dataField")
    .property("dataValue")
    .property("dataLabel")
    .property("showPercent")
    .property("showValue")
    .property("padding")
    .property("fontFamily")
    .property("fontSize")
    .property("raphaelPaper")
	.property("left")
	.property("top");
    

wso2vis.s.chart.raphael.PieChart.prototype.load = function (w) {
    if (w !== undefined) {
        this.width(w);
    }
    
    if (this.raphaelPaper() == null) {
        this.raphaelPaper(Raphael(this.divEl(), this.width(), this.width())); //Create a new Raphael paper.
    }
	return this; 
};

wso2vis.s.chart.raphael.PieChart.prototype.update = function () {
    this.convertData(this);
    var parent = this;     
    
    var colors = wso2vis.util.generateColors(this.formattedData.length, this.colscheme());
    
    var is_label_visible = false,
        leave_timer;
    
    var currY = 0;
    var df = this.traverseToDataField(this.data, this.dataField());    
    if (df instanceof Array) {
        df = df;
    }
    else {
        df = [df];
    }
    
    var first;
    
    var paper = this.raphaelPaper(),
        rad = Math.PI / 180,
        cx = this.left() + this.width()/2,
        cy = this.top() + this.width()/2,
        radius = (this.width()/2) - this.padding(),
        data = this.formattedData;
		
	if (this.chart != null) 
	{
		var item1 = this.chart.pop(); 
		
		while (item1 != null) {
			item1.remove();
			item1 = this.chart.pop(); 
		}
	}

	this.chart = paper.set();
        
    function sector(cx, cy, radius, startAngle, endAngle, params) {
        var x1 = cx + radius * Math.cos(-startAngle * rad),
            x2 = cx + radius * Math.cos(-endAngle * rad),
            y1 = cy + radius * Math.sin(-startAngle * rad),
            y2 = cy + radius * Math.sin(-endAngle * rad);
        return paper.path(["M", cx, cy, "L", x1, y1, "A", radius, radius, 0, +(endAngle - startAngle > 180), 0, x2, y2, "z"]).attr(params);
    }
    
	var thischart = this.chart;
	
    var angle = 0,
        total = 0,
        start = 0,
        process = function (j) {
            var value = data[j]["value"],
                perc = Math.round((value / total) * 100),
                angleplus = 360 * (value / total),
                popangle = angle + (angleplus / 2),
                color = "hsb(" + start + ", 1, 0.8)",
                ms = 500,
                delta = 30,
                bcolor = colors[j],
                p = sector(cx, cy, radius, angle, angle + angleplus, {fill: bcolor, stroke: "none", "stroke-width": 1}),
                opac = 1,
                labelValue = (parent.showValue()) ? data[j]["value"] : "",
                labelPercent = (parent.showPercent()) ? " ("+perc+"%)" : "",
                txt = paper.text(cx + (radius - delta) * Math.cos(-popangle * rad), cy + (radius - delta) * Math.sin(-popangle * rad), labelValue + labelPercent ).attr({fill: "#fff", stroke: "none", opacity: opac, "font-family": parent.fontFamily(), "font-size": parent.fontSize()});
            angle += angleplus;
            thischart.push(p);
            thischart.push(txt);
            start += .1;
            
            if (parent.tooltip()) {
                (function (data, lbl, func, org) {
                    $(p.node).hover(function (e) {
                        clearTimeout(leave_timer);
                        var tip = func({label:lbl, value:data, total:total, first:first, raw:org});
                        wso2vis.environment.tooltip.show(e.pageX, e.pageY, tip);
                        is_label_visible = true;
                    }, function () {
                        leave_timer = setTimeout(function () {
                            wso2vis.environment.tooltip.hide();
                            is_label_visible = false;
                        }, 2);
                    });
                })(parent.formattedData[i]["value"], parent.formattedData[i]["label"], parent.onTooltip, df[i]);
                
                (function (data, lbl, func, org) {
                    $(p.node).mousemove(function (e) {
                        if (is_label_visible) {
                            clearTimeout(leave_timer);
                            var tip = func({label:lbl, value:data, total:total, first:first, raw:org});
                            wso2vis.environment.tooltip.show(e.pageX, e.pageY, tip);
                        }
                    });
                })(parent.formattedData[i]["value"], parent.formattedData[i]["label"], parent.onTooltip, df[i]);
            }
        };
        
    for (var i = 0, ii = data.length; i < ii; i++) {
        total += data[i]["value"];
    }
    
    for (var i = 0; i < ii; i++) {
        process(i);
    }
};

wso2vis.s.chart.raphael.PieChart.prototype.convertData = function (that) {
    var df = that.traverseToDataField(that.data, that.dataField());    
    var dcount = 1;
    if (df instanceof Array) {
        dcount = df.length;
    }
    
    that.formattedData = [];
    
    for (var i = 0; i < dcount; i++) {
        that.formattedData.push({"label":getLbl(i), "value":getVal(i)});
    }
    
    function getVal(x) {
        var r;
        if (df instanceof Array) {
            r = df[x];
        }
        else {
            r = df;
        }        
        return parseInt(that.traverseToDataField(r, that.dataValue()));
    }
    
    function getLbl(x) {
        var r;
        if (df instanceof Array) {
            r = df[x];
        }
        else {
            r = df;
        }
        return that.traverseToDataField(r, that.dataLabel());
    }
};

wso2vis.s.chart.raphael.PieChart.prototype.onTooltip = function (data) {
    return data.label + ":" + data.value;
};

