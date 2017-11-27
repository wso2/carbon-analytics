package org.wso2.carbon.das.yarn.core.utils;

public class YarnDeploymentConstants {

    public static final String MODE_DISTRIBUTED ="yarn";
    public static final String DEPLOYMENT_CONFIG_NS = "deployment.config";
    public static final Integer SPAPP_MASTER_MEMORY = 256;
    public static final Integer SPAPP_MASTER_VCORES =2;
    public static final String SP_APP_MASTER_QUEUE = "default";
    public static final Integer SP_APP_MASTER_PRIORITY =0;
    public static final String SPAPP_MASTER_JAR_PATH = "/home/piyumi/Documents/org.wso2.carbon.das.yarnapp"
            + ".core/target/org.wso2.carbon.das.yarnapp.core-1.0-SNAPSHOT.jar";
    public static final String SPAPP_MASTER_MAIN_CLASS ="org.wso2.carbon.das.yarnapp.core.SPAPPMaster";
    public static final String SIDDHIAPP_HOLDER_HDFS_PATH ="siddhiappholderList.ser";
    public static final String SPAPP_MASTER = "SPAPPMaster.jar";
}
