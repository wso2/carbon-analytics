package org.wso2.carbon.analytics.hive.extension;

import java.util.HashSet;
import java.util.Set;

public class AnalyzerMeta {

    private String analyzerName;

    private String classAnalyzer;

    private Set<String> parameters = new HashSet<String>();

    public Set<String> getParameters() {
        return parameters;
    }

    public void setParameters(Set<String> parameters) {
        this.parameters = parameters;
    }

    public String getAnalyzerName() {
        return analyzerName;
    }

    public void setAnalyzerName(String analyzerName) {
        this.analyzerName = analyzerName;
    }

    public String getClassAnalyzer() {
        return classAnalyzer;
    }

    public void setClassAnalyzer(String classAnalyzer) {
        this.classAnalyzer = classAnalyzer;
    }

}
