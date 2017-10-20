import React from 'react';
// import './index.css';
// Material-UI
import Typography from 'material-ui/Typography';
import TemplateGroup from './TemplateGroup';
import Header from "./Header";
import Grid from 'material-ui/Grid';
import BusinessRulesFunctions from "../utils/BusinessRulesFunctions";
import BusinessRulesConstants from "../utils/BusinessRulesConstants";

/**
 * Allows to select a Template Group, among Template Groups displayed as thumbnails
 */
const styles = {
    containerDiv: {
        maxWidth: 750
    },
    root: {
        flexGrow: 1,
    },
    control: {
        padding: 5,
    },
    spacing: '0'
}

class TemplateGroupSelector extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            mode: props.mode, // 'template' or 'scratch'
            templateGroups: props.templateGroups // Available Template Groups
        }
    }

    render() {
        let templateGroups

        // Business rule to be created from template
        if(this.state.mode === BusinessRulesConstants.BUSINESS_RULE_TYPE_TEMPLATE){
            templateGroups = this.state.templateGroups.map((templateGroup) =>
                <Grid item key={templateGroup.uuid}>
                    <TemplateGroup
                        key={templateGroup.uuid}
                        name={templateGroup.name}
                        uuid={templateGroup.uuid}
                        description={templateGroup.description}
                        onClick={(e) =>
                            BusinessRulesFunctions.loadBusinessRulesFromTemplateCreator(templateGroup.uuid)
                        }
                    />
                </Grid>
            )
        }else{
            // Business rule to be created from scratch
            templateGroups = this.state.templateGroups.map((templateGroup) =>
                <Grid item key={templateGroup.uuid}>
                    <TemplateGroup
                        key={templateGroup.uuid}
                        name={templateGroup.name}
                        uuid={templateGroup.uuid}
                        description={templateGroup.description}
                        onClick={(e) =>
                            BusinessRulesFunctions.loadBusinessRuleFromScratchCreator(templateGroup.uuid)
                        }
                    />
                </Grid>
            )
        }



        return (
            <div>
                <Header
                    title="Business Rule Manager"
                />
                <center>
                    <br/>
                    <Typography type="headline">
                        Select a Template Group
                    </Typography>
                    <br/>

                    <Grid container style={styles.root}>
                        <Grid item xs={12}>
                            <Grid container justify="center" spacing={Number(styles.spacing)}>
                                {templateGroups}
                            </Grid>
                        </Grid>
                    </Grid>

                </center>
            </div>
        )
    }
}

export default TemplateGroupSelector;
