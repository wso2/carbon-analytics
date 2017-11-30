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
import org.wso2.carbon.das.yarn.core.internal.YarnServiceDataHolder;
import org.wso2.carbon.stream.processor.core.distribution.DeploymentStatus;

import static org.wso2.carbon.das.yarn.core.utils.YarnDeploymentConstants.SIDDHIAPP_HOLDER_HDFS_PATH;
import static org.wso2.carbon.das.yarn.core.utils.YarnDeploymentConstants.SPAPP_MASTER;
import static org.wso2.carbon.das.yarn.core.utils.YarnDeploymentConstants.SPAPP_MASTER_MAIN_CLASS;
import static org.wso2.carbon.das.yarn.core.utils.YarnDeploymentConstants.SPAPP_MASTER_MEMORY;
import static org.wso2.carbon.das.yarn.core.utils.YarnDeploymentConstants.SPAPP_MASTER_VCORES;
import static org.wso2.carbon.das.yarn.core.utils.YarnDeploymentConstants.SP_APP_MASTER_PRIORITY;
import static org.wso2.carbon.das.yarn.core.utils.YarnDeploymentConstants.SP_APP_MASTER_QUEUE;
import static org.wso2.carbon.das.yarn.core.utils.YarnDeploymentConstants.SP_BUILPACK_HDSFS_NAME;


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
     * This method will be used to deploy ApplicationMaster to the YARN framework.
     *
     * @param distributedSiddhiQuery
     * @return
     */
    @Override public DeploymentStatus deploy(DistributedSiddhiQuery distributedSiddhiQuery) {

        List<SiddhiAppHolder> appsToDeploy = getSiddhiAppHolders(distributedSiddhiQuery);
        try {
            //yarnState returned with value true indicates that applicationMaster was launched without failure
            yarnState = createApplicationSubmissionContext(appsToDeploy);

        } catch (Exception e) {
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
        return true;
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

        //setting up applicationMaster resource requirement
        Resource capability = Records.newRecord(Resource.class);
        capability.setMemory(SPAPP_MASTER_MEMORY);
        capability.setVirtualCores(SPAPP_MASTER_VCORES);
        LOG.debug("AppMaster capability = " + capability);
        appMasterContext.setResource(capability);

        //setting up priority of the Application
        Priority pri = Records.newRecord(Priority.class);
        pri.setPriority(SP_APP_MASTER_PRIORITY);
        appMasterContext.setPriority(pri);
        appMasterContext.setQueue(SP_APP_MASTER_QUEUE);

        //setting up class implementations for DistributedFileSystem and LocalFileSystem separately
        conf.set("fs.hdfs.impl", org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
        conf.set("fs.file.impl", org.apache.hadoop.fs.LocalFileSystem.class.getName());

        //setting up Configurations specified in the core-site.xml and hdfs-site.xml
        //This is requirement is needed as HADOOP_CONF classpath is not included while running stream processor
        conf.addResource(new Path(YarnServiceDataHolder.getYarnConfig().getCoreSiteXML()));
        conf.addResource(new Path(YarnServiceDataHolder.getYarnConfig().getHdfsSiteXML()));

        ContainerLaunchContext amContainer = Records.newRecord(ContainerLaunchContext.class);
        Map<String, LocalResource> localResources = new HashMap<>();
        FileSystem fs = FileSystem.get(conf);

        Path appMasterSrc = new Path(YarnServiceDataHolder.getYarnConfig().getAppMasterJarAbsolutePath());
        Path appMasterDestination = new Path(fs.getHomeDirectory(), SPAPP_MASTER);

        LOG.info("Copy App Master jar from local filesystem Distrubuted File System");

        fs.copyFromLocalFile(false, true, appMasterSrc, appMasterDestination);
        FileStatus destinationStatus = fs.getFileStatus(appMasterDestination);
        LocalResource amJarRsrc = Records.newRecord(LocalResource.class);
        amJarRsrc.setType(LocalResourceType.FILE);
        amJarRsrc.setVisibility(LocalResourceVisibility.APPLICATION);
        amJarRsrc.setResource(ConverterUtils.getYarnUrlFromPath(appMasterDestination));
        amJarRsrc.setTimestamp(destinationStatus.getModificationTime());
        amJarRsrc.setSize(destinationStatus.getLen());
        localResources.put(SPAPP_MASTER, amJarRsrc);


        LOG.info("Copy WSO2_SP tar file from local filesystem Distrubuted File System");

        Path spLocalSrcPath = new Path(YarnServiceDataHolder.getYarnConfig().getSpCompressedBuildPackPath());
        Path spHDFSPath = new Path(fs.getHomeDirectory(), SP_BUILPACK_HDSFS_NAME);
        fs.copyFromLocalFile(false, true, spLocalSrcPath, spHDFSPath);
        FileStatus spdestinationStatus = fs.getFileStatus(appMasterDestination);
        LocalResource spLocalResource = Records.newRecord(LocalResource.class);
        spLocalResource.setType(LocalResourceType.FILE);
        spLocalResource.setVisibility(LocalResourceVisibility.APPLICATION);
        spLocalResource.setResource(ConverterUtils.getYarnUrlFromPath(spHDFSPath));
        spLocalResource.setTimestamp(spdestinationStatus.getModificationTime());
        spLocalResource.setSize(spdestinationStatus.getLen());
        localResources.put(SP_BUILPACK_HDSFS_NAME, amJarRsrc);


        //Serializing list of siddhiappHolders writing to the HDFS
        writeSerializedSiddhiAPPHolders(appstoDeploy);

        amContainer.setLocalResources(localResources);

        //set environment for application Master  (classpath)
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
        vargs.add(SPAPP_MASTER_MAIN_CLASS);

        vargs.add(YarnServiceDataHolder.getYarnConfig().getSpUnzippedBundleName());
        //these arguments corresponds to creating stdout and stderr files for logging which happens from tasks inside
        //applicationMaster container
        //the created log files can be found in the $HADOOP_HOME/logs/userlogs/application_ID/container_ID
        //application_id corresponds to the ID assigned for the application
        //container_ID corresponds to the ID assigned for the applicationMaster
        vargs.add("1>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stdout");
        vargs.add("2>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stderr");

        StringBuilder command = new StringBuilder();

        for (CharSequence str : vargs) {
            command.append(str).append(" ");
        }

        List<String> commands = new ArrayList<>();
        commands.add(command.toString());

        //setting up commands required to be executed at container launch
        //executed commands can be found at yarn.nodemanager.local-dirs
        // /usercache/username/appcache/applicationID/containerID/launcher_sh.sh
        amContainer.setCommands(commands);
        appMasterContext.setAMContainerSpec(amContainer);
        yarnClient.submitApplication(appMasterContext);

        LOG.info("Starting APPMaster Monitoring loop");
        return serviceState(appId);
    }

    /**
     * Invocation of this method writes a serialized  Concrete list of {@link SiddhiAppHolder} to the
     * distributed file-system.
     * Writing to the HDFS contains an over-head as it is IO-bound but this way enables us to locate the serialized
     * object from HDFS for SPAPPMaster
     * @param appstoDeploy
     */
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

    /**
     * This method returns cluster capability from Automatic Server Management (ASM)  prior to the deployment
     *
     * @throws Exception
     */
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

        //Provide Access Client List of the Queue
        //ACL indicates the users with the privilege to submits a job,
        //kill a job, or change its priority.
        List<QueueUserACLInfo> listAclInfo = yarnClient.getQueueAclsInfo();
        for (QueueUserACLInfo aclInfo : listAclInfo) {
            for (QueueACL userAcl : aclInfo.getUserAcls()) {
                LOG.info("User ACL Info for Queue" + ", queueName="
                                 + aclInfo.getQueueName() + ", userAcl="
                                 + userAcl.name());
            }
        }
    }

    /**
     * This method is intended to monitor SPAPPMaster state for every 1000 seconds
     */
    private boolean serviceState(ApplicationId appId) throws Exception {
        while (true) {
            try {
                //yarn client application will monitor the status of ApplicationMaster for every second
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



