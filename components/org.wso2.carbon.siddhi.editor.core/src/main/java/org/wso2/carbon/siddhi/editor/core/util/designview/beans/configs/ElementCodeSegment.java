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

// TODO class comment
public class ElementCodeSegment implements Comparable {
    private int[] queryContextStartIndex;
    private int[] queryContextEndIndex;

    public ElementCodeSegment(int[] queryContextStartIndex, int[] queryContextEndIndex) {
        this.queryContextStartIndex = queryContextStartIndex;
        this.queryContextEndIndex = queryContextEndIndex;
    }

    public int[] getQueryContextStartIndex() {
        return queryContextStartIndex;
    }

    public void setQueryContextStartIndex(int[] queryContextStartIndex) {
        this.queryContextStartIndex = queryContextStartIndex;
    }

    public int[] getQueryContextEndIndex() {
        return queryContextEndIndex;
    }

    public void setQueryContextEndIndex(int[] queryContextEndIndex) {
        this.queryContextEndIndex = queryContextEndIndex;
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

    public boolean isOverlappingWith(ElementCodeSegment majorSegment) {
        return majorSegment.getStartLine() <= getStartLine() && getStartLine() <= majorSegment.getEndLine();
    }

    public boolean isValid() {
        if (getStartLine() == getEndLine()) {
            return (getEndColumn() - getStartColumn() > 0);
        }
        return (getEndLine() - getStartLine() > 0);
    }

    @Override
    public int compareTo(Object o) {
        ElementCodeSegment seg2 = (ElementCodeSegment) o;
        if (queryContextStartIndex[0] > seg2.queryContextStartIndex[0]) {
            return 1;
        } else if (queryContextStartIndex[0] == seg2.queryContextStartIndex[0]) {
            if (queryContextStartIndex[1] > seg2.queryContextStartIndex[1]) {
                return 1;
            } else if (queryContextStartIndex[1] == seg2.queryContextStartIndex[1]) {
                return 0;
            } else {
                return -1;
            }
        } else {
            return -1;
        }
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
}
