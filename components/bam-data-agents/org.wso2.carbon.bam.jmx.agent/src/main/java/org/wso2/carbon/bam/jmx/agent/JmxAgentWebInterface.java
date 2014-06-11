/*
*  Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.

  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*
*/

package org.wso2.carbon.bam.jmx.agent;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.jmx.agent.exceptions.JmxProfileException;
import org.wso2.carbon.bam.jmx.agent.exceptions.ProfileAlreadyExistsException;
import org.wso2.carbon.bam.jmx.agent.exceptions.ProfileDoesNotExistException;
import org.wso2.carbon.bam.jmx.agent.profiles.Profile;
import org.wso2.carbon.bam.jmx.agent.profiles.ProfileManager;
import org.wso2.carbon.bam.jmx.agent.tasks.JmxTaskAdmin;
import org.wso2.carbon.core.AbstractAdmin;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.RuntimeMBeanException;
import javax.management.RuntimeOperationsException;
import javax.management.openmbean.CompositeData;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketAddress;
import java.rmi.UnmarshalException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


public class JmxAgentWebInterface extends AbstractAdmin {

    private static final Log log = LogFactory.getLog(JmxAgentWebInterface.class);
    private ProfileManager profMan;
    private JmxTaskAdmin admin;


    public JmxAgentWebInterface() {
        profMan = new ProfileManager();
        admin = new JmxTaskAdmin();
    }

    /**
     * Adds a profile to be monitored
     *
     * @param profile The profile to be monitored
     * @return Whether adding was successful or not
     * @throws ProfileAlreadyExistsException
     */
    public boolean addProfile(Profile profile)
            throws ProfileAlreadyExistsException, JmxProfileException {

        profMan.addProfile(profile);

        //if the profile needs to be scheduled
        if (profile.isActive()) {
            try {
                admin.scheduleProfile(profile);
            } catch (AxisFault axisFault) {
                log.error(axisFault);
            }
        }

        return true;
    }

    /**
     * @param profileName : The name of the profile that is needed
     * @return : A profile object
     * @throws ProfileDoesNotExistException
     */
    public Profile getProfile(String profileName)
            throws ProfileDoesNotExistException, JmxProfileException {

        return profMan.getProfile(profileName);
    }

    /**
     * Updates a monitoring profile
     *
     * @param profile The profile to be monitored
     * @return Whether updating was successful or not
     * @throws ProfileDoesNotExistException
     */
    public boolean updateProfile(Profile profile)
            throws ProfileDoesNotExistException, JmxProfileException {
        //reschedule the profile if the profile is already scheduled
        //else the profile monitoring period will not change
        //(if the cron has been changed)

        if (profile.isActive()) {
            try {
                admin.removeProfile(profile.getName());
                admin.scheduleProfile(profile);
            } catch (AxisFault axisFault) {
                log.error(axisFault);
            }
        }

        return profMan.updateProfile(profile);
    }

    /**
     * Deletes a monitoring profile
     *
     * @param profileName The name of the profile to be deleted
     * @return Whether delete operation was successful or not
     * @throws ProfileDoesNotExistException
     */
    public boolean deleteProfile(String profileName)
            throws ProfileDoesNotExistException, JmxProfileException {
        JmxTaskAdmin admin = new JmxTaskAdmin();

        //if the task is scheduled
        try {
            if (admin.profileExists(profileName)) {
                admin.removeProfile(profileName);
            }
        } catch (AxisFault e) {
            log.error(e);
        }
        return profMan.deleteProfile(profileName);
    }

    /**
     * Start to monitor a profile
     *
     * @param profileName Name of the profile to be monitored
     * @throws ProfileDoesNotExistException
     * @throws AxisFault
     */
    public void startMonitoringProfile(String profileName)
            throws ProfileDoesNotExistException, AxisFault {

        try {
            Profile profile = profMan.getProfile(profileName);

            //don't do this if the task is already scheduled
            if (!admin.isTaskScheduled(profileName)) {
                admin.scheduleProfile(profile);

                profile.setActive(true);
                profMan.updateProfile(profile);
            }
        } catch (ProfileDoesNotExistException e) {
            log.error(e);
            throw e;
        } catch (AxisFault axisFault) {
            log.error(axisFault);
            throw new AxisFault("Error Scheduling " + profileName);
        } catch (JmxProfileException e) {
            log.error(e);
        }
    }

