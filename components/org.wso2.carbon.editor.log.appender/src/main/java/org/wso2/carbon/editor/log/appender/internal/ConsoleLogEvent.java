package org.wso2.carbon.editor.log.appender.internal;

import java.util.Date;

public class ConsoleLogEvent {
    private Date timeStamp;
    private String level;
    private String message;
    private String Fqcn;

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFqcn() {
        return Fqcn;
    }

    public void setFqcn(String fqcn) {
        Fqcn = fqcn;
    }

}
