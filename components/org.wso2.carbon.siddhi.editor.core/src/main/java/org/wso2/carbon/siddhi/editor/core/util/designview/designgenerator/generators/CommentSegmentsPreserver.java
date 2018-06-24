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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Contains methods for preserving Comment segments in a Siddhi app, when generating Siddhi ElementConfig objects
 */
public class CommentSegmentsPreserver {
    private String siddhiAppString;
    private List<ElementCodeSegment> elementCodeSegments = new ArrayList<>();
    private List<CommentCodeSegment> commentCodeSegments = new ArrayList<>();

    /**
     * The Constructor.
     * Accepts a Set of ElementCodeSegments and puts each member to the List of ElementCodeSegments
     * @param siddhiAppString           The complete Siddhi app string
     * @param elementCodeSegments       Set of ElementCodeSegment objects
     */
    public CommentSegmentsPreserver(String siddhiAppString, Set<ElementCodeSegment> elementCodeSegments) {
        this.siddhiAppString = siddhiAppString;
        this.elementCodeSegments.addAll(elementCodeSegments);
    }

    /**
     * Generates a list of CommentCodeSegments
     * @return                                  List of CommentCodeSegments
     * @throws DesignGenerationException        Error while getting code segments between two ElementCodeSegments
     */
    public List<CommentCodeSegment> generateCommentCodeSegments() throws DesignGenerationException {
        Collections.sort(elementCodeSegments);
        elementCodeSegments = filterMajorElementCodeSegments(elementCodeSegments);
        for (int i = 1; i < elementCodeSegments.size(); i++) {
            if (hasCodeSegmentInBetween(elementCodeSegments.get(i - 1), elementCodeSegments.get(i))) {
                CommentCodeSegment codeSegmentInBetween =
                        getCodeSegmentInBetween(elementCodeSegments.get(i - 1), elementCodeSegments.get(i));
                if (isCommentValid(codeSegmentInBetween)) {
                    commentCodeSegments.add(codeSegmentInBetween);
                }
            }
        }
        return commentCodeSegments;
    }

    private boolean hasCodeSegmentInBetween(ElementCodeSegment previousSegment, ElementCodeSegment currentSegment) {
        return (getCharCountFromLineAndColumn(
                currentSegment.getStartLine(),
                currentSegment.getStartColumn(),
                siddhiAppString) -
                getCharCountFromLineAndColumn(
                        previousSegment.getEndLine(),
                        previousSegment.getEndColumn(), siddhiAppString) > 1);
    }

    /**
     * Returns the comment segment, which exists before the first even element's code
     * @return                                  CommentCodeSegment object, representing the first comment segment
     * @throws DesignGenerationException        Error while getting the first comment segment
     */
    public CommentCodeSegment getCommentSegmentBeforeContent() throws DesignGenerationException {
        return getCommentSegmentBeforeContent(elementCodeSegments.get(0));
    }

    /**
     * Returns the comment segment before the given code segment
     * @param firstElementCodeSegment           Code segment of the first ever element from the Siddhi app
     * @return                                  CommentCodeSegment object
     * @throws DesignGenerationException        Error while getting the comment segment
     */
    private CommentCodeSegment getCommentSegmentBeforeContent(ElementCodeSegment firstElementCodeSegment)
            throws DesignGenerationException {
        if (!(firstElementCodeSegment.getStartLine() == 1 && firstElementCodeSegment.getStartColumn() == 0)) {
            // There are comments in between the first ever code segment
            int[] firstCommentEndingLineAndColumn =
                    getPreviousQueryIndexes(firstElementCodeSegment.getQueryContextStartIndex(), siddhiAppString);
            if (firstCommentEndingLineAndColumn == null) {
                return null;
            }
            return new CommentCodeSegment(
                    new int[]{1, 0},
                    firstCommentEndingLineAndColumn,
                    ConfigBuildingUtilities
                            .getStringWithQueryContextIndexes(
                                    new int[]{1, 0}, firstCommentEndingLineAndColumn, siddhiAppString));
        }
        return null;
    }

    /**
     * Returns filtered code segments, that is only the major elements of the Siddhi app
     * @param elementCodeSegments       Unfiltered list of ElementCodeSegments
     * @return                          Filtered list of ElementCodeSegments
     */
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

    /**
     * Returns whether the given ElementCodeSegment is contained within any other ElementCodeSegment in the given list
     * @param elementCodeSegment        ElementCodeSegment object to perform the check
     * @param elementCodeSegments       List of ElementCodeSegments, that might contain the elementCodeSegment
     * @return                          Whether the given ElementCodeSegment is contained
     *                                  within any other ElementCodeSegment in the given list, or not
     */
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

