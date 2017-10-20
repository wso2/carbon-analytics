import React from 'react';
import ReactDOM from 'react-dom';
// import './index.css';
// Material-UI
import Property from './Property';
import Button from 'material-ui/Button';
import TextField from 'material-ui/TextField';
import Dialog, {DialogActions, DialogContent, DialogContentText, DialogTitle,} from 'material-ui/Dialog';
import BusinessRulesFunctions from "../utils/BusinessRulesFunctions";
import BusinessRulesConstants from "../utils/BusinessRulesConstants";
import BusinessRulesAPIs from "../utils/BusinessRulesAPIs";
import Header from "./Header";
import Typography from 'material-ui/Typography';
import {FormControl, FormHelperText} from 'material-ui/Form';
import Select from 'material-ui/Select';
import Input, {InputLabel} from 'material-ui/Input';
import BusinessRulesMessageStringConstants from "../utils/BusinessRulesMessageStringConstants";
import {MenuItem} from 'material-ui/Menu';
import ShowProgressComponent from "./ShowProgressComponent";
import Paper from 'material-ui/Paper';
import Grid from 'material-ui/Grid';

/**
 * Represents a form, shown to create Business Rules from template
 */

// Button Style
const styles = {
    button: {
        backgroundColor: '#EF6C00',
        color: 'white',
        marginRight: 10
    },
    secondaryButton: {
        marginRight: 10
    },
    container: {
        align: 'center',
        maxWidth: 800
    },
    formRoot: {
        flexGrow: 1,
        marginTop: 30,
    },
    formPaper: {
        padding: 50,
        maxWidth: 600,
    }
}

