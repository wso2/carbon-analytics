import React from 'react';
import ReactDOM from 'react-dom';
import {CircularProgress} from 'material-ui/Progress';
import Typography from "material-ui";
import Header from "./Header";
import Paper from 'material-ui/Paper';
import BusinessRulesMessages from "../utils/BusinessRulesMessages";

/**
 * Shows either the waiting progress or the error, in between processes
 */

// Styles related to this component
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

class ProgressDisplay extends React.Component {

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
                                    {BusinessRulesMessages.ERROR_PROCESSING_YOUR_REQUEST}
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

export default ProgressDisplay;