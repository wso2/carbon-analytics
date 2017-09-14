package org.wso2.carbon.status.dashboard.core.persistence.datasourceServicers;

import com.zaxxer.hikari.HikariDataSource;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.datasource.core.api.DataSourceService;
import org.wso2.carbon.datasource.core.exception.DataSourceException;

/**
 *
 */
public class StatusDashboardWorkerDataHolder {
    private static StatusDashboardWorkerDataHolder instance = new StatusDashboardWorkerDataHolder();
    private static final String DATASOURCE_NAME = "WSO2_STATUS_DASHBOARD_DB";
    private HikariDataSource dataSource
            = null;
    private StatusDashboardWorkerDataHolder() {
    }

    @Reference(
            name = "org.wso2.carbon.datasource.DataSourceService",
            service = DataSourceService.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
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

    public static StatusDashboardWorkerDataHolder getInstance() {
        return instance;
    }

    public HikariDataSource getDataSource() {
        return dataSource;
    }
}
