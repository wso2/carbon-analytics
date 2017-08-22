/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.analytics.test.osgi.util;

import com.google.common.io.Files;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class RDBMSConfig {

    private static final Logger log = Logger.getLogger(RDBMSConfig.class);

    private static String connectionUrlMysql = "jdbc:mysql://{{container.ip}}:{{container.port}}/WSO2_ANALYTICS_DB?useSSL=false";
    private static String connectionUrlPostgres = "jdbc:postgresql://{{container.ip}}:{{container.port}}/WSO2_ANALYTICS_DB";
    private static String connectionUrlOracle = "jdbc:oracle:thin:@{{container.ip}}:{{container.port}}/XE";
    private static String connectionUrlMsSQL = "jdbc:sqlserver://{{container.ip}}:{{container.port}};" +
            "databaseName=tempdb";
    private static final String CONNECTION_URL_H2 = "jdbc:h2:./target/WSO2_ANALYTICS_DB";

    private static final String JDBC_DRIVER_CLASS_NAME_H2 = "org.h2.Driver";
    private static final String JDBC_DRIVER_CLASS_NAME_MYSQL = "com.mysql.jdbc.Driver";
    private static final String JDBC_DRIVER_CLASS_NAME_ORACLE = "oracle.jdbc.driver.OracleDriver";
    private static final String JDBC_DRIVER_CLASS_POSTGRES = "org.postgresql.Driver";
    private static final String JDBC_DRIVER_CLASS_MSSQL = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

    private static String url = CONNECTION_URL_H2;
    private static String driverClassName = JDBC_DRIVER_CLASS_NAME_H2;
    private static String username = "root";
    private static String password = "root";

    public static void createDSFromXML() {
        RDBMSType type = getRDBMSType();
        username = System.getenv("DATABASE_USER");
        password = System.getenv("DATABASE_PASSWORD");
        String port = System.getenv("PORT");

        switch (type) {
            case MySQL:
                url = connectionUrlMysql.replace("{{container.ip}}", getIpAddressOfContainer()).
                        replace("{{container.port}}", port);
                driverClassName = JDBC_DRIVER_CLASS_NAME_MYSQL;
                break;
            case H2:
                url = CONNECTION_URL_H2;
                driverClassName = JDBC_DRIVER_CLASS_NAME_H2;
                username = "wso2carbon";
                password = "wso2carbon";
                break;
            case POSTGRES:
                url = connectionUrlPostgres.replace("{{container.ip}}", getIpAddressOfContainer()).
                        replace("{{container.port}}", port);
                driverClassName = JDBC_DRIVER_CLASS_POSTGRES;
                break;
            case ORACLE:
                url = connectionUrlOracle.replace("{{container.ip}}", getIpAddressOfContainer()).
                        replace("{{container.port}}", port);
                driverClassName = JDBC_DRIVER_CLASS_NAME_ORACLE;
                break;
            case MSSQL:
                url = connectionUrlMsSQL.replace("{{container.ip}}", getIpAddressOfContainer()).
                        replace("{{container.port}}", port);
                driverClassName = JDBC_DRIVER_CLASS_MSSQL;
                break;
        }

        generateDSXML();

    }

    /**
     * Method to create and save the master-datasources.xml
     */
    private static void generateDSXML() {
        DocumentBuilder documentBuilder = null;
        try {
            documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

            Document document = documentBuilder.newDocument();

            Element dataSourcesConfigurationRoot = document.createElement("datasources-configuration");
            Element dataSourcesRoot = document.createElement("datasources");
            Element dataSourceRoot = document.createElement("datasource");
            Element dataSourceName = document.createElement("name");
            Element dataSourceDescription = document.createElement("description");
            Element jndiConfig = document.createElement("jndiConfig");
            Element jndiConfigName = document.createElement("name");
            Element definition = document.createElement("definition");

            dataSourcesConfigurationRoot.appendChild(dataSourcesRoot);
            dataSourceName.appendChild(document.createTextNode("WSO2_ANALYTICS_DB"));
            dataSourceDescription.appendChild(document
                    .createTextNode("The datasource used for Siddhi App state persistence"));
            dataSourceRoot.appendChild(jndiConfig);
            jndiConfigName.appendChild(document.createTextNode("jdbc/WSO2_ANALYTICS_DB"));
            definition.setAttribute("type", "RDBMS");

            jndiConfig.appendChild(jndiConfigName);
            dataSourceRoot.appendChild(dataSourceName);
            dataSourceRoot.appendChild(dataSourceDescription);
            dataSourceRoot.appendChild(jndiConfig);
            Element configurationRoot = document.createElement("configuration");

            Element configURL = document.createElement("url");
            configURL.appendChild(document.createTextNode(url));
            Element configUsername = document.createElement("username");
            configUsername.appendChild(document.createTextNode(username));
            Element configPassword = document.createElement("password");
            configPassword.appendChild(document.createTextNode(password));
            Element configDriverClassName = document.createElement("driverClassName");
            configDriverClassName.appendChild(document.createTextNode(driverClassName));

            Element configMaxActive = document.createElement("maxActive");
            configMaxActive.appendChild(document.createTextNode("50"));
            Element configMaxWait = document.createElement("maxWait");
            configMaxWait.appendChild(document.createTextNode("60000"));
            Element configTestOnBorrow = document.createElement("testOnBorrow");
            configTestOnBorrow.appendChild(document.createTextNode("true"));
            Element configValidationQuery = document.createElement("validationQuery");
            configValidationQuery.appendChild(document.createTextNode("SELECT 1"));
            Element configValidationInterval = document.createElement("validationInterval");
            configValidationInterval.appendChild(document.createTextNode("30000"));
            Element configDefaultAutoCommit = document.createElement("defaultAutoCommit");
            configDefaultAutoCommit.appendChild(document.createTextNode("false"));

            configurationRoot.appendChild(configURL);
            configurationRoot.appendChild(configUsername);
            configurationRoot.appendChild(configPassword);
            configurationRoot.appendChild(configDriverClassName);
            configurationRoot.appendChild(configMaxActive);
            configurationRoot.appendChild(configMaxWait);
            configurationRoot.appendChild(configTestOnBorrow);
            configurationRoot.appendChild(configValidationQuery);
            configurationRoot.appendChild(configValidationInterval);
            configurationRoot.appendChild(configDefaultAutoCommit);

            definition.appendChild(configurationRoot);
            dataSourceRoot.appendChild(definition);
            dataSourcesRoot.appendChild(dataSourceRoot);
            document.appendChild(dataSourcesConfigurationRoot);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            File masterDatasourceFile = new File("src/test/resources/conf/master-datasources.xml");
            Files.createParentDirs(masterDatasourceFile);
            StreamResult result = new StreamResult(masterDatasourceFile);
            transformer.transform(source, result);

        } catch (ParserConfigurationException e) {
            log.error("Error in parsing master-datasources.xml file");
        } catch (TransformerConfigurationException e) {
            log.error("Error in creating configurations for master-datasources.xml file");
        } catch (TransformerException e) {
            log.error("Error in creating master-datasources.xml file");
        } catch (IOException e) {
            log.error("Can not save master-datasource.xml file");
        }

    }

    private enum RDBMSType {
        MySQL, H2, ORACLE, MSSQL, POSTGRES
    }

    private static RDBMSType getRDBMSType() {
        return RDBMSType.valueOf(System.getenv("DATABASE_TYPE"));
    }

    /**
     * Utility for get Docker running host
     *
     * @return docker host
     * @throws URISyntaxException if docker Host url is malformed this will throw
     */
    private static String getIpAddressOfContainer() {
        String ip = System.getenv("DOCKER_HOST_IP");
        String dockerHost = System.getenv("DOCKER_HOST");
        if (!StringUtils.isEmpty(dockerHost)) {
            try {
                URI uri = new URI(dockerHost);
                ip = uri.getHost();
            } catch (URISyntaxException e) {
                log.error("Error while getting the docker Host url." + e.getMessage(), e);
            }
        }
        return ip;
    }
}



