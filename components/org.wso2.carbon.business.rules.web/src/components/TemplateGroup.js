import React from 'react';
// import './index.css';
// Material-UI
import Typography from 'material-ui/Typography';
import Card, {CardActions, CardContent} from 'material-ui/Card';
import Cake from 'material-ui-icons/Cake';
import NoteAdd from 'material-ui-icons/ArrowForward'
import Avatar from 'material-ui/Avatar';
import IconButton from 'material-ui/IconButton';

/**
 * Represent each Template Group, that is shown as a thumbnail
 */

// Styles related to this component
const styles = {
    card: {
        width: 345,
        height: 200,
        margin: 15
    },
    avatarButton: {
        color: 'white',
        width: 55,
        height: 55
    }
}

class TemplateGroup extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            name: props.name,
            uuid: props.uuid,
            uuid: props.uuid,
            description: props.description,
            onClick: props.onClick // Stored onClick action
        }
    }

    /**
     * Generates initials to be shown in the avatar
     */
    generateAvatarInitials() {
        var avatarInitials = "";
        // Contains words split by space
        var splitWords = this.state.name.split(" ")

        if (splitWords.length >= 2) {
            // Two letter initials
            avatarInitials += (splitWords[0][0] + splitWords[splitWords.length - 1][0])
        } else {
            // One letter initial
            avatarInitials += splitWords[0][0]
        }

        return avatarInitials
    }

    /**
     * Generates a style with backgroundColor for the given name
     *
     * @param name
     * @returns {{style: {backgroundColor: string}}}
     */
    generateAvatarColor(name) {
        var hash = 0;
        for (let i = 0; i < name.length; i++) {
            hash = name.charCodeAt(i) + ((hash << 5) - hash);
        }

        var c = (hash & 0x00FFFFFF)
            .toString(16)
            .toUpperCase();

        var color = "00000".substring(0, 6 - c.length) + c;
        // Put the random color to an object
        let style = {backgroundColor: '#' + color.toString()}
        return {style}
    }

    /**
     * Generates style with a random backgroundColor, from an array of given colors
     * @returns {{style: {backgroundColor: string}}}
     */
    generateAvatarColor() {
        let colors = [
            '#009688',
            '#03A9F4',
            '#EF6C00',
            '#4527A0',
            '#C51162',
        ];
        // Put the random color to an object
        let style = {
            backgroundColor: colors[Math.floor(Math.random() * colors.length)],
            width: 55,
            height: 55
        }
        return {style}
    }

    render() {
        return (
            <Card style={styles.card}>
                <CardContent>
                    <br/>
                    <IconButton aria-label="Create" onClick={this.state.onClick} style={styles.avatarButton}>
                        {/*<NoteAdd/>*/}
                        <Avatar style={this.generateAvatarColor()['style']} onClick={this.state.onClick}>
                            {this.generateAvatarInitials()}
                        </Avatar>
                    </IconButton>
                    <br/>
                    <br/>
                    <Typography type="headline" component="h2">
                        {this.state.name}
                    </Typography>
                    <Typography component="subheading" color="secondary">
                        {this.state.description}
                    </Typography>
                </CardContent>
            </Card>
        )
    }
}

export default TemplateGroup;
