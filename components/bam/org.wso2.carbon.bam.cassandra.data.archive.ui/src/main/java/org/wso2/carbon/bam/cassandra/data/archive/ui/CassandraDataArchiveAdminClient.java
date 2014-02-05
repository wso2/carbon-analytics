package org.wso2.carbon.bam.cassandra.data.archive.ui;


import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.cassandra.data.archive.stub.CassandraArchivalServiceException;
import org.wso2.carbon.bam.cassandra.data.archive.stub.CassandraArchivalServiceStub;
import org.wso2.carbon.bam.cassandra.data.archive.stub.util.ArchiveConfiguration;

import java.rmi.RemoteException;
import java.util.Locale;
import java.util.ResourceBundle;

public class CassandraDataArchiveAdminClient {

    private static final Log log = LogFactory.getLog(CassandraDataArchiveAdminClient.class);
    private static final String BUNDLE = "org.wso2.carbon.bam.cassandra.data.archive.ui.i18n.Resources";
    private CassandraArchivalServiceStub stub;
    private ResourceBundle bundle;


    public CassandraDataArchiveAdminClient(String cookie, String backendServerURL, ConfigurationContext configContext, Locale locale) throws AxisFault {
        String serviceURL = backendServerURL + "CassandraArchivalService";
        bundle = ResourceBundle.getBundle(BUNDLE, locale);

        stub = new CassandraArchivalServiceStub(configContext, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    public void archiveCassandraData(ArchiveConfiguration archiveConfig) throws RemoteException, CassandraArchivalServiceException {
        stub.archiveCassandraData(archiveConfig);
    }
}