    /**
     * Stop monitoring of a profile
     *
     * @param profileName Name of the profile that should be withdrawn from being monitored
     * @throws ProfileDoesNotExistException
     */
    public void stopMonitoringProfile(String profileName) throws ProfileDoesNotExistException {
        try {
            Profile profile = profMan.getProfile(profileName);

            //don't do this if the task is not scheduled
            if (admin.isTaskScheduled(profileName)) {
                admin.removeProfile(profileName);

                profile.setActive(false);
                profMan.updateProfile(profile);
            }
        } catch (ProfileDoesNotExistException e) {
            log.error(e);
            throw e;
        } catch (AxisFault axisFault) {
            log.error(axisFault);
        } catch (JmxProfileException e) {
            log.error(e);
        }
    }

    /**
     * @return : An aaray of all the profiles
     */
    public Profile[] getAllProfiles() throws JmxProfileException {
        return profMan.getAllProfiles();
    }


    /**
     * @param url      : The URL for the JMX server
     * @param userName : The User name for the JMX server
     * @param password : The password for the JMX server
     * @return : The name array of the MBeans which the JMX server. The format of the array is
     *         [Domain name][MBean Canonical name]
     * @throws IOException
     */
    public String[][] getMBeans(String url, String userName, String password)
            throws IOException {

        JMXConnector jmxc = null;

        try {
            jmxc = getJmxConnector(url, userName, password);

            MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();

            int count = 0;
            Set<ObjectName> names = new TreeSet<ObjectName>(mbsc.queryNames(null, null));
            String[][] nameArr = new String[names.size()][2];

            for (ObjectName name : names) {
                nameArr[count][0] = name.getDomain();
                nameArr[count][1] = name.getCanonicalName();
                count++;
            }

            return nameArr;

        } catch (MalformedURLException e) {
            log.error(e);
            throw e;
        } catch (IOException e) {
            log.error(e);
            throw e;
        } finally {
            if (jmxc != null) {
                jmxc.close();
            }
        }
    }

