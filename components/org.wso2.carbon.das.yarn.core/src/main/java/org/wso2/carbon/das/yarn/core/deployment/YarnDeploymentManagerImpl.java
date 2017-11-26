package org.wso2.carbon.das.yarn.core.deployment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;


import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.yarn.api.ApplicationConstants;
import org.apache.hadoop.yarn.api.protocolrecords.GetNewApplicationResponse;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.api.records.ApplicationSubmissionContext;
import org.apache.hadoop.yarn.api.records.ContainerLaunchContext;
import org.apache.hadoop.yarn.api.records.FinalApplicationStatus;
import org.apache.hadoop.yarn.api.records.LocalResource;
import org.apache.hadoop.yarn.api.records.LocalResourceType;
import org.apache.hadoop.yarn.api.records.LocalResourceVisibility;
import org.apache.hadoop.yarn.api.records.NodeReport;
import org.apache.hadoop.yarn.api.records.NodeState;
import org.apache.hadoop.yarn.api.records.Priority;
import org.apache.hadoop.yarn.api.records.QueueACL;
import org.apache.hadoop.yarn.api.records.QueueInfo;
import org.apache.hadoop.yarn.api.records.QueueUserACLInfo;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.hadoop.yarn.api.records.YarnApplicationState;
import org.apache.hadoop.yarn.api.records.YarnClusterMetrics;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.client.api.YarnClientApplication;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.util.ConverterUtils;
import org.apache.hadoop.yarn.util.Records;
import org.apache.log4j.Logger;
import org.wso2.carbon.das.jobmanager.core.DeploymentManager;
import org.wso2.carbon.das.jobmanager.core.appCreator.DistributedSiddhiQuery;
import org.wso2.carbon.das.jobmanager.core.model.SiddhiAppHolder;
import org.wso2.carbon.stream.processor.core.distribution.DeploymentStatus;

import static org.wso2.carbon.das.yarn.core.utils.YarnDeploymentConstants.SIDDHIAPP_HOLDER_HDFS_PATH;
import static org.wso2.carbon.das.yarn.core.utils.YarnDeploymentConstants.SPAPP_MASTER_JAR_PATH;
import static org.wso2.carbon.das.yarn.core.utils.YarnDeploymentConstants.SPAPP_MASTER_MAIN_CLASS;
import static org.wso2.carbon.das.yarn.core.utils.YarnDeploymentConstants.SPAPP_MASTER_MEMORY;
import static org.wso2.carbon.das.yarn.core.utils.YarnDeploymentConstants.SPAPP_MASTER_VCORES;
import static org.wso2.carbon.das.yarn.core.utils.YarnDeploymentConstants.SP_APP_MASTER_PRIORITY;
import static org.wso2.carbon.das.yarn.core.utils.YarnDeploymentConstants.SP_APP_MASTER_QUEUE;


/**
 * This implementation corresponds to the <code>Yarn</code> distribution implementation of
 * <code>YarnClient</code>
 */
public class YarnDeploymentManagerImpl implements DeploymentManager {
    private static final Logger LOG = Logger.getLogger(YarnDeploymentManagerImpl.class);
    private Configuration conf;
    private YarnClient yarnClient;
    private volatile boolean yarnState = false;
    private ApplicationId appId;

    public YarnDeploymentManagerImpl() {
        this.conf = new YarnConfiguration();
        this.yarnClient = YarnClient.createYarnClient();
        this.yarnClient.init(conf);
    }

    /**
     * Gets a list of deployable SiddhiQueryGroup Object
     * This method will be used for Yarn deployment of ApplicationMaster
     *
     * @param distributedSiddhiQuery distributed Siddhi app
     * @return
     */
    @Override public DeploymentStatus deploy(DistributedSiddhiQuery distributedSiddhiQuery) {
        // TODO: 11/14/17  serialize appsTodeploy and put to HDFS and via that localize them to applicationMaster

        List<SiddhiAppHolder> appsToDeploy = getSiddhiAppHolders(distributedSiddhiQuery);
        try {
            yarnState = createApplicationSubmissionContext(appsToDeploy);
            //if yarnState == true ---- > this will in a success or both failure (failure in code )
            //if yarnState == false -- > need to stop and if okay re deploy in the yarn framework again
        } catch (Exception e) {
            // TODO: 11/14/17 catch but should not continue ---> throw distribution error exception
            LOG.error("Distribution error :", e);
        }
        return getDeploymentStatus(true, appsToDeploy);
    }

