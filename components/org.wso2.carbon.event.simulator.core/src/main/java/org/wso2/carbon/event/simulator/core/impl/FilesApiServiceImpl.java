package org.wso2.carbon.event.simulator.core.impl;

import org.apache.commons.io.FilenameUtils;
import org.wso2.carbon.analytics.permissions.PermissionProvider;
import org.wso2.carbon.analytics.permissions.bean.Permission;
import org.wso2.carbon.event.simulator.core.api.FilesApiService;
import org.wso2.carbon.event.simulator.core.api.NotFoundException;
import org.wso2.carbon.event.simulator.core.exception.FileAlreadyExistsException;
import org.wso2.carbon.event.simulator.core.exception.FileOperationsException;
import org.wso2.carbon.event.simulator.core.exception.InvalidFileException;
import org.wso2.carbon.event.simulator.core.internal.generator.csv.util.FileUploader;
import org.wso2.carbon.event.simulator.core.internal.util.CommonOperations;
import org.wso2.carbon.event.simulator.core.internal.util.EventSimulatorConstants;
import org.wso2.carbon.event.simulator.core.service.EventSimulatorDataHolder;
import org.wso2.carbon.stream.processor.common.exception.ResponseMapper;
import org.wso2.carbon.utils.Utils;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.formparam.FileInfo;

import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
                            date = "2017-07-20T09:30:14.336Z")
public class FilesApiServiceImpl extends FilesApiService {
    private final Path CSV_BASE_PATH = Paths.get(Utils.getRuntimePath().toString(),
            EventSimulatorConstants.DIRECTORY_DEPLOYMENT, EventSimulatorConstants.DIRECTORY_CSV_FILES);

    private static final String PERMISSION_APP_NAME = "SIM";
    private static final String MANAGE_SIMULATOR_PERMISSION_STRING = "simulator.manage";
    private static final String VIEW_SIMULATOR_PERMISSION_STRING = "simulator.view";

    public Response deleteFile(String fileName) throws NotFoundException, FileOperationsException {
        FileUploader fileUploader = FileUploader.getFileUploaderInstance();
        CommonOperations.validatePath(fileName);
        if (FilenameUtils.isExtension(fileName, EventSimulatorConstants.CSV_FILE_EXTENSION)) {
            boolean deleted = false;
            try {
                deleted = fileUploader.deleteFile(fileName, CSV_BASE_PATH.toString());
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

    public Response getFileNames() throws NotFoundException {
        try {
            return Response.status(Response.Status.OK)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(
                            FileUploader.getFileUploaderInstance()
                                    .retrieveFileNameList(EventSimulatorConstants.CSV_FILE_EXTENSION, CSV_BASE_PATH)
                           )
                    .build();
        } catch (FileOperationsException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(new ResponseMapper(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage()))
                    .build();
        }
    }

    public Response updateFile(String fileName, InputStream fileInputStream, FileInfo fileInfo)
            throws NotFoundException, FileOperationsException {
        CommonOperations.validatePath(fileName);
        FileUploader fileUploader = FileUploader.getFileUploaderInstance();
        if (FilenameUtils.isExtension(fileName, EventSimulatorConstants.CSV_FILE_EXTENSION)) {
            if (FilenameUtils.isExtension(fileInfo.getFileName(), EventSimulatorConstants.CSV_FILE_EXTENSION)) {
                if (fileUploader.validateFileExists(fileName)) {
                    boolean deleted = false;
                    try {
                        deleted = fileUploader.deleteFile(fileName, CSV_BASE_PATH.toString());
                    } catch (FileOperationsException e) {
                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                .header("Access-Control-Allow-Origin", "*")
                                .entity(new ResponseMapper(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage()))
                                .build();
                    }
                    if (deleted) {
                        try {
                            fileUploader.uploadFile(fileInfo, fileInputStream, CSV_BASE_PATH.toString());
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

    public Response uploadFile(InputStream fileInputStream, FileInfo fileInfo)
            throws NotFoundException, FileOperationsException {
        CommonOperations.validatePath(fileInfo.getFileName());
        try {
            FileUploader.getFileUploaderInstance().uploadFile(fileInfo, fileInputStream, CSV_BASE_PATH.toString());
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

    @Override
    public Response deleteFile(String fileName, Request request) throws NotFoundException, FileOperationsException {
        if (getUserName(request) != null && !getPermissionProvider().hasPermission(getUserName(request), new
                Permission(PERMISSION_APP_NAME, MANAGE_SIMULATOR_PERMISSION_STRING))) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Insufficient permission to perform the action")
                    .build();
        }
        return deleteFile(fileName);
    }

    @Override
    public Response getFileNames(Request request) throws NotFoundException {
        if (getUserName(request) != null && !(getPermissionProvider().hasPermission(getUserName(request), new Permission
                (PERMISSION_APP_NAME, MANAGE_SIMULATOR_PERMISSION_STRING)) || getPermissionProvider().hasPermission
                (getUserName(request), new Permission(PERMISSION_APP_NAME, VIEW_SIMULATOR_PERMISSION_STRING)))) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Insufficient permission to perform the action")
                    .build();
        }
        return getFileNames();
    }

    @Override
    public Response updateFile(String fileName, InputStream fileInputStream, FileInfo fileDetail, Request request)
            throws NotFoundException, FileOperationsException {
        if (getUserName(request) != null && !getPermissionProvider().hasPermission(getUserName(request), new
                Permission(PERMISSION_APP_NAME, MANAGE_SIMULATOR_PERMISSION_STRING))) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Insufficient permission to perform the action")
                    .build();
        }
        return updateFile(fileName, fileInputStream, fileDetail);
    }

    @Override
    public Response uploadFile(InputStream fileInputStream, FileInfo fileDetail, Request request)
            throws NotFoundException, FileOperationsException {
        if (getUserName(request) != null && !getPermissionProvider().hasPermission(getUserName(request), new
                Permission(PERMISSION_APP_NAME, MANAGE_SIMULATOR_PERMISSION_STRING))) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Insufficient permission to perform the action")
                    .build();
        }
        return uploadFile(fileInputStream, fileDetail);
    }

    private static String getUserName(Request request) {
        Object username = request.getProperty("username");
        return username != null ? username.toString() : null;
    }

    private PermissionProvider getPermissionProvider() {
        return EventSimulatorDataHolder.getPermissionProvider();
    }
}
