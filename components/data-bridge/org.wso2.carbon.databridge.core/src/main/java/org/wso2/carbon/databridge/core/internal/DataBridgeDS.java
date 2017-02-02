/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.databridge.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.utils.EventDefinitionConverterUtils;
import org.wso2.carbon.databridge.core.DataBridge;
import org.wso2.carbon.databridge.core.DataBridgeReceiverService;
import org.wso2.carbon.databridge.core.DataBridgeServiceValueHolder;
import org.wso2.carbon.databridge.core.DataBridgeSubscriberService;
import org.wso2.carbon.databridge.core.Utils.AgentSession;
import org.wso2.carbon.databridge.core.definitionstore.AbstractStreamDefinitionStore;
import org.wso2.carbon.databridge.core.definitionstore.InMemoryStreamDefinitionStore;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;
import org.wso2.carbon.databridge.core.internal.authentication.AuthenticationHandler;
import org.wso2.carbon.databridge.core.internal.utils.DataBridgeCoreBuilder;


import java.io.File;
import java.util.List;

/**
 * @scr.component name="databridge.component" immediate="true"
 * @scr.reference name="org.wso2.carbon.identity.authentication.internal.AuthenticationServiceComponent"
 * interface="org.wso2.carbon.identity.authentication.AuthenticationService"
 * cardinality="1..1" policy="dynamic" bind="setAuthenticationService"  unbind="unsetAuthenticationService"
 * @scr.reference name="user.realmservice.default" interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService"  unbind="unsetRealmService"
 * @scr.reference name="stream.definitionStore.service"
 * interface="org.wso2.carbon.databridge.core.definitionstore.AbstractStreamDefinitionStore" cardinality="1..1"
 * policy="dynamic" bind="setEventStreamStoreService" unbind="unsetEventStreamStoreService"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="0..1"
 * policy="dynamic"  bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 */
public class DataBridgeDS {
    private static final Log log = LogFactory.getLog(DataBridgeDS.class);
    private ServiceRegistration receiverServiceRegistration;
    private ServiceRegistration subscriberServiceRegistration;
    private DataBridge databridge;
    /**
     * initialize the agent server here.
     *
     * @param context
     */
    protected void activate(ComponentContext context) {
        File filePath = new File("resources");
        try {
            if (databridge == null) {
                InMemoryStreamDefinitionStore streamDefinitionStore = DataBridgeServiceValueHolder.getStreamDefinitionStore();
                // TODO: 1/31/17 temporary implementation
                databridge = new DataBridge(new AuthenticationHandler() {
                    @Override
                    public boolean authenticate(String userName,
                                                String password) {
                        return true;// allays authenticate to true
                    }

                    @Override
                    public void initContext(AgentSession agentSession) {
                        //To change body of implemented methods use File | Settings | File Templates.
                    }

                    @Override
                    public void destroyContext(AgentSession agentSession) {

                    }
                },streamDefinitionStore, DataBridgeCoreBuilder.getDatabridgeConfigPath());

                databridge = new DataBridge(new AuthenticationHandler() {
                    @Override
                    public boolean authenticate(String userName,
                                                String password) {
                        return true;// always authenticate to true
                    }

                    @Override
                    public void initContext(AgentSession agentSession) {
                        //To change body of implemented methods use File | Settings | File Templates.
                    }

                    @Override
                    public void destroyContext(AgentSession agentSession) {

                    }
                }, streamDefinitionStore, filePath.getAbsolutePath()+File.separator+"data-bridge-config.xml");

                try {
                    List<String> streamDefinitionStrings = DataBridgeCoreBuilder.loadStreamDefinitionXML();
                    for (String streamDefinitionString : streamDefinitionStrings) {
                        try {
                            StreamDefinition streamDefinition = EventDefinitionConverterUtils.convertFromJson(streamDefinitionString);
                            // TODO: 1/24/17  removed tenant dependency (no realm implementation in c5)
                            //int tenantId = DataBridgeServiceValueHolder.getRealmService().getTenantManager().getTenantId(streamDefinitionString[0]);
                            /*if (tenantId == MultitenantConstants.INVALID_TENANT_ID) {
                                log.warn("Tenant " + streamDefinitionString[0] + " does not exist, Error in defining event stream " + streamDefinitionString[1]);
                                continue;
                            }*/

                            streamDefinitionStore.saveStreamDefinition(streamDefinition);


                            /*try {
                                PrivilegedCarbonContext.startTenantFlow();
                                PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                                privilegedCarbonContext.setTenantId(tenantId);
                                privilegedCarbonContext.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
                                privilegedCarbonContext.setTenantDomain(streamDefinitionString[0]);

                                streamDefinitionStore.saveStreamDefinition(streamDefinition, MultitenantConstants.SUPER_TENANT_ID);

                            } catch (DifferentStreamDefinitionAlreadyDefinedException e) {
                                log.warn("Error redefining event stream of " + streamDefinitionString[0] + ": " + streamDefinitionString[1], e);
                            } catch (RuntimeException e) {
                                log.error("Error in defining event stream " + streamDefinitionString[0] + ": " + streamDefinitionString[1], e);
                            } catch (StreamDefinitionStoreException e) {
                                log.error("Error in defining event stream in store " + streamDefinitionString[0] + ": " + streamDefinitionString[1], e);
                            } finally {
                                PrivilegedCarbonContext.endTenantFlow();
                            }*/
                        } catch (MalformedStreamDefinitionException e) {
                            log.error("Malformed Stream Definition for " + streamDefinitionString, e);
                        }
                    }
                } catch (Throwable t) {
                    log.error("Cannot load stream definitions ", t);
                }


                receiverServiceRegistration = context.getBundleContext().
                        registerService(DataBridgeReceiverService.class.getName(), databridge, null);
                subscriberServiceRegistration = context.getBundleContext().
                        registerService(DataBridgeSubscriberService.class.getName(), databridge, null);
//                databridgeRegistration =
//                        context.getBundleContext().registerService(DataBridge.class.getName(), databridge, null);
                log.info("Successfully deployed Agent Server ");
            }
        }  catch (RuntimeException e) {
            log.error("Error in starting Agent Server ", e);
        }
    }


    protected void deactivate(ComponentContext context) {
        context.getBundleContext().ungetService(receiverServiceRegistration.getReference());
        context.getBundleContext().ungetService(subscriberServiceRegistration.getReference());
//        databridgeRegistration.unregister();
        if (log.isDebugEnabled()) {
            log.debug("Successfully stopped agent server");
        }
    }

    /*protected void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    protected void unsetAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = null;
    }*/

    /*protected void setRealmService(RealmService realmService) {
        DataBridgeServiceValueHolder.setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        DataBridgeServiceValueHolder.setRealmService(null);
    }*/

    protected void setEventStreamStoreService(
            InMemoryStreamDefinitionStore abstractStreamDefinitionStore) {
        DataBridgeServiceValueHolder.setStreamDefinitionStore(abstractStreamDefinitionStore);
    }

    protected void unsetEventStreamStoreService(
            InMemoryStreamDefinitionStore abstractStreamDefinitionStore) {
        DataBridgeServiceValueHolder.setStreamDefinitionStore(null);
    }

    /*protected void setConfigurationContextService(ConfigurationContextService contextService) {
        DataBridgeServiceValueHolder.setConfigurationContextService(contextService);
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        DataBridgeServiceValueHolder.setConfigurationContextService(null);
    }*/

}
