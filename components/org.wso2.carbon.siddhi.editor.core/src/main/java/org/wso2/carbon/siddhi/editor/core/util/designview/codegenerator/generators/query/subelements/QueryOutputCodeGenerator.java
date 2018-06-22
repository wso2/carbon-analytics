package org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator.generators.query.subelements;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.output.QueryOutputConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.output.types.DeleteOutputConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.output.types.InsertOutputConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.output.types.UpdateInsertIntoOutputConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.output.types.setattribute.SetAttributeConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator.generators.SubElementCodeGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.CodeGeneratorConstants;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.SiddhiCodeBuilderConstants;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.CodeGenerationException;

public class QueryOutputCodeGenerator {

    /**
     * Generates a Siddhi string representation of a QueryOutputConfig object
     *
     * @param queryOutput The QueryOutputConfig object to be converted
     * @return The converted string representation of the given QueryOutputConfig object
     * @throws CodeGenerationException Error while generating code
     */
    public static String generateQueryOutput(QueryOutputConfig queryOutput) throws CodeGenerationException {
        if (queryOutput == null) {
            throw new CodeGenerationException("A given query output element is empty");
        } else if (queryOutput.getType() == null || queryOutput.getType().isEmpty()) {
            throw new CodeGenerationException("The 'type' value of a given query output element is empty");
        }

        StringBuilder queryOutputStringBuilder = new StringBuilder();

        switch (queryOutput.getType().toUpperCase()) {
            case CodeGeneratorConstants.INSERT:
                InsertOutputConfig insertOutputConfig = (InsertOutputConfig) queryOutput.getOutput();
                queryOutputStringBuilder.append(generateInsertOutput(insertOutputConfig, queryOutput.getTarget()));
                break;
            case CodeGeneratorConstants.DELETE:
                DeleteOutputConfig deleteOutputConfig = (DeleteOutputConfig) queryOutput.getOutput();
                queryOutputStringBuilder.append(generateDeleteOutput(deleteOutputConfig, queryOutput.getTarget()));
                break;
            case CodeGeneratorConstants.UPDATE:
            case CodeGeneratorConstants.UPDATE_OR_INSERT_INTO:
                UpdateInsertIntoOutputConfig updateInsertIntoOutput =
                        (UpdateInsertIntoOutputConfig) queryOutput.getOutput();
                queryOutputStringBuilder.append(generateUpdateOutput(queryOutput.getType(),
                        updateInsertIntoOutput, queryOutput.getTarget()));
                break;
            default:
                throw new CodeGenerationException("Unidentified query output type: " + queryOutput.getType());
        }

        return queryOutputStringBuilder.toString();
    }

    /**
     * Generates a Siddhi string representation of a InsertOutputConfig object
     *
     * @param insertOutput The InsertOutputConfig object to be converted
     * @param target       The name of the target output definition
     * @return The Siddhi string representation of the InsertOutputConfig to respective target
     * @throws CodeGenerationException Error while generating code
     */
    private static String generateInsertOutput(InsertOutputConfig insertOutput, String target)
            throws CodeGenerationException {
        if (insertOutput == null) {
            throw new CodeGenerationException("A given insert query output element is empty");
        } else if (target == null || target.isEmpty()) {
            throw new CodeGenerationException("The 'target' value of a given query output element is empty");
        }

        StringBuilder insertOutputStringBuilder = new StringBuilder();

        insertOutputStringBuilder.append(SiddhiCodeBuilderConstants.INSERT)
                .append(SiddhiCodeBuilderConstants.SPACE);

        if (insertOutput.getEventType() != null && !insertOutput.getEventType().isEmpty()) {
            switch (insertOutput.getEventType().toUpperCase()) {
                case CodeGeneratorConstants.CURRENT_EVENTS:
                    insertOutputStringBuilder.append(SiddhiCodeBuilderConstants.CURRENT_EVENTS)
                            .append(SiddhiCodeBuilderConstants.SPACE);
                    break;
                case CodeGeneratorConstants.EXPIRED_EVENTS:
                    insertOutputStringBuilder.append(SiddhiCodeBuilderConstants.EXPIRED_EVENTS)
                            .append(SiddhiCodeBuilderConstants.SPACE);
                    break;
                case CodeGeneratorConstants.ALL_EVENTS:
                    insertOutputStringBuilder.append(SiddhiCodeBuilderConstants.ALL_EVENTS)
                            .append(SiddhiCodeBuilderConstants.SPACE);
                    break;
                default:
                    throw new CodeGenerationException("Unidentified insert query output event type: "
                            + insertOutput.getEventType());
            }
        }

        insertOutputStringBuilder.append(SiddhiCodeBuilderConstants.INTO)
                .append(SiddhiCodeBuilderConstants.SPACE)
                .append(target)
                .append(SiddhiCodeBuilderConstants.SEMI_COLON);

        return insertOutputStringBuilder.toString();
    }

