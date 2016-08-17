package org.kurator.yaml;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Actor config
 */
@JsonTypeName(value = "PythonActor")
public class ActorConfig extends YamlComponent {

    private ActorProperties properties;

    public ActorProperties getProperties() {
        return properties;
    }

    public void setProperties(ActorProperties properties) {
        this.properties = properties;
    }

}