    /**
     * Returns whether the given container ElementCodeSegment contains the contained ElementCodeSegment or not
     * @param containedElement          ElementCodeSegment object that can be contained
     * @param containerElement          ElementCodeSegment object that can contain
     * @return                          Whether the containerElement contains the containedElement or not
     */
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
        if (containedStart < containedEnd &&
                containedStart != -1 && containedEnd != -1 && containerStart != -1 && containerEnd != -1) {
            return containerStart <= containedStart && containedEnd <= containerEnd;
        }
        return false;
    }

    /**
     * Binds relevant CommentCodeSegment object from the given list of CommentCodeSegments,
     * to SiddhiElementConfigs from the given siddhiAppConfigReference object.
     * Comment segment that exists above a Code segment of an element, is bound to that particular element
     * @param commentCodeSegments               List of CommentCodeSegments
     * @param siddhiAppConfigReference          SiddhiAppConfig object
     * @return                                  SiddhiAppConfig object with CommentCodeSegment bindings
     */
    public SiddhiAppConfig bindCommentsToElements(Collection<CommentCodeSegment> commentCodeSegments,
                                                  SiddhiAppConfig siddhiAppConfigReference) {
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

        return siddhiAppConfigReference;
    }

    /**
     * Gets the comment segment in between the given ElementCodeSegment objects
     * @param previous                          First ElementCodeSegment object
     * @param current                           Second ElementCodeSegment object
     * @return                                  CommentCodeSegment object
     * @throws DesignGenerationException        Error while getting the comment segment
     */
    private CommentCodeSegment getCodeSegmentInBetween(ElementCodeSegment previous, ElementCodeSegment current)
            throws DesignGenerationException {
        int[][] inBetweenIndexes =
                getIndexesInBetween(previous.getQueryContextEndIndex(), current.getQueryContextStartIndex());
        if (inBetweenIndexes[0] == null || inBetweenIndexes[1] == null) {
            return null;
        }
        return new CommentCodeSegment(
                inBetweenIndexes[0],
                inBetweenIndexes[1],
                ConfigBuildingUtilities.getStringWithQueryContextIndexes(
                        inBetweenIndexes[0], inBetweenIndexes[1], siddhiAppString));

    }

    /**
     * Gets query indexes, in between the given query indexes
     * @param prevIndex     Query indexes of the first element
     * @param current       Query indexes of the second element
     * @return              Query indexes in between the given first and the second elements
     */
    private int[][] getIndexesInBetween(int[] prevIndex, int[] current) {
        return new int[][]{
                getNextQueryIndexes(prevIndex, siddhiAppString),
                getPreviousQueryIndexes(current, siddhiAppString)};
    }

    /**
     * Gets the previous query indexes for the given query indexes, in the given Siddhi app string
     * @param queryIndexes                  Query indexes depicting the current line and column
     * @param siddhiAppString               Complete Siddhi app string
     * @return                              Previous query indexes
     */
    private int[] getPreviousQueryIndexes(int[] queryIndexes, String siddhiAppString) {
        if (queryIndexes[1] == 0) {
            // Last column of the previous line is the end
            String previousLineContent = getLineContent(queryIndexes[0] - 1, siddhiAppString);
            if (previousLineContent != null) {
                return new int[]{queryIndexes[0] - 1, previousLineContent.length()};
            }
        } else {
            // Current beginning column is the end
            return new int[]{queryIndexes[0], queryIndexes[1]};
        }
        return null;
    }

    /**
     * Gets the next query indexes for the given query indexes, in the given Siddhi app string
     * @param queryIndexes                  Query indexes depicting the current line and column
     * @param siddhiAppString               Complete Siddhi app string
     * @return                              Next query indexes
     */
    private int[] getNextQueryIndexes(int[] queryIndexes, String siddhiAppString) {
        String lineContent = getLineContent(queryIndexes[0], siddhiAppString);
        if (lineContent != null) {
            if (lineContent.length() == queryIndexes[1]) {
                // Return next line's first column
                return new int[]{queryIndexes[0] + 1, 0};
            } else {
                // Return next column on the current line
                return new int[]{queryIndexes[0], queryIndexes[1] + 1};
            }
        }
        return null;
    }

    /**
     * Gets content of the line that has the given number, in the given Siddhi app string
     * @param lineNo                Line number
     * @param siddhiAppString       Complete Siddhi app string
     * @return                      Content of the line
     */
    private String getLineContent(int lineNo, String siddhiAppString) {
        int lineCounter = 1;
        int charCounter = 0;
        for (char character : siddhiAppString.toCharArray()) {
            if (lineNo == lineCounter) {
                // Mark this
                String subString = siddhiAppString.substring(charCounter);
                return subString.split("\n")[0] + "\n";
            }
            if (character == '\n') {
                lineCounter++;
            }
            charCounter++;
        }
        return null;
    }

    /**
     * Returns whether the given CommentCodeSegment is valid as a Siddhi comment or not
     * @param commentCodeSegment        CommentCodeSegment object
     * @return                          Whether the CommentCodeSegment is valid or not
     */
    private boolean isCommentValid(CommentCodeSegment commentCodeSegment) {
        if (commentCodeSegment == null) {
            return false;
        }
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

    /**
     * Assigns the previous CommentCodeSegment from the given list, to the SiddhiElementConfig object
     * @param siddhiElementConfigReference          SiddhiElementConfig object
     * @param commentCodeSegments                   List of CommentCodeSegment objects
     */
    private void assignPreviousCommentSegment(SiddhiElementConfig siddhiElementConfigReference,
                                              Collection<CommentCodeSegment> commentCodeSegments) {
        siddhiElementConfigReference.setPreviousCommentSegment(
                findPreviousCommentCodeSegment(siddhiElementConfigReference, commentCodeSegments));
    }

    /**
     * Finds and returns the previous CommentCodeSegment object for the given SiddhiElementConfig object,
     * from the given list of CommentCodeSegments when exists,
     * otherwise returns null
     * @param siddhiElementConfig           SiddhiElementConfig object
     * @param commentCodeSegments           List of CommentCodeSegment objects
     * @return                              CommentCodeSegment object
     */
    private CommentCodeSegment findPreviousCommentCodeSegment(SiddhiElementConfig siddhiElementConfig,
                                                              Collection<CommentCodeSegment> commentCodeSegments) {
        int[] previousCommentCodeSegmentEndIndex =
                getPreviousCommentCodeSegmentEndIndexes(siddhiElementConfig.getQueryContextStartIndex());
        if (previousCommentCodeSegmentEndIndex == null) {
            return null;
        }
        CommentCodeSegment commentCodeSegment =
                findCommentCodeSegmentEndingWith(previousCommentCodeSegmentEndIndex, commentCodeSegments);
        if (commentCodeSegment != null) {
            commentCodeSegments.remove(commentCodeSegment); // To avoid comments getting bound to more than one elements
        }
        return commentCodeSegment;
    }

    /**
     * Gets the indexes of the previous CommentCodeSegment, from the given start indexes
     * @param currentStartIndexes           Starting indexes after a CommentCodeSegment
     * @return                              Ending indexes of the CommentCodeSegment
     */
    private int[] getPreviousCommentCodeSegmentEndIndexes(int[] currentStartIndexes) {
        if (!(currentStartIndexes[0] == 0 && currentStartIndexes[1] == 0)) {
            return getPreviousQueryIndexes(currentStartIndexes, siddhiAppString);
        }
        return null;
    }

    /**
     * Returns the CommentCodeSegment which is ending with the given indexes,
     * from the given list of CommentCodeSegments
     * @param endIndexes                    Ending query indexes of the CommentCodeSegment
     * @param commentCodeSegments           List of CommentCodeSegments
     * @return                              CommentCodeSegment which is ending with the given indexes
     */
    private CommentCodeSegment findCommentCodeSegmentEndingWith(int[] endIndexes,
                                                                Collection<CommentCodeSegment> commentCodeSegments) {
        for (CommentCodeSegment commentCodeSegment : commentCodeSegments) {
            if (commentCodeSegment != null) {
                int[] commentSegmentEndIndex = commentCodeSegment.getQueryContextEndIndex();
                if (commentSegmentEndIndex != null &&
                        commentSegmentEndIndex[0] == endIndexes[0] && commentSegmentEndIndex[1] == endIndexes[1]) {
                    return commentCodeSegment;
                }
            }
        }
        return null;
    }

    /**
     * Gets the character count until the given line and column, in the given Siddhi app string
     * @param line                  Line number
     * @param column                Column number
     * @param siddhiAppString       Complete Siddhi app string
     * @return                      Character count
     */
    private int getCharCountFromLineAndColumn(int line, int column, String siddhiAppString) {
        int characterCounter = 0;
        for (int l = 1; l < line; l++) {
            String lineContent = getLineContent(l, siddhiAppString);
            if (lineContent != null) {
                characterCounter += lineContent.length() + 1;
            }
        }
        String requiredLineContent = getLineContent(line, siddhiAppString);
        if (requiredLineContent != null) {
            for (int c = 0; c <= requiredLineContent.length(); c++) {
                if (c == column) {
                    return characterCounter;
                }
                characterCounter++;
            }
        }
        return -1;
    }
}
