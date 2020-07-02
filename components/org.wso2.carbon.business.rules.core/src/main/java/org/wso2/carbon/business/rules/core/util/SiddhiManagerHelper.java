package org.wso2.carbon.business.rules.core.util;

import org.wso2.carbon.business.rules.core.exceptions.SiddhiManagerHelperException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SiddhiManagerHelper {
    /**
     * To avoid instantiation
     */
    private SiddhiManagerHelper() {

    }

    /**
     * Gives the name of the given siddhiApp,
     *
     * @param siddhiApp
     * @return String
     * @throws SiddhiManagerHelperException
     */
    public static String getSiddhiAppName(Object siddhiApp) throws SiddhiManagerHelperException {

        // Regex match and find name
        Pattern siddhiAppNamePattern = Pattern.compile(TemplateManagerConstants.SIDDHI_APP_NAME_REGEX_PATTERN);
        Matcher siddhiAppNameMatcher = siddhiAppNamePattern.matcher(siddhiApp.toString());
        if (siddhiAppNameMatcher.find()) {
            return siddhiAppNameMatcher.group(2);
        }
        throw new SiddhiManagerHelperException("Invalid SiddhiApp Name Found");
    }
}
