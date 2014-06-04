package org.wso2.carbon.bam.toolbox.deployer.ui.client;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.toolbox.deployer.stub.BAMToolboxDepolyerServiceBAMToolboxDeploymentExceptionException;
import org.wso2.carbon.bam.toolbox.deployer.stub.BAMToolboxDepolyerServiceStub;
import org.wso2.carbon.bam.toolbox.deployer.stub.BAMToolboxDepolyerServiceStub.ToolBoxStatusDTO;
import org.wso2.carbon.bam.toolbox.deployer.stub.BAMToolboxDepolyerServiceStub.BasicToolBox;

import javax.activation.DataHandler;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;

/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
public class BAMToolBoxDeployerClient {

    private BAMToolboxDepolyerServiceStub stub;

    private static String BAMToolBoxService = "BAMToolboxDepolyerService";
    private static Log log = LogFactory.getLog(BAMToolBoxDeployerClient.class);

    public BAMToolBoxDeployerClient(String cookie,
                                    String backEndServerURL,
                                    ConfigurationContext configCtx) throws AxisFault {
        String serviceURL = backEndServerURL + BAMToolBoxService;
        stub = new BAMToolboxDepolyerServiceStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    public boolean uploadToolBox(DataHandler toolbox, String toolName)
            throws RemoteException, BAMToolboxDepolyerServiceBAMToolboxDeploymentExceptionException {
        try {
            return stub.uploadBAMToolBox(toolbox, toolName);
        } catch (RemoteException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (BAMToolboxDepolyerServiceBAMToolboxDeploymentExceptionException e) {
            log.error(e.getFaultMessage().getBAMToolboxDeploymentException().getMessage(), e);
            throw e;
        }
    }

    public ToolBoxStatusDTO getToolBoxStatus(String toolType, String search)
            throws BAMToolboxDepolyerServiceBAMToolboxDeploymentExceptionException, RemoteException {
        try {
            return stub.getDeployedToolBoxes(toolType, search);
        } catch (RemoteException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (BAMToolboxDepolyerServiceBAMToolboxDeploymentExceptionException e) {
            log.error(e.getFaultMessage().getBAMToolboxDeploymentException().getMessage(), e);
            throw e;
        }
    }

    public boolean undeployToolBox(String[] toolName)
            throws RemoteException, BAMToolboxDepolyerServiceBAMToolboxDeploymentExceptionException {
        try {
            return stub.undeployToolBox(toolName);
        } catch (RemoteException e) {
            log.error(e);
            throw e;
        } catch (BAMToolboxDepolyerServiceBAMToolboxDeploymentExceptionException e) {
            log.error(e.getFaultMessage().getBAMToolboxDeploymentException());
            throw e;
        }
    }

    public void downloadBAMTool(String toolName, HttpServletResponse response)
            throws BAMToolboxDepolyerServiceBAMToolboxDeploymentExceptionException {
        try {
            ServletOutputStream out = response.getOutputStream();
            DataHandler downloadData = stub.downloadToolBox(toolName);
            if (downloadData != null) {
                String fileName = "";
                if (!toolName.endsWith(".tbox")) {
                    fileName = toolName + ".tbox";
                } else fileName = toolName;
                response.setHeader("Content-Disposition", "fileName=" + fileName);
                response.setContentType(downloadData.getContentType());
                InputStream in = downloadData.getDataSource().getInputStream();
                int nextChar;
                while ((nextChar = in.read()) != -1) {
                    out.write((char) nextChar);
                }
                out.flush();
                in.close();
            } else {
                out.write("The requested service archive was not found on the server".getBytes());
            }
        } catch (RemoteException e) {
            log.error(e.getMessage(), e);
            throw new BAMToolboxDepolyerServiceBAMToolboxDeploymentExceptionException(e);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new BAMToolboxDepolyerServiceBAMToolboxDeploymentExceptionException(e);
        }
    }

    public void deploySample(String sampleId)
            throws BAMToolboxDepolyerServiceBAMToolboxDeploymentExceptionException {
        try {
            stub.deployBasicToolBox(Integer.parseInt(sampleId));
        } catch (RemoteException e) {
            log.error(e.getMessage(), e);
            throw new BAMToolboxDepolyerServiceBAMToolboxDeploymentExceptionException(e);
        } catch (BAMToolboxDepolyerServiceBAMToolboxDeploymentExceptionException e) {
            log.error(e.getFaultMessage().getBAMToolboxDeploymentException().getMessage(), e);
            throw e;
        }
    }

    public BasicToolBox[] getAllBasicTools()
            throws BAMToolboxDepolyerServiceBAMToolboxDeploymentExceptionException {
        try {
            return stub.getBasicToolBoxes();
        } catch (RemoteException e) {
            log.error(e.getMessage(), e);
            throw new BAMToolboxDepolyerServiceBAMToolboxDeploymentExceptionException(e);
        }
    }

    public void installToolBoxFromURL(String url)
            throws BAMToolboxDepolyerServiceBAMToolboxDeploymentExceptionException {
        try {
            stub.deployToolBoxFromURL(url);
        } catch (RemoteException e) {
            log.error(e.getMessage(), e);
            throw new BAMToolboxDepolyerServiceBAMToolboxDeploymentExceptionException(e);
        } catch (BAMToolboxDepolyerServiceBAMToolboxDeploymentExceptionException e) {
            log.error(e.getFaultMessage().getBAMToolboxDeploymentException().getMessage(), e);
            throw e;
        }
    }

}
