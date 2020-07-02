package org.wso2.carbon.business.rules.core.manager;

import org.wso2.carbon.business.rules.core.services.TemplateManagerService;

import java.util.Map;
import javax.ws.rs.core.Response;

public interface SiddhiAppDeployer {

    Response deploySiddhiApp(TemplateManagerService templateManagerService, Object siddhiApp);

    Response updateSiddhiApp(TemplateManagerService templateManagerService, Object siddhiApp);

    Response deleteSiddhiApp(TemplateManagerService templateManagerService, String siddhiAppName);

    Response reShuffle(TemplateManagerService templateManagerService);
}
