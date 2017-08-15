package org.wso2.carbon.stream.processor.core.persistence.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class that maps database configurations from resources/rdbms-table-config.xml
 */
@XmlRootElement(name = "rdbms-table-configuration")
public class RDBMSQueryConfiguration {

    private RDBMSQueryConfigurationEntry[] databaseQueryEntries;

    @XmlElement(name = "database")
    public RDBMSQueryConfigurationEntry[] getDatabaseQueryEntries() {
        return databaseQueryEntries;
    }

    public void setDatabaseQueryEntries(RDBMSQueryConfigurationEntry[] databaseQueryEntries) {
        this.databaseQueryEntries = databaseQueryEntries;
    }
}
