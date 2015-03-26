<jsp:include page="includes/header.jsp" />
<jsp:include page="includes/footer.jsp" />

<script type="text/javascript">
  var bindingSource = new dashboard.BindingSource();

  var POLLING_INTERVAL = 5000;

  $("#menu-toggle").click(function(e) {
      e.preventDefault();
      $("#wrapper").toggleClass("toggled");
  });

	$(document).ready(function() {
		$.getJSON("/carbon/analytics-dashboard/ajax/dashboards_ajaxprocessor.jsp", function( data ) {
			if(data.length == 0) {
				console.log("No dashboards"); 
				showNoDashboards();
			} else {
				$("#container").remove();
				var source   = $("#tplDashboards").html();
				var template = Handlebars.compile(source);
				$("body").append(template());
				drawSidebar(data);

			}
				appendFooter();
		});
	});


	function drawSidebar(data) {
		  var sidebar = $("#sidebar");
		  var listGroup = jQuery('<div/>', {
		      class: 'list-group'
		  });

	    data.forEach(function (dashboard,index) {
	      var item = jQuery('<a/>', {
	        class: 'list-group-item',
	        id: dashboard.id,
	        href: '#',
	        title: dashboard.title,
	        text: dashboard.title
	      }).appendTo(listGroup);

	      item.on('click',function (e) {
	          drawDashboard(dashboard.id);
	      });
	      if(index == 0 ) {
	        drawDashboard(dashboard.id);
	      }
	    });

	  	listGroup.appendTo(sidebar);
	};

  function drawDashboard(dashboardId) {
    	var request = {
    		"action" : "getDashboardById",
    		"dashboardId" : dashboardId
    	};
      	$.getJSON("/carbon/analytics-dashboard/ajax/dashboards_ajaxprocessor.jsp",request,function (data) {
	        if(data.widgets && data.widgets.length!=0) {
        		var title = data.title;
	        	var widgets = data.widgets;
	        	//Remove the data from the #canvans so that we can redraw the widgets
	        	$("#canvas").empty();
	        	$("#canvas").removeData();

	        	var gridster = $("#canvas").gridster( {
	        	    widget_base_dimensions: [100, 100],
	        	    widget_margins: [5, 5],
	        	    min_cols: 1,
	        	    max_cols: 8,
	        	    avoid_overlapped_widgets: false,
	        	    autogenerate_stylesheet: true

	        	}).data('gridster');

	        	//draw each widget
	        	for (var i = 0; i < widgets.length; i++) {
	        	  var widget = new Widget(widgets[i]);
	        	  drawWidget(gridster,widget);
	        	};
	        } else {
	        	console.log("no wodgets");
	        	var source   = $("#tplNoWidgets").html();
	        	var template = Handlebars.compile(source);

	        	$("#widget-container").empty();
	        	$("#widget-container").append(template());
	        }

      });

      console.log(bindingSource); 
    
  };
   
  function drawWidget(gridster,widget) {
    console.log("+++ Drawing widget " + widget.id); 
    var request = {
      "action" : "getWidget",
      "dataview" : widget.dataview,
      "widgetId" : widget.id,
    };
    $.getJSON("/dashboard/servlet/dataview", request,function (data) {
      var dataTable = new igviz.DataTable();
      data.columns.forEach(function (element,index) {
          var type = 'N';
          if(element.type == 'STRING' || element.type == 'BOOL') {
              type = 'C';
          }
          dataTable.addColumn(element.name,type);
      });

      //Create the widget canvas first
      var source   = $("#tplWidget").html();
      var template = Handlebars.compile(source);
      var templateCtxt = { "title" : data.widget.title, "id" : data.widget.id };
      gridster.add_widget(template(templateCtxt),widget.dimensions.width, widget.dimensions.height);

      //do some width hiegth manipulation
      data.widget.config.width = widget.dimensions.width * 100;        
      data.widget.config.height = widget.dimensions.height * 100;     

      // console.log(data); 

      // var chart = igviz.plot("#" + data.widget.id,data.widget.config,dataTable);
      var chart = igviz.setUp("#" + data.widget.id,JSON.parse(data.widget.config),dataTable);

      bindingSource.addWidget(data.id,chart);

      //check for widgets type and set the data access strategy.
      //E.g Poll if type set to batch, subscribe for WS if type set to realtime
      //request data for this widget and register a callback to process them later
      $.getJSON("data/data/" + data.id + ".json",function (d) {
        console.log("+++ Received data for widget " + widget.id); 
        chart.plot(d.data);
      });
      
      
    });

  };

	function showNoDashboards() {
		var source   = $("#tplNoDashboards").html();
		var template = Handlebars.compile(source);

		$("#container").append(template());
	}

	function appendFooter() {
		var source   = $("#footer").html();
		var template = Handlebars.compile(source);

		$("#container").append(template());
	}

  //now fetch all channels and keep pushing data into BindingSource
  //This is a way to constantly update all widgets in a dashboard
  // setInterval(function(){ 
  //   // console.log("Hello"); 
  //   bindingSource.channels.forEach(function(channel,i) {
  //     console.log("+++ Fetching data for [ " + channel.name + " ]");
  //     $.getJSON("data/data/updates/" + channel.name + ".json",function (d) {
  //       bindingSource.onDataReceived(d,true);
  //     });
  //   });
  // }, POLLING_INTERVAL);

  // $("#pusher").click(function() {
  //   console.log("+++ inside pusher"); 
  //   var data = {
  //     "dataview" : "dv1",
  //     "data" : [
  //         [0,"Books","Colombo","A","50",900],
  //         [1,"Pens","Kandy", "B","4",100],
  //         [2,"Pencils","Gampaha","C","5", 200],
  //         [3, "Papers","Jaffna","D","70", 600],
  //         [4, "Highlighter","Galle","E","66",400],
  //         [5,"Folders","Colombo","F","84",900],
  //         [6,"Clips","Nuwaraeliya","F","90",50],
  //         [7,"Puncher","Kurunegala","F","10",250],
  //         [8,"Stapeler","Monaragala","F","9",400],
  //         [9,"Foo","Ampara","F","11",200],
  //         [10,"Bar","Colombo","F","12",300]
  //     ]
  //   };
  //    bindingSource.onDataReceived(data,true);

  // }); 

