import React from 'react';
import ReactDOM from 'react-dom';
import {CircularProgress} from 'material-ui/Progress';
import {Typography} from "material-ui";
import Header from "./Header";
import Paper from 'material-ui/Paper';
import BusinessRulesMessageStringConstants from "../utils/BusinessRulesMessageStringConstants";

/**
 * Shows circular progress
 */

const styles = {
    progress: {
        color: '#EF6C00',
    },
    paper: {
        maxWidth: 400,
        paddingTop: 30,
        paddingBottom: 30
    },
}

class ShowProgressComponent extends React.Component {

    render() {
        return (
            <div>
                <center>
                    <Header
                        title="Business Rule Manager"
                    />
                    <br/>
                    <br/>
                    <div>
                        <Paper style={styles.paper}>
                            <br/>
                            {(this.props.error) ?
                                (<div>
                                    <Typography type="headline">
                                    {BusinessRulesMessageStringConstants.ERROR_PROCESSING_YOUR_REQUEST}
                                </Typography>
                                    <Typography type="body2">
                                        {this.props.error}
                                    </Typography>
                                </div>) :
                                (<div>
                                    <CircularProgress size={50}/>
                                    <Typography type="subheading">
                                        Please wait
                                    </Typography>
                                </div>)
                            }

                            <br/>
                        </Paper>
                    </div>
                    <br/>
                </center>
            </div>
        )

    }
}

export default ShowProgressComponent;