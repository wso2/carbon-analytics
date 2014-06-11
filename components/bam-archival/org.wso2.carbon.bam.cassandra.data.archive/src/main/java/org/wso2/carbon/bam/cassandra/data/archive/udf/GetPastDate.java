package org.wso2.carbon.bam.cassandra.data.archive.udf;

import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public final class GetPastDate extends UDF {

    private LongWritable longWritable = new LongWritable();

    public LongWritable evaluate(Text noOfDays) {

        String noOfDaysInString = noOfDays.toString();
        int noOfDaysInt = Integer.parseInt(noOfDaysInString);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
        Calendar cal = Calendar.getInstance();

        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.AM_PM, 0);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        cal.add(Calendar.DATE, -noOfDaysInt);

        longWritable.set(cal.getTimeInMillis());
        return longWritable;

    }

}

