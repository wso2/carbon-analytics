package org.wso2.carbon.status.dashbord.core;

import org.easymock.EasyMock;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.config.ConfigProviderFactory;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.secvault.SecureVault;
import org.wso2.carbon.secvault.SecureVaultUtils;
import org.wso2.carbon.status.dashboard.core.api.NotFoundException;
import org.wso2.carbon.status.dashboard.core.bean.SpDashboardConfiguration;
import org.wso2.carbon.status.dashboard.core.impl.WorkersApiServiceImpl;
import org.wso2.carbon.status.dashboard.core.internal.DashboardDataHolder;
import org.wso2.carbon.status.dashboard.core.model.Worker;
import javax.ws.rs.core.Response;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Created by chathurika on 11/23/17.
 */
public class DashboardCoreTestCase {
    private WorkersApiServiceImpl workersApiService;
    private SpDashboardConfiguration spDashboardConfiguration;
    private Worker worker = new Worker();
    private DashboardDataHolder dashboardDataHolder;
    private static final String OS_NAME_KEY = "os.name";
    private static final String WINDOWS_PARAM = "indow";
    private static final String PASSWORD = "n3wP4s5w0r4";
    private SecureVault secureVault;
    private ConfigProvider configProvider;

    @BeforeClass
    public void setup() throws Exception {
        secureVault = EasyMock.mock(SecureVault.class);
        configProvider = ConfigProviderFactory.getConfigProvider(getResourcePath("conf",
                "Example.yaml").get(), secureVault);

        DashboardDataHolder.getInstance().setConfigProvider(configProvider);
    }

    @Test
    public void testAddWorkerMethodWithoutHost() throws NotFoundException, ConfigurationException {
        worker.port(9090);
        workersApiService = new WorkersApiServiceImpl();
        Response response = workersApiService.addWorker(worker);
        Assert.assertTrue(response.getEntity().toString().contains("Invalid data"));
    }

//    @Test
//    public void testAddWorkerMethod() throws NotFoundException, ConfigurationException {
//        worker.port(9090);
//        worker.host("localhost");
//        workersApiService = new WorkersApiServiceImpl();
//        Response response = workersApiService.addWorker(worker);
//        Assert.assertTrue(response.getEntity().toString().contains("Invalid data"));
//    }

    /**
     * Get the path of a provided resource.
     *
     * @param resourcePaths path strings to the location of the resource
     * @return path of the resources
     */
    public static Optional<Path> getResourcePath(String... resourcePaths) {
        URL resourceURL = SecureVaultUtils.class.getClassLoader().getResource("");
        if (resourceURL != null) {
            String resourcePath = resourceURL.getPath();
            if (resourcePath != null) {
                resourcePath = System.getProperty(OS_NAME_KEY).contains(WINDOWS_PARAM) ?
                        resourcePath.substring(1) : resourcePath;
                return Optional.ofNullable(Paths.get(resourcePath, resourcePaths));
            }
        }
        return Optional.empty(); // Resource do not exist
    }
}
