<jsp:include page="includes/header.jsp" />

<div class="row">
	<div class="panel panel-default">
		<div class="panel-body">
			<form class="form-horizontal">
			<fieldset>

			<!-- Form Name -->
			<legend>Create New Dataview</legend>

			<div id="error-alert" class="alert alert-danger alert-dismissible" role="alert">
			  <button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>
			  <strong>Something went wrong!</strong><p id="errorText"></p>
			</div>

			<!-- Text input-->
			<div class="form-group">
			  <label class="col-md-4 control-label" for="textinput">Display Name</label>  
			  <div class="col-md-4">
			  <input id="txtName" name="textinput" type="text" placeholder="Friendly name for the Dataset" class="form-control input-md">
			  <span class="help-block">E.g Sales By Region</span>  
			  </div>
			</div>

			<!-- Select Basic -->
			<div class="form-group">
			  <label class="col-md-4 control-label" for="selectbasic">Datasource</label>
			  <div class="col-md-4">
			    <select id="dsList" name="selectbasic" class="form-control">
			     
			    </select>
			  </div>
			</div>

			<div id="batchContent">
				<!-- Multiple Checkboxes -->
				<div class="form-group" >
				  <label class="col-md-4 control-label" for="checkboxes">Required Columns</label>
				  <div class="col-md-4" id="cols">
				  </div>
				</div>

				<!-- Textarea -->
				<div class="form-group">
				  <label class="col-md-4 control-label" for="textarea">Filter(Optional)</label>
				  <div class="col-md-4">                     
				    <textarea class="form-control" id="txtFilter" name="textarea"></textarea>
				    <span class="help-block">E.g Sales By Region</span> 
				  </div>
				</div>
			</div>

            <div id="realTimeContent">
                <!-- Multiple Checkboxes -->
                <div class="form-group" >
                    <label class="col-md-4 control-label" for="checkboxes">Available Stream Attributes</label>
                        <div class="col-md-4" id="colsRealTime"></div>
                </div>
            </div>

			<!-- Button (Double) -->
			<div class="form-group">
			  <label class="col-md-4 control-label" for="button1id"></label>
			  <div class="col-md-8">
			    <button type="button" id="btnCancel" name="button1id" class="btn btn-default">Cancel</button>
			    <button type="button" id="btnSaveDV" name="button2id" class="btn btn-success">Save</button>
			  </div>
			</div>

			</fieldset>
			</form>


		</div>
	</div>
</div>

<jsp:include page="includes/footer.jsp" />

