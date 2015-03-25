<!-- </div> -->
  <!-- /wrapper -->

  </div>
  <!-- /container -->

  <!-- New Dashboard creation modal -->
  <div class="modal fade" id="mdlDashboard" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
    <div class="modal-dialog">
      <div class="modal-content">
        <div class="modal-header">
          <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
          <h4 class="modal-title" id="myModalLabel">Create a new dashboard</h4>
        </div>
        <div class="modal-body">
          <form>
            <div class="form-group">
              <label for="exampleInputEmail1">Dashboard Title</label>
              <input type="text" class="form-control" id="dashboardTitle">
            </div>
            <div class="form-group">
              <label for="exampleInputPassword1">Group</label>
              <input type="text" class="form-control" id="dashboardGroup">
            </div>
          </form>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
          <button id="btnSave" type="button" class="btn btn-primary">Save</button>
        </div>
      </div>
    </div>
  </div>

  <script src="assets/js/jquery.min.js"></script>
  <script src="assets/js/bootstrap.min.js"></script>
  <script src="assets/js/jquery.gridster.min.js"></script>
  <script src="assets/js/handlebars-v3.0.0.js"></script>
  <script src="assets/js/d3.min.js"></script>
  <script src="assets/js/vega.js"></script>
  <script src="assets/js/igviz.js"></script>
  <script src="assets/js/dashboard.js"></script>
  <script src="assets/js/widgetgen.js"></script>

  <script type="text/javascript">
      $("#btnSave").on("click",function () {
        var request = {
          "action" : "addDashboard",
          "title" : $("#dashboardTitle").val(),
          "group" : $("#dashboardGroup").val()
        };
        $.post( "/carbon/analytics-dashboard/ajax/dashboards_ajaxprocessor.jsp", request,function( data ) {
          console.log("POST sent to server. Received data " + data); 
          window.location.reload();
          $('#mdlDashboard').modal('hide');
        });
        
      });

      
  </script>

  
</body>
</html>