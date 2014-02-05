/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.event.processor.siddhi.extension;


import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.processor.siddhi.extension.internal.SiddhiExtensionValueHolder;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.event.AtomicEvent;
import org.wso2.siddhi.core.exception.QueryCreationException;
import org.wso2.siddhi.core.executor.expression.ConstantExpressionExecutor;
import org.wso2.siddhi.core.executor.expression.ExpressionExecutor;
import org.wso2.siddhi.core.executor.function.FunctionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.extension.annotation.SiddhiExtension;

@SiddhiExtension(namespace = "wso2", function = "getRegistryResource")
public class GetRegistryResourceExecutorFunction extends FunctionExecutor {
    Attribute.Type returnType = Attribute.Type.STRING;
    private ExpressionExecutor pathExpressionExecutor;
    private String path;
    private ResourceExecutor executor;
    private Registry registry;


    @Override
    public void init(Attribute.Type[] types, SiddhiContext siddhiContext) {

        if (attributeSize != 2) {
            throw new QueryCreationException("GetRegistryProperty has to have 1 expression; resource path, but " + attributeSize + " expressions provided!");
        }
        pathExpressionExecutor = attributeExpressionExecutors.get(0);

        if (pathExpressionExecutor instanceof ConstantExpressionExecutor && pathExpressionExecutor.getReturnType() == Attribute.Type.STRING) {
            path = (String) pathExpressionExecutor.execute(null);
        } else {
            throw new QueryCreationException("GetRegistryProperty's 1st expression should be resource path with siring type, but found " + pathExpressionExecutor.getReturnType());
        }

        if (path == null) {
            executor = new ResourceExecutor() {
                @Override
                public Object execute(AtomicEvent event) {
                    String path = (String) pathExpressionExecutor.execute(event);
                    if (path.startsWith("gov:")) {
                        try {
                            Registry registry = SiddhiExtensionValueHolder.getInstance().getGovernanceRegistry(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
                            Resource resource = registry.get(path.replaceFirst("gov:", ""));
                            return (RegistryUtils.decodeBytes((byte[]) resource.getContent())).toString();
                        } catch (RegistryException e) {
                            return null;
                        }
                    } else {
                        try {
                            Registry registry = SiddhiExtensionValueHolder.getInstance().getConfigRegistry(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
                            Resource resource = registry.get(path.replaceFirst("conf:", ""));
                            return (RegistryUtils.decodeBytes((byte[]) resource.getContent())).toString();
                        } catch (RegistryException e) {
                            return null;
                        }
                    }
                }
            };
        } else {
            path = (String) pathExpressionExecutor.execute(null);
            if (path.startsWith("gov:")) {
                path = path.replaceFirst("gov:", "");
                try {
                    registry = SiddhiExtensionValueHolder.getInstance().getGovernanceRegistry(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
                    executor = new ResourceExecutor() {
                        @Override
                        public Object execute(AtomicEvent event) {
                            try {
                                Resource resource = registry.get(path);
                                return (RegistryUtils.decodeBytes((byte[]) resource.getContent())).toString();
                            } catch (RegistryException e) {
                                return null;
                            }

                        }
                    };
                } catch (RegistryException e) {
                    throw new QueryCreationException("GetRegistryProperty cannot obtain GovernanceRegistry from " + pathExpressionExecutor.execute(null));
                }
            } else {
                path = path.replaceFirst("conf:", "");
                try {
                    registry = SiddhiExtensionValueHolder.getInstance().getConfigRegistry(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
                    executor = new ResourceExecutor() {
                        @Override
                        public Object execute(AtomicEvent event) {
                            try {
                                Resource resource = registry.get(path);
                                return (RegistryUtils.decodeBytes((byte[]) resource.getContent())).toString();
                            } catch (RegistryException e) {
                                return null;
                            }

                        }
                    };
                } catch (RegistryException e) {
                    throw new QueryCreationException("GetRegistryProperty cannot obtain ConfigRegistry from " + pathExpressionExecutor.execute(null));
                }

            }
        }

    }

    @Override
    public Object execute(AtomicEvent event) {
        return executor.execute(event);
    }

    @Override
    public Attribute.Type getReturnType() {
        return returnType;
    }


    protected Object process(Object obj) {
        //this will not be called
        return null;
    }

    @Override
    public void destroy() {

    }


    interface ResourceExecutor {
        Object execute(AtomicEvent event);
    }

}

