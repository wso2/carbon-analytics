package org.wso2.carbon.bam.gadgetgenwizard.service;

import org.apache.axiom.om.*;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.jaxp.OMSource;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.bam.gadgetgenwizard.internal.GGWUtils;
import org.wso2.carbon.bam.gadgetgenwizard.service.beans.*;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.ndatasource.core.CarbonDataSource;
import org.wso2.carbon.registry.common.services.RegistryAbstractAdmin;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.core.AuthorizationManager;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class GadgetGenService extends RegistryAbstractAdmin {

    public static final String JAGGERY_APP_GENERATOR_XSLT = "jaggery-app-generator.xslt";

    public static final String GADGET_GENERATOR_XSLT = "gadget-generator.xsl";

    // CSS Files
    private static final String JQPLOT_CSS = "gadgetgen/css/jquery.jqplot.min.css";
    private static final String DATA_TABLES_CSS = "gadgetgen/css/jquery.dataTables.css";

    // JS Files
    private static final String JQUERY_JS = "gadgetgen/js/jquery.min.js";
    private static final String JQPLOT_JS = "gadgetgen/js/jquery.jqplot.min.js";
    private static final String EXCANVAS_JS = "gadgetgen/js/excanvas.min.js";
    private static final String BARGRAPH_JS = "gadgetgen/js/plugins/jqplot.barRenderer.js";
    private static final String CATEGORY_AXIS_JS = "gadgetgen/js/plugins/jqplot.categoryAxisRenderer.js";
    private static final String DATA_TABLES_JS = "gadgetgen/js/jquery.dataTables.min.js";

    // Image Files
    private static final String BACK_DISABLED_PNG = "gadgetgen/images/back_disabled.png";
    private static final String BACK_ENABLED_PNG = "gadgetgen/images/back_enabled.png";
    private static final String BACK_ENABLED_HOVER_PNG = "gadgetgen/images/back_enabled_hover.png";
    private static final String FORWARD_DISABLED_PNG = "gadgetgen/images/forward_disabled.png";
    private static final String FORWARD_ENABLED_PNG = "gadgetgen/images/forward_enabled.png";
    private static final String FORWARD_ENABLED_HOVER_PNG = "gadgetgen/images/forward_enabled_hover.png";
    private static final String SORT_ASC_PNG = "gadgetgen/images/sort_asc.png";
    private static final String SORT_ASC_DISABLED_PNG = "gadgetgen/images/sort_asc_disabled.png";
    private static final String SORT_BOTH_PNG = "gadgetgen/images/sort_both.png";
    private static final String SORT_DESC_PNG = "gadgetgen/images/sort_desc.png";
    private static final String SORT_DESC__DISABLED_PNG = "gadgetgen/images/sort_desc_disabled.png";

    private static final String[] GADGET_RESOURCES_JS = {EXCANVAS_JS, JQPLOT_JS, JQUERY_JS, BARGRAPH_JS, CATEGORY_AXIS_JS, DATA_TABLES_JS};
    private static final String[] GADGET_RESOURCES_CSS = {JQPLOT_CSS, DATA_TABLES_CSS};
    private static final String[] GADGET_RESOURCES_IMAGES = {BACK_DISABLED_PNG, BACK_ENABLED_PNG, BACK_ENABLED_HOVER_PNG,
            FORWARD_DISABLED_PNG, FORWARD_ENABLED_PNG, FORWARD_ENABLED_HOVER_PNG, SORT_ASC_PNG, SORT_ASC_DISABLED_PNG,
            SORT_BOTH_PNG, SORT_DESC_PNG, SORT_DESC__DISABLED_PNG};

    private static final Log log = LogFactory.getLog(GadgetGenService.class);

//    public static final String GADGETGEN_JAG_FILEPATH = GADGET_GEN_APP_DIR + File.separator + "gadgetgen.jag";

    private static final String GADGETGEN_COMP_REG_PATH = "repository/components/org.wso2.carbon.bam.gadgetgen/";


    public String createGadget(WSMap map) throws GadgetGenException {
        OMDocument intermediateXML = createIntermediateXML(map);
        String gadgetXMLPath = applyXSLTForGadgetXML(intermediateXML);
        applyXSLTForJaggeryScript(intermediateXML);
        copyGadgetResourcesToRegistry(GADGETGEN_COMP_REG_PATH);
        return getServerURL(map)+"registry/resource"+gadgetXMLPath;
    }

    public String[] getDataSourceNames() throws GadgetGenException {
        try {
            List<CarbonDataSource> allDataSources = GGWUtils.getDataSourceService().getAllDataSources();
            List<String> dataSourceNames = new ArrayList<String>();
            for (CarbonDataSource carbonDataSource : allDataSources) {
                DBConnInfo dbConnInfo;
//                dbConnInfo.setJdbcURL(carbonDataSource.getDSMInfo().getDefinition().getDsXMLConfiguration());
                dataSourceNames.add(carbonDataSource.getDSMInfo().getName());
            }
            return dataSourceNames.toArray(new String[dataSourceNames.size()]);
        } catch (DataSourceException e) {
            String errorMsg = "Unable to retrieve data sources. " + e.getMessage();
            log.error(errorMsg, e);
            throw new GadgetGenException(errorMsg, e);
        }

    }

    public WSResultSet executeQuery(DBConnInfo dbConnInfo, String sqlQuery) throws GadgetGenException {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            Class.forName(dbConnInfo.getDriverClass()).newInstance();
            connection = DriverManager.getConnection(dbConnInfo.getJdbcURL(), dbConnInfo.getUsername(), dbConnInfo.getPassword());
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sqlQuery);
            ResultSetMetaData metaData = resultSet.getMetaData();

            WSResultSet wsResultSet = new WSResultSet();
            int columnCount = metaData.getColumnCount();
            wsResultSet.setColumnCount(columnCount);
            List<String> columnNames = new ArrayList<String>();


            List<WSRow> rows = new ArrayList<WSRow>();
            // loop if both less than 10 times and if there are no results
            for (int i = 0; (i < 10 && resultSet.next()); i++) {
                List<String> rowList = new ArrayList<String>();
                for (int j = 1; j < (columnCount + 1); j++) {
                    // optimization: add column names also in this loop, but only once
                    if (i == 0) {
                        String columnName = metaData.getColumnName(j);
                        columnNames.add(columnName);
                    }
                    rowList.add(resultSet.getString(j));
                }
                WSRow wsRow = new WSRow();
                wsRow.setRow(rowList.toArray(new String[rowList.size()]));
                rows.add(wsRow);
            }
            wsResultSet.setRows(rows.toArray(new WSRow[rows.size()]));
            wsResultSet.setColumnNames(columnNames.toArray(new String[columnNames.size()]));
            return wsResultSet;
        } catch (InstantiationException e) {
            String errorMsg = "The class cannot be instantiated. " + e.getMessage();
            log.error(errorMsg, e);
            throw new GadgetGenException(errorMsg, e);
        } catch (IllegalAccessException e) {
            String errorMsg = "The class does not have public constructor. " + e.getMessage();
            log.error(errorMsg, e);
            throw new GadgetGenException(errorMsg, e);
        } catch (ClassNotFoundException e) {
            String errorMsg = "The JDBC Driver Class " + dbConnInfo.getDriverClass() + " is not present in class path. " + e.getMessage();
            log.error(errorMsg, e);
            throw new GadgetGenException(errorMsg, e);
        } catch (SQLException e) {
            String errorMsg = "Cannot establish database connection. " + e.getMessage();
            log.error(errorMsg, e);
            throw new GadgetGenException(errorMsg, e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    String errorMsg = "Cannot close connection. " + e.getMessage();
                    log.error(errorMsg, e);
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    String errorMsg = "Cannot close statement. " + e.getMessage();
                    log.error(errorMsg, e);
                }
            }
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    String errorMsg = "Cannot close result set. " + e.getMessage();
                    log.error(errorMsg, e);
                }
            }
        }
    }

    public boolean validateDBConnection(DBConnInfo dbConnInfo) throws GadgetGenException {
        Connection connection = null;
        try {
            Class.forName(dbConnInfo.getDriverClass()).newInstance();
            connection = DriverManager.getConnection(dbConnInfo.getJdbcURL(), dbConnInfo.getUsername(), dbConnInfo.getPassword());
        } catch (InstantiationException e) {
            String errorMsg = "The class cannot be instantiated. " + e.getMessage();
            log.error(errorMsg, e);
            throw new GadgetGenException(errorMsg, e);
        } catch (IllegalAccessException e) {
            String errorMsg = "The class does not have public constructor. " + e.getMessage();
            log.error(errorMsg, e);
            throw new GadgetGenException(errorMsg, e);
        } catch (ClassNotFoundException e) {
            String errorMsg = "The JDBC Driver Class " + dbConnInfo.getDriverClass() + " is not present in class path. " + e.getMessage();
            log.error(errorMsg, e);
            throw new GadgetGenException(errorMsg, e);
        } catch (SQLException e) {
            String errorMsg = "Cannot establish database connection. " + e.getMessage();
            log.error(errorMsg, e);
            throw new GadgetGenException(errorMsg, e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    String errorMsg = "Cannot close connection. " + e.getMessage();
                    log.error(errorMsg, e);
                }
            }
        }
        return true;
    }

    private void copyGadgetResourcesToRegistry(String gadgetRegistryPath) throws GadgetGenException {
        List<String> gadgetResources = new ArrayList<String>();
        gadgetResources.addAll(Arrays.asList(GADGET_RESOURCES_JS));
        gadgetResources.addAll(Arrays.asList(GADGET_RESOURCES_CSS));
        gadgetResources.addAll(Arrays.asList(GADGET_RESOURCES_IMAGES));
        for (String gadgetResource : gadgetResources) {
            Resource resource = convertInputStreamToResource(this.getClass().getClassLoader().getResourceAsStream(gadgetResource));
            String gadgetResourcePath = gadgetRegistryPath + gadgetResource;
            copyResourceWithAnonymousPermission(resource, gadgetResourcePath);
        }
    }

    private void copyResourceWithAnonymousPermission(Resource resource, String gadgetResourcePath) throws GadgetGenException {
        try {
            Registry registry = getConfigSystemRegistry();

            if (!registry.resourceExists(gadgetResourcePath)) {

                registry.put(gadgetResourcePath, resource);
                AuthorizationManager authorizationManager = ((UserRegistry) getRootRegistry()).getUserRealm().getAuthorizationManager();
                authorizationManager.authorizeRole(CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME,
                        RegistryConstants.CONFIG_REGISTRY_BASE_PATH + "/" + gadgetResourcePath, ActionConstants.GET);
                // bug with permission update
                setPermissionUpdateTimestamp();
            }
        } catch (RegistryException e) {
            String msg = "Error inserting resource to registry. " + e.getMessage();
            log.error(msg, e);
            throw new GadgetGenException(msg, e);
        } catch (UserStoreException e) {
            String msg = "Cannot get authorization manager. " + e.getMessage();
            log.error(msg, e);
            throw new GadgetGenException(msg, e);
        }
    }

    private Resource convertInputStreamToResource(InputStream inputStream) throws GadgetGenException {
        try {
            if (inputStream == null) {
                throw new GadgetGenException("input stream cannot be null");
            }
            Resource resource = getConfigSystemRegistry().newResource();
            byte[] bytes = IOUtils.toByteArray(inputStream);
            resource.setContent(bytes);
            return resource;
        } catch (IOException e) {
            String msg = "Error converting file to byte array. " + e.getMessage();
            log.error(msg, e);
            throw new GadgetGenException(msg, e);
        } catch (RegistryException e) {
            String msg = "Error creating new resource. " + e.getMessage();
            log.error(msg, e);
            throw new GadgetGenException(msg, e);
        }
    }

    private void applyXSLTForJaggeryScript(OMDocument intermediateXML) throws GadgetGenException {
        String jaggery_app_dir = "";
        int  tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
            jaggery_app_dir = CarbonUtils.getCarbonRepository() + File.separator + "jaggeryapps";
        } else {
            jaggery_app_dir = CarbonUtils.getCarbonTenantsDirPath() + File.separator
                    + CarbonContext.getThreadLocalCarbonContext().getTenantId() + File.separator + "jaggeryapps";
        }
        String gadget_gen_app_dir = jaggery_app_dir + File.separator + "gadgetgen";
        try {
            File gadgetGenAppDir = new File(gadget_gen_app_dir);
            if (!gadgetGenAppDir.exists()) {
                FileUtils.forceMkdir(gadgetGenAppDir);
            }
            String gadgetFileName = getGadgetFileName(intermediateXML);
            File gadgetGenFile = new File(gadget_gen_app_dir + File.separator + gadgetFileName + ".jag");
            if (gadgetGenFile.exists()) {
                FileUtils.forceDelete(gadgetGenFile);
            }
            applyXSLT(intermediateXML, FileUtils.openOutputStream(gadgetGenFile), JAGGERY_APP_GENERATOR_XSLT);

        } catch (FileNotFoundException e) {
            String error = "XSLT file not found. This should be in the classpath. " + e.getMessage();
            log.error(error, e);
            throw new GadgetGenException(error, e);
        } catch (IOException e) {
            String error = "Error creating directory structure for jaggery app. " + e.getMessage();
            log.error(error, e);
            throw new GadgetGenException(error, e);
        }

    }

    private String getGadgetFileName(OMDocument intermediateXML) throws GadgetGenException {
        OMElement gadgetFileNameEl = intermediateXML.getOMDocumentElement().
                getFirstChildWithName(new QName("http://wso2.com/bam/gadgetgen", "gadget-filename", "gg"));
        if (gadgetFileNameEl == null) {
            String errorMsg = "No gadget file name in intermediate xml" + intermediateXML.getOMDocumentElement().toString();
            GadgetGenException gadgetGenException = new GadgetGenException(errorMsg);
            log.error(errorMsg, gadgetGenException);
            throw gadgetGenException;
        }

        String gadgetFileName = gadgetFileNameEl.getText();
        if (!gadgetFileName.matches("[_a-zA-Z0-9\\-\\.]+")) {
            String errorMsg = "Invalid file name for gadget : " + gadgetFileName;
            GadgetGenException gadgetGenException = new GadgetGenException(errorMsg);
            log.error(errorMsg, gadgetGenException);
            throw gadgetGenException;
        }

        return gadgetFileName;
    }

    private void applyXSLT(OMDocument intermediateXML, OutputStream outputStream, String xsltFileNameInClassPath) throws GadgetGenException {
        try {
            StreamResult result = new StreamResult(outputStream);

            InputStream xsltStream = this.getClass().getClassLoader().getResourceAsStream(xsltFileNameInClassPath);

            StAXOMBuilder stAXOMBuilder = new StAXOMBuilder(xsltStream);

            Source xsltSource = new OMSource(stAXOMBuilder.getDocumentElement());
            Source xmlSource = new OMSource(intermediateXML.getOMDocumentElement());


            TransformerFactory transFact =
                    TransformerFactory.newInstance();
            Transformer transformer = transFact.newTransformer(xsltSource);
            transformer.transform(xmlSource, result);
        } catch (XMLStreamException e) {
            String error = "XML error reading XSLT file. " + e.getMessage();
            log.error(error, e);
            throw new GadgetGenException(error, e);
        } catch (TransformerException e) {
            String error = "XSLT transformation error during Code generation. " + e.getMessage();
            log.error(error, e);
            throw new GadgetGenException(error, e);
        }
    }

    private String applyXSLTForGadgetXML(OMDocument intermediateXML) throws GadgetGenException {
        Resource resource;
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            applyXSLT(intermediateXML, byteArrayOutputStream, GADGET_GENERATOR_XSLT);
            Registry configSystemRegistry = getConfigSystemRegistry();
            resource = configSystemRegistry.newResource();
            resource.setContent(byteArrayOutputStream.toByteArray());
            String gadgetXMLPath = GADGETGEN_COMP_REG_PATH + "gadgetgen/" + getGadgetFileName(intermediateXML) + ".xml";
            String completeGadgetPath = RegistryConstants.CONFIG_REGISTRY_BASE_PATH + "/" + gadgetXMLPath;
            // if resource exists, send an error and do not continue
            if (configSystemRegistry.resourceExists(gadgetXMLPath)) {
                throw new GadgetGenException("Choose a different gadget name. Gadget already exists at " +
                        completeGadgetPath);
            }
            copyResourceWithAnonymousPermission(resource, gadgetXMLPath);
            return completeGadgetPath;
        } catch (RegistryException e) {
            String error = "Error creating resource. " + e.getMessage();
            log.error(error, e);
            throw new GadgetGenException(error, e);
        }
    }

    private OMDocument createIntermediateXML(WSMap map) {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMDocument omDocument = factory.createOMDocument();
        OMNamespace gadgetgenNamespace = factory.createOMNamespace("http://wso2.com/bam/gadgetgen", "gg");
        OMElement rootElement = factory.createOMElement("gadgetgen", gadgetgenNamespace);
        WSMapElement[] wsMapElements = map.getWsMapElements();
        boolean barChartFound = false;
        boolean tableFound = false;
        OMElement barChart = factory.createOMElement("BarChart", gadgetgenNamespace);
        OMElement table = factory.createOMElement("Table", gadgetgenNamespace);

        for (WSMapElement wsMapElement : wsMapElements) {
            // check for bar graph properties
            if (wsMapElement.getKey().startsWith("bar")) {
                barChartFound = true;
                OMElement omElement = factory.createOMElement(wsMapElement.getKey(), gadgetgenNamespace);
                omElement.setText(wsMapElement.getValue());
                barChart.addChild(omElement);
            } else if (wsMapElement.getKey().startsWith("table")) {
                tableFound = true;
                OMElement omElement = factory.createOMElement(wsMapElement.getKey(), gadgetgenNamespace);
                omElement.setText(wsMapElement.getValue());
                table.addChild(omElement);
            } else if (wsMapElement.getKey().startsWith("jaggeryAppUrl")) {
                OMElement omElement = factory.createOMElement(wsMapElement.getKey(), gadgetgenNamespace);
                omElement.setText(getJaggeryFileUrl(wsMapElement.getValue()));
                rootElement.addChild(omElement);
            }

            OMElement omElement = factory.createOMElement(wsMapElement.getKey(), gadgetgenNamespace);
            omElement.setText(wsMapElement.getValue());
            rootElement.addChild(omElement);
        }
        if (barChartFound) {
            rootElement.addChild(barChart);
        }
        if (tableFound) {
            rootElement.addChild(table);
        }

        omDocument.addChild(rootElement);
        return omDocument;
    }

    private String getJaggeryFileUrl(String serverUrl) {
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
            return serverUrl+"gadgetgen/";
        } else {
            return serverUrl + "jaggeryapps/gadgetgen/";
        }
    }

    private String getServerURL(WSMap map) {
        WSMapElement[] wsMapElements = map.getWsMapElements();
        String stringUrl = "";
        for (WSMapElement wsMapElement : wsMapElements) {
           if (wsMapElement.getKey().startsWith("jaggeryAppUrl")) {
                stringUrl= wsMapElement.getValue();
               break;
           }
        }
        try {
            URL url = new URL(stringUrl);
             if (url.getPort() == -1) {
                //ie.port is not set in the url
                if (url.getProtocol().equalsIgnoreCase("https")) {
                    return "http://" + url.getHost() + url.getPath();
                } else {
                    return stringUrl;
                }
            } else {
//            String portOffset = CarbonUtils.getServerConfiguration().
//                        getFirstProperty("Ports.Offset");
              int  port =  CarbonUtils.
                      getTransportPort(GGWUtils.getConfigurationContextService().
                              getServerConfigContext(), "http");
            return "http://"+url.getHost()+":"+port+url.getPath();
             }
        } catch (MalformedURLException e) {
            return "";
        }
    }
}

