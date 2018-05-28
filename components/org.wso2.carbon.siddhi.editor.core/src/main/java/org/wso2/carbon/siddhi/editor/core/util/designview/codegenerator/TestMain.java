package org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator;

import org.wso2.carbon.siddhi.editor.core.util.designview.SiddhiAppConverter;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.EventFlow;

public class TestMain {

    public static void main(String[] args) {
        String siddhiApp = "@App:name(\"ConversionTest\")\n" +
                "@App:description(\"Test the CodeGenerator and DesignGenerator classes\")\n" +
                "\n" +
                "-- Definition Tests\n" +
                "@sink(type='log')\n" +
                "define stream InStream(name string, age int);\n" +
                "@sink(type='log')\n" +
                "define stream NewStream(name string, age int);\n" +
                "\n" +
                "define table InTable(name string, age int);\n" +
                "\n" +
                "define window InWindow(name string, age int) time(1 min);\n" +
                "\n" +
                "define trigger InTrigger at every 5 min;\n" +
                "\n" +
                "define aggregation InAggregation\n" +
                "    from InStream\n" +
                "    select name, avg(age) as avgAge\n" +
                "    group by name\n" +
                "    aggregate every min;\n" +
                "\n" +
                "define function concatFn[javascript] return string {\n" +
                "    var str1 = data[0];\n" +
                "    var str2 = data[1];\n" +
                "    var str3 = data[2];\n" +
                "    var responce = str1 + str2 + str3;\n" +
                "    return responce;\n" +
                "};\n" +
                "\n" +
                "-- Queries\n" +
                "from InStream[age >= 18][age < 70]#window.time(10 min)[age < 30]#str:tokenize('string', 'regex')\n" +
                "select *\n" +
                "insert into OutStream1;\n" +
                "\n" +
                "from InStream[age >= 18][age < 70][age < 30]#str:tokenize('string', 'regex')#window.time(10 min) unidirectional\n" +
                "    join NewStream\n" +
                "    on InStream.name == NewStream.name\n" +
                "    select InStream.name as name, avg(NewStream.age) as avgAge\n" +
                "    insert into OutStream2;\n";

        SiddhiAppConverter siddhiAppConverter = new SiddhiAppConverter();
        EventFlow eventFlow = siddhiAppConverter.generateDesign(siddhiApp);
        System.out.println(siddhiAppConverter.generateCode(eventFlow));
    }

}
