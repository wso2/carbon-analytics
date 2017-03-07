package org.wso2.carbon.event.template.manager.core.structure.domain;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class Scenarios {
    private List<Scenario> scenario;

    public List<Scenario> getScenario() {
        return scenario;
    }

    public void setScenario(List<Scenario> scenario) {
        this.scenario = scenario;
    }
}
