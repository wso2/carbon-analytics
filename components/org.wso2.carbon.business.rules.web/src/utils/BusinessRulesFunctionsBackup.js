import React from 'react';
import ReactDOM from 'react-dom';
// import './index.css';
// Material-UI
import TemplateGroupSelector from "../components/TemplateGroupSelector";
import BusinessRulesConstants from "./BusinessRulesConstants";
import RuleTemplateSelector from "../components/RuleTemplateSelector";
import BusinessRuleEditor from "../components/BusinessRuleEditor";
import BusinessRuleFromScratchCreator from "../components/BusinessRuleFromScratchCreator";
import BusinessRuleCreator from "../components/BusinessRuleCreator";

class BusinessRulesFunctions {
    // Load from API and store
    availableTemplateGroups = this.getTemplateGroups()
    /* Start of Methods related to API calls **************************************/
    availableBusinessRules = this.getBusinessRules()
    // Properties given in the form, for Creating a Business Rule
    businessRuleEnteredValues = {
        'uuid': '',
        'name': '',
        'templateGroupUUID': '',
        'ruleTemplateUUID': '',
        'type': '',
        'properties': {}
    }

    /**
     * Loads the edit form for the given Business Rule
     *
     * @param businessRuleUUID
     */
    static loadBusinessRuleEditor(businessRuleUUID) {
        ReactDOM.render(
            <BusinessRuleEditor
                businessRule={this.getBusinessRule(businessRuleUUID)}
            />, document.getElementById('root'));
    }

    static loadBusinessRuleCreator() {
        ReactDOM.render(
            <BusinessRuleCreator/>,
            document.getElementById('root')
        );
    }

    /**
     * Shows available Template Groups to select one,
     * for creating a Business Rule from template
     */
    static loadTemplateGroupSelector() {
        ReactDOM.render(<TemplateGroupSelector
            templateGroups={this.getTemplateGroups()}
        />, document.getElementById('root'));
    }

    /**
     * Shows form to create a BusinessRule from scratch,
     * by selecting input & output rule templates from a list of available ones
     */
    static loadBusinessRuleFromScratchCreator() {
        ReactDOM.render(
            <BusinessRuleFromScratchCreator
                templateGroups={this.getTemplateGroups()}/>,
            document.getElementById('root')
        )
    }

    /**
     * Shows available Rule Templates of given type under given templateGroup,
     * to select one and generate a form out of that
     */
    static loadRuleTemplateSelector(templateGroupUUID, ruleTemplateTypeFilter) {
        var foundTemplateGroup = this.getTemplateGroup(templateGroupUUID)
        var availableRuleTemplates = this.getRuleTemplates(templateGroupUUID)
        ReactDOM.render(<RuleTemplateSelector
            selectedTemplateGroup={foundTemplateGroup}
            ruleTemplateTypeFilter={ruleTemplateTypeFilter}
            ruleTemplates={availableRuleTemplates}
        />, document.getElementById('root'));
    }

// API [4] is the POST for CreateBusinessRule

