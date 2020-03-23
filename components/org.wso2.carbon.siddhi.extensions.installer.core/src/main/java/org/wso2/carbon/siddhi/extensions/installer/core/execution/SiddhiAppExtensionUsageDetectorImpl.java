/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.siddhi.extensions.installer.core.execution;

import org.wso2.carbon.siddhi.extensions.installer.core.config.mapping.models.ExtensionConfig;
import org.wso2.carbon.siddhi.extensions.installer.core.config.mapping.models.ExtensionIdentifierConfig;
import org.wso2.carbon.siddhi.extensions.installer.core.exceptions.ExtensionsInstallerException;
import org.wso2.carbon.siddhi.extensions.installer.core.models.SiddhiAppExtensionUsage;
import org.wso2.carbon.siddhi.extensions.installer.core.util.ResponseEntityCreator;
import org.wso2.carbon.siddhi.extensions.installer.core.util.SiddhiAppUsageExtractor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Detects extensions that have been used in Siddhi apps.
 */
public class SiddhiAppExtensionUsageDetectorImpl implements SiddhiAppExtensionUsageDetector {

    private static final String TYPE_KEY = "type";
    private static final String NAME_KEY = "name";

    /**
     * Extension configurations, denoted by extension Id.
     */
    private final Map<String, ExtensionConfig> extensionConfigs;

    public SiddhiAppExtensionUsageDetectorImpl(Map<String, ExtensionConfig> extensionConfigs) {
        this.extensionConfigs = extensionConfigs;
    }

    @Override
    public Map<String, Map<String, Object>> getUsedExtensionStatuses(String siddhiAppString)
        throws ExtensionsInstallerException {
        List<SiddhiAppExtensionUsage> usages = SiddhiAppUsageExtractor.extractUsages(siddhiAppString);
        Map<String, Map<String, Object>> usedExtensionStatuses = new HashMap<>();
        DependencyRetriever dependencyRetriever = new DependencyRetrieverImpl(extensionConfigs);
        for (SiddhiAppExtensionUsage usage : usages) {
            ExtensionConfig usedExtension = detectUsedExtension(usage);
            if (usedExtension != null) {
                String extensionId = usedExtension.getExtensionInfo().get(NAME_KEY);
                if (extensionId != null) {
                    ResponseEntityCreator.addUsedExtensionStatusResponse(
                        usage,
                        extensionId,
                        dependencyRetriever.getExtensionStatus(usedExtension),
                        usedExtensionStatuses);
                }
            }
        }
        return usedExtensionStatuses;
    }

    /**
     * Matches and returns the extension which has been used by the given Siddhi app extension usage.
     * Extension is matched in the following order:
     * 1. Match by unique attribute value (if specified).
     * 2. Match by regex of a unique attribute value (if specified).
     * 3. Match by alternative type (if specified).
     * 4. Match by name.
     *
     * @param usage Extension usage in a Siddhi app.
     * @return Extension config object when a match is found, otherwise null.
     */
    private ExtensionConfig detectUsedExtension(SiddhiAppExtensionUsage usage) {
        return matchByUniqueAttributeValue(usage)
            .orElse(matchByUniqueAttributeValueRegex(usage)
                .orElse(matchByAlternativeType(usage)
                    .orElse(matchByName(usage)
                        .orElse(null))));
    }

    /**
     * Returns an optional extension config object,
     * when the given usage matches an extension by a unique attribute's value,
     * as specified in the extension's {@link ExtensionIdentifierConfig}.
     * Extensions of different variations, that share the same value for 'type' in Siddhi,
     * but are identifiable based on a unique attribute's value fall under this category.
     * Eg:
     * - type='rdbms' (common).
     * - jdbc.driver.name='com.mysql.jdbc.Driver' for MySQL.
     * - jdbc.driver.name='org.postgresql.Driver' for Postgres.
     *
     * @param usage Extension usage in a Siddhi app.
     * @return Optional Extension config object if found.
     */
    private Optional<ExtensionConfig> matchByUniqueAttributeValue(SiddhiAppExtensionUsage usage) {
        Optional<Map.Entry<String, ExtensionConfig>> matchedExtensionEntry =
            extensionConfigs.entrySet().stream()
                .filter(extension -> {
                    ExtensionIdentifierConfig extensionIdentifier = extension.getValue().getIdentifier();
                    if (extensionIdentifier != null && extensionIdentifier.isUniqueAttributeValueValid()) {
                        String uniqueAttribute = extensionIdentifier.getUniqueAttribute();
                        String uniqueAttributeValue = extensionIdentifier.getUniqueAttributeValue();
                        return (Objects.equals(usage.getProperties().get(TYPE_KEY), extensionIdentifier.getType()) &&
                            Objects.equals(usage.getProperties().get(uniqueAttribute), uniqueAttributeValue));
                    }
                    return false;
                }).findFirst();
        return matchedExtensionEntry.map(Map.Entry::getValue);
    }

