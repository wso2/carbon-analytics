/**
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
define(function (require) {

    var TokenIterator = require("ace/token_iterator").TokenIterator;

    var SiddhiFormatter = {};

    SiddhiFormatter.newLinesRules = [
        {
            type: 'comment.block.start',
            breakBefore: true,
            breakAfter: true
        },
        {
            type: 'comment.block.end',
            breakBefore: false,
            breakAfter: true
        },
        {
            type: 'comment.block',
            breakBefore: false,
            breakAfter: false
        },
        {
            type: 'annotation.plan.start',
            breakBefore: true,
            breakAfter: false
        },
        {
            type: 'annotation.common.start',
            checkIsInsidePartition: true,
            breakBefore: true,
            breakAfter: false
        },
        {
            type: 'paren.lparen',
            value: "{",
            breakBefore: false,
            breakAfter: true
        },
        {
            type: 'paren.rparen',
            value: "}",
            breakBefore: true,
            breakAfter: false
        },
        {
            type: 'keyword.other',
            value: ";",
            breakBefore: false,
            breakAfter: true
        },
        {
            type: 'keyword.other',
            value: ");",
            breakBefore: false,
            breakAfter: true
        },
        {
            type: 'keyword',
            value: "define",
            breakBefore: true,
            breakAfter: false
        },
        {
            type: 'keyword',
            value: "from",
            checkIsInsidePartition: true,
            breakBefore: true,
            breakAfter: false
        },
        {
            type: 'keyword',
            value: "on",
            breakBefore: true,
            checkIsInsidePartition: true,
            addTabSpace: true,
            breakAfter: false
        },
        {
            type: 'keyword',
            value: "select",
            checkIsInsidePartition: true,
            breakBefore: true,
            breakAfter: false
        },
        {
            type: 'keyword',
            value: "group",
            breakBefore: true,
            checkIsInsidePartition: true,
            addTabSpace: true,
            breakAfter: false
        },
        {
            type: 'keyword',
            value: "having",
            breakBefore: true,
            checkIsInsidePartition: true,
            addTabSpace: true,
            breakAfter: false
        },
        {
            type: 'keyword',
            value: "order",
            breakBefore: true,
            checkIsInsidePartition: true,
            addTabSpace: true,
            breakAfter: false
        },
        {
            type: 'keyword',
            value: "limit",
            breakBefore: true,
            checkIsInsidePartition: true,
            addTabSpace: true,
            breakAfter: false
        },
        {
            type: 'keyword',
            value: "insert",
            checkIsInsidePartition: true,
            breakBefore: true,
            breakAfter: false
        },
        {
            type: 'keyword',
            value: "delete",
            checkIsInsidePartition: true,
            breakBefore: true,
            breakAfter: false
        },
        {
            type: 'keyword',
            value: "update",
            checkIsInsidePartition: true,
            breakBefore: true,
            breakAfter: false
        },
        {
            type: 'keyword',
            value: "return",
            checkIsInsidePartition: true,
            avoidBreakBeforeUpon: {
                lastNonWhiteSpaceToken: {
                    type: 'keyword.other',
                    value: "]"
                }
            },
            breakBefore: true,
            breakAfter: false
        },
        {
            type: 'keyword',
            value: "aggregate",
            breakBefore: true,
            addTabSpace: true,
            breakAfter: false
        },
        {
            type: 'keyword',
            value: "partition",
            breakBefore: true,
            breakAfter: false
        },
        {
            type: 'keyword',
            value: "begin",
            breakBefore: true,
            breakAfter: false
        },
        {
            type: 'keyword',
            value: "end",
            breakBefore: true,
            breakAfter: false
        }
    ];

    SiddhiFormatter.spaceRules = [
        {
            type: 'keyword.other',
            prepend: false,
            append: false
        },
        {
            type: 'keyword.other',
            value: ",",
            prepend: false,
            append: true
        },
        {
            type: 'keyword.operator',
            prepend: true,
            append: true
        },
        {
            type: 'text',
            prepend: false,
            append: false
        }
    ];

    SiddhiFormatter.beautify = function (session, start, end) {
        if (_.isNil(start)) {
            start = 0;
        }

        if (_.isNil(end)) {
            end = 0;
        }

        var iterator = new TokenIterator(session, start, end);
        this.session = session;
        var code = this.format(iterator);
        session.doc.setValue(code);
    };

    SiddhiFormatter.format = function (iterator) {

        var token = iterator.getCurrentToken(),
            newLinesRules = this.newLinesRules,
            spaceRules = this.spaceRules,
            code = '',
            lastToken = {},
            lastNonWhiteSpaceToken = {},
            nextToken = {},
            value = '',
            space = ' ',
            newLine = '\n',
            tab = '\t',
            isCodeEndingWithANewLine = false,
            isValueContainsANewLine = false,
            isInsideAPartition = false,
            lastNewLineCharacterIsAtRow = 0,
            currentTokenRowNumber = 0,
            commonAnnotationLevel = 0,
            noOfTabsInBeginOfNewLineInAnnotation = 0;

        while (token !== null) {

            // user added new line handling starts here
            var tokenRow = iterator.getCurrentTokenRow();
            if (tokenRow > currentTokenRowNumber) {
                var rowDifference = tokenRow - currentTokenRowNumber;
                if (rowDifference > 1) {
                    // adding only one empty line
                    if (code !== "") { // don't add newlines in the beginning of the file
                        if (!_.endsWith(code, newLine)) {
                            code += newLine + newLine;
                            isCodeEndingWithANewLine = true;
                        } else {
                            code += newLine;
                            isCodeEndingWithANewLine = true;
                        }
                    }
                } else {
                    if (code !== "") { // don't add newlines in the beginning of the file
                        code += newLine;
                        isCodeEndingWithANewLine = true;
                    }
                }
                currentTokenRowNumber = tokenRow;
                lastNewLineCharacterIsAtRow = tokenRow - 1;

                // adding tabs spaces for annotations which user entered a piece of the annotation in the second line
                // ex: @sink(newLine type = 'log') will be converted to @sink(newLine tabSpace type = 'log')
                if (commonAnnotationLevel > 0) {
                    noOfTabsInBeginOfNewLineInAnnotation = 0;
                    for (var i = 0; i < commonAnnotationLevel; i++) {
                        code += tab;
                        noOfTabsInBeginOfNewLineInAnnotation += 1;
                    }
                }
            } // user added new line handling ends here

            if (!token) {
                token = iterator.stepForward();
                continue;
            }

            nextToken = iterator.stepForward();
            value = token.value;

            // trim unnecessary white spaces to a single space other than new lines
            if (token.type === 'whitespace') {
                // space inside an annotation will be removed.
                // ex: @sink(newLine tabSpace space type = 'log') will be converted to @sink(newLine tabSpace type = 'log')
                // All the other whitespaces in the line(not in an annotation line) will be converted to space
                token.value = token.value.trim();
                value = commonAnnotationLevel > 0 && _.endsWith(code, tab) ? '' : space;

            }

            // check whether inside an annotation element
            if (token.type === 'annotation.common.start') {
                commonAnnotationLevel += 1;
            }
            // checking the end of an annotation
            if (commonAnnotationLevel > 0 && (token.type === 'keyword.other.annotation.mid.end'
                || token.type === 'keyword.other.annotation.full.end'
                || token.type === 'keyword.other.rparen')) {
                var tokenValue = token.value;
                var regex = new RegExp("\\)", "gi");
                commonAnnotationLevel -= tokenValue.match(regex).length;
            }

            // check whether inside a partition element
            if (token.value.toLowerCase() === 'begin') {
                isInsideAPartition = true;
            } else if (token.value.toLowerCase() === 'end') {
                isInsideAPartition = false;
            }

            // put spaces
            spaceRules.forEach(function (spaceRule) {
                if (token.type === spaceRule.type && (!spaceRule.value || token.value === spaceRule.value)) {
                    if (spaceRule.prepend && !_.endsWith(code, space)) {
                        value = space + token.value;
                        isValueContainsANewLine = false;
                    }
                    if (spaceRule.append && !_.isEqual(nextToken.type, 'whitespace') && nextToken.value !== space) {
                        value += space;
                    }
                }
            });

            // put new lines
            newLinesRules.forEach(function (newLineRule) {
                if (token.type === newLineRule.type && (!newLineRule.value || token.value === newLineRule.value)) {
                    // Here isCodeEndingWithANewLine is used instead of _.endsWith(code, newLine) because after the
                    // new line character if a whitespace is there, then _.endsWith(code, newLine) returns false while
                    // isCodeEndingWithANewLine returns true
                    if (newLineRule.breakBefore && !isCodeEndingWithANewLine) {
                        var skipBreakBefore = false;
                        if (_.has(newLineRule, 'avoidBreakBeforeUpon')) {
                            var avoidBreakBeforeUpon = _.get(newLineRule, 'avoidBreakBeforeUpon');
                            if (_.isEqual(_.get(avoidBreakBeforeUpon, 'lastNonWhiteSpaceToken.type'),
                                    _.get(lastNonWhiteSpaceToken, 'type'))
                                && _.isEqual(_.get(avoidBreakBeforeUpon, 'lastNonWhiteSpaceToken.value'),
                                    _.get(lastNonWhiteSpaceToken, 'value'))) {
                                skipBreakBefore = true;
                            }
                        }
                        if (!skipBreakBefore && code !== "") { // don't add newlines in the beginning of the file
                            code += newLine;
                            isCodeEndingWithANewLine = true;
                        }
                    }
                    // add tab spaces for common annotation types
                    if (commonAnnotationLevel > 1) {
                        var loopEndSize = commonAnnotationLevel - 1;
                        if (_.endsWith(code, tab)) {
                            loopEndSize = commonAnnotationLevel - noOfTabsInBeginOfNewLineInAnnotation - 1;
                            noOfTabsInBeginOfNewLineInAnnotation = 0;
                        }
                        for (var i = 0; i < loopEndSize; i++) {
                            code += tab;
                        }
                    }
                    if (newLineRule.addTabSpace) {
                        code += tab;
                    }
                    // adding tab spaces related to partitions
                    if (isInsideAPartition === true && newLineRule.checkIsInsidePartition) {
                        code += tab;
                    }
                    if (newLineRule.breakAfter) {
                        value += newLine;
                        isValueContainsANewLine = true;
                        isCodeEndingWithANewLine = true;
                    }
                }
            });

            code += value;
            if (token.type !== 'whitespace') {
                isCodeEndingWithANewLine = isValueContainsANewLine;
                isValueContainsANewLine = false;
            }

            lastToken = token;

            if (!_.isEqual(token.type, 'whitespace')) {
                lastNonWhiteSpaceToken = token;
            }

            token = nextToken;

            if (token === null) {
                break;
            }
        }

        return code;
    };

    return SiddhiFormatter;
});
