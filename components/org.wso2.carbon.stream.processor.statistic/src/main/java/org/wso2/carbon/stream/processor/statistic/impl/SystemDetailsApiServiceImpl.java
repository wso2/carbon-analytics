package org.wso2.carbon.stream.processor.statistic.impl;

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.kernel.config.model.CarbonConfiguration;
import org.wso2.carbon.stream.processor.statistic.api.NotFoundException;
import org.wso2.carbon.stream.processor.statistic.api.SystemDetailsApiService;
import org.wso2.carbon.stream.processor.statistic.internal.WorkerDetails;

import javax.ws.rs.core.Response;
import java.util.Hashtable;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen", date = "2017-09-11T05:58:14.415Z")
public class SystemDetailsApiServiceImpl extends SystemDetailsApiService {
    private static final Log log = LogFactory.getLog(SystemDetailsApiServiceImpl.class);
    private static ConfigProvider configProvider;
    private Map<String, String> properties = new Hashtable<>();

    @Reference(
            name = "org.wso2.carbon.config",
            service = ConfigProvider.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            unbind = "unregisterConfigProvider"
    )
    private void registerConfigProvider(ConfigProvider configProvider) {
        this.configProvider = configProvider;
    }

    @Override
    public Response systemDetailsGet() throws NotFoundException {
        log.info("WorkerDetails Info get &&&&&&&&&&&&&&&&&&&&&&");
        log.info(properties.toString());
        String jsonString;
        Response.Status status;
        try {
            CarbonConfiguration carbonConfiguration = configProvider.getConfigurationObject(
                    CarbonConfiguration.class);
            Gson gson = new Gson();
            jsonString = gson.toJson(carbonConfiguration.getId());
            log.info(jsonString + "IDDDDDDDDDDDDDDDDDDD");
            status = Response.Status.OK;
        } catch (org.wso2.carbon.config.ConfigurationException  e) {
            status = Response.Status.NOT_FOUND;
            WorkerDetails wk = new WorkerDetails();
            Gson gson = new Gson();
            jsonString = gson.toJson(wk);
            log.error("Error");
        }
        return Response.status(status).entity(jsonString).build();
    }
}
