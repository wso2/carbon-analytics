package org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator;

import org.wso2.carbon.siddhi.editor.core.util.designview.SiddhiAppConverter;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.EventFlow;

public class MainTest {

    public static void main(String[] args) {
        String siddhiApp1 = "@App:name(\"JoinStream\")\n" +
                "@App:description(\"Test Siddhi App For The Join Stream Query\")\n" +
                "\n" +
                "-- Please refer to https://docs.wso2.com/display/SP400/Quick+Start+Guide on getting started with SP editor. \n" +
                "\n" +
                "define stream TempStream(deviceID long, roomNo int, temp double);\n" +
                "define stream RegulatorStream(deviceID long, roomNo int, isOn bool);\n" +
                "\n" +
                "from TempStream[temp > 30.0]#window.time(1 min) as T\n" +
                "  join RegulatorStream[isOn == false]#window.length(1) as R\n" +
                "  on T.roomNo == R.roomNo\n" +
                "select T.roomNo, R.deviceID\n" +
                "insert into RegulatorActionStream;\n";

        String siddhiApp2 = "@App:name(\"JoinAggregation\")\n" +
                "@App:description(\"Test Siddhi App For The Join Window Query\")\n" +
                "\n" +
                "-- Please refer to https://docs.wso2.com/display/SP400/Quick+Start+Guide on getting started with SP editor. \n" +
                "\n" +
                "define stream TradeStream (symbol string, price double, volume long, timestamp long);\n" +
                "\n" +
                "define aggregation TradeAggregation\n" +
                "  from TradeStream\n" +
                "  select symbol, avg(price) as avgPrice, sum(price) as total\n" +
                "    group by symbol\n" +
                "    aggregate by timestamp every sec ... year;\n" +
                "    \n" +
                "define stream StockStream (symbol string, value int);\n" +
                "\n" +
                "from StockStream as S join TradeAggregation as T\n" +
                "  on S.symbol == T.symbol \n" +
                "  within \"2014-02-15 00:00:00 +05:30\", \"2014-03-16 00:00:00 +05:30\" \n" +
                "  per \"days\" \n" +
                "select S.symbol, T.total, T.avgPrice \n" +
                "group by S.symbol \n" +
                "insert into AggregateStockStream;";

        String siddhiApp3 = "@App:name(\"JoinTable\")\n" +
                "@App:description(\"Test Siddhi App For The Join Table Query\")\n" +
                "\n" +
                "-- Please refer to https://docs.wso2.com/display/SP400/Quick+Start+Guide on getting started with SP editor. \n" +
                "\n" +
                "\n" +
                "define table RoomTypeTable (roomNo int, type string);\n" +
                "define stream TempStream (deviceID long, roomNo int, temp double);\n" +
                "\n" +
                "from TempStream join RoomTypeTable\n" +
                "    on RoomTypeTable.roomNo == TempStream.roomNo\n" +
                "select deviceID, RoomTypeTable.type as roomType, type, temp\n" +
                "    having roomType == 'server-room'\n" +
                "insert into ServerRoomTempStream;";

        String siddhiApp4 = "@App:name(\"JoinWindow\")\n" +
                "@App:description(\"Test Siddhi App For The Join Window Query\")\n" +
                "\n" +
                "-- Please refer to https://docs.wso2.com/display/SP400/Quick+Start+Guide on getting started with SP editor. \n" +
                "\n" +
                "define window TwoMinTempWindow (roomNo int, temp double) time(2 min);\n" +
                "define stream CheckStream (requestId string);\n" +
                "\n" +
                "from CheckStream as C join TwoMinTempWindow as T\n" +
                "    on T.temp > 40\n" +
                "select requestId, count(T.temp) as count\n" +
                "insert into HighTempCountStream;";

        String siddhiApp5 = "define stream Instream(name string, age int);\n" +
                "\n" +
                "define function concatFn[javascript] return string {\n" +
                "    var str1 = data[0];\n" +
                "    var str2 = data[1];\n" +
                "    var str3 = data[2];\n" +
                "    var responce = str1 + str2 + str3;\n" +
                "    return responce;\n" +
                "};";

        SiddhiAppConverter siddhiAppConverter = new SiddhiAppConverter();
        EventFlow eventFlow = siddhiAppConverter.generateDesign(siddhiApp5);
        System.out.println(siddhiAppConverter.generateCode(eventFlow));
    }

}
