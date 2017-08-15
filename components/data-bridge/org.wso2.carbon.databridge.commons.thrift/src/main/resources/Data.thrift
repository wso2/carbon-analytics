namespace java org.wso2.carbon.databridge.commons.thrift.data

enum ThriftAttributeType{
    INT,
    LONG,
    FLOAT,
    DOUBLE,
    BOOL,
    STRING
}

struct ThriftAttribute{
    1: optional string name;
    2: optional ThriftAttributeType attributeType;
}

struct ThriftEventBundle {
    1: optional string sessionId;
    2: optional i32 eventNum;
    3: optional list<i32>  intAttributeList;
    4: optional list<i64>  longAttributeList;
    5: optional list<double>  doubleAttributeList;
    6: optional list<bool>  boolAttributeList;
    7: optional list<string>  stringAttributeList;
    8: optional map<i32,map<string,string>>  arbitraryDataMapMap;
}



