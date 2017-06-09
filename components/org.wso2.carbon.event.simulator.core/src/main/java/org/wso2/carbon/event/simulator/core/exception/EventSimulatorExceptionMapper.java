package org.wso2.carbon.event.simulator.core.exception;

import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.stream.processor.common.exception.ResponseMapper;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * ExceptionMapper used to map custom exceptions of simulator
 */

@Component(
        name = "EventSimulatorExceptionMapper",
        service = ExceptionMapper.class,
        immediate = true
)
public class EventSimulatorExceptionMapper implements ExceptionMapper<Exception> {

    /**
     * toResponse() will return a response based on the type of exception thrown
     *
     * @param e exception
     * @return response
     * */
    @Override
    public Response toResponse(Exception e) {

        String className = e.getClass().getSimpleName();
        switch (className) {
            case "CSVFileDeploymentException":
            case "InsufficientAttributesException":
            case "InvalidConfigException":
            case "InvalidFileException":
                return Response.status(Response.Status.BAD_REQUEST)
                        .header("Access-Control-Allow-Origin", "*")
                        .entity(new ResponseMapper(Response.Status.BAD_REQUEST, e.getMessage()))
                        .type("application/json")
                        .build();
            case "EventGenerationException":
            case "FileOperationsException":
            case "SimulationConfigDeploymentException":
            case "SimulatorInitializationException":
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .header("Access-Control-Allow-Origin", "*")
                        .entity(new ResponseMapper(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage()))
                        .type("application/json")
                        .build();
            case "FileAlreadyExistsException":
                return Response.status(Response.Status.CONFLICT)
                        .header("Access-Control-Allow-Origin", "*")
                        .entity(new ResponseMapper(Response.Status.CONFLICT, e.getMessage()))
                        .type("application/json")
                        .build();
            case "FileLimitExceededException":
                return Response.status(Response.Status.FORBIDDEN)
                        .header("Access-Control-Allow-Origin", "*")
                        .entity(new ResponseMapper(Response.Status.FORBIDDEN, e.getMessage()))
                        .type("application/json")
                        .build();
            default:
                return null;

        }
    }
}