    /**
     * If YarnClient can communicate with the appMaster try to undeploy or else
     * kill AppMaster itself
     */
    @Override public boolean unDeploy(String siddhiAppName) {
        //kill the AppMaster or ----- > check if  YarnClient can communicate withe AppMaster running on
        try {
            if (appId != null) {
                yarnClient.killApplication(appId);
                return true;
            } else {
                LOG.info("SPAPPMaster is not Running..Can not Un-deploy running applications ");
                return false;
            }
        } catch (Exception e) {
            LOG.error("Could not kill SPAPPMaster..", e);
            return false;
        }
    }

    @Override public boolean isDeployed(String parentSiddhiAppName) {

        //if successful in handing over from yarn client from app Master then return true else return false;
        //this will happen to all the apps irrespective if their appName
        return false;
    }

    private boolean createApplicationSubmissionContext(List<SiddhiAppHolder> appstoDeploy) throws Exception {

        LOG.info("Starting YarnClient service....");
        yarnClient.start();

        //get Yarn Cluster report before starting APPMaster
        getYarnClusterReport();

        YarnClientApplication appMaster = yarnClient.createApplication();
        ApplicationSubmissionContext appMasterContext = appMaster.getApplicationSubmissionContext();

        GetNewApplicationResponse appResponse = appMaster.getNewApplicationResponse();
        int maxMem = appResponse.getMaximumResourceCapability().getMemory();

        if (SPAPP_MASTER_MEMORY > maxMem) {
            LOG.info("AM memory specified above max threshold of cluster. Using max value."
                             + ", specified=" + SPAPP_MASTER_MEMORY + ", max=" + maxMem);
        }else {
            maxMem = SPAPP_MASTER_MEMORY;
        }

        appId = appMasterContext.getApplicationId();
        appMasterContext.setApplicationName("SPAPPMaster");
        Resource capability = Records.newRecord(Resource.class);

        // TODO: 11/14/17 change appMaster memory requirement
        capability.setMemory(maxMem);
        capability.setVirtualCores(SPAPP_MASTER_VCORES);
        LOG.debug("AppMaster capability = " + capability);
        appMasterContext.setResource(capability);

        Priority pri = Records.newRecord(Priority.class);
        pri.setPriority(SP_APP_MASTER_PRIORITY);
        appMasterContext.setPriority(pri);
        appMasterContext.setQueue(SP_APP_MASTER_QUEUE);

        conf.set("fs.hdfs.impl", org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
        conf.set("fs.file.impl", org.apache.hadoop.fs.LocalFileSystem.class.getName());

        ContainerLaunchContext amContainer = Records.newRecord(ContainerLaunchContext.class);
        Map<String, LocalResource> localResources = new HashMap<>();

        // TODO: 11/26/17 add this to yarn.config
        conf.addResource(new Path("file:///usr/local/hadoop/etc/hadoop/core-site.xml")); // Replace with actual path
        conf.addResource(new Path("file:///usr/local/hadoop/etc/hadoop/hdfs-site.xml"));
        FileSystem fs = FileSystem.get(conf);
        
        // TODO: 11/13/17 set applicationMaster main class path
        // TODO: 11/13/17 handle when app master jar file is not located
        Path appMasterSrc = new Path(SPAPP_MASTER_JAR_PATH);
        Path appMasterDestination = new Path(fs.getHomeDirectory(), "AppMaster.jar");
        LOG.info("AppMaster Yarn URL = " + appMasterDestination.toUri().toString());

        LOG.info("Copy App Master jar from local filesystem and add to local environment");
        fs.copyFromLocalFile(false, true, appMasterSrc, appMasterDestination);
        FileStatus destinationStatus = fs.getFileStatus(appMasterDestination);
        LocalResource amJarRsrc = Records.newRecord(LocalResource.class);
        amJarRsrc.setType(LocalResourceType.FILE);
        amJarRsrc.setVisibility(LocalResourceVisibility.APPLICATION);
        amJarRsrc.setResource(ConverterUtils.getYarnUrlFromPath(appMasterDestination));
        amJarRsrc.setTimestamp(destinationStatus.getModificationTime());
        amJarRsrc.setSize(destinationStatus.getLen());
        localResources.put("AppMaster.jar", amJarRsrc);

        //Serializing distributed SiddhiApp object for distribution and putting to the HDFS
        //the file in the HDFS will be later localized for SPAPPMaster
        //This way is error free compared to passing as arguments but not efficient
       // DistributedQuerySerializer distributedQuerySerializer = new DistributedQuerySerializer(conf);

        //hard coded path for now better if we can give it from deployment.config
        writeSerializedSiddhiAPPHolders(appstoDeploy);

        // TODO: 11/14/17 localize serialized siddhiAppHolders for APPMaster
       /* //localize this file to SPAPPMaster
        LOG.info("Copy Serialized distributed SiddhiAPPs from local filesystem to local environment");
        Path serializedFile = new Path(serializedSiddhiAppHolderPath);
        Path serializedFileDist = new Path(fs.getHomeDirectory(), "siddhiAppHolderList.ser");
        LOG.debug("Distributed Serialized file Yarn URL = " + appMasterDestination.toUri().toString());

        LOG.info("Copy Serialized file from local filesystem and add to local environment");
        fs.copyFromLocalFile(false, true, serializedFile, serializedFileDist);
        destinationStatus = fs.getFileStatus(serializedFileDist);
        LocalResource serializedFileRsrc = Records.newRecord(LocalResource.class);
        serializedFileRsrc.setType(LocalResourceType.FILE);
        serializedFileRsrc.setVisibility(LocalResourceVisibility.APPLICATION);
        serializedFileRsrc.setResource(ConverterUtils.getYarnUrlFromPath(appMasterDestination));
        serializedFileRsrc.setTimestamp(destinationStatus.getModificationTime());
        serializedFileRsrc.setSize(destinationStatus.getLen());
        localResources.put("siddhiAppHolderList.ser", amJarRsrc);*/
        amContainer.setLocalResources(localResources);

        //set environment
        Map<String, String> env = new HashMap<>();
        StringBuilder classPathEnv = new StringBuilder(ApplicationConstants.Environment.CLASSPATH.$()).append(
                File.pathSeparatorChar).append("./*");

        for (String c : conf.getStrings(YarnConfiguration.YARN_APPLICATION_CLASSPATH,
                                        YarnConfiguration.DEFAULT_YARN_APPLICATION_CLASSPATH)) {
            classPathEnv.append(File.pathSeparatorChar);
            classPathEnv.append(c.trim());
        }

        env.put("CLASSPATH", classPathEnv.toString());
        amContainer.setEnvironment(env);

        Vector<CharSequence> vargs = new Vector<>(30);

        vargs.add(ApplicationConstants.Environment.JAVA_HOME.$() + "/bin/java");

        // TODO: 11/13/17 ApplicationMaster main class comes here
        vargs.add(SPAPP_MASTER_MAIN_CLASS);

        vargs.add("1>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stdout");
        vargs.add("2>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stderr");

        StringBuilder command = new StringBuilder();

        for (CharSequence str : vargs) {
            command.append(str).append(" ");
        }

        List<String> commands = new ArrayList<>();
        commands.add(command.toString());
        amContainer.setCommands(commands);
        appMasterContext.setAMContainerSpec(amContainer);
        yarnClient.submitApplication(appMasterContext);

        LOG.info("Starting APPMaster Monitoring loop");
        return serviceState(appId);
    }


    public void writeSerializedSiddhiAPPHolders(List<SiddhiAppHolder> appstoDeploy)  {
        try {
            FileSystem fs = FileSystem.get(conf);
            Path hdfsPath = new Path(fs.getHomeDirectory(),SIDDHIAPP_HOLDER_HDFS_PATH);
            ObjectOutputStream oos = new ObjectOutputStream(fs.create(hdfsPath));
            oos.writeObject(appstoDeploy);
            oos.close();

        } catch (FileNotFoundException e) {
            LOG.error("Error while creating Query Holder file",e);
        } catch (IOException e) {
            LOG.error("Error while creating serialized Query holder",e);
        }
    }

    // TODO: 11/14/17 change the getDeployment Status
    private DeploymentStatus getDeploymentStatus(boolean isDeployed, List<SiddhiAppHolder> siddhiAppHolders) {
        Map<String, List<String>> deploymentDataMap = new HashMap<>();
        return new DeploymentStatus(isDeployed, deploymentDataMap);
    }


    private List<SiddhiAppHolder> getSiddhiAppHolders(DistributedSiddhiQuery distributedSiddhiQuery) {
        List<SiddhiAppHolder> siddhiAppHolders = new ArrayList<>();
        distributedSiddhiQuery.getQueryGroups().forEach(queryGroup -> {
            queryGroup.getSiddhiQueries().forEach(query -> {
                siddhiAppHolders.add(new SiddhiAppHolder(distributedSiddhiQuery.getAppName(),
                                                         queryGroup.getGroupName(), query.getAppName(), query.getApp(),
                                                         null));
            });
        });
        return siddhiAppHolders;
    }

    private void getYarnClusterReport() throws Exception {
        YarnClusterMetrics clusterMetrics = yarnClient.getYarnClusterMetrics();
        LOG.info("Got Cluster metric info from ASM" + ", numNodeManagers="
                         + clusterMetrics.getNumNodeManagers());

        List<NodeReport> clusterNodeReports = yarnClient
                .getNodeReports(NodeState.RUNNING);
        LOG.info("Got Cluster node info from ASM");
        for (NodeReport node : clusterNodeReports) {
            LOG.info("Got node report from ASM for" + ", nodeId="
                             + node.getNodeId() + ", nodeAddress"
                             + node.getHttpAddress() + ", nodeRackName"
                             + node.getRackName() + ", nodeNumContainers"
                             + node.getNumContainers());
        }

        QueueInfo queueInfo = yarnClient.getQueueInfo("default");
        LOG.info("Queue info" + ", queueName=" + queueInfo.getQueueName()
                         + ", queueCurrentCapacity=" + queueInfo.getCurrentCapacity()
                         + ", queueMaxCapacity=" + queueInfo.getMaximumCapacity()
                         + ", queueApplicationCount="
                         + queueInfo.getApplications().size()
                         + ", queueChildQueueCount=" + queueInfo.getChildQueues().size());

        List<QueueUserACLInfo> listAclInfo = yarnClient.getQueueAclsInfo();
        for (QueueUserACLInfo aclInfo : listAclInfo) {
            for (QueueACL userAcl : aclInfo.getUserAcls()) {
                LOG.info("User ACL Info for Queue" + ", queueName="
                                 + aclInfo.getQueueName() + ", userAcl="
                                 + userAcl.name());
            }
        }
    }

    private boolean serviceState(ApplicationId appId) throws Exception {
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                LOG.error("Thread sleep in monitoring loop interrupted");
            }

            ApplicationReport report = yarnClient.getApplicationReport(appId);
            LOG.info("Got application report from ASM for" + ", appId="
                             + appId.getId() + ", clientToAMToken="
                             + report.getClientToAMToken() + ", appDiagnostics="
                             + report.getDiagnostics() + ", appMasterHost="
                             + report.getHost() + ", appQueue=" + report.getQueue()
                             + ", appMasterRpcPort=" + report.getRpcPort()
                             + ", appStartTime=" + report.getStartTime()
                             + ", yarnAppState="
                             + report.getYarnApplicationState().toString()
                             + ", distributedFinalState="
                             + report.getFinalApplicationStatus().toString()
                             + ", appTrackingUrl=" + report.getTrackingUrl()
                             + ", appUser=" + report.getUser());


            YarnApplicationState state = report.getYarnApplicationState();
            FinalApplicationStatus dsStatus = report.getFinalApplicationStatus();
            if (YarnApplicationState.FINISHED == state) {
                if (FinalApplicationStatus.SUCCEEDED == dsStatus) {
                    LOG.info("Application has completed successfully.");
                    return true;
                } else {
                    LOG.info("Application finished unsuccessfully." + " YarnState=" + state.toString() + ", "
                                     + "DSFinalStatus=" + dsStatus.toString());
                    return false;
                }
            } else if (YarnApplicationState.KILLED == state || YarnApplicationState.FAILED == state) {
                LOG.info("Application did not finish." + " YarnState=" + state.toString() + ", DSFinalStatus=" +
                                 dsStatus.toString());
                return false;
            }

        }
    }
}


