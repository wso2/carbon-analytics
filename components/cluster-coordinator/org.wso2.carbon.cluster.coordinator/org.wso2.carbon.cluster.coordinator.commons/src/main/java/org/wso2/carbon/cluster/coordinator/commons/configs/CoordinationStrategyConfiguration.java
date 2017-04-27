/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.cluster.coordinator.commons.configs;

import org.wso2.carbon.kernel.utils.Utils;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * The class which extracts configuration property names from the yaml.
 */
public class CoordinationStrategyConfiguration {

    private static CoordinationStrategyConfiguration instance = new CoordinationStrategyConfiguration();
    private CoordinationStrategyConfigUtil config = null;

    private CoordinationStrategyConfiguration() {
        Yaml yaml = new Yaml();
        InputStream in = null;
        try {
            in = new FileInputStream(Utils.getCarbonConfigHome() + File.separator + File.separator
                    + "coordinatorConfig.yaml");
            config = yaml.loadAs(in, CoordinationStrategyConfigUtil.class);
            in.close();
        } catch (FileNotFoundException e) {
            config = null;
        } catch (IOException e) {
        }
    }

    public static CoordinationStrategyConfiguration getInstance() {
        return instance;
    }

    public String getConfiguration() {
        if (config != null) {
            return config.getStrategy();
        }
        return null;
    }

    public Map<String, Integer> getRdbmsConfigs() {
        if (config != null) {
            return config.getRdbmsConfig();
        }
        return null;
    }

    public Map<String, String> getZooKeeperConfigs() {
        if (config != null) {
            return config.getZookeeperConfig();
        }
        return null;
    }
}
