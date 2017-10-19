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
 * Has values for all the constants related to Business Rules web app
 */
const BusinessRulesConstants = { //todo: move constants to their belonging classes
    // Rule Template types
    RULE_TEMPLATE_TYPE_TEMPLATE: "template",
    RULE_TEMPLATE_TYPE_INPUT: "input",
    RULE_TEMPLATE_TYPE_OUTPUT: "output",

    // Mode of Business Rule form
    BUSINESS_RULE_FORM_MODE_CREATE: "create",
    BUSINESS_RULE_FORM_MODE_EDIT: "edit",
    BUSINESS_RULE_FORM_MODE_VIEW: "view",

    // Business Rule types
    BUSINESS_RULE_TYPE_TEMPLATE: "template",
    BUSINESS_RULE_TYPE_SCRATCH: "scratch",

    // Business Rule from scratch property types
    BUSINESS_RULE_FROM_SCRATCH_PROPERTY_TYPE_INPUT: "inputData",
    BUSINESS_RULE_FROM_SCRATCH_PROPERTY_TYPE_OUTPUT: "outputData",
    BUSINESS_RULE_FROM_SCRATCH_PROPERTY_TYPE_RULE_COMPONENTS: "ruleComponents",
    BUSINESS_RULE_FROM_SCRATCH_RULE_COMPONENT_PROPERTY_TYPE_FILTER_RULES: "filterRules",
    BUSINESS_RULE_FROM_SCRATCH_RULE_COMPONENT_PROPERTY_TYPE_RULE_LOGIC: "ruleLogic",
    BUSINESS_RULE_FROM_SCRATCH_RULE_PROPERTY_TYPE_OUTPUT_MAPPINGS: "outputMappings",

    // Business Rule deployment statuses
    BUSINESS_RULE_DEPLOYMENT_STATUS_DEPLOYED: "deployed",
    BUSINESS_RULE_DEPLOYMENT_STATUS_NOT_DEPLOYED: "notDeployed",

    // Business Rule Filter Rule operators
    BUSINESS_RULE_FILTER_RULE_OPERATORS: ['<','<=','>','>=','==','!='],

    // Business Rule deployment statuses
    BUSINESS_RULE_STATUS_DEPLOYED: 3,
    BUSINESS_RULE_STATUS_DEPLOYMENT_FAILED: 1, // Tried to save & deploy, but only save was successful
    BUSINESS_RULE_STATUS_NOT_DEPLOYED: 0, // Tried only to save, and was successful todo: check number

    BUSINESS_RULE_STATUS_DEPLOYED_STRING: 'Deployed',
    BUSINESS_RULE_STATUS_NOT_DEPLOYED_STRING: 'Not Deployed',
    BUSINESS_RULE_STATUS_DEPLOYMENT_FAILED_STRING: 'Deployment Failed',




    // URL for APIs
    BASE_URL: window.location.origin
}

export default BusinessRulesConstants;
