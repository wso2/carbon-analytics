package org.wso2.carbon.dashboard.mgt.oauth.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.dashboard.mgt.oauth.stub.OAuthMgtServiceStub;
import org.wso2.carbon.dashboard.mgt.oauth.stub.types.carbon.ConsumerEntry;

import java.rmi.RemoteException;
import java.util.Locale;

/**
 * The class contains the wiring with the backend service
 */
public class OAuthMgtServiceClient {

    private static final Log log = LogFactory.getLog(OAuthMgtServiceClient.class);
    public OAuthMgtServiceStub stub;

    public OAuthMgtServiceClient(String cookie,
                                 String backendServerURL,
                                 ConfigurationContext configCtx,
                                 Locale locale) throws AxisFault {
        String serviceURL = backendServerURL + "OAuthMgtService";

        stub = new OAuthMgtServiceStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(
                org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                cookie);
    }

    public ConsumerEntry[] getConsumerEntries() {
        ConsumerEntry[] entries = new ConsumerEntry[0];
        try {
            entries = stub.getConsumerEntries();
            if (entries == null) {
                return new ConsumerEntry[0];
            }
        } catch (RemoteException e) {
            log.error(e.getMessage(), e);
        }
        return entries;
    }

    public ConsumerEntry[] getConsumerPagedEntries(int pageNum) {
        ConsumerEntry[] entries = new ConsumerEntry[0];
        try {
            entries = stub.getConsumerPagedEntries(pageNum);
            if (entries == null) {
                return new ConsumerEntry[0];
            }
        } catch (RemoteException e) {
            log.error(e.getMessage(), e);
        }
        return entries;
    }

    public boolean addConsumerEntry(ConsumerEntry entry, String mode) {
        try {
            boolean ret = stub.addConsumerEntry(entry, mode);
            return ret;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }

    }

    public ConsumerEntry getConsumerEntry(String consumerServiceName) {
        try {
            return stub.getConsumerEntry(consumerServiceName);
        } catch (RemoteException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public int getNumberOfPages() {
        try {
            return stub.getNumberOfPages();
        } catch (RemoteException e) {
            log.error(e.getMessage(), e);
            return 0;
        }
    }

    public boolean deleteConsumerEntry(String consumerServiceName) {
        try {
            stub.deleteConsumerEntry(consumerServiceName);
        } catch (RemoteException e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }
}
