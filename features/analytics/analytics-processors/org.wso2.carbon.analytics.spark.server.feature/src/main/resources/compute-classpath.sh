FWDIR="$(cd "`dirname "$0"`"/..; pwd)"
CLASSPATH=$CLASSPATH:"$FWDIR/repository/components/dropins/org.wso2.carbon.analytics.spark.utils-1.0.3-SNAPSHOT.jar"
echo "$CLASSPATH"
