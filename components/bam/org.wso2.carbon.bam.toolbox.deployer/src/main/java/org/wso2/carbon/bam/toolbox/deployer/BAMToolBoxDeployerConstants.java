package org.wso2.carbon.bam.toolbox.deployer;


import org.wso2.carbon.registry.core.RegistryConstants;

import java.io.File;

/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class BAMToolBoxDeployerConstants {

    public static final String BAM_BASE_PATH = RegistryConstants.PATH_SEPARATOR
            + "repository" + RegistryConstants.PATH_SEPARATOR + "conf" + RegistryConstants.PATH_SEPARATOR + "org.wso2.carbon.bam";
    public static final String TOOL_BOX_CONF = RegistryConstants.PATH_SEPARATOR + "toolbox";
    public static final String FILE_SEPERATOR = RegistryConstants.PATH_SEPARATOR;
    public static final String SCRIPTS_DIR = "analytics";
    public static final String DASHBOARD_DIR = "dashboard";
    public static final String GADGETS_DIR = "gadgets";
    public static final String JASPER_DIR = "jasper";
    public static final String JAGGERY_DIR = "jaggery";
    public static final String STREAM_DEFN_DIR = "streamDefn";
    public static final String GADGET_META_FILE = "dashboard.properties";
    public static final String ANALYZERS_PROPERTIES_FILE = "analyzers.properties";
    public static final String JASPER_META_FILE = "jasper.properties";
    public static final String DATASOURCE = "datasource";
    public static final String DATASOURCE_CONFIGURATION = "datasource.configuration";

    public static final String DASHBOARD_TABS_VAR_NAME = "dashboard.tabs";
    public static final String DASHBOARD_TAB_PREFIX = "dashboard.tabs";
    public static final String TAB_NAME_SUFFIX = "name";
    public static final String GADGET_NAME_SUFFIX = "gadgets";

    public static final String JAGGERY_APPS = "dashboard.jaggery.apps";
    public static final String JAGGERY_APP_MAIN_DISPLAY_NANE = "main.displayName";
    public static final String JAGGERY_APP_SUBS = "tabs";
    public static final String JAGGERY_APP_SUB_DISPLAY_NAME="displayName";
    public static final String JAGGERY_APP_SUB_URL = "url";

    public static final String ANALYZER_SCRIPTS_VAR_NAME = "analyzers.scripts";
    public static final String ANALYZER_SCRIPT_PREFIX = "analyzers.scripts";
    public static final String ANALYZER_SCRIPT_FILE_NAME_SUFFIX = "filename";
    public static final String ANALYZER_SCRIPT_DESCRIPTION_SUFFIX = "description";
    public static final String ANALYZER_SCRIPT_CRON_SUFFIX = "cron";

    public static final String JASPER_TABS_VAR_NAME = "jasper.tabs";
    public static final String JASPER_TAB_PREFIX = "jasper.tabs";
    public static final String JRXML_NAME_SUFFIX = "jrxml";


    public static final String JAGGERY_DEPLOYMENT_DIR = "jaggeryapps";

    public static final String BAM_ARTIFACT_EXT = "tbox";

    public static String BAM_DEPLOYMET_FOLDER = "bam-toolbox";
    public static String PORT_OFF_SET = "Ports.Offset";

    public static String BAM_TOOLBOX_HOME = "samples" + File.separator + "toolboxes";
    public static String BAM_DEFAULT_TOOLBOX_PROP_FILE = "toolbox.properties";

    public static String TOOLBOXES_VAR_NAME = "toolboxes.name";
    public static String TOOLBOXES_PREFIX = "toolboxes";
    public static String TOOLBOXES_NAME_SUFFIX = "name";
    public static String TOOLBOXES_DESC_SUFFIX = "description";
    public static String TOOLBOXES_LOCATION_SUFFIX = "location";
    public static String TOOLBOXES_DEFAULT_SUFFIX = "default";

    public static String CARBON_HOME = "${CARBON_HOME}";

    public static String STREAM_DEFN_PROP_FILE = "streams.properties";

    public static final String STREAMS_DEFN_VAR_NAME = "streams.definitions";
    public static final String STREAM_DEFN_PREFIX = "streams.definitions";
    public static final String STREAM_DEFN_FILE_NAME_SUFFIX = "filename";
    public static final String STREAM_DEFN_DESCRIPTION_SUFFIX = "description";
    public static final String STREAM_DEFN_USERNAME_SUFFIX = "username";
    public static final String STREAM_DEFN_PASSWORD_SUFFIX = "password";
    public static final String STREAM_DEFN_SECONDARY_INDEXES = "secondaryindexes";
    public static final String STREAM_DEFN_CUSTOM_INDEXES    = "customindexes";
    public static final String STREAM_DEFN_FIXED_SEARCH_PROPERTIES = "fixedsearchproperties";
    public static final String STREAM_DEFN_INCREMETAL_INDEX = "enableIncrementalIndex";
    public static final String STREAM_DEFN_ARBITRARY_INDEXES = "arbitraryindexes";
}
