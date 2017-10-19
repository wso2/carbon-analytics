// todo: bind handleClicks in the new way
// todo: refactor TemplateGroup as BusinessDomain
// todo: No cards for Rule Templates. Replace with Selects. Show description and preview form below to give an idea
import React from 'react';
import ReactDOM from 'react-dom';
// import './index.css';
// Material-UI
import Typography from 'material-ui/Typography';
import Card, {CardContent, CardHeader} from 'material-ui/Card';
import Button from 'material-ui/Button';
import Cake from 'material-ui-icons/Cake'
import Menu from 'material-ui-icons/Menu'
import Code from 'material-ui-icons/Code'
import IconButton from 'material-ui/IconButton';
import TextField from 'material-ui/TextField';
import {FormControl, FormHelperText} from 'material-ui/Form';
import Input, {InputLabel} from 'material-ui/Input';
import {MenuItem} from 'material-ui/Menu';
import Select from 'material-ui/Select';


// Rule Template types
const RULE_TEMPLATE_TYPE_TEMPLATE = "template"

/**
 * Creates Business Rule from template / from scratch
 */
class BusinessRulesCreator extends React.Component {
    render() {
        return (
            <div>
                <Typography type="headline" component="h2">
                    Let's create a business rule
                </Typography>
                <br/>
                <div>

                    <Button fab color="primary" aria-label="add" onClick={(e) => runBusinessRuleCreatorFromTemplate()}>
                        <Menu/>
                    </Button>

                    &nbsp;&nbsp;

                    <Button fab color="default" aria-label="edit" onClick={(e) => alert("Not implemented yet")}>
                        <Code/>
                    </Button>


                </div>
            </div>
        );
    }
}

/**
 * Displays available Template Groups as cards
 */
class TemplateGroups extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            templateGroups: getTemplateGroups()
        }
    }

    render() {
        const templateGroups = this.state.templateGroups.map((templateGroup) =>
            <div key={templateGroup.uuid}>
                <Card>
                    <CardHeader
                        title={templateGroup.name}
                        subheader={templateGroup.uuid}
                    />
                    <CardContent>
                        <Typography component="p">
                            {templateGroup.description}
                        </Typography>
                    </CardContent>
                    <IconButton aria-label="Create" color="default"
                                onClick={(e) => displayRuleTemplates(templateGroup)}>
                        <Cake/>
                    </IconButton>
                </Card>
                <br/>
            </div>
        );
        return (
            <div>
                <Typography type="headline" component="h2">
                    Select a template group
                </Typography>
                <br/>
                <div>{templateGroups}</div>
            </div>
        )
    }
}

/**
 * Displays all the available Rule Templates
 */
class RuleTemplates extends React.Component {
    /**
     * Updates selected Rule Template in the state,
     * when Rule Template is selected from the list
     */
    handleRuleTemplateSelected = name => event => {
        // should only update rule template
        let state = this.state
        // Get the selected rule template
        let selectedRuleTemplate = getRuleTemplate(this.state.templateGroup.name, event.target.value)
        // Assign selected rule template object in state
        state['selectedRuleTemplate'] = selectedRuleTemplate
        state['businessRuleEnteredValues']['ruleTemplateUUID'] = selectedRuleTemplate.uuid
        this.setState(state)
    };
    /**
     * Updates the Business Rule name in the maintained states, on change of the text field which allows to enter Business Rule Name
     */
    handleBusinessRuleNameChange = event => {
        let state = this.state
        state.businessRuleEnteredValues['name'] = event.target.value
        state.businessRuleEnteredValues['uuid'] = generateBusinessRuleUUID(event.target.value)
        this.setState(
            {state}
        )
        businessRuleEnteredValues = this.state.businessRuleEnteredValues
    }

    constructor(props) {
        super(props);
        this.state = {
            // The selected Template Group
            templateGroup: props.templateGroup,
            // Rule Templates under the selected template group
            ruleTemplates: props.ruleTemplates,
            // Selected Rule Template
            // Name & description initially entered to prevent null display value for Selection field
            selectedRuleTemplate: {'name': '', 'description': 'Please select a rule template'},
            // To store properties, entered for the Business Rule
            businessRuleEnteredValues: {
                'uuid': '',
                'name': '',
                'templateGroupUUID': props.templateGroup.uuid,
                'ruleTemplateUUID': '',
                'type': '',
                'properties': {}
            }
        }
    }

