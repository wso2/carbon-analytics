package org.eclipse;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.lowagie.text.html.HtmlEncoder;
import com.lowagie.text.pdf.codec.Base64.OutputStream;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporterParameter;
import net.sf.jasperreports.j2ee.servlets.ImageServlet;




public class abServlet extends HttpServlet implements Servlet {

	private Connection con = null;
	private Map<String, Object> parameters;
	private String reportName;
	private String reportPath ; 
	
		
	
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		getConnection();
	    parameters = new HashMap<String, Object>();
	    ServletContext sc = getServletContext();
		reportPath = sc.getRealPath("/WEB-INF/reports");
		setParameters(req);
		
		try {
	            JasperReport jasperReport;
	           PrintWriter pw = resp.getWriter();
	           if(reportPath != null)
	        	   jasperReport = JasperCompileManager.compileReport(reportPath+"/" +reportName);
	           else
	            	 jasperReport = JasperCompileManager.compileReport("/"+reportName);
		           
	            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport,parameters, con);
			   
	            
	            resp.setContentType("text/html");
	            
	            JRHtmlExporter htmlExporter = new JRHtmlExporter();
	            HttpSession session =  req.getSession();
	            session.setAttribute(ImageServlet.DEFAULT_JASPER_PRINT_SESSION_ATTRIBUTE,jasperPrint);
	            htmlExporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
	            htmlExporter.setParameter(JRExporterParameter.OUTPUT_WRITER,pw);
	            //avoid using small images for aligning
	            htmlExporter.setParameter(JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN, false);
	            //specify the resource that is used to send the images to the browser 
	            htmlExporter.setParameter(JRHtmlExporterParameter.IMAGES_URI,"servlets/image?image=");
	            htmlExporter.exportReport();
	          //  JasperExportManager.exportReportToHtmlFile(sourceFileName);
	              pw.flush();
	            pw.close();
	    		} catch (JRException e) {
	    			
	    			log(e.toString()); 
					e.printStackTrace();
				}
	    
	}  
	
	/**
	 * convert the given value to the corresponding month
	 * @param monthVal value corresponding to a specific month
	 * @return month corresponding to the value 
	 */
	private String getMonth(int monthVal){
		String month = "January";
		switch (monthVal) {
		case 0:
			month = "January";
			break;
		case 1:
			month = "Februaty";
			break;
		case 2:
			month = "March";
			break;
		case 3:
			month = "April";
			break;
		case 4:
			month = "May";
			break;
		case 5:
			month = "June";
			break;
		case 6:
			month = "July";
			break;
		case 7:
			month = "August";
			break;
		case 8:
			month = "September";  
			break;
		case 9:
			month = "October";
			break;
		case 10:
			month = "November";
			break;
		case 11:
			month = "December";
			break;
		}
		
		return month;
	}
	
	
	/**
	 * get the database connection
	 */
	private void getConnection(){
		  try {
			        Class.forName("com.mysql.jdbc.Driver");
			        con = DriverManager.getConnection("jdbc:mysql://localhost:3306/testdb","root","root");
			   }catch( Exception e ){
		   	        e.printStackTrace();
		       }
	}
	
	/**
	 * set parameters of the reports
	 * @param req HttpServletRequest
	 */
	private void setParameters(HttpServletRequest req){
		parameters.put("monthspassed",24144);
		parameters.put("m1",24145);
		parameters.put("m2",24146);
		parameters.put("m3",24144);
		parameters.put("m4",24144);
		reportName = "test.jrxml";
//	    parameters.put(parameterMonthspassed, 1); 
		
//		String reportType = req.getParameter("view");
//		//parameters for the first report
//		if(reportType.equals("view"))
//		{
//			int month = Integer.parseInt(req.getParameter("month1"));
//			int year = Integer.parseInt(req.getParameter("year1"));	
//			int monthspassed = year*12 + month;
//			parameters.put(parameterMonth, getMonth(month)+"  "+ year); 
//		    parameters.put(parameterMonthspassed, 1); 
//		    parameters.put(parameterSubreportDir, reportPath+"/");
//			reportName = "tone.jrxml";
//		}
//		//parameters for the second report
//		else if(reportType.equals(" view"))
//		{
//			int numberOfMonths = Integer.parseInt(req.getParameter("NoOfMonths"));
//			int year = Integer.parseInt(req.getParameter("year2"));	
//			int month = Integer.parseInt(req.getParameter("month2"));
//			int yearTop = Integer.parseInt(req.getParameter("year3"));	
//			int monthTop = Integer.parseInt(req.getParameter("month3"));
//			int monthspassed = yearTop*12 + monthTop;
//			parameters.put(parameterMonth, getMonth(month)+"  "+ year); 
//			parameters.put(parameterMonthspassed, 1); 
//		    parameters.put(parameterMonth1, 1); 
//		    parameters.put(parameterMonth2, 1); 
//		    parameters.put(parameterMonth3, 2); 
//		    parameters.put(parameterMonth4, 1); 
//		    parameters.put(parameterMonth5, 3); 
//		    parameters.put(parameterMonth6, 1); 
//		    parameters.put(parameterMonth7, 1); 
//		    parameters.put(parameterMonth8, 4); 
//		    parameters.put(parameterMonth9, 1); 
//		    parameters.put(parameterMonth10, 1); 
//		    parameters.put(parameterMonth11, 1); 
//		    parameters.put(parameterMonth12, 1); 
//		    parameters.put(parameterSubreportDir, reportPath+"/");   
////		    int numberOfMonths = Integer.parseInt(req.getParameter("NoOfMonths"));
////			int year = Integer.parseInt(req.getParameter("year2"));	
////			int month = Integer.parseInt(req.getParameter("month2"));
////			int yearTop = Integer.parseInt(req.getParameter("year3"));	
////			int monthTop = Integer.parseInt(req.getParameter("month3"));
////			int monthspassed = yearTop*12 + monthTop;
////		    parameters.put("month", getMonth(month)+"  "+ year); 
////			parameters.put("monthspassed", monthspassed); 
////		    int start = year*12 + month;
////			for (int i = 0; i < numberOfMonths; i++) {
////				parameters.put("m"+i+1, start++);
////				
////			}
//		 reportName = "report2.jrxml";
	//	}
	}
	}

		
