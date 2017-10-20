import React from 'react';
// import './index.css';
// Material-UI
import Typography from 'material-ui/Typography';
import CreateButton from "./CreateButton";
import BusinessRulesFunctions from "../utils/BusinessRulesFunctions";
import Grid from 'material-ui/Grid';
import Header from "./Header";
import BusinessRulesConstants from "../utils/BusinessRulesConstants";

/**
 * Allows to create a Business Rule either from scratch or from a Template
 */
const styles = {
    root: {
        flexGrow: 1,
    },
    control: {
        padding: 5,
    },
    spacing: '40'
}

class BusinessRuleCreator extends React.Component {
    render() {
        return (
            <div>
                <Header
                    title="Business Rule Manager"
                />
                <br/>
                <center>
                    <Typography type="headline">
                        Let's create a business rule
                    </Typography>
                    <br/>

                    <Grid container style={styles.root}>
                        <Grid item xs={12}>
                            <Grid container justify="center" spacing={Number(styles.spacing)}>
                                <Grid item>
                                    <CreateButton
                                        onClick={(e) => BusinessRulesFunctions.loadTemplateGroupSelector(
                                            BusinessRulesConstants.BUSINESS_RULE_TYPE_TEMPLATE)}
                                        mode={BusinessRulesConstants.BUSINESS_RULE_TYPE_TEMPLATE}
                                        text='From Template'
                                    />
                                </Grid>
                                <Grid item>
                                    <CreateButton
                                        onClick={(e) => BusinessRulesFunctions.loadTemplateGroupSelector(
                                            BusinessRulesConstants.BUSINESS_RULE_TYPE_SCRATCH)}
                                        mode={BusinessRulesConstants.BUSINESS_RULE_TYPE_SCRATCH}
                                        text='From The Scratch'
                                    />
                                </Grid>
                            </Grid>
                        </Grid>
                    </Grid>

                </center>
            </div>
        );
    }
}

export default BusinessRuleCreator;
