package org.wso2.carbon.stream.processor.template.manager.core.util;

/**
 * Consists of constants related to root.Template Manager
 */
public class TemplateManagerConstants {
    // Directory locations
    public static final String TEMPLATES_DIRECTORY = "/home/senthuran/Desktop/rough-templates/"; // todo: not finalized
    public static final String BUSINESS_RULES_DIRECTORY = "/home/senthuran/Desktop/rough-templates/BR/"; // todo: not finalized

    // Pattern of a templated element in Templates
    public static final String TEMPLATED_ELEMENT_REGEX_PATTERN = "(\\$\\{[^}]+\\})"; // templateName
    public static final String TEMPLATED_ELEMENT_START = "${";
    public static final String TEMPLATED_ELEMENT_END = "}";
    public static final String TEMPLATED_ELEMENT_NAME_REGEX_PATTERN = "\\$\\{(\\S+)\\}"; // to extract element name, from [element with template pattern]

    // Pattern of a SiddhiApp name
    public static final String SIDDHI_APP_NAME_REGEX_PATTERN = "@Plan:name(['\\\"])";

}
