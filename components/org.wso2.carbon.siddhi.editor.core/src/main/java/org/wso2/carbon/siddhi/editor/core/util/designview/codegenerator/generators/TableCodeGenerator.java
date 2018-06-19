package org.wso2.carbon.siddhi.editor.core.util.designview.codegenerator.generators;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.TableConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.SiddhiStringBuilderConstants;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.CodeGenerationException;
import org.wso2.carbon.siddhi.editor.core.util.designview.utilities.CodeGeneratorHelper;

public class TableCodeGenerator {

    /**
     * Generates a table definition string from a TableConfig object
     *
     * @param table The TableConfig object to be converted
     * @return The converted table definition string
     * @throws CodeGenerationException Error while generating code
     */
    public String generateTableCode(TableConfig table) throws CodeGenerationException {
        if (table == null) {
            throw new CodeGenerationException("A given table element is empty");
        } else if (table.getName() == null || table.getName().isEmpty()) {
            throw new CodeGenerationException("The name of a given table element is empty");
        }

        return CodeGeneratorHelper.getStore(table.getStore()) +
                CodeGeneratorHelper.getAnnotations(table.getAnnotationList()) +
                SiddhiStringBuilderConstants.DEFINE_TABLE +
                SiddhiStringBuilderConstants.SPACE +
                table.getName() +
                SiddhiStringBuilderConstants.SPACE +
                SiddhiStringBuilderConstants.OPEN_BRACKET +
                CodeGeneratorHelper.getAttributes(table.getAttributeList()) +
                SiddhiStringBuilderConstants.CLOSE_BRACKET +
                SiddhiStringBuilderConstants.SEMI_COLON;
    }


}
