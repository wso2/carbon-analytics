<jsp:include page="includes/header.jsp" />



<div class="modal fade" id="mdlDVInfo" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content" id="dvContentMdl">
      
  </div>
</div>

<jsp:include page="includes/footer.jsp" />

<script type="text/javascript">
	
	var dashboardList = [];

	$(document).ready(function() {

		$.getJSON("/dashboard/servlet/dataview", function( data ) {
			var source   = $("#tplDataViews").html();
			var template = Handlebars.compile(source);
			$("#container").append(template({ dataviews : data}));
			appendFooter();

			//load the DVContent for the first item in the list
			renderDVContent($("#dvList a:first-child").attr("data-dataview")); 

			//Start binding UI events from here onwards
			$('#dvList a').click(function () {
				var dataview = $(this).attr('data-dataview');
				currentDv=dataview;
				console.log("Loading DV content for " + dataview);
				renderDVContent(dataview);
			});
			
		});





	});	//end of document.ready()

	function renderDVContent(dataview) {
		console.log("reloading..."); 
		var dv = {name:"foo",type:"bar",datasource:"bax",widgets:[{id:"1234ew",title:"Sales By Region"}]};
		// var dv = {name:"foo",type:"bar",datasource:"bax",widgets:[]};
		var dashboards = [];	
		//fetch all dashboards. They are required to populate the dropdown list
		$.getJSON("/dashboard/servlet/dashboard", function( data ) {
			data.forEach(function (d,i) {
				dashboards.push(d);
			});
		});

		//fetch DV content
		var request = {
			"action" : "getDataViewById",
			"dataViewId" : dataview
		};
		
		$.getJSON("/dashboard/servlet/dataview",request,function (data) {
			// var dashboards = [];
			var source   = $("#tplDVInfo").html();
			var template = Handlebars.compile(source);
			$("#dvContent").empty();

			var viewContext = {
				dataview:data,
				widgets:data.widgets,
				dashboards:dashboards
			};	

			$("#dvContent").append(template(viewContext));
			$("#dvTitle").empty().append(data.name + " Overview");

			//For each widget in a dataview, confiugre add to dashboard
			$(".dropdown-menu li a").click(function (event) {
				var widgetId = $(this).attr('data-widget');
				var dashboard = $(this).attr('data-dashboard');
				console.log("Adding widget " + widgetId +" to dashboard " + dashboard);
				addToDashboard(dashboard,widgetId);
			});


			var dataTable = {}
			function setType(str) {
			    if (str == 'STRING' || str == 'BOOL')
			        return 'C'
			    else
			        return 'N'
			}
			dataTable.metadata = {types: [], names: []}
			dataTable.data = [];
			typeList = [];
			for (i = 0; i < data.columns.length; i++) {
			    dataTable.metadata.names.push(data.columns[i].name);
			    dataTable.metadata.types.push(setType(data.columns[i].type))
			    typeList.push(data.columns[i].type)
			}

			// dataTable.data = writeSample(typeList);
			dataTable.data = [
			    [0, 'Books', 'Colombo', "A", "3", 100],
			    [1, 'Pens', 'Kandy', "B", "4", 500],

			    [2, 'Pencils', 'Gampaha', "C", "5", 300],

			    [3, 'Papers', 'Jaffna', "D", "7", 400],

			    [4, 'Highlighter', 'Gall', "E", "6", 200],
			]
			updateWidgetList(data,dataTable);

		});
	};

	function appendFooter() {
		var source   = $("#footer").html();
		var template = Handlebars.compile(source);

		$("#container").append(template());
	};

	function addToDashboard(dashboardId,widgetId) {
		var request = {
			"action" : "addWidget",
			"dashboardId" : dashboardId,
			"widgetId" : widgetId
		};
		$.post( "/dashboard/servlet/dashboard", request,function( data ) {
		  console.log("POST sent to server. Received data " + data); 
		});

		window.location.href = "http://localhost:8080/dashboard";

	};

	function addWidgetToDataView (widget,dataview) {
		console.log("Adding widget [" + widget.title + "] to DataView " + dataview);
		//call the backend 
		
		var widgetX = {
			id:generateId(),
			title:widget.title,
			config:JSON.stringify(widget.config)
		};
		console.log(widgetX); 

		var request = {
			"action" : "addWidget",
			"dataview" : dataview,
			"widgetDefinition" : JSON.stringify(widgetX)
		};
		$.post( "/dashboard/servlet/dataview", request,function( data ) {
		  console.log("Widget added to DV"); 
		  //load the DV content again
		  renderDVContent(dataview);
		});

		
	};

	function generateId()
	{
	    var text = "";
	    var possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

	    for( var i=0; i < 5; i++ )
	        text += possible.charAt(Math.floor(Math.random() * possible.length));

	    return text;
	}


