package org.wso2.carbon.das.jobmanager.core.util;

/**
 * This class contains the constants needed for the distributed deployment.
 */
public class DistributedConstants {
    public static final String RUNTIME_NAME_WORKER = "default"; // TODO: 10/15/17 For testing, change to worker

    public static final String RUNTIME_NAME_MANAGER = "manager";

    public static final String CLUSTER_CONFIG_NS = "cluster.config";

    public static final String DEPLOYMENT_CONFIG_NS = "deployment.config";

    public static final String MODE_DISTRIBUTED = "distributed";

    public static final String MODE_HA = "ha";

    public static final String KEY_NODE_INFO = "nodeInfo";

    public static final String TASK_UPSERT_RESOURCE_MAPPING = "Inserting/Updating resource mapping group";

    public static final String TASK_GET_RESOURCE_MAPPING = "Getting resource mapping group";

    public static final String CREATE_RESOURCE_MAPPING_TABLE =
            "CREATE TABLE IF NOT EXISTS RESOURCE_POOL_TABLE (\n"
                    + "                        GROUP_ID VARCHAR(512) NOT NULL,\n"
                    + "                        RESOURCE_MAPPING BLOB NOT NULL,\n"
                    + "                        PRIMARY KEY (GROUP_ID)\n" + ");\n";

    public static final String PS_REPLACE_RESOURCE_MAPPING_ROW =
            "REPLACE INTO RESOURCE_POOL_TABLE (GROUP_ID, RESOURCE_MAPPING) VALUES (?,?);";

    public static final String PS_SELECT_RESOURCE_MAPPING_ROW =
            "SELECT GROUP_ID, RESOURCE_MAPPING FROM RESOURCE_POOL_TABLE WHERE GROUP_ID =?";

}
