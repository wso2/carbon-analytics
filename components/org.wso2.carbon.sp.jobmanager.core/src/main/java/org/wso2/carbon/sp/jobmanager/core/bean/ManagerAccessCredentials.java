package org.wso2.carbon.sp.jobmanager.core.bean;

import org.wso2.carbon.config.annotation.Element;

/**
 * Worker node accessing credentials.
 */
public class ManagerAccessCredentials {
    @Element(description = "Username across cluster", required = true)
    private String username;

    @Element(description = "Password across cluster")
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
