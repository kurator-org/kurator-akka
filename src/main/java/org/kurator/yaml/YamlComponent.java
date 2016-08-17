package org.kurator.yaml;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Yaml component base class
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ActorConfig.class, name = "PythonActor"),
        @JsonSubTypes.Type(value = WorkflowConfig.class, name = "Workflow")
})
public abstract class YamlComponent {

}