function fetchData() {
  var url = "http://localhost:9763/analytics/tables/expenses";
  $.getJSON(url,function(data) {
    console.log(data); 
  });
};



</script>

<script id="tplDashboards" type="text/x-handlebars-template">
	<div id="wrapper">

	  <!-- Sidebar -->
	  <div id="sidebar-wrapper">

	      <div class="panel panel-default">
	        <div class="panel-body" id="sidebar">

	        </div>
	      </div>
	  </div>
	  <!-- /#sidebar-wrapper -->

	  <!-- Page Content -->
	  <div id="page-content-wrapper">
	      <div class="container-fluid">
	          <div class="row" id="widget-container">

	              <div  class="gridster">
	                <ul id="canvas"></ul>
	              </div>

	          </div>
	          <!-- /row -->

	         

	      </div>  
	      <!-- /container -->
	  </div>
	  <!-- /#page-content-wrapper -->


	</div>
	<!-- /wrapper -->
</script>

<script id="tplNoDashboards" type="text/x-handlebars-template">
  <div class="panel panel-default">
      <div class="panel-body">
          <div class="blank-slate-message">
              <h2>You have not created any Dashboards yet.</h2>
              <p>Get started by creating a new Dashboard.</p>
              <a href="#">
                  <button data-toggle="modal" data-target="#mdlDashboard" type="button" class="btn btn-success">Create New Dashboard</button>
              </a>
          </div>

      </div>
  </div>
</script>

<script id="tplNoWidgets" type="text/x-handlebars-template">
  <div class="panel panel-default">
      <div class="panel-body">
          <div class="blank-slate-message">
              <h2>This Dashboard has no widgets.</h2>
              <p>Browse for widgets and add to Dashboard.</p>
              <a href="dataviews.jsp">
                  <button type="button" class="btn btn-success">Add Widget</button>
              </a>
          </div>

      </div>
  </div>
</script>

<script id="footer" type="text/x-handlebars-template">
	<hr>
	<p class="small text-muted">Built with &#9829; by <a href="https://wso2.com">WSO2</a></p>
</script>

<script id="tplWidget" type="text/x-handlebars-template">
  <li>
    <div class="chart-wrapper">
      <div class="chart-title">
        {{title}}
      </div>
      <div class="chart-stage" id="{{id}}">
      </div>
    </div>
  </li>
</script>
