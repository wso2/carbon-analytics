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
import TemplateGroupSelector from "../components/TemplateGroupSelector";

/**
 * Contains messages / errors to be displayed
 */
const BusinessRulesMessages = {
    // Business Rule form (common)
    SELECT_RULE_TEMPLATE: 'Please select a rule template',
    BUSINESS_RULE_NAME_FIELD_NAME: 'Business rule name',
    BUSINESS_RULE_NAME_FIELD_DESCRIPTION: 'Please enter',
    // Errors
    ALL_FIELDS_REQUIRED_ERROR_TITLE: 'Error submitting your form',
    ALL_FIELDS_REQUIRED_ERROR_CONTENT: 'Please fill in all the required values',
    ALL_FIELDS_REQUIRED_ERROR_PRIMARY_BUTTON: 'OK',

    // Business Rule from scratch form
    // Filter component
    RULE_LOGIC_HELPER_TEXT: "Enter the Rule Logic, referring filter rule numbers. Eg: (1 OR 2) AND (NOT(3))",
    RULE_LOGIC_WARNING: "Rule logic contains invalid number(s) for filter rules",
    // Output component
    MAPPING_NOT_AVAILABLE: 'Please select both input & output rule templates',

    // Error codes related to save & deployment
    BUSINESS_RULE_SAVE_SUCCESSFUL : 200,
    BUSINESS_RULE_SAVE_AND_DEPLOYMENT_SUCCESS: 201,
    BUSINESS_RULE_SAVE_SUCCESSFUL_DEPLOYMENT_FAILURE: 501,
    BUSINESS_RULE_SAVE_AND_DEPLOYMENT_FAILURE: 500,

    // Messages related to save & deployment
    BUSINESS_RULE_SAVE_SUCCESSFUL_MESSAGE : 'Successfully saved business rule',
    BUSINESS_RULE_SAVE_AND_DEPLOYMENT_SUCCESS_MESSAGE: 'Business rule deployed successfully',
    BUSINESS_RULE_SAVE_SUCCESSFUL_DEPLOYMENT_FAILURE_MESSAGE: 'Saved, and failed to deploy business rule',
    BUSINESS_RULE_SAVE_AND_DEPLOYMENT_FAILURE_MESSAGE: 'Failed to save and deploy business rule',

    // Deleting business rule
    BUSINESS_RULE_DELETION_SUCCESSFUL: 202,
    BUSINESS_RULE_DELETION_FAILURE: 502,

    // Re-deploying business rule
    BUSINESS_RULE_REDEPLOY_SUCCESSFUL: 203,
    BUSINESS_RULE_REDEPLOY_FAILURE: 503,


    BUSINESS_RULE_DELETION_SUCCESSFUL_MESSAGE: 'Successfully deleted business rule',
    BUSINESS_RULE_DELETION_FAILURE_MESSAGE: 'Failed to delete business rule',

    BUSINESS_RULE_DELETION_CONFIRMATION_TITLE: 'Confirm delete',
    BUSINESS_RUL_DELETION_CONFIRMATION_CONTENT: 'Do you really want to delete this business rule?',

    // Common errors
    CONNECTION_FAILURE: 'There was an error while connecting to the server', //todo: Cannot connect to the server -
    // todo the heading (Connection Error)
    ERROR_PROCESSING_YOUR_REQUEST: 'Error processing your request',
    API_FAILURE: 'There was some problem in sending data through the API',

    // Error titles
    ERROR_500: 'Server error' // todo: no need for them, since dialog box is only shown during deletion & non filled
}

export default BusinessRulesMessages;
