package org.wso2.carbon.analytics.dataservice.rest.beans;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "indexType")
@XmlEnum
public enum IndexTypeBean {
	@XmlEnumValue("STRING")
	STRING,
	@XmlEnumValue("INTEGER")
    INTEGER,
    @XmlEnumValue("LONG")
    LONG,
    @XmlEnumValue("FLOAT")
    FLOAT,
    @XmlEnumValue("DOUBLE")
    DOUBLE,
    @XmlEnumValue("BOOLEAN")
    BOOLEAN

}
