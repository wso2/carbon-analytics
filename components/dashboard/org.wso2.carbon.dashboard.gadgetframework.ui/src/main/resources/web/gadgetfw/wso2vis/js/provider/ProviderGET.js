/**
 * @class ProviderGET
 * @augments wso2vis.p.Provider
 * @constructor
 * @param {String} url The data source URL
 **/
wso2vis.p.ProviderGET = function(url, args, that) {
    this.url = url;
    this.that = that;
    this.args = args;
    this.xmlHttpReq = null;

    wso2vis.p.Provider.call(this);
};

wso2vis.extend(wso2vis.p.ProviderGET, wso2vis.p.Provider);

wso2vis.p.ProviderGET.prototype.initialize = function() {
};

wso2vis.p.ProviderGET.prototype.pullData = function() {
    this.flowStart(this);
    // Make sure the XMLHttpRequest object was instantiated
    if (this.preValidate(this)) {
        var that = this;
        if (!that.xmlHttpReq) {
            that.xmlHttpReq = this.createXmlHttpRequest();
        }
        if (that.xmlHttpReq) {
            that.xmlHttpReq.open("GET", that.getURLwithRandomParam(), true); // to prevent IE caching
            that.xmlHttpReq.onreadystatechange = function() {
                if (that.xmlHttpReq.readyState == 4) {
                    that.pushData(that.parseResponse(that.xmlHttpReq.responseText, that));
                }
            };
            that.xmlHttpReq.send(null);
        }
    }
};

wso2vis.p.ProviderGET.prototype.pullDataSync = function() {
    this.flowStart(this);
    if (this.preValidate(this)) {
        var that = this;
        if (!that.xmlHttpReq) {
            that.xmlHttpReq = this.createXmlHttpRequest();
        }

        if (that.xmlHttpReq) {
            that.xmlHttpReq.open("GET", that.getURLwithRandomParam(), false); // to prevent IE caching
            that.xmlHttpReq.send(null);
            if (that.xmlHttpReq.status == 200) {
                that.pushData(that.parseResponse(that.xmlHttpReq.responseText, that));
                return true;
            }
        }
        return false;
    }
};

wso2vis.p.ProviderGET.prototype.preValidate = function(that, url, args) {
    return true;
};

wso2vis.p.ProviderGET.prototype.parseResponse = function(response, that) {
    var resp = that.parseXml(response);
    return that.xmlToJson(resp, "  ");
};

wso2vis.p.ProviderGET.prototype.getURLwithRandomParam = function() {
    var that = this.that ? this.that : this;
    var url = this.url instanceof Function ? this.url.apply(that, this.args) : this.url;
    if (url.indexOf('?') == -1) {
        return url + '?random=' + new Date().getTime();
    }
    return url + '&random=' + new Date().getTime();
}

wso2vis.p.ProviderGET.prototype.createXmlHttpRequest = function() {
    var request;

    // Lets try using ActiveX to instantiate the XMLHttpRequest
    // object
    try {
        request = new ActiveXObject("Microsoft.XMLHTTP");
    } catch(ex1) {
        try {
            request = new ActiveXObject("Msxml2.XMLHTTP");
        } catch(ex2) {
            request = null;
        }
    }

    // If the previous didn't work, lets check if the browser natively support XMLHttpRequest
    if (!request && typeof XMLHttpRequest != "undefined") {
        //The browser does, so lets instantiate the object
        request = new XMLHttpRequest();
    }

    return request;
}

/**
 * converts xml string to a dom object
 *
 * @param {string} [xml] a xml string
 * @returns {dom} a xml dom object
 */
wso2vis.p.ProviderGET.prototype.parseXml = function(xml) {
    var dom = null;
    if (window.DOMParser) {
        try {
            dom = (new DOMParser()).parseFromString(xml, "text/xml");
        }
        catch (e) {
            dom = null;
        }
    }
    else if (window.ActiveXObject) {
        try {
            dom = new ActiveXObject('Microsoft.XMLDOM');
            dom.async = false;
            if (!dom.loadXML(xml)) // parse error ..
                window.alert(dom.parseError.reason + dom.parseError.srcText);
        }
        catch (e) {
            dom = null;
        }
    }
    else
        window.alert("oops");
    return dom;
}

/**
 * Once passed an xml dom object xmlToJson will create a corresponding JSON object.
 *
 * @param {DOM} [xml] a xml dom object
 * @param {string} [tab] an optional whitespace character to beutify the created JSON string.
 * @returns {object} a JSON object
 */
