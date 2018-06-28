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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Preserves Comments of major elements, that reside in the outer scope of Siddhi app
 */
public class OuterScopeCommentsPreserver extends ScopedCommentsPreserver {

    public OuterScopeCommentsPreserver(String siddhiAppString, Set<ElementCodeSegment> elementCodeSegments) {
        super(siddhiAppString, new ArrayList<>(elementCodeSegments));
    }

    @Override
    public List<CommentCodeSegment> generateCommentCodeSegments() throws DesignGenerationException {
        Collections.sort(elementCodeSegments);
        elementCodeSegments = filterMajorElementCodeSegments(elementCodeSegments);
        commentCodeSegments = detectCommentCodeSegments(elementCodeSegments);
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
}
