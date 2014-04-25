package org.wso2.carbon.analytics.hive.cassandra;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.commons.io.FileUtils;
import org.wso2.carbon.analytics.hive.exception.HiveExecutionException;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.InputStream;

/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class CassandraStreamDefnConfigReader {


    private static final String STREAMDEFN_XML = "streamdefn.xml";
    private static final String REPLICATION_FACTOR_ELEMENT = "ReplicationFactor";
    private static final String READ_CONSISTENCY_LEVEL_ELEMENT = "ReadConsistencyLevel";
    private static final String WRITE_CONSISTENCY_LEVEL_ELEMENT = "WriteConsistencyLevel";
    private static final String STRATEGY_CLASS_ELEMENT = "StrategyClass";
    private static final String KEY_SPACE_NAME_ELEMENT = "keySpaceName";
    private static final String INDEX_KEY_SPACE_NAME_ELEMENT = "eventIndexKeySpaceName";


    private static int replicationFactor;
    private static String readConsistencyLevel;
    private static String writeConsistencyLevel;
    private static String strategyClass;

    private static String keySpaceName;
    private static String indexKeySpaceName;



    public static void readConfigFile() throws HiveExecutionException {
        InputStream in;
        try {
            String configFilePath = CarbonUtils.getCarbonConfigDirPath() + File.separator + "advanced" + File.separator + STREAMDEFN_XML;
            in = FileUtils.openInputStream(new File(configFilePath));
        } catch (Exception e) {
            throw new HiveExecutionException("Error while reading " + STREAMDEFN_XML, e);
        }

        OMXMLParserWrapper builder = OMXMLBuilderFactory.createOMBuilder(in);

        OMElement documentElement = builder.getDocumentElement();

        OMElement replicationFactorEl = documentElement.getFirstChildWithName(new QName(REPLICATION_FACTOR_ELEMENT));
        if (replicationFactorEl != null) {
            replicationFactor = Integer.parseInt(replicationFactorEl.getText());
        }

        OMElement readLevelEl = documentElement.getFirstChildWithName(new QName(READ_CONSISTENCY_LEVEL_ELEMENT));
        if (replicationFactorEl != null) {
            readConsistencyLevel = readLevelEl.getText();
        }

        OMElement writeLevelEl = documentElement.getFirstChildWithName(new QName(WRITE_CONSISTENCY_LEVEL_ELEMENT));
        if (writeLevelEl != null) {
            writeConsistencyLevel = writeLevelEl.getText();
        }


        OMElement strategyEl = documentElement.getFirstChildWithName(new QName(STRATEGY_CLASS_ELEMENT));
        if (strategyEl != null) {
            strategyClass = strategyEl.getText();
        }


        OMElement keySpaceElement = documentElement.getFirstChildWithName(new QName(KEY_SPACE_NAME_ELEMENT));
        if (keySpaceElement != null) {
            keySpaceName = keySpaceElement.getText();
        }

        OMElement indexKeySpaceElement = documentElement.getFirstChildWithName(new QName(INDEX_KEY_SPACE_NAME_ELEMENT));
        if (indexKeySpaceElement != null) {
            indexKeySpaceName = indexKeySpaceElement.getText();
        }
    }



    public static String getStrategyClass() {
        return strategyClass;
    }

    public static int getReplicationFactor() {
        return replicationFactor;
    }

    public static String getReadConsistencyLevel() {
        return readConsistencyLevel;
    }

    public static String getWriteConsistencyLevel() {
        return writeConsistencyLevel;
    }

    public static String getKeySpaceName() {
        return keySpaceName;
    }

    public static String getIndexKeySpaceName() {
        return indexKeySpaceName;
    }
}