wso2vis.p.ProviderGET.prototype.xmlToJson = function(xml, tab) {
    var X = {
        toObj: function(xml) {
            var o = {};
            if (xml.nodeType == 1) {
                if (xml.attributes.length)
                    for (var i = 0; i < xml.attributes.length; i++)
                        o["@" + xml.attributes[i].nodeName] = (xml.attributes[i].nodeValue || "").toString();
                if (xml.firstChild) {
                    var textChild = 0, cdataChild = 0, hasElementChild = false;
                    for (var n = xml.firstChild; n; n = n.nextSibling) {
                        if (n.nodeType == 1) hasElementChild = true;
                        else if (n.nodeType == 3 && n.nodeValue.match(/[^ \f\n\r\t\v]/)) textChild++;
                        else if (n.nodeType == 4) cdataChild++;
                    }
                    if (hasElementChild) {
                        if (textChild < 2 && cdataChild < 2) {
                            X.removeWhite(xml);
                            for (var n = xml.firstChild; n; n = n.nextSibling) {
                                if (n.nodeType == 3)
                                    o["#text"] = X.escape(n.nodeValue);
                                else if (n.nodeType == 4)
                                    o["#cdata"] = X.escape(n.nodeValue);
                                else if (o[n.nodeName]) {
                                    if (o[n.nodeName] instanceof Array)
                                        o[n.nodeName][o[n.nodeName].length] = X.toObj(n);
                                    else
                                        o[n.nodeName] = [o[n.nodeName], X.toObj(n)];
                                }
                                else
                                    o[n.nodeName] = X.toObj(n);
                            }
                        }
                        else {
                            if (!xml.attributes.length)
                                o = X.escape(X.innerXml(xml));
                            else
                                o["#text"] = X.escape(X.innerXml(xml));
                        }
                    } //(hasElementChild)
                    else if (textChild) {
                        if (!xml.attributes.length)
                            o = X.escape(X.innerXml(xml));
                        else
                            o["#text"] = X.escape(X.innerXml(xml));
                    }
                    else if (cdataChild) {
                        if (cdataChild > 1)
                            o = X.escape(X.innerXml(xml));
                        else
                            for (var n = xml.firstChild; n; n = n.nextSibling)
                                o["#cdata"] = X.escape(n.nodeValue);
                    }
                }
                if (!xml.attributes.length && !xml.firstChild) o = null;
            }
            else if (xml.nodeType == 9) {
                o = X.toObj(xml.documentElement);
            }
            else
                alert("unhandled node type: " + xml.nodeType);
            return o;
        },
        toJson: function(o, name, ind) {
            var p = name.lastIndexOf(':');
            if (p != -1) {
                if (p + 1 >= name.length)
                    name = "";
                else
                    name = name.substr(p + 1);
            }
            var json = name ? ("\"" + name + "\"") : "";
            if (o instanceof Array) {
                for (var i = 0,n = o.length; i < n; i++)
                    o[i] = X.toJson(o[i], "", ind + "\t");
                json += (name ? ":[" : "[") + (o.length > 1 ? ("\n" + ind + "\t" + o.join(",\n" + ind + "\t") + "\n" + ind) : o.join("")) + "]";
            }
            else if (o == null)
                json += (name && ":") + "null";
            else if (typeof(o) == "object") {
                var arr = [];
                for (var m in o)
                    arr[arr.length] = X.toJson(o[m], m, ind + "\t");
                json += (name ? ":{" : "{") + (arr.length > 1 ? ("\n" + ind + "\t" + arr.join(",\n" + ind + "\t") + "\n" + ind) : arr.join("")) + "}";
            }
            else if (typeof(o) == "string")
                json += (name && ":") + "\"" + o.toString() + "\"";
            else
                json += (name && ":") + o.toString();
            return json;
        },
        innerXml: function(node) {
            var s = ""
            if ("innerHTML" in node)
                s = node.innerHTML;
            else {
                var asXml = function(n) {
                    var s = "";
                    if (n.nodeType == 1) {
                        s += "<" + n.nodeName;
                        for (var i = 0; i < n.attributes.length; i++)
                            s += " " + n.attributes[i].nodeName + "=\"" + (n.attributes[i].nodeValue || "").toString() + "\"";
                        if (n.firstChild) {
                            s += ">";
                            for (var c = n.firstChild; c; c = c.nextSibling)
                                s += asXml(c);
                            s += "</" + n.nodeName + ">";
                        }
                        else
                            s += "/>";
                    }
                    else if (n.nodeType == 3)
                        s += n.nodeValue;
                    else if (n.nodeType == 4)
                        s += "<![CDATA[" + n.nodeValue + "]]>";
                    return s;
                };
                for (var c = node.firstChild; c; c = c.nextSibling)
                    s += asXml(c);
            }
            return s;
        },
        escape: function(txt) {
            return txt.replace(/[\\]/g, "\\\\")
                    .replace(/[\"]/g, '\\"')
                    .replace(/[\n]/g, '\\n')
                    .replace(/[\r]/g, '\\r');
        },
        removeWhite: function(e) {
            e.normalize();
            for (var n = e.firstChild; n;) {
                if (n.nodeType == 3) {
                    if (!n.nodeValue.match(/[^ \f\n\r\t\v]/)) {
                        var nxt = n.nextSibling;
                        e.removeChild(n);
                        n = nxt;
                    }
                    else
                        n = n.nextSibling;
                }
                else if (n.nodeType == 1) {
                    X.removeWhite(n);
                    n = n.nextSibling;
                }
                else
                    n = n.nextSibling;
            }
            return e;
        }
    };
    if (xml.nodeType == 9)
        xml = xml.documentElement;
    var json = X.toJson(X.toObj(X.removeWhite(xml)), xml.nodeName, "\t");
    return JSON.parse("{\n" + tab + (tab ? json.replace(/\t/g, tab) : json.replace(/\t|\n/g, "")) + "\n}");
}

