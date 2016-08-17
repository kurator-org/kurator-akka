package org.kurator.yaml;

/**
 * Workflow config
 */
public class WorkflowConfig extends YamlComponent {

    private String id;
    private String type;
    private WorkflowProperties properties;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public WorkflowProperties getProperties() {
        return properties;
    }

    public void setProperties(WorkflowProperties properties) {
        this.properties = properties;
    }

}
