import React from 'react';
import Typography from 'material-ui/Typography';
import Button from 'material-ui/Button';
// import './index.css';
// Material-UI
import IconButton from 'material-ui/IconButton';
import Cake from 'material-ui-icons/Cake';
import List from 'material-ui-icons/List';
import Create from 'material-ui-icons/Create';
import Grid from 'material-ui/Grid';
import Logo from '../images/wso2-logo.svg';
import BusinessRulesConstants from "../utils/BusinessRulesConstants";

/**
 * Represents a Create Button used in the Business Rule Creator, which will direct to
 * the specific create business rule page
 */

// Styles related to this component
const styles = {
    button: {
        padding: 50,
        margin: 25,
        height: 120,
        width: 120,
        color: 'white',
        backgroundColor: '#424242',
        borderRadius: 900,
    },
    div: {
        paddingLeft: 7,
        paddingRight: 7
    }
}

class CreateButton extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            mode: props.mode,
            text: props.text, // Text for the button
            onClick: props.onClick // Stores onClick function
        }
    }

    render() {
        let icon
        let text

        if(this.state.mode === BusinessRulesConstants.BUSINESS_RULE_TYPE_TEMPLATE){
            icon = <List/>
            text = 'From Template'
        }else{
            icon = <Create/>
            text = 'From the scratch'
        }

        return (
            <div style={styles.div}>
                <Button raised style={styles.button} onClick={this.state.onClick}>
                    {icon}
                </Button>
                <Typography type="title" color="inherit">
                    {text}
                </Typography>
            </div>
        )
    }
}

export default CreateButton;
