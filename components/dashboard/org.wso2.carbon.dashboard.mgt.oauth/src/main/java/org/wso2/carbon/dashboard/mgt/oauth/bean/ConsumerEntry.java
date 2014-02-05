package org.wso2.carbon.dashboard.mgt.oauth.bean;

/**
 * A bean to keep OAuth data
 */
public class ConsumerEntry implements Comparable {
    private String consumerKey;
    private String consumerSecret;
    private String keyType;
    private String service;
/*    private String resPath;

    public String getResPath() {
        return resPath;
    }

    public void setResPath(String resPath) {
        this.resPath = resPath;
    }*/

    public String getConsumerKey() {
        return consumerKey;
    }

    public String getConsumerSecret() {
        return consumerSecret;
    }

    public String getKeyType() {
        return keyType;
    }

    public String getService() {
        return service;
    }

    public void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }

    public void setConsumerSecret(String consumerSecret) {
        this.consumerSecret = consumerSecret;
    }

    public void setKeyType(String keyType) {
        this.keyType = keyType;
    }

    public void setService(String service) {
        this.service = service;
    }

    public int compareTo(Object anotherEntry) {
        if (!(anotherEntry instanceof ConsumerEntry))
            throw new ClassCastException("A ConsumerEntry object expected.");
        String anotherService = ((ConsumerEntry) anotherEntry).getService();
        return this.service.compareTo(anotherService);
    }
}
