namespace java org.wso2.carbon.databridge.commons.thrift.service.general

include "Data.thrift"
include "Exception.thrift"

service ThriftEventTransmissionService {
    string defineStream(1: string sessionId, 2: string streamDefinition) throws (1:Exception.ThriftDifferentStreamDefinitionAlreadyDefinedException ade, 2:Exception.ThriftMalformedStreamDefinitionException mtd, 3:Exception.ThriftStreamDefinitionException tde,4:Exception.ThriftSessionExpiredException se ),
    string findStreamId (1: string sessionId, 2: string streamName, 3: string streamVersion) throws (1:Exception.ThriftNoStreamDefinitionExistException tnde,2:Exception.ThriftSessionExpiredException se ),
    void publish(1:Data.ThriftEventBundle eventBundle) throws (1:Exception.ThriftUndefinedEventTypeException ue,2:Exception.ThriftSessionExpiredException se),
    bool deleteStreamById(1: string sessionId, 2: string streamId) throws (1:Exception.ThriftSessionExpiredException se ),
    bool deleteStreamByNameVersion(1: string sessionId, 2: string streamName, 3: string streamVersion) throws (1:Exception.ThriftSessionExpiredException se )
}