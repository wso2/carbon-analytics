package org.wso2.carbon.event.simulator.core.api;

import org.wso2.carbon.event.simulator.core.api.*;
import org.wso2.carbon.event.simulator.core.model.*;

import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;

import java.io.File;

import org.wso2.carbon.event.simulator.core.model.InlineResponse2001;

import java.util.List;

import org.wso2.carbon.event.simulator.core.api.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
                            date = "2017-07-20T09:30:14.336Z")
public abstract class FilesApiService {
    public abstract Response deleteFile(String fileName) throws NotFoundException;

    public abstract Response getFileNames() throws NotFoundException;

    public abstract Response updateFile(String fileName, InputStream fileInputStream, FileInfo fileDetail)
            throws NotFoundException;

    public abstract Response uploadFile(InputStream fileInputStream, FileInfo fileDetail) throws NotFoundException;
}
