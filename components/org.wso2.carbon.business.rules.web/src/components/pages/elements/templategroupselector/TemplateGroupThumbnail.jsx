/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

import React, { Component } from 'react';
import PropTypes from 'prop-types';
// Material UI Components
import Typography from 'material-ui/Typography';
import Card, { CardContent } from 'material-ui/Card';
import Avatar from 'material-ui/Avatar';
// CSS
import '../../../../index.css';

/**
 * Styles related to this component
 */
const styles = {
    card: {
        width: 345,
        height: 200,
        margin: 15,
    },
    avatarButton: {
        color: 'white',
        width: 55,
        height: 55,
    },
};

/**
 * Represents Thumbnail of a Template Group
 */
export default class TemplateGroupThumbnail extends Component {
    /**
     * Generates initials for the avatar, with the given template group name
     * @param {string} templateGroupName        Name of the template group
     * @returns {string}                        Initials for the avatar
     */
    generateAvatarInitials(templateGroupName) {
        let avatarInitials = '';
        // Contains words split by space
        const splitWords = templateGroupName.split(' ');

        if (splitWords.length >= 2) {
            // Two letter initials
            avatarInitials += (splitWords[0][0] + splitWords[splitWords.length - 1][0]);
        } else {
            // One letter initial
            avatarInitials += splitWords[0][0];
        }

        return avatarInitials;
    }

    /**
     * Generates a style with the given name, for the avatar
     * @param {string} name         Name of the template group
     * @returns {Object}            Style for the avatar
     */
    generateAvatarStyle(name) {
        const colors = [
            '#673AB7',
            '#3F51B5',
            '#2196F3',
            '#009688',
            '#4CAF50',
            '#F44336',
            '#E91E63',
        ];
        let charCodeSum = 0;
        for (let i = 0; i < name.length; i++) {
            charCodeSum += name.charCodeAt(i);
        }
        return {
            backgroundColor: colors[charCodeSum % colors.length],
            width: 55,
            height: 55,
        };
    }

    render() {
        return (
            <Card style={styles.card}>
                <CardContent>
                    <br />
                    <Avatar style={this.generateAvatarStyle(this.props.name)}>
                        {this.generateAvatarInitials(this.props.name)}
                    </Avatar>
                    <br />
                    <Typography type="headline" component="h2">
                        {this.props.name}
                    </Typography>
                    <Typography component="subheading" color="secondary">
                        {this.props.description || ''}
                    </Typography>
                </CardContent>
            </Card>
        );
    }
}

TemplateGroupThumbnail.propTypes = {
    name: PropTypes.string.isRequired,
    description: PropTypes.string,
};

TemplateGroupThumbnail.defaultProps = {
    description: '',
};
