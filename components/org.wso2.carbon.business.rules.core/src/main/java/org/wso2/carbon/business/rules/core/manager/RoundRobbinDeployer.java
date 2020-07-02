package org.wso2.carbon.business.rules.core.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.business.rules.core.exceptions.SiddhiAppsApiHelperException;
import org.wso2.carbon.business.rules.core.exceptions.SiddhiManagerHelperException;
import org.wso2.carbon.business.rules.core.services.TemplateManagerService;
import org.wso2.carbon.business.rules.core.util.SiddhiManagerHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;

public class RoundRobbinDeployer implements SiddhiAppDeployer {
    private static final Logger log = LoggerFactory.getLogger(RoundRobbinDeployer.class);

    @Override
    public Response deploySiddhiApp(TemplateManagerService templateManagerService, Object siddhiApp) {
        Map nodes = templateManagerService.getNodes();
        Map<String, Long> deployedSiddhiAppCountInEachNode = new HashMap<>();
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        List<Object> responseData = new ArrayList<Object>();

        String siddhiAppName;
        try {
            siddhiAppName = SiddhiManagerHelper.getSiddhiAppName(siddhiApp);
        } catch (SiddhiManagerHelperException e) {
            log.error("Error occured while retrieving the siddhi app name", e);
            return Response.serverError().entity("Error occured while retrieving the siddhi app name " +
                    e.getMessage()).build();
        }
        for (Object node : nodes.keySet()) {
            boolean isSiddhiAppAvailable = false;
            try {
                isSiddhiAppAvailable = templateManagerService.checkSiddhiAppAvailability(node.toString(),
                        siddhiAppName);
            } catch (SiddhiAppsApiHelperException e) {
                log.error("Error occured while retrieving siddhi app availability from node " + node.toString(), e);
            }
            if (isSiddhiAppAvailable) {
                log.error("Failed to deploy the siddhi app. Siddhi app already exists in node" + node.toString());
                responseData.add("Failed to deploy the siddhi app. Siddhi app already exists in node " +
                        node.toString());
                return Response.status(Response.Status.CONFLICT).entity(gson.toJson(responseData)).build();
            }
            try {
                Long siddhiAppCount = templateManagerService.getSiddhiAppCount(node.toString());
                deployedSiddhiAppCountInEachNode.put(node.toString(), siddhiAppCount);
            } catch (SiddhiAppsApiHelperException e) {
                log.error("Error occured while retrieving siddhi app count from node " + node.toString(), e);
            }

        }
        Map.Entry<String, Long> deploybleNode = deployedSiddhiAppCountInEachNode.entrySet().stream().
                min(Map.Entry.comparingByValue()).get();

        try {
            templateManagerService.deploySiddhiApp(deploybleNode.getKey(), siddhiApp.toString());
            log.info("Siddhi App " + siddhiAppName + " deployed successfully on " + deploybleNode.getKey());
        } catch (SiddhiAppsApiHelperException e) {
            log.error("Error occured while deploying siddhi app on node " + deploybleNode.getKey(), e);
            return Response.status(e.getStatus()).entity("Error occured while deploying siddhi app on node " +
                    deploybleNode.getKey()).build();
        }
        return Response.accepted().entity("Siddhi App deployed successfully on " + deploybleNode.getKey()).build();
    }

    @Override
    public Response updateSiddhiApp(TemplateManagerService templateManagerService, Object siddhiApp) {
        return null;
    }

    @Override
    public Response deleteSiddhiApp(TemplateManagerService templateManagerService, String siddhiAppName) {
        return null;
    }

    @Override
    public Response reShuffle(TemplateManagerService templateManagerService) {
        return null;
    }
}
