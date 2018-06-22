package org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator.generators.query.subelements;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.attributesselection.AttributesSelectionConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.attributesselection.SelectedAttribute;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.attributesselection.UserDefinedSelectionConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.AttributeSelection;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.SiddhiCodeBuilderConstants;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.CodeGenerationException;

public class QuerySelectCodeGenerator {

    /**
     * Generates a Siddhi string representation of an AttributesSelectionConfig object
     * Example For Query Select - select * ...
     *
     * @param attributesSelection The AttributesSelectionConfig object to be converted
     * @return The converted Siddhi string representation of the given AttributesSelectionConfig object
     * @throws CodeGenerationException Error while generating code
     */
    public static String generateQuerySelect(AttributesSelectionConfig attributesSelection) throws CodeGenerationException {
        if (attributesSelection == null) {
            throw new CodeGenerationException("A given attribute selection element is empty");
        }

        StringBuilder attributesSelectionStringBuilder = new StringBuilder();

        attributesSelectionStringBuilder.append(SiddhiCodeBuilderConstants.SELECT)
                .append(SiddhiCodeBuilderConstants.SPACE);

        if (attributesSelection.getType() == null || attributesSelection.getType().isEmpty()) {
            throw new CodeGenerationException("The 'type' value of a given attribute selection element is empty");
        }

        switch (attributesSelection.getType().toUpperCase()) {
            case AttributeSelection.TYPE_USER_DEFINED:
                UserDefinedSelectionConfig userDefinedSelection = (UserDefinedSelectionConfig) attributesSelection;
                attributesSelectionStringBuilder.append(generateUserDefinedSelection(userDefinedSelection));
                break;
            case AttributeSelection.TYPE_ALL:
                attributesSelectionStringBuilder.append(SiddhiCodeBuilderConstants.ALL);
                break;
            default:
                throw new CodeGenerationException("Undefined attribute selection type:"
                        + attributesSelection.getType());
        }

        return attributesSelectionStringBuilder.toString();
    }

    /**
     * Generates a Siddhi string representation of a UserDefinedSelectionConfig object
     * Example of the UserDefinedSelectionConfig - select name, avg(age) as avgAge, ...
     *
     * @param userDefinedSelection The UserDefinedSelectionConfig object to be converted
     * @return The Siddhi string representation of the given UserDefinedSelectionConfig
     * @throws CodeGenerationException Error while generating code
     */
    private static String generateUserDefinedSelection(UserDefinedSelectionConfig userDefinedSelection)
            throws CodeGenerationException {
        if (userDefinedSelection == null || userDefinedSelection.getValue() == null ||
                userDefinedSelection.getValue().isEmpty()) {
            throw new CodeGenerationException("A given user defined selection value is empty");
        }

        StringBuilder userDefinedSelectionStringBuilder = new StringBuilder();

        int attributesLeft = userDefinedSelection.getValue().size();
        for (SelectedAttribute attribute : userDefinedSelection.getValue()) {
            if (attribute.getExpression() == null || attribute.getExpression().isEmpty()) {
                throw new CodeGenerationException("The 'expression' value of a given select" +
                        " attribute element is empty");
            }
            userDefinedSelectionStringBuilder.append(attribute.getExpression());
            if (attribute.getAs() != null && !attribute.getAs().isEmpty() &&
                    !attribute.getAs().equals(attribute.getExpression())) {
                userDefinedSelectionStringBuilder.append(SiddhiCodeBuilderConstants.SPACE)
                        .append(SiddhiCodeBuilderConstants.AS)
                        .append(SiddhiCodeBuilderConstants.SPACE)
                        .append(attribute.getAs());
            }
            if (attributesLeft != 1) {
                userDefinedSelectionStringBuilder.append(SiddhiCodeBuilderConstants.COMMA);
            }
            attributesLeft--;
        }

        return userDefinedSelectionStringBuilder.toString();
    }

}