    render() {
        // Available Rule Templates, of type 'template', 'input' or 'output'
        var ruleTemplates = this.state.ruleTemplates

        // Only add Rule Templates of type 'template' and map to display todo: do for from scratch similar to this
        var templateTypeRuleTemplates = []
        for (let ruleTemplate of ruleTemplates) {
            if (ruleTemplate.type === RULE_TEMPLATE_TYPE_TEMPLATE) {
                templateTypeRuleTemplates.push(ruleTemplate)
            }
        }
        var ruleTemplatesToDisplay = templateTypeRuleTemplates.map((ruleTemplate) =>
            <MenuItem key={ruleTemplate.uuid} value={ruleTemplate.name}>
                {ruleTemplate.name}
            </MenuItem>
        );

        // To display the form
        var businessRuleForm = <div></div>

        // If a ruleTemplate has been selected
        if (this.state.selectedRuleTemplate.name !== "") {
            // To store property objects in a re-arranged format
            var propertiesArray = []
            for (var propertyKey in this.state.selectedRuleTemplate.properties) {
                // Push as an object,
                // which has the original object's Key & Value
                // denoted by new Keys : 'propertyName' & 'propertyObject'
                propertiesArray.push(
                    {
                        propertyName: propertyKey,
                        propertyObject: this.state.selectedRuleTemplate.properties[propertyKey.toString()]
                    }
                )
            }

            // Map available properties of the selected rule template
            const properties = propertiesArray.map((property) =>
                <Property
                    key={property.propertyName}
                    name={property.propertyName}
                    fieldName={property.propertyObject.fieldName}
                    description={property.propertyObject.description}
                    defaultValue={property.propertyObject.defaultValue}
                    options={property.propertyObject.options}
                    enteredValues={this.state.businessRuleEnteredValues}
                />
            );

            // To display the form
            businessRuleForm =
                <div>
                    <br/>
                    <TextField
                        id="businessRuleName"
                        name="businessRuleName"
                        label="Business Rule name"
                        placeholder="Please enter"
                        value={this.state.businessRuleEnteredValues.name}
                        required={true}
                        onChange={this.handleBusinessRuleNameChange}
                    />
                    <br/>
                    {properties}
                    <br/>
                    <Button raised color="primary"
                            onClick={(e) => prepareBusinessRule(e)} //todo: what about a public method
                    >Create</Button>
                </div>
        } else {
            businessRuleForm = <div></div>
        }

        return (
            <div>
                <Typography type="headline" component="h2">
                    {this.state.templateGroup.name}
                </Typography>
                <br/>
                <Typography component="p">
                    Select a template and fill the form to create the business rule
                </Typography>

                <br/>
                <FormControl>
                    <InputLabel htmlFor="ruleTemplate">RuleTemplate</InputLabel>
                    <Select
                        value={this.state.selectedRuleTemplate.name}
                        onChange={this.handleRuleTemplateSelected('selectedRuleTemplate')}
                        input={<Input id="ruleTemplate"/>}
                    >
                        {ruleTemplatesToDisplay}
                    </Select>
                    <FormHelperText>{this.state.selectedRuleTemplate.description}</FormHelperText>
                </FormControl>
                <br/>
                {businessRuleForm}
            </div>
        )
    }
}

/**
 * Represents Property, which is going to be shown as an input element
 */
class Property extends React.Component {
    // Handles onChange of Selection field
    handleSelectChange = name => event => {
        // Update / add entered value for this property to the state
        let state = this.state
        state['value'] = event.target.value
        state['enteredValues']['properties'][this.state.name] = event.target.value
        this.setState(state)
        businessRuleEnteredValues = this.state.enteredValues
    }

