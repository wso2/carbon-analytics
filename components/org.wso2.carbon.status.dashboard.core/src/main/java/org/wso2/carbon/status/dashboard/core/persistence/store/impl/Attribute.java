package org.wso2.carbon.status.dashboard.core.persistence.store.impl;

/**
 * Created by yasara on 9/13/17.
 */
public class Attribute {
    String name;
    String type;

    public Attribute(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
