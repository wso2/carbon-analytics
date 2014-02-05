package org.wso2.carbon.event.output.adaptor.manager.admin.internal;/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * To group the output  event adaptor properties with their values - used to display the event adaptor details
 */
public class OutputEventAdaptorPropertiesDto {

    private OutputEventAdaptorPropertyDto[] outputEventAdaptorPropertyDtos;

    public OutputEventAdaptorPropertyDto[] getOutputEventAdaptorPropertyDtos() {
        return outputEventAdaptorPropertyDtos;
    }

    public void setOutputEventAdaptorPropertyDtos(
            OutputEventAdaptorPropertyDto[] outputEventAdaptorPropertyDtos) {
        this.outputEventAdaptorPropertyDtos = outputEventAdaptorPropertyDtos;
    }
}
