package org.wso2.carbon.analytics.hive.extension.util;

import org.wso2.carbon.analytics.hive.extension.AnalyzerMeta;

import java.util.HashMap;
import java.util.Map;

public class AnalyzerHolder {

    static private Map<String,AnalyzerMeta>  analyzers= new HashMap<String, AnalyzerMeta>();

    public static AnalyzerMeta getAnalyzer(String name){
       return analyzers.get(name);
    }

    public static void addAnalyzer(String name, AnalyzerMeta analyzerMeta){
        analyzers.put(name,analyzerMeta);
    }

    public static boolean isAnalyzer(String name){
        return analyzers.containsKey(name);
    }

}
