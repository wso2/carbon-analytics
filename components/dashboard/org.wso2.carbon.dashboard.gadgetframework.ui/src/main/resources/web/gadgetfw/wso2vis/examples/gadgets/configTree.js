ConfigTree = function (_canvas, _allowedDepth, _allowedParentNode) {
    var allowedDepth = _allowedDepth;
    var allowedParentNode = _allowedParentNode;
    var canvas = canvas;

    this.treeView = new wso2vis.s.form.TreeView()
			 .canvas(_canvas)
             .dataField(["node"])
             .nodeLabel(["label"])
             .nodeValue(["value"])
             .nodeChildren(["children"]);
    this.treeView.create();

    this.treeView.onExpand = onExpand;
    this.treeView.onCollapse = onCollapse;
    this.treeView.onLabelClick = onLabelClick;

    function isRestrictedNode(node) {
        if ( node.depth >= allowedDepth ) {
            if ( (allowedParentNode !== undefined) && (node.parent.label !== allowedParentNode) ) {
                return true;
            }
            return false;
        }
    };

    function onExpand (n) { 
        if( !isRestrictedNode(n) ) {
            console.log(n);
        } else {
            console.log("restricted node!");
        }
        return false;
    };

    function onCollapse (n) { 
        if( !isRestrictedNode(n) ) {
            console.log(n);
        } else {
            console.log("restricted node!");
        }
        return false;
    };

    function onLabelClick (n) {
        if( !isRestrictedNode(n) ) {
            console.log(n);
        } else {
            console.log("restricted node!");
        }
    };
};

