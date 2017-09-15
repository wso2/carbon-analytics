package org.wso2.carbon.status.dashboard.core.persistence.datasourceServicers;

import com.zaxxer.hikari.HikariDataSource;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.datasource.core.api.DataSourceService;
import org.wso2.carbon.datasource.core.exception.DataSourceException;

/**
 *
 */
@Component(
        name = "org.wso2.carbon.status.dashboard.db.source.metrics",
        immediate = true
)
public class StatusDashboardMetricsDataHolder {
    private static StatusDashboardMetricsDataHolder instance = new StatusDashboardMetricsDataHolder();
    private static final String DATASOURCE_NAME = "WSO2_METRICS_DB";
    private static HikariDataSource dataSource = null;
    public StatusDashboardMetricsDataHolder() {
    }

    @Activate
    protected void start(BundleContext bundleContext) {
    }

    @Reference(
            name = "org.wso2.carbon.datasource.DataSourceService",
            service = DataSourceService.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            unbind = "unregisterDataSourceService"
    )
    protected void onDataSourceServiceReady(DataSourceService service) {
        try {
            dataSource = (HikariDataSource) service.getDataSource(DATASOURCE_NAME);
        } catch (DataSourceException e) {
            e.printStackTrace();
        }
    }

    public static StatusDashboardMetricsDataHolder getInstance() {
        return instance;
    }

    public HikariDataSource getDataSource() {
        return dataSource;
    }
}
