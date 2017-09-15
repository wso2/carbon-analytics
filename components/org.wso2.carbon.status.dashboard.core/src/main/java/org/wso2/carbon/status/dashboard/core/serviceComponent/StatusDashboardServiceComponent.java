package org.wso2.carbon.status.dashboard.core.serviceComponent;

import com.zaxxer.hikari.HikariDataSource;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.datasource.core.api.DataSourceService;
import org.wso2.carbon.datasource.core.exception.DataSourceException;
import org.wso2.carbon.status.dashboard.core.config.DBQueries;
import org.wso2.carbon.status.dashboard.core.config.SpDashboardConfiguration;
import org.wso2.carbon.status.dashboard.core.persistence.store.impl.WorkerDetailsStore;

import javax.xml.bind.ValidationException;
import java.sql.SQLException;

/**
 * .
 */
@Component(
        name = "org.wso2.carbon.status.dashboard.test.service",
        service = StatusDashboardServiceComponent.class,
        immediate = true
)
public class StatusDashboardServiceComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(StatusDashboardServiceComponent.class);
    private static final String DATASOURCE_NAME = "WSO2_STATUS_DASHBOARD_DB";
    private static HikariDataSource dataSource = null;
    private static ConfigProvider configProvider;
    private static SpDashboardConfiguration dashboardConfiguration;
    @Activate
    protected void start(BundleContext bundleContext) {
        LOGGER.info("333333333333!!!!!!!!!!");
        WorkerDetailsStore workerDetailsStore = null;
        try {
            workerDetailsStore = new WorkerDetailsStore
                    ("WSO2_STATUS_DASHBOARD_DB", "WORKERS_CONFIGURATION");
            Object[] data = new Object[]{"localhost:7070", "localhost", 7070, "admin", "admin"};
            workerDetailsStore.insert(data);
            // TODO: 9/15/17 did not check
            workerDetailsStore.select("", "*");
        } catch (ValidationException | SQLException e) {
            e.printStackTrace();
        }
    }

    @Reference(
            name = "org.wso2.carbon.config",
            service = ConfigProvider.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterConfigProvider"
    )

    public void registerConfigProvider(ConfigProvider configProvider) {
        configProvider = configProvider;
        try {
            dashboardConfiguration = configProvider.getConfigurationObject(SpDashboardConfiguration.class);
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }

    }

    public static DBQueries generateConfigReader(String type) {
        return dashboardConfiguration.getDatabaseConfiguration(type);
    }

    protected void unregisterConfigProvider(ConfigProvider configProvider) {
        this.configProvider = null;
    }

    @Reference(
            name = "org.wso2.carbon.datasource.DataSourceService",
            service = DataSourceService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterDataSourceService"
    )
    protected void onDataSourceServiceReady(DataSourceService service) {
        try {
            dataSource = (HikariDataSource) service.getDataSource(DATASOURCE_NAME);
        } catch (DataSourceException e) {
            e.printStackTrace();
        }
    }

    protected void unregisterDataSourceService(DataSourceService service) {
        dataSource = null;
    }

    public static ConfigProvider getConfigProvider() {
        return configProvider;
    }

    public static HikariDataSource getDataSource() {
        return dataSource;
    }
}
