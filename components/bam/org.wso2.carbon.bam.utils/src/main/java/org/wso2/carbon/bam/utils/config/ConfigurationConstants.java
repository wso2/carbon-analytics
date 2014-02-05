/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bam.utils.config;

import javax.xml.namespace.QName;

public class ConfigurationConstants {

    public static final String BAM_KEYSPACE = "BAMKeyspace";
    
    public static final String BAM_CONFIG_FILE_NAME = "bam.xml";

    public static final String EVENT_TYPE = "EVENT";
    public static final String META_TYPE = "META";
    public static final String CORRELATION_TYPE = "CORRELATION";
    
    public static final QName INDEX_QNAME = new QName("index");
    public static final QName CF_QNAME = new QName("ColumnFamily");
    public static final QName GRANULARITY_QNAME = new QName("granularity");
    public static final QName ROWKEY_QNAME = new QName("rowKey");
    public static final QName PART_QNAME = new QName("part");
    public static final QName NAME = new QName("name");
    public static final QName STORE_INDEX = new QName("storeIndex");
    public static final QName TYPE = new QName("type");
    public static final QName DEFAULT_CF_QNAME = new QName("defaultCF");

    public static final QName INDEX_ROW_KEY_QNAME = new QName("indexRowKey");

}
