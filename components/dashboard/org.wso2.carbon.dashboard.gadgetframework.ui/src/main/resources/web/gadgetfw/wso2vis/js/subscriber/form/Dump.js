/**
 * @class Dump
 * @extends Subscriber
 */
wso2vis.s.form.Dump = function() { 
    wso2vis.s.Subscriber.call(this);
};

wso2vis.extend(wso2vis.s.form.Dump, wso2vis.s.Subscriber);

wso2vis.s.form.Dump.prototype.property("canvas");

wso2vis.s.form.Dump.prototype.create = function() {
    var newElementHTML = '<div id="treecontainer"><div id="div1"></div></div>'
    + '   <div id="recordframe">'
    + '	    <div id="div2"></div>'
    + '	    <div id="div3"></div>'
    + '	    <div id="div4"></div>'
    + '	</div>';    
    return newElementHTML;
};

wso2vis.s.form.Dump.prototype.pushData = function(data) {
    var canvas = document.getElementById(this.canvas());
    canvas.innerHTML = this.create();
    var str = JSON.stringify(data);
    var r = BCJT.tree.init(str, "div1", {"rootNode": "json", "index": 0,"newtree":false, "nodeclick": function(p){
	if (p.jsonPath == "json"){
	}else{				
		document.getElementById("div2").innerHTML = "Path: " + p.jsonPath;
		document.getElementById("div4").innerHTML = "Value: " + p.jsonValue;
		document.getElementById("div3").innerHTML = "Type: " + p.jsonType;
	}				   
    }});   
};

wso2vis.s.form.Dump.prototype.load = function() {    var canvas = document.getElementById(this.canvas());    canvas.innerHTML = "data comes here!";};
wso2vis.s.form.Dump.prototype.unload = function() {    var canvas = document.getElementById(this.canvas());    canvas.innerHTML = "";};

