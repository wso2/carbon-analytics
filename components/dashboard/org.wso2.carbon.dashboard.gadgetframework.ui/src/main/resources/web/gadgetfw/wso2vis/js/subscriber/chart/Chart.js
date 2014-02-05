/**
 * @class
 * Base class for all charts
 */
wso2vis.s.chart.Chart = function (canvas, ttle, desc) {
    wso2vis.s.Subscriber.call(this);
    /* @private */
    this.title(ttle)
            .description(desc)
            .divEl(canvas)
            .tooltip(true)
            .legend(true)
            .marks(false)
            .width(600)
            .height(500)
            .titleFont("10px sans-serif")
            .labelFont("10px sans-serif")
            .legendX(0)
            .legendY(0)
            .paddingTop(25)
            .paddingLeft(10)
            .paddingRight(60)
            .paddingBottom(10);

    /* @private */
    this.data = null;
    this.formattedData = null;

    wso2vis.environment.charts.push(this);
    id = wso2vis.environment.charts.length - 1;
    this.getID = function() {
        return id;
    };
};

wso2vis.extend(wso2vis.s.chart.Chart, wso2vis.s.Subscriber);

wso2vis.s.chart.Chart.prototype
        .property("title")
        .property("description")
        .property("divEl")
        .property("msgDiv")
        .property("tooltip")
        .property("legend")
        .property("x")
        .property("y")
        .property("width")
        .property("height")
        .property("paddingTop")
        .property("paddingLeft")
        .property("paddingRight")
        .property("paddingBottom")
        .property("anchorTop")
        .property("anchorLeft")
        .property("anchorRight")
        .property("anchorBottom")
        .property("legendX")
        .property("legendY")
        .property("titleFont")
        .property("labelFont")
        .property("marks");

wso2vis.s.chart.Chart.prototype.pushData = function (d) {
    if (this.validateData(d)) {
        this.data = d;
        this.preUpdate();
        this.update();
        this.postUpdate();
    } else {
        this.updateMessageDiv(this.messageInterceptFunction());
    }
};

wso2vis.s.chart.Chart.prototype.validateData = function (d) {
    //Check whether we have valid data or not.
    if (d === null || d === undefined) {
        return false;
    }
    else {
        return true;
    }
};

wso2vis.s.chart.Chart.prototype.update = function () {
};

wso2vis.s.chart.Chart.prototype.updateMessageDiv = function (s) {

    if (this.msgDiv() !== undefined) {
        var msgdiv = document.getElementById(this.msgDiv());
        if (msgdiv !== undefined) {
            msgdiv.innerHTML = s;
            msgdiv.style.display = "block";
        }
    }
};

wso2vis.s.chart.Chart.prototype.showMessageDiv = function () {
    if (this.msgDiv() !== undefined) {
        var msgdiv = document.getElementById(this.msgDiv());
        if (msgdiv !== undefined) {
            msgdiv.style.display = "block";
        }
    }
};

wso2vis.s.chart.Chart.prototype.hideMessageDiv = function (s) {
    if (this.msgDiv() !== undefined) {
        var msgdiv = document.getElementById(this.msgDiv());
        if (msgdiv !== undefined) {
            msgdiv.style.display = "none";
        }
    }
};

wso2vis.s.chart.Chart.prototype.messageInterceptFunction = function () {

    return "Invalid Data";
};

wso2vis.s.chart.Chart.prototype.onClick = function () {
};

wso2vis.s.chart.Chart.prototype.onTooltip = function (data) {
    return "";
};

wso2vis.s.chart.Chart.prototype.onKey = function () {
};

wso2vis.s.chart.Chart.prototype.traverseToDataField = function (object, dataFieldArray) {
    var a = object;
    try { //Try catch outside the loop TODO
        for (var i = 0; i < dataFieldArray.length; i++) {
            a = a[dataFieldArray[i]];
        }
    }
    catch (e) {
        this.updateMessageDiv(this.messageInterceptFunction());
    }
    return a;
};

wso2vis.s.chart.Chart.prototype.getDataObject = function (dataObj, i) {
    if (dataObj instanceof Array) {
        return dataObj[i];
    }
    else {
        return dataObj;
    }
};

wso2vis.s.chart.Chart.prototype.preUpdate = function () {

};

wso2vis.s.chart.Chart.prototype.postUpdate = function () {

};

wso2vis.s.chart.Chart.prototype.show = function() {
    var divEl = document.getElementById(this.divEl());
    if (divEl !== undefined) {
        divEl.style.display = "block";
    }
};

wso2vis.s.chart.Chart.prototype.hide = function() {
    var divEl = document.getElementById(this.divEl());
    if (divEl !== undefined) {
        divEl.style.display = "none";
    }
};

