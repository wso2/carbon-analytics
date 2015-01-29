<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.bam.jmx.agent.stub.JmxAgentIOExceptionException" %>
<%@ page import="org.wso2.carbon.bam.jmx.agent.stub.profiles.xsd.Profile" %>
<%@ page import="org.wso2.carbon.bam.jmx.agent.ui.JmxConnector" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.io.IOException" %>
<%@ page import="java.rmi.RemoteException" %>

<%
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);

    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().
                    getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    JmxConnector connector = new JmxConnector(configContext, serverURL, cookie);

    //get the data
    String url = request.getParameter("jmxServerUrl");
    String userName = request.getParameter("jmxUserName");
    String password = request.getParameter("jmxUserPass");


    //if mbeans are needed
    if (request.getParameter("requestType").equalsIgnoreCase("mBeans")) {
        String[][] mBeans;
        try {
            mBeans = connector.getMBeans(url, userName, password);
            if (mBeans != null) {
                for (String[] mBean : mBeans) {
                    try {
                        for (int k = 0; k < mBean.length; k++) {

                            if (k == 0) {
                                continue;
                            }
                            out.print(mBean[k] + "____");
                        }
                    } catch (IOException e) {
                        out.print(e.getMessage());
                    }
                }
            }

        } catch (RemoteException e) {
            //allows to propagate the exceptions to the UI layer
            out.print(e.getMessage());
        } catch (JmxAgentIOExceptionException e) {
            //allows to propagate the exceptions to the UI layer
            out.print(e.getMessage());
        }
    }

    //if attributes are needed
    if (request.getParameter("requestType").equalsIgnoreCase("atributes")) {

        String attribute = request.getParameter("mBean");
        try {
            String[][] attributes =
                    connector.getMBeanAttributes(attribute, url, userName, password);

            //if there are attributes
            if (attributes != null) {
                for (String[] attr : attributes) {
                    //if the attribute is a simple type
                    if (attr.length < 2) {
                        out.print(attr[0] + "____");
                    }

                    //if this is a composite attribute
                    else {
                        for (String compositeArr : attr) {
                            out.print(compositeArr + "+;+;");
                        }
                        out.print("____");
                    }
                }
            }
        } catch (RemoteException e) {
            //allows to propagate the exceptions to the UI layer
            out.print(e.getMessage());
        }
    }

    //if the profiles list is needed
    if (request.getParameter("requestType").equalsIgnoreCase("profiles")) {
        Profile[] profiles = connector.getAllProfiles();

        if (profiles != null) {
            if (profiles[0] != null) {
                for (Profile profile : profiles) {
                    out.print(profile.getName() + "____");
                }
            }
        }
    }


    //check for the availability of the DataPublisher
    if (request.getParameter("requestType").equalsIgnoreCase("dpAvailability")) {
        String connectionType = request.getParameter("pubConnType");
        String address = request.getParameter("dpUrl");
        String ipAddress = address.split(":")[0];
        int port = Integer.parseInt(address.split(":")[1]);

        out.print(connector.checkDataPublisherAvailability(connectionType, ipAddress, port));
    }


%>