/*
	Most of the following code is taken from http://braincast.nl/samples/jsoneditor/ , author unknown - the site is down. :)
*/
var BCJT = function(){
	return{
		info: {"version": "1.1", "www": "http://braincast.nl", "date": "april 2008", "description": "Braincast Json Tree object."},
		util: function(){
			function addLoadEvent(func){var oldonload = window.onload;if (typeof window.onload != 'function'){window.onload = func;}else{window.onload = function(){if(oldonload){oldonload();}func();};}}
			function addEvent(obj, type, fn){if (obj.attachEvent){obj['e'+type+fn] = fn;obj[type+fn] = function(){obj['e'+type+fn](window.event);};obj.attachEvent('on'+type, obj[type+fn]);}else{obj.addEventListener(type, fn, false);}}
			function getElementById(strid){return document.getElementById(strid);}
			return{addLoadEvent: addLoadEvent,addEvent: addEvent, $: getElementById};
		}(),
		tree: function(){
			var treeIndex = 0;
			var li = 0;
			function TreeUtil(){
				var oldA;
				function makeUrl(jsonpath, text, li, index, clickable){return (clickable) ? "<a id=\"a"+li+"\" onclick=\"BCJT.tree.forest["+index+"].getNodeValue('"+jsonpath+"', this);return false\">"+text+"</a>" : text;}
				function isTypeOf(thing){return (thing !== null) ? thing.constructor : thing;}
				function strIsTypeOf(con){switch(con){case Array: return 'array';case Object: return 'object';case Function: return 'function';case String: return 'string';case Number: return 'number';case Boolean: return 'boolean';case null: return 'null';default: return 'undeterimable type';}}
				function getParentLi(item){
					/* not used */
					return (item.nodeName == "LI") ? item.id : getParentLi(item.parentNode);
				}
				return{
					strIsTypeOf: strIsTypeOf,
					isTypeOf: isTypeOf,
					getNodeValue: function(jsonPath, aobj){
						if (aobj){
							if (isTypeOf(aobj) == String){
								aobj = document.getElementById(aobj);
							}
							if (oldA){oldA.className = "au";} aobj.className = "as"; oldA = aobj;
						}
						this.cp = "BCJT.tree.forest["+this.index+"]."+jsonPath;
						this.ca = aobj;
						this.cli = document.getElementById("li"+aobj.id.substr(1));
						var params = {"jsonPath":"", "jsonValue": "", "jsonType": null, "a":{}, li: {}};
						try{
							var jsval = eval("BCJT.tree.forest["+this.index+"]."+jsonPath);
							var typ = isTypeOf(jsval);
							var txt;			
							if (typ == Function){txt = (jsval.toSource) ? jsval.toSource() : txt = jsval;
							}else if (typ == String){txt = JSON.stringify(jsval).replace(/(^")|("$)/g, "");
							}else{
								txt = JSON.stringify(jsval);
							}
							params.jsonPath = jsonPath;
							params.jsonValue = txt;
							params.jsonType = strIsTypeOf(typ);					
							params.a = this.ca;
							params.li = this.cli;
							this.nodeclick(params);
						}catch(e){
							BCJT.tree.error = "Could not get value!<br />" + e;
						}
					},
					makeTree: function(content,dots,inside){
						var out = ""; var t;
						if(content === null){
						  	if (!inside){out += "<ul><li><s>null</s></li></ul>";}
						}else if (isTypeOf(content) == Array){
							for (var i=0; i<content.length; i++){
								dots += "["+i+"]";
								out += this.makeTree(content[i], dots, false);
								dots = dots.substr(0, dots.length - (""+i).length-2);
							}
						}else if(isTypeOf(content) == Object){
							out += "<ul>";
							for(var j in content){
								dots += "[\\'"+j+"\\']";
								t = this.makeTree(content[j], dots, true);				
								dots = dots.substr(0, dots.length - j.length - 6);
								li++;
								out+="<li id=\"li"+li+"\">"+makeUrl(dots+"[\\'"+j+"\\']", j, li, this.index, this.clickable)+" "+t;
							}
							out += "</ul>"; 
						}else if(isTypeOf(content) == String){out += "</li>";
						}else{out += "</li>";}
						return out; 
					},
					reloadTree: function(){
						li = 0;
						if (this.clickable){
							if (this.rootLink === ""){this.rootLink = "BCJT.tree.forest["+this.index+"].getNodeValue('json', this);return false";}						
							this.el.innerHTML = "<ul id=\"tree"+this.index+"\" class=\"mktree\"><li><a id=\"a0\" onclick=\""+this.rootLink+"\">"+this.rootNode+"</a><ul>"+this.makeTree(this.json, "json", false).substr(4)+"</ul></li></ul>";
						}else{this.el.innerHTML = "<ul id=\"tree"+this.index+"\" class=\"mktree\"><li>"+this.rootNode+"<ul>"+this.makeTree(this.json, "json", false).substr(4)+"</ul></li></ul>";}						
						if (this.mktree){ BCJT.mktree.processList( document.getElementById("tree"+this.index) );}
					}
				};
			}
			function Tree(json, div, params){
				if (!params){ params = {};}
				var options = {"json":json,"nodeclick":function(){}, "mktree": true, "clickable": true, "index": treeIndex, "el": document.getElementById(div), "cp":null, "ca":null,"cli":null, rootNode: "json", rootLink: "", "newtree": true};
				for (var key in options){this[key] = (params[key] !== undefined) ? params[key] : options[key];}
				if (this.newtree){
					if (this.clickable){
						if (this.rootLink === ""){this.rootLink = "BCJT.tree.forest["+this.index+"].getNodeValue('json', this);return false";}
						this.el.innerHTML = "<ul id=\"tree"+treeIndex+"\" class=\"mktree\"><li><a id=\"a0\" onclick=\""+this.rootLink+"\">"+this.rootNode+"</a><ul>"+this.makeTree(json, "json", false).substr(4)+"</ul></li></ul>";
					}else{this.el.innerHTML = "<ul id=\"tree"+treeIndex+"\" class=\"mktree\"><li>"+this.rootNode+"<ul>"+this.makeTree(json, "json", false).substr(4)+"</ul></li></ul>";}
					BCJT.tree.forest.push(this);
					treeIndex++;
				}else{
					if (this.clickable){
						if (this.rootLink === ""){this.rootLink = "BCJT.tree.forest["+this.index+"].getNodeValue('json', this);return false";}
						this.el.innerHTML = "<ul id=\"tree"+this.index+"\" class=\"mktree\"><li><a id=\"a0\" onclick=\""+this.rootLink+"\">"+this.rootNode+"</a><ul>"+this.makeTree(json, "json", false).substr(4)+"</ul></li></ul>";
					}else{this.el.innerHTML = "<ul id=\"tree"+this.index+"\" class=\"mktree\"><li>"+this.rootNode+"<ul>"+this.makeTree(json, "json", false).substr(4)+"</ul></li></ul>";}
					li = 0;
					BCJT.tree.forest[this.index] = this;
				}
				if (this.mktree){ BCJT.mktree.processList( document.getElementById("tree"+this.index) );}
				return this;
			}
			Tree.prototype = new TreeUtil();
			return{
				forest: [],
				_tree: Tree, /* expose the internal Tree object for prototype purposes */
				init: function(json, div, params){
					try{
						var j = (json.constructor === Object) ? json : eval('(' +json+ ')');
						new Tree(j, div, params);
						return true;
					}catch(e){
						BCJT.tree.error = "Build tree failed!<br />" + e;
						return false;
					}
				},
				error: ""
			};
		}(),
		mktree: function(){
			/* All below code was obtained from: http://www.javascripttoolbox.com/lib/mktree/ 
			   the autor is: Matt Kruse (http://www.mattkruse.com/)
			   (The code below was slightly modified!)
			*/
			var nodeClosedClass ="liClosed", nodeOpenClass = "liOpen", nodeBulletClass = "liBullet", nodeLinkClass = "bullet";
			
			/* the two below functions will prevent memory leaks in IE */
			function treeNodeOnclick(){this.parentNode.className = (this.parentNode.className==nodeOpenClass) ? nodeClosedClass : nodeOpenClass;return false;}	
			function retFalse(){return false;}		
			function processList(ul){
				if (!ul.childNodes || ul.childNodes.length===0){return;}
				var childNodesLength = ul.childNodes.length;
				for (var itemi=0;itemi<childNodesLength;itemi++){
					var item = ul.childNodes[itemi];
					if (item.nodeName == "LI"){
						var subLists = false;
						var itemChildNodesLength = item.childNodes.length;
						for (var sitemi=0;sitemi<itemChildNodesLength;sitemi++){
							var sitem = item.childNodes[sitemi];
							if (sitem.nodeName=="UL"){subLists = true; processList(sitem);}
						}
						var s = document.createElement("SPAN");
						var t = '\u00A0';
						s.className = nodeLinkClass;
						if (subLists){
							if (item.className===null || item.className===""){item.className = nodeClosedClass;}
							if (item.firstChild.nodeName=="#text") {t = t+item.firstChild.nodeValue; item.removeChild(item.firstChild);}
							s.onclick = treeNodeOnclick;
						}else{item.className = nodeBulletClass; s.onclick = retFalse;}
						s.appendChild(document.createTextNode(t));
						item.insertBefore(s,item.firstChild);
					}
				}
			}
			// Performs 3 functions:
			// a) Expand all nodes
			// b) Collapse all nodes
			// c) Expand all nodes to reach a certain ID
			function expandCollapseList(ul,nodeOpenClass,itemId){
				if (!ul.childNodes || ul.childNodes.length===0){return false;}
				for (var itemi=0;itemi<ul.childNodes.length;itemi++){
					var item = ul.childNodes[itemi];
					if (itemId!==null && item.id==itemId){return itemId;}
					if (item.nodeName == "LI"){
						var subLists = false;
						for (var sitemi=0;sitemi<item.childNodes.length;sitemi++){
							var sitem = item.childNodes[sitemi];
							if (sitem.nodeName=="UL"){
								subLists = true;
								var ret = expandCollapseList(sitem,nodeOpenClass,itemId);
								if (itemId!==null && ret){item.className=nodeOpenClass; return itemId;}
							}
						}
						if (subLists && itemId===null){item.className = nodeOpenClass;}
					}
				}
			}
			// Full expands a tree with a given ID
			function expandTree(treeId) {
			  var ul = document.getElementById(treeId);
			  if (ul === null) { return false; }
			  expandCollapseList(ul,nodeOpenClass);
			}
			
			// Fully collapses a tree with a given ID
			function collapseTree(treeId) {
			  var ul = document.getElementById(treeId);
			  if (ul === null) { return false; }
			  expandCollapseList(ul,nodeClosedClass);
			}
			
			// Expands enough nodes to expose an LI with a given ID
			function expandToItem(treeId,itemId) {
			  var ul = document.getElementById(treeId);
			  if (ul === null) { return false; }
			  var ret = expandCollapseList(ul,nodeOpenClass,itemId);			  
			  if (ret) {
			    var o = document.getElementById(itemId);
			    if (o.scrollIntoView) {
			      o.scrollIntoView(false);
			    }
			  }
			}
			return{
				processList: processList,
				expandCollapseList: expandCollapseList,
				expandTree: expandTree,
				collapseTree: collapseTree,
				expandToItem: expandToItem
			};
		}()
	};
}();









