package org.wso2.carbon.event.simulator.core.api;

import org.wso2.carbon.event.simulator.core.exception.FileOperationsException;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.formparam.FileInfo;

import javax.ws.rs.core.Response;
import java.io.InputStream;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2017-07-20T09:30:14.336Z")
public abstract class FilesApiService {

    public abstract Response deleteFile(String fileName, Request request)
            throws NotFoundException, FileOperationsException;

    public abstract Response getFileNames(Request request) throws NotFoundException;

    public abstract Response updateFile(String fileName, InputStream fileInputStream, FileInfo fileDetail,
                                        Request request)
            throws NotFoundException, FileOperationsException;

    public abstract Response uploadFile(InputStream fileInputStream, FileInfo fileDetail, Request request)
            throws NotFoundException, FileOperationsException;
}
