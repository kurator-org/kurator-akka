package org.kurator.akka;

public class YWActorConfig extends ActorConfig {
    public void setOnInit(String value)         { config.put("onInit", value); }
    public void setOnData(String value)         { config.put("onData", value); }
    public void setOnStart(String value)        { config.put("onStart", value); }
    public void setOnEnd(String value)          { config.put("onEnd", value); }
}
