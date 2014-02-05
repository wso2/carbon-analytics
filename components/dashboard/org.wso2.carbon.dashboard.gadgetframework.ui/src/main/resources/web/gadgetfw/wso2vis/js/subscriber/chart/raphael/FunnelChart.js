wso2vis.s.chart.raphael.FunnelChart = function(canvas, chartTitle, chartDesc) {
    wso2vis.s.chart.Chart.call(this, canvas, chartTitle, chartDesc);
    
    this.colscheme(20)
        .gap(4)
        .labelRelief(15)
        .labelSpan(1)
        .showPercent(true)
        .showValue(true);
}

// inherits from Chart
wso2vis.extend(wso2vis.s.chart.raphael.FunnelChart, wso2vis.s.chart.Chart);

wso2vis.s.chart.raphael.FunnelChart.prototype
    .property("colscheme")
    .property("dataField")
    .property("dataValue")
    .property("dataLabel")
    .property("gap")
    .property("showPercent")
    .property("showValue")
    .property("labelRelief")
    .property("labelSpan");
    

wso2vis.s.chart.raphael.FunnelChart.prototype.load = function (w, h) {
    if (w !== undefined) {
        this.width(w);
    }
    if (h !== undefined) {
        this.height(h);
    }
    
    this.r = Raphael(this.divEl(), this.width(), this.height());  
    
    this.wf = this.width() / 406.01;
	this.hf = this.height() / 325.01;
	this.eh = 1007.9 * this.hf;
	this.ew = 663 * this.wf;
	this.e1x = -560 * this.wf + 5;
	this.e2x = 173 * this.wf + 5;
	this.ey = -136 * this.hf;   	
	this.fc = 139 * this.wf + 5;     
	
	return this; 
};

wso2vis.s.chart.raphael.FunnelChart.prototype.update = function () {
    this.convertData(this);
    
    var total = 0;
    
    for (var i = 0; i < this.formattedData.length; i++) {
        total += this.formattedData[i]["value"];
    }
    
    var funnelHeightRatio = (this.height()  - this.gap() * (this.formattedData.length - 1) - this.labelRelief() * this.formattedData.length) / total;        
    
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
    
    for (i = 0; i < this.formattedData.length; i++) {
        var crect;
        if (i != 0) {
            this.r.rect(0, currY, this.width(), this.gap()).attr({fill:"#fff", stroke:"#fff"});          
            crect = this.r.rect(0, currY + this.gap(), this.width(), funnelHeightRatio * this.formattedData[i]["value"] + this.labelRelief()).attr({fill:colors[i], stroke:"#fff"});
            currY += this.gap() + funnelHeightRatio * this.formattedData[i]["value"] + this.labelRelief();                
        }
        else {
            crect = this.r.rect(0, 0, this.width(), funnelHeightRatio * this.formattedData[i]["value"] + this.labelRelief()).attr({fill:colors[i], stroke:"#fff"});
            currY += funnelHeightRatio * this.formattedData[i]["value"] + this.labelRelief();           
            first = this.formattedData[i]["value"];    
        }
        
        if (this.tooltip()) {
            (function (data, lbl, func, org) {
                $(crect.node).hover(function (e) {
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
            })(this.formattedData[i]["value"], this.formattedData[i]["label"], this.onTooltip, df[i]);//(this.fc - frame.attrs.width/2 , crect.attrs.y + crect.attrs.height, this.formattedData[i]["value"], this.formattedData[i]["label"]);
            
            (function (data, lbl, func, org) {
                $(crect.node).mousemove(function (e) {
                    if (is_label_visible) {
                        clearTimeout(leave_timer);
                        var tip = func({label:lbl, value:data, total:total, first:first, raw:org});
                        wso2vis.environment.tooltip.show(e.pageX, e.pageY, tip);
                    }
                });
            })(this.formattedData[i]["value"], this.formattedData[i]["label"], this.onTooltip, df[i]);//(this.fc - frame.attrs.width/2 , crect.attrs.y + crect.attrs.height, this.formattedData[i]["value"], this.formattedData[i]["label"]);
        }
    }   

    var el1 = this.r.ellipse(-560 * this.wf + 5 + 663 * this.wf / 2, -136 * this.hf + 1007.9 * this.hf / 2, 663 * this.wf / 2, 1007.9 * this.hf / 2);
    var el2 = this.r.ellipse(173 * this.wf + 5 + 663 * this.wf / 2, -136 * this.hf + 1007.9 * this.hf / 2, 663 * this.wf / 2, 1007.9 * this.hf / 2); 
    
    el1.attr({fill:"#fff", opacity:0.9, stroke:"#fff"});
    el2.attr({fill:"#fff", opacity:0.8, stroke:"#fff"});
    
    currY = 0;
    for (i = 0; i < this.formattedData.length; i++) {
        var t2;
        if (i != 0) {
            var t = this.r.text(this.width(), currY + this.gap() + funnelHeightRatio * this.formattedData[i]["value"] + this.labelRelief(), this.formattedData[i]["label"]).attr({fill:colors[i]});
            t.attr({"font-size":12});              
            var bbox = t.getBBox();
            t.translate(-bbox.width/2 - 2 * this.labelSpan(), -bbox.height/2 - this.labelSpan());
            var str = this.showValue()?this.formattedData[i]["value"]:"";
            if ((this.formattedData[0]["value"] != 0) && this.showPercent()) {
                str += "(" + (this.formattedData[i]["value"] * 100/ this.formattedData[0]["value"]).toFixed() + "%)";
            }
            t2 = this.r.text(this.fc, currY + this.gap() + funnelHeightRatio * this.formattedData[i]["value"] + this.labelRelief(), str).attr({fill:"#fff"});
            t2.attr({"font-size":10});
            bbox = t2.getBBox();
            t2.translate(0, -bbox.height/2 - this.labelSpan());
            currY += this.gap() + funnelHeightRatio * this.formattedData[i]["value"] + this.labelRelief();                
        }
        else {
            var t = this.r.text(this.width(), funnelHeightRatio * this.formattedData[i]["value"] + this.labelRelief(), this.formattedData[i]["label"]).attr({fill:colors[i]});            
            t.attr({"font-size":12});  
            var bbox = t.getBBox();
            t.translate(-bbox.width/2 - 2 * this.labelSpan(), -bbox.height/2 - this.labelSpan());            
            var str = this.showValue()?this.formattedData[i]["value"]:"";
            if ((this.formattedData[0]["value"] != 0) && this.showPercent()) {
                str += "(" + (this.formattedData[i]["value"] * 100/ this.formattedData[0]["value"]).toFixed() + "%)";
            }
            t2 = this.r.text(this.fc, funnelHeightRatio * this.formattedData[i]["value"] + this.labelRelief(), str).attr({fill:"#fff"});
            t2.attr({"font-size":10});
            bbox = t2.getBBox();
            t2.translate(0, -bbox.height/2 - this.labelSpan());
            currY += funnelHeightRatio * this.formattedData[i]["value"] + this.labelRelief();                
        }
    }   
    this.r.rect(0, 0, this.width()-1, this.height()-1);
};

wso2vis.s.chart.raphael.FunnelChart.prototype.convertData = function (that) {
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

wso2vis.s.chart.raphael.FunnelChart.prototype.onTooltip = function (data) {
    return data.label + ":" + data.value;
};




