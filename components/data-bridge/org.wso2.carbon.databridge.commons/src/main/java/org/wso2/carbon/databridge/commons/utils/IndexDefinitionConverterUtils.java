package org.wso2.carbon.databridge.commons.utils;

import org.wso2.carbon.databridge.commons.IndexDefinition;
import org.wso2.carbon.databridge.commons.IndexDefinitionConstants;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
public class IndexDefinitionConverterUtils {

    public static IndexDefinition getIndexDefinition(String indexDefinitionStr) {
        IndexDefinition indexDefinition = new IndexDefinition();
        indexDefinition.setIndexDataFromStore(indexDefinitionStr);
        return indexDefinition;
    }


    public static String getIndexDefinitionString(IndexDefinition indexDefinition) {
        if (indexDefinition != null) {
            String indexDefnStr = "";
            if (indexDefinition.getArbitraryIndexDefn() != null)
                indexDefnStr += "[" + IndexDefinitionConstants.ARBITRARY_INDEX + ":=" +
                        indexDefinition.getArbitraryIndexDefn() + "]" + ",\n";
            if (indexDefinition.getCustomIndexDefn() != null)
                indexDefnStr += "[" + IndexDefinitionConstants.CUSTOM_INDEX + ":=" +
                        indexDefinition.getCustomIndexDefn() + "]" + ",\n";
            if (indexDefinition.getFixedSearchDefn() != null)
                indexDefnStr += "[" + IndexDefinitionConstants.FIXED_INDEX + ":=" +
                        indexDefinition.getFixedSearchDefn() + "]" + ",\n";
            if (indexDefinition.isIncrementalIndex())
                indexDefnStr += "[" + IndexDefinitionConstants.INCREMENTAL_INDEX + ":=" +
                        indexDefinition.isIncrementalIndex() + "]" + ",\n";
            if (indexDefinition.getSecondaryIndexDefn() != null)
                indexDefnStr += "[" + IndexDefinitionConstants.SECONDARY_INDEX + ":=" +
                        indexDefinition.getSecondaryIndexDefn() + "]" + ",\n";
            if (!indexDefnStr.isEmpty()) {
                return indexDefnStr.substring(0, indexDefnStr.length() - 2);
            } else {
                return null;
            }
        }
        return null;
    }


    public static String getSecondaryIndexString(String indexString){
      return getIndexString(indexString, IndexDefinitionConstants.SECONDARY_INDEX);
    }

    public static String getIncrementalIndexString(String indexString){
       return getIndexString(indexString, IndexDefinitionConstants.INCREMENTAL_INDEX);
    }

    public static String getFixedIndexString(String indexString){
       return getIndexString(indexString, IndexDefinitionConstants.FIXED_INDEX);
    }

    public static String getCustomIndexString(String indexString){
       return getIndexString(indexString, IndexDefinitionConstants.CUSTOM_INDEX);
    }

    public static String getArbitraryIndexString(String indexString){
       return getIndexString(indexString, IndexDefinitionConstants.ARBITRARY_INDEX);
    }

    private static String getIndexString(String indexString, String indexName){
        Pattern pattern = Pattern.compile("\\[(.*?)\\]");
        Matcher matcher = pattern.matcher(indexString);

        while (matcher.find()) {
           if(matcher.group(1).trim().startsWith(indexName)){
              String indexValue = matcher.group(1).replace(indexName, "");
              indexValue = indexValue.replace(":=", "");
              return indexValue.trim();
           }
        }
        return "";
    }


}
