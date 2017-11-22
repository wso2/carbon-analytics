# Carbon Analytics
---

|  Branch | Build Status |
| :------------ |:-------------
| master      | [![Build Status](https://wso2.org/jenkins/job/platform-builds/job/carbon-analytics/badge/icon)](https://wso2.org/jenkins/job/platform-builds/job/carbon-analytics/) |

---

This project contains compontents which implements common functionalities used in WSO2 analytics platform.

#### Carbon Analytics repo contains following components:

* event-streams      
* event-recevier
* event-publisher  
* event-monitor
* event-processor-manager
* application-deployer

## How to build from the source
### Prerequisites
* Java 8 or above
* [Apache Maven](https://maven.apache.org/download.cgi#) 3.x.x
* [Node.js](https://nodejs.org/en/) 8.x.x or above
### Steps
1. Install above prerequisites if they have not been already installed
2. Get a clone from [this](https://github.com/wso2/carbon-analytics.git) repository
3. Run one of the following maven commands from carbon-analytics directory
   * To build with the tests
        ```bash
         mvn clean install 
        ```
   * To build without running any unit/integration test
        ```bash
         mvn clean install -Dmaven.test.skip=true
        ```
## How to Contribute
* Please report issues at [DAS JIRA](https://wso2.org/jira/browse/DAS) or [CEP JIRA](https://wso2.org/jira/browse/CEP)
* Send your bug fixes pull requests to the [master branch](https://github.com/wso2/carbon-analytics/tree/master)

## Contact us
WSO2 Carbon developers can be contacted via the mailing lists:

* Carbon Developers List : dev@wso2.org
* Carbon Architecture List : architecture@wso2.org
