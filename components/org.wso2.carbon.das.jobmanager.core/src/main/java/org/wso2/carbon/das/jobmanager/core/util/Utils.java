package org.wso2.carbon.das.jobmanager.core.util;

public class Utils {


    public static boolean nullAllowEquals(Object lhs, Object rhs) {
        return lhs == null && rhs == null || !((lhs == null && rhs != null) || (lhs != null && rhs == null))
                && (lhs != null && lhs.equals(rhs));
    }
}
