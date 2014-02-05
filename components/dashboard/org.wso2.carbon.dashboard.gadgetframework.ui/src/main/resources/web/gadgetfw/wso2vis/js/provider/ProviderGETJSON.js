/**
* @class ProviderGET
* @extends Provider
**/
wso2vis.p.ProviderGETJSON = function(url, args, that) {
	wso2vis.p.ProviderGET.call(this, url, args, that);
};

wso2vis.extend(wso2vis.p.ProviderGETJSON, wso2vis.p.ProviderGET);


wso2vis.p.ProviderGETJSON.prototype.parseResponse = function(response, that) {
    return JSON.parse(response);
};

wso2vis.p.ProviderGETJSON.prototype.initialize = function() {
};
