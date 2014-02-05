<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ page import="org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext" %>
<%@ page import="org.wso2.carbon.registry.core.service.RegistryService" %>
<%@ page import="org.wso2.carbon.context.CarbonContext" %>
<%@ page import="org.wso2.carbon.registry.core.Registry" %>
<%@ page import="org.wso2.carbon.registry.core.exceptions.RegistryException" %>
<%@ page import="org.wso2.carbon.registry.core.RegistryConstants" %>
<%@ page import="org.wso2.carbon.registry.core.Resource" %>
<%@ page import="net.sf.jasperreports.engine.JasperCompileManager" %>
<%@ page import="net.sf.jasperreports.engine.JasperReport" %>
<%@ page import="net.sf.jasperreports.engine.JRException" %>
<%@ page import="java.sql.DriverManager" %>
<%@ page import="java.sql.Connection" %>
<%@ page import="net.sf.jasperreports.engine.JasperPrint" %>
<%@ page import="net.sf.jasperreports.engine.JasperFillManager" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="net.sf.jasperreports.engine.export.JRHtmlExporter" %>
<%@ page import="net.sf.jasperreports.j2ee.servlets.ImageServlet" %>
<%@ page import="net.sf.jasperreports.engine.JRExporterParameter" %>
<%@ page import="net.sf.jasperreports.engine.export.JRHtmlExporterParameter" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="org.wso2.carbon.ndatasource.core.DataSourceService" %>
<%@ page import="org.w3c.dom.Element" %>
<%@ page import="org.wso2.carbon.ndatasource.rdbms.RDBMSConfiguration" %>
<%@ page import="org.wso2.carbon.ndatasource.rdbms.RDBMSDataSourceReader" %>
<%@ page import="org.wso2.carbon.ndatasource.core.utils.DataSourceUtils" %>

<%

    String jasperPath = "/repository/dashboards/jasper";

    RegistryService registryService = (RegistryService)
            SuperTenantCarbonContext.getCurrentContext().getOSGiService(RegistryService.class);

    DataSourceService dataSourceService =
            (DataSourceService) SuperTenantCarbonContext.getCurrentContext().
                    getOSGiService(DataSourceService.class);

    int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

    Registry registry = null;
    try {
        registry = registryService.getConfigSystemRegistry(tenantId);
    } catch (RegistryException e) {
        e.printStackTrace();
    }

    String jrxml = request.getParameter("jrxml");
    String dataSourceName = request.getParameter("datasource");

    String registryPath = jasperPath + RegistryConstants.PATH_SEPARATOR + jrxml;

    Resource jrxmlResource = null;
    try {
        jrxmlResource = registry.get(registryPath);
    } catch (RegistryException e) {
        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }

    JasperReport jasperReport = null;
    try {
        jasperReport = JasperCompileManager.compileReport(jrxmlResource.getContentStream());
    } catch (JRException e) {
        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    } catch (RegistryException e) {
        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }

    Connection con = null;
    try {
/*        Class.forName("com.mysql.jdbc.Driver");
        con = DriverManager.getConnection("jdbc:mysql://localhost:3306/testdb", "root", "root");*/

        Element element = (Element) dataSourceService.getDataSource(dataSourceName).
                getDSMInfo().getDefinition().getDsXMLConfiguration();
        RDBMSConfiguration rdbmsConfiguration = RDBMSDataSourceReader.loadConfig(
                DataSourceUtils.elementToString(element));

        Class.forName(rdbmsConfiguration.getDriverClassName());
        con = DriverManager.getConnection(rdbmsConfiguration.getUrl(),
                                          rdbmsConfiguration.getUsername(),
                                          rdbmsConfiguration.getPassword());
/*        DataSource dataSource = (DataSource) dataSourceService.getDataSource(dataSourceName)
                .getDSObject();
        con = dataSource.getConnection();*/
    } catch (Exception e) {
        e.printStackTrace();
    }

    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("monthspassed", 24144);
    parameters.put("m1", 24145);
    parameters.put("m2", 24146);
    parameters.put("m3", 24144);
    parameters.put("m4", 24144);

    JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, con);
    response.setContentType("text/html");

    JRHtmlExporter htmlExporter = new JRHtmlExporter();

    session.setAttribute(ImageServlet.DEFAULT_JASPER_PRINT_SESSION_ATTRIBUTE, jasperPrint);
    htmlExporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);

    PrintWriter pw = response.getWriter();
    htmlExporter.setParameter(JRExporterParameter.OUTPUT_WRITER, pw);
    //avoid using small images for aligning
    htmlExporter.setParameter(JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN, false);
    //specify the resource that is used to send the images to the browser
    htmlExporter.setParameter(JRHtmlExporterParameter.IMAGES_URI, "servlets/image?image=");
    htmlExporter.exportReport();
    //  JasperExportManager.exportReportToHtmlFile(sourceFileName);
    pw.flush();
    pw.close();


%>