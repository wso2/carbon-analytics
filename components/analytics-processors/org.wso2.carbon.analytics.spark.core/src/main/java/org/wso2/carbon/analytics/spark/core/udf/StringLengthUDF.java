package org.wso2.carbon.analytics.spark.core.udf;

import org.apache.spark.sql.api.java.UDF1;import java.lang.Exception;import java.lang.Integer;import java.lang.Override;import java.lang.String;

/**
 * Created by niranda on 5/29/15.
 */
public class StringLengthUDF implements UDF1 <String, Integer> {
    @Override
    public Integer call(String s) throws Exception {
        return s.length();
    }
}
