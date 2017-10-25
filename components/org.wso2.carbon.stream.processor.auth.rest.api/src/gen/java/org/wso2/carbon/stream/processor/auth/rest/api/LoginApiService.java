package org.wso2.carbon.stream.processor.auth.rest.api;

import org.wso2.carbon.stream.processor.auth.rest.api.*;
import org.wso2.carbon.stream.processor.auth.rest.api.dto.*;

import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;
import org.wso2.msf4j.Request;

import org.wso2.carbon.stream.processor.auth.rest.api.dto.ErrorDTO;
import org.wso2.carbon.stream.processor.auth.rest.api.dto.RedirectionDTO;
import org.wso2.carbon.stream.processor.auth.rest.api.dto.UserDTO;

import java.util.List;
import org.wso2.carbon.stream.processor.auth.rest.api.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public abstract class LoginApiService {
    public abstract Response loginAppNamePost(String appName
 ,String username
 ,String password
 ,String grantType
 ,Boolean rememberMe
  ,Request request) throws NotFoundException;
    public abstract Response loginCallbackAppNameGet(String appName
  ,Request request) throws NotFoundException;
}
