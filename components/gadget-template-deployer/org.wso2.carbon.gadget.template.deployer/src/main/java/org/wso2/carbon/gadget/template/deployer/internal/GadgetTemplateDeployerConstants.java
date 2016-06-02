package org.wso2.carbon.gadget.template.deployer.internal;

import org.wso2.carbon.registry.core.RegistryConstants;

public class GadgetTemplateDeployerConstants {
    private GadgetTemplateDeployerConstants() {
    }

    public static final String ARTIFACT_TYPE = "gadget";

    public static final String CONFIG_TAG = "config";

    public static final String PROPERTIES_TAG = "properties";

    public static final String PROPERTY_TAG = "property";

    public static final String ARTIFACTS_TAG = "artifacts";

    public static final String ARTIFACT_TAG = "artifact";

    public static final String NAME_ATTRIBUTE = "name";

    public static final String FILE_ATTRIBUTE = "file";

    public static final String APP_NAME = "portal";

    public static final String DIRECTORY_NAME = "directoryName";

    public static final String TEMPLATE_DIRECTORY = "templateDirectory";

    public static final String DEFAULT_STORE_TYPE = "fs";

    public static final String ARTIFACT_DIRECTORY_MAPPING_PATH = "repository" + RegistryConstants.PATH_SEPARATOR + "components" + RegistryConstants.PATH_SEPARATOR +
            "org.wso2.carbon.gadget.template.deployer" + RegistryConstants.PATH_SEPARATOR + "artifact.directory.mapping";
}
