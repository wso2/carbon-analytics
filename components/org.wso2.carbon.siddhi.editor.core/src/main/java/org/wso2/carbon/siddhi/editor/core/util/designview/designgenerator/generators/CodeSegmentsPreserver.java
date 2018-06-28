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

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.ElementCodeSegment;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.SiddhiElementConfig;
import org.wso2.siddhi.query.api.SiddhiElement;

import java.util.HashSet;
import java.util.Set;

/**
 * Contains methods to preserve code segments of Siddhi elements through their Config Generators,
 * in order to help preserving comments
 */
public abstract class CodeSegmentsPreserver {
    private Set<ElementCodeSegment> preservedCodeSegments = new HashSet<>();

    public Set<ElementCodeSegment> getPreservedCodeSegments() {
        return preservedCodeSegments;
    }

    /**
     * Preserves code segment of the given SiddhiElement object
     * @param siddhiElement     SiddhiElement object which represents a parsed Siddhi element
     */
    public void preserveCodeSegment(SiddhiElement siddhiElement) {
        if (isCodeSegmentValid(siddhiElement)) {
            preservedCodeSegments.add(
                    new ElementCodeSegment(
                            siddhiElement.getQueryContextStartIndex(), siddhiElement.getQueryContextEndIndex()));
        }
    }

    /**
     * Preserves code segment of the given SiddhiElement,
     * and binds the query start and end indexes to the given SiddhiElementConfig
     * @param bindFromElement           SiddhiElement object from which,
     *                                  code segment indexes are acquired and preserved
     * @param bindToElementConfig       SiddhiElementConfig object to which, the acquired indexes should be bound
     */
    protected void preserveAndBindCodeSegment(SiddhiElement bindFromElement, SiddhiElementConfig bindToElementConfig) {
        preserveCodeSegment(bindFromElement);
        bindCodeSegmentIndexes(bindFromElement, bindToElementConfig);
    }

    /**
     * Binds query context start and end indexes of the given SiddhiElement object,
     * to the given SiddhiElementConfig object
     * @param bindFromElement           SiddhiElement object from which, code segment indexes are acquired
     * @param bindToElementConfig       SiddhiElementConfig object to which, the acquired code segment indexes are bound
     */
    protected void bindCodeSegmentIndexes(SiddhiElement bindFromElement,
                                          SiddhiElementConfig bindToElementConfig) {
        if (isCodeSegmentValid(bindFromElement)) {
            bindToElementConfig.setQueryContextStartIndex(bindFromElement.getQueryContextStartIndex());
            bindToElementConfig.setQueryContextEndIndex(bindFromElement.getQueryContextEndIndex());
        }
    }

    /**
     * Preserves the code segments that are preserved in each of the given CodeSegmentsPreservers
     * @param codeSegmentsPreservers        One or more CodeSegmentsPreserver objects,
     *                                      whose preserved code segments are accessed for preserving
     */
    protected void preserveCodeSegmentsOf(CodeSegmentsPreserver... codeSegmentsPreservers) {
        for (CodeSegmentsPreserver codeSegmentsPreserver : codeSegmentsPreservers) {
            preservedCodeSegments.addAll(codeSegmentsPreserver.getPreservedCodeSegments());
        }
    }

    /**
     * Returns whether the given SiddhiElement object's code segment is valid or not
     * @param siddhiElement     SiddhiElement object
     * @return                  true if the code segment is valid, otherwise false
     */
    private boolean isCodeSegmentValid(SiddhiElement siddhiElement) {
        return siddhiElement != null &&
                siddhiElement.getQueryContextStartIndex() != null &&
                siddhiElement.getQueryContextEndIndex() != null;
    }
}
