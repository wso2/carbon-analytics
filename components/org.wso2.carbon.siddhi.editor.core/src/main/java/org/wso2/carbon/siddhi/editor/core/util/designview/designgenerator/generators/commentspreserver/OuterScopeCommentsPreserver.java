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

package org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.generators.commentspreserver;

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

public class OuterScopeCommentsPreserver extends CommentsPreserver {

    public OuterScopeCommentsPreserver(String siddhiAppString, Set<ElementCodeSegment> elementCodeSegments) {
        super(siddhiAppString, new ArrayList<>(elementCodeSegments));
    }

    @Override
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

    @Override
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
            commentCodeSegments.remove(commentCodeSegment); // To avoid comments getting bound to more than one element
        }
        return commentCodeSegment;
    }
}