    /** [1]
     * Gets available Template Groups
     * todo: from API
     *
     * @returns {Array}
     */
    static getTemplateGroups() {
        // todo: remove hardcode *****************************
        var receivedTemplateGroups = [
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
                        "script": "/*\n" +
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
                                "type": "siddhiApp",
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
                        "properties": {
                            "sourceTopicList": {
                                "fieldName": "Data source topic list",
                                "description": "Name of the data source list that you want to subscribe",
                                "defaultValue": "StockStream",
                                "options": ["StockStream", "SampleStockStream2"]
                            },
                            "sourceMapType": {
                                "fieldName": "Mapping type for data source",
                                "description": "Data source maps data in this format, to the input stream",
                                "defaultValue": "xml",
                                "options": ["xml", "json"]
                            },
                            "sinkTopic": {
                                "fieldName": "Result topic",
                                "description": "Name of the topic that you want to output the filtered results",
                                "defaultValue": "resultTopic",
                                "options": ["resultTopic", "SampleResultTopic2"]
                            },
                            "sinkMapType": {
                                "fieldName": "Mapping type for data sink",
                                "description": "Data from the output stream, is mapped in this format to the sink",
                                "defaultValue": "xml",
                                "options": ["xml", "json"]
                            },
                            "minShareVolumesMargin": {
                                "fieldName": "Minimum margin for volume shares",
                                "description": "Shares that have a volume below this margin are considered as low volume shares",
                                "defaultValue": "10"
                            },
                            "maxShareVolumesMargin": {
                                "fieldName": "Maximum margin for volume shares",
                                "description": "Shares that have a volume above this margin are considered as high volume shares",
                                "defaultValue": "10000"
                            }
                        }
                    },
                    {
                        "name": "Stock Exchange Input",
                        "uuid": "stock-exchange-input",
                        "type": "input",
                        "instanceCount": "many",
                        "script":
                        "/*\n" +
                        "          Derives kafka topic list name with the prefix 'kafka_', since type of source is kafka\n" +
                        "          */\n" +
                        "          function deriveKafkaTopicListName(givenName){\n" +
                        "            return 'kafka_'+givenName;\n" +
                        "          }\n" +
                        "          var kafkaTopicList = deriveKafkaTopicListName('${topicList}')",
                        "description": "configured kafka source to recieve stock exchange updates",
                        "templates": [
                            {
                                "type": "siddhiApp",
                                "content":
                                "@App:name('appName1')\n" +
                                "\n" +
                                "            @source(type='kafka', topic.list=${kafkaTopicList}, partition.no.list='0', threading.option='single.thread', group.id='group', bootstrap.servers='localhost:9092', @map(type='json'))\n" +
                                "            define stream StockStream(symbol string, price float, volume long, name string);",
                                "exposedStreamDefinition": "define stream StockStream(symbol string, price float, volume long, name string);"
                            }
                        ],
                        "properties": {
                            "topicList": {
                                "fieldName": "Data source topic list",
                                "description": "Name of the data source list that you want to subscribe",
                                "defaultValue": "StockStream",
                                "options": ["StockStream", "SampleStockStream2"]
                            },
                            "topicList2": {
                                "fieldName": "Data source topic list",
                                "description": "Name of the data source list that you want to subscribe",
                                "defaultValue": "StockStream",
                                "options": ["StockStream", "SampleStockStream2"]
                            }
                        }
                    },
                    {
                        "name": "Stock Exchange Input 2",
                        "uuid": "stock-exchange-input-2",
                        "type": "input",
                        "instanceCount": "many",
                        "script":
                        "/*\n" +
                        "          Derives kafka topic list name with the prefix 'kafka_', since type of source is kafka\n" +
                        "          */\n" +
                        "          function deriveKafkaTopicListName(givenName){\n" +
                        "            return 'kafka_'+givenName;\n" +
                        "          }\n" +
                        "          var kafkaTopicList = deriveKafkaTopicListName('${topicList}')",
                        "description": "configured kafka source to recieve stock exchange updates",
                        "templates": [
                            {
                                "type": "siddhiApp",
                                "content":
                                "@App:name('appName1')\n" +
                                "\n" +
                                "            @source(type='kafka', topic.list=${kafkaTopicList}, partition.no.list='0', threading.option='single.thread', group.id='group', bootstrap.servers='localhost:9092', @map(type='json'))\n" +
                                "            define stream StockStream(symbol string, price float, volume long, name string);",
                                "exposedStreamDefinition": "define stream StockStream(symbol string, price float, volume long, name string);"
                            }
                        ],
                        "properties": {
                            "topicList": {
                                "fieldName": "Data source topic list",
                                "description": "Name of the data source list that you want to subscribe",
                                "defaultValue": "StockStream_1",
                                "options": ["StockStream_1", "SampleStockStream2_1"]
                            }
                        }
                    },
                    {
                        "name": "Stock Exchange Output",
                        "uuid": "stock-exchange-output",
                        "type": "output",
                        "instanceCount": "many",
                        "script":
                        "/*\n" +
                        "          Derives kafka topic name with the prefix 'kafka_', since type of sink is kafka\n" +
                        "          */\n" +
                        "          function deriveKafkaTopicName(givenName){\n" +
                        "            return 'kafka_'+givenName;\n" +
                        "          }\n" +
                        "          var kafkaTopic = deriveKafkaTopicName('${resultTopic}')",
                        "description": "configured kafka sink to output the filtered stock exchange data",
                        "templates": [
                            {
                                "type": "siddhiApp",
                                "content":
                                "@App:name('appName2')\n" +
                                "\n" +
                                "             @sink(type='kafka', topic=${kafkaTopic}, bootstrap.servers='localhost:9092', partition.no='0', @map(type='xml'))\n" +
                                "             define stream StockStream(companyName string, companySymbol string, sellingPrice float);",
                                "exposedStreamDefinition": "define stream StockStream(companyName string, companySymbol string, sellingPrice float);"
                            }
                        ],
                        "properties": {
                            "resultTopic": {
                                "fieldName": "Result Topic",
                                "description": "Name of the first topic that you want to output the filtered results",
                                "defaultValue": "resultTopic",
                                "options": ["resultTopic", "SampleResultTopic2"]
                            },
                            "resultTopic2": {
                                "fieldName": "Result Topic 2",
                                "description": "Name of the second topic that you want to output the filtered results",
                                "defaultValue": "resultTopic_1",
                                "options": ["resultTopic_1", "SampleResultTopic2_1"]
                            }
                        }
                    },
                    {
                        "name": "Stock Exchange Output 2",
                        "uuid": "stock-exchange-output-2",
                        "type": "output",
                        "instanceCount": "many",
                        "script":
                        "/*\n" +
                        "          Derives kafka topic name with the prefix 'kafka_', since type of sink is kafka\n" +
                        "          */\n" +
                        "          function deriveKafkaTopicName(givenName){\n" +
                        "            return 'kafka_'+givenName;\n" +
                        "          }\n" +
                        "          var kafkaTopic = deriveKafkaTopicName('${resultTopic}')",
                        "description": "configured kafka sink to output the filtered stock exchange data",
                        "templates": [
                            {
                                "type": "siddhiApp",
                                "content":
                                "@App:name('appName2')\n" +
                                "\n" +
                                "             @sink(type='kafka', topic=${kafkaTopic}, bootstrap.servers='localhost:9092', partition.no='0', @map(type='xml'))\n" +
                                "             define stream StockStream(companyName string, companySymbol string, sellingPrice float);",
                                "exposedStreamDefinition": "define stream StockStream(companyName string, companySymbol string, sellingPrice float, additional float);"
                            }
                        ],
                        "properties": {
                            "resultTopic": {
                                "fieldName": "Result Topic",
                                "description": "Name of the topic that you want to output the filtered results",
                                "defaultValue": "resultTopic_1",
                                "options": ["resultTopic_1", "SampleResultTopic2_1"]
                            }
                        }
                    }
                ]
            },
            {
                "name": "Stock Exchange 2",
                "uuid": "stock-exchange-2",
                "description": "Copied Domain for stock exchange analytics",
                "ruleTemplates": [
                    {
                        "name": "Stock Data Analysis 2",
                        "uuid": "stock-data-analysis-2",
                        "type": "template",
                        "instanceCount": "many",
                        "script": "/*\n" +
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
                                "type": "siddhiApp",
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
                        "properties": {
                            "sourceTopicList": {
                                "fieldName": "Data source topic list",
                                "description": "Name of the data source list that you want to subscribe",
                                "defaultValue": "StockStream_2",
                                "options": ["StockStream_2", "SampleStockStream2_2"]
                            },
                            "sourceMapType": {
                                "fieldName": "Mapping type for data source",
                                "description": "Data source maps data in this format, to the input stream",
                                "defaultValue": "xml",
                                "options": ["xml", "json"]
                            },
                            "sinkTopic": {
                                "fieldName": "Result topic",
                                "description": "Name of the topic that you want to output the filtered results",
                                "defaultValue": "resultTopic_2",
                                "options": ["resultTopic_2", "SampleResultTopic2_2"]
                            },
                            "sinkMapType": {
                                "fieldName": "Mapping type for data sink",
                                "description": "Data from the output stream, is mapped in this format to the sink",
                                "defaultValue": "xml",
                                "options": ["xml", "json"]
                            },
                            "minShareVolumesMargin": {
                                "fieldName": "Minimum margin for volume shares",
                                "description": "Shares that have a volume below this margin are considered as low volume shares",
                                "defaultValue": "10"
                            },
                            "maxShareVolumesMargin": {
                                "fieldName": "Maximum margin for volume shares",
                                "description": "Shares that have a volume above this margin are considered as high volume shares",
                                "defaultValue": "10000"
                            }
                        }
                    },
                    {
                        "name": "Stock Exchange Input 2",
                        "uuid": "stock-exchange-input-2",
                        "type": "input",
                        "instanceCount": "many",
                        "script":
                        "/*\n" +
                        "          Derives kafka topic list name with the prefix 'kafka_', since type of source is kafka\n" +
                        "          */\n" +
                        "          function deriveKafkaTopicListName(givenName){\n" +
                        "            return 'kafka_'+givenName;\n" +
                        "          }\n" +
                        "          var kafkaTopicList = deriveKafkaTopicListName('${topicList}')",
                        "description": "configured kafka source to recieve stock exchange updates",
                        "templates": [
                            {
                                "type": "siddhiApp",
                                "content":
                                "@App:name('appName1')\n" +
                                "\n" +
                                "            @source(type='kafka', topic.list=${kafkaTopicList}, partition.no.list='0', threading.option='single.thread', group.id='group', bootstrap.servers='localhost:9092', @map(type='json'))\n" +
                                "            define stream StockStream(symbol string, price float, volume long, name string);",
                                "exposedStreamDefinition": "define stream StockStream(symbol string, price float, volume long, name string);"
                            }
                        ],
                        "properties": {
                            "topicList": {
                                "fieldName": "Data source topic list",
                                "description": "Name of the data source list that you want to subscribe",
                                "defaultValue": "StockStream_2",
                                "options": ["StockStream_2", "SampleStockStream2_2"]
                            }
                        }
                    },
                    {
                        "name": "Stock Exchange Output 2",
                        "uuid": "stock-exchange-output-2",
                        "type": "output",
                        "instanceCount": "many",
                        "script":
                        "/*\n" +
                        "          Derives kafka topic name with the prefix 'kafka_', since type of sink is kafka\n" +
                        "          */\n" +
                        "          function deriveKafkaTopicName(givenName){\n" +
                        "            return 'kafka_'+givenName;\n" +
                        "          }\n" +
                        "          var kafkaTopic = deriveKafkaTopicName('${resultTopic}')",
                        "description": "configured kafka sink to output the filtered stock exchange data",
                        "templates": [
                            {
                                "type": "siddhiApp",
                                "content":
                                "@App:name('appName2')\n" +
                                "\n" +
                                "             @sink(type='kafka', topic=${kafkaTopic}, bootstrap.servers='localhost:9092', partition.no='0', @map(type='xml'))\n" +
                                "             define stream StockStream(companyName string, companySymbol string, sellingPrice float);",
                                "exposedStreamDefinition": "define stream StockStream(companyName string, companySymbol string, sellingPrice float);"
                            }
                        ],
                        "properties": {
                            "resultTopic": {
                                "fieldName": "Result Topic",
                                "description": "Name of the topic that you want to output the filtered results",
                                "defaultValue": "resultTopic_2",
                                "options": ["resultTopic_2", "SampleResultTopic2_2"]
                            }
                        }
                    }
                ]
            }
        ]

        return receivedTemplateGroups
        // todo: *********************************************
        // todo: Get Template Groups from API
    }

    /** [2]
     * Get available Rule Templates, belong to the given Template Group
     * todo: from API
     *
     * @param templateGroupName
     */
    static getRuleTemplates(templateGroupUUID) {
        // todo: remove hardcode ******************************
        var templateGroups = this.getTemplateGroups()
        for (let templateGroup of templateGroups) {
            if (templateGroup.uuid === templateGroupUUID) {
                return templateGroup.ruleTemplates
            }
        }
        // todo: **********************************************
        // todo: Return Rule Templates from API
    }

    /** [3]
     * Get available Properties, belong to the given Template Group and Rule Template
     * todo: from API
     *
     * @param templateGroupName
     * @param ruleTemplateName
     * @returns {*|Array}
     */
    static getRuleTemplateProperties(templateGroupName, ruleTemplateName) {
        // todo: remove hardcode ******************************
        var ruleTemplates
        for (let templateGroup of this.availableTemplateGroups) {
            if (templateGroup.name === templateGroupName) {
                ruleTemplates = templateGroup.ruleTemplates
                break
            }
        }
        for (let ruleTemplate of ruleTemplates) {
            if (ruleTemplate.name === ruleTemplateName) {
                return ruleTemplate.properties
            }
        }
        // todo: **********************************************
        // todo: Return Properties from API
    }

    /** [5]
     * Gets available BusinessRulesCreator
     * todo: from API
     *
     * @returns {[null,null]}
     */
    static getBusinessRules() {
        // todo: remove hardcode *****************************
        var receivedBusinessRules = [
            {
                "uuid": "my-stock-data-analysis",
                "name": "My Stock Data Analysis",
                "templateGroupUUID": "stock-exchange",
                "ruleTemplateUUID": "stock-data-analysis",
                "type": "template",
                "properties": {
                    "sourceTopicList": "StockStream",
                    "sourceMapType": "xml",
                    "sinkTopic": "resultTopic",
                    "sinkMapType": "xml",
                    "minShareVolumesMargin": "20",
                    "maxShareVolumesMargin": "10000"
                }
            },
            {
                "uuid": "custom-stock-exchange-analysis-for-wso2",
                "name": "Custom Stock Exchange Analysis for WSO2",
                "templateGroupUUID": "stock-exchange",
                "inputRuleTemplateUUID": "stock-exchange-input",
                "outputRuleTemplateUUID": "stock-exchange-output",
                "type": "scratch",
                "properties": {
                    "inputData": {
                        "topicList": "SampleStockStream2",
                        "topicList2": "StockStream"
                    },
                    "ruleComponents": {
                        "filterRules": ["price > 1000", "volume < 50", "name == 'WSO2 Inc'"],
                        "ruleLogic": ["1 OR (2 AND 3)"]
                    },
                    "outputData": {
                        "resultTopic": "SampleResultTopic2",
                        "resultTopic2": "SampleResultTopic2_1"
                    },
                    "outputMappings": {
                        "companyName": "name",
                        "companySymbol": "symbol",
                        "sellingPrice": "price"
                    }
                }
            }
        ]

        return receivedBusinessRules
        // todo: *********************************************
        // todo: Get BusinessRulesCreator from API ******************
    }