</script>

<script id="tplDataViews" type="text/x-handlebars-template">
	<div class="panel panel-default">
		<div class="panel-heading">
		    <h2 class="panel-title">Choose Widget(s) from a DataView 
		    <span class="pull-right"><a href="index.jsp">Back to Dashboard</a></span></h2>

		  </div>
	    <div class="panel-body">
	    	{{#if dataviews}}
		        <div class="col-md-3">
		        	<div class="list-group" id="dvList">
		        		{{#each dataviews}}
			              <a class="list-group-item" href="#" data-dataview="{{id}}">
			              	<i class="fa fa-history fa-fw"></i>
			              	{{name}}
			               </a>
				        {{/each}}  
			        </div>
		        </div>
		        <div class="col-md-9">
		        	<div class="panel panel-default" id="dvInfo">
		        		<div class="panel-heading">
    				    	<h2 class="panel-title" id="dvTitle"></h2>
    				    </div>
    				    <div class="panel-body" id="dvContent"></div>
		        	</div>
		        </div>
		    {{else}}
		    	<div class="blank-slate-message">
		    	    <h2>You have not created any DataViews.</h2>
		    	    <p>Get started by creating a new DataView.</p>
		    	    <a href="#">
		    	        <button data-toggle="modal" data-target="#mdlDVInfo" type="button" class="btn btn-success">Create New DataView</button>
		    	    </a>
		    	</div>
		    {{/if}}

	    </div>
	</div>
</script>

<script id="tplDVInfo" type="text/x-handlebars-template">
	<span class="pull-right"><button type="button" class="btn btn-success addNew">New Widget</button></span>
	<h4>Properties</h4>
	<table class="table">
	      <tbody>
	        <tr>
	          <th scope="row">Name</th>
	          <td>{{dataview.name}}</td>
	        </tr>
	        <tr>
	          <th scope="row">Type</th>
	          <td>{{dataview.type}}</td>
	        </tr>
	        <tr>
	          <th scope="row">Datasource</th>
	          <td>{{dataview.datasource}}</td>
	        </tr>
	        <tr>
	          <th scope="row">Filter</th>
	          <td>{{dataview.filter}}</td>
	        </tr>
	        <tr>
	          <th scope="row">Columns</th>
	          <td>{{dataview.columns}}</td>
	        </tr>
	      </tbody>
	</table>
	{{#if widgets}}
		<h4>Associated Widgets</h4>
        <div class="row">
        	<div class="container-fluid">
        		{{#each widgets}}
		          <div class="col-sm-6 col-md-6">
		            <div class="thumbnail">
		              <div class="caption">
		                <h4>{{title}}</h4>

		                <button type="button" class="btn btn-default" id="btnTrash">
		                	<span class="glyphicon glyphicon-trash" aria-hidden="true"></span>
		                </button>
		                <button type="button" class="btn btn-default" id="btnCog">
		                	<span class="glyphicon glyphicon-cog" aria-hidden="true"></span>
		                </button>

		                <div class="btn-group" role="group">
		                  <button type="button" class="btn btn-success dropdown-toggle" 
		                  data-toggle="dropdown"  aria-expanded="false">
		                    Add to Dashboard
		                    <span class="caret"></span>
		                  </button>
		                  <ul class="dropdown-menu" role="menu">
		                  	{{#each ../dashboards}}
		                    	<li><a href="#" data-widget="{{../../dataview.id}}.{{../id}}" data-dashboard="{{id}}">{{title}}</a></li>
		                    {{/each}}
		                  </ul>
		                </div>

		               </div>

		              </div>
		            </div>
		          </div>
		        {{/each}}  
        	</div>
        </div>
    {{else}}
    	<div class="blank-slate-message">
    	    <h4>This DataView has no widgets associated.</h4>
    	    <p class="text-muted">Get started by creating a new Widget with DataView data</p>
    	        <button type="button" class="btn btn-success addNew">Create New Widget</button>
    	</div>
    {{/if}}
</script>

<script id="footer" type="text/x-handlebars-template">
	<hr>
	<p class="small text-muted">Built with &#9829; by <a href="https://wso2.com">WSO2</a></p>
</script>

<div id="myModal" class="modal fade bs-example-modal-lg" tabindex="-1" role="dialog"
     aria-labelledby="myLargeModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="myModalLabel">Modal title</h4>
            </div>
            <div class="modal-body">

                <div class="col-md-12">


                    <div class="row">
                        <div class="col-md-6">
                            <p>Preview :</p>

                            <div id="chartDiv"></div>

                        </div>

                        <div class="col-md-6">
                            <p>Configuration :</p>

                            <div id="Attributeform">

                                <div id="hiddenForm" class="col-md-11">
                                    <form class="form-horizontal">

                                        <div class="form-group" id="chartTypeSelection">
                                            <label for="chartType" class="col-sm-6 control-label">Chart
                                                Type </label>

                                            <div class="col-sm-6">
                                                <select class="form-control" id="chartType"
                                                        name="chartType">
                                                    <option value="">--Select--</option>
                                                    <option value="bar">Bar</option>
                                                    <option value="line">Line</option>
                                                    <option value="area">Area</option>
                                                    <option value="stackedBar">StackedBar</option>
                                                    <option value="groupedBar">GroupedBar</option>
                                                    <option value="stackedArea">StackedArea</option>
                                                    <option value="multiArea">MultiArea</option>
                                                    <option value="scatter">Scatter</option>
                                                </select>
                                            </div>
                                        </div>
                                        <div class="form-group attr bar line area stackedBar groupedBar stackedArea multiArea scatter">
                                            <label for="title"
                                                   class="col-sm-6 control-label">Title</label>

                                            <div class="col-sm-6">
                                                <input name='title' type="text" class="form-control"
                                                       id="title" placeholder="title">
                                            </div>
                                        </div>

                                        <div class="form-group attr var bar line area stackedBar groupedBar stackedArea multiArea scatter">
                                            <label for="xAxis"
                                                   class="col-sm-6 control-label">X-Axis </label>

                                            <div class="col-sm-6">
                                                <select class="form-control" id="xAxis"
                                                        name="xAxis">
                                                </select>
                                            </div>
                                        </div>


                                        <div class="form-group var attr bar area stackedBar groupedBar stackedArea  scatter">
                                            <label for="yAxis"
                                                   class="col-sm-6 control-label">Y-Axis </label>

                                            <div class="col-sm-6">
                                                <select class="form-control" id="yAxis"
                                                        name="yAxis">
                                                </select>
                                            </div>
                                        </div>

                                        <div class="form-group var  attr line multiArea">
                                            <label for="yAxises" class="col-sm-6 control-label">Y-Axis </label>

                                            <div class="col-sm-6">
                                                <select multiple class="form-control" id="yAxises"
                                                        name="yAxis">

                                                </select>
                                            </div>
                                        </div>
                                        <div class="form-group var attr stackedBar groupedBar stackedArea ">
                                            <label for="groupedBy" class="col-sm-6 control-label">GroupedBy </label>

                                            <div class="col-sm-6">
                                                <select class="form-control" id="groupedBy"
                                                        name="groupedBy">
                                                </select>
                                            </div>
                                        </div>

                                        <div class="form-group var scatter attr">
                                            <label for="pointColor" class="col-sm-6 control-label">Color
                                                variable </label>

                                            <div class="col-sm-6">
                                                <select class="form-control" id="pointColor"
                                                        name="pointColor">
                                                </select>
                                            </div>
                                        </div>


                                        <div class="form-group var scatter attr">
                                            <label for="pointSize" class="col-sm-6 control-label">Color
                                                variable </label>

                                            <div class="col-sm-6">
                                                <select class="form-control" id="pointSize"
                                                        name="pointSize">
                                                </select>
                                            </div>
                                        </div>
                                        <div class="form-group  scatter attr">
                                            <label for="minColor" class="col-sm-6 control-label">Color
                                                For Max value </label>

                                            <div class="col-sm-6">
                                                <input type="color" class="form-control"
                                                       id="minColor" name="minColor">

                                            </div>
                                        </div>

                                        <div class="form-group  scatter attr">
                                            <label for="maxColor" class="col-sm-6 control-label">Color
                                                For Min Value </label>

                                            <div class="col-sm-6">
                                                <input type="color" class="form-control"
                                                       id="maxColor" name="maxColor">

                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <div class="col-sm-offset-2 col-sm-6">
                                                <a id="preview" class="btn btn-default">Preview</a>
                                            </div>
                                        </div>

                                    </form>
                                </div>

                            </div>
                        </div>

                    </div>
                    <div class="row">

                        <p>SampleData :</p>

                        <div id="data">

                        </div>
                    </div>

                </div>

            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">Close
                </button>
                <button id="modalSave" type="button" class="btn btn-primary">Save changes</button>
            </div>
        </div>

    </div>
</div>