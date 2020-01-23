package org.wso2.carbon.siddhi.editor.core.commons.request;

import java.util.Map;

/**
 * Bean class to represent the Siddhi application start request.
 */
public class AppStartRequest {
    private String siddhiAppName;
    private Map<String, String> variables;

    public String getSiddhiAppName() {
        return siddhiAppName;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, String> variables) {
        this.variables = variables;
    }
}
