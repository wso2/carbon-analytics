/**
 * Abstract Class for Providers
 * @class Provider
 * @constructor
 */
wso2vis.p.Provider = function() {
    this.drList = [];
    wso2vis.environment.providers.push(this);
    id = wso2vis.environment.providers.length - 1;
    this.getID = function() {
        return id;
    }
};

wso2vis.p.Provider.prototype.initialize = function() {
};

wso2vis.p.Provider.prototype.addDataReceiver = function(dataReceiver) {
    this.drList.push(dataReceiver);
};

wso2vis.p.Provider.prototype.pushData = function(data) {
    // loop all data receivers. Pump data to them.
    //console.log(JSON.stringify(data) + this.url);
    if (this.postValidate(this, data)) {
        for (i = 0; i < this.drList.length; i++) {
            (this.drList[i]).pushData(data);
        }
    }
};

wso2vis.p.Provider.prototype.preValidate = function(that) {
    return true;
};

wso2vis.p.Provider.prototype.postValidate = function(that, data) {
    return true;
};

wso2vis.p.Provider.prototype.errorCallback = function(phase, that, type) {
};

wso2vis.p.Provider.prototype.flowStart = function(that) {
};

wso2vis.p.Provider.prototype.pullData = function() {

};

