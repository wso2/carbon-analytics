namespace java org.wso2.carbon.databridge.commons.thrift.exception

exception ThriftStreamDefinitionException {
    1: required string message
}

exception ThriftNoStreamDefinitionExistException {
    1: required string message
}

exception ThriftDifferentStreamDefinitionAlreadyDefinedException {
    1: required string message
}

exception ThriftMalformedStreamDefinitionException {
    1: required string message
}

exception ThriftUndefinedEventTypeException {
    1: required string message
}
exception ThriftSessionExpiredException {
    1: required string message
}

exception ThriftAuthenticationException {
    1: required string message
}
