<%

  
  // session.setAttribute("loggedIn", false);
  if(request.getParameter("action") != null) {
    out.println("sjdskjdskdjskdj");
  }


%>

<!DOCTYPE html>
<html>
<head>
  <title>WSO2 Analytics Dashboard</title>
  <link rel="stylesheet" href="assets/css/bootstrap.min.css">
  <link rel="stylesheet" href="assets/css/keen-dashboards.css">
  <link rel="stylesheet" href="assets/css/simple-sidebar.css">
  <!-- <link rel="stylesheet" href="assets/css/jquery.gridster.min.css"> -->
  <link rel="stylesheet" href="assets/css/main.css">
</head>

<body class="application">

  <div class="navbar navbar-inverse navbar-fixed-top" role="navigation">
    <div class="container-fluid">
      <div class="navbar-header">
        <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
          <span class="sr-only">Toggle navigation</span>
          <span class="icon-bar"></span>
          <span class="icon-bar"></span>
          <span class="icon-bar"></span>
        </button>
        <a class="navbar-brand" href="../">
          <span class="glyphicon glyphicon-stats"></span>
        </a>
        <a class="navbar-brand" href="./">Analytics Dashboard</a>
      </div>
      <div class="navbar-collapse collapse">
        <ul class="nav navbar-nav navbar-left">
          <li>
            <button type="button" class="btn btn-default navbar-btn" id="menu-toggle">
              <span class="glyphicon glyphicon-menu-hamburger" aria-hidden="true"></span>
            </button>
          </li>
          <li><a href="http://localhost/dashboard" title="Add Widget"><span class="glyphicon glyphicon glyphicon-th-large"></span></a></li>
          <li><a href="http://localhost/dashboard" title="New Dashboard"><span class="glyphicon glyphicon-plus-sign"></span></a></li>
          <li><a href="http://localhost/dashboard" title="Edit Dashboard"><span class="glyphicon glyphicon-pencil"></span></a></li>
          <li><a href="http://localhost/dashboard" title="Delete Dashboard"><span class="glyphicon glyphicon-remove-circle"></span></a></li>
        </ul>

        <ul class="nav navbar-nav navbar-right">
          <li><a href="../navbar/">Help</a></li>
          <li class="dropdown">
            <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-expanded="false">Dunith Dan<span class="caret"></span></a>
            <ul class="dropdown-menu" role="menu">
              <li><a href="#">Preferences</a></li>
              <li class="divider"></li>
              <li class="dropdown-header">Nav header</li>
              <li><a href="#">Sign Out</a></li>
            </ul>
        </li>
        </ul>
      </div>
    </div>
  </div>

  <div class="container">
    <form class="signin" action="./login.jsp?action=login">
      <div class="form-group">
        <label for="exampleInputEmail1">Username</label>
        <input type="text" class="form-control" id="userName">
      </div>
      <div class="form-group">
        <label for="exampleInputPassword1">Password</label>
        <input type="password" class="form-control">
      </div>
      <button type="submit" class="btn btn-default">Login</button>
    </form>
  </div>


  <script src="assets/js/jquery.min.js"></script>
  <script src="assets/js/bootstrap.min.js"></script>
  <script src="assets/js/d3.min.js"></script>
  <script src="assets/js/vega.js"></script>
  <script src="assets/js/igviz.js"></script>
  <!-- // <script src="assets/js/jquery.gridster.min.js"></script> -->

  <!-- Menu Toggle Script -->
  <script>
    $("#menu-toggle").click(function(e) {
        e.preventDefault();
        $("#wrapper").toggleClass("toggled");
    });

    
    $("#login").click(function(e){
      //invoke backend API to get authenticated
      var data = {
              user: "Donald Duck",
              city: "Duckburg"
      };
      $.post("login.jsp?action=login",data, function(data, status){
              
      });

    });


  


  </script>

</body>
</html>
