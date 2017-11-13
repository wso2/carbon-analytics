package org.wso2.carbon.das.jobmanager.core.deployment;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.yarn.api.ApplicationConstants;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ApplicationSubmissionContext;
import org.apache.hadoop.yarn.api.records.ContainerLaunchContext;
import org.apache.hadoop.yarn.api.records.LocalResource;
import org.apache.hadoop.yarn.api.records.LocalResourceType;
import org.apache.hadoop.yarn.api.records.LocalResourceVisibility;
import org.apache.hadoop.yarn.api.records.Priority;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.client.api.YarnClientApplication;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.apache.hadoop.yarn.util.ConverterUtils;
import org.apache.hadoop.yarn.util.Records;
import org.apache.log4j.Logger;
import org.wso2.carbon.das.jobmanager.core.DeploymentManager;
import org.wso2.carbon.das.jobmanager.core.appCreator.DistributedSiddhiQuery;
import org.wso2.carbon.stream.processor.core.distribution.DeploymentStatus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;


/**
 *This implementation corresponds to the <code>Yarn</code> distribution implementation of
 * <code>YarnClient</code>
*/
public class YarnDeploymentManagerImpl implements DeploymentManager{

    private static final Logger LOG = Logger.getLogger(YarnDeploymentManagerImpl.class);
    private Configuration conf;
    private YarnClient yarnClient;

    public YarnDeploymentManagerImpl() {
        this.conf = new YarnConfiguration();
        this.yarnClient = YarnClient.createYarnClient();
    }

    /**
     * Gets a list of deployable SiddhiQueryGroup Object
     * This method will be used for Yarn deployment of ApplicationMaster
     * @param distributedSiddhiQuery distributed Siddhi app
     * @return
     */
    @Override public DeploymentStatus deploy(DistributedSiddhiQuery distributedSiddhiQuery){

        return null;
    }

    /**
     * This method will be used to kill SP APPMaster
     * @param siddhiAppName distributed Siddhi app name
     * @return
     */
    @Override public boolean unDeploy(String siddhiAppName) {
        return false;
    }

    @Override public boolean isDeployed(String parentSiddhiAppName) {
        return false;
    }

    private void createApplicationSubmissionContext() throws IOException, YarnException {
        yarnClient.start();
        YarnClientApplication appMaster = yarnClient.createApplication();
        ApplicationSubmissionContext appMasterContext = appMaster.getApplicationSubmissionContext();

        ApplicationId appId = appMasterContext.getApplicationId();
        appMasterContext.setApplicationName("SPAPPMaster");
        //Specify resource capability of the applicationMaster

        Resource capability = Records.newRecord(Resource.class);
        capability.setMemory(this.conf.getInt("yarn.app.mapreduce.am.resource.mb", 2048));
        capability.setVirtualCores(this.conf.getInt("yarn.app.mapreduce.am.resource.cpu-vcores", 1));
        appMasterContext.setResource(capability);

        Priority pri = Records.newRecord(Priority.class);
        pri.setPriority(0);
        appMasterContext.setPriority(pri);
        appMasterContext.setQueue("default");

        ContainerLaunchContext amContainer = Records.newRecord(ContainerLaunchContext.class);
        Map<String,LocalResource> localResources = new HashMap<String,LocalResource>();
        FileSystem fs = FileSystem.get(conf);
        //AM jar file path
        Path appMasterSrc = new Path("");
        Path appMasterDestination = new Path(fs.getHomeDirectory(), "AppMaster.jar");

        String appMasterURI = appMasterDestination.toUri().toString();
        fs.copyFromLocalFile(false, true, appMasterSrc, appMasterDestination);
        FileStatus destStatus = fs.getFileStatus(appMasterDestination);
        LocalResource amJarRsrc = Records.newRecord(LocalResource.class);
        amJarRsrc.setType(LocalResourceType.FILE);
        amJarRsrc.setVisibility(LocalResourceVisibility.APPLICATION);
        amJarRsrc.setResource(ConverterUtils.getYarnUrlFromPath(appMasterDestination));
        amJarRsrc.setTimestamp(destStatus.getModificationTime());
        amJarRsrc.setSize(destStatus.getLen());
        localResources.put("AppMaster.jar", amJarRsrc);
        amContainer.setLocalResources(localResources);


        //set environment
        Map<String ,String > env = new HashMap<String, String>();
        StringBuilder classPathEnv = new StringBuilder(ApplicationConstants.Environment.CLASSPATH.$()).append(
                File.pathSeparatorChar).append("./*");

        for (String c : conf.getStrings(YarnConfiguration.YARN_APPLICATION_CLASSPATH, YarnConfiguration.DEFAULT_YARN_APPLICATION_CLASSPATH))

        {
            classPathEnv.append(File.pathSeparatorChar);
            classPathEnv.append(c.trim());
        }

        env.put("CLASSPATH", classPathEnv.toString());
        amContainer.setEnvironment(env);
        System.out.println(classPathEnv.toString());


        //Launch applicationMaster
        Vector<CharSequence> vargs = new Vector<CharSequence>(30);

        vargs.add(ApplicationConstants.Environment.JAVA_HOME.$() + "/bin/java");

        // TODO: 11/13/17 ApplicationMaster main class comes here
        vargs.add("");
        vargs.add("--jar "+ appMasterURI );

        vargs.add("1>" +ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stdout");
        vargs.add("2>" +ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stderr");

        StringBuilder command = new StringBuilder();

        for (CharSequence str : vargs)
        {
            command.append(str).append(" ");
        }

        List<String> commands = new ArrayList<String>();
        commands.add(command.toString());
        amContainer.setCommands(commands);


        //specify ContainerLaunchContext for ApplicationMasterContext
        appMasterContext.setAMContainerSpec(amContainer);






        yarnClient.submitApplication(appMasterContext);
    }
}