    /**
     * Generates a Siddhi string representation of a DeleteOutputConfig object
     *
     * @param deleteOutput The DeleteOutputConfig object to be converted
     * @param target       The name of the target output definition
     * @return The Siddhi string representation of the DeleteOutputConfig object to the respective target
     * @throws CodeGenerationException Error while generating code
     */
    private static String generateDeleteOutput(DeleteOutputConfig deleteOutput, String target)
            throws CodeGenerationException {
        if (deleteOutput == null) {
            throw new CodeGenerationException("A given delete query output element is empty");
        } else if (deleteOutput.getOn() == null || deleteOutput.getOn().isEmpty()) {
            throw new CodeGenerationException("The 'on' statement of a given delete query" +
                    " output element is null/empty");
        } else if (target == null || target.isEmpty()) {
            throw new CodeGenerationException("The 'target' value of a given delete query output is empty");
        }

        return SiddhiCodeBuilderConstants.DELETE +
                SiddhiCodeBuilderConstants.SPACE +
                target +
                SubElementCodeGenerator.generateForEventType(deleteOutput.getEventType()) +
                SiddhiCodeBuilderConstants.SPACE +
                SiddhiCodeBuilderConstants.ON +
                SiddhiCodeBuilderConstants.SPACE +
                deleteOutput.getOn() +
                SiddhiCodeBuilderConstants.SEMI_COLON;
    }

    /**
     * Generates a Siddhi string representation of a UpdateInsertIntoOutputConfig object
     *
     * @param type                   The type of output object the one is (i.e. Either update|update or insert into)
     * @param updateInsertIntoOutput The UpdateInsertIntoConfig object to be converted
     * @param target                 The name of the target output definition
     * @return The Siddhi string representation of the UpdateInsertIntoOutputConfig object to the respective target
     * @throws CodeGenerationException Error while generating code
     */
    private static String generateUpdateOutput(String type, UpdateInsertIntoOutputConfig updateInsertIntoOutput,
                                               String target) throws CodeGenerationException {
        if (updateInsertIntoOutput == null) {
            throw new CodeGenerationException("A given update/insert query output element is empty");
        } else if (updateInsertIntoOutput.getOn() == null || updateInsertIntoOutput.getOn().isEmpty()) {
            throw new CodeGenerationException("The 'on' value of a given update/insert query" +
                    " element is empty");
        } else if (target == null || target.isEmpty()) {
            throw new CodeGenerationException("The 'target' value of a given update/insert" +
                    " query output element is empty");
        }

        StringBuilder updateInsertIntoOutputStringBuilder = new StringBuilder();
        if (type.equalsIgnoreCase(CodeGeneratorConstants.UPDATE)) {
            updateInsertIntoOutputStringBuilder.append(SiddhiCodeBuilderConstants.UPDATE);
        } else if (type.equalsIgnoreCase(CodeGeneratorConstants.UPDATE_OR_INSERT_INTO)) {
            updateInsertIntoOutputStringBuilder.append(SiddhiCodeBuilderConstants.UPDATE_OR_INSERT_INTO);
        }

        updateInsertIntoOutputStringBuilder.append(SiddhiCodeBuilderConstants.SPACE)
                .append(target)
                .append(SubElementCodeGenerator.generateForEventType(updateInsertIntoOutput.getEventType()))
                .append(SiddhiCodeBuilderConstants.SPACE);

        if (updateInsertIntoOutput.getSet() != null && !updateInsertIntoOutput.getSet().isEmpty()) {
            updateInsertIntoOutputStringBuilder.append(SiddhiCodeBuilderConstants.SET)
                    .append(SiddhiCodeBuilderConstants.SPACE);
            int setAttributesLeft = updateInsertIntoOutput.getSet().size();
            for (SetAttributeConfig setAttribute : updateInsertIntoOutput.getSet()) {
                updateInsertIntoOutputStringBuilder.append(generateSetAttribute(setAttribute));

                if (setAttributesLeft != 1) {
                    updateInsertIntoOutputStringBuilder.append(SiddhiCodeBuilderConstants.COMMA);
                }
                setAttributesLeft--;
            }
            updateInsertIntoOutputStringBuilder.append(SiddhiCodeBuilderConstants.SPACE);
        }

        updateInsertIntoOutputStringBuilder.append(SiddhiCodeBuilderConstants.ON)
                .append(SiddhiCodeBuilderConstants.SPACE)
                .append(updateInsertIntoOutput.getOn())
                .append(SiddhiCodeBuilderConstants.SEMI_COLON);

        return updateInsertIntoOutputStringBuilder.toString();
    }

    /**
     * Generates a Siddhi string representation of a SetAttributeConfig object
     *
     * @param setAttribute The given SetAttributeConfig object to be converted
     * @return The converted Siddhi string representation of the SetAttributeConfig object
     * @throws CodeGenerationException Error while generating code
     */
    private static String generateSetAttribute(SetAttributeConfig setAttribute) throws CodeGenerationException {
        if (setAttribute == null) {
            throw new CodeGenerationException("A given set attribute element given is empty");
        } else if (setAttribute.getAttribute() == null || setAttribute.getAttribute().isEmpty()) {
            throw new CodeGenerationException("The 'attribute' value of a given set attribute element is empty");
        } else if (setAttribute.getValue() == null || setAttribute.getValue().isEmpty()) {
            throw new CodeGenerationException("The 'value' attribute of a given set attribute element is empty");
        }

        return setAttribute.getAttribute() +
                SiddhiCodeBuilderConstants.SPACE +
                SiddhiCodeBuilderConstants.EQUAL +
                SiddhiCodeBuilderConstants.SPACE +
                setAttribute.getValue();
    }

}
