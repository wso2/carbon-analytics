package org.wso2.carbon.data.provider.rdbms.utils;

import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.data.provider.rdbms.bean.RDBMSDataProviderConfBean;
import org.wso2.carbon.database.query.manager.QueryProvider;
import org.wso2.carbon.database.query.manager.config.Queries;
import org.wso2.carbon.database.query.manager.exception.QueryMappingNotAvailableException;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import org.yaml.snakeyaml.introspector.BeanAccess;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

import static org.wso2.carbon.data.provider.utils.DataProviderValueHolder.getDataProviderHelper;

/**
 * Holds the database queries.
 */
public class RDBMSQueryManager {
    private Map<String, String> queries;

    public RDBMSQueryManager(String databaseType, String databaseVersion) throws
            QueryMappingNotAvailableException,
            ConfigurationException, IOException {
        this.queries = readConfigs(databaseType, databaseVersion);
    }

    private Map<String, String> readConfigs(String databaseType, String databaseVersion) throws ConfigurationException,
            QueryMappingNotAvailableException, IOException {
        try {
            RDBMSDataProviderConfBean deploymentConfigurations = getDataProviderHelper().getConfigProvider()
                    .getConfigurationObject(RDBMSDataProviderConfBean.class);
            ArrayList<Queries> deploymentQueries = deploymentConfigurations.getQueries();
            ArrayList<Queries> componentQueries;
            URL url = this.getClass().getClassLoader().getResource("queries.yaml");
            if (url != null) {
                RDBMSDataProviderConfBean componentConfigurations = readYamlContent(url.openStream());
                componentQueries = componentConfigurations.getQueries();
            } else {
                throw new RuntimeException("Unable to load queries.yaml file.");
            }
            queries = QueryProvider.mergeMapping(databaseType, databaseVersion, componentQueries, deploymentQueries);
        } catch (ConfigurationException e) {
            throw new ConfigurationException("Unable to read queries.yaml configurations: " + e.getMessage(), e);
        } catch (QueryMappingNotAvailableException e) {
            throw new QueryMappingNotAvailableException("Unable to load queries: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new IOException("Unable to load content from queries.yaml file: " + e.getMessage(), e);
        }
        return queries;
    }

    public String getQuery(String key) {
        if (!this.queries.containsKey(key)) {
            throw new RuntimeException("Unable to find the configuration entry for the key: " + key);
        }
        return this.queries.get(key);
    }

    private RDBMSDataProviderConfBean readYamlContent(InputStream yamlContent) {
        Yaml yaml = new Yaml(new CustomClassLoaderConstructor(RDBMSDataProviderConfBean.class,
                RDBMSDataProviderConfBean.class.getClassLoader()));
        yaml.setBeanAccess(BeanAccess.FIELD);
        return yaml.loadAs(yamlContent, RDBMSDataProviderConfBean.class);
    }
}
