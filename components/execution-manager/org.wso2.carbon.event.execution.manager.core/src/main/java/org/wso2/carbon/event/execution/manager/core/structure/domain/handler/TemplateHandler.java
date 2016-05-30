package org.wso2.carbon.event.execution.manager.core.structure.domain.handler;

import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.annotation.DomHandler;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;

public class TemplateHandler implements DomHandler<String, StreamResult> {

    private StringWriter xmlWriter = new StringWriter();

    @Override
    public StreamResult createUnmarshaller(ValidationEventHandler errorHandler) {
        return new StreamResult(xmlWriter);
    }

    @Override
    public String getElement(StreamResult rt) {
        return rt.getWriter().toString().trim();
    }

    @Override
    public Source marshal(String xml, ValidationEventHandler errorHandler) {
        StringReader xmlReader = new StringReader(xml.trim());
        return new StreamSource(xmlReader);
    }
}
