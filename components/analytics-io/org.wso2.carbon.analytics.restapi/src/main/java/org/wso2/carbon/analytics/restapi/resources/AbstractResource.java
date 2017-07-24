/**
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.analytics.restapi.resources;

import org.wso2.carbon.analytics.restapi.Constants;
import org.wso2.carbon.analytics.restapi.beans.ResponseBean;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * The Abstract Class AbstractResource.
 */
public abstract class AbstractResource {

	/**
	 * Handle response.
	 * @param responseStatus the response status
	 * @param message the message
	 * @return the response
	 */
	public Response handleResponse(ResponseStatus responseStatus, String message) {
		Response response;
		switch (responseStatus) {
			case CONFLICT:
				ResponseBean conflicted = getResponseMessage(Constants.Status.FAILED, message);
				response = Response.status(Status.CONFLICT.getStatusCode()).entity(conflicted).build();
				break;
			case CREATED:
				ResponseBean created = getResponseMessage(Constants.Status.CREATED, message);
				response = Response.status(Status.CREATED.getStatusCode()).entity(created).build();
				break;
			case SUCCESS:
				ResponseBean success = getResponseMessage(Constants.Status.SUCCESS, message);
				response = Response.ok().entity(success).build();
				break;
			case FAILED:
				ResponseBean failed = getResponseMessage(Constants.Status.FAILED, message);
				response = Response.serverError().entity(failed).build();
				break;
			case INVALID:
				ResponseBean invalid = getResponseMessage(Constants.Status.FAILED, message);
				response = Response.status(Status.BAD_REQUEST.getStatusCode()).entity(invalid).build();
				break;
			case FORBIDDEN:
				ResponseBean forbidden = getResponseMessage(Constants.Status.UNAUTHORIZED, message);
				response = Response.status(Status.FORBIDDEN.getStatusCode()).entity(forbidden).build();
				break;
            case UNAUTHENTICATED:
				ResponseBean unauthenticated = getResponseMessage(Constants.Status.UNAUTHORIZED, message);
				response = Response.status(Status.FORBIDDEN.getStatusCode()).entity(unauthenticated).build();
				break;
			case NON_EXISTENT:
				ResponseBean nonExistent =
				                           getResponseMessage(Constants.Status.NON_EXISTENT,
				                                              message);
				response = Response.status(Status.NOT_FOUND.getStatusCode()).entity(nonExistent).build();
				break;
			default:
				response = Response.noContent().build();
				break;
		}
		return response;
	}

	/**
	 * Gets the response message.
	 * @param status the status
	 * @param message the message
	 * @return the response message
	 */
	private ResponseBean getResponseMessage(String status, String message) {
		ResponseBean standardResponse = new ResponseBean(status);
		if (message != null) {
			standardResponse.setMessage(message);
		}
		return standardResponse;
	}

	/**
	 * The Enum ResponseStatus.
	 */
	public enum ResponseStatus {
		/** The "conflict" response */
		CONFLICT,
		/** The "created" response */
		CREATED,
		/** The "success" response. */
		SUCCESS,
		/** The "failed" response. */
		FAILED,
		/** The "invalid" response. */
		INVALID,
		/** The "forbidden" response. */
		FORBIDDEN,
        /** The "forbidden" response. */
        UNAUTHENTICATED,
		/** The "non existent" response. */
		NON_EXISTENT
	}

}
