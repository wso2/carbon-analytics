package org.wso2.carbon.status.dashboard.core.config;

import java.util.Map;

/**
 * Model class containing data type mappings.
 */
public class DBMapping {

    private Map<String,String> typeMap;
    private String binaryType;
    private String booleanType;
    private String doubleType;
    private String floatType;
    private String integerType;
    private String longType;
    private String stringType;
    private String stringSize;

    public DBMapping(Map<String,String> typeMappings){
        this.typeMap=typeMappings;
        this.setTypeMappings(typeMappings);
    }

    private void setTypeMappings(Map<String,String> propertyMap){
        this.binaryType=propertyMap.get("binaryType");
        this.booleanType=propertyMap.get("booleanType");
        this.doubleType=propertyMap.get("doubleType");
        this.floatType=propertyMap.get("floatType");
        this.integerType=propertyMap.get("integerType");
        this.longType=propertyMap.get("longType");
        this.stringType=propertyMap.get("stringType");
        this.stringSize=propertyMap.get("stringSize");
    }

    public String getBinaryType() {
        return binaryType;
    }

    public String getBooleanType() {
        return booleanType;
    }

    public String getDoubleType() {
        return doubleType;
    }

    public String getFloatType() {
        return floatType;
    }

    public String getIntegerType() {
        return integerType;
    }

    public String getLongType() {
        return longType;
    }

    public String getStringType() {
        return stringType;
    }

    public String getStringSize() {
        return stringSize;
    }

}
