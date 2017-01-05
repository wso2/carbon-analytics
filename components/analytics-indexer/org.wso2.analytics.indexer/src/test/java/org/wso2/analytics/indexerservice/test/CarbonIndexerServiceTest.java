package org.wso2.analytics.indexerservice.test;

import org.testng.annotations.BeforeClass;
import org.wso2.analytics.indexerservice.CarbonIndexerService;
import org.wso2.analytics.indexerservice.exceptions.IndexerException;
import org.wso2.analytics.indexerservice.utils.IndexerUtils;

import java.util.ServiceLoader;

/**
 * This class contains the unit tests for indexer service;
 */
public class CarbonIndexerServiceTest {
    private CarbonIndexerService indexerService;


    @BeforeClass
    public void init() throws IndexerException {
        System.setProperty(IndexerUtils.WSO2_ANALYTICS_INDEX_CONF_DIRECTORY_SYS_PROP, "src/test/resources/conf");
        ServiceLoader<CarbonIndexerService> analyticsDataServiceServiceLoader = ServiceLoader.load(CarbonIndexerService.class);
        if (this.indexerService == null) {
            this.indexerService = analyticsDataServiceServiceLoader.iterator().next();
            if (this.indexerService == null) {
                throw new IndexerException("Indexer Service cannot be loaded!");
            }
        }
    }
}
