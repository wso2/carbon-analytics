package org.wso2.carbon.status.dashboard.core;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.status.dashboard.core.persistence.store.impl.WorkerDetailsStore;

import javax.xml.bind.ValidationException;
import java.sql.SQLException;

/**
 * .
 */
@Component(
        name = "org.wso2.carbon.status.dashboard.test.service",
        immediate = true
)
public class TestDBService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestDBService.class);
    @Activate
    protected void start(BundleContext bundleContext) throws ValidationException, SQLException {
        WorkerDetailsStore workerDetailsStore = new WorkerDetailsStore
                ("WSO2_STATUS_DASHBOARD_DB", "WORKERS_CONFIGURATION");
        Object[] data = new Object[]{"localhost:8080", "localhost", "8080", "admin", "admin"};
        workerDetailsStore.insert(data);

    }

}
