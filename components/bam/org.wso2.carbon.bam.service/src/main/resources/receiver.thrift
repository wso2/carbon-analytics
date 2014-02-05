namespace java org.wso2.carbon.bam.service

exception SessionTimeOutException {
    1: required string message
}

struct Event {
    1: optional map<string,binary> correlation,
    2: optional map<string,binary> meta,
    3: required map<string,binary> event
}

service ReceiverService {
   void publish(1:required Event event, 2:required string sessionId) throws
                            (1:SessionTimeOutException ste)
}

