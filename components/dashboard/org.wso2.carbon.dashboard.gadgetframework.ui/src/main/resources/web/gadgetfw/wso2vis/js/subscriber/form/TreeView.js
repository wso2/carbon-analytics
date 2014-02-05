/**
 * @class TreeView
 * @extends Subscriber
 */
wso2vis.s.form.TreeView = function() {
	wso2vis.s.Subscriber.call(this);

    /* @private */
    this.tree = null;
    this.data = null;
};

wso2vis.extend(wso2vis.s.form.TreeView, wso2vis.s.Subscriber);

wso2vis.s.form.TreeView.prototype
    .property("canvas")
    .property("nodeLabel")
    .property("nodeValue")
    .property("nodeChildren")
    .property("dataField");

wso2vis.s.form.TreeView.prototype.create = function() {

    var that = this;

    //instantiate the TreeView control:
    this.tree = new YAHOO.widget.TreeView(this.canvas());
    var rootNode = this.tree.getRoot();

    if( this.data !== null ){
        //begin adding children 
        rec(rootNode, this.data);
    }

    function rec(node, data) {
        var children;
        if( data === undefined || data === null ) {
            return;
        }

        var dataField = that.traverseToDataField(data, that.dataField());

        if (dataField instanceof Array) {
            children = dataField.length;
        }
        else {
            children = 1;
        }

        for (var i=0; i<children; i++) {
            var dataObj;
            if ( dataField instanceof Array ){
                dataObj = dataField[i];
            }
            else {
                dataObj = dataField;
            }
            var nodeLabel = that.traverseToDataField(dataObj, that.nodeLabel());
            var nodeValue = that.traverseToDataField(dataObj, that.nodeValue());
            var nodeChildren = that.traverseToDataField(dataObj, that.nodeChildren());

            var dataNode = {};
            dataNode.label = nodeLabel;
            dataNode.value = nodeValue;

            var childNode = new YAHOO.widget.TextNode(dataNode, node, true);
            rec(childNode, nodeChildren);
        }
    }

    // Expand and collapse happen prior to the actual expand/collapse,
    // and can be used to cancel the operation
    this.tree.subscribe("expand", this.onExpand);
    this.tree.subscribe("collapse", this.onCollapse);
    this.tree.subscribe("labelClick", this.onLabelClick);

    this.tree.draw();
};

wso2vis.s.form.TreeView.prototype.update = function() {    var canvas = document.getElementById(this.canvas());    canvas.innerHTML = "";

    this.create();};

wso2vis.s.form.TreeView.prototype.onExpand = function(node) {
    console.log(node.index + " - " + node.label + " was expanded");
};

wso2vis.s.form.TreeView.prototype.onCollapse = function(node) {
    console.log(node.index + " - " + node.label + " was collapsed");
};

wso2vis.s.form.TreeView.prototype.onLabelClick = function(node) {
    console.log(node.index + " - " + node.label + " label was clicked");
};

wso2vis.s.form.TreeView.prototype.pushData = function(data) {
      this.data = data;//console.log(data);
      this.update();
};

wso2vis.s.form.TreeView.prototype.traverseToDataField = function (object, dataFieldArray) {
	var a = object;					
	for (var i = 0; i < dataFieldArray.length; i++) {
		a = a[dataFieldArray[i]];
	}
	return a;
};

