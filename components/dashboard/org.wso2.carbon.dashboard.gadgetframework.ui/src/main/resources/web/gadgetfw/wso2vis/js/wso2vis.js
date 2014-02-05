/**
 * The top-level WSO2Vis namespace. All public methods and fields should be
 * registered on this object. Note that core wso2vis source is surrounded by an
 * anonymous function, so any other declared globals will not be visible outside
 * of core methods. This also allows multiple versions of WSO2Vis to coexist,
 * since each version will see their own <tt>wso2vis</tt> namespace.
 *
 * @namespace The top-level wso2vis namespace, <tt>wso2vis</tt>.
 */
var wso2vis = {};

/**
 * @namespace wso2vis namespace for Providers, <tt>wso2vis.p</tt>.
 */
wso2vis.p = {};

/**
 * @namespace wso2vis namespace for Filters, <tt>wso2vis.f</tt>.
 */
wso2vis.f = {};

/**
 * @namespace wso2vis namespace for Filter Forms, <tt>wso2vis.f.form</tt>.
 */
wso2vis.f.form = {};

/**
 * @namespace wso2vis namespace for Subscribers, <tt>wso2vis.s</tt>.
 */
wso2vis.s = {};

/**
 * @namespace wso2vis namespace for Subscriber Charts, <tt>wso2vis.s.chart</tt>.
 */
wso2vis.s.chart = {};

/**
 * @namespace wso2vis namespace for Subscriber Protovis Charts, <tt>wso2vis.s.chart.protovis</tt>.
 */
wso2vis.s.chart.protovis = {};

/**
 * @namespace wso2vis namespace for Subscriber Raphael Charts, <tt>wso2vis.s.chart.raphael</tt>.
 */
wso2vis.s.chart.raphael = {};

/**
 * @namespace wso2vis namespace for Subscriber Infovis Charts, <tt>wso2vis.s.chart.raphael</tt>.
 */
wso2vis.s.chart.infovis = {};

/**
 * @namespace wso2vis namespace for Subscriber composite Charts, <tt>wso2vis.s.chart.composite</tt>.
 */
wso2vis.s.chart.composite = {};

/**
 * @namespace wso2vis namespace for Subscriber Forms, <tt>wso2vis.s.form</tt>.
 */
wso2vis.s.form = {};

/**
 * @namespace wso2vis namespace for Subscriber Gauges, <tt>wso2vis.s.gauge</tt>.
 */
wso2vis.s.gauge = {};

/**
 * @namespace wso2vis namespace for Subscriber Raphael Charts, <tt>wso2vis.s.chart.raphael</tt>.
 */
wso2vis.s.gauge.raphael = {};

/**
 * @namespace wso2vis namespace for Utility Components, <tt>wso2vis.u</tt>.
 */
wso2vis.u = {};

/**
 * @namespace wso2vis namespace for utility functions, <tt>wso2vis.util</tt>.
 */
wso2vis.util = {};

/**
 * @namespace wso2vis namespace for Adaptors, <tt>wso2vis.a</tt>.
 */
wso2vis.a = {};

/**
 * @namespace wso2vis namespace for controls, <tt>wso2vis.c</tt>.
 */
wso2vis.c = {};

/**
 * @namespace wso2vis namespace for user defined custom functions, <tt>wso2vis.fn</tt>.
 */
wso2vis.fn = {};

/**
 * WSO2Vis major and minor version numbers.
 *
 * @namespace WSO2Vis major and minor version numbers.
 */
wso2vis.version = {
   /**
    * The major version number.
    *
    * @type number
    * @constant
    */
    major: 0,

   /**
    * The minor version number.
    *
    * @type number
    * @constant
    */
    minor: 1
};

/**
 * WSO2Vis environment. All data providers, filters and charts are registred in the environment.
 * @namespace wso2vis namespace for environment variables, <tt>wso2vis.environment</tt>.
 */ 
wso2vis.environment = { 
   /** 
    * providers array
    */
    providers: [],

   /** 
    * filters array
    */
    filters: [],
    
   /**
    * charts array
    */
    charts: [],
    
   /** 
    * dialogs array
    */
    dialogs: [],
    
    /**
     * subscribers array
     */
     subscribers: [],
     
    /**
     * adapters array
     */
     adapters: [],

	 /**
	  * controls array
	  */
	 controls: [],
	 
	 /**
	  * gauges array
	  */
	 gauges: []
	
};

wso2vis.fn.getProviderFromID = function(id) {
    if ((id >= 0) && (wso2vis.environment.providers.length > id)) {
        return wso2vis.environment.providers[id];
    }
    return null;
};

wso2vis.fn.getFilterFromID = function(id) {
    if ((id >= 0) && (wso2vis.environment.filters.length > id)) {
        return wso2vis.environment.filters[id];
    }
    return null;    
};

wso2vis.fn.getChartFromID = function(id) {
    if ((id >= 0) && (wso2vis.environment.charts.length > id)) {
        return wso2vis.environment.charts[id];
    }
    return null;
};

wso2vis.fn.getDialogFromID = function(id) {
    if ((id >= 0) && (wso2vis.environment.dialogs.length > id)) {
        return wso2vis.environment.dialogs[id];
    }
    return null;
};

wso2vis.fn.getElementFromID = function(id) {
    if ((id >= 0) && (wso2vis.environment.elements.length > id)) {
        return wso2vis.environment.elements[id];
    }
    return null;
};

wso2vis.fn.getAdapterFromID = function(id) {
    if ((id >= 0) && (wso2vis.environment.adapters.length > id)) {
        return wso2vis.environment.adapters[id];
    }
    return null;
};

wso2vis.fn.getControlFromID = function(id) {
    if ((id >= 0) && (wso2vis.environment.controls.length > id)) {
        return wso2vis.environment.controls[id];
    }
    return null;
};

wso2vis.fn.getGaugeFromID = function(id) {
    if ((id >= 0) && (wso2vis.environment.gauges.length > id)) {
        return wso2vis.environment.gauges[id];
    }
    return null;
};

wso2vis.fn.traverseToDataField = function (object, dataFieldArray) {
	var a = object;					
	for (var i = 0; i < dataFieldArray.length; i++) {
		a = a[dataFieldArray[i]];
	}
	return a;
};

wso2vis.fn.traverseNKillLeaf = function (object, dataFieldArray) {
	var a = object;
	for (var i = 0; i < dataFieldArray.length; i++) {
		if (i == dataFieldArray.length - 1) {
			delete a[dataFieldArray[i]];
		}
		else {
			a = a[dataFieldArray[i]];
		}
	}
};

/* using "Parasitic Combination Inheritance" */
wso2vis.extend = function(subc, superc /*, overrides*/) {
    if (!superc||!subc) {
        throw new Error("extend failed, please check that " +
                        "all dependencies are included.");
    }
    var F = function() {}/*, i*/;
    F.prototype=superc.prototype;
    subc.prototype=new F();
    subc.prototype.constructor=subc;
    subc.superclass=superc.prototype;
    if (superc.prototype.constructor == Object.prototype.constructor) {
        superc.prototype.constructor=superc;
    }

    /* Lets worry about the following later
    if (overrides) {
        for (i in overrides) {
            if (L.hasOwnProperty(overrides, i)) {
                subc.prototype[i]=overrides[i];
            }
        }

        L._IEEnumFix(subc.prototype, overrides);
    } */
};

wso2vis.initialize = function() {
    wso2vis.environment.tooltip = new wso2vis.c.Tooltip();        
};


