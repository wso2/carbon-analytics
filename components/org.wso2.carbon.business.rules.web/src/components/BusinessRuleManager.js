import React from 'react';
import ReactDOM from 'react-dom';
// import './index.css';
// Material-UI
import Typography from 'material-ui/Typography';
import CreateButton from "./CreateButton";
import Header from "./Header";
import TemplateGroupSelector from "./TemplateGroupSelector";

/** todo: not this. This is Business Rule creator's earlier implementation
 * Allows to create a Business Rule either from scratch or from a Template
 */
class BusinessRuleManager extends React.Component {
    // TEST FUNCTION
    loadTemplateGroupSelector() {
        ReactDOM.render(<TemplateGroupSelector
            templateGroups={[
                {
                    "name": "Stock Exchange",
                    "uuid": "stock-exchange",
                    "description": "Domain for stock exchange analytics",
                    "ruleTemplates": [
                        {
                            "name": "Stock Data Analysis",
                            "uuid": "stock-data-analysis",
                            "type": "template",
                            "instanceCount": "many",
                            "script":
                            "/*\n" +
                            "          Derives share volume margin deviation, between the user given min and max share volume margins\n" +
                            "          */\n" +
                            "          function deriveVolumeMarginDeviation(minShareVolumesMargin, maxShareVolumesMargin){\n" +
                            "            return (maxShareVolumesMargin - minShareVolumesMargin);\n" +
                            "          }\n" +
                            "\n" +
                            "          /*\n" +
                            "            Derives kafka topic / topic list name with the prefix 'kafka_', since type of source is kafka\n" +
                            "          */\n" +
                            "          function deriveKafkaTopic(givenName){\n" +
                            "            return 'kafka_'+givenName\n" +
                            "          }\n" +
                            "\n" +
                            "          var sourceKafkaTopicList = deriveKafkaTopic('${sourceTopicList}');\n" +
                            "          var sinkKafkaTopic = deriveKafkaTopic('${sinkTopic}');\n" +
                            "          // To test whether this unwanted variable causes any issues\n" +
                            "          var marginDeviation = deriveVolumeMarginDeviation(${minShareVolumesMargin}, ${maxShareVolumesMargin});\n" +
                            "          var mediumShareVolumesMargin = marginDeviation/2;",
                            "description": "Analyzes data of company stocks, related to share volumes",
                            "templates": [
                                {
                                    "type": "siddhiApp",
                                    "content":
                                    "@App:name('lowShareVolumesAnalysis')\n" +
                                    "\n" +
                                    "            @source(type='kafka', topic.list='${sourceKafkaTopicList}', partition.no.list='0', threading.option='single.thread', group.id='group', bootstrap.servers='localhost:9092', @map(type=${sourceMapType}))\n" +
                                    "            define stream StockInputStream(symbol string, price float, shareVolume long, tradeVolume long, company string);\n" +
                                    "\n" +
                                    "            @sink(type='kafka', topic='${sinkKafkaTopic}', bootstrap.servers='localhost:9092', partition.no='0', @map(type=${sinkMapType}))\n" +
                                    "            define stream LowShareVolumesStream(symbol string, price float, totalVolume long, company string);\n" +
                                    "\n" +
                                    "            from StockInputStream[volume < ${minShareVolumesMargin}]\n" +
                                    "            select symbol, price, volume as totalVolume, company\n" +
                                    "            insert into LowShareVolumesStream;"
                                },
                                {
                                    "type":
                                        "siddhiApp",
                                    "content":
                                    "@App:name('mediumShareVolumesAnalysis')\n" +
                                    "\n" +
                                    "            @source(type='kafka', topic.list='${sourceKafkaTopicList}', partition.no.list='0', threading.option='single.thread', group.id='group', bootstrap.servers='localhost:9092', @map(type=${sourceMapType}))\n" +
                                    "            define stream StockInputStream(symbol string, price float, shareVolume long, tradeVolume long, company string);\n" +
                                    "\n" +
                                    "            @sink(type='kafka', topic='${sinkKafkaTopic}', bootstrap.servers='localhost:9092', partition.no='0', @map(type=${sinkMapType}))\n" +
                                    "            define stream MediumShareVolumesStream(symbol string, price float, totalVolume long, company string);\n" +
                                    "\n" +
                                    "            from StockInputStream[volume == ${mediumShareVolumesMargin}]\n" +
                                    "            select symbol, price, volume as totalVolume, company\n" +
                                    "            insert into MediumShareVolumesStream;"
                                }
                            ],
                            "properties":
                                {
                                    "sourceTopicList":
                                        {
                                            "fieldName":
                                                "Data source topic list", "description":
                                            "Name of the data source list that you want to subscribe", "defaultValue":
                                            "StockStream", "options":
                                            ["StockStream", "SampleStockStream2"]
                                        }
                                    ,
                                    "sourceMapType":
                                        {
                                            "fieldName":
                                                "Mapping type for data source", "description":
                                            "Data source maps data in this format, to the input stream", "defaultValue":
                                            "xml", "options":
                                            ["xml", "json"]
                                        }
                                    ,
                                    "sinkTopic":
                                        {
                                            "fieldName":
                                                "Result topic",
                                            "description":
                                                "Name of the topic that you want to output the filtered results",
                                            "defaultValue":
                                                "resultTopic",
                                            "options":
                                                ["resultTopic", "SampleResultTopic2"]
                                        }
                                    ,
                                    "sinkMapType":
                                        {
                                            "fieldName":
                                                "Mapping type for data sink",
                                            "description":
                                                "Data from the output stream, is mapped in this format to the sink",
                                            "defaultValue":
                                                "xml",
                                            "options":
                                                ["xml", "json"]
                                        }
                                    ,
                                    "minShareVolumesMargin":
                                        {
                                            "fieldName":
                                                "Minimum margin for volume shares",
                                            "description":
                                                "Shares that have a volume below this margin are considered as low volume shares",
                                            "defaultValue":
                                                "10"
                                        }
                                    ,
                                    "maxShareVolumesMargin":
                                        {
                                            "fieldName":
                                                "Maximum margin for volume shares",
                                            "description":
                                                "Shares that have a volume above this margin are considered as high volume shares",
                                            "defaultValue":
                                                "10000"
                                        }
                                }
                        },
                        {
                            "name":
                                "Stock Exchange Input",
                            "uuid":
                                "stock-exchange-input",
                            "type":
                                "input",
                            "instanceCount":
                                "many",
                            "script":
                            "/*\n" +
                            "          Derives kafka topic list name with the prefix 'kafka_', since type of source is kafka\n" +
                            "          */\n" +
                            "          function deriveKafkaTopicListName(givenName){\n" +
                            "            return 'kafka_'+givenName;\n" +
                            "          }\n" +
                            "          var kafkaTopicList = deriveKafkaTopicListName('${topicList}')",
                            "description":
                                "configered kafka source to recieve stock exchange updates",
                            "templates":
                                [
                                    {
                                        "type": "siddhiApp",
                                        "content":
                                        "@App:name('appName1')\n" +
                                        "\n" +
                                        "            @source(type='kafka', topic.list=${kafkaTopicList}, partition.no.list='0', threading.option='single.thread', group.id='group', bootstrap.servers='localhost:9092', @map(type='json'))\n" +
                                        "            define stream StockStream(symbol string, price float, volume long, company string, );",
                                        "exposedStreamDefinition": "define stream StockStream(company string, symbol string, shareVolume long, tradeVolume long, price float, changePercentage float);"
                                    }
                                ],
                            "properties":
                                {
                                    "topicList":
                                        {
                                            "fieldName": "Data source topic list",
                                            "description": "Name of the data source list that you want to subscribe",
                                            "defaultValue": "StockStream",
                                            "options": ["StockStream", "SampleStockStream2"]
                                        }
                                }
                        },
                        {
                            "name":
                                "Stock Exchange Output",
                            "uuid":
                                "stock-exchange-output",
                            "type":
                                "output",
                            "instanceCount":
                                "many",
                            "script":
                            "/*\n" +
                            "          Derives kafka topic name with the prefix 'kafka_', since type of sink is kafka\n" +
                            "          */\n" +
                            "          function deriveKafkaTopicName(givenName){\n" +
                            "            return 'kafka_'+givenName;\n" +
                            "          }\n" +
                            "          var kafkaTopic = deriveKafkaTopicName('${resultTopic}')",
                            "description":
                                "configured kafka sink to output the filterd stock exchange data",
                            "templates":
                                [
                                    {
                                        "type": "siddhiApp",
                                        "content":
                                        "@App:name('appName2')\n" +
                                        "\n" +
                                        "             @sink(type='kafka', topic=${kafkaTopic}, bootstrap.servers='localhost:9092', partition.no='0', @map(type='xml'))\n" +
                                        "             define stream StockStream(symbol string, price float, volume long, company string, );\",\n" +
                                        "             \"exposedStreamDefinition\" :\"define stream StockStream( companyName string, companySymbol string, changePercentage float);"
                                    }
                                ],
                            "properties":
                                {
                                    "resultTopic":
                                        {
                                            "fieldName":
                                                "Result Topic",
                                            "description": "Name of the topic that you want to output the filtered results",
                                            "defaultValue": "resultTopic",
                                            "options": ["resultTopic", "SampleResultTopic2"]
                                        }
                                }
                        }
                    ]
                },
                {
                    "name": "Stock Exchange 2",
                    "uuid": "stock-exchange-2",
                    "description": "Copied domain for stock exchange analytics",
                    "ruleTemplates": [
                        {
                            "name": "Stock Data Analysis",
                            "uuid": "stock-data-analysis",
                            "type": "template",
                            "instanceCount": "many",
                            "script":
                            "/*\n" +
                            "          Derives share volume margin deviation, between the user given min and max share volume margins\n" +
                            "          */\n" +
                            "          function deriveVolumeMarginDeviation(minShareVolumesMargin, maxShareVolumesMargin){\n" +
                            "            return (maxShareVolumesMargin - minShareVolumesMargin);\n" +
                            "          }\n" +
                            "\n" +
                            "          /*\n" +
                            "            Derives kafka topic / topic list name with the prefix 'kafka_', since type of source is kafka\n" +
                            "          */\n" +
                            "          function deriveKafkaTopic(givenName){\n" +
                            "            return 'kafka_'+givenName\n" +
                            "          }\n" +
                            "\n" +
                            "          var sourceKafkaTopicList = deriveKafkaTopic('${sourceTopicList}');\n" +
                            "          var sinkKafkaTopic = deriveKafkaTopic('${sinkTopic}');\n" +
                            "          // To test whether this unwanted variable causes any issues\n" +
                            "          var marginDeviation = deriveVolumeMarginDeviation(${minShareVolumesMargin}, ${maxShareVolumesMargin});\n" +
                            "          var mediumShareVolumesMargin = marginDeviation/2;",
                            "description": "Analyzes data of company stocks, related to share volumes",
                            "templates": [
                                {
                                    "type": "siddhiApp",
                                    "content":
                                    "@App:name('lowShareVolumesAnalysis')\n" +
                                    "\n" +
                                    "            @source(type='kafka', topic.list='${sourceKafkaTopicList}', partition.no.list='0', threading.option='single.thread', group.id='group', bootstrap.servers='localhost:9092', @map(type=${sourceMapType}))\n" +
                                    "            define stream StockInputStream(symbol string, price float, shareVolume long, tradeVolume long, company string);\n" +
                                    "\n" +
                                    "            @sink(type='kafka', topic='${sinkKafkaTopic}', bootstrap.servers='localhost:9092', partition.no='0', @map(type=${sinkMapType}))\n" +
                                    "            define stream LowShareVolumesStream(symbol string, price float, totalVolume long, company string);\n" +
                                    "\n" +
                                    "            from StockInputStream[volume < ${minShareVolumesMargin}]\n" +
                                    "            select symbol, price, volume as totalVolume, company\n" +
                                    "            insert into LowShareVolumesStream;"
                                },
                                {
                                    "type":
                                        "siddhiApp",
                                    "content":
                                    "@App:name('mediumShareVolumesAnalysis')\n" +
                                    "\n" +
                                    "            @source(type='kafka', topic.list='${sourceKafkaTopicList}', partition.no.list='0', threading.option='single.thread', group.id='group', bootstrap.servers='localhost:9092', @map(type=${sourceMapType}))\n" +
                                    "            define stream StockInputStream(symbol string, price float, shareVolume long, tradeVolume long, company string);\n" +
                                    "\n" +
                                    "            @sink(type='kafka', topic='${sinkKafkaTopic}', bootstrap.servers='localhost:9092', partition.no='0', @map(type=${sinkMapType}))\n" +
                                    "            define stream MediumShareVolumesStream(symbol string, price float, totalVolume long, company string);\n" +
                                    "\n" +
                                    "            from StockInputStream[volume == ${mediumShareVolumesMargin}]\n" +
                                    "            select symbol, price, volume as totalVolume, company\n" +
                                    "            insert into MediumShareVolumesStream;"
                                }
                            ],
                            "properties":
                                {
                                    "sourceTopicList":
                                        {
                                            "fieldName":
                                                "Data source topic list", "description":
                                            "Name of the data source list that you want to subscribe", "defaultValue":
                                            "StockStream", "options":
                                            ["StockStream", "SampleStockStream2"]
                                        }
                                    ,
                                    "sourceMapType":
                                        {
                                            "fieldName":
                                                "Mapping type for data source", "description":
                                            "Data source maps data in this format, to the input stream", "defaultValue":
                                            "xml", "options":
                                            ["xml", "json"]
                                        }
                                    ,
                                    "sinkTopic":
                                        {
                                            "fieldName":
                                                "Result topic",
                                            "description":
                                                "Name of the topic that you want to output the filtered results",
                                            "defaultValue":
                                                "resultTopic",
                                            "options":
                                                ["resultTopic", "SampleResultTopic2"]
                                        }
                                    ,
                                    "sinkMapType":
                                        {
                                            "fieldName":
                                                "Mapping type for data sink",
                                            "description":
                                                "Data from the output stream, is mapped in this format to the sink",
                                            "defaultValue":
                                                "xml",
                                            "options":
                                                ["xml", "json"]
                                        }
                                    ,
                                    "minShareVolumesMargin":
                                        {
                                            "fieldName":
                                                "Minimum margin for volume shares",
                                            "description":
                                                "Shares that have a volume below this margin are considered as low volume shares",
                                            "defaultValue":
                                                "10"
                                        }
                                    ,
                                    "maxShareVolumesMargin":
                                        {
                                            "fieldName":
                                                "Maximum margin for volume shares",
                                            "description":
                                                "Shares that have a volume above this margin are considered as high volume shares",
                                            "defaultValue":
                                                "10000"
                                        }
                                }
                        },
                        {
                            "name":
                                "Stock Exchange Input",
                            "uuid":
                                "stock-exchange-input",
                            "type":
                                "input",
                            "instanceCount":
                                "many",
                            "script":
                            "/*\n" +
                            "          Derives kafka topic list name with the prefix 'kafka_', since type of source is kafka\n" +
                            "          */\n" +
                            "          function deriveKafkaTopicListName(givenName){\n" +
                            "            return 'kafka_'+givenName;\n" +
                            "          }\n" +
                            "          var kafkaTopicList = deriveKafkaTopicListName('${topicList}')",
                            "description":
                                "configered kafka source to recieve stock exchange updates",
                            "templates":
                                [
                                    {
                                        "type": "siddhiApp",
                                        "content":
                                        "@App:name('appName1')\n" +
                                        "\n" +
                                        "            @source(type='kafka', topic.list=${kafkaTopicList}, partition.no.list='0', threading.option='single.thread', group.id='group', bootstrap.servers='localhost:9092', @map(type='json'))\n" +
                                        "            define stream StockStream(symbol string, price float, volume long, company string, );",
                                        "exposedStreamDefinition": "define stream StockStream(company string, symbol string, shareVolume long, tradeVolume long, price float, changePercentage float);"
                                    }
                                ],
                            "properties":
                                {
                                    "topicList":
                                        {
                                            "fieldName": "Data source topic list",
                                            "description": "Name of the data source list that you want to subscribe",
                                            "defaultValue": "StockStream",
                                            "options": ["StockStream", "SampleStockStream2"]
                                        }
                                }
                        },
                        {
                            "name":
                                "Stock Exchange Output",
                            "uuid":
                                "stock-exchange-output",
                            "type":
                                "output",
                            "instanceCount":
                                "many",
                            "script":
                            "/*\n" +
                            "          Derives kafka topic name with the prefix 'kafka_', since type of sink is kafka\n" +
                            "          */\n" +
                            "          function deriveKafkaTopicName(givenName){\n" +
                            "            return 'kafka_'+givenName;\n" +
                            "          }\n" +
                            "          var kafkaTopic = deriveKafkaTopicName('${resultTopic}')",
                            "description":
                                "configured kafka sink to output the filterd stock exchange data",
                            "templates":
                                [
                                    {
                                        "type": "siddhiApp",
                                        "content":
                                        "@App:name('appName2')\n" +
                                        "\n" +
                                        "             @sink(type='kafka', topic=${kafkaTopic}, bootstrap.servers='localhost:9092', partition.no='0', @map(type='xml'))\n" +
                                        "             define stream StockStream(symbol string, price float, volume long, company string, );\",\n" +
                                        "             \"exposedStreamDefinition\" :\"define stream StockStream( companyName string, companySymbol string, changePercentage float);"
                                    }
                                ],
                            "properties":
                                {
                                    "resultTopic":
                                        {
                                            "fieldName":
                                                "Result Topic",
                                            "description": "Name of the topic that you want to output the filtered results",
                                            "defaultValue": "resultTopic",
                                            "options": ["resultTopic", "SampleResultTopic2"]
                                        }
                                }
                        }
                    ]
                }
            ]}
        />, document.getElementById('root'));
    }

    render() {
        return (
            <div>
                <Header
                    title="Business Rule Manager"
                />
                <center>
                    <Typography type="headline">
                        Let's create a business rule
                    </Typography>
                    <br/>
                    <CreateButton
                        onClick={(e) => this.loadTemplateGroupSelector()}
                        text='From Template'
                    />
                    <CreateButton
                        text='From The Scratch'
                    />
                </center>
            </div>
        );
    }
}

export default BusinessRuleManager;
