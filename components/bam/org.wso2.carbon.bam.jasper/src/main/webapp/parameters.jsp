<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@taglib uri="http://java.sun.com/jstl/core_rt" prefix="c"%>
<%@taglib uri="http://java.sun.com/jstl/fmt_rt" prefix="fmt"%>


<HTML>
	<HEAD>
		<TITLE>OT ANALYTICS</TITLE>
		<link rel="stylesheet" href="templatemo_style.css">
	</HEAD>


	<jsp:useBean id="now" class="java.util.Date" />
	<fmt:formatDate var="currentYear" value="${now}" pattern="yyyy" />

	<BODY>
		<body>
	<div id="templatemo_container">
		<div id="templatemo_header">
        	<div id="templatemo_logo">
            OT ANALYTICS</div>

            <div id="templatemo_menu">
                  <div class="templatemo_menu_item">
                        <div class="templatemo_menu_home">
                            <a href="#" class="current">HOME</a>
                        </div>
                  </div>
                  <div class="templatemo_menu_item">
                        <div class="templatemo_menu_content">
                            <a href="#">Contact</a>
                        </div>
                  </div>



                  <div class="last">
                        <div class="templatemo_menu_member">
                            <a href="#">About Us</a>
                        </div>
              	  </div>


            </div>
        </div>

        <div class="templatemo_h_line"></div>


        	<h1>REPORT TYPES</h1>



<FORM action="/jasper/abServlet" method="post">
		Top ten users, organizations and tags of the month of
			<SELECT name="month1">
				<OPTION value="0">January</OPTION>
				<OPTION value="1">February</OPTION>
				<OPTION value="2">March</OPTION>
				<OPTION value="3">April</OPTION>
				<OPTION value="4">May</OPTION>
				<OPTION value="5">June</OPTION>
				<OPTION value="6">July</OPTION>
				<OPTION value="7">August</OPTION>
				<OPTION value="8">September</OPTION>
				<OPTION value="9">October</OPTION>
				<OPTION value="10">November</OPTION>
				<OPTION value="11">December</OPTION>
			</SELECT>

			<SELECT name="year1">
				<c:forEach begin="2011" end="${currentYear}" step="1" var="year1">
					<OPTION value="${year1}">${year1}</OPTION>
				</c:forEach>
			</SELECT>

<Br>

			<INPUT type="submit" name="view" value="view" class="button">

			<Br><Br><Br>

			Access history of top ten users, organizations and tags of the month
			<SELECT name="month3">
				<OPTION value="0">January</OPTION>
				<OPTION value="1">February</OPTION>
				<OPTION value="2">March</OPTION>
				<OPTION value="3">April</OPTION>
				<OPTION value="4">May</OPTION>
				<OPTION value="5">June</OPTION>
				<OPTION value="6">July</OPTION>
				<OPTION value="7">August</OPTION>
				<OPTION value="8">September</OPTION>
				<OPTION value="9">October</OPTION>
				<OPTION value="10">November</OPTION>
				<OPTION value="11">December</OPTION>
			</SELECT>

			<SELECT name="year3">
				<c:forEach begin="2011" end="${currentYear}" step="1" var="year3">
					<OPTION value="${year3}">${year3}</OPTION>
				</c:forEach>
			</SELECT>
			<Br>over
			<SELECT name="NoOfMonths">
				<OPTION value="1">1</OPTION>
				<OPTION value="2">2</OPTION>
				<OPTION value="3">3</OPTION>
				<OPTION value="4">4</OPTION>
				<OPTION value="5">5</OPTION>
				<OPTION value="6">6</OPTION>
				<OPTION value="7">7</OPTION>
				<OPTION value="8">8</OPTION>
				<OPTION value="9">9</OPTION>
				<OPTION value="10">10</OPTION>
				<OPTION value="11">11</OPTION>
				<OPTION value="12">12</OPTION>
			</SELECT>
			months   started from:
				<SELECT name="month2">
				<OPTION value="0">January</OPTION>
				<OPTION value="1">February</OPTION>
				<OPTION value="2">March</OPTION>
				<OPTION value="3">April</OPTION>
				<OPTION value="4">May</OPTION>
				<OPTION value="5">June</OPTION>
				<OPTION value="6">July</OPTION>
				<OPTION value="7">August</OPTION>
				<OPTION value="8">September</OPTION>
				<OPTION value="9">October</OPTION>
				<OPTION value="10">November</OPTION>
				<OPTION value="11">December</OPTION>
			</SELECT>

			<SELECT name="year2">
				<c:forEach begin="2011" end="${currentYear}" step="1" var="year2">
					<OPTION value="${year2}">${year2}</OPTION>
				</c:forEach>
			</SELECT>

			<Br>

			<INPUT type="submit" name="view" value=" view" class="button">
		</FORM>















          </div>





           <Br><Br><Br><Br><Br><Br><Br><Br><Br><Br>

        <div id="templatemo_footer">
                Copyright © 2024 <a href="http://www.templatemo.com/page/1" target="_blank">Your Company Name</a> | Designed by <a href="http://www.templatemo.com/page/1" target="_blank">Free CSS Templates</a>		</div>

</div><!-- End Of Container -->
    <div style="text-align: center; font-size: 0.75em;">Design Downloaded from <a href="http://www.template4all.com/">free website templates</a> | <a href="http://www.freethemes4all.com/">free css templates</a> | <a href="http://www.seodesign.us/" title="free wallpapers hd" target="_blank">Free Wallpapers HD</a>.</div>
    <div id="templatemo_bottom"></div>
<!--  Free CSS Templates by TemplateMo.com  -->
</body>
</html>