<script type="text/javascript">
	
	$("#btnCancel").click(function(e){
		e.preventDefault();
		window.location.href = "/carbon/analytics-dashboard/dataviews_ajaxprocessor.jsp";
	});

	$(document).ready(function () {
		$("#error-alert").hide();
	});

	$("#batchContent").hide();
    $("#realTimeContent").hide();

	var request = {action : "getTables"};
	$.getJSON("/carbon/analytics-dashboard/ajax/analytics_ajaxprocessor.jsp",request,function (data) {
		$("#dsList").empty();
		$("#dsList").append($('<option/>').val("-1")
			.html("--Select a Datasource--")
			.attr("type","-1")
		);
		data.forEach(function(datasource,i){
			var item = $('<option></option>')
			                        .val(datasource.name)
			                        .html(datasource.name)
			                        .attr("data-type",datasource.type);
			$("#dsList").append(item);
		});
	});

	$("#dsList").change(function() {
		var item = this;
		var dsList = $("#dsList option:selected");
		var type = dsList.attr("data-type");

		if(dsList.val() != "-1" && type == "batch") {
			console.log("Selected datasource: " + type); 
			$("#batchContent").show();
			// var columns = [{"name" : "Year","type" : "string"},{"name" : "Expenses","type" : "int"}];
			fetchColumns(dsList.val());
		} else if(dsList.val() != "-1" && type == "realTime"){
		 //construct the columns list if the datasource type is "realTime"
            $("#realTimeContent").show();
            fetchStreamDefinitonDetails(dsList.val())

        }
		

	});

	function fetchColumns(table) {
		console.log("Fetching table schema for table: " + table); 
		var request = {
			"action" : "getSchema",
			"table" : table
		};
		$.getJSON("/carbon/analytics-dashboard/ajax/analytics_ajaxprocessor.jsp",request,function (columns) {
			$("#cols").empty();
			columns.forEach(function(col,i){
				console.log(col.name + col.type); 
				var div = $('<div/>').attr("class","checkbox");
				var label = $("<label/>").attr("for",col.name).appendTo(div);
				var input = $("<input/>").attr("type","checkbox")
				.attr("name","columns").attr("value",col.name)
				.attr("id",col.name).attr("data-type",col.type)
				.appendTo(label);
				label.append(col.name);
				$("#cols").append(div);
			});
		});
	};

    function fetchStreamDefinitonDetails(streamId){

        var request = {action : "getStreamDefinition",streamId : streamId};

        $.getJSON("/carbon/analytics-dashboard/ajax/analytics_ajaxprocessor.jsp",request,function (data) {
            $("#colsRealTime").empty();
            data.forEach(function(col,i){
                console.log(col.name + col.type);
                var div = $('<div/>').attr("class","checkbox");
                var label = $("<label/>").attr("for",col.name).appendTo(div);
                var input = $("<input/>").attr("type","checkbox")
                        .attr("name","columns").attr("value",col.name)
                        .attr("id",col.name).attr("data-type",col.type)
                        .prop("checked",true)
                        .attr('readonly',true)
                        .appendTo(label);
                label.append(col.name);
                $("#colsRealTime").append(div);
            });
        });
    }

	$("#btnSaveDV").click(function(e){
		var name = $("#txtName").val();
		var type = $("#dsList option:selected").attr("data-type");
        var uiPublisherAdapterAlreadyExists = false;
		if(!name) {
			alert("Dataview must have a unique name!");
			return false;
		}
		if(!type) {
			alert("Please select a datasource");
			return false;
		}
		var datasource = $("#dsList option:selected").text();
		
		var dataview = {id:generateId(),name : name,type: type,dataSource:datasource};
		//construct the columns list if the datasource type is "batch"
		if(type=="batch") {
			var columns = [];
			var checked = $('input[name="columns"]:checked'); 
			if(checked.size() >= 2) {
				checked.each(function() {
					// console.log(this.attr("data-type")); 
				   columns.push({
				   	"name" : this.value,
				   	"type" : "string"
				   });
				});	
				dataview.columns = columns;
			} else {
				alert("Please select atleast two columns");
				return false;
			}
			var filter = $("#txtFilter").text();
			dataview.filter = filter;
		} else if(type == "realTime"){ //construct the columns list if the datasource type is "realTime"

            var columns = [];
            var checked = $('input[name="columns"]:checked');
            checked.each(function() {
                // console.log(this.attr("data-type"));
                columns.push({
                    "name" : this.value,
                    "type" : "string"
                });
            });
            dataview.columns = columns;

            //deploying event Publsiher for selected Stream
            var request = {action : "deployOutputAdapterUIPublisher",streamId : datasource};

            $.getJSON("/carbon/analytics-dashboard/ajax/analytics_ajaxprocessor.jsp",request,function (data) {

                if(!data.isDeployedSuccess){
                    console.log(data.message);
                }

            });
        }
		console.log(dataview);
		//finally send the created DataView object to server
		$.ajax({
		   type: 'POST',
		   url: '/carbon/analytics-dashboard/ajax/dataviews_ajaxprocessor.jsp?action=addDataView',
		   data: {"definition" : JSON.stringify(dataview)},
		   async : false,
		   error: function (request, status, error) {
		   		if(status=="error") {
		   		   $("#errorText").html(request.responseText);
		   		   $("#success-alert").hide();
		           $("#error-alert").show();
		   		}
		   },
		   success: function(data) {
		   		console.log("Data received from server: " + data); 
		   		window.location.href = "/carbon/analytics-dashboard/dataviews_ajaxprocessor.jsp";
		   		return false;
		   	 	// $("#error-alert").hide();
		   		// $("#success-alert").show();
		   }
		});
	});

	

</script>