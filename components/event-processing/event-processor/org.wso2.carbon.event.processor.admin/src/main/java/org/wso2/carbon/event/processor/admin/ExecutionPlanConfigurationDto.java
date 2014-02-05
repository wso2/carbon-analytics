package org.wso2.carbon.event.processor.admin;

public class ExecutionPlanConfigurationDto {

    private String name;
    private String description;
    private SiddhiConfigurationDto[] siddhiConfigurations;
    private StreamConfigurationDto[] importedStreams;
    private StreamConfigurationDto[] exportedStreams;
    private String queryExpressions;
    private boolean tracingEnabled;
    private boolean statisticsEnabled;

    public SiddhiConfigurationDto[] getSiddhiConfigurations() {
        return siddhiConfigurations;
    }

    public void setSiddhiConfigurations(SiddhiConfigurationDto[] siddhiConfigurations) {
        this.siddhiConfigurations = siddhiConfigurations;
    }

    public StreamConfigurationDto[] getImportedStreams() {
        return importedStreams;
    }

    public void setImportedStreams(StreamConfigurationDto[] importedStreams) {
        this.importedStreams = importedStreams;
    }

    public StreamConfigurationDto[] getExportedStreams() {
        return exportedStreams;
    }

    public void setExportedStreams(StreamConfigurationDto[] exportedStreams) {
        this.exportedStreams = exportedStreams;
    }

    public String getQueryExpressions() {
        return queryExpressions;
    }

    public void setQueryExpressions(String queryExpressions) {
        this.queryExpressions = queryExpressions;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isTracingEnabled() {
        return tracingEnabled;
    }

    public void setTracingEnabled(boolean tracingEnabled) {
        this.tracingEnabled = tracingEnabled;
    }

    public boolean isStatisticsEnabled() {
        return statisticsEnabled;
    }

    public void setStatisticsEnabled(boolean statisticsEnabled) {
        this.statisticsEnabled = statisticsEnabled;
    }
}
