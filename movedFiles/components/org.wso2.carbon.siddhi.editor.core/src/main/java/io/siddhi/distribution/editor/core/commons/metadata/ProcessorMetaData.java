/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.siddhi.distribution.editor.core.commons.metadata;

import java.util.List;

/**
 * For storing Processor and ExpressionExecutor related meta data
 * Used in JSON responses.
 */
public class ProcessorMetaData {

    private String name;
    private String namespace;
    private List<ExampleMetaData> examples;
    private String description;
    private List<ParameterMetaData> parameters;
    private List<String[]> parameterOverloads;
    private List<AttributeMetaData> returnAttributes;
    private List<SyntaxMetaData> syntax;
    private String type;

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getNamespace() {

        return namespace;
    }

    public void setNamespace(String namespace) {

        this.namespace = namespace;
    }

    public List<ExampleMetaData> getExamples() {
        return examples;
    }

    public void setExamples(List<ExampleMetaData> examples) {
        this.examples = examples;
    }

    public String getDescription() {

        return description;
    }

    public void setDescription(String description) {

        this.description = description;
    }

    public List<ParameterMetaData> getParameters() {

        return parameters;
    }

    public void setParameters(List<ParameterMetaData> parameters) {

        this.parameters = parameters;
    }

    public void setReturnAttributes(List<AttributeMetaData> returnAttributes) {

        this.returnAttributes = returnAttributes;
    }

    public List<AttributeMetaData> getReturnAttributes() {

        return returnAttributes;
    }

    public void setParameterOverloads(List<String[]> parameterOverloads) {

        this.parameterOverloads = parameterOverloads;
    }

    public List<String[]> getParameterOverloads() {

        return parameterOverloads;
    }

    public List<SyntaxMetaData> getSyntax() {
        return syntax;
    }

    public void setSyntax(List<SyntaxMetaData> syntax) {
        this.syntax = syntax;
    }

    public String getType() {
        return type;

    }

    public void setType(String type) {
        this.type = type;
    }

}

