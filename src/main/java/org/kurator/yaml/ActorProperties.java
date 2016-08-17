package org.kurator.yaml;

import java.util.List;
import java.util.Map;

/**
 * Actor properties
 */
public class ActorProperties {

    private String code;
    private String module;
    private String onData;
    private Map<String, Object> inputs;
    private Map<String, Object> parameters;
    private Object listensTo;
    private List<Object> actors;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getOnData() {
        return onData;
    }

    public void setOnData(String onData) {
        this.onData = onData;
    }

    public Map<String, Object> getInputs() {
        return inputs;
    }

    public void setInputs(Map<String, Object> inputs) {
        this.inputs = inputs;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public Object getListensTo() {
        return listensTo;
    }

    public void setListensTo(Object listensTo) {
        this.listensTo = listensTo;
    }

    public List<Object> getActors() {
        return actors;
    }

    public void setActors(List<Object> actors) {
        this.actors = actors;
    }

}
