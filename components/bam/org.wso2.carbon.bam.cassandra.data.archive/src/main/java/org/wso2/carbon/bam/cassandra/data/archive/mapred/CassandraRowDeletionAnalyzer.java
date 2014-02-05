package org.wso2.carbon.bam.cassandra.data.archive.mapred;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.HiveContext;
import org.apache.hadoop.util.ToolRunner;
import org.wso2.carbon.analytics.hive.extension.AbstractHiveAnalyzer;

import java.util.Date;
import java.util.Map;


public class CassandraRowDeletionAnalyzer extends AbstractHiveAnalyzer {


    private static final Log log = LogFactory.getLog(CassandraRowDeletionAnalyzer.class);

    @Override
    public void execute() {

        CassandraMapReduceRowDeletion mapReduceRowDeletion = new CassandraMapReduceRowDeletion();
        HiveConf hiveConf = HiveContext.getCurrentContext().getConf();
        String[] array = new String[] {hiveConf.get("hadoop.tmp.dir") + "/archivalTempResults" + new Date().getTime()};
        try {
            ToolRunner.run(hiveConf, mapReduceRowDeletion, array);
        } catch (Exception e) {
            log.error("Failed to run map reduce jobs",e);
        }
    }
}
