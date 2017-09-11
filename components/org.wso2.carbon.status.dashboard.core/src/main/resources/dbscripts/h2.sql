-- 
-- Copyright 2017 WSO2 Inc. (http://wso2.org)
-- 
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
-- 
--     http://www.apache.org/licenses/LICENSE-2.0
-- 
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
-- 

CREATE TABLE IF NOT EXISTS WORKER_CONFIG (
    ID VARCHAR(255) PRIMARY KEY,
    USERNAME VARCHAR(255) NOT NULL,
    PASSWORD VARCHAR(255) NOT NULL,
    HOST VARCHAR(255) NOT NULL,
    PORT VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS WORKER_DETAILS (
    ID BIGINT AUTO_INCREMENT PRIMARY KEY,
    SOURCE VARCHAR(255) NOT NULL,
    TIMESTAMP BIGINT NOT NULL,
    NAME VARCHAR(255) NOT NULL,
    COUNT BIGINT NOT NULL
);


CREATE INDEX ID ON WORKER_CONFIG (ID);

