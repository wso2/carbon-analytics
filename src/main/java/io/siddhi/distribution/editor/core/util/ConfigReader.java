package io.siddhi.distribution.editor.core.util;

import io.siddhi.distribution.editor.core.commons.kubernetes.SiddhiProcess;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Retrieve user configs and generates a deployment yaml content.
 */
public class ConfigReader {
    private static final String RESOURCES_DIR = "resources/docker-export";
    private static final String RUNNER_DEPLOYMENT_YAML_FILE = "runner-deployment.yaml";
    private static final String TOOLING_DEPLOYMENT_YAML_FILE = "deployment.yaml";
    private static final String DIRECTORY_CONF = "conf";
    private static final String DIRECTORY_PROFILE = "tooling";
    private static final String SIDDHI_NAMESPACE = "siddhi";
    private static final String DATA_SOURCES_NAMESPACE = "dataSources";

    public String export() throws IOException {
        Path toolingDeploymentYamlPath = Paths.get(
                Constants.CARBON_HOME,
                DIRECTORY_CONF,
                DIRECTORY_PROFILE,
                TOOLING_DEPLOYMENT_YAML_FILE
        );
        Path runnerDeploymentYamlPath = Paths.get(
                Constants.RUNTIME_PATH,
                RESOURCES_DIR,
                RUNNER_DEPLOYMENT_YAML_FILE
        );
        if (!Files.isReadable(toolingDeploymentYamlPath)) {
            throw new IOException("Config file " + toolingDeploymentYamlPath.toString() + " is not readable.");
        }

        if (!Files.isReadable(runnerDeploymentYamlPath)) {
            throw new IOException("Config file " + runnerDeploymentYamlPath.toString() + " is not readable.");
        }
        String toolingDeploymentYamlContent = new String(
                Files.readAllBytes(
                        toolingDeploymentYamlPath
                ),
                StandardCharsets.UTF_8
        );
        String runnerDeploymentYamlContent = new String(
                Files.readAllBytes(
                        runnerDeploymentYamlPath
                ),
                StandardCharsets.UTF_8
        );
        Yaml loadYaml = new Yaml();
        Map<String, Object> runnerConfigMap = loadYaml.load(toolingDeploymentYamlContent);
        Map<String, Object> toolingConfigMap = loadYaml.load(runnerDeploymentYamlContent);
        if (runnerConfigMap != null) {
            if (toolingConfigMap.get(DATA_SOURCES_NAMESPACE) != null) {
                runnerConfigMap.put(DATA_SOURCES_NAMESPACE, toolingConfigMap.get(DATA_SOURCES_NAMESPACE));
            }
            if (toolingConfigMap.get(SIDDHI_NAMESPACE) != null) {
                runnerConfigMap.put(SIDDHI_NAMESPACE, toolingConfigMap.get(SIDDHI_NAMESPACE));
            }
            Representer representer = new Representer();
            representer.addClassTag(SiddhiProcess.class, Tag.MAP);
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            Yaml dumpYaml = new Yaml(representer, options);
            return dumpYaml.dump(runnerConfigMap);
        }
        return "";
    }
}
