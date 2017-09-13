package org.wso2.carbon.status.dashboard.core.persistence.store.impl;

import java.util.SortedMap;

/**
 * Maintains the condition string returned by the ConditionVisitor as well as a map of parameters to be used at runtime.
 */
public class RDBMSCompiledCondition {

    private String compiledQuery;
    private SortedMap<Integer, Object> parameters;

    public RDBMSCompiledCondition(String compiledQuery, SortedMap<Integer, Object> parameters) {
        this.compiledQuery = compiledQuery;
        this.parameters = parameters;
    }

    public RDBMSCompiledCondition cloneCompiledCondition(String key) {
        return new RDBMSCompiledCondition(this.compiledQuery, this.parameters);
    }

    public String getCompiledQuery() {
        return compiledQuery;
    }

    public String toString() {
        return getCompiledQuery();
    }

    public SortedMap<Integer, Object> getParameters() {
        return parameters;
    }
}
