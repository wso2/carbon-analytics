package org.wso2.carbon.analytics.spark.core.udf;

/**
 * Created by niranda on 5/29/15.
 */
public class SampleUDFs {

    public Integer stringLengthTest(String s) {
        return s.length();
    }
}
