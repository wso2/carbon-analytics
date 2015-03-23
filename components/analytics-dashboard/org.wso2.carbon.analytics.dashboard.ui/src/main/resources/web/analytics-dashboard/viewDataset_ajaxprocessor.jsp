<jsp:include page="includes/header.jsp" />

<div class="row">
	<h3>Sales 2015</h3>
	<div class="panel panel-default">
		<div class="panel-body">
				<h4>Summary</h4>
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
					<h4>Associated Widgets</h4>
			        <div class="blank-slate-message">
			            <h4>This DataView has no widgets associated.</h4>
			            <p class="text-muted">Get started by creating a new Widget with DataView data</p>
			                <button data-toggle="modal" data-target="#myModal" type="button" class="btn btn-success addNew">Create New Widget</button>
			        </div>
		</div>
	</div>
</div>

<jsp:include page="includes/footer.jsp" />

<script type="text/javascript">

	$("#ds-grid .grid-item").click(function (e) {
		console.log(e); 
	});

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