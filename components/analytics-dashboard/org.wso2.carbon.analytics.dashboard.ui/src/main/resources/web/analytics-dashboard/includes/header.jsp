<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
  <title>WSO2 Analytics Dashboard</title>
  <link rel="stylesheet" href="assets/css/bootstrap.min.css">
  <link rel="stylesheet" href="assets/css/font-awesome.min.css">
  <link rel="stylesheet" href="assets/css/keen-dashboards.css">
  <link rel="stylesheet" href="assets/css/simple-sidebar.css">
  <link rel="stylesheet" href="assets/css/jquery.gridster.min.css">
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
          <li><a href="dataviews.jsp" title="Add Widget"><span class="glyphicon glyphicon glyphicon-th-large"></span></a></li>
          <!-- <li><a href="#" title="New Dashboard" data-toggle="modal" data-target="#mdlDashboard"><span class="glyphicon glyphicon-plus-sign"></span></a></li> -->
          <!-- <li><p class="navbar-btn">
                    <a href="#" class="btn btn-success" data-toggle="modal" data-target="#mdlDashboard">New Dashboard</a>
                </p></li> -->
          <li><a href="http://localhost/dashboard" title="Edit Dashboard"><span class="glyphicon glyphicon-pencil"></span></a></li>
          <li><a id="pusher" href="#" title="Delete Dashboard"><span class="glyphicon glyphicon-remove-circle"></span></a></li>
        </ul>

        <ul class="nav navbar-nav navbar-right">
          <li><a href="dataviews.jsp">Browse Datasets</a></li>
          <li class="dropdown">
            <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-expanded="false">Dunith<span class="caret"></span></a>
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

  <!-- <div id="wrapper"> -->
  <div class="container" id="container">