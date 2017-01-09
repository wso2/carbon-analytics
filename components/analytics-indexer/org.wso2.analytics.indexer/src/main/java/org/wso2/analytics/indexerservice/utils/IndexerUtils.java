package org.wso2.analytics.indexerservice.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.wso2.analytics.indexerservice.CarbonIndexDocument;
import org.wso2.analytics.indexerservice.CarbonIndexDocumentField;
import org.wso2.analytics.indexerservice.IndexSchema;
import org.wso2.analytics.indexerservice.IndexSchemaField;
import org.wso2.analytics.indexerservice.exceptions.IndexerException;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This class contains the utility methods required by the indexer service.
 */
public class IndexerUtils {

    private static Log log = LogFactory.getLog(IndexerUtils.class);

    public static final String CUSTOM_WSO2_CONF_DIR_NAME = "conf";
    public static final String WSO2_ANALYTICS_INDEX_CONF_DIRECTORY_SYS_PROP = "wso2_custom_index_conf_dir";
    private static final String tenantDomain = "DEFAULT";

    public static String getIndexerConfDirectory() throws IndexerException {
        File confDir = null;
        try {
            confDir = new File(getConfDirectoryPath());
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Error in getting the indexer config path: " + e.getMessage(), e);
            }
        }
        if (confDir == null || !confDir.exists()) {
            return getCustomIndexerConfDirectory();
        } else {
            return confDir.getAbsolutePath();
        }
    }

    public static String getConfDirectoryPath() {
        String carbonConfigDirPath = System.getProperty("carbon.config.dir.path");
        if (carbonConfigDirPath == null) {
            carbonConfigDirPath = System.getenv("CARBON_CONFIG_DIR_PATH");
            if (carbonConfigDirPath == null) {
                return getBaseDirectoryPath() + File.separator + "conf";
            }
        }
        return carbonConfigDirPath;
    }

    private static String getCustomIndexerConfDirectory() throws IndexerException {
        String path = System.getProperty(WSO2_ANALYTICS_INDEX_CONF_DIRECTORY_SYS_PROP);
        if (path == null) {
            path = Paths.get("").toAbsolutePath().toString() + File.separator + CUSTOM_WSO2_CONF_DIR_NAME;
        }
        File confDir = new File(path);
        if (!confDir.exists()) {
            throw new IndexerException("The custom WSO2 index configuration directory does not exist at '" + path + "'. "
                    + "This can be given by correctly pointing to a valid configuration directory by setting the "
                    + "Java system property '" + WSO2_ANALYTICS_INDEX_CONF_DIRECTORY_SYS_PROP + "'.");
        }
        return confDir.getAbsolutePath();
    }

    public static String getBaseDirectoryPath() {
        String baseDir = System.getProperty("analytics.home");
        if (baseDir == null) {
            baseDir = System.getenv("ANALYTICS_HOME");
            System.setProperty("analytics.home", baseDir);
        }
        return baseDir;
    }

    public static File getFileFromSystemResources(String fileName) throws URISyntaxException {
        File file = null;
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        if (classLoader != null) {
            URL url = classLoader.getResource(fileName);
            if (url == null) {
                url = classLoader.getResource(File.separator + fileName);
            }
            file = new File(url.toURI());
        }
        return file;
    }

    public static IndexSchema getMergedIndexSchema(IndexSchema oldSchema, IndexSchema newSchema) {
        IndexSchema mergedSchema = new IndexSchema();
        mergedSchema.setDefaultSearchField(newSchema.getDefaultSearchField());
        mergedSchema.setUniqueKey(newSchema.getUniqueKey());
        mergedSchema.setFields(oldSchema.getFields());
        for (Map.Entry<String, IndexSchemaField> indexFieldEntry : newSchema.getFields().entrySet()) {
            mergedSchema.addField(indexFieldEntry.getKey(), indexFieldEntry.getValue());
        }
        return mergedSchema;
    }

    public static Map<String, SolrInputField> getSolrFields(Map<String, CarbonIndexDocumentField> fields) {
        Map<String, SolrInputField> solrFields = new LinkedHashMap<>(fields.size());
        solrFields.putAll(fields);
        return solrFields;
    }

    public static List<SolrInputDocument> getSolrInputDocuments(List<CarbonIndexDocument> docs) {
        List<SolrInputDocument> solrDocs = new ArrayList<>(docs.size());
        solrDocs.addAll(docs);
        return solrDocs;
    }

    public static String getTableNameWithDomainName(String tableName) {
        if (tableName != null) {
            return tenantDomain.toUpperCase() + "_" + tableName.toUpperCase();
        } else {
            return null;
        }
    }
}
