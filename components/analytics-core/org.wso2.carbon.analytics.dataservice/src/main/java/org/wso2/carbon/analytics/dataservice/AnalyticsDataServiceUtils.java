package org.wso2.carbon.analytics.dataservice;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.IteratorUtils;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.RecordGroup;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;

/**
 * Analytics data service utilities.
 */
public class AnalyticsDataServiceUtils {

    @SuppressWarnings("unchecked")
    public static List<Record> listRecords(AnalyticsDataService ads,
                                           AnalyticsDataResponse response) throws AnalyticsException {
        List<Record> result = new ArrayList<Record>();
        for (RecordGroup rg : response.getRecordGroups()) {
            result.addAll(IteratorUtils.toList(ads.readRecords(response.getRecordStoreName(), rg)));
        }
        return result;
    }
    
}
