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
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.StreamConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.partition.PartitionConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.partition.PartitionWithElement;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.QueryConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.query.QueryListType;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.DesignGenerationException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Preserves Comments of major elements, that reside in a Partition scope
 */
public class PartitionScopeCommentsPreserver extends ScopedCommentsPreserver {
    private PartitionConfig partitionConfig;
    private ElementCodeSegment partitionCodeSegment;

    public PartitionScopeCommentsPreserver(String siddhiAppString,
                                           PartitionConfig partitionConfig,
                                           Set<ElementCodeSegment> elementCodeSegments) {
        super(siddhiAppString, new ArrayList<>(elementCodeSegments));
        this.partitionConfig = partitionConfig;
        partitionCodeSegment =
                new ElementCodeSegment(
                        partitionConfig.getQueryContextStartIndex(),
                        partitionConfig.getQueryContextEndIndex());
    }

    @Override
    public List<CommentCodeSegment> generateCommentCodeSegments() throws DesignGenerationException {
        Collections.sort(elementCodeSegments);
        elementCodeSegments = filterCurrentPartitionContainedCodeSegments(elementCodeSegments);
        elementCodeSegments = filterMajorElementCodeSegments(elementCodeSegments);
        commentCodeSegments = detectCommentCodeSegments(elementCodeSegments);
        commentCodeSegments = filterCommentsAfterPartitionWithKeyword(commentCodeSegments);
        return commentCodeSegments;
    }

    /**
     * Returns the filtered list of Code segments that only belong to the current Partition,
     * from the given list of ElementCodeSegments
     * @param elementCodeSegments       Unfiltered list of ElementCodeSegment objects
     * @return                          Filtered list of ElementCodeSegment objects
     */
    private List<ElementCodeSegment> filterCurrentPartitionContainedCodeSegments(
            List<ElementCodeSegment> elementCodeSegments) {
        List<ElementCodeSegment> filteredElementCodeSegments = new ArrayList<>();
        ElementCodeSegment partitionElementCodeSegment =
                new ElementCodeSegment(
                        partitionConfig.getQueryContextStartIndex(), partitionConfig.getQueryContextEndIndex());
        for (ElementCodeSegment elementCodeSegment : elementCodeSegments) {
            if (!elementCodeSegment.equals(partitionElementCodeSegment) &&
                    doesSegmentBelongToCurrentPartition(elementCodeSegment)) {
                filteredElementCodeSegments.add(elementCodeSegment);
            }
        }
        return filteredElementCodeSegments;
    }

    /**
     * Returns only the Comment segments, existing after 'partition with' elements
     * @param commentCodeSegments       List of CommentCodeSegment objects
     * @return                          List of filtered CommentCodeSegment objects
     */
    private List<CommentCodeSegment> filterCommentsAfterPartitionWithKeyword(
            List<CommentCodeSegment> commentCodeSegments) {
        List<CommentCodeSegment> filteredCommentCodeSegments = new ArrayList<>();
        int[] partitionWithLastIndex = getLastPartitionWithElementEndIndex(partitionConfig.getPartitionWith());
        for (CommentCodeSegment commentCodeSegment : commentCodeSegments) {
            if (commentCodeSegment.getStartLine() > partitionWithLastIndex[0] ||
                    (commentCodeSegment.getStartLine() == partitionWithLastIndex[0] &&
                            commentCodeSegment.getStartColumn() >= partitionWithLastIndex[1])) {
                filteredCommentCodeSegments.add(commentCodeSegment);
            }
        }
        filteredCommentCodeSegments
                .set(0, getTrimmedFirstInnerPartitionCommentSegment(filteredCommentCodeSegments.get(0)));
        return filteredCommentCodeSegments;
    }

