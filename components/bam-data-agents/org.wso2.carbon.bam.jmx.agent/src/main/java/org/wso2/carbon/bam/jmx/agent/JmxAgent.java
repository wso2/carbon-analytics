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

import org.apache.log4j.Logger;
import org.wso2.carbon.bam.jmx.agent.exceptions.JmxConnectionException;
import org.wso2.carbon.bam.jmx.agent.exceptions.JmxMBeanException;
import org.wso2.carbon.bam.jmx.agent.profiles.Profile;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JmxAgent {

    private static Logger log = Logger.getLogger(JmxAgent.class);

    private Profile profile;

    public Profile getProfile() {
        return profile;
    }

    public JmxAgent(Profile profile) {
        this.profile = profile;
    }

    public JMXConnector openJmxConnection() throws JmxConnectionException {

        if (profile == null) {
            log.error("Profile is null.");
            throw new JmxConnectionException("Profile is null.");
        }

        if (log.isDebugEnabled()) {
            log.debug("Prepare open new jmx connection to " + profile.getUrl());
        }

        JMXConnector jmxConnector = null;

        try {
            JMXServiceURL jmxServiceURL = new JMXServiceURL(profile.getUrl());

            Map<String, String[]> map = new HashMap<String, String[]>(1);
            map.put(JmxConstant.JMX_REMOTE_CREDENTIALS_STR,
                    new String[]{profile.getUserName(), profile.getPass()});

            jmxConnector = JMXConnectorFactory.connect(jmxServiceURL, map);

            if (log.isDebugEnabled()) {
                log.debug("Successfully created for Jmx connection to " + profile.getUrl());
            }

        } catch (IOException ex) {
            log.error("Unable to create JMX connection.", ex);
            throw new JmxConnectionException("Unable to create JMX connection.", ex);
        }

        return jmxConnector;
    }


    public void closeJmxConnection(JMXConnector jmxConnector) throws JmxConnectionException {
        if (jmxConnector != null) {
            try {
                jmxConnector.close();

                if (log.isDebugEnabled()) {
                    log.debug("Successfully close the jmx connection to" + ((profile != null) ? profile.getUrl() : ""));
                }
            } catch (IOException e) {
                log.error("Unable to close JMX connection.", e);
                throw new JmxConnectionException("Unable to close JMX connection", e);
            }
        }
    }


    public Object getAttribute(JMXConnector jmxConnector, String mBean, String attr)
            throws JmxMBeanException {

        if (jmxConnector != null) {
            try {
                MBeanServerConnection mBeanServerConnection = jmxConnector.getMBeanServerConnection();
                ObjectName mBeanName = new ObjectName(mBean);
                if (mBeanServerConnection.isRegistered(mBeanName)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Found MBean:" + mBean);
                    }
                    return mBeanServerConnection.getAttribute(mBeanName, attr);
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("MBean is not registered " + mBean);
                    }
                }
            } catch (IOException e) {
                log.error("Unable to open MBean server connection", e);
                throw new JmxMBeanException("Unable to open MBean server connection", e);
            } catch (MalformedObjectNameException e) {
                log.error("Object name issue", e);
                throw new JmxMBeanException("Object name issue", e);
            } catch (AttributeNotFoundException e) {
                log.error("Cannot find the mbean", e);
                throw new JmxMBeanException("Cannot find the mbean", e);
            } catch (MBeanException e) {
                log.error("MBean exception", e);
                throw new JmxMBeanException("MBean exception", e);
            } catch (ReflectionException e) {
                log.error("Reflection exception", e);
                throw new JmxMBeanException("Reflection exception", e);
            } catch (InstanceNotFoundException e) {
                log.error("Instance not found exception", e);
                throw new JmxMBeanException("Instance not found exception", e);
            }
        }
        return null;
    }
}
