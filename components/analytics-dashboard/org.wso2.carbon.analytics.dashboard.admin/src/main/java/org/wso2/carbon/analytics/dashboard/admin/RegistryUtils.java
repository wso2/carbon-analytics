/**
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.

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
package org.wso2.carbon.analytics.dashboard.admin;

import com.google.gson.Gson;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.registry.api.Collection;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class RegistryUtils {

	//TODO- Use privileged carbon context
	private static CarbonContext carbonContext = CarbonContext.getThreadLocalCarbonContext();
	private static Registry registry = carbonContext.getRegistry(RegistryType.SYSTEM_GOVERNANCE);

	/**
	 * Logger
	 */
	private static Log logger = LogFactory.getLog(RegistryUtils.class);

	/**
	 * Writes an object to the given registry url and registry type as a Json String.
	 *
	 * @param url     Relative url to where the resource will be saved.
	 * @param content Data to be written to the registry as a json string. Must be a bean object(eg- String objects are not supported).
	 */
	protected static void writeResource(String url, Object content) throws AxisFault {
		try {
			Gson gson = new Gson();

			Resource resource = registry.newResource(); //new resource in the registry
			resource.setContent(gson.toJson(content));

			resource.setMediaType("application/json");
			registry.put(url, resource);

		} catch (Exception re) {
			String errorMessage = "Unable to write resource at url:" + url;
			logger.error(errorMessage);
			throw new AxisFault(errorMessage);
		}
	}

	/**
	 * Reads a resource from the registry
	 *
	 * @param url         Relative url from where the resource will be read.
	 * @param targetClass Target object type which the json content will be mapped into.
	 * @return Object by mapping into a given class, the json read from the registry.
	 */
	protected static Object readResource(String url, Class targetClass) throws AxisFault {
		InputStream contentStream = null;
		InputStreamReader isr = null;
		try {
			Resource resource = registry.get(url);
			Gson gson = new Gson();

			contentStream = resource.getContentStream();
			isr = new InputStreamReader(contentStream);

			return gson.fromJson(isr, targetClass);

		} catch (Exception e) {
			String errorMessage = "Unable to read resource from url:" + url;
			logger.error(errorMessage);
			throw new AxisFault(errorMessage);
		} finally {
			closeQuitely(contentStream);
			closeQuitely(isr);
		}
	}

	/**
	 * Close colseables
	 */
	private static void closeQuitely(Closeable closeable) {
		try {
			if (closeable != null) {
				closeable.close();
			}
		} catch (IOException ignore) {
	    /* ignore */
		}
	}

	/**
	 * Checks if a resource with given name(url->location+resource name) exits in the registry.
	 *
	 * @param url Resource url relative to the registry.
	 * @return True if resource exists.
	 * @throws AxisFault .
	 */
	protected static boolean isResourceExist(String url) throws AxisFault {
		try {
			return registry.resourceExists(url);
		} catch (RegistryException e) {
			String errorMessage = "Unable perform isResourceExists check";
			logger.error(errorMessage);
			throw new AxisFault(e.getMessage(), e);
		}
	}

	/**
	 * Get all the resources in a directory url as a Collection.
	 *
	 * @param collectionURL Collection url relative to the registry.
	 * @return Collection.
	 * @throws AxisFault
	 */
	protected static Collection readCollection(String collectionURL) throws AxisFault {

		try {
			return (Collection) registry.get(collectionURL);
		} catch (Exception e) {
			String errorMessage = "Unable to read resource collection from url:" + collectionURL;
			logger.error(errorMessage);
			throw new AxisFault(errorMessage);
		}
	}

	/**
	 * @param url url of the resource to be deleted from the registry.
	 * @throws AxisFault
	 */
	protected static boolean deleteResource(String url) throws AxisFault {
		try {
			registry.delete(url);
			return true;
		} catch (Exception e) {
			String errorMessage = "Unable to delete resource at url:" + url;
			logger.error(errorMessage);
			return false;
		}
	}

}
