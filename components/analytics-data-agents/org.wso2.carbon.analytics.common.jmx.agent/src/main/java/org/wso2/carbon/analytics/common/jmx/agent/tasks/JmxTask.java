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

package org.wso2.carbon.analytics.common.jmx.agent.tasks;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.common.jmx.agent.JmxAgent;
import org.wso2.carbon.analytics.common.jmx.agent.JmxConstant;
import org.wso2.carbon.analytics.common.jmx.agent.PublisherUtil;
import org.wso2.carbon.analytics.common.jmx.agent.exceptions.JmxConnectionException;
import org.wso2.carbon.analytics.common.jmx.agent.exceptions.JmxMBeanException;
import org.wso2.carbon.analytics.common.jmx.agent.exceptions.JmxProfileException;
import org.wso2.carbon.analytics.common.jmx.agent.exceptions.ProfileDoesNotExistException;
import org.wso2.carbon.analytics.common.jmx.agent.profiles.MBean;
import org.wso2.carbon.analytics.common.jmx.agent.profiles.MBeanAttribute;
import org.wso2.carbon.analytics.common.jmx.agent.profiles.MBeanAttributeProperty;
import org.wso2.carbon.analytics.common.jmx.agent.profiles.Profile;
import org.wso2.carbon.analytics.common.jmx.agent.profiles.ProfileManager;
import org.wso2.carbon.analytics.common.jmx.agent.tasks.internal.JmxTaskServiceComponent;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.event.stream.core.EventStreamService;
import org.wso2.carbon.event.stream.core.exception.EventStreamConfigurationException;
import org.wso2.carbon.ntask.core.AbstractTask;

import javax.management.openmbean.CompositeData;
import javax.management.remote.JMXConnector;
import java.util.ArrayList;
import java.util.List;
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
                StreamDefinition streamDefinition = createStreamDefinition(streamName, version, jmxAgent);
                publishData(streamDefinition, jmxAgent, profileName);
            } catch (MalformedStreamDefinitionException e) {
                log.error(e.getErrorMessage(), e);
            } catch (JmxConnectionException | JmxMBeanException e) {
                log.error(e.getLocalizedMessage(), e);
            }
        }
    }

    private void publishData(StreamDefinition streamDef, JmxAgent jmxAgent, String profileName) {
        ArrayList<Object> arrayList = getMBeansDetail(jmxAgent);
        if (arrayList == null) {
            return;
        }
        String host = PublisherUtil.getHostAddress();
        if ((jmxAgent.getProfile() != null) && (jmxAgent.getProfile().getUrl() != null)) {
            host = jmxAgent.getProfile().getUrl().substring(18, jmxAgent.getProfile().getUrl().indexOf(FORWARD_SLASH, 19));
        }
        EventStreamService eventStreamService = JmxTaskServiceComponent.getEventStreamService();
        if (eventStreamService != null) {
            try {
                if (eventStreamService.getStreamDefinition(streamDef.getStreamId()) == null) {
                    eventStreamService.addEventStreamDefinition(streamDef);
                    if (log.isDebugEnabled()) {
                        log.debug("Added stream definition to event publisher service.");
                    }
                }
            } catch (EventStreamConfigurationException e) {
                log.error("Error in adding stream definition to service:" + e.getMessage(), e);
            }
            Event tracingEvent = new Event();
            tracingEvent.setTimeStamp(System.currentTimeMillis());
            tracingEvent.setStreamId(streamDef.getStreamId());
            Object[] metaInfo = new Object[]{EVENT_TYPE, host};
            tracingEvent.setMetaData(metaInfo);
            tracingEvent.setPayloadData(arrayList.toArray());
            eventStreamService.publish(tracingEvent);
            if (log.isDebugEnabled()) {
                log.debug("Successfully published event");
            }
        }
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

    private StreamDefinition createStreamDefinition(String streamName, String version, JmxAgent jmxAgent)
            throws MalformedStreamDefinitionException, JmxConnectionException, JmxMBeanException {

        StreamDefinition streamDefinition = new StreamDefinition(streamName, version);
        streamDefinition.setDescription("JMX monitoring data");
        streamDefinition.setNickName("JMX");
        List<Attribute> metaDataList = getMetaAttributeList();
        streamDefinition.setMetaData(metaDataList);
        List<Attribute> payloadDataList = new ArrayList<>();
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
                            payloadDataList.add(getColumnName(attrValue, mBeanAttributeProperty.getAliasName()));
                        }
                    } else {
                        attrValue = jmxAgent.getAttribute(jmxConnector, mBean.getMBeanName(), mBeanAttribute.getAttributeName());
                        payloadDataList.add(getColumnName(attrValue, mBeanAttribute.getAliasName()));
                    }
                }
            }
        } finally {
            if (jmxConnector != null) {
                try {
                    jmxAgent.closeJmxConnection(jmxConnector);
                } catch (JmxConnectionException e) {
                    log.error("Unable to close Jmx connection.", e);
                }
            }
        }
        streamDefinition.setPayloadData(payloadDataList);
        return streamDefinition;
    }

    private List<Attribute> getMetaAttributeList() {
        List<Attribute> metaDataList = new ArrayList<>();
        metaDataList.add(new Attribute("clientType", AttributeType.STRING));
        metaDataList.add(new Attribute("host", AttributeType.STRING));
        return metaDataList;
    }

    private Attribute getColumnName(Object attrValue, String alias) {
        AttributeType attributeType = null;
        if (attrValue instanceof String) {
            attributeType = AttributeType.STRING;
        } else if (attrValue instanceof Integer) {
            attributeType = AttributeType.INT;
        } else if (attrValue instanceof Long) {
            attributeType = AttributeType.LONG;
        } else if (attrValue instanceof Double) {
            attributeType = AttributeType.DOUBLE;
        } else if (attrValue instanceof Boolean) {
            attributeType = AttributeType.BOOL;
        } else if (attrValue instanceof Float) {
            attributeType = AttributeType.FLOAT;
        } else {
            log.error("Missed attribute in stream def: " + alias);
        }
        return new Attribute(alias, attributeType);
    }
}
