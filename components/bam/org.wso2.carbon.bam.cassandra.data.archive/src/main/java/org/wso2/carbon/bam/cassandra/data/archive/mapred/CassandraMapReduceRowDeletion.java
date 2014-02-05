package org.wso2.carbon.bam.cassandra.data.archive.mapred;


import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.cassandra.service.ThriftCluster;
import me.prettyprint.cassandra.service.template.ThriftColumnFamilyTemplate;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.factory.HFactory;
import org.apache.cassandra.db.IColumn;
import org.apache.cassandra.hadoop.ColumnFamilyInputFormat;
import org.apache.cassandra.hadoop.ConfigHelper;
import org.apache.cassandra.thrift.SlicePredicate;
import org.apache.cassandra.thrift.SliceRange;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.HiveContext;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.thrift.TBaseHelper;
import org.wso2.carbon.bam.cassandra.data.archive.util.CassandraArchiveUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;

public class CassandraMapReduceRowDeletion extends Configured implements Tool {

    private static final String JOB_NAME = "PURGE_CASSANDRA_DATA";
    private static final String INPUT_PARTITIONER = "org.apache.cassandra.dht.RandomPartitioner";


    private static final String INPUT_KEYSPACE_USERNAME_CONFIG = "cassandra.input.keyspace.username";
    private static final String INPUT_KEYSPACE_PASSWD_CONFIG = "cassandra.input.keyspace.passwd";

    private static final String BAM_EVENT_DATA_KEYSPACE = "EVENT_KS";

    public int run(String[] strings) throws Exception {


        HiveConf hiveConf = HiveContext.getCurrentContext().getConf();
        Job job = new Job(hiveConf, JOB_NAME);
        job.setJarByClass(CassandraMapReduceRowDeletion.class);
        job.setMapperClass(CassandraMapReduceRowDeletion.RowKeyMapper.class);

        job.setInputFormatClass(ColumnFamilyInputFormat.class);

        job.setNumReduceTasks(0);

        String hiveAuxJars= job.getConfiguration().get(HiveConf.ConfVars.HIVEAUXJARS.toString());
        job.getConfiguration().set("tmpjars",hiveAuxJars);
        ConfigHelper.setRangeBatchSize(hiveConf, 1000);


        SliceRange sliceRange = new SliceRange(ByteBuffer.wrap(new byte[0]),
                ByteBuffer.wrap(new byte[0]), true, 1000);

        SlicePredicate slicePredicate = new SlicePredicate();
        slicePredicate.setSlice_range(sliceRange);

        Configuration configuration = job.getConfiguration();

        String cassandraUsername = configuration.get(CassandraArchiveUtil.CASSANDRA_USERNAME);
        String cassandraPassword = configuration.get(CassandraArchiveUtil.CASSANDRA_PASSWORD);

        configuration.set(INPUT_KEYSPACE_USERNAME_CONFIG, cassandraUsername);
        configuration.set(INPUT_KEYSPACE_PASSWD_CONFIG, cassandraPassword);

        Cluster cluster = CassandraArchiveUtil.getCassandraCluster();
        String columnFamilyName = configuration.get(CassandraArchiveUtil.COLUMN_FAMILY_NAME);
        String cassandraPort = configuration.get(CassandraArchiveUtil.CASSANDRA_PORT);
        String cassandraHostIp = configuration.get(CassandraArchiveUtil.CASSANDRA_HOST_IP);
        ConfigHelper.setInputColumnFamily(configuration, BAM_EVENT_DATA_KEYSPACE, columnFamilyName);
        ConfigHelper.setInputRpcPort(configuration, cassandraPort);
        ConfigHelper.setInputInitialAddress(configuration, cassandraHostIp);
        ConfigHelper.setInputPartitioner(configuration, INPUT_PARTITIONER);
        ConfigHelper.setInputSlicePredicate(configuration, slicePredicate);

        FileOutputFormat.setOutputPath(job, new Path(strings[0]));

        job.waitForCompletion(true);

        cluster.dropColumnFamily(BAM_EVENT_DATA_KEYSPACE,columnFamilyName);

        return job.isSuccessful() ? 0 : 1;
    }


    public static class RowKeyMapper extends Mapper<ByteBuffer, SortedMap<ByteBuffer, IColumn>, Text, LongWritable> {

        private static StringSerializer stringSerializer = StringSerializer.get();
        private static String columnFamilyName;
        private static Cluster cluster;


        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            Configuration configuration = context.getConfiguration();
            columnFamilyName = configuration.get(CassandraArchiveUtil.CASSANDRA_ORIGINAL_CF);
            String cassandraUsername = configuration.get(CassandraArchiveUtil.CASSANDRA_USERNAME);
            String cassandraPassword = configuration.get(CassandraArchiveUtil.CASSANDRA_PASSWORD);
            String cassandraPort = configuration.get(CassandraArchiveUtil.CASSANDRA_PORT);
            String cassandraHostIp = configuration.get(CassandraArchiveUtil.CASSANDRA_HOST_IP);
            String connectionURL = createConnectionUrl(cassandraHostIp, cassandraPort);

            CassandraHostConfigurator hostConfigurator = new CassandraHostConfigurator(connectionURL);
            Map<String, String> credentials = new HashMap<String, String>();
            credentials.put("username", cassandraUsername);
            credentials.put("password", cassandraPassword);
            cluster = new ThriftCluster(CassandraArchiveUtil.DEFAULT_CASSANDRA_CLUSTER, hostConfigurator, credentials);

        }

        private String createConnectionUrl(String cassandraHostIp, String cassandraPort) {
            String connectionURL ="";
            if(cassandraHostIp.contains(",")){
                String[] array = cassandraHostIp.split(",");
                for(int i=1;i<=array.length;i++){
                    String host = array[i-1];
                    if(i<array.length){
                        connectionURL +=  host + ":" + cassandraPort + ",";
                    }else {
                        connectionURL += host + ":" + cassandraPort;
                    }
                }
            }else {
                connectionURL = cassandraHostIp + ":" + cassandraPort;
            }
            return connectionURL;
        }

        public void map(ByteBuffer key, SortedMap<ByteBuffer, IColumn> columns, Context context) throws IOException, InterruptedException {

            String rowkey = StringSerializer.get().fromByteBuffer(TBaseHelper.rightSize(key));
            Keyspace keyspace = HFactory.createKeyspace(BAM_EVENT_DATA_KEYSPACE, cluster);
            ThriftColumnFamilyTemplate<String, String> template = new ThriftColumnFamilyTemplate<String, String>(keyspace,
                    columnFamilyName,
                    stringSerializer,
                    stringSerializer);
            template.deleteRow(rowkey);

        }
    }
}
