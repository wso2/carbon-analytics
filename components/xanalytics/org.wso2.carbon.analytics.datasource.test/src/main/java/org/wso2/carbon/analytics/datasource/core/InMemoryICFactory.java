/*
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.analytics.datasource.core;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

/**
 * In-memory JNDI initial context factory implementation.
 */
public class InMemoryICFactory implements InitialContextFactory {

    private static Context context;
    
    static {
        try {
            context = new InitialContext(true) {
                private Map<String, Object> bindings = new HashMap<String, Object>();

                @Override
                public void bind(String name, Object obj)
                        throws NamingException {
                    this.bindings.put(name, obj);
                }

                @Override
                public Object lookup(String name) throws NamingException {
                    return this.bindings.get(name);
                }
            };
        } catch (NamingException ignore) { } 
    }
    
    @Override
    public Context getInitialContext(Hashtable<?, ?> environment)
            throws NamingException {
        return context;
    }
    
}