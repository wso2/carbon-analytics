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
package org.wso2.carbon.dashboard.social;

import com.google.common.collect.ImmutableSet;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import org.apache.shindig.gadgets.http.InvalidationHandler;
import org.apache.shindig.gadgets.servlet.HttpRequestHandler;
import org.apache.shindig.social.core.config.SocialApiGuiceModule;
import org.apache.shindig.social.opensocial.oauth.OAuthDataStore;
import org.apache.shindig.social.opensocial.spi.ActivityService;
import org.apache.shindig.social.opensocial.spi.AppDataService;
import org.apache.shindig.social.opensocial.spi.PersonService;
import org.apache.shindig.social.sample.oauth.SampleOAuthDataStore;
import org.apache.shindig.social.sample.oauth.SampleRealm;
import org.wso2.carbon.dashboard.social.handlers.GSActivityHandler;
import org.wso2.carbon.dashboard.social.handlers.GSAppDataHandler;
import org.wso2.carbon.dashboard.social.handlers.GSPersonHandler;
import org.wso2.carbon.dashboard.social.services.GSActivityService;
import org.wso2.carbon.dashboard.social.services.GSAppDataService;
import org.wso2.carbon.dashboard.social.services.GSPersonService;

import java.util.Set;


public class GuiceModuleImpl extends SocialApiGuiceModule {

    @Override
    protected void configure() {
        super.configure();

      /*  bind(String.class).annotatedWith(Names.named("shindig.canonical.json.db"))
                .toInstance("sampledata/canonicaldb.json");*/

        bind(ActivityService.class).to(GSActivityService.class);
        bind(AppDataService.class).to(GSAppDataService.class);
        bind(PersonService.class).to(GSPersonService.class);
        //bind(MessageService.class).to(GSMessageService.class);

        bind(OAuthDataStore.class).to(GSOAuthDataStore.class);
       // bind(new TypeLiteral<Set<Object>>(){}).annotatedWith(Names.named("org.apache.shindig.gadgets.handlers")).toInstance(getHandlers());
      //    bind(new TypeLiteral<Set<Object>>(){}).annotatedWith(Names.named("org.apache.shindig.gadgets.handlers")).toInstance(getGadgetHandlers());


        // We do this so that jsecurity realms can get access to the jsondbservice singleton
        //requestStaticInjection(SampleRealm.class);
    }

    @Override
    protected Set<Object> getHandlers() {
        ImmutableSet.Builder<Object> handlers = ImmutableSet.builder();
       //handlers.addAll(super.getHandlers());
        handlers.add(GSActivityHandler.class);
        handlers.add(GSAppDataHandler.class);
        handlers.add(GSPersonHandler.class);
        //handlers.add(GSMessageHandler.class);
        return handlers.build();
    }
    private Set<Object> getGadgetHandlers(){
         ImmutableSet.Builder<Object> handlers = ImmutableSet.builder();
        handlers.add(InvalidationHandler.class);
        handlers.add(HttpRequestHandler.class);
         handlers.add(GSActivityHandler.class);
        handlers.add(GSAppDataHandler.class);
        handlers.add(GSPersonHandler.class);
         return handlers.build();
    }


}
