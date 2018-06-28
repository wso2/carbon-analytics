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

package org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs;

import java.util.Arrays;

/**
 * Represents Code segment of a Siddhi element in a Siddhi app, which is generated as a config
 */
public class ElementCodeSegment implements Comparable<ElementCodeSegment> {
    private int[] queryContextStartIndex;
    private int[] queryContextEndIndex;

    public ElementCodeSegment(int[] queryContextStartIndex, int[] queryContextEndIndex) {
        this.queryContextStartIndex = new int[]{queryContextStartIndex[0], queryContextStartIndex[1]};
        this.queryContextEndIndex = new int[]{queryContextEndIndex[0], queryContextEndIndex[1]};
    }

    public int[] getQueryContextStartIndex() {
        return new int[]{queryContextStartIndex[0], queryContextStartIndex[1]};
    }

    public void setQueryContextStartIndex(int[] queryContextStartIndex) {
        this.queryContextStartIndex = new int[]{queryContextStartIndex[0], queryContextStartIndex[1]};
    }

    public int[] getQueryContextEndIndex() {
        return new int[]{queryContextEndIndex[0], queryContextEndIndex[1]};
    }

    public void setQueryContextEndIndex(int[] queryContextEndIndex) {
        this.queryContextEndIndex = new int[]{queryContextEndIndex[0], queryContextEndIndex[1]};
    }

    public int getStartLine() {
        return queryContextStartIndex[0];
    }

    public int getStartColumn() {
        return queryContextStartIndex[1];
    }

    public int getEndLine() {
        return queryContextEndIndex[0];
    }

    public int getEndColumn() {
        return queryContextEndIndex[1];
    }

    public boolean isValid() {
        if (getStartLine() == getEndLine()) {
            return (getEndColumn() - getStartColumn() > 0);
        }
        return (getEndLine() - getStartLine() > 0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ElementCodeSegment that = (ElementCodeSegment) o;
        return Arrays.equals(getQueryContextStartIndex(), that.getQueryContextStartIndex()) &&
                Arrays.equals(getQueryContextEndIndex(), that.getQueryContextEndIndex());
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(getQueryContextStartIndex());
        result = 31 * result + Arrays.hashCode(getQueryContextEndIndex());
        return result;
    }

    @Override
    public int compareTo(ElementCodeSegment elementCodeSegment) {
        if (getStartLine() > elementCodeSegment.getStartLine()) {
            return 1;
        } else if (getStartLine() == elementCodeSegment.getStartLine()) {
            if (getEndLine() == elementCodeSegment.getEndLine()) {
                if (getStartColumn() == elementCodeSegment.getStartColumn()) {
                    return Integer.compare(getEndColumn(), elementCodeSegment.getEndColumn());
                } else {
                    return Integer.compare(getStartColumn(), elementCodeSegment.getStartColumn());
                }
            } else {
                return Integer.compare(getEndLine(), elementCodeSegment.getEndLine());
            }
        } else {
            return -1;
        }
    }
}
