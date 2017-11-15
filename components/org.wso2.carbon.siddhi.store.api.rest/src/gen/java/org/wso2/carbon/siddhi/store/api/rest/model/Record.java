package org.wso2.carbon.siddhi.store.api.rest.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * This class represents the bean class forthe returned event/record from the table
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen", date = "2017-11-01T11:26:25.925Z")
public class Record extends ArrayList<Object> {

    /*@Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode());
    }
*/
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Record {\n");
        sb.append("    ").append(toIndentedString(super.toString())).append("\n");
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
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