    /**
     * @param mBean    : The name of the MBean
     * @param url      : The URL for the JMX server
     * @param userName : The User name for the JMX server
     * @param password : The password for the JMX server
     * @return : The set of attributes in a MBean
     * @throws MalformedObjectNameException
     * @throws IntrospectionException
     * @throws InstanceNotFoundException
     * @throws IOException
     * @throws ReflectionException
     */
    public String[][] getMBeanAttributeInfo(String mBean, String url, String userName,
                                            String password)
            throws MalformedObjectNameException, IntrospectionException, InstanceNotFoundException,
                   IOException, ReflectionException {


        JMXConnector jmxc = getJmxConnector(url, userName, password);

        MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();
        ObjectName mBeanName = new ObjectName(mBean);

        MBeanAttributeInfo[] attrs = mbsc.getMBeanInfo(mBeanName).getAttributes();

        ArrayList<String[]> strAttrs = new ArrayList<String[]>();

        for (MBeanAttributeInfo info : attrs) {

            //check the suitability of the attribute
            try {
                Object result = mbsc.getAttribute(mBeanName, info.getName());

                //if this is an instance of a primary data type supported by cassandra
                if (result instanceof String || result instanceof Integer ||
                    result instanceof Double || result instanceof Long ||
                    result instanceof Boolean || result instanceof Float) {
                    strAttrs.add(new String[]{info.getName()});
                }

                //if this is a composite data type
                if (result instanceof CompositeData) {
                    CompositeData cd = (CompositeData) result;
                    ArrayList<String> keys = new ArrayList<String>();
                    //add the attribute name
                    keys.add(info.getName());
                    for (String key : cd.getCompositeType().keySet()) {
                        //check whether the key returns a primary data type
                        Object attrValue = cd.get(key);
                        if (attrValue instanceof String || attrValue instanceof Integer ||
                            attrValue instanceof Double || attrValue instanceof Long ||
                            attrValue instanceof Boolean || attrValue instanceof Float) {
                            keys.add(key);
                        }
                    }
                    //if this composite data object has keys which returns attributes with
                    // primary data types
                    if (keys.size() > 1) {
                        strAttrs.add(keys.toArray(new String[keys.size()]));
                    }
                }
            } catch (MBeanException e) {
                log.error("Removed the attribute " + info.getName() + " of " + mBean +
                          " from the UI list due to: " + e.getMessage());
            } catch (AttributeNotFoundException e) {
                log.error("Removed the attribute " + info.getName() + " of " + mBean +
                          " from the UI list due to: " + e.getMessage());
            } catch (UnmarshalException e) {
                log.error("Removed the attribute " + info.getName() + " of " + mBean +
                          " from the UI list due to: " + e.getMessage());
            } catch (RuntimeOperationsException e) {
                log.error("Removed the attribute " + info.getName() + " of " + mBean +
                          " from the UI list due to: " + e.getMessage());

            } catch (RuntimeMBeanException e) {
                log.error("Removed the attribute " + info.getName() + " of " + mBean +
                          " from the UI list due to: " + e.getMessage());
            } catch (ReflectionException e) {
                log.error("Removed the attribute " + info.getName() + " of " + mBean +
                          " from the UI list due to: " + e.getMessage());
            } catch (Exception e) {
                log.error("Removed the attribute " + info.getName() + " of " + mBean +
                          " from the UI list due to: " + e.getMessage());
            }
        }

        //close the connection
        jmxc.close();

        return strAttrs.toArray(new String[strAttrs.size()][]);
    }

    private JMXConnector getJmxConnector(String url, String userName, String password)
            throws IOException {
        JMXServiceURL jmxServiceURL = new JMXServiceURL(url);
        Map<String, String[]> map = new HashMap<String, String[]>(1);

        map.put(JmxConstant.JMX_REMOTE_CREDENTIALS_STR, new String[]{userName, password});
        return JMXConnectorFactory.connect(jmxServiceURL, map);
    }

    /**
     * Test the availability of the DataPublisher by
     * trying to connect to it (credentials are not checked)
     *
     * @return : whether the test was successful or not
     */
    public boolean testDataPublisherAvailability(String connectionType, String url, int port) {

        //check for tcp and ssl port availability
        if (connectionType.equalsIgnoreCase("tcp://") || connectionType.equalsIgnoreCase("ssl://")) {

            DatagramSocket ds = null;

            try {
                ds = new DatagramSocket(port);
                ds.setReuseAddress(true);

                return true;
            } catch (IOException e) {
                log.error(e);
            } finally {
                if (ds != null) {
                    ds.close();
                }
            }
        }

        //check for http and https port availability
        if (connectionType.equalsIgnoreCase("http://") || connectionType.equalsIgnoreCase("https://")) {

            Socket socket = null;

            try {
                socket = new Socket();
                socket.setReuseAddress(true);

                SocketAddress sa = new InetSocketAddress(url, port);
                socket.connect(sa);
                return true;
            } catch (IOException e) {
                log.error(e);
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        //do nothing
                    }
                }
            }
        }

        return false;
    }

    /**
     * Adds the profile for the toolbox
     *
     * @return - Whether the profile was successfully deployed or not
     */
    public boolean addToolboxProfile() {

        boolean success = false;

        try {
            profMan.createToolboxProfile();
            success = true;
        } catch (ProfileAlreadyExistsException e) {
            log.error(e);
        } catch (JmxProfileException e) {
            log.error(e);
        }
        return success;
    }
}
