package org.kurator.yaml;

import java.util.List;
import java.util.Map;

/**
 * Workflow properties
 */
public class WorkflowProperties {

    private List<Object> actors;
    private Map<String, WorkflowParameter> parameters;

    public List<Object> getActors() {
        return actors;
    }

    public void setActors(List<Object> actors) {
        this.actors = actors;
    }

    public Map<String, WorkflowParameter> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, WorkflowParameter> parameters) {
        this.parameters = parameters;
    }

}
