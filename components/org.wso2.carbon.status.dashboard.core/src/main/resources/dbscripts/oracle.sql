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

CREATE TABLE METRIC_GAUGE (
    ID NUMBER(20) PRIMARY KEY,
    SOURCE VARCHAR(255) NOT NULL,
    TIMESTAMP NUMBER(20) NOT NULL,
    NAME VARCHAR(255) NOT NULL,
    VALUE VARCHAR(100) NOT NULL
)
/
CREATE SEQUENCE METRIC_GAUGE_SEQUENCE START WITH 1 INCREMENT BY 1 NOCACHE
/
CREATE OR REPLACE TRIGGER METRIC_GAUGE_TRIGGER
            BEFORE INSERT
            ON METRIC_GAUGE
            REFERENCING NEW AS NEW
            FOR EACH ROW
            BEGIN
                SELECT METRIC_GAUGE_SEQUENCE.nextval INTO :NEW.ID FROM dual;
            END;
/

CREATE TABLE METRIC_COUNTER (
    ID NUMBER(20) PRIMARY KEY,
    SOURCE VARCHAR(255) NOT NULL,
    TIMESTAMP NUMBER(20) NOT NULL,
    NAME VARCHAR(255) NOT NULL,
    COUNT NUMBER(20) NOT NULL
)
/
CREATE SEQUENCE METRIC_COUNTER_SEQUENCE START WITH 1 INCREMENT BY 1 NOCACHE
/
CREATE OR REPLACE TRIGGER METRIC_COUNTER_TRIGGER
            BEFORE INSERT
            ON METRIC_COUNTER
            REFERENCING NEW AS NEW
            FOR EACH ROW
            BEGIN
                SELECT METRIC_COUNTER_SEQUENCE.nextval INTO :NEW.ID FROM dual;
            END;
/

CREATE INDEX IDX_TIMESTAMP_GAUGE ON METRIC_GAUGE (TIMESTAMP)
