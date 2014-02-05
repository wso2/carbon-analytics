package org.wso2.carbon.dashboard.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.dashboard.stub.resource.*;
import org.wso2.carbon.dashboard.stub.resource.beans.xsd.*;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpSession;
import java.rmi.RemoteException;

/**
 * Used by the Gadget Content Processor to call the backend server to get the registry resource
 */
public class GadgetResourceServiceClient {
    private GadgetContentServiceStub stub;
    private String epr;
    private static final Log log = LogFactory.getLog(GadgetResourceServiceClient.class);

    public GadgetResourceServiceClient(ServletConfig config, HttpSession session)
            throws RegistryException {

        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config.
                getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        epr = backendServerURL + "GadgetContentService";

        try {
            stub = new GadgetContentServiceStub(configContext, epr);

            ServiceClient client = stub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

        } catch (AxisFault axisFault) {
            String msg = "Failed to initiate resource service client. " + axisFault.getMessage();
            log.error(msg, axisFault);
            throw new RegistryException(msg, axisFault);
        }
    }

    public ContentDownloadBean getContentDownloadBean(String path, String tenantDomain) throws GadgetContentServiceException, RemoteException {
        ContentDownloadBean bean = stub.getContentDownloadBean(path,tenantDomain);
        return bean;
    }
}
