wso2vis.s.chart.raphael.DependencyTree = function(canvas, chartTitle, chartDesc) {
    wso2vis.s.chart.Chart.call(this, canvas, chartTitle, chartDesc);
    this.div = canvas;
    this.nodelength(50)
        .nodeheight(20)
        .edgelength(20)
        .sx(30) // distance between 2 successive horizontal nodes
        .sy(40) // distance between 2 successive vertical nodes
        .arrowsize(5)
        .arrowpos("mid");

}

//inherits from Chart
wso2vis.extend(wso2vis.s.chart.raphael.DependencyTree, wso2vis.s.chart.Chart);

wso2vis.s.chart.raphael.DependencyTree.prototype
	.property("dataField")
	.property("dataValue")
    .property("nodelength")
    .property("nodeheight")
    .property("edgelength")
    .property("sx")
    .property("sy")
    .property("arrowsize")
    .property("arrowpos");





Raphael.fn.node = function(x, y, width, height, node_label, url) {

	var box = this.rect(x,y , width, height);

	var mid_x = (x*2 + width)/2;
	var mid_y = (y*2 + height)/2;

	//truncate string
	var trunc_string = null;
	trunc_string = (node_label.length < 15 )? node_label : node_label.substring(0,11) + "...";

	var box_label = this.text(mid_x, mid_y, trunc_string);

	// configure tooltip
	var tip = new wso2vis.c.Tooltip();
	box_label.node.onmouseover = function(e) {
		tip.show(e.pageX, e.pageY, node_label);
	}
	box_label.node.onmouseout = function (e) {
		tip.hide();
	}

	// set up url on label
	if (url) {
		box_label.node.onclick = function(e) {
			open(url);
		}
		box_label.node.onmouseover = function(e) {
			box_label.attr({
				fill: "#25B"

			});
			box_label.node.style.cursor = "pointer";
			tip.show(e.pageX, e.pageY, node_label);
		}
		box_label.node.onmouseout = function (e) {
			box_label.attr({fill: "#000"});
			tip.hide();
		}
	}
	return [box, box_label];
}

Raphael.fn.edge = function (x1, y1, x2, y2, size, arrow_position, edge_label) {
	var angle = Math.atan2(y2-y1,x2-x1) * 180 / Math.PI ;
	var arrow_x = x2;
	var arrow_y = y2;
	var mid_x = (x1+x2)/2;
	var mid_y = (y1+y2)/2;

	if (arrow_position == "start") {
		arrow_x = x1;
		arrow_y = y1;
	} else if (arrow_position == "mid") {
		arrow_x = mid_x;
		arrow_y = mid_y;
	} else if (arrow_position == "end") {
		arrow_x = x2;
		arrow_y = y2;
	}

	var arrow_path = this.path("M"+arrow_x+" "+arrow_y+"l"+0+" "+-(size/2)+"l"+size+" "+(size/2)+"l"+-size+" "+(size/2) +"z").attr("fill","black").rotate(angle, arrow_x, arrow_y);
	var line_path = this.path("M"+x1+" "+y1+"L"+x2+" "+y2);
	var label;
	if (edge_label) {
		//truncate string
		var trunc_string = null;
		trunc_string = (edge_label.length < 9 )? edge_label : edge_label.substring(0,5) + "...";

		label = this.text(mid_x, mid_y-(size+2), trunc_string).rotate(angle, mid_x, mid_y);

		// configure tooltip
		var tip = new wso2vis.c.Tooltip();
		label.node.onmouseover = function(e) {
			tip.show(e.pageX, e.pageY, edge_label);
		}
		label.node.onmouseout = function (e) {
			tip.hide();
		}


	}
	return [arrow_path, line_path, label];
}



Raphael.fn.node_edge = function ( x, y, px, py, node_length, node_height, node_label, edge_length, edge_label, size, arrow_position, url) {
	var _node = this.node(x,y - (node_height/2),node_length, node_height, node_label, url);
	_node[0].attr({fill: '#4AE',
        stroke: '#3b4449',
        'stroke-width': 3,
        'stroke-linejoin': 'round'});
	var _edge = null;
	if (px != null || py != null) {
		_edge = this.edge(px, py, x , y , size, arrow_position, edge_label);
		_edge[0].attr({ stroke: '#3b4449',
	        'stroke-width': 3,
	        'stroke-linejoin': 'round'});
		_edge[1].attr({ stroke: '#3b4449',
	        'stroke-width': 3,
	        'stroke-linejoin': 'round'});
	}
	return [_node, _edge];
}

//public function
wso2vis.s.chart.raphael.DependencyTree.prototype.load = function (w, h) {
    if (w !== undefined) {
        this.width(w);
    }
    if (h !== undefined) {
        this.height(h);
    }

    this.paper = Raphael(this.divEl(), this.width(), this.height());

	return this;
};


