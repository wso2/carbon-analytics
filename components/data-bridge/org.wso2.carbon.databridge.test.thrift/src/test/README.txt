Steps to run the Performance test

1. Build Agent component
2. Add following jars to the test/lib folder
    commons-pool-1.5.6.wso2v1.jar
    gson-2.1.jar
    guava-0.9.0.wso2v1.jar
    junit-4.10.jar
    libthrift-0.8.0.wso2v1.jar
    org.wso2.carbon.base-4.0.0-SNAPSHOT.jar
    org.wso2.carbon.logging-4.0.0-SNAPSHOT.jar
    slf4j-1.5.10.wso2v1.jar

3. Add client-truststore.jks
       wso2carbon.jks
   to the resources folder or point to the correct key&thrust stores at KeyStoreUtil

4. To test One Server Multi Client test, run
     ant OneServerMultiClient-Server -Devents=10000
     ant OneServerMultiClient-Client -Devents=10000 -Dclients=10

5. To test Multi Server One Client test, run
     ant MultiServerOneClient-Server -Devents=10000 -Dservers=10
     ant MultiServerOneClient-Client -Devents=10000 -Dservers=10

