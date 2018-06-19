/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.siddhi.editor.core;

import org.apache.log4j.Logger;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test the Editor CodeGenerator class
 */
public class CodeGeneratorTest {
    private static final Logger log = Logger.getLogger(CodeGeneratorTest.class);

    @BeforeClass
    public void init() {
        log.info("==================================");
        log.info("Before Test Method Has Been Called");
        log.info("==================================");
    }

    @Test
    public void streamGenerationTest() {
        log.info("Stream Definition Test");
    }

    @Test
    public void tableGenerationTest() {
        log.info("Table Definition Test");
    }

    @Test
    public void windowGenerationTest() {
        log.info("Window Definition Test");
    }

    @Test
    public void triggerGenerationTest() {
        log.info("Trigger Definition Test");
    }

    @Test
    public void aggregationGenerationTest() {
        log.info("Aggregation Definition Test");
    }

    @Test
    public void functionGenerationTest() {
        log.info("Function Definition Test");
    }

}
