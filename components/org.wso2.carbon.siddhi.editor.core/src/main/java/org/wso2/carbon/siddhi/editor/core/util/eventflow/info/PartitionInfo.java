package org.wso2.carbon.siddhi.editor.core.util.eventflow.info;

import java.util.ArrayList;
import java.util.List;

public class PartitionInfo extends Info {

    // Variables
    private List<QueryInfo> queries = new ArrayList<>();
    private List<PartitionTypeInfo> partitionTypes = new ArrayList<>();

    // Default Constructor
    public PartitionInfo() {
    }

    // Constructor
    public PartitionInfo(String id, String name, String definition, List<QueryInfo> queries,
                         List<PartitionTypeInfo> partitionTypes) {
        setId(id);
        setName(name);
        setDefinition(definition);
        setQueries(queries);
        setPartitionTypes(partitionTypes);
    }

    // Setters
    public void setQueries(List<QueryInfo> queries) {
        this.queries = queries;
    }

    public void setPartitionTypes(List<PartitionTypeInfo> partitionTypes) {
        this.partitionTypes = partitionTypes;
    }

    // Getters
    public List<QueryInfo> getQueries() {
        return queries;
    }

    public List<PartitionTypeInfo> getPartitionTypes() {
        return partitionTypes;
    }

    // Query List Handlers
    public void addQuery(QueryInfo queryInfo) {
        queries.add(queryInfo);
    }

    public List<String> getQueryIds() {
        List<String> queryIds = new ArrayList<>();
        for (QueryInfo query : queries) {
            queryIds.add(query.getId());
        }
        return queryIds;
    }

    // Partition List Handlers
    public void addPartitionType(PartitionTypeInfo partitionTypeInfo) {
        partitionTypes.add(partitionTypeInfo);
    }

}