    constructor(props) {
        super(props);
        this.state = {
            name: props.name,
            fieldName: props.fieldName,
            description: props.description,
            defaultValue: props.defaultValue,
            value: props.defaultValue,
            type: props.type,
            options: props.options,
            // Has entered values for all the properties. Copied and referred by each property when rendering
            enteredValues: props.enteredValues
        }
        // Update default values as selected values in the state
        let state = this.state
        state['enteredValues']['properties'][this.state.name] = this.state.defaultValue
        this.state = state
        // this.state.enteredValues['properties'][this.state.name] = this.state.defaultValue
        businessRuleEnteredValues = this.state.enteredValues

        this.handleSelectChange = this.handleSelectChange.bind(this);
        this.handleTextChange = this.handleTextChange.bind(this);
    }

    // Handles onChange of Text Fields
    handleTextChange(event, value) {
        this.setState({
            value: value
        })
        // Update / add entered value for this property
        let state = this.state
        state['enteredValues'][this.state.name] = event.target.value
        this.setState(state)
        businessRuleEnteredValues['properties'] = this.state.enteredValues
    }

    // Renders each Property either as a TextField or Radio Group, with default values and elements as specified
    render() {
        if (this.state.options) {
            const options = this.state.options.map((option) => (
                <MenuItem key={option} name={option} value={option}>{option}</MenuItem>))
            return (
                <div>
                    <br/>
                    <FormControl>
                        <InputLabel htmlFor={this.state.name}>{this.state.fieldName}</InputLabel>
                        <Select
                            value={this.state.value}
                            onChange={this.handleSelectChange(this.state.name)}
                            input={<Input id={this.state.name}/>}
                        >
                            {options}
                        </Select>
                        <FormHelperText>{this.state.description}</FormHelperText>
                    </FormControl>
                    <br/>
                </div>
            );
        } else {
            return (
                <div>
                    <TextField
                        required
                        id={this.state.name}
                        name={this.state.name}
                        label={this.state.fieldName}
                        defaultValue={this.state.defaultValue}
                        helperText={this.state.description}
                        margin="normal"
                        onChange={this.handleTextChange}
                    />
                    <br/>
                </div>
            );
        }
    }
}

/* Start of Methods related to API calls **************************************/

/** [1]
 * Gets available Template Groups
 * todo: from API
 *
 * @returns {Array}
 */
