package org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator.generators;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.TableConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.SiddhiCodeBuilderConstants;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.CodeGenerationException;

public class TableCodeGenerator {
    /**
     * Generates a table definition string from a TableConfig object
     *
     * @param table The TableConfig object to be converted
     * @return The converted table definition string
     * @throws CodeGenerationException Error while generating code
     */
    public String generateTable(TableConfig table) throws CodeGenerationException {
        if (table == null) {
            throw new CodeGenerationException("A given table element is empty");
        } else if (table.getName() == null || table.getName().isEmpty()) {
            throw new CodeGenerationException("The name of a given table element is empty");
        }

        return SubElementCodeGenerator.generateStore(table.getStore()) +
                SubElementCodeGenerator.generateAnnotations(table.getAnnotationList()) +
                SiddhiCodeBuilderConstants.DEFINE_TABLE +
                SiddhiCodeBuilderConstants.SPACE +
                table.getName() +
                SiddhiCodeBuilderConstants.SPACE +
                SiddhiCodeBuilderConstants.OPEN_BRACKET +
                SubElementCodeGenerator.generateAttributes(table.getAttributeList()) +
                SiddhiCodeBuilderConstants.CLOSE_BRACKET +
                SiddhiCodeBuilderConstants.SEMI_COLON;
    }

}
