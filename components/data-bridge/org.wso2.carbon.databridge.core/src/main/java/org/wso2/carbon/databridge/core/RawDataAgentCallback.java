/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.databridge.core;

import org.wso2.carbon.databridge.commons.StreamDefinition;

/**
 * RawData Agent Callback
 */
public interface RawDataAgentCallback {

    /**
     * will get called  when types are defined
     *
     * @param streamDefinition TypeDefinition of event streams
     */
    void definedStream(StreamDefinition streamDefinition);

    /**
     * will get called  when types are removed
     *
     * @param streamDefinition TypeDefinition of event streams
     */
    void removeStream(StreamDefinition streamDefinition);

    /**
     * will get called when Events arrive
     *
     * @param eventComposite Event Composite
     */
    void receive(Object eventComposite);

}
