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

package org.wso2.carbon.bam.jmx.agent.tasks;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.data.publisher.util.PublisherUtil;
import org.wso2.carbon.bam.jmx.agent.JmxAgent;
import org.wso2.carbon.bam.jmx.agent.JmxConstant;
import org.wso2.carbon.bam.jmx.agent.TenantPublisherConfigData;
import org.wso2.carbon.bam.jmx.agent.exceptions.JmxConnectionException;
import org.wso2.carbon.bam.jmx.agent.exceptions.JmxMBeanException;
import org.wso2.carbon.bam.jmx.agent.exceptions.JmxProfileException;
import org.wso2.carbon.bam.jmx.agent.exceptions.ProfileDoesNotExistException;
import org.wso2.carbon.bam.jmx.agent.profiles.MBean;
import org.wso2.carbon.bam.jmx.agent.profiles.MBeanAttribute;
import org.wso2.carbon.bam.jmx.agent.profiles.MBeanAttributeProperty;
import org.wso2.carbon.bam.jmx.agent.profiles.Profile;
import org.wso2.carbon.bam.jmx.agent.profiles.ProfileManager;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.exception.AuthenticationException;
import org.wso2.carbon.databridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.StreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import org.wso2.carbon.ntask.core.AbstractTask;

import javax.management.openmbean.CompositeData;
import javax.management.remote.JMXConnector;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Map;

public class JmxTask extends AbstractTask {

    private static final Log log = LogFactory.getLog(JmxTask.class);

    private static final String EVENT_TYPE = "externalEvent";
    private static final String STR_NULL = "NULL";
    private static final char FORWARD_SLASH = '/';
    static final String STREAM_NAME_PREFIX = "jmx.agent.";

    public JmxTask() {
    }

    @Override
    public void execute() {

        if (log.isDebugEnabled()) {
            log.info("Running the profile : " + this.getProperties().
                    get(JmxConstant.JMX_PROFILE_NAME));
        }

        Map<String, String> dataMap = this.getProperties();
        //get profile name
        String profileName = dataMap.get(JmxConstant.JMX_PROFILE_NAME);

        //get the profile
        Profile profile;
        try {
            profile = new ProfileManager().getProfile(profileName);
        } catch (ProfileDoesNotExistException e) {
            log.error("Profile does not exist:" + profileName, e);
            return;
        } catch (JmxProfileException e) {
            log.error("Exception occurred: ", e);
            return;
        }

        if (profile != null) {
            //Publish the data
            try {
                //create a Jmx JmxAgent to fetch Jmx data
                JmxAgent jmxAgent = new JmxAgent(profile);
                //Create a Stream name
                String streamName = STREAM_NAME_PREFIX + profile.getName();

                //Append ".0.0 for the sake of string matching! "
                String version = Integer.toString(profile.getVersion()) + ".0.0";

                DataPublisher dataPublisher = createDataPublisher(profile);
                String streamId = dataPublisher.findStreamId(streamName, version);

                if (streamId == null) {
                    createStreamDefinition(streamName, version, jmxAgent, dataPublisher);
                }
                publishData(streamId, dataPublisher, jmxAgent, profileName);

            } catch (StreamDefinitionException e) {
                log.error("Stream definition seems to be invalid : " + e);
            } catch (AgentException e) {
                log.error(e.getErrorMessage(), e);
            } catch (MalformedURLException e) {
                log.error(e.getLocalizedMessage(), e);
            } catch (AuthenticationException e) {
                log.error(e.getErrorMessage(), e);
                //remove all the data publishers
                TenantPublisherConfigData.getDataPublisherMap().clear();
                if (log.isDebugEnabled()) {
                    log.info("Data Publisher hash table cleared");
                }
            } catch (TransportException e) {
                log.error(e);
            } catch (DifferentStreamDefinitionAlreadyDefinedException e) {
                log.error(e.getErrorMessage(), e);
            } catch (MalformedStreamDefinitionException e) {
                log.error(e.getErrorMessage(), e);
            } catch (JmxConnectionException e) {
                log.error(e.getLocalizedMessage(), e);
            } catch (JmxMBeanException e) {
                log.error(e.getLocalizedMessage(), e);
            }
        }
    }

