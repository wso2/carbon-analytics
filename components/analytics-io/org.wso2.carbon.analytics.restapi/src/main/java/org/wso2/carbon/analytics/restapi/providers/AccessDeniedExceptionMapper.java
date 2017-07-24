package org.wso2.carbon.analytics.restapi.providers;

/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.restapi.AccessDeniedException;
import org.wso2.carbon.analytics.restapi.Constants;
import org.wso2.carbon.analytics.restapi.beans.ResponseBean;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;


/**
 * The Class AccessDeniedExceptionMapper triggers when AccessDeniedException occurred.
 */

@Provider
@Produces({ MediaType.APPLICATION_JSON })
public class AccessDeniedExceptionMapper implements ExceptionMapper<AccessDeniedException> {


    Log logger = LogFactory.getLog(AccessDeniedExceptionMapper.class);

    @Override
    public Response toResponse(AccessDeniedException e) {

        ResponseBean errorResponse = new ResponseBean();
        errorResponse.setStatus(Constants.Status.FAILED);
        errorResponse.setMessage(e.getMessage());
        logger.error("Unauthorized access: ", e);
        return Response.status(Response.Status.FORBIDDEN).entity(errorResponse).build();
    }
}
