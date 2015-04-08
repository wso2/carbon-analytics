/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.analytics.dataservice.io.commons.beans;

import java.util.ArrayList;

/**
 * This class represents the index configuration bean which contains the index column definition and
 * scoring parameters for scoring function.
 */
public class IndexConfigurationBean {

    private IndexEntryBean[] indices;
    private String[] scoreParams;

    public IndexEntryBean[] getIndices() {
        if (indices == null) {
            return new ArrayList<IndexEntryBean>(0).toArray(new IndexEntryBean[0]);
        }
        return indices;
    }

    public void setIndices(IndexEntryBean[] indices) {
        this.indices = indices;
    }

    public String[] getScoreParams() {
        if (scoreParams == null) {
            return new ArrayList<String>(0).toArray(new String[0]);
        }
        return scoreParams;
    }

    public void setScoreParams(String[] scoreParams) {
        this.scoreParams = scoreParams;
    }

    @Override
    public String toString() {
        return "indices: " + indices.toString() + ", score params: " + scoreParams;
    }
}