    private DataPublisher createDataPublisher(Profile profile)
            throws AgentException, MalformedURLException, AuthenticationException,
                   TransportException {

        String dataPublisherReceiverUrl = profile.getDpReceiverAddress();
        String dataPublisherUname = profile.getDpUserName();
        String dataPublisherPass = profile.getDpPassword();
        String dataPublisherReceiverConnectionType = profile.getDpReceiverConnectionType();
        String dataPublisherSecureConnectionType = profile.getDpSecureUrlConnectionType();
        String dataPublisherSecureUrl = profile.getDpSecureAddress();

        //get the tenant ID
        int tenantID = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        //create the key for data publisher storage. Using this key, the correct data
        //publisher with the correct configuration will be returned
        String key = dataPublisherSecureConnectionType + dataPublisherSecureUrl +
                     dataPublisherReceiverConnectionType + dataPublisherReceiverUrl +
                     dataPublisherUname + dataPublisherPass + String.valueOf(tenantID);

        DataPublisher dataPublisher;

        //check for the availability of the data publisher
        if (TenantPublisherConfigData.getDataPublisherMap().containsKey(key)) {
            if (log.isDebugEnabled()) {
                log.info("DataPublisher exists for tenant " + tenantID);
            }

            dataPublisher = TenantPublisherConfigData.getDataPublisherMap().get(key);

        } else {
            if (log.isDebugEnabled()) {
                log.info("DataPublisher does not exist for tenant " + tenantID);
            }

            dataPublisher = new DataPublisher(dataPublisherSecureConnectionType +
                                              dataPublisherSecureUrl,
                                              dataPublisherReceiverConnectionType +
                                              dataPublisherReceiverUrl,
                                              dataPublisherUname, dataPublisherPass);

            TenantPublisherConfigData.getDataPublisherMap().put(key, dataPublisher);
        }


        return dataPublisher;
    }

    private void publishData(String streamId, DataPublisher dataPublisher,
                             JmxAgent jmxAgent, String profileName) throws AgentException {

        ArrayList<Object> arrayList = getMBeansDetail(jmxAgent);
        if (arrayList == null) {
            return;
        }

        String host = PublisherUtil.getHostAddress();
        if ((jmxAgent.getProfile() != null) && (jmxAgent.getProfile().getUrl() != null)) {
            host = jmxAgent.getProfile().getUrl().substring(18, jmxAgent.getProfile().getUrl().indexOf(FORWARD_SLASH, 19));
        }

        Event jmxEvent = new Event(streamId, System.currentTimeMillis(),
                                   new Object[]{EVENT_TYPE, host}, null, arrayList.toArray());
        dataPublisher.publish(jmxEvent);

        if (log.isDebugEnabled()) {
            log.info("jmx Event published for " + profileName);
        }
    }

    private ArrayList<Object> getMBeansDetail(JmxAgent jmxAgent) {
        ArrayList<Object> arrayList = new ArrayList<Object>();
        JMXConnector jmxConnector = null;

        try {
            jmxConnector = jmxAgent.openJmxConnection();

            MBean[] mBeans = jmxAgent.getProfile().getSelectedMBeans();
            Object attrValue;
            for (MBean mBean : mBeans) {
                for (MBeanAttribute mBeanAttribute : mBean.getAttributes()) {
                    // If MBean is a composite.
                    if (mBeanAttribute.getProperties() != null) {
                        CompositeData cd = (CompositeData)
                                jmxAgent.getAttribute(jmxConnector, mBean.getMBeanName(), mBeanAttribute.getAttributeName());

                        for (MBeanAttributeProperty mBeanAttributeProperty : mBeanAttribute.getProperties()) {
                            attrValue = cd.get(mBeanAttributeProperty.getPropertyName());
                            addMBeanDetail(arrayList, attrValue);
                        }
                    } else {
                        attrValue = jmxAgent.getAttribute(jmxConnector, mBean.getMBeanName(), mBeanAttribute.getAttributeName());
                        addMBeanDetail(arrayList, attrValue);
                    }
                }
            }
        } catch (JmxConnectionException e) {
            log.error("Jmx Connection Exception", e);
            return null;
        } catch (JmxMBeanException e) {
            log.error("Jmx MBean exception", e);
            return null;
        } finally {
            if (jmxConnector != null) {
                try {
                    jmxAgent.closeJmxConnection(jmxConnector);
                } catch (JmxConnectionException e) {
                    log.error("Unable to close JMX connection.", e);
                }
            }
        }
        return arrayList;
    }