class BusinessRuleFromTemplateForm extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            formMode: props.formMode, // 'create', 'edit' or 'view'
            businessRuleName: props.businessRuleName,
            businessRuleUUID: props.businessRuleUUID,
            selectedTemplateGroup: props.selectedTemplateGroup,
            templateRuleTemplates: props.templateRuleTemplates, // Rule templates of type 'template'
            selectedRuleTemplate: props.selectedRuleTemplate,
            // To store values given for properties displayed in the form
            businessRuleProperties: props.businessRuleProperties,

            // For displaying messages / errors
            displayDialog: false,
            dialogTitle: '',
            dialogContentText: '',
            dialogPrimaryButtonText: ''
        }

        // Assign default values for properties in the state, for 'create' mode
        if (this.state.formMode === BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_CREATE) {
            let state = this.state
            state['businessRuleName'] = ''
            state['businessRuleUUID'] = ''
            state['businessRuleProperties'] = {}
            this.state = state
        } else {
            // Assign entered values for properties in the state, for 'edit' or todo : 'view' modes
            let state = this.state
            state['businessRuleProperties'] = props.businessRuleProperties
            this.state = state
        }
    }

    /**
     * Updates the selected Rule Template in the state,
     * when Rule Template is selected from the list
     *
     * @param templateGroupUUID
     * @param event
     */
    handleRuleTemplateSelected(templateGroupUUID, event) {
        let state = this.state
        let that = this
        // Get selected rule template & update in the state
        let selectedRuleTemplatePromise = BusinessRulesFunctions.getRuleTemplate(templateGroupUUID, event.target.value)
        selectedRuleTemplatePromise.then(function (selectedRuleTemplateResponse) {
            state['selectedRuleTemplate'] = selectedRuleTemplateResponse.data

            // Set properties in the state as default value
            for (let propertyKey in state.selectedRuleTemplate.properties) {
                state['businessRuleProperties'][propertyKey] =
                    that.state.selectedRuleTemplate.properties[propertyKey.toString()]['defaultValue']
            }

            that.setState(state)
        })
    }

    /**
     * Updates the value of the respective property in the state, when a property is changed
     *
     * @param property Property name
     * @param value Entered value for the property
     */
    handleValueChange(property, value) {
        let state = this.state
        state['businessRuleProperties'][property] = value
        this.setState(state)
    }

    /**
     * Updates the name of business rule when it is changed, in the state
     *
     * @param event
     */
    handleBusinessRuleNameChange(event) {
        let state = this.state
        state['businessRuleName'] = event.target.value
        state['businessRuleUUID'] = BusinessRulesFunctions.generateBusinessRuleUUID(event.target.value)
        this.setState(state)
    }

    /**
     * Creates a business rule object from the values entered in the form, and sends to the API,
     * to save only if given deployStatus is false, otherwise, also to deploy
     *
     * @param deployStatus
     */
    createBusinessRule(deployStatus) { //todo: deployStatus flag : check it
        // Validate whether all required fields are filled or not
        if (this.isBusinessRuleValid()) {
            // Prepare the business rule object
            var businessRuleObject = {}
            businessRuleObject['name'] = this.state.businessRuleName
            businessRuleObject['uuid'] = this.state.businessRuleUUID
            businessRuleObject['type'] = BusinessRulesConstants.BUSINESS_RULE_TYPE_TEMPLATE
            businessRuleObject['templateGroupUUID'] = this.state.selectedTemplateGroup.uuid
            businessRuleObject['ruleTemplateUUID'] = this.state.selectedRuleTemplate.uuid
            businessRuleObject['properties'] = this.state.businessRuleProperties

            // Send prepared business rule object to API
            let apis = new BusinessRulesAPIs(BusinessRulesConstants.BASE_URL)
            apis.createBusinessRule(JSON.stringify(businessRuleObject), deployStatus.toString()).then(function (response) {
                BusinessRulesFunctions.loadBusinessRuleModifier(true, response.status);
            }).catch(function (error) {
                ReactDOM.render(
                    <ShowProgressComponent
                        error={BusinessRulesMessageStringConstants.API_FAILURE}/>, document.getElementById('root'))
            })
            // Show 'please wait'
            ReactDOM.render(<ShowProgressComponent/>, document.getElementById('root'))

        } else {
            // Display error
            this.setDialog(BusinessRulesMessageStringConstants.ALL_FIELDS_REQUIRED_ERROR_TITLE,
                BusinessRulesMessageStringConstants.ALL_FIELDS_REQUIRED_ERROR_CONTENT,
                BusinessRulesMessageStringConstants.ALL_FIELDS_REQUIRED_ERROR_PRIMARY_BUTTON)
        }
    }


    /**
     * Re-creates a new business rule object for the business rule with the existing UUID, and sends to the API,
     * to save only if given deployStatus is false, otherwise, also to deploy
     *
     * @param deployStatus
     */
    updateBusinessRule(deployStatus) {
        // Validate whether all required fields are filled or not
        if (this.isBusinessRuleValid()) {
            // Prepare the business rule object
            var businessRuleObject = {}
            businessRuleObject['name'] = this.state.businessRuleName
            businessRuleObject['uuid'] = this.state.businessRuleUUID
            businessRuleObject['type'] = BusinessRulesConstants.BUSINESS_RULE_TYPE_TEMPLATE
            businessRuleObject['templateGroupUUID'] = this.state.selectedTemplateGroup.uuid
            businessRuleObject['ruleTemplateUUID'] = this.state.selectedRuleTemplate.uuid
            businessRuleObject['properties'] = this.state.businessRuleProperties

            // Send prepared business rule object to API
            let apis = new BusinessRulesAPIs(BusinessRulesConstants.BASE_URL)
            // Deployment true or false
            apis.updateBusinessRule(businessRuleObject['uuid'], JSON.stringify(businessRuleObject), deployStatus).then(function (response) {
                BusinessRulesFunctions.loadBusinessRuleModifier(true, response.status.toString());
            })
        } else {
            // Display error
            this.setDialog(BusinessRulesMessageStringConstants.ALL_FIELDS_REQUIRED_ERROR_TITLE,
                BusinessRulesMessageStringConstants.ALL_FIELDS_REQUIRED_ERROR_CONTENT,
                BusinessRulesMessageStringConstants.ALL_FIELDS_REQUIRED_ERROR_PRIMARY_BUTTON)
        }
    }

    /**
     * Checks whether the business rule object in the state is a valid one or not
     */
    isBusinessRuleValid() {
        if (this.state.businessRuleName === '' || BusinessRulesFunctions.isEmpty(this.state.businessRuleName)) {
            return false
        }
        if (this.state.businessRuleUUID === '' || BusinessRulesFunctions.isEmpty(this.state.businessRuleUUID)) {
            return false
        }
        if (this.state.selectedTemplateGroup.uuid === '' ||
            BusinessRulesFunctions.isEmpty(this.state.selectedTemplateGroup.uuid)) {
            return false
        }
        if (this.state.selectedRuleTemplate.uuid === '' ||
            BusinessRulesFunctions.isEmpty(this.state.selectedRuleTemplate)) {
            return false
        }
        for (let propertyKey in this.state.businessRuleProperties) {
            if (this.state.businessRuleProperties[propertyKey.toString()] === '') {
                // No need for isEmpty check, since default values are assigned at the beginning, and '' if erased
                return false
            }
        }
        return true
    }

    /**
     * Sets members of the state with given values, which will be used to show content in the dialog
     *
     * @param title
     * @param contentText
     * @param primaryButtonText
     */
    setDialog(title, contentText, primaryButtonText) {
        let state = this.state
        state['displayDialog'] = true
        state['dialogTitle'] = title
        state['dialogContentText'] = contentText
        state['dialogPrimaryButtonText'] = primaryButtonText
        this.setState(state)
    }

    /**
     * Closes the dialog
     */
    dismissDialog() {
        this.setState({displayDialog: false})
    }

    /**
     * Shows the dialog, with displaying the contents available from the state
     *
     * @returns {XML}
     */
    showDialog() {
        return (
            <Dialog open={this.state.displayDialog}
                    onRequestClose={(e) => this.dismissDialog()}
            >
                <DialogTitle>{this.state.dialogTitle}</DialogTitle>
                <DialogContent>
                    <DialogContentText>
                        {this.state.dialogContentText}
                    </DialogContentText>
                </DialogContent>
                <DialogActions>
                    <Button style={styles.secondaryButton}
                            onClick={(e) => this.dismissDialog()}
                            color="default">
                        {this.state.dialogPrimaryButtonText}
                    </Button>
                </DialogActions>
            </Dialog>
        )
    }

    render() {
        // To display available rule templates of type 'template' as drop down
        let templateRuleTemplatesToDisplay = this.state.templateRuleTemplates.map((ruleTemplate) =>
            <MenuItem key={ruleTemplate.uuid} value={ruleTemplate.uuid}>
                {ruleTemplate.name}
            </MenuItem>
        )
        let ruleTemplatesSelectionToDisplay =
            <FormControl
                disabled={this.state.formMode !== BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_CREATE}>
                <InputLabel htmlFor="ruleTemplate">RuleTemplate</InputLabel>
                <Select
                    value={(!BusinessRulesFunctions.isEmpty(this.state.selectedRuleTemplate)) ?
                        (this.state.selectedRuleTemplate.uuid) :
                        ('')
                    }
                    onChange={(e) => this.handleRuleTemplateSelected(this.state.selectedTemplateGroup.uuid, e)}
                    input={<Input id="ruleTemplate"/>}
                >
                    {templateRuleTemplatesToDisplay}
                </Select>
                <FormHelperText>
                    {(!BusinessRulesFunctions.isEmpty(this.state.selectedRuleTemplate)) ?
                        (this.state.selectedRuleTemplate.description) :
                        (BusinessRulesMessageStringConstants.SELECT_RULE_TEMPLATE)
                    }
                </FormHelperText>
            </FormControl>

        // Business Rule Name text field
        let businessRuleNameTextField

        // To store properties in re-arranged format, in order to generate fields
        let properties = []
        // To display properties as input fields
        let propertiesToDisplay

        // If a rule template has been selected
        if (this.state.selectedRuleTemplate) {
            // To display business rule name field
            businessRuleNameTextField =
                <TextField
                    id="businessRuleName"
                    name="businessRuleName"
                    label={BusinessRulesMessageStringConstants.BUSINESS_RULE_NAME_FIELD_NAME}
                    placeholder={BusinessRulesMessageStringConstants.BUSINESS_RULE_NAME_FIELD_DESCRIPTION}
                    value={this.state.businessRuleName}
                    required={true} //todo: no need of True
                    onChange={(e) => this.handleBusinessRuleNameChange(e)}
                    disabled={this.state.formMode !== BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_CREATE}
                />

            // Push each property key+ object in the rule template, as an object
            // which has the original object's Key & Value
            // denoted by new Keys : 'propertyName' & 'propertyObject' todo: put reason
            for (let propertyKey in this.state.selectedRuleTemplate.properties) {
                // Modify default value, as the entered property value,
                // in order to display initially in the form
                properties.push({
                    propertyName: propertyKey, //todo: hasOwnProperty check
                    propertyObject: this.state.selectedRuleTemplate.properties[propertyKey.toString()]
                })
            } //todo: try to do it in a more readable way

            // To display each property as an input field
            propertiesToDisplay = properties.map((property) =>
                <Property
                    key={property.propertyName}
                    name={property.propertyName}
                    fieldName={property.propertyObject.fieldName}
                    description={property.propertyObject.description}
                    value={this.state['businessRuleProperties'][property.propertyName]}
                    options={property.propertyObject.options}
                    onValueChange={(e) => this.handleValueChange(property.propertyName, e)}
                    errorState={this.state['businessRuleProperties'][property.propertyName] == ''}
                    disabledState={this.state.formMode === BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_VIEW}
                />
            )
        }

        // Save, and Save & Deploy buttons
        let submitButtons

        // If form should be displayed for Creating a business rule
        if (this.state.formMode === BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_CREATE) {
            if (!BusinessRulesFunctions.isEmpty(this.state.selectedRuleTemplate)) {
                submitButtons =
                    <div>
                        <Button raised color="default" style={styles.secondaryButton}
                                onClick={(e) => this.createBusinessRule(false)}>
                            Save
                        </Button>
                        <Button raised color="primary" style={styles.button}
                                onClick={(e) => this.createBusinessRule(true)}>
                            Save & Deploy
                        </Button>
                    </div>
            }
        } else if (this.state.formMode === BusinessRulesConstants.BUSINESS_RULE_FORM_MODE_EDIT) {
            // If form should be displayed for Editing a business rule
            submitButtons =
                <div>
                    <Button raised color="default" style={styles.secondaryButton}
                            onClick={(e) => this.updateBusinessRule(false)}>
                        Save
                    </Button>
                    <Button raised color="primary" style={styles.button}
                            onClick={(e) => this.updateBusinessRule(true)}>
                        Save & Deploy
                    </Button>
                </div>
        }

        return (
            <div>
                {this.showDialog()}
                <Header
                    title="Business Rule Manager"
                />
                <br/>
                <div style={styles.formRoot}>
                    <center>
                    <Grid container spacing={24} justify='center'>
                        <Grid item xs>
                            <Paper style={styles.formPaper}>
                                <Typography type="headline">
                                    {this.state.selectedTemplateGroup.name}
                                </Typography>
                                <Typography type="subheading">
                                    {this.state.selectedTemplateGroup.description}
                                </Typography>
                                <br/>
                                {ruleTemplatesSelectionToDisplay}
                                <br/>
                                <br/>
                                {businessRuleNameTextField}
                                {propertiesToDisplay}
                                <br/>
                                {submitButtons}
                            </Paper>
                        </Grid>
                    </Grid>
                    </center>
                </div>
            </div>
        )
    }
}

export default BusinessRuleFromTemplateForm;
