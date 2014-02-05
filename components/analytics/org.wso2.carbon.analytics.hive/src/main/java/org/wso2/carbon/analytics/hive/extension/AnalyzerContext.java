package org.wso2.carbon.analytics.hive.extension;

import org.apache.hadoop.hive.metastore.HiveContext;

import java.util.HashMap;
import java.util.Map;

public class AnalyzerContext {

    private String analyzerName;

    private Map<String,String> parameters = new HashMap<String,String>();

    public String getAnalyzerName() {
        return analyzerName;
    }

    public void setAnalyzerName(String analyzerName) {
        this.analyzerName = analyzerName;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }


    public void setProperty(String key, String value) {
        HiveContext.getCurrentContext().setProperty(key, value);
    }

    public String getProperty(String key) {
        return HiveContext.getCurrentContext().getProperty(key);
    }

    public void removeProperty(String key) {
        HiveContext.getCurrentContext().setProperty(key, null);
    }
}
