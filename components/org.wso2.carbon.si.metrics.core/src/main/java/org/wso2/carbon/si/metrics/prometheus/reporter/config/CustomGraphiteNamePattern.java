package org.wso2.carbon.si.metrics.prometheus.reporter.config;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomGraphiteNamePattern {

    private Pattern pattern;
    private String patternStr;

    /**
     * Creates a new GraphiteNamePattern from the given simplified glob pattern.
     *
     * @param pattern The glob style pattern to be used.
     */
    CustomGraphiteNamePattern(final String pattern) {
        initializePattern(pattern);
    }

    /**
     * Matches the metric name against the pattern.
     *
     * @param metricName The metric name to be tested.
     * @return {@code true} if the name is matched, {@code false} otherwise.
     */
    boolean matches(final String metricName) {
        return metricName != null && pattern.matcher(metricName).matches();
    }

    /**
     * Extracts parameters from the given metric name based on the pattern.
     * The resulting map has keys named as '${n}' where n is the 0 based position in the pattern.
     * E.g.:
     * pattern: org.test.controller.*.status.*
     * extractParameters("org.test.controller.gather.status.400") ->
     * {${0} -> "gather", ${1} -> "400"}
     *
     * @param metricName The metric name to extract parameters from.
     * @return A parameter map where keys are named '${n}' where n is 0 based parameter position in the pattern.
     */
    Map<String, String> extractParameters(final String metricName) {
        final Matcher matcher = this.pattern.matcher(metricName);
        final Map<String, String> params = new HashMap<String, String>();
        if (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                params.put(String.format("${%d}", i - 1), matcher.group(i));
            }
        }

        return params;
    }

    /**
     * Turns the GLOB pattern into a REGEX.
     *
     * @param pattern The pattern to use
     */
    private void initializePattern(String pattern) {
        pattern = pattern.replace("(.*)", "~"); //replace (.*) to avoid replacing * by ([^.]*)
        final String[] split = pattern.split(Pattern.quote("*"), -1);
        final StringBuilder escapedPattern = new StringBuilder(Pattern.quote(split[0]));
        for (int i = 1; i < split.length; i++) {
            String quoted = Pattern.quote(split[i]);
            escapedPattern.append("([^.]*)").append(quoted);
        }

        final String regex = "^" + escapedPattern.toString() + "$";
        this.patternStr = regex.replace("~", "\\E(.*)\\Q");
        this.pattern = Pattern.compile(patternStr);
    }

    String getPatternString() {
        return this.patternStr;
    }
}