    private void addMBeanDetail(ArrayList<Object> arrayList, Object attrValue) {
        if (attrValue == null) {
            arrayList.add(STR_NULL);
        } else {
            if (attrValue instanceof String || attrValue instanceof Integer ||
                attrValue instanceof Long || attrValue instanceof Double ||
                attrValue instanceof Boolean || attrValue instanceof Float) {
                arrayList.add(attrValue);
            }
        }
    }

    private void createStreamDefinition(String streamName, String version, JmxAgent jmxAgent,
                                        DataPublisher dataPublisher)
            throws MalformedURLException, StreamDefinitionException,
                   DifferentStreamDefinitionAlreadyDefinedException, AgentException,
                   MalformedStreamDefinitionException, JmxConnectionException, JmxMBeanException {

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.
                append("{'name':'").append(streamName).append("',").
                append("  'version':'").append(version).append("',").
                append("  'nickName': 'JMX Dump',").
                append("  'description': 'JMX monitoring data',").
                append("  'metaData':[").
                append("          {'name':'clientType','type':'STRING'},").
                append("          {'name':'host','type':'STRING'}").
                append("  ],").
                append("  'payloadData':[");

        JMXConnector jmxConnector = null;

        try {
            jmxConnector = jmxAgent.openJmxConnection();

            MBean[] mBeans = jmxAgent.getProfile().getSelectedMBeans();
            //add the attributes
            Object attrValue;
            for (MBean mBean : mBeans) {
                for (MBeanAttribute mBeanAttribute : mBean.getAttributes()) {

                    // If MBean is a composite.
                    if (mBeanAttribute.getProperties() != null) {
                        CompositeData cd = (CompositeData)
                                jmxAgent.getAttribute(jmxConnector, mBean.getMBeanName(), mBeanAttribute.getAttributeName());

                        for (MBeanAttributeProperty mBeanAttributeProperty : mBeanAttribute.getProperties()) {
                            attrValue = cd.get(mBeanAttributeProperty.getPropertyName());
                            appendColumnName(stringBuilder, attrValue, mBeanAttributeProperty.getAliasName());
                        }
                    } else {
                        attrValue = jmxAgent.getAttribute(jmxConnector, mBean.getMBeanName(), mBeanAttribute.getAttributeName());
                        appendColumnName(stringBuilder, attrValue, mBeanAttribute.getAliasName());
                    }
                }
            }

            //to delete the last comma
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            stringBuilder.append("  ]}");

            dataPublisher.defineStream(stringBuilder.toString());

        } finally {
            if (jmxConnector != null) {
                try {
                    jmxAgent.closeJmxConnection(jmxConnector);
                } catch (JmxConnectionException e) {
                    log.error("Unable to close Jmx connection.", e);
                }
            }
        }
    }

    private void appendColumnName(StringBuilder stringBuilder, Object attrValue, String alias) {

        //if the value is a string
        if (attrValue instanceof String) {
            stringBuilder.append("{'name':'").append(alias).append("','type':'STRING'},");
        }
        //if the value is an integer
        else if (attrValue instanceof Integer) {
            stringBuilder.append("{'name':'").append(alias).append("','type':'INT'},");
        }
        //if the value is a long
        else if (attrValue instanceof Long) {
            stringBuilder.append("{'name':'").append(alias).append("','type':'LONG'},");
        }
        //if the value is a double
        else if (attrValue instanceof Double) {
            stringBuilder.append("{'name':'").append(alias).append("','type':'DOUBLE'},");
        }
        //if the value is a boolean
        else if (attrValue instanceof Boolean) {
            stringBuilder.append("{'name':'").append(alias).append("','type':'BOOL'},");
        }
        //if the value is a float
        else if (attrValue instanceof Float) {
            stringBuilder.append("{'name':'").append(alias).append("','type':'FLOAT'},");
        } else {
            log.error("Missed attribute in stream def: " + alias);
        }
    }
}
