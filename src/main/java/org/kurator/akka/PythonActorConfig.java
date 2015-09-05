package org.kurator.akka;

public class PythonActorConfig extends ActorConfig {
    public void setCode(String value)           { config.put("code", value); }     
    public void setOnData(String value)         { config.put("onData", value); }
    public void setOnStart(String value)        { config.put("onStart", value); }
    public void setPythonClass(String value)    { config.put("pythonClass", value); }
    public void setScript(String value)         { config.put("script", value); }
}