    @Override
    public SiddhiAppConfig bindCommentsToElements(Collection<CommentCodeSegment> commentCodeSegments,
                                                  SiddhiAppConfig siddhiAppConfigReference) {
        PartitionConfig currentPartitionConfigReference = getCurrentPartitionConfigReference(siddhiAppConfigReference);
        if (currentPartitionConfigReference != null) {
            for (StreamConfig streamConfig : currentPartitionConfigReference.getStreamList()) {
                assignPreviousCommentSegment(streamConfig, commentCodeSegments);
            }
            for (Map.Entry<QueryListType, List<QueryConfig>> queryListEntries :
                    currentPartitionConfigReference.getQueryLists().entrySet()) {
                for (QueryConfig queryConfig : queryListEntries.getValue()) {
                    assignPreviousCommentSegment(queryConfig, commentCodeSegments);
                }
            }
        }
        return siddhiAppConfigReference;
    }

    /**
     * Gets the ending index of the very last 'partition with' element
     * @param partitionWithElements     List of 'partition with' elements
     * @return                          Query context end index of the very last 'partition with' element
     */
    private int[] getLastPartitionWithElementEndIndex(List<PartitionWithElement> partitionWithElements) {
        List<ElementCodeSegment> partitionWithCodeSegments = new ArrayList<>();
        for (PartitionWithElement partitionWithElement : partitionWithElements) {
            partitionWithCodeSegments.add(
                    new ElementCodeSegment(
                            partitionWithElement.getQueryContextStartIndex(),
                            partitionWithElement.getQueryContextEndIndex()));
        }
        Collections.sort(partitionWithCodeSegments);
        return partitionWithCodeSegments.get(partitionWithCodeSegments.size() - 1).getQueryContextEndIndex();
    }

    /**
     * Returns the first comment within the partition, after trimming the 'begin' keyword.
     * Since the 'begin' keyword is not parsed, it will be detected as a comment that is immediately after the
     * last ever 'partition with' element.
     * @param firstCommentCodeSegment       First comment segment, that contains the 'begin' keyword
     * @return                              Comment segment trimmed out of the 'begin' keyword
     */
    private CommentCodeSegment getTrimmedFirstInnerPartitionCommentSegment(CommentCodeSegment firstCommentCodeSegment) {
        final String BEGIN = "begin";
        String[] splitArray = firstCommentCodeSegment.getContent().split(BEGIN);
        String newContent = String.join(BEGIN, Arrays.copyOfRange(splitArray, 1, splitArray.length));

        int line = firstCommentCodeSegment.getStartLine();
        int column = firstCommentCodeSegment.getStartColumn();
        for (char character : splitArray[0].toCharArray()) {
            if (character == '\n') {
                line++;
                column = 0;
            } else {
                column++;
            }
        }
        column += BEGIN.length();

        return new CommentCodeSegment(
                new int[]{line, column}, firstCommentCodeSegment.getQueryContextEndIndex(), newContent);
    }

    /**
     * Finds and returns the reference of the PartitionConfig object,
     * for the Partition whose comments are about to be preserved with this Preserver,
     * from the given SiddhiAppConfig reference
     * @param siddhiAppConfigReference          SiddhiAppConfig object reference
     * @return                                  PartitionConfig object reference
     */
    private PartitionConfig getCurrentPartitionConfigReference(SiddhiAppConfig siddhiAppConfigReference) {
        for (PartitionConfig partitionConfigReference : siddhiAppConfigReference.getPartitionList()) {
            ElementCodeSegment codeSegment =
                    new ElementCodeSegment(
                            partitionConfigReference.getQueryContextStartIndex(),
                            partitionConfigReference.getQueryContextEndIndex());
            if (codeSegment.equals(partitionCodeSegment)) {
                return partitionConfigReference;
            }
        }
        return null;
    }

    /**
     * Returns whether the given code segment belongs to the current Partition or not
     * @param elementCodeSegment        ElementCodeSegment object
     * @return                          Whether the code segment belongs to the current Partition or not
     */
    private boolean doesSegmentBelongToCurrentPartition(ElementCodeSegment elementCodeSegment) {
        ElementCodeSegment partitionElementCodeSegment =
                new ElementCodeSegment(
                        partitionConfig.getQueryContextStartIndex(),
                        partitionConfig.getQueryContextEndIndex());
        return isSegmentContainedIn(elementCodeSegment, partitionElementCodeSegment);
    }
}
