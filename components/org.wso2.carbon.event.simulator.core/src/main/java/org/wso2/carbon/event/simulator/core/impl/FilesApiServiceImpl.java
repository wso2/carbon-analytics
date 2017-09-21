package org.wso2.carbon.event.simulator.core.impl;

import org.apache.commons.io.FilenameUtils;
import org.wso2.carbon.event.simulator.core.api.*;
import org.wso2.carbon.event.simulator.core.exception.FileAlreadyExistsException;
import org.wso2.carbon.event.simulator.core.exception.FileOperationsException;
import org.wso2.carbon.event.simulator.core.exception.InvalidFileException;
import org.wso2.carbon.event.simulator.core.internal.generator.csv.util.FileUploader;
import org.wso2.carbon.event.simulator.core.internal.util.EventSimulatorConstants;
import org.wso2.carbon.event.simulator.core.model.*;

import java.io.File;

import org.wso2.carbon.event.simulator.core.model.InlineResponse2001;

import java.nio.file.Paths;
import java.util.List;

import org.wso2.carbon.event.simulator.core.api.NotFoundException;

import java.io.InputStream;

import org.wso2.carbon.stream.processor.common.exception.ResponseMapper;
import org.wso2.carbon.utils.Utils;
import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
                            date = "2017-07-20T09:30:14.336Z")
public class FilesApiServiceImpl extends FilesApiService {
    @Override
    public Response deleteFile(String fileName) throws NotFoundException {
        FileUploader fileUploader = FileUploader.getFileUploaderInstance();
        if (FilenameUtils.isExtension(fileName, EventSimulatorConstants.CSV_FILE_EXTENSION)) {
            boolean deleted = false;
            try {
                deleted = fileUploader.deleteFile(fileName, (Paths.get(Utils.getRuntimePath().toString(),
                                                                       EventSimulatorConstants.DIRECTORY_DEPLOYMENT,
                                                                       EventSimulatorConstants.DIRECTORY_CSV_FILES)).toString());
            } catch (FileOperationsException e) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .header("Access-Control-Allow-Origin", "*")
                        .entity(new ResponseMapper(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage()))
                        .build();
            }
            if (deleted) {
                return Response.ok()
                        .header("Access-Control-Allow-Origin", "*")
                        .entity(new ResponseMapper(Response.Status.OK, "Successfully deleted file '" +
                                fileName + "'"))
                        .build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .header("Access-Control-Allow-Origin", "*")
                        .entity(new ResponseMapper(Response.Status.NOT_FOUND, "File '" + fileName +
                                "' does not exist"))
                        .build();
            }
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(new ResponseMapper(Response.Status.NOT_FOUND, "File '" + fileName +
                            "' is not a CSV file."))
                    .build();
        }
    }

    @Override
    public Response getFileNames() throws NotFoundException {
        try {
            return Response.status(Response.Status.OK)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(
                            FileUploader.getFileUploaderInstance()
                                    .retrieveFileNameList(EventSimulatorConstants.CSV_FILE_EXTENSION,
                                                          (Paths.get(Utils.getRuntimePath().toString(),
                                                                     EventSimulatorConstants.DIRECTORY_DEPLOYMENT,
                                                                     EventSimulatorConstants.DIRECTORY_CSV_FILES)))
                           )
                    .build();
        } catch (FileOperationsException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(new ResponseMapper(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage()))
                    .build();
        }
    }

    @Override
    public Response updateFile(String fileName, InputStream fileInputStream, FileInfo fileInfo)
            throws NotFoundException {
        FileUploader fileUploader = FileUploader.getFileUploaderInstance();
        if (FilenameUtils.isExtension(fileName, EventSimulatorConstants.CSV_FILE_EXTENSION)) {
            if (FilenameUtils.isExtension(fileInfo.getFileName(), EventSimulatorConstants.CSV_FILE_EXTENSION)) {
                if (fileUploader.validateFileExists(fileName)) {
                    boolean deleted = false;
                    try {
                        deleted = fileUploader.deleteFile(fileName, (Paths.get(Utils.getRuntimePath().toString(),
                                                                               EventSimulatorConstants.DIRECTORY_DEPLOYMENT,
                                                                               EventSimulatorConstants
                                                                                           .DIRECTORY_CSV_FILES))
                                    .toString());
                    } catch (FileOperationsException e) {
                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                .header("Access-Control-Allow-Origin", "*")
                                .entity(new ResponseMapper(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage()))
                                .build();
                    }
                    if (deleted) {
                        try {
                            fileUploader.uploadFile(fileInfo, fileInputStream,
                                                    (Paths.get(Utils.getRuntimePath().toString(),
                                                               EventSimulatorConstants.DIRECTORY_DEPLOYMENT,
                                                               EventSimulatorConstants.DIRECTORY_CSV_FILES)).toString());
                        } catch (FileAlreadyExistsException | FileOperationsException | InvalidFileException e) {
                            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                    .header("Access-Control-Allow-Origin", "*")
                                    .entity(new ResponseMapper(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage()))
                                    .build();
                        }
                        return Response.ok()
                                .header("Access-Control-Allow-Origin", "*")
                                .entity(new ResponseMapper(Response.Status.OK, "Successfully updated CSV" +
                                        " file '" + fileName + "' with file '" + fileInfo.getFileName() + "'."))
                                .build();
                    } else {
                        return Response.status(Response.Status.NOT_FOUND)
                                .header("Access-Control-Allow-Origin", "*")
                                .entity(new ResponseMapper(Response.Status.NOT_FOUND, "File '" + fileName +
                                        "' does not exist"))
                                .build();
                    }
                } else {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .header("Access-Control-Allow-Origin", "*")
                            .entity(new ResponseMapper(Response.Status.NOT_FOUND, "File '" + fileName + "' " +
                                    "does not exist."))
                            .build();
                }
            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                        .header("Access-Control-Allow-Origin", "*")
                        .entity(new ResponseMapper(Response.Status.BAD_REQUEST, "File '" + fileName + "' is" +
                                " not a CSV file"))
                        .build();
            }
        } else {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(new ResponseMapper(Response.Status.BAD_REQUEST, "File '" + fileName + "' is" +
                            " not a CSV file"))
                    .build();
        }
    }

    @Override
    public Response uploadFile(InputStream fileInputStream, FileInfo fileInfo) throws NotFoundException {
        try {
            FileUploader.getFileUploaderInstance().uploadFile(fileInfo, fileInputStream,
                                                              (Paths.get(Utils.getRuntimePath().toString(), EventSimulatorConstants.DIRECTORY_DEPLOYMENT,
                                                                         EventSimulatorConstants.DIRECTORY_CSV_FILES)).toString());
        } catch (FileAlreadyExistsException | FileOperationsException | InvalidFileException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(new ResponseMapper(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage()))
                    .build();
        }
        return Response.status(Response.Status.CREATED)
                .header("Access-Control-Allow-Origin", "*")
                .entity(new ResponseMapper(Response.Status.CREATED, "Successfully uploaded file '" +
                        fileInfo.getFileName() + "'"))
                .build();
    }
}
