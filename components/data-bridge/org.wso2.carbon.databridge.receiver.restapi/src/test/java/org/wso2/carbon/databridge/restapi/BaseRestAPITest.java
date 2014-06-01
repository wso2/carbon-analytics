package org.wso2.carbon.databridge.restapi;

import org.apache.cxf.endpoint.Server;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class BaseRestAPITest {


    private static Server server;
    private static String testURI;

    @BeforeClass
    public static void startupJAXRSContainer() throws Exception {


//        JAXRSServerFactoryBean serverFactory = new JAXRSServerFactoryBean();
//        serverFactory.setResourceClasses(StreamsService.class);
//        serverFactory.setResourceProvider(StreamsService.class,
//                new SingletonResourceProvider(new StreamsService()));
//        testURI = "http://localhost:9000/";
//        serverFactory.setAddress(testURI);
//
//        server = serverFactory.create();

//        JAXRSServerFactoryBean sf  = new JAXRSServerFactoryBean();
//
//          BindingFactoryManager manager = sf.getBus().getExtension(BindingFactoryManager.class);
//          JAXRSBindingFactory factory = new JAXRSBindingFactory();
//          factory.setBus(sf.getBus());
//          manager.registerBindingFactory(JAXRSBindingFactory.JAXRS_BINDING_ID, factory);
//
//        JSONProvider jsonProvider = new JSONProvider();
//
//          if (jsonProvider == null) {
//              Map<String, String> namespaceMap    = new HashMap<String, String>();
//              XmlSchema xmlSchemaAnnotation     = NextVersion.class.getPackage().getAnnotation(XmlSchema.class);
//              namespaceMap.put(xmlSchemaAnnotation.namespace(), "cns");
//              jsonProvider    = new JSONProvider();
//              jsonProvider.setNamespaceMap(namespaceMap);
//              jsonProvider.setIgnoreNamespaces(false);
//          }
//
//          sf.setProvider(jsonProvider);
//
//
//          Map<Object, Object>  extensionsMap   = new HashMap<Object, Object>();
//          extensionsMap.put("json", "application/json");
//          extensionsMap.put("xml", "application/xml");
//
//          sf.setExtensionMappings(extensionsMap);
//
//          sf.setServiceBean(new StreamsService());
//          sf.getInInterceptors().add(new LoggingInInterceptor());
//          sf.getOutInterceptors().add(new LoggingOutInterceptor());
//
//          sf.setAddress("http://localhost:9000/");
//
//          sf.create();
    }

    @Test
    public void testHappyPath() {

//        WebClient client = WebClient.create(testURI);
//        Response response = client.get();
//        System.out.println(response);

    }

    @AfterClass
    public static void shutdownJAXRSContainer() throws Exception {
//        server.stop();
//        server.destroy();
    }
}