function getTemplateGroups() {
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
                                        "Result topic", "description":
                                    "Name of the topic that you want to output the filtered results", "defaultValue":
                                    "resultTopic", "options":
                                    ["resultTopic", "SampleResultTopic2"]
                                }
                            ,
                            "sinkMapType":
                                {
                                    "fieldName":
                                        "Mapping type for data sink", "description":
                                    "Data from the output stream, is mapped in this format to the sink", "defaultValue":
                                    "xml", "options":
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
                                        "Result topic", "description":
                                    "Name of the topic that you want to output the filtered results", "defaultValue":
                                    "resultTopic", "options":
                                    ["resultTopic", "SampleResultTopic2"]
                                }
                            ,
                            "sinkMapType":
                                {
                                    "fieldName":
                                        "Mapping type for data sink", "description":
                                    "Data from the output stream, is mapped in this format to the sink", "defaultValue":
                                    "xml", "options":
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
function getRuleTemplates(templateGroupName) {
    // todo: remove hardcode ******************************
    var templateGroups = availableTemplateGroups
    for (let templateGroup of templateGroups) {
        if (templateGroup.name === templateGroupName) {
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
function getRuleTemplateProperties(templateGroupName, ruleTemplateName) {
    // todo: remove hardcode ******************************
    var ruleTemplates
    for (let templateGroup of availableTemplateGroups) {
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

// API [4] is the POST for CreateBusinessRule

/** [5]
 * Gets available BusinessRulesCreator
 * todo: from API
 *
 * @returns {[null,null]}
 */
function getBusinessRules() {
    // todo: remove hardcode *****************************
    var receivedBusinessRules = [
        {
            "uuid": "aaabbbcccddd",
            "name": "TemperatureLoggings",
            "templateGroupName": "SensorDataAnalysis1",
            "ruleTemplateName": "SensorLoggings1",
            "type": "template",
            "properties": {
                "inStream1": "myInputStream1",
                "property1": "sensorName",
                "outStream1": "myOutputStream1"
            }
        },
        {
            "uuid": "eeefffggghhh",
            "name": "HumidityAnalytics",
            "templateGroupName": "SensorDataAnalysis1",
            "ruleTemplateName": "SensorAnalytics1",
            "type": "template",
            "properties": {
                "inStream1": "myInputStream2",
                "property1": "sensorID",
                "property2": "humidity",
                "outStream1": "myOutputStream2",
                "outStream2": "myOutputStream1"
            }
        }
    ]

    return receivedBusinessRules
    // todo: *********************************************
    // todo: Get BusinessRulesCreator from API ******************
}

/** [6]
 * Gets the BusinessRule with the given UUID
 * todo: from API
 *
 * @param businessRuleUUID
 * @returns {null|null}
 */
function getBusinessRule(businessRuleUUID) {
    // todo: remove hardcode ******************************
    for (let businessRule of availableBusinessRules) {
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
function getTemplateGroup(templateGroupName) {
    // todo: remove hardcode ******************************
    for (let templateGroup of availableTemplateGroups) {
        if (templateGroup.name === templateGroupName) {
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
function getRuleTemplate(templateGroupName, ruleTemplateName) {
    // todo: remove hardcode ******************************
    var ruleTemplates
    for (let templateGroup of availableTemplateGroups) {
        if (templateGroup.name === templateGroupName) {
            var foundTemplateGroupObject = templateGroup
            ruleTemplates = templateGroup.ruleTemplates
            break
        }
    }
    for (let ruleTemplate of ruleTemplates) {
        if (ruleTemplate.name === ruleTemplateName) {
            var foundRuleTemplateObject = ruleTemplate
            // Assign belonging Template Group
            foundRuleTemplateObject['templateGroup'] = foundTemplateGroupObject
            return ruleTemplate
        }
    }
    // todo: **********************************************
    // todo: Return Rule Template from API
}

// End of Functions that have API calls unnecessarily //////////////////////////

/* End of Methods related to API calls ****************************************/

/**
 * Starts and runs Business Rules Creator
 */
function startBusinessRulesCreator() {
    console.log("[Started Business Rules Creator]")
    ReactDOM.render(<BusinessRulesCreator/>, document.getElementById("root"))
}

/**
 * Starts and runs Business Rules Creator from Template
 */
function runBusinessRuleCreatorFromTemplate() {
    console.log("[Started Create Business Rule from Template]")

    ReactDOM.render(
        <TemplateGroups
            templateGroups={availableTemplateGroups}
        />, document.getElementById("root"))
}

/**
 * Displays RuleTemplates belonging to the given Template Group
 * @param templateGroup
 */
function displayRuleTemplates(templateGroup) {
    ReactDOM.render(<RuleTemplates
        templateGroup={templateGroup}
        ruleTemplates={getRuleTemplates(templateGroup.name)}
    />, document.getElementById("root"))
}

/**
 * Prepares the Business Rule object to send the form element names & filled values as Key Value pairs, to the API
 *
 * @param filledValues
 */
function prepareBusinessRule() {
    createObjectForBusinessRuleCreation()
}

/**
 * Generates UUID for a given Business Rule name
 *
 * @param businessRuleName
 * @returns {string}
 */
function generateBusinessRuleUUID(businessRuleName) {
    return businessRuleName.toLowerCase().split(' ').join('-')
}

/* Roughly implemented functions *//////////////////////////////////////////////////////////////////////////////////////

/**
 * Gives the mapped properties, to send to the API to create Business Rule
 */
function createObjectForBusinessRuleCreation() {
    if ((!(businessRuleEnteredValues.name != null)) || businessRuleEnteredValues.name === "") {
        alert("Enter a name for the Business Rule")
    } else {
        var businessRule = businessRuleEnteredValues
        businessRule['type'] = 'template'
        console.log(businessRule)
        alert("Generated Business Rule object. Check console")
    }
}

/* End of Roughly implemented functions *///////////////////////////////////////////////////////////////////////////////

// Load from API and store
var availableTemplateGroups = getTemplateGroups()
var availableBusinessRules = getBusinessRules()

// todo: ((!)Q) look into this. Seems not a good practise. If so, solution?
// Properties given in the form, for Creating a Business Rule
var businessRuleEnteredValues = {
    'uuid': '',
    'name': '',
    'templateGroupUUID': '',
    'ruleTemplateUUID': '',
    'type': '',
    'properties': {}
}

// Start & Run BusinessRulesCreator();
startBusinessRulesCreator();