package org.wso2.carbon.siddhi.store.api.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

import io.swagger.annotations.ApiModelProperty;

/**
 * This clss represents the bean class or the query reqest body
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen", date = "2017-11-01T11:26:25.925Z")
public class Query {
    @JsonProperty("query")
    private String query = null;
    @JsonProperty("tableDef")
    private String tableDef = null;

    public Query query(String tableDef, String query) {
        this.query = query;
        this.tableDef = tableDef;
        return this;
    }

    @ApiModelProperty(example = "@PrimaryKey('firstname','lastname', 'age')" +
                                "@store(type='solr', url='localhost:9983', collection='StudentStore', " +
                                "base.config='baseconfig', shards='2', replicas='2', schema='firstname string stored," +
                                "lastname string stored, age int stored', commit.async='true')" +
                                "define table FooTable(firstname string, lastname string, age int);")
    public String getTableDef() {
        return tableDef;
    }

    public String getQuery() {
        return query;
    }

    public void setTableDef(String tableDef){
        this.tableDef = tableDef;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Query)) {
            return false;
        }

        Query query1 = (Query) o;

        if (!query.equals(query1.query)) {
            return false;
        }
        if (!tableDef.equals(query1.tableDef)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = query.hashCode();
        result = 31 * result + tableDef.hashCode();
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Query {\n");

        sb.append("    tableDef: ").append(toIndentedString(tableDef)).append("\n");
        sb.append("    query: ").append(toIndentedString(query)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}

