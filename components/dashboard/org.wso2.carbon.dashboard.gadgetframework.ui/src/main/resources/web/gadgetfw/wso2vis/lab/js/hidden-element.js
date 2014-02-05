wso2vis.ide.hwElement = function (hElement, type, name) { /* type = 0 for provider, 1 for filter, 2 for adaptor */
	this.elem = hElement;
	this.type = type;
	this.name = name;
}

wso2vis.ide.hwElement.prototype.pushData = function (d) {
	this.elem.pushData(d);
}

wso2vis.ide.hwElement.prototype.pullData = function () {
	this.elem.pullData();
}

wso2vis.ide.hwElement.prototype.pullDataSync = function () {
	this.elem.pullDataSync();
}

wso2vis.ide.hwElement.prototype.initialize = function () {
	this.elem.initialize();
}

wso2vis.ide.hwElement.prototype.addDataReceiver = function (res) {
	this.elem.addDataReceiver(res);
}





