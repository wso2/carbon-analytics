package org.wso2.carbon.das.yarn.core.bean;

import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;

/**
 * following class is responsible for holding configurations required for YarnDeployment.
 */
@Configuration(namespace = "yarn.config",description = "Yarn Distributed Deployment Configuration")
public class YarnConfig {

    @Element(description = "Absolute file path of core.-site.xml", required = true)
    private String coreSiteXML;

    @Element(description = "Absolute file path of hdfs-site.xml",required = true)
    private String hdfsSiteXML;

    @Element(description = "Application Master Jar absolute File Path",required = true)
    private String appMasterJarAbsolutePath;

    public String getCoreSiteXML() {
        return coreSiteXML;
    }

    public void setCoreSiteXML(String coreSiteXML) {
        this.coreSiteXML = coreSiteXML;
    }

    public String getHdfsSiteXML() {
        return hdfsSiteXML;
    }

    public void setHdfsSiteXML(String hdfsSiteXML) {
        this.hdfsSiteXML = hdfsSiteXML;
    }

    public String getAppMasterJarAbsolutePath() {
        return appMasterJarAbsolutePath;
    }

    public void setAppMasterJarAbsolutePath(String appMasterJarAbsolutePath) {
        this.appMasterJarAbsolutePath = appMasterJarAbsolutePath;
    }
}
