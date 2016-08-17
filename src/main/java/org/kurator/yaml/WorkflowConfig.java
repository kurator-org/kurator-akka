package org.kurator.yaml;

/**
 * Workflow config
 */
public class WorkflowConfig extends YamlComponent {

    private WorkflowProperties properties;

    public WorkflowProperties getProperties() {
        return properties;
    }

    public void setProperties(WorkflowProperties properties) {
        this.properties = properties;
    }

}
