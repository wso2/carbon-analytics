/*
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

package org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.generators;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.CommentCodeSegment;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.ElementCodeSegment;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.SiddhiAppConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.FunctionConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.SiddhiElementConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.StreamConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.TableConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.TriggerConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.WindowConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.aggregation.AggregationConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.partition.PartitionConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.QueryConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.sourcesink.SourceSinkConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.query.QueryListType;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.DesignGenerationException;
import org.wso2.carbon.siddhi.editor.core.util.designview.utilities.ConfigBuildingUtilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

// TODO class comment
public class CommentsPreserver {
    private String siddhiAppString;
    private List<ElementCodeSegment> elementCodeSegments;
    private List<CommentCodeSegment> commentCodeSegments = new ArrayList<>();

    public CommentsPreserver(String siddhiAppString, List<ElementCodeSegment> elementCodeSegments) {
        this.siddhiAppString = siddhiAppString;
        this.elementCodeSegments = elementCodeSegments;
    }

    public List<CommentCodeSegment> generateCommentCodeSegments() throws DesignGenerationException {
        Collections.sort(elementCodeSegments);
        elementCodeSegments = filterMajorElementCodeSegments(elementCodeSegments);
        for (int i = 1; i < elementCodeSegments.size(); i++) {
            CommentCodeSegment previousCommentSegment =
                    getCodeSegmentInBetween(elementCodeSegments.get(i - 1), elementCodeSegments.get(i));
            if (isCommentValid(previousCommentSegment)) {
                commentCodeSegments.add(previousCommentSegment); // TODO put content
            }
        }
        return commentCodeSegments;
    }

    private List<ElementCodeSegment> filterMajorElementCodeSegments(List<ElementCodeSegment> elementCodeSegments) {
        // Eliminates elements that are inside any other element in the elementCodeSegments list
        List<ElementCodeSegment> filteredElementCodeSegments = new ArrayList<>();
        for (ElementCodeSegment currentSegment : elementCodeSegments) {
            if (!isContainedInAnySegment(currentSegment, elementCodeSegments)) {
                filteredElementCodeSegments.add(currentSegment);
            }
        }
        return filteredElementCodeSegments;
    }

    private boolean isContainedInAnySegment(ElementCodeSegment elementCodeSegment,
                                            List<ElementCodeSegment> elementCodeSegments) {
        boolean isContainedInAnySegment = false;
        for (ElementCodeSegment comparedSegment : elementCodeSegments) {
            if (!elementCodeSegment.equals(comparedSegment)) {
                if (isSegmentContainedIn(elementCodeSegment, comparedSegment)) {
                    isContainedInAnySegment = true;
                    break;
                }
            }
        }
        return isContainedInAnySegment;
    }

    private boolean isSegmentContainedIn(ElementCodeSegment containedElement, ElementCodeSegment containerElement) {
        int containedStart =
                getCharCountFromLineAndColumn(
                        containedElement.getStartLine(), containedElement.getStartColumn(), siddhiAppString);
        int containedEnd =
                getCharCountFromLineAndColumn(
                        containedElement.getEndLine(), containedElement.getEndColumn(), siddhiAppString);
        int containerStart =
                getCharCountFromLineAndColumn(
                        containerElement.getStartLine(), containerElement.getStartColumn(), siddhiAppString);
        int containerEnd =
                getCharCountFromLineAndColumn(
                        containerElement.getEndLine(), containerElement.getEndColumn(), siddhiAppString);
        // TODO validate whether a 'valid segment with START < END'
        return containerStart <= containedStart && containedEnd <= containerEnd;
    }

    public SiddhiAppConfig bindCommentsToElements(List<CommentCodeSegment> commentCodeSegments,
                                                  SiddhiAppConfig siddhiAppConfigReference) {
        // For each element of each list
        // find previous comment section if exists
        // Assign
        // TODO others
        for (SourceSinkConfig sourceConfig : siddhiAppConfigReference.getSourceList()) {
            assignPreviousCommentSegment(sourceConfig, commentCodeSegments);
        }
        for (SourceSinkConfig sinkConfig : siddhiAppConfigReference.getSinkList()) {
            assignPreviousCommentSegment(sinkConfig, commentCodeSegments);
        }
        for (StreamConfig streamConfig : siddhiAppConfigReference.getStreamList()) {
            assignPreviousCommentSegment(streamConfig, commentCodeSegments);
        }
        for (TableConfig tableConfig : siddhiAppConfigReference.getTableList()) {
            assignPreviousCommentSegment(tableConfig, commentCodeSegments);
        }
        for (TriggerConfig triggerConfig : siddhiAppConfigReference.getTriggerList()) {
            assignPreviousCommentSegment(triggerConfig, commentCodeSegments);
        }
        for (WindowConfig windowConfig : siddhiAppConfigReference.getWindowList()) {
            assignPreviousCommentSegment(windowConfig, commentCodeSegments);
        }
        for (AggregationConfig aggregationConfig : siddhiAppConfigReference.getAggregationList()) {
            assignPreviousCommentSegment(aggregationConfig, commentCodeSegments);
        }
        for (FunctionConfig functionConfig : siddhiAppConfigReference.getFunctionList()) {
            assignPreviousCommentSegment(functionConfig, commentCodeSegments);
        }
        for (Map.Entry<QueryListType, List<QueryConfig>> queryListEntries :
                siddhiAppConfigReference.getQueryLists().entrySet()) {
            for (QueryConfig queryConfig : queryListEntries.getValue()) {
                assignPreviousCommentSegment(queryConfig, commentCodeSegments);
            }
        }
        for (PartitionConfig partitionConfig : siddhiAppConfigReference.getPartitionList()) {
            assignPreviousCommentSegment(partitionConfig, commentCodeSegments);
        }

        // TODO others
        // TODO possibly move this into siddhiappconfig to look properly
        return siddhiAppConfigReference;
    }

    private CommentCodeSegment getCodeSegmentInBetween(ElementCodeSegment previous, ElementCodeSegment current)
            throws DesignGenerationException {
        int[][] inBetweenIndexes =
                getIndexesInBetween(previous.getQueryContextEndIndex(), current.getQueryContextStartIndex());
        return new CommentCodeSegment(
                inBetweenIndexes[0],
                inBetweenIndexes[1],
                ConfigBuildingUtilities.getStringWithQueryContextIndexes(
                        inBetweenIndexes[0], inBetweenIndexes[1], siddhiAppString));

    }

    private int[][] getIndexesInBetween(int[] prevIndex, int[] current) {
        int[][] test = new int[][]{
                getNextLineAndColumn(prevIndex, siddhiAppString),
                getPreviousLineAndColumn(current, siddhiAppString)};
        return test;
    }

    private int[] getPreviousLineAndColumn(int[] index, String siddhiAppString) {
        if (index[1] == 0) {
            // Last column of the line before should be found
            String previousLineContent = getLineContent(siddhiAppString, index[0] - 1);
            if (previousLineContent != null) {
                return new int[]{index[0] - 1, previousLineContent.length()}; // TODO chekc this minus one
            }
        } else {
            // Return previous column from the current column
            return new int[]{index[0], index[1] - 1};
        }
        return null; // TODO LOOK INTO
    }

    private int[] getNextLineAndColumn(int[] index, String siddhiAppString) {
        String lineContent = getLineContent(siddhiAppString,index[0]);
        if (lineContent != null) {
            if (lineContent.length() == index[1]) { // TODO look into length() - 1 if doesn't work
                // Return next line's first column
                return new int[]{index[0] + 1, 0};
            } else {
                // Return next column on the current line
                return new int[]{index[0], index[1] + 1};
            }
        }
        return null; // TODO LOOK INTO
    }

    private String getLineContent(String siddhiAppString, int lineNo) {
        int lineCounter = 1;
        int charCounter = 0;
        for (char character : siddhiAppString.toCharArray()) {
            if (lineNo == lineCounter) {
                // Mark this
                String subString = siddhiAppString.substring(charCounter);
                return subString.split("\n")[0];
            }
            if (character == '\n') {
                lineCounter++;
            }
            charCounter++;
        }
        return null;
    }

    private boolean isCommentValid(CommentCodeSegment commentCodeSegment) {
        // TODO implement: Probably begin with null check
        if (commentCodeSegment.getQueryContextEndIndex()[0] < commentCodeSegment.getQueryContextStartIndex()[0]) {
            return false;
        } else if (commentCodeSegment.getQueryContextEndIndex()[0] ==
                commentCodeSegment.getQueryContextStartIndex()[0]) {
            if (commentCodeSegment.getQueryContextEndIndex()[1] < commentCodeSegment.getQueryContextStartIndex()[1]) {
                return false;
            } else if (commentCodeSegment.getQueryContextEndIndex()[1] ==
                    commentCodeSegment.getQueryContextStartIndex()[1]) {
                return false;
            }
        }
        return true;
    }

    private void assignPreviousCommentSegment(SiddhiElementConfig siddhiElementConfigReference,
                                              List<CommentCodeSegment> commentCodeSegments) {
        siddhiElementConfigReference.setPreviousCommentSegment(
                findPreviousCommentCodeSegment(siddhiElementConfigReference, commentCodeSegments));
    }

    private CommentCodeSegment findPreviousCommentCodeSegment(SiddhiElementConfig siddhiElementConfig,
                                                              List<CommentCodeSegment> commentCodeSegments) {
        int[] previousCommentCodeSegmentEndIndex =
                getPreviousCommentCodeSegmentEndIndex(siddhiElementConfig.getQueryContextStartIndex());
        return findCommentCodeSegmentEndingWith(previousCommentCodeSegmentEndIndex, commentCodeSegments);
    }

    private int[] getPreviousCommentCodeSegmentEndIndex(int[] currentStartIndex) {
        if (!(currentStartIndex[0] == 0 && currentStartIndex[1] == 0)) {
            return getPreviousLineAndColumn(currentStartIndex, siddhiAppString);
        }
        return new int[]{-1, -1};
    }

    private CommentCodeSegment findCommentCodeSegmentEndingWith(int[] endIndex,
                                                                List<CommentCodeSegment> commentCodeSegments) {
        for (CommentCodeSegment commentCodeSegment : commentCodeSegments) {
            int[] targetEndIndex = commentCodeSegment.getQueryContextEndIndex();
            if (targetEndIndex[0] == endIndex[0] && targetEndIndex[1] == endIndex[1]) {
                return commentCodeSegment;
            }
        }
        return null;
    }

    private int getCharCountFromLineAndColumn(int line, int column, String siddhiAppString) {
        int characterCounter = 0;
        for (int l = 1; l < line; l++) {
            characterCounter += getLineContent(siddhiAppString, l).length() + 1;
        }
        for (int c = 0; c < getLineContent(siddhiAppString, line).length(); c++) {
            if (c == column) {
                return characterCounter;
            }
            characterCounter++;
        }
        return -1;
    }
}
