/**
 * @class ProviderGETMakeRequest
 * @extends ProviderGET
 * @author suho@wso2.com
 **/

wso2vis.p.ProviderGETMakeRequest = function(url) {
    wso2vis.p.ProviderGET.call(this, url);
};

wso2vis.extend(wso2vis.p.ProviderGETMakeRequest, wso2vis.p.ProviderGET);

wso2vis.p.ProviderGETMakeRequest.prototype.pullData = function() {
    // Make sure the XMLHttpRequest object was instantiated
    var that = this;
    var st = new Date().getTime();

    var params = {};
    params[gadgets.io.RequestParameters.CONTENT_TYPE] = gadgets.io.ContentType.DOM;

    var refreshInterval = 0;
    var sep = "?";
    if (this.url.indexOf("nocache=") < 0) {
        if (this.url.indexOf("?") > -1) {
            sep = "&";
        }
        this.url = [ this.url, sep, "nocache=", refreshInterval ].join("");
    }

    if (this.url.indexOf("randVal=") < 0) {
        if (this.url.indexOf("?") > -1) {
            sep = "&";
        }
        this.url = [ this.url, sep, "randVal=", st ].join("");
    } else {
        var urlArr = this.url.split("&");
        urlArr.pop();
        this.url = [ urlArr.join(""), "&", "randVal=", st ].join("");
    }

    gadgets.io.makeRequest(this.url, callback, params);

    function callback(resp) {
        that.parseResponse(resp, that);
    }

}

wso2vis.p.ProviderGETMakeRequest.prototype.pullDataSync = function() {
    var that = this;
    var params = {};
    params[gadgets.io.RequestParameters.CONTENT_TYPE] = gadgets.io.ContentType.DOM;
    var refreshInterval = 0;
    var sep = "?";
    if (this.url.indexOf("nocache=") < 0) {
        if (this.url.indexOf("?") > -1) {
            sep = "&";
        }
        this.url = [ this.url, sep, "nocache=", refreshInterval ].join("");
    }

    gadgets.io.makeRequest(this.url, callback, params);

    function callback(resp) {
        that.parseResponse(resp, that);
    }

    return false;
}

wso2vis.p.ProviderGETMakeRequest.prototype.parseResponse = function(response, that) {
    that.pushData(that.xmlToJson(response.data, "  "));
}


