<jsp:include page="includes/header.jsp" />

<div class="row">
	<div class="panel panel-default">
		<div class="panel-body">
			<form class="form-horizontal" action="viewDataset.jsp">
			<fieldset>

			<!-- Form Name -->
			<legend>Create New Dataset</legend>

			<!-- Text input-->
			<div class="form-group">
			  <label class="col-md-4 control-label" for="textinput">Id</label>  
			  <div class="col-md-4">
			  <input id="textinput" name="textinput" type="text" placeholder="Unique ID for this Dataset" class="form-control input-md">
			    
			  </div>
			</div>

			<!-- Text input-->
			<div class="form-group">
			  <label class="col-md-4 control-label" for="textinput">Display Name</label>  
			  <div class="col-md-4">
			  <input id="textinput" name="textinput" type="text" placeholder="Friendly name for the Dataset" class="form-control input-md">
			  <span class="help-block">E.g Sales By Region</span>  
			  </div>
			</div>

			<!-- Select Basic -->
			<!-- <div class="form-group">
			  <label class="col-md-4 control-label" for="selectbasic">Type</label>
			  <div class="col-md-4">
			    <select id="selectbasic" name="selectbasic" class="form-control">
			      <option value="1">Realtime</option>
			      <option value="2">Batch</option>
			      <option value="">File</option>
			    </select>
			  </div>
			</div> -->

			<!-- Select Basic -->
			<div class="form-group">
			  <label class="col-md-4 control-label" for="selectbasic">Datasource</label>
			  <div class="col-md-4">
			    <select id="selectbasic" name="selectbasic" class="form-control">
			      <option value="1">SalesData</option>
			      <option value="2">AccountsMaster</option>
			      <option value="">SensorStream</option>
			      <option value="">TrafficStream</option>
			    </select>
			  </div>
			</div>

			<!-- Multiple Checkboxes -->
			<div class="form-group">
			  <label class="col-md-4 control-label" for="checkboxes">Required Columns</label>
			  <div class="col-md-4">
			  <div class="checkbox">
			    <label for="checkboxes-0">
			      <input type="checkbox" name="checkboxes" id="checkboxes-0" value="1">
			      OrderId
			    </label>
				</div>
			  <div class="checkbox">
			    <label for="checkboxes-1">
			      <input type="checkbox" name="checkboxes" id="checkboxes-1" value="2">
			      OrderDate
			    </label>
				</div>
			  <div class="checkbox">
			    <label for="checkboxes-2">
			      <input type="checkbox" name="checkboxes" id="checkboxes-2" value="">
			      Item
			    </label>
				</div>
			  <div class="checkbox">
			    <label for="checkboxes-3">
			      <input type="checkbox" name="checkboxes" id="checkboxes-3" value="">
			      Amount
			    </label>
				</div>
			  <div class="checkbox">
			    <label for="checkboxes-4">
			      <input type="checkbox" name="checkboxes" id="checkboxes-4" value="">
			      Location
			    </label>
				</div>
			  </div>
			</div>

			<!-- Textarea -->
			<div class="form-group">
			  <label class="col-md-4 control-label" for="textarea">Filter(Optional)</label>
			  <div class="col-md-4">                     
			    <textarea class="form-control" id="textarea" name="textarea">Insert a Lucene query here</textarea>
			  </div>
			</div>

			<!-- Button (Double) -->
			<div class="form-group">
			  <label class="col-md-4 control-label" for="button1id"></label>
			  <div class="col-md-8">
			    <button id="button1id" name="button1id" class="btn btn-default">Cancel</button>
			    <button id="button2id" name="button2id" class="btn btn-success">Save</button>
			  </div>
			</div>

			</fieldset>
			</form>


		</div>
	</div>
</div>

<jsp:include page="includes/footer.jsp" />

<script type="text/javascript">

	$("#ds-grid .grid-item").click(function (e) {
		console.log(e); 
	});

</script>