/** 
 * DataSubscriber 
 */
wso2vis.s.Subscriber = function() {
    this.attr = [];
    wso2vis.environment.subscribers.push(this);
    id = wso2vis.environment.subscribers.length - 1;
    this.getID = function() {
        return id;
    };
};

wso2vis.s.Subscriber.prototype.property = function(name) {
    /*
    * Define the setter-getter globally
    */
    wso2vis.s.Subscriber.prototype[name] = function(v) {
      if (arguments.length) {
        this.attr[name] = v;
        return this;
      }
      return this.attr[name];
    };

    return this;
};

/**
 * Set data to the subscriber. Providers use this method to push data to subscribers.
 *
 * @param {object} [data] a JSON object.
 */
wso2vis.s.Subscriber.prototype.pushData = function(data) {
};

