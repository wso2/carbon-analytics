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

IF NOT EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[METRIC_GAUGE]') AND TYPE IN (N'U'))
CREATE TABLE METRIC_GAUGE (
    ID INTEGER IDENTITY PRIMARY KEY,
    SOURCE VARCHAR(255) NOT NULL,
    TIMESTAMP BIGINT NOT NULL,
    NAME VARCHAR(255) NOT NULL,
    VALUE VARCHAR(100) NOT NULL,
);

IF NOT EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[METRIC_COUNTER]') AND TYPE IN (N'U'))
CREATE TABLE METRIC_COUNTER (
    ID INTEGER IDENTITY PRIMARY KEY,
    SOURCE VARCHAR(255) NOT NULL,
    TIMESTAMP BIGINT NOT NULL,
    NAME VARCHAR(255) NOT NULL,
    COUNT BIGINT NOT NULL,
);

CREATE INDEX IDX_TIMESTAMP_GAUGE ON METRIC_GAUGE (TIMESTAMP);

