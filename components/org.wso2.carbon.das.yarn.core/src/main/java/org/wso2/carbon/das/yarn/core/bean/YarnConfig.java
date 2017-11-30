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

    @Element(description = "WSO2 SP compressed tar ball absolute File path",required = true)
    private String spCompressedBuildPackPath;

    @Element(description = "WSO2 sp unzipped bundle name", required = true)
    private String spUnzippedBundleName;

    public String getSpUnzippedBundleName() {
        return spUnzippedBundleName;
    }

    public void setSpUnzippedBundleName(String spUnzippedBundleName) {
        this.spUnzippedBundleName = spUnzippedBundleName;
    }

    public String getCoreSiteXML() {
        return coreSiteXML;
    }

    public String getSpCompressedBuildPackPath() {
        return spCompressedBuildPackPath;
    }

    public void setSpCompressedBuildPackPath(String spCompressedBuildPackPath) {
        this.spCompressedBuildPackPath = spCompressedBuildPackPath;
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