wso2vis.s.chart.raphael.DependencyTree.prototype.draw_tree_node_edge = function (paper, px, py ,x,y, node_label, edge_label, url) {
	var node_length = this.nodelength();
	var edge_length = this.edgelength();
	var node_height = this.nodeheight();
	var size = this.arrowsize();
	var arrow_position = this.arrowpos();
	paper.node_edge(x, y, px, py, node_length, node_height, node_label, edge_length, edge_label, size, arrow_position, url);
	return [x + node_length, y];
}


wso2vis.s.chart.raphael.DependencyTree.prototype.draw_json_node = function (paper, jsonObj, px, py, pid, x, y) {
	var edge_label = null;
	if (jsonObj.data.edges) {
		var edge_array = jsonObj.data.edges;
		for (var j = 0;j < edge_array.length; j++) {
			var edge_ids = edge_array[j].id.split("---");
			if (edge_ids != null && edge_ids.length > 1) {
				if (edge_ids[0] == pid && edge_ids[1] == jsonObj.id) {
					edge_label = edge_array[j].name;
				}
			}
		}
	}
	var url = null;
	if (jsonObj.data.url) {
		url = jsonObj.data.url;
	}
	return this.draw_tree_node_edge(paper, px, py, x, y, jsonObj.name, edge_label, url );

}

// Node class
function Node (_json) {
	this.json = _json;
	this.visited;
	this.level;
	this.px;
	this.py;
	this.pid;
	this.i;
	this.scount;
	Node.prototype.getChildren = function () {
		var childArray = new Array();
		var child;
		var children = this.json.children;
		for (var i = 0; i < children.length; i++) {
			childArray.push(new Node(children[i]));
		}
		return childArray;
	}
}

function coYCalc() {
	var sy = 5;
	var h = 8;
	cy = jNode.py;
	cy += -((sCount * h) + (sCount-1) * sy)
	//
}


wso2vis.s.chart.raphael.DependencyTree.prototype.calc_draw_single_node = function (paper, jNode) {
	var initX = 10;
	var initY = this.height() /2;
	var d = 30;
	var sx = this.sx();

	var h = 10;
	if (jNode.level == 0) {
		return this.draw_json_node(paper, jNode.json, null, null, null, initX, initY);
	} else {
		var sy = this.sy(); //200/(2*jNode.level);
		cx = jNode.px + sx; //+ (jNode.pLevel - (json.level + 1)) * (d + sx) ;
		var sCount = jNode.sCount;
		var addConst = (sCount == 0) ? 0 : (sCount - 1) ;
		cy = jNode.py + (h + sy) * (jNode.i - ((sCount-1)/2))/Math.pow(3, jNode.level); //((jNode.i  * (h + sy)) - (((sCount - 1) * (sy + h)) / 2));
		return this.draw_json_node(paper, jNode.json, jNode.px, jNode.py, jNode.pid, cx, cy);
	}

}

wso2vis.s.chart.raphael.DependencyTree.prototype.DrawTreeBFS = function (paper, jNode) {
	var queue = new Array();
	jNode.visited = true;
	queue.push(jNode);
	var level = 0;
	var px, py;
	while (queue.length > 0) {
		var pNode = queue.shift();
		level = pNode.level;
		var coArr = this.calc_draw_single_node(paper, pNode);
		px = coArr[0];
		py = coArr[1];
		pid = pNode.json.id;
		 //document.write(pNode.level + "," +pNode.json.id+" | ");
		var children = pNode.getChildren();
		var lInc = false;
		for (var i = 0; i < children.length; i++) {
			if (i == 0) {
				level++;
			}
			if (!children[i].visited) {
				children[i].visited = true;
				children[i].level = level;
				children[i].pid = pid;
				children[i].px = px;
				children[i].py = py;
				children[i].i = i;
				children[i].sCount = children.length;
				queue.push(children[i]);

			}
		}
	}

}

wso2vis.s.chart.raphael.DependencyTree.prototype.populateData = function (thisObject) {
	// Space Tree can only be drawn with a JSON Tree i.e. with JSON nodes
	this._dataField = thisObject.traverseToDataField(thisObject.data, thisObject.dataField());
	if ((this._dataField instanceof Array) && (this._dataField.length < 1)) {
		return false;
	}

	return true;

};


wso2vis.s.chart.raphael.DependencyTree.prototype.update = function () {
	if (this.populateData(this)) {
		this.paper.clear();
		var root = new Node(this._dataField[0]);
		root.level = 0;
		this.DrawTreeBFS(this.paper, root);
	}

}
