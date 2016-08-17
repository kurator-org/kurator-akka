package org.kurator.yaml;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Actor config
 */
@JsonTypeName(value = "PythonActor")
public class ActorConfig extends YamlComponent {

    private String id;
    private String type;
    private ActorProperties properties;

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

    public ActorProperties getProperties() {
        return properties;
    }

    public void setProperties(ActorProperties properties) {
        this.properties = properties;
    }

}
