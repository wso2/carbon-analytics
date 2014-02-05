package org.wso2.carbon.bam.toolbox.deployer.ui.upload;

import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.bam.toolbox.deployer.stub.BAMToolboxDepolyerServiceBAMToolboxDeploymentExceptionException;
import org.wso2.carbon.bam.toolbox.deployer.stub.BAMToolboxDepolyerServiceStub.ToolBoxStatusDTO;
import org.wso2.carbon.bam.toolbox.deployer.ui.client.BAMToolBoxDeployerClient;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.ui.transports.fileupload.AbstractFileUploadExecutor;
import org.wso2.carbon.utils.FileItemData;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
public class ToolBoxUploader extends AbstractFileUploadExecutor {

    @Override
    public boolean execute(HttpServletRequest request, HttpServletResponse response) throws CarbonException, IOException {

        String webContext = (String) request.getAttribute(CarbonConstants.WEB_CONTEXT);

        HttpSession session = request.getSession();
        String serverURL = CarbonUIUtil.getServerURL(session.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) session.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

        BAMToolBoxDeployerClient client = new BAMToolBoxDeployerClient(cookie, serverURL, configContext);

        List<FileItemData> toolbox;
        Map<String, ArrayList<FileItemData>> fileItemsMap = getFileItemsMap();
        Map<String, ArrayList<String>> formFields = getFormFieldsMap();

        String selectedType = formFields.get("selectedToolType").get(0);
        if (selectedType.equals("0")) {
            toolbox = fileItemsMap.get("toolbox");
            FileItemData uploadedTool = toolbox.get(0);
            try {
                ToolBoxStatusDTO statusDTO = client.getToolBoxStatus("1", null);
                String msg = checkInRepo(uploadedTool.getFileItem().getName(), statusDTO);
                if (msg.equals("")) {
                    if (client.uploadToolBox(uploadedTool.getDataHandler(), uploadedTool.getFileItem().getName())) {
                        response.sendRedirect("../" + webContext + "/bam-toolbox/listbar.jsp?success=true");
                    } else {
                        response.sendRedirect("../" + webContext + "/bam-toolbox/uploadbar.jsp?success=false&message=" +
                                "Error while deploying toolbox!");
                    }
                    return true;
                } else {
                    response.sendRedirect("../" + webContext + "/bam-toolbox/uploadbar.jsp?success=false&message="
                            + msg);
                    return false;
                }
            } catch (BAMToolboxDepolyerServiceBAMToolboxDeploymentExceptionException e) {
                response.sendRedirect("../" + webContext + "/bam-toolbox/uploadbar.jsp?success=false&message="
                        + e.getFaultMessage().getBAMToolboxDeploymentException().getMessage());
                return false;
            }
        } else if (selectedType.equals("-1")) {
            List<String> toolBoxURL = formFields.get("urltoolbox");
            String url = toolBoxURL.get(0);
            int slashIndex = url.lastIndexOf('/');
            String toolName = url.substring(slashIndex + 1);

            ToolBoxStatusDTO statusDTO = null;
            try {
                statusDTO = client.getToolBoxStatus("1", null);
                String msg = checkInRepo(toolName, statusDTO);
                if (msg.equals("")) {
                    client.installToolBoxFromURL(url);
                    response.sendRedirect("../" + webContext + "/bam-toolbox/listbar.jsp?success=true");
                    new File("tmp/" + url.substring(slashIndex + 1)).delete();

                    return true;
                } else {
                    response.sendRedirect("../" + webContext + "/bam-toolbox/uploadbar.jsp?success=false&message="
                            + msg);
                    return false;
                }
            } catch (BAMToolboxDepolyerServiceBAMToolboxDeploymentExceptionException e) {
                response.sendRedirect("../" + webContext + "/bam-toolbox/uploadbar.jsp?success=false&message="
                        + e.getFaultMessage().getBAMToolboxDeploymentException().getMessage());
                return false;
            }

        } else {
            try {
                client.deploySample(selectedType);
                response.sendRedirect("../" + webContext + "/bam-toolbox/listbar.jsp?success=true");
                return true;
            } catch (BAMToolboxDepolyerServiceBAMToolboxDeploymentExceptionException e) {
                response.sendRedirect("../" + webContext + "/bam-toolbox/uploadbar.jsp?success=false&message="
                        + e.getFaultMessage().getBAMToolboxDeploymentException().getMessage());
                return false;
            }
        }
    }


    private String checkInRepo(String toolName, ToolBoxStatusDTO statusDTO) {
        toolName = toolName.replaceAll(".tbox", "");
        String msg = "";
        if (isInList(toolName, statusDTO.getDeployedTools())) {
            msg = "The uploaded toolbox is already Installed. \nPlease upload a different toolbox.";
            return msg;
        } else if (isInList(toolName, statusDTO.getToBeDeployedTools())) {
            msg = "The uploaded toolbox is already Installing. \nPlease wait until it's installed.";
            return msg;
        } else if (isInList(toolName, statusDTO.getToBeUndeployedTools())) {
            msg = "The uploaded toolbox is Un-installing. \nPlease wait until it's un-installed.";
            return msg;
        } else {
            return msg;
        }
    }


    private boolean isInList(String name, String[] searchList) {
        if (null != searchList) {
            for (String aName : searchList) {
                if (name.equalsIgnoreCase(aName)) {
                    return true;
                }
            }
            return false;
        } else {
            return false;
        }
    }


}
