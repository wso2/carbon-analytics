package org.wso2.carbon.stream.processor.auth.rest.api;

import org.wso2.carbon.stream.processor.auth.rest.api.*;
import org.wso2.carbon.stream.processor.auth.rest.api.dto.*;

import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;
import org.wso2.msf4j.Request;

import org.wso2.carbon.stream.processor.auth.rest.api.dto.ErrorDTO;

import java.util.List;
import org.wso2.carbon.stream.processor.auth.rest.api.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public abstract class LogoutApiService {
    public abstract Response logoutAppNamePost(String appName
  ,Request request) throws NotFoundException;
}