    /**
     * Returns an optional extension config object,
     * when the given usage matches an extension by the regex of a unique attribute's value,
     * as specified in the extension's {@link ExtensionIdentifierConfig}.
     * Extensions of different variations, that share the same value for 'type' in Siddhi,
     * but are identifiable based on the regex of a unique attribute's value fall under this category.
     * Eg:
     * - type='cdc' (common).
     * - url='jdbc:mysql://...' for MySQL.
     * - url='jdbc:postgresql://...' for Postgres.
     *
     * @param usage Extension usage in a Siddhi app.
     * @return Optional Extension config object if found.
     */
    private Optional<ExtensionConfig> matchByUniqueAttributeValueRegex(SiddhiAppExtensionUsage usage) {
        Optional<Map.Entry<String, ExtensionConfig>> matchedExtensionEntry =
            extensionConfigs.entrySet().stream()
                .filter(extension -> {
                    ExtensionIdentifierConfig extensionIdentifier = extension.getValue().getIdentifier();
                    if (extensionIdentifier != null && extensionIdentifier.isUniqueAttributeValueRegexValid()) {
                        String uniqueAttribute = extensionIdentifier.getUniqueAttribute();
                        String uniqueAttributeValueRegex = extensionIdentifier.getUniqueAttributeValueRegex();
                        return (
                            Objects.equals(usage.getProperties().get(TYPE_KEY), extensionIdentifier.getType()) &&
                                usage.getProperties().get(uniqueAttribute) != null &&
                                usage.getProperties().get(uniqueAttribute).matches(uniqueAttributeValueRegex));
                    }
                    return false;
                }).findFirst();
        return matchedExtensionEntry.map(Map.Entry::getValue);
    }

    /**
     * Returns an optional extension config object,
     * when the given usage matches an extension by an alternative type, as specified in the extension's
     * {@link ExtensionIdentifierConfig}.
     * Extensions such that, the same extension is denoted by different values for 'type' in Siddhi
     * fall under this category.
     * Eg:
     * All of the following denote the 'http' extension.
     * - type='http'.
     * - type='http-service-response'.
     * - type='http-call-response'.
     *
     * @param usage Extension usage in a Siddhi app.
     * @return Optional Extension config object if found.
     */
    private Optional<ExtensionConfig> matchByAlternativeType(SiddhiAppExtensionUsage usage) {
        Optional<Map.Entry<String, ExtensionConfig>> matchedExtensionEntry =
            extensionConfigs.entrySet().stream()
                .filter(extension -> {
                    ExtensionIdentifierConfig extensionIdentifier = extension.getValue().getIdentifier();
                    if (extensionIdentifier != null && extensionIdentifier.isAlternativeTypeValid()) {
                        return extensionIdentifier.getAlternativeTypes().contains(usage.getProperties().get(TYPE_KEY));
                    }
                    return false;
                }).findFirst();
        return matchedExtensionEntry.map(Map.Entry::getValue);
    }

    /**
     * Returns an optional extension config object,
     * when the given usage's 'type' is same as the extension's name.
     * Extensions that don't have a special {@link ExtensionIdentifierConfig} will be matched by name.
     * Eg: type='ibmmq' matches the extension with 'ibmmq' as its name.
     *
     * @param usage Extension usage in a Siddhi app.
     * @return Optional Extension config object if found.
     */
    private Optional<ExtensionConfig> matchByName(SiddhiAppExtensionUsage usage) {
        Optional<Map.Entry<String, ExtensionConfig>> matchedExtensionEntry =
            extensionConfigs.entrySet().stream()
                .filter(extension -> {
                    String extensionName = extension.getValue().getExtensionInfo().get(NAME_KEY);
                    return (extensionName != null &&
                        Objects.equals(extensionName, usage.getProperties().get(TYPE_KEY)));
                }).findFirst();
        return matchedExtensionEntry.map(Map.Entry::getValue);
    }

}