// End of Functions that have API calls unnecessarily //////////////////////////

    /* End of Methods related to API calls ****************************************/

    /** [6]
     * Gets the BusinessRule with the given UUID
     * todo: from API
     *
     * @param businessRuleUUID
     * @returns {null|null}
     */
    static getBusinessRule(businessRuleUUID) {
        // todo: remove hardcode ******************************
        for (let businessRule of this.getBusinessRules()) {
            if (businessRuleUUID === businessRule.uuid) {
                return businessRule
            }
        }
        // todo: *********************************************
        // todo: Get BusinessRule from API *******************

    }

// Functions that have API calls unnecessarily /////////////////////////////////
    /**
     * Gets the Template Group with the given name
     * todo: from API (We have available templateGroups in front end itself)
     *
     * @param templateGroupName
     * @returns {*}
     */
    static getTemplateGroup(templateGroupUUID) {
        // todo: remove hardcode ******************************
        for (let templateGroup of this.getTemplateGroups()) {
            if (templateGroup.uuid === templateGroupUUID) {
                return templateGroup
            }
        }
        // todo: **********************************************
        // todo: Return Template Group from API
    }

    /**
     * Gets the Rule Template with the given name, that belongs to the given Template Group name
     * todo: from API (We have available templateGroups in front end itself)
     * todo: make sure to assign the belonging templateGroup for ruleTemplate
     *
     * @param templateGroupName
     * @param ruleTemplateName
     * @returns {*}
     */
    static getRuleTemplate(templateGroupUUID, ruleTemplateUUID) {
        // todo: remove hardcode ******************************
        var ruleTemplates
        for (let templateGroup of this.getTemplateGroups()) {
            if (templateGroup.uuid === templateGroupUUID) {
                var foundTemplateGroupObject = templateGroup
                ruleTemplates = templateGroup.ruleTemplates
                break
            }
        }
        for (let ruleTemplate of ruleTemplates) {
            if (ruleTemplate.uuid === ruleTemplateUUID) {
                var foundRuleTemplateObject = ruleTemplate
                // Assign belonging Template Group
                foundRuleTemplateObject['templateGroup'] = foundTemplateGroupObject

                return ruleTemplate
            }
        }
        // todo: **********************************************
        // todo: Return Rule Template from API
    }

    /**
     * TODO : Implement
     * Gets the deployment status of a given Business Rule
     *
     * @param businessRuleUUID
     */
    static getBusinessRuleDeploymentStatus(businessRuleUUID) {
        // Generates a random status for now
        let statuses = [
            BusinessRulesConstants.BUSINESS_RULE_DEPLOYMENT_STATUS_DEPLOYED,
            BusinessRulesConstants.BUSINESS_RULE_DEPLOYMENT_STATUS_NOT_DEPLOYED
        ]

        return statuses[Math.floor(Math.random() * statuses.length)]
    }

    /* Roughly implemented functions *//////////////////////////////////////////////////////////////////////////////////////

    /* End of Roughly implemented functions *///////////////////////////////////////////////////////////////////////////////

    /**
     * Generates UUID for a given Business Rule name
     *
     * @param businessRuleName
     * @returns {string}
     */
    static generateBusinessRuleUUID(businessRuleName) {
        return businessRuleName.toLowerCase().split(' ').join('-')
    }
}

export default BusinessRulesFunctions;
