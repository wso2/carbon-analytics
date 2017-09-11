package org.wso2.carbon.status.dashboard.core.persistence.store.impl;


public class WorkerDetails {
    private String id;
    private String carbonID;
    private String cluterID;
    private String os;
    private String osVersion;
    private String javaHome;
    private String javaVersion;
    private String country;
    private String timeZone;

    public WorkerDetails(String id) {
        this.id = id;
    }

    public String getCarbonID() {
        return carbonID;
    }

    public void setCarbonID(String carbonID) {
        this.carbonID = carbonID;
    }

    public String getCluterID() {
        return cluterID;
    }

    public void setCluterID(String cluterID) {
        this.cluterID = cluterID;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getJavaHome() {
        return javaHome;
    }

    public void setJavaHome(String javaHome) {
        this.javaHome = javaHome;
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public void setJavaVersion(String javaVersion) {
        this.javaVersion = javaVersion;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }
}